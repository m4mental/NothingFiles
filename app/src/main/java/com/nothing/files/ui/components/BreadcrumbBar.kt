package com.nothing.files.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite
import java.io.File

@Composable
fun BreadcrumbBar(
    currentPath: String,
    onNavigateToPath: (String) -> Unit,
    onNavigateUp: () -> Unit,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    LaunchedEffect(currentPath) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    val segments = currentPath.trim('/').split('/').filter { it.isNotEmpty() }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Home Button
        IconButton(
            onClick = onNavigateHome,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Home,
                contentDescription = "Home",
                tint = NothingWhite,
                modifier = Modifier.size(20.dp)
            )
        }

        // Parent Up Button
        val canGoUp = currentPath != "/" && currentPath.isNotEmpty()
        IconButton(
            onClick = onNavigateUp,
            enabled = canGoUp,
            modifier = Modifier.size(36.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Up",
                tint = if (canGoUp) NothingWhite else NothingTextSecondary.copy(alpha = 0.3f),
                modifier = Modifier.size(20.dp)
            )
        }

        // Path Scroll Row
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Root Node
            NothingCard(
                shape = RoundedCornerShape(6.dp),
                backgroundColor = if (segments.isEmpty()) NothingRed else NothingSurfaceElevated,
                borderColor = NothingBorder,
                onClick = { onNavigateToPath("/") }
            ) {
                Text(
                    text = "ROOT",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = NothingWhite,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            var accumulatingPath = ""
            segments.forEachIndexed { index, seg ->
                accumulatingPath += "/$seg"
                val isLast = index == segments.size - 1
                val targetPath = accumulatingPath

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = NothingTextSecondary,
                    modifier = Modifier.size(14.dp)
                )

                NothingCard(
                    shape = RoundedCornerShape(6.dp),
                    backgroundColor = if (isLast) NothingRed else NothingSurface,
                    borderColor = if (isLast) NothingRed else NothingBorder,
                    onClick = { onNavigateToPath(targetPath) }
                ) {
                    Text(
                        text = seg,
                        fontFamily = FontFamily.Default,
                        fontWeight = if (isLast) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp,
                        color = NothingWhite,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}
