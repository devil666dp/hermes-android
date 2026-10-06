package org.hermes.android.data.network

object LossyTextReconciler {

    private val DENSE_SCRIPT_REGEX = Regex("[\\p{IsHan}\\p{IsHiragana}\\p{IsKatakana}\\p{IsHangul}]")
    private const val DENSE_SCRIPT_MIN_RUN = 6

    fun normalizeText(value: String): String {
        return value.replace(Regex("\\s+"), " ").trim()
    }

    private fun isDenseScriptChar(codePoint: Int): Boolean {
        val s = String(Character.toChars(codePoint))
        return DENSE_SCRIPT_REGEX.containsMatchIn(s)
    }

    private fun isDenseScriptHeavy(codePoints: IntArray): Boolean {
        if (codePoints.isEmpty()) return false
        var denseCount = 0
        for (cp in codePoints) {
            if (isDenseScriptChar(cp)) denseCount++
        }
        return (denseCount.toDouble() / codePoints.size) > 0.3
    }

    private fun toCodePoints(s: String): IntArray {
        val count = s.codePointCount(0, s.length)
        val result = IntArray(count)
        var charIndex = 0
        for (i in 0 until count) {
            val cp = s.codePointAt(charIndex)
            result[i] = cp
            charIndex += Character.charCount(cp)
        }
        return result
    }

    private fun indexOfSeq(haystack: IntArray, needle: IntArray, start: Int, len: Int, from: Int): Int {
        val maxK = haystack.size - len
        for (k in from..maxK) {
            var match = true
            for (m in 0 until len) {
                if (haystack[k + m] != needle[start + m]) {
                    match = false
                    break
                }
            }
            if (match) return k
        }
        return -1
    }

    fun isLossyChunkCopy(
        partial: String,
        full: String,
        minRun: Int? = null,
        minLength: Int = 12,
        minCoverage: Double = 0.3
    ): Boolean {
        if (partial.isEmpty() || full.isEmpty()) return false
        val partialCodePoints = toCodePoints(partial)
        val fullCodePoints = toCodePoints(full)

        if (partialCodePoints.size < minLength) return false
        if (partialCodePoints.size >= fullCodePoints.size) return false
        if (partialCodePoints.size < (minCoverage * fullCodePoints.size)) return false

        val run = minRun ?: if (isDenseScriptHeavy(partialCodePoints) || isDenseScriptHeavy(fullCodePoints)) {
            DENSE_SCRIPT_MIN_RUN
        } else {
            3
        }

        var i = 0
        var j = 0
        while (i < partialCodePoints.size) {
            val remaining = partialCodePoints.size - i
            val probeLen = minOf(run, remaining)
            val at = indexOfSeq(fullCodePoints, partialCodePoints, i, probeLen, j)
            if (at < 0) return false
            if (probeLen < run && remaining > probeLen) return false

            var len = probeLen
            while (
                i + len < partialCodePoints.size &&
                at + len < fullCodePoints.size &&
                partialCodePoints[i + len] == fullCodePoints[at + len]
            ) {
                len++
            }
            i += len
            j = at + len
        }
        return true
    }

    private fun commonSuffixLength(a: String, b: String): Int {
        var i = a.length - 1
        var j = b.length - 1
        var n = 0
        while (i >= 0 && j >= 0 && a[i] == b[j]) {
            i--
            j--
            n++
        }
        return n
    }

    private fun tailHeadOverlap(a: String, b: String): Int {
        val max = minOf(a.length, b.length)
        for (k in max downTo 1) {
            if (a.endsWith(b.substring(0, k))) {
                val aStart = a.length - k
                val startsMidWord = aStart > 0 && Character.isLetterOrDigit(a[aStart - 1]) && Character.isLetterOrDigit(a[aStart])
                val endsMidWord = k < b.length && Character.isLetterOrDigit(b[k - 1]) && Character.isLetterOrDigit(b[k])
                if (!startsMidWord && !endsMidWord) {
                    return k
                }
            }
        }
        return 0
    }

    fun mergeStreamedWithFinal(streamed: String, final: String): String {
        val streamedContent = streamed.trim()
        val finalContent = final.trim()
        if (streamedContent.isEmpty()) return finalContent
        if (finalContent.isEmpty()) return streamedContent

        val normStreamed = normalizeText(streamedContent)
        val normFinal = normalizeText(finalContent)

        if (normFinal.contains(normStreamed)) return finalContent
        if (normStreamed.contains(normFinal)) return streamedContent

        if (isLossyChunkCopy(normStreamed, normFinal)) {
            return finalContent
        }

        val overlap = tailHeadOverlap(streamedContent, finalContent)
        if (overlap > 0) {
            return streamedContent + finalContent.substring(overlap)
        }

        val suffix = commonSuffixLength(streamedContent, finalContent)
        if (suffix > 0) {
            val shared = finalContent.substring(finalContent.length - suffix)
            val meaningful = shared.replace(Regex("[\\s\\p{Punct}]"), "").length
            val shorter = minOf(streamedContent.length, finalContent.length)
            if (meaningful >= 3 && (suffix.toDouble() / shorter) >= 0.5) {
                return finalContent
            }
        }

        return "$streamedContent\n\n$finalContent"
    }
}
