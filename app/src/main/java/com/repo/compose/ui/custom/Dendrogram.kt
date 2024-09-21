package com.repo.compose.ui.custom

import com.repo.compose.data.RootResponseUI
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Коэффициент увеличения для координат
 */
private const val COEFFICIENT_INCREASE = 20f

/**
 * Коэффициент увеличения для конечных узлов
 */
private const val COEFFICIENT_INCREASE_END_NODES = 10f

@Composable
fun DendrogramUI(
    entryNodes: RootResponseUI?,
    modifier: Modifier = Modifier,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale *= zoom
                    offsetX += pan.x / scale
                    offsetY += pan.y / scale
                }
            }
    ) {
        withTransform({
            scale(scale, scale, pivot = Offset(size.width / 2, size.height / 2))
            translate(offsetX, offsetY)
        }) {
            entryNodes?.let {
                drawDendrogram(
                    rootNode = createBinaryTree(it),
                    modifier = modifier,
                    textMeasurer = textMeasurer,
                    level = maxLevel(it),
                    y = size.height / 2f - (maxLevel(it) * COEFFICIENT_INCREASE + COEFFICIENT_INCREASE_END_NODES) / 2
                )
            }
        }
    }
}

fun DrawScope.drawDendrogram(
    rootNode: Node?,
    modifier: Modifier = Modifier,
    textMeasurer: TextMeasurer,
    level: Int,
    x: Float = size.width / 2,
    y: Float = size.height / 2
) {
    rootNode?.let { node ->
        // Draw left child
        node.left?.let { leftNode ->
            val decreaseX = if (leftNode.isLastLevel()) {
                x - (countLeafNodes(node.right) - 0.5f) * COEFFICIENT_INCREASE
            } else {
                x - (countLeafNodes(node.right)) * COEFFICIENT_INCREASE
            }
            val increaseY = if (leftNode.isLastLevel()) {
                y + level * COEFFICIENT_INCREASE
            } else if (leftNode.isPenultimate()) {
                y + level * COEFFICIENT_INCREASE
            } else {
                y + COEFFICIENT_INCREASE
            }
            drawLine(Color.Black, Offset(x, y), Offset(decreaseX, y))
            drawLine(Color.Black, Offset(decreaseX, y), Offset(decreaseX, increaseY))
            drawDendrogram(leftNode, modifier, textMeasurer, level - 1, decreaseX, increaseY)
        }

        // Draw right child
        node.right?.let { rightNode ->
            val increaseX = x + countLeafNodes(node.left) * COEFFICIENT_INCREASE
            val increaseY = if (rightNode.isLastLevel()) {
                y + level * COEFFICIENT_INCREASE
            } else if (rightNode.isPenultimate()) {
                y + level * COEFFICIENT_INCREASE
            } else {
                y + COEFFICIENT_INCREASE
            }
            drawLine(Color.Black, Offset(x, y), Offset(increaseX, y))
            drawLine(Color.Black, Offset(increaseX, y), Offset(increaseX, increaseY))
            drawDendrogram(rightNode, modifier, textMeasurer, level - 1, increaseX, increaseY)
        }

        // Draw node text
        if (node.parent.children.isNullOrEmpty()) {
            val text = node.parent.name ?: "0"
            val style = TextStyle(
                fontSize = 10.sp,
                color = Color.Black,
                background = Color.Red.copy(alpha = 0.2f)
            )
            //Todo "Текст полностью подписывается только когда устройство в горизонтальной ориентации,
            // когда X выходит за пределы видимости в вертикальной ориентации происходил краш, надо доработать"
            if (x in 0.0..size.width.toDouble()) {
                val measure = textMeasurer.measure(text, style)
                drawText(
                    textMeasurer = textMeasurer,
                    text = text,
                    style = style,
                    topLeft = Offset(
                        x = x - measure.size.width / 2,
                        y = y
                    )
                )
            }
        }
    }
}

/**
 * Последний ли это ребенок в ветке
 */
private fun Node.isLastLevel(): Boolean {
    return this.parent.children.isNullOrEmpty()
}

private fun Node.isPenultimate(): Boolean {
    val secondHighestLevel = mutableListOf<Node>()

    fun traverse(currentNode: Node?, currentLevel: Int) {
        currentNode?.let {
            if (currentLevel == this.depth) {
                secondHighestLevel.add(currentNode)
            }
            traverse(currentNode.left, currentLevel + 1)
            traverse(currentNode.right, currentLevel + 1)
        }
    }

    traverse(this, 0)
    return secondHighestLevel.indexOf(this) == secondHighestLevel.size - 2
}

private fun createBinaryTree(rootResponse: RootResponseUI, depth: Int = 0): Node {
    return Node(rootResponse, depth = depth).also { node ->
        rootResponse.children?.let {
            if (it.isNotEmpty()) {
                node.left = createBinaryTree(it[0], depth + 1)
                if (it.size > 1) {
                    node.right = createBinaryTree(it[1], depth + 1)
                }
            }
        }
    }
}

private fun maxLevel(response: RootResponseUI): Int {
    var maxChildLevel = -1
    response.children?.forEach { child ->
        val childLevel = maxLevel(child)
        if (childLevel > maxChildLevel) {
            maxChildLevel = childLevel
        }
    }
    return maxOf(maxChildLevel, response.level ?: 0) + 1
}

private fun countLeafNodes(node: Node?): Int {
    if (node == null) return 0
    return if (node.left == null && node.right == null) 1 else countLeafNodes(node.left) + countLeafNodes(
        node.right
    )
}

data class Node(
    val parent: RootResponseUI,
    var left: Node? = null,
    var right: Node? = null,
    var depth: Int = 0
)
