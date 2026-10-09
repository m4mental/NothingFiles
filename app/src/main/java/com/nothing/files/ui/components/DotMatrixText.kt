package com.nothing.files.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nothing.files.ui.theme.LedOff
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingWhite

/**
 * High-fidelity 5x7 Dot Matrix character generator inspired by Nothing OS NDot 57 typography.
 */
object DotMatrixFont {
    // 5 columns x 7 rows bitmask (each column is a 7-bit number: bit 0 = top, bit 6 = bottom)
    private val glyphs = mapOf(
        'A' to intArrayOf(0x7E, 0x11, 0x11, 0x11, 0x7E),
        'B' to intArrayOf(0x7F, 0x49, 0x49, 0x49, 0x36),
        'C' to intArrayOf(0x3E, 0x41, 0x41, 0x41, 0x22),
        'D' to intArrayOf(0x7F, 0x41, 0x41, 0x22, 0x1C),
        'E' to intArrayOf(0x7F, 0x49, 0x49, 0x49, 0x41),
        'F' to intArrayOf(0x7F, 0x09, 0x09, 0x09, 0x01),
        'G' to intArrayOf(0x3E, 0x41, 0x49, 0x49, 0x7A),
        'H' to intArrayOf(0x7F, 0x08, 0x08, 0x08, 0x7F),
        'I' to intArrayOf(0x00, 0x41, 0x7F, 0x41, 0x00),
        'J' to intArrayOf(0x20, 0x40, 0x41, 0x3F, 0x01),
        'K' to intArrayOf(0x7F, 0x08, 0x14, 0x22, 0x41),
        'L' to intArrayOf(0x7F, 0x40, 0x40, 0x40, 0x40),
        'M' to intArrayOf(0x7F, 0x02, 0x0C, 0x02, 0x7F),
        'N' to intArrayOf(0x7F, 0x04, 0x08, 0x10, 0x7F),
        'O' to intArrayOf(0x3E, 0x41, 0x41, 0x41, 0x3E),
        'P' to intArrayOf(0x7F, 0x09, 0x09, 0x09, 0x06),
        'Q' to intArrayOf(0x3E, 0x41, 0x51, 0x21, 0x5E),
        'R' to intArrayOf(0x7F, 0x09, 0x19, 0x29, 0x46),
        'S' to intArrayOf(0x46, 0x49, 0x49, 0x49, 0x31),
        'T' to intArrayOf(0x01, 0x01, 0x7F, 0x01, 0x01),
        'U' to intArrayOf(0x3F, 0x40, 0x40, 0x40, 0x3F),
        'V' to intArrayOf(0x1F, 0x20, 0x40, 0x20, 0x1F),
        'W' to intArrayOf(0x7F, 0x20, 0x18, 0x20, 0x7F),
        'X' to intArrayOf(0x63, 0x14, 0x08, 0x14, 0x63),
        'Y' to intArrayOf(0x07, 0x08, 0x70, 0x08, 0x07),
        'Z' to intArrayOf(0x61, 0x51, 0x49, 0x45, 0x43),
        '0' to intArrayOf(0x3E, 0x51, 0x49, 0x45, 0x3E),
        '1' to intArrayOf(0x00, 0x42, 0x7F, 0x40, 0x00),
        '2' to intArrayOf(0x42, 0x61, 0x51, 0x49, 0x46),
        '3' to intArrayOf(0x21, 0x41, 0x45, 0x4B, 0x31),
        '4' to intArrayOf(0x18, 0x14, 0x12, 0x7F, 0x10),
        '5' to intArrayOf(0x27, 0x45, 0x45, 0x45, 0x39),
        '6' to intArrayOf(0x3C, 0x4A, 0x49, 0x49, 0x30),
        '7' to intArrayOf(0x01, 0x71, 0x09, 0x05, 0x03),
        '8' to intArrayOf(0x36, 0x49, 0x49, 0x49, 0x36),
        '9' to intArrayOf(0x06, 0x49, 0x49, 0x29, 0x1E),
        ' ' to intArrayOf(0x00, 0x00, 0x00, 0x00, 0x00),
        '.' to intArrayOf(0x00, 0x60, 0x60, 0x00, 0x00),
        ':' to intArrayOf(0x00, 0x36, 0x36, 0x00, 0x00),
        '/' to intArrayOf(0x40, 0x20, 0x10, 0x08, 0x04),
        '-' to intArrayOf(0x08, 0x08, 0x08, 0x08, 0x08),
        '_' to intArrayOf(0x40, 0x40, 0x40, 0x40, 0x40),
        '%' to intArrayOf(0x23, 0x13, 0x08, 0x64, 0x62),
        '(' to intArrayOf(0x00, 0x1C, 0x22, 0x41, 0x00),
        ')' to intArrayOf(0x00, 0x41, 0x22, 0x1C, 0x00),
        '[' to intArrayOf(0x00, 0x7F, 0x41, 0x41, 0x00),
        ']' to intArrayOf(0x00, 0x41, 0x41, 0x7F, 0x00),
        '#' to intArrayOf(0x14, 0x7F, 0x14, 0x7F, 0x14),
        '!' to intArrayOf(0x00, 0x00, 0x5F, 0x00, 0x00),
        '?' to intArrayOf(0x02, 0x01, 0x51, 0x09, 0x06),
        '+' to intArrayOf(0x08, 0x08, 0x3E, 0x08, 0x08)
    )

    fun getGlyph(char: Char): IntArray {
        val upper = char.uppercaseChar()
        return glyphs[upper] ?: glyphs['?'] ?: intArrayOf(0, 0, 0, 0, 0)
    }
}

@Composable
fun DotMatrixText(
    text: String,
    modifier: Modifier = Modifier,
    dotSize: Dp = 2.dp,
    dotSpacing: Dp = 1.dp,
    activeColor: Color = LedOnRed,
    inactiveColor: Color = LedOff,
    showInactiveGrid: Boolean = true
) {
    val cleanText = text.uppercase()
    val charWidthDots = 5
    val charHeightDots = 7
    val charSpacingDots = 1

    val totalCols = if (cleanText.isEmpty()) 0 else (cleanText.length * (charWidthDots + charSpacingDots) - charSpacingDots)
    val totalRows = charHeightDots

    val totalWidth = (dotSize * totalCols) + (dotSpacing * (totalCols - 1).coerceAtLeast(0))
    val totalHeight = (dotSize * totalRows) + (dotSpacing * (totalRows - 1).coerceAtLeast(0))

    Canvas(
        modifier = modifier
            .width(totalWidth)
            .height(totalHeight)
    ) {
        val dotRadius = dotSize.toPx() / 2f
        val step = dotSize.toPx() + dotSpacing.toPx()

        var colOffset = 0
        for (char in cleanText) {
            val glyph = DotMatrixFont.getGlyph(char)
            for (col in 0 until charWidthDots) {
                val colBits = if (col < glyph.size) glyph[col] else 0
                for (row in 0 until charHeightDots) {
                    val isBitSet = (colBits and (1 shl row)) != 0
                    val cx = (colOffset + col) * step + dotRadius
                    val cy = row * step + dotRadius

                    if (isBitSet) {
                        drawCircle(
                            color = activeColor,
                            radius = dotRadius,
                            center = Offset(cx, cy)
                        )
                    } else if (showInactiveGrid) {
                        drawCircle(
                            color = inactiveColor,
                            radius = dotRadius * 0.75f,
                            center = Offset(cx, cy)
                        )
                    }
                }
            }
            colOffset += (charWidthDots + charSpacingDots)
        }
    }
}
