package cn.yiiguxing.plugin.figlet

import com.github.dtmo.jfiglet.FigFont
import com.github.dtmo.jfiglet.FigletRenderer
import com.github.dtmo.jfiglet.LayoutOptions

class FixedSmushingFigletRenderer(private val figFont: FigFont) : FigletRenderer(figFont) {

    val horizontalSmushingMode get() = smushMode.horizontalSmushingMode
    val verticalSmushingMode get() = smushMode.verticalSmushingMode

    override fun renderText(text: String): String {
        val figLines = super.renderText(text)
            .lines()
            .chunked(figFont.height)
        var output = figLines.first()
        for (i in 1 until figLines.size) {
            output = smushVerticalFigLines(output, figLines[i], smushMode)
        }

        return output.joinToString("\n")
    }

    companion object {
        private const val HORIZONTAL_SMUSHING_MARK = 0xFF
        private const val VERTICAL_SMUSHING_MARK = 0x7F00
        private const val VERTICAL_SMUSHING_RULE_MARK = 0x1F00

        val Int.horizontalSmushingMode get() = this and HORIZONTAL_SMUSHING_MARK
        val Int.verticalSmushingMode get() = this and VERTICAL_SMUSHING_MARK
        private val Int.verticalSmushingRule get() = this and VERTICAL_SMUSHING_RULE_MARK

        private const val RESULT_VALID = 0
        private const val RESULT_END = 1
        private const val RESULT_INVALID = 2

        private const val RULE2_STR = "|/\\[]{}()<>"
        private const val RULE3_CLASSES = "| /\\ [] {} () <>"

        private fun <T> List<T>.chunked(count: Int): List<MutableList<T>> {
            val result = ArrayList<MutableList<T>>()
            for (i in 0 until size step count) {
                result.add(subList(i, Math.min(i + count, size)).toMutableList())
            }

            return result
        }

        private fun smushVerticalFigLines(
            figLine1: MutableList<String>,
            figLine2: MutableList<String>,
            smushMode: Int
        ): MutableList<String> {
            val len1 = figLine1[0].length
            val len2 = figLine2[0].length

            if (len1 > len2) {
                padLines(figLine2, len1 - len2)
            } else if (len2 > len1) {
                padLines(figLine1, len2 - len1)
            }

            val overlap = getVerticalSmushDist(figLine1, figLine2, smushMode)
            return verticalSmush(figLine1, figLine2, overlap, smushMode)
        }

        private fun padLines(lines: MutableList<String>, numSpaces: Int) {
            val padding = StringBuilder()
            for (i in 0 until numSpaces) {
                padding.append(' ')
            }
            for (i in lines.indices) {
                lines[i] += padding.toString()
            }
        }

        private fun verticalSmush(
            figLine1: MutableList<String>,
            figLine2: MutableList<String>,
            overlap: Int,
            smushMode: Int
        ): MutableList<String> {
            val length1 = figLine1.size
            val length2 = figLine2.size
            val piece = figLine1.take(Math.max(0, length1 - overlap)).toMutableList()
            val piece1 = figLine1.takeLast(overlap)
            val piece2 = figLine2.take(Math.min(overlap, length2))

            for (i in piece1.indices) {
                val line = if (i >= length2) {
                    piece1[i]
                } else {
                    verticallySmushLines(piece1[i], piece2[i], smushMode)
                }
                piece.add(line)
            }

            piece.addAll(figLine2.takeLast(length2 - Math.min(overlap, length2)))

            return piece
        }

        private fun verticallySmushLines(line1: String, line2: String, smushMode: Int): String {
            val vSmushMode = smushMode.verticalSmushingMode
            val isFitting =
                LayoutOptions.islayoutOptionSelected(vSmushMode, LayoutOptions.VERTICAL_FITTING_BY_DEFAULT)
            val isUniversalSmushing =
                LayoutOptions.islayoutOptionSelected(vSmushMode, LayoutOptions.VERTICAL_SMUSHING_BY_DEFAULT) &&
                        vSmushMode.verticalSmushingRule == 0

            val result = StringBuilder()
            for (i in 0 until Math.min(line1.length, line2.length)) {
                val char1 = line1[i]
                val char2 = line2[i]
                if (char1 != ' ' && char2 != ' ') {
                    if (isFitting || isUniversalSmushing) {
                        result.append(uniSmush(char1, char2))
                    } else {
                        var validSmush: Char? = null
                        if (LayoutOptions.islayoutOptionSelected(
                                vSmushMode,
                                LayoutOptions.VERTICAL_VERTICAL_LINE_SMUSHING
                            )
                        ) {
                            validSmush = vRule5Smush(char1, char2)
                        }
                        if (validSmush == null) {
                            validSmush = vSmush(char1, char2, smushMode)
                        }

                        validSmush?.let { result.append(it) }
                    }
                } else {
                    result.append(uniSmush(char1, char2))
                }
            }

            return result.toString()
        }

        private fun vSmush(char1: Char, char2: Char, vSmushMode: Int): Char? {
            var validSmush: Char? = null
            if (LayoutOptions.islayoutOptionSelected(vSmushMode, LayoutOptions.VERTICAL_EQUAL_CHARACTER_SMUSHING)) {
                validSmush = vRule1Smush(char1, char2)
            }
            if (validSmush == null &&
                LayoutOptions.islayoutOptionSelected(vSmushMode, LayoutOptions.VERTICAL_UNDERSCORE_SMUSHING)
            ) {
                validSmush = vRule2Smush(char1, char2)
            }
            if (validSmush == null &&
                LayoutOptions.islayoutOptionSelected(vSmushMode, LayoutOptions.VERTICAL_HIERARCHY_SMUSHING)
            ) {
                validSmush = vRule3Smush(char1, char2)
            }
            if (validSmush == null &&
                LayoutOptions.islayoutOptionSelected(vSmushMode, LayoutOptions.VERTICAL_HORIZONTAL_LINE_SMUSHING)
            ) {
                validSmush = vRule4Smush(char1, char2)
            }

            return validSmush
        }

        private fun getVerticalSmushDist(
            figLine1: MutableList<String>,
            figLine2: MutableList<String>,
            smushMode: Int
        ): Int {
            val maxDist = figLine1.size
            var curDist = 1
            while (curDist <= maxDist) {
                val lines1 = figLine1.takeLast(Math.max(curDist, 0))
                val lines2 = figLine2.take(Math.min(curDist, maxDist))
                var result = -1
                loop@ for (i in lines1.indices) {
                    when (checkVerticalSmush(lines1[i], lines2[i], smushMode)) {
                        RESULT_END -> result = RESULT_END
                        RESULT_INVALID -> {
                            result = RESULT_INVALID
                            break@loop
                        }
                        else -> {
                            if (result == -1) {
                                result = RESULT_VALID
                            }
                        }
                    }
                }

                if (result == RESULT_INVALID) {
                    curDist--
                    break
                }
                if (result == RESULT_END) {
                    break
                }
                if (result == RESULT_VALID) {
                    curDist++
                }
            }

            return Math.min(maxDist, curDist)
        }

        private fun checkVerticalSmush(line1: String, line2: String, smushMode: Int): Int {
            val vSmushMode = smushMode.verticalSmushingMode
            if (vSmushMode == 0) {
                return RESULT_INVALID
            }

            val length = Math.min(line1.length, line2.length)
            if (length == 0) {
                return RESULT_INVALID
            }

            val isFitting =
                LayoutOptions.islayoutOptionSelected(vSmushMode, LayoutOptions.VERTICAL_FITTING_BY_DEFAULT)
            val isUniversalSmushing =
                LayoutOptions.islayoutOptionSelected(vSmushMode, LayoutOptions.VERTICAL_SMUSHING_BY_DEFAULT) &&
                        vSmushMode.verticalSmushingRule == 0

            var endSmush = false
            for (i in 0 until length) {
                val char1 = line1[i]
                val char2 = line2[i]
                if (char1 != ' ' && char2 != ' ') {
                    if (isFitting) {
                        return RESULT_INVALID
                    }
                    if (isUniversalSmushing) {
                        return RESULT_END
                    }
                    if (vRule5Smush(char1, char2) != null) {
                        endSmush = endSmush || false
                        continue
                    }

                    endSmush = true
                    if (vSmush(char1, char2, smushMode) == null) {
                        return RESULT_INVALID
                    }
                }
            }

            return if (endSmush) RESULT_END else RESULT_VALID
        }

        private fun uniSmush(ch1: Char, ch2: Char?, hardBlank: Char? = null): Char {
            return if (ch2 == null || ch2 == ' ') {
                ch1
            } else if (ch2 == hardBlank && ch1 != ' ') {
                ch1
            } else {
                ch2
            }
        }

        private fun vRule1Smush(ch1: Char, ch2: Char): Char? {
            return ch1.takeIf { it == ch2 }
        }

        private fun vRule2Smush(ch1: Char, ch2: Char): Char? {
            if (ch1 == '_') {
                if (ch2 in RULE2_STR) {
                    return ch2
                }
            } else if (ch2 == '_') {
                if (ch1 in RULE2_STR) {
                    return ch1
                }
            }
            return null
        }

        private fun vRule3Smush(ch1: Char, ch2: Char): Char? {
            val r3Pos1 = RULE3_CLASSES.indexOf(ch1)
            val r3Pos2 = RULE3_CLASSES.indexOf(ch2)
            if (r3Pos1 != -1 && r3Pos2 != -1) {
                if (r3Pos1 != r3Pos2 && Math.abs(r3Pos1 - r3Pos2) != 1) {
                    return RULE3_CLASSES[Math.max(r3Pos1, r3Pos2)]
                }
            }
            return null
        }

        private fun vRule4Smush(ch1: Char, ch2: Char): Char? {
            if ((ch1 == '-' && ch2 == '_') || (ch1 == '_' && ch2 == '-')) {
                return '='
            }
            return null
        }

        private fun vRule5Smush(ch1: Char, ch2: Char): Char? {
            if (ch1 == '|' && ch2 == '|') {
                return '|'
            }
            return null
        }
    }
}