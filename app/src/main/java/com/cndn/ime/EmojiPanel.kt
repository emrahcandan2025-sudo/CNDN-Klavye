package com.cndn.ime

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

interface EmojiPanelListener {
    fun onEmojiSelected(emoji: String)
    fun onEmojiBackspace()
    fun onBackToKeyboard()
}

class EmojiPanel @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var listener: EmojiPanelListener? = null

    private data class Category(val icon: String, val emojis: List<String>)

    private val categories = listOf(
        Category("😀", listOf("😀","😁","😂","🤣","😊","😍","😘","😜","🤔","😴","😎","🥳","😢","😡","🥺","👍","👎","👏","🙏","💪","❤️","🔥","✨","🎉")),
        Category("🐶", listOf("🐶","🐱","🐭","🐹","🐰","🦊","🐻","🐼","🐨","🐯","🦁","🐮","🐷","🐸","🐵","🐔","🐧","🐦","🦋","🐝","🐢","🐍","🦄","🐳")),
        Category("🍕", listOf("🍏","🍎","🍊","🍋","🍌","🍉","🍇","🍓","🍕","🍔","🍟","🌭","🍿","🥪","🍩","🍪","🍰","🍫","☕","🍵","🥤","🍺","🍷","🥗")),
        Category("⚽", listOf("⚽","🏀","🏈","⚾","🎾","🏐","🎱","🏓","🎳","🎮","🎲","🎯","🚗","✈️","🚀","⛵","🏖️","🏔️","🎵","🎸","📚","💻","📱","⌚")),
        Category("💡", listOf("💡","⭐","🌙","☀️","🌈","☁️","⚡","❄️","💧","🌸","🌹","🍀","💎","🔑","🔒","📌","✅","❌","❓","❗","💯","🆗","🕐","📅"))
    )

    private var currentCategory = 0

    private val tabBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2E2E2E") }
    private val tabActiveBg = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#2FBF71") }
    private val bgPaint = Paint().apply { color = Color.parseColor("#1E1E1E") }
    private val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textAlign = Paint.Align.CENTER }
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        color = Color.WHITE
    }

    private var tabBarHeight = 0f
    private var cellSize = 0f
    private val columns = 8
    private var backKeyRect = RectF()
    private var tabRects: List<RectF> = emptyList()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        tabBarHeight = h * 0.16f
        cellSize = w / columns.toFloat()
        emojiPaint.textSize = cellSize * 0.5f
        labelPaint.textSize = tabBarHeight * 0.4f

        val backWidth = w * 0.14f
        backKeyRect = RectF(0f, 0f, backWidth, tabBarHeight)
        val remaining = w - backWidth
        val each = remaining / categories.size
        tabRects = categories.indices.map { i ->
            RectF(backWidth + i * each, 0f, backWidth + (i + 1) * each, tabBarHeight)
        }
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        canvas.drawRect(backKeyRect, tabBg)
        canvas.drawText("⌨", backKeyRect.centerX(), backKeyRect.centerY() - (labelPaint.ascent() + labelPaint.descent()) / 2f, labelPaint)

        categories.forEachIndexed { i, cat ->
            val r = tabRects[i]
            canvas.drawRect(r, if (i == currentCategory) tabActiveBg else tabBg)
            canvas.drawText(cat.icon, r.centerX(), r.centerY() - (labelPaint.ascent() + labelPaint.descent()) / 2f, labelPaint)
        }

        val emojis = categories[currentCategory].emojis
        emojis.forEachIndexed { index, emoji ->
            val col = index % columns
            val row = index / columns
            val cx = col * cellSize + cellSize / 2f
            val cy = tabBarHeight + row * cellSize + cellSize / 2f
            canvas.drawText(emoji, cx, cy - (emojiPaint.ascent() + emojiPaint.descent()) / 2f, emojiPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action != MotionEvent.ACTION_UP) return true
        val x = event.x
        val y = event.y

        if (backKeyRect.contains(x, y)) {
            listener?.onBackToKeyboard()
            return true
        }
        tabRects.forEachIndexed { i, r ->
            if (r.contains(x, y)) {
                currentCategory = i
                invalidate()
                return true
            }
        }
        if (y > tabBarHeight) {
            val col = (x / cellSize).toInt().coerceIn(0, columns - 1)
            val row = ((y - tabBarHeight) / cellSize).toInt()
            val index = row * columns + col
            val emojis = categories[currentCategory].emojis
            if (index in emojis.indices) {
                listener?.onEmojiSelected(emojis[index])
            }
        }
        return true
    }
}
