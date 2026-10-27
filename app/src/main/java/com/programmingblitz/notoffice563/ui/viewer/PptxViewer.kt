package com.programmingblitz.notoffice563.ui.viewer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Slideshow
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.testTag
import com.programmingblitz.notoffice563.ui.theme.PptPresentationAmber
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassBadge
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassSurface
import com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassTokens

@Composable
fun PptxViewer(
    state: ViewerContentState.PptxContent,
    onSlideSelect: (Int) -> Unit,
    onAddSlide: (() -> Unit)? = null,
    onDeleteSlide: ((Int) -> Unit)? = null,
    onSaveOriginal: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val currentSlide = state.parsedPresentation.slides.getOrNull(state.currentSlideIndex)
        ?: state.parsedPresentation.slides.firstOrNull()

    Column(modifier = modifier.fillMaxSize()) {
        // Top Liquid Glass Presentation Status Bar
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            shape = LiquidGlassTokens.ShapeMedium,
            isDark = isDark,
            elevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LiquidGlassBadge(
                        text = "POWERPOINT DECK",
                        color = PptPresentationAmber,
                        containerColor = PptPresentationAmber.copy(alpha = 0.18f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Slide ${state.currentSlideIndex + 1} of ${state.parsedPresentation.slides.size}",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onSaveOriginal != null) {
                        com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton(
                            text = "Save",
                            onClick = onSaveOriginal,
                            accentColor = PptPresentationAmber,
                            height = 32.dp,
                            modifier = Modifier.testTag("pptx_save_button")
                        )
                    }
                }
            }
        }

        // Active Slide Preview Canvas (16:9 ratio solid presentation canvas)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            val canvasBg = if (isDark) LiquidGlassTokens.DocumentCanvasDark else LiquidGlassTokens.DocumentCanvasLight
            val canvasTextPrimary = if (isDark) LiquidGlassTokens.DarkTextPrimary else Color(0xFF0F172A)
            val canvasTextSecondary = if (isDark) LiquidGlassTokens.DarkTextSecondary else Color(0xFF64748B)

            if (currentSlide != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .shadow(12.dp, RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(canvasBg)
                        .border(
                            1.5.dp,
                            PptPresentationAmber.copy(alpha = 0.4f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Slide Header Pill & Number
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(LiquidGlassTokens.ShapeCapsule)
                                    .background(PptPresentationAmber.copy(alpha = 0.15f))
                                    .border(1.dp, PptPresentationAmber.copy(alpha = 0.4f), LiquidGlassTokens.ShapeCapsule)
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "SLIDE ${currentSlide.slideNumber}",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PptPresentationAmber
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Slide Title
                        Text(
                            text = currentSlide.title.ifEmpty { "Untitled Slide" },
                            style = TextStyle(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = PptPresentationAmber,
                                lineHeight = 26.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Slide Body Bullets
                        if (currentSlide.bodyBullets.isNotEmpty()) {
                            currentSlide.bodyBullets.forEach { bullet ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Text(
                                        text = "• ",
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = PptPresentationAmber
                                        )
                                    )
                                    Text(
                                        text = bullet,
                                        style = TextStyle(
                                            fontSize = 14.sp,
                                            color = canvasTextPrimary,
                                            lineHeight = 20.sp
                                        )
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = "(No body text content on this slide)",
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    color = canvasTextSecondary
                                )
                            )
                        }

                        // Speaker Notes Block
                        if (currentSlide.notes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isDark) Color(0x26FFFFFF) else Color(0xFFF1F5F9))
                                    .border(0.75.dp, if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.Top) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Notes,
                                        contentDescription = null,
                                        tint = PptPresentationAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = "Speaker Notes",
                                            style = TextStyle(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = PptPresentationAmber
                                            )
                                        )
                                        Text(
                                            text = currentSlide.notes,
                                            style = TextStyle(
                                                fontSize = 12.sp,
                                                color = canvasTextSecondary
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Slide Filmstrip Navigator (Horizontal Liquid Glass Thumbnail Strip)
        LiquidGlassSurface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            shape = LiquidGlassTokens.ShapeMedium,
            isDark = isDark,
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(vertical = 10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Slide Filmstrip",
                        style = TextStyle(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) LiquidGlassTokens.DarkTextSecondary else LiquidGlassTokens.LightTextSecondary
                        )
                    )

                    if (onAddSlide != null) {
                        com.programmingblitz.notoffice563.ui.theme.glass.LiquidGlassButton(
                            text = "+ Slide",
                            onClick = onAddSlide,
                            accentColor = PptPresentationAmber,
                            height = 28.dp,
                            modifier = Modifier.testTag("pptx_add_slide_button")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(state.parsedPresentation.slides) { index, slide ->
                        val isSelected = index == state.currentSlideIndex

                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(72.dp)
                                .shadow(if (isSelected) 6.dp else 2.dp, RoundedCornerShape(10.dp))
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) {
                                        PptPresentationAmber.copy(alpha = 0.2f)
                                    } else {
                                        if (isDark) Color(0x33FFFFFF) else Color.White
                                    }
                                )
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) PptPresentationAmber else (if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f)),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { onSlideSelect(index) }
                                .padding(8.dp)
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                Text(
                                    text = "${slide.slideNumber}. ${slide.title}",
                                    style = TextStyle(
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) PptPresentationAmber else (if (isDark) LiquidGlassTokens.DarkTextPrimary else LiquidGlassTokens.LightTextPrimary)
                                    ),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
