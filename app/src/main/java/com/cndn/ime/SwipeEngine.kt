package com.cndn.ime

object SwipeEngine {

    data class Suggestion(val word: String, val score: Double)

    fun suggest(path: List<Char>, maxResults: Int = 3): List<Suggestion> {
        if (path.size < 2) return emptyList()
        val cleanPath = collapseRepeats(path)

        val scored = Dictionary.words.mapNotNull { word ->
            val score = scoreWord(word, cleanPath)
            if (score > 0) Suggestion(word, score) else null
        }

        return scored.sortedByDescending { it.score }.take(maxResults)
    }

    private fun collapseRepeats(path: List<Char>): List<Char> {
        val result = mutableListOf<Char>()
        for (c in path) {
            if (result.isEmpty() || result.last() != c) result.add(c)
        }
        return result
    }

    private fun scoreWord(word: String, path: List<Char>): Double {
        val w = word.lowercase()
        if (w.isEmpty()) return 0.0

        var score = 0.0

        if (w.first() == path.first()) score += 3.0
        if (w.last() == path.last()) score += 2.0

        var pi = 0
        var matched = 0
        for (ch in w) {
            while (pi < path.size && path[pi] != ch) pi++
            if (pi < path.size) {
                matched++
                pi++
            }
        }
        score += matched * 1.5

        val coverage = matched.toDouble() / w.length
        if (coverage < 0.6) return 0.0

        val lengthDiff = kotlin.math.abs(w.length - path.size)
        score -= lengthDiff * 0.4

        return score
    }
}
