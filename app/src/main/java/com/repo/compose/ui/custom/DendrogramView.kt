package com.repo.compose.ui.custom

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.repo.compose.R
import com.repo.compose.data.RootResponseUI

private const val TEXT_SIZE = 8f

/**
 * Отрисовка дендрограммы
 */
class DendrogramView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var nodes: Node? = null
    private var entryNodes: RootResponseUI? = null

    fun setEntryNodes(response: RootResponseUI) {
        entryNodes = response
        nodes = createBinaryTree(response)
        nodes?.calculateSize()
        invalidate()
    }

    private val scaleMatrix = Matrix()

    /**
     * Коэффициент увеличения для координат
     */
    private val coefficientIncrease = 20f

    /**
     * Коэффициент увеличения для конечных узлов
     */
    private val coefficientIncreaseEndNodes = 10f

    /**
     * Для отрисовки линий
     */
    private val paint = Paint().apply {
        color = context.getColor(R.color.black)
        strokeWidth = 1f
        strokeCap = Paint.Cap.ROUND
        isAntiAlias = true
    }

    /**
     * Отрисовка текста
     */
    private val paintText = Paint().apply {
        textSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            TEXT_SIZE,
            resources.displayMetrics
        )
        isAntiAlias = true
    }

    private var scaleGestureDetector: ScaleGestureDetector? = null
    private var gestureDetector: GestureDetector? = null

    init {
        scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
        gestureDetector = GestureDetector(context, GestureListener())
    }

    /**
     * Масштабирование с помощью двух пальцев
     */
    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleMatrix.postScale(
                detector.scaleFactor, detector.scaleFactor, detector.focusX, detector.focusY
            )
            invalidate()
            return true
        }
    }

    /**
     * Смещение и обработчик одиночного касания
     */
    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            callbackTouch?.invoke()
            return true
        }

        override fun onScroll(
            e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float
        ): Boolean {
            val dx = distanceX / scaleMatrix.scale()
            val dy = distanceY / scaleMatrix.scale()
            scaleMatrix.preTranslate(-dx, -dy)
            invalidate()
            return true
        }
    }

    /**
     * Обработчик одиночного касание
     */
    private var callbackTouch: (() -> Unit)? = null
    fun setCallbackTouch(callback: () -> Unit) {
        this.callbackTouch = callback
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector?.onTouchEvent(event)
        gestureDetector?.onTouchEvent(event)
        return true
    }

    /**
     * Расчеты размера дендрограммы относительно размеров экрана
     */
    private fun Node.calculateSize() {
        val width = MeasureSpec.getSize(measuredWidth).toFloat() - paddingStart - paddingEnd
        val height = MeasureSpec.getSize(measuredHeight).toFloat() - paddingTop - paddingBottom
        val zoomStep = 0.1f
        val widthDendrogram =
            (countLeafNodes(this) * coefficientIncrease + coefficientIncreaseEndNodes * 2 + paddingStart + paddingEnd) * 2
        val scaleX = width / widthDendrogram

        var scaleFactor = scaleX
        while (widthDendrogram * scaleFactor < width) {
            scaleFactor += zoomStep
        }
        scaleMatrix.reset()
        scaleMatrix.setScale(scaleFactor, scaleFactor, width / 2, height / 2)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.setMatrix(scaleMatrix)

        entryNodes?.let {
            drawNode(
                canvas,
                nodes,
                width / 2f,
                height / 2f - (maxLevel(it) * coefficientIncrease + coefficientIncreaseEndNodes) / 2,
                maxLevel(it)
            )
        }
        canvas.restore()
    }

    /**
     * Сформировать новую модель для бинарного дерева
     */
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

    /**
     * Отрисовать узлы
     */
    private fun drawNode(
        canvas: Canvas, node: Node?, x: Float, y: Float, level: Int
    ) {
        node?.let {
            //Отрисовка левого ребенка
            node.left?.let {
                val decreaseX = if (it.isLastLevel()) {
                    x - (countLeafNodes(node.right) - 0.5f) * coefficientIncrease
                } else {
                    x - (countLeafNodes(node.right)) * coefficientIncrease
                }

                val increaseY = if (it.isLastLevel()) {
                    y + level * coefficientIncrease
                } else if (it.isPenultimate()) {
                    y + level * coefficientIncrease
                } else {
                    y + coefficientIncrease
                }

                // горизонтальная линия (с центра влево)
                canvas.drawLine(x, y, decreaseX, y, paint)
                // линия перпендикулярно вниз, меняется только Y
                canvas.drawLine(decreaseX, y, decreaseX, increaseY, paint)
                drawNode(canvas, node.left, decreaseX, increaseY, level - 1)
            }

            //Отрисовка правого ребенка
            node.right?.let {
                val increaseX = x + countLeafNodes(node.left) * coefficientIncrease
                val increaseY = if (it.isLastLevel()) {
                    y + level * coefficientIncrease
                } else if (it.isPenultimate()) {
                    y + level * coefficientIncrease
                } else {
                    y + coefficientIncrease
                }

                // горизонтальная линия (с центра влево)
                canvas.drawLine(x, y, increaseX, y, paint)
                // линия перпендикулярно вниз, меняется только Y
                canvas.drawLine(increaseX, y, increaseX, increaseY, paint)
                drawNode(canvas, node.right, increaseX, increaseY, level - 1)
            }

            if (node.parent.children?.isEmpty() == true) {
                val text = node.parent.name?.split(".")?.get(0) ?: ""
                val averageX = x - paintText.measureText(text) / 2
                val averageY = y + paintText.textSize
                paintText.color = Color.parseColor(node.parent.color)
                canvas.drawText(text, averageX, averageY, paintText)
            }
        }
    }

    /**
     * Центрирование
     */
    fun fitToCenter() {
        nodes?.calculateSize()
        invalidate()
    }

    /**
     * Увеличить масштаб
     */
    fun zoomPlus() {
        updateScale(scaleMatrix.scale() * 1.2f)
    }

    /**
     * Уменьшить масштаб
     */
    fun zoomMinus() {
        updateScale(scaleMatrix.scale() / 1.2f)
    }

    private fun updateScale(scale: Float) {
        scaleMatrix.setScale(scale, scale, (width / 2).toFloat(), (height / 2).toFloat())
        invalidate()
    }

    private fun Matrix.scale(): Float {
        return FloatArray(9).apply { this@scale.getValues(this) }[Matrix.MSCALE_Y]
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

    /**
     * Вычисляем максимальный уровень среди дочерних элементов
     *
     * @return максимальный уровень среди текущего узла и его дочерних элементов
     */
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

    /**
     * Вычислить количество конечных точек
     */
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

}
