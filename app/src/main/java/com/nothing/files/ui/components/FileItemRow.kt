package com.nothing.files.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nothing.files.model.FileItem
import com.nothing.files.ui.theme.LedOff
import com.nothing.files.ui.theme.LedOnRed
import com.nothing.files.ui.theme.NothingAccentBlue
import com.nothing.files.ui.theme.NothingAccentGreen
import com.nothing.files.ui.theme.NothingAccentOrange
import com.nothing.files.ui.theme.NothingAccentPurple
import com.nothing.files.ui.theme.NothingBorder
import com.nothing.files.ui.theme.NothingRed
import com.nothing.files.ui.theme.NothingSurface
import com.nothing.files.ui.theme.NothingSurfaceElevated
import com.nothing.files.ui.theme.NothingTextSecondary
import com.nothing.files.ui.theme.NothingTextTertiary
import com.nothing.files.ui.theme.NothingWhite

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FileItemRow(
    file: FileItem,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = when {
        file.isDirectory -> NothingWhite
        file.isApk -> NothingAccentPurple
        file.isArchive -> NothingRed
        file.isImage -> NothingAccentOrange
        file.isVideo -> NothingRed
        file.isAudio -> NothingAccentGreen
        file.isTextOrCode -> NothingAccentBlue
        else -> NothingTextSecondary
    }

    val backgroundColor = when {
        isSelected -> NothingSurfaceElevated
        else -> NothingSurface
    }

    val borderColor = when {
        isSelected -> NothingRed
        else -> NothingBorder
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Selection indicator or Type Badge
            if (isSelectionMode) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(20.dp)) {
                        drawCircle(
                            color = if (isSelected) LedOnRed else LedOff,
                            radius = size.minDimension / 2f
                        )
                    }
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = NothingWhite,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            } else {
                // Dot Matrix Type Badge
                NothingCard(
                    modifier = Modifier.size(38.dp),
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = NothingSurfaceElevated,
                    borderColor = badgeColor.copy(alpha = 0.6f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (file.isDirectory) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = "Folder",
                                tint = NothingWhite,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = file.dotMatrixTypeTag,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.8.sp,
                                color = badgeColor
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File Details (Name + Meta)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = NothingWhite
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = file.formattedSize,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NothingTextSecondary
                    )

                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = NothingTextTertiary
                    )

                    Text(
                        text = file.formattedDate,
                        fontSize = 11.sp,
                        color = NothingTextSecondary
                    )

                    Text(
                        text = "•",
                        fontSize = 10.sp,
                        color = NothingTextTertiary
                    )

                    Text(
                        text = file.permissions,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = NothingTextTertiary
                    )
                }
            }

            // More Options Icon
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "Options",
                    tint = NothingTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
