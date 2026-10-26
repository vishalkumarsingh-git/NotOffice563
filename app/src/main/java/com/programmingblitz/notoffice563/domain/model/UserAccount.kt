package com.programmingblitz.notoffice563.domain.model

/**
 * Represents the authenticated user account returned by the identity provider (Google).
 * If unauthenticated, this is null.
 * Missing fields are kept as null (never fabricated).
 */
data class UserAccount(
    val id: String,
    val displayName: String? = null,
    val email: String? = null,
    val photoUrl: String? = null,
    val idToken: String? = null
) {
    /**
     * Computes clean 1-2 letter initials from the real display name or email,
     * without inventing fake identities.
     */
    fun getInitials(): String {
        val name = displayName?.trim()
        if (!name.isNullOrBlank()) {
            val parts = name.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            return when {
                parts.size >= 2 -> "${parts[0].first().uppercaseChar()}${parts[1].first().uppercaseChar()}"
                parts.isNotEmpty() && parts[0].isNotEmpty() -> parts[0].take(2).uppercase()
                else -> "U"
            }
        }
        val mail = email?.trim()
        if (!mail.isNullOrBlank()) {
            val localPart = mail.substringBefore('@')
            return if (localPart.isNotEmpty()) localPart.take(2).uppercase() else "U"
        }
        return "U"
    }
}
