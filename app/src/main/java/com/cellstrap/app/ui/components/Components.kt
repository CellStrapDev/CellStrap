package com.cellstrap.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cellstrap.app.ui.theme.*

@Composable
fun CellCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    borderGlow: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val borderColor = if (borderGlow) CellGreen.copy(alpha = 0.4f) else CellCardBorder

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(CellCard)
            .border(1.dp, borderColor, shape)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun GlowButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    icon: ImageVector? = null,
    variant: ButtonVariant = ButtonVariant.PRIMARY
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "scale"
    )

    val bgColor = when (variant) {
        ButtonVariant.PRIMARY -> if (enabled) CellGreen else CellGreenDark
        ButtonVariant.SECONDARY -> CellCard
        ButtonVariant.DANGER -> CellError
        ButtonVariant.OUTLINE -> Color.Transparent
    }
    val textColor = when (variant) {
        ButtonVariant.PRIMARY -> CellBlack
        ButtonVariant.SECONDARY -> CellTextPrimary
        ButtonVariant.DANGER -> Color.White
        ButtonVariant.OUTLINE -> CellGreen
    }
    val borderColor = when (variant) {
        ButtonVariant.OUTLINE -> CellGreen
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .scale(scale)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled && !isLoading
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = if (variant == ButtonVariant.PRIMARY) CellBlack else CellGreen,
                strokeWidth = 2.dp
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 20.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = text,
                    color = textColor,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

enum class ButtonVariant {
    PRIMARY, SECONDARY, DANGER, OUTLINE
}

@Composable
fun StatusBadge(
    text: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = CellTextPrimary
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = CellTextMuted
                )
            }
        }
        if (action != null) {
            action()
        }
    }
}

@Composable
fun FlagRow(
    flagKey: String,
    flagValue: String,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CellSurface)
            .border(1.dp, CellDivider, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = flagKey,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = CellGreenGlow,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = flagValue,
                fontSize = 11.sp,
                color = CellTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row {
            if (onEdit != null) {
                TextButton(onClick = onEdit, contentPadding = PaddingValues(4.dp)) {
                    Text("Edit", fontSize = 11.sp, color = CellGreen)
                }
            }
            if (onDelete != null) {
                TextButton(onClick = onDelete, contentPadding = PaddingValues(4.dp)) {
                    Text("✕", fontSize = 11.sp, color = CellError)
                }
            }
        }
    }
}

@Composable
fun CellDividerLine(modifier: Modifier = Modifier) {
    Divider(
        modifier = modifier,
        color = CellDivider,
        thickness = 1.dp
    )
}

@Composable
fun GreenDot(size: Dp = 8.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(CellGreen)
    )
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = CellTextSecondary
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = CellTextMuted)
        Text(value, style = MaterialTheme.typography.bodySmall, color = valueColor, fontWeight = FontWeight.Medium)
    }
}
