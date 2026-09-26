package com.cndn.ime

object TurkishLayout {

    private val numberRow = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")

    private val row1 = listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "ğ", "ü")
    private val row2 = listOf("a", "s", "d", "f", "g", "h", "j", "k", "l", "ş", "i")
    private val row3 = listOf("z", "x", "c", "v", "b", "n", "m", "ö", "ç")

    fun letterRows(shifted: Boolean): List<List<Key>> {
        fun toKeys(letters: List<String>): List<Key> = letters.map { ch ->
            val display = if (shifted) turkishUpper(ch) else ch
            Key(display, KeyType.CHAR, display[0].code)
        }

        val r1 = toKeys(row1)
        val r2 = toKeys(row2)
        val r3mid = toKeys(row3)

        val shiftKey = Key("⇧", KeyType.SHIFT, weight = 1.5f)
        val backspaceKey = Key("⌫", KeyType.BACKSPACE, weight = 1.5f)

        return listOf(
            numberRow.map { Key(it, KeyType.CHAR, it[0].code) },
            r1,
            r2,
            listOf(shiftKey) + r3mid + listOf(backspaceKey)
        )
    }

    fun bottomRow(): List<Key> = listOf(
        Key("123", KeyType.SYMBOLS, weight = 1.3f),
        Key("☺", KeyType.EMOJI, weight = 1.1f),
        Key(",", KeyType.COMMA_PERIOD, ','.code, weight = 1f),
        Key("boşluk", KeyType.SPACE, ' '.code, weight = 4.2f),
        Key(".", KeyType.COMMA_PERIOD, '.'.code, weight = 1f),
        Key("⏎", KeyType.ENTER, weight = 1.5f)
    )

    fun turkishUpper(s: String): String = when (s) {
        "i" -> "İ"
        "ş" -> "Ş"
        "ğ" -> "Ğ"
        "ü" -> "Ü"
        "ö" -> "Ö"
        "ç" -> "Ç"
        else -> s.uppercase()
    }
}
