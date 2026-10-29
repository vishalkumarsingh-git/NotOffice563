package com.programmingblitz.notoffice563.document.evaluator

object SpreadsheetFormulaEvaluator {

    /**
     * Evaluates a formula string (e.g. "=A1+B1", "=SUM(A1:B2)", "=AVERAGE(A1:A10)", "=MIN(B1:B5)", "=MAX(C1:C8)")
     * against a 2D sheet rows representation.
     * Returns the evaluated numeric or text result, or error message (e.g. "#VALUE!", "#DIV/0!").
     */
    fun evaluate(formula: String, rows: List<List<String>>): String {
        val trimmed = formula.trim()
        if (!trimmed.startsWith("=")) return trimmed
        val expression = trimmed.substring(1).trim()
        if (expression.isEmpty()) return ""

        return try {
            evalExpression(expression, rows)
        } catch (_: Exception) {
            "#ERROR!"
        }
    }

    private fun evalExpression(expr: String, rows: List<List<String>>): String {
        val upper = expr.uppercase().trim()

        // 1. Check Function calls: SUM, AVERAGE, MIN, MAX, COUNT
        if (upper.startsWith("SUM(") && upper.endsWith(")")) {
            val inner = expr.substring(4, expr.length - 1)
            val numbers = collectNumbersFromArg(inner, rows)
            val sum = numbers.sum()
            return formatNumber(sum)
        }
        if (upper.startsWith("AVERAGE(") && upper.endsWith(")")) {
            val inner = expr.substring(8, expr.length - 1)
            val numbers = collectNumbersFromArg(inner, rows)
            if (numbers.isEmpty()) return "#DIV/0!"
            val avg = numbers.average()
            return formatNumber(avg)
        }
        if (upper.startsWith("MIN(") && upper.endsWith(")")) {
            val inner = expr.substring(4, expr.length - 1)
            val numbers = collectNumbersFromArg(inner, rows)
            if (numbers.isEmpty()) return "0"
            return formatNumber(numbers.minOrNull() ?: 0.0)
        }
        if (upper.startsWith("MAX(") && upper.endsWith(")")) {
            val inner = expr.substring(4, expr.length - 1)
            val numbers = collectNumbersFromArg(inner, rows)
            if (numbers.isEmpty()) return "0"
            return formatNumber(numbers.maxOrNull() ?: 0.0)
        }
        if (upper.startsWith("COUNT(") && upper.endsWith(")")) {
            val inner = expr.substring(6, expr.length - 1)
            val numbers = collectNumbersFromArg(inner, rows)
            return numbers.size.toString()
        }

        // 2. Arithmetic expressions with +, -, *, /
        return evalArithmetic(upper, rows)
    }

    private fun evalArithmetic(expr: String, rows: List<List<String>>): String {
        // Resolve cell references in the expression first (e.g. A1, B2)
        val cellRegex = Regex("[A-Z]+[0-9]+")
        val resolvedExpr = cellRegex.replace(expr) { match ->
            val ref = match.value
            val cellVal = getCellValueByRef(ref, rows)
            val num = cellVal.toDoubleOrNull() ?: 0.0
            num.toString()
        }

        val result = evaluateMathString(resolvedExpr)
        return formatNumber(result)
    }

    private fun evaluateMathString(str: String): Double {
        val terms = mutableListOf<String>()
        val ops = mutableListOf<Char>()
        var current = StringBuilder()
        var i = 0
        while (i < str.length) {
            val c = str[i]
            if ((c == '+' || c == '-') && current.isNotEmpty() && str[i - 1] != '*' && str[i - 1] != '/') {
                terms.add(current.toString().trim())
                ops.add(c)
                current = StringBuilder()
            } else {
                current.append(c)
            }
            i++
        }
        if (current.isNotEmpty()) terms.add(current.toString().trim())

        var total = evalMulDiv(terms.firstOrNull() ?: "0")
        for (idx in ops.indices) {
            val nextVal = evalMulDiv(terms[idx + 1])
            if (ops[idx] == '+') total += nextVal
            else total -= nextVal
        }
        return total
    }

    private fun evalMulDiv(term: String): Double {
        val parts = mutableListOf<String>()
        val ops = mutableListOf<Char>()
        var current = StringBuilder()
        for (c in term) {
            if (c == '*' || c == '/') {
                parts.add(current.toString().trim())
                ops.add(c)
                current = StringBuilder()
            } else {
                current.append(c)
            }
        }
        if (current.isNotEmpty()) parts.add(current.toString().trim())

        var result = parts.firstOrNull()?.toDoubleOrNull() ?: 0.0
        for (idx in ops.indices) {
            val next = parts[idx + 1].toDoubleOrNull() ?: 0.0
            if (ops[idx] == '*') result *= next
            else {
                if (next == 0.0) throw ArithmeticException("Divide by zero")
                result /= next
            }
        }
        return result
    }

    private fun collectNumbersFromArg(arg: String, rows: List<List<String>>): List<Double> {
        val numbers = mutableListOf<Double>()
        val tokens = arg.split(",", ";").map { it.trim() }
        for (token in tokens) {
            if (token.contains(":")) {
                val rangeParts = token.split(":")
                if (rangeParts.size == 2) {
                    val startRef = rangeParts[0].trim().uppercase()
                    val endRef = rangeParts[1].trim().uppercase()
                    numbers.addAll(getCellsInRange(startRef, endRef, rows))
                }
            } else {
                val num = if (token.matches(Regex("[A-Z]+[0-9]+", RegexOption.IGNORE_CASE))) {
                    getCellValueByRef(token.uppercase(), rows).toDoubleOrNull()
                } else {
                    token.toDoubleOrNull()
                }
                if (num != null) numbers.add(num)
            }
        }
        return numbers
    }

    private fun getCellsInRange(startRef: String, endRef: String, rows: List<List<String>>): List<Double> {
        val startCol = colToIndex(startRef.filter { it.isLetter() })
        val startRow = (startRef.filter { it.isDigit() }.toIntOrNull() ?: 1) - 1
        val endCol = colToIndex(endRef.filter { it.isLetter() })
        val endRow = (endRef.filter { it.isDigit() }.toIntOrNull() ?: 1) - 1

        val minR = minOf(startRow, endRow).coerceAtLeast(0)
        val maxR = maxOf(startRow, endRow)
        val minC = minOf(startCol, endCol).coerceAtLeast(0)
        val maxC = maxOf(startCol, endCol)

        val list = mutableListOf<Double>()
        for (r in minR..maxR) {
            if (r >= rows.size) break
            val row = rows[r]
            for (c in minC..maxC) {
                if (c < row.size) {
                    val cell = row[c]
                    val evaluated = if (cell.startsWith("=")) evaluate(cell, rows) else cell
                    val num = evaluated.toDoubleOrNull()
                    if (num != null) list.add(num)
                }
            }
        }
        return list
    }

    fun getCellValueByRef(ref: String, rows: List<List<String>>): String {
        val col = colToIndex(ref.filter { it.isLetter() })
        val row = (ref.filter { it.isDigit() }.toIntOrNull() ?: 1) - 1
        if (row in rows.indices) {
            val cell = rows[row].getOrNull(col) ?: ""
            if (cell.startsWith("=")) {
                return evaluate(cell, rows)
            }
            return cell
        }
        return ""
    }

    private fun colToIndex(colLetters: String): Int {
        var result = 0
        for (char in colLetters.uppercase()) {
            if (char in 'A'..'Z') {
                result = result * 26 + (char - 'A' + 1)
            }
        }
        return (result - 1).coerceAtLeast(0)
    }

    private fun formatNumber(num: Double): String {
        return if (num % 1.0 == 0.0 && !num.isInfinite() && !num.isNaN()) {
            num.toLong().toString()
        } else {
            String.format(java.util.Locale.US, "%.2f", num)
        }
    }
}
