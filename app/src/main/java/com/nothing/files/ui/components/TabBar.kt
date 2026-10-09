package com.nothing.files.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.model.TabInfo
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingBorderLight
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingWhite

@Composable
fun TabBar(
    tabs: List<TabInfo>,
    activeTabId: String,
    onSelectTab: (TabInfo) -> Unit,
    onCloseTab: (TabInfo) -> Unit,
    onNewTab: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isActive = tab.id == activeTabId

                NothingCard(
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = if (isActive) NothingSurfaceElevated else NothingSurface,
                    borderColor = if (isActive) NothingRed else NothingBorder,
                    borderWidth = if (isActive) 1.5.dp else 1.dp,
                    onClick = { onSelectTab(tab) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (isActive) {
                            Canvas(modifier = Modifier.size(6.dp)) {
                                drawCircle(color = LedOnRed)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }

                        Text(
                            text = tab.title.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = if (isActive) NothingWhite else NothingTextSecondary
                        )

                        if (tabs.size > 1) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = { onCloseTab(tab) },
                                    modifier = Modifier.size(16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close Tab",
                                        tint = if (isActive) NothingWhite else NothingTextSecondary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add Tab Button
        IconButton(
            onClick = onNewTab,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "New Tab",
                tint = NothingRed,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
