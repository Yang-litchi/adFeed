package com.example.adfeed.ui.components
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun CollectButton(
    isCollected: Boolean,
    count: Int,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isCollected) 1.2f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "collect_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable { onClick() }
            .padding(4.dp)
    ) {
        Icon(
            imageVector = if (isCollected)
                Icons.Filled.Bookmark
            else
                Icons.Outlined.BookmarkBorder,
            contentDescription = "收藏",
            tint = if (isCollected)
                Color(0xFFFFAA00)
            else
                Color.Gray,
            modifier = Modifier
                .size(22.dp)
                .scale(scale)
        )

        Spacer(modifier = Modifier.width(4.dp))

        Text(
            text = if (count >= 1000)
                "${"%.1f".format(count / 1000f)}k"
            else
                count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )
    }
}