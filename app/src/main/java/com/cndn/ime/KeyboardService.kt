package com.cndn.ime

import android.graphics.Color
import android.inputmethodservice.InputMethodService
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView

class KeyboardService : InputMethodService(), KeyboardActionListener, EmojiPanelListener {

    private lateinit var rootView: LinearLayout
    private lateinit var candidateBar: LinearLayout
    private lateinit var panelContainer: FrameLayout
    private lateinit var keyboardView: KeyboardView
    private lateinit var emojiPanel: EmojiPanel

    private var capsForNextCharOnly = true

    override fun onCreateInputView(): View {
        rootView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.parseColor("#1E1E1E"))
        }

        candidateBar = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundColor(Color.parseColor("#141414"))
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(40)
            )
        }

        panelContainer = FrameLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(230)
            )
        }

        keyboardView = KeyboardView(this).apply { listener = this@KeyboardService }
        emojiPanel = EmojiPanel(this).apply {
            listener = this@KeyboardService
            visibility = View.GONE
        }

        panelContainer.addView(keyboardView)
        panelContainer.addView(emojiPanel)

        rootView.addView(candidateBar)
        rootView.addView(panelContainer)

        return rootView
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun showSuggestions(words: List<String>) {
        candidateBar.removeAllViews()
        for (w in words) {
            val tv = TextView(this).apply {
                text = w
                setTextColor(Color.WHITE)
                textSize = 16f
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f)
                setOnClickListener {
                    commitWord(w)
                    candidateBar.removeAllViews()
                }
            }
            candidateBar.addView(tv)
        }
    }

    private fun commitWord(word: String) {
        val ic = currentInputConnection ?: return
        ic.commitText("$word ", 1)
    }

    override fun onChar(char: Char) {
        val ic = currentInputConnection ?: return
        ic.commitText(char.toString(), 1)
        if (capsForNextCharOnly && keyboardView.shifted) {
            keyboardView.shifted = false
        }
        candidateBar.removeAllViews()
    }

    override fun onSpace() {
        currentInputConnection?.commitText(" ", 1)
        candidateBar.removeAllViews()
    }

    override fun onEnter() {
        currentInputConnection?.sendKeyEvent(
            android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER)
        )
    }

    override fun onBackspace() {
        val ic = currentInputConnection ?: return
        val selected = ic.getSelectedText(0)
        if (selected.isNullOrEmpty()) {
            ic.deleteSurroundingText(1, 0)
        } else {
            ic.commitText("", 1)
        }
    }

    override fun onToggleShift() {
        keyboardView.shifted = !keyboardView.shifted
    }

    override fun onSwitchToSymbols() {
        candidateBar.removeAllViews()
    }

    override fun onSwitchToLetters() {
        candidateBar.removeAllViews()
    }

    override fun onSwitchToEmoji() {
        keyboardView.visibility = View.GONE
        emojiPanel.visibility = View.VISIBLE
    }

    override fun onSwipeSuggestions(words: List<String>) {
        if (words.isNotEmpty()) showSuggestions(words)
    }

    override fun onEmojiSelected(emoji: String) {
        currentInputConnection?.commitText(emoji, 1)
    }

    override fun onEmojiBackspace() {
        onBackspace()
    }

    override fun onBackToKeyboard() {
        emojiPanel.visibility = View.GONE
        keyboardView.visibility = View.VISIBLE
    }
}
