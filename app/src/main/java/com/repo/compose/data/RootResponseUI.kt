package com.repo.compose.data

import java.util.Random

data class RootResponseUI(
    val childCount: Int?,
    val children: List<RootResponseUI>?,
    val name: String?,
    val level: Int?,
    val color: String?
)

fun generateBinaryTree(level: Int = 5, currentLevel: Int = 0): RootResponseUI {
    if (currentLevel >= level) {
        val random = Random()
        val name = random.nextInt(21)
        return RootResponseUI(null, null, name.toString(), null, null)
    }

    val childCount = 2 // Две ветки на каждом уровне
    val children = List(childCount) { generateBinaryTree(level, currentLevel + 1) }
    val name = "Node Level $currentLevel"
    val color = if (currentLevel % 2 == 0) "Red" else "Blue"

    return RootResponseUI(
        childCount = children.size,
        children = children,
        name = name,
        level = currentLevel,
        color = color
    )
}
