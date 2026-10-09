package com.nothing.files.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.model.StorageCategoryInfo
import com.nothing.files.ui.theme.DotGridInactive
import com.nothing.files.ui.theme.LedOff
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite

@Composable
fun StorageMeterWidget(
    totalSpace: String,
    usedSpace: String,
    usedPercentage: Int,
    categories: List<StorageCategoryInfo>,
    onCategoryClick: (StorageCategoryInfo) -> Unit,
    onWidgetClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NothingCard(
        modifier = modifier.fillMaxWidth(),
        onClick = onWidgetClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .padding(end = 0.dp)
                    ) {
                        Canvas(modifier = Modifier.size(8.dp)) {
                            drawCircle(color = LedOnRed)
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "INTERNAL STORAGE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        letterSpacing = 1.2.sp,
                        color = NothingWhite
                    )
                }

                Text(
                    text = "$usedPercentage%",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = NothingRed
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle size
            Text(
                text = "$usedSpace used of $totalSpace",
                fontSize = 13.sp,
                color = NothingTextSecondary,
                fontFamily = FontFamily.Default
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Dot Matrix Progress Bar
            DotMatrixProgressBar(
                percentage = usedPercentage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    NothingCard(
                        shape = RoundedCornerShape(8.dp),
                        backgroundColor = NothingSurfaceElevated,
                        borderColor = NothingBorder,
                        borderWidth = 1.dp,
                        onClick = { onCategoryClick(cat) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Canvas(modifier = Modifier.size(6.dp)) {
                                drawCircle(color = cat.color)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = cat.title.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NothingWhite
                            )
                            if (cat.totalBytes > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = cat.formattedSize,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = NothingTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DotMatrixProgressBar(
    percentage: Int,
    modifier: Modifier = Modifier,
    activeColor: Color = LedOnRed,
    inactiveColor: Color = LedOff
) {
    val clamped = percentage.coerceIn(0, 100)

    Canvas(modifier = modifier) {
        val totalCols = 40
        val rows = 3
        val colStep = size.width / totalCols
        val rowStep = size.height / rows
        val dotRadius = (minOf(colStep, rowStep) * 0.35f)

        val activeCols = (totalCols * clamped) / 100

        for (col in 0 until totalCols) {
            val isActive = col < activeCols
            val color = if (isActive) activeColor else inactiveColor
            for (row in 0 until rows) {
                val cx = (col + 0.5f) * colStep
                val cy = (row + 0.5f) * rowStep
                drawCircle(
                    color = color,
                    radius = dotRadius,
                    center = Offset(cx, cy)
                )
            }
        }
    }
}
