package com.cndn.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.hypot

interface KeyboardActionListener {
    fun onChar(char: Char)
    fun onSpace()
    fun onEnter()
    fun onBackspace()
    fun onToggleShift()
    fun onSwitchToSymbols()
    fun onSwitchToLetters()
    fun onSwitchToEmoji()
    fun onSwipeSuggestions(words: List<String>)
}

class KeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var listener: KeyboardActionListener? = null

    var shifted: Boolean = false
        set(value) {
            field = value
            refreshLayout()
        }

    var symbolsMode: Boolean = false
        set(value) {
            field = value
            refreshLayout()
        }

    private var rows: List<List<Key>> = emptyList()
    private var bottom: List<Key> = TurkishLayout.bottomRow()

    private val keyBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2E2E2E") }
    private val keyBgSpecial = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3A3A3A") }
    private val bgPaint = Paint().apply { color = Color.parseColor("#1E1E1E") }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }
    private val swipeTrailPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2FBF71")
        style = Paint.Style.STROKE
        strokeWidth = 10f
        strokeCap = Paint.Cap.ROUND
    }

    private val keyGap = 6f
    private var rowHeight = 0f

    private var isSwiping = false
    private var swipePath = Path()
    private val swipedChars = mutableListOf<Char>()
    private var lastKeyHit: Key? = null
    private var downX = 0f
    private var downY = 0f
    private var downKey: Key? = null

    init {
        layoutKeys()
    }

    private fun layoutKeys() {
        rows = if (symbolsMode) symbolRows() else TurkishLayout.letterRows(shifted)
    }

    private fun symbolRows(): List<List<Key>> {
        val r1 = "1234567890".map { Key(it.toString(), KeyType.CHAR, it.code) }
        val r2 = "@#₺&*-+()".map { Key(it.toString(), KeyType.CHAR, it.code) }
        val r3 = listOf(Key("=\\<", KeyType.CHAR, '='.code, 1.5f)) +
                "/;:!?\"".map { Key(it.toString(), KeyType.CHAR, it.code) } +
                listOf(Key("⌫", KeyType.BACKSPACE, weight = 1.5f))
        return listOf(r1, r2, r3)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        computeKeyBounds(w.toFloat(), h.toFloat())
    }

    private fun computeKeyBounds(w: Float, h: Float) {
        val totalRows = rows.size + 1
        rowHeight = h / totalRows

        var y = 0f
        for (row in rows) {
            layoutRow(row, w, y, rowHeight)
            y += rowHeight
        }
        layoutRow(bottom, w, y, rowHeight)

        textPaint.textSize = rowHeight * 0.38f
    }

    private fun layoutRow(row: List<Key>, totalWidth: Float, y: Float, height: Float) {
        val totalWeight = row.sumOf { it.weight.toDouble() }.toFloat()
        var x = 0f
        for (key in row) {
            val w = (key.weight / totalWeight) * totalWidth
            key.x = x + keyGap / 2f
            key.y = y + keyGap / 2f
            key.width = w - keyGap
            key.height = height - keyGap
            x += w
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        for (row in rows) drawRow(canvas, row)
        drawRow(canvas, bottom)

        if (isSwiping) canvas.drawPath(swipePath, swipeTrailPaint)
    }

    private fun drawRow(canvas: Canvas, row: List<Key>) {
        for (key in row) {
            val paint = if (key.type == KeyType.CHAR || key.type == KeyType.COMMA_PERIOD) keyBg else keyBgSpecial
            val rect = RectF(key.x, key.y, key.x + key.width, key.y + key.height)
            canvas.drawRoundRect(rect, 14f, 14f, paint)
            val ty = key.centerY() - (textPaint.descent() + textPaint.ascent()) / 2f
            canvas.drawText(key.label, key.centerX(), ty, textPaint)
        }
    }

    private fun findKeyAt(x: Float, y: Float): Key? {
        for (row in rows) for (key in row) if (key.contains(x, y)) return key
        for (key in bottom) if (key.contains(x, y)) return key
        return null
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x
                downY = event.y
                downKey = findKeyAt(downX, downY)
                isSwiping = false
                swipePath = Path().apply { moveTo(downX, downY) }
                swipedChars.clear()
                lastKeyHit = downKey
                downKey?.let { if (it.type == KeyType.CHAR) swipedChars.add(it.label.lowercase()[0]) }
            }
            MotionEvent.ACTION_MOVE -> {
                val moved = hypot((event.x - downX).toDouble(), (event.y - downY).toDouble())
                val key = findKeyAt(event.x, event.y)
                if (moved > rowHeight * 0.6f && key !== downKey) {
                    isSwiping = true
                }
                if (isSwiping) {
                    swipePath.lineTo(event.x, event.y)
                    if (key != null && key !== lastKeyHit && key.type == KeyType.CHAR) {
                        swipedChars.add(key.label.lowercase()[0])
                        lastKeyHit = key
                    }
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isSwiping && swipedChars.size >= 2) {
                    val suggestions = SwipeEngine.suggest(swipedChars).map { it.word }
                    listener?.onSwipeSuggestions(suggestions)
                } else {
                    downKey?.let { handleTap(it) }
                }
                isSwiping = false
                invalidate()
            }
        }
        return true
    }

    private fun handleTap(key: Key) {
        when (key.type) {
            KeyType.CHAR, KeyType.COMMA_PERIOD -> listener?.onChar(key.label[0])
            KeyType.SPACE -> listener?.onSpace()
            KeyType.ENTER -> listener?.onEnter()
            KeyType.BACKSPACE -> listener?.onBackspace()
            KeyType.SHIFT -> listener?.onToggleShift()
            KeyType.SYMBOLS -> {
                if (symbolsMode) { symbolsMode = false; listener?.onSwitchToLetters() }
                else { symbolsMode = true; listener?.onSwitchToSymbols() }
            }
            KeyType.EMOJI -> listener?.onSwitchToEmoji()
        }
    }

    fun refreshLayout() {
        layoutKeys()
        if (width > 0 && height > 0) {
            computeKeyBounds(width.toFloat(), height.toFloat())
        }
        invalidate()
    }
}
