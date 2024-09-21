package com.repo.compose.ui.custom

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

/**
 * Кастомная кнопка
 *
 * @param icon - иконка кнопки
 * @param colors - цвет кнопки
 * @param onClick - действие по клику
 */
@Composable
fun IconLabelButton(
    icon: ImageVector,
    colors: Color = Color(0xFFAAD9FF),
    label: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color = colors)
            .clickable { onClick.invoke() }
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Button Icon"
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(text = label)
    }
}