package com.cndn.ime

enum class KeyType { CHAR, SHIFT, BACKSPACE, SPACE, ENTER, SYMBOLS, EMOJI, COMMA_PERIOD }

data class Key(
    val label: String,
    val type: KeyType,
    val code: Int = -1,
    val weight: Float = 1f
) {
    var x: Float = 0f
    var y: Float = 0f
    var width: Float = 0f
    var height: Float = 0f

    fun contains(px: Float, py: Float): Boolean =
        px >= x && px <= x + width && py >= y && py <= y + height

    fun centerX() = x + width / 2f
    fun centerY() = y + height / 2f
}
