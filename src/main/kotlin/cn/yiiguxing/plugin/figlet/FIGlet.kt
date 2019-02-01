package cn.yiiguxing.plugin.figlet

import cn.yiiguxing.plugin.figlet.FIGlet.Layout.*
import com.github.dtmo.jfiglet.FigletRenderer
import com.github.dtmo.jfiglet.FigFont
import com.github.dtmo.jfiglet.LayoutOptions
import java.lang.Math.max
import java.lang.Math.min

object FIGlet {

    private const val HORIZONTAL_SMUSHING_MARK = 0xFF
    private const val VERTICAL_SMUSHING_MARK = 0x7F00
    private const val VERTICAL_SMUSHING_RULE_MARK = 0x1F00

    private val Int.horizontalSmushingMode get() = this and HORIZONTAL_SMUSHING_MARK
    private val Int.verticalSmushingMode get() = this and VERTICAL_SMUSHING_MARK
    private val Int.verticalSmushingRule get() = this and VERTICAL_SMUSHING_RULE_MARK

    val fonts: Array<out String> = arrayOf(
        "1Row",
        "3-D",
        "3D Diagonal",
        "3D-ASCII",
        "3x5",
        "4Max",
        "5 Line Oblique",
        "Acrobatic",
        "Alligator",
        "Alligator2",
        "Alpha",
        "Alphabet",
        "AMC 3 Line",
        "AMC 3 Liv1",
        "AMC AAA01",
        "AMC Neko",
        "AMC Razor",
        "AMC Razor2",
        "AMC Slash",
        "AMC Slider",
        "AMC Thin",
        "AMC Tubes",
        "AMC Untitled",
        "ANSI Shadow",
        "Arrows",
        "ASCII New Roman",
        "Avatar",
        "B1FF",
        "Banner",
        "Banner3-D",
        "Banner3",
        "Banner4",
        "Barbwire",
        "Basic",
        "Bear",
        "Bell",
        "Benjamin",
        "Big Chief",
        "Big Money-ne",
        "Big Money-nw",
        "Big Money-se",
        "Big Money-sw",
        "Big",
        "Bigfig",
        "Binary",
        "Block",
        "Blocks",
        "Bloody",
        "Bolger",
        "Braced",
        "Bright",
        "Broadway KB",
        "Broadway",
        "Bubble",
        "Bulbhead",
        "Caligraphy",
        "Caligraphy2",
        "Calvin S",
        "Cards",
        "Catwalk",
        "Chiseled",
        "Chunky",
        "Coinstak",
        "Cola",
        "Colossal",
        "Computer",
        "Contessa",
        "Contrast",
        "Cosmike",
        "Crawford",
        "Crawford2",
        "Crazy",
        "Cricket",
        "Cursive",
        "Cyberlarge",
        "Cybermedium",
        "Cybersmall",
        "Cygnet",
        "DANC4",
        "Dancing Font",
        "Decimal",
        "Def Leppard",
        "Delta Corps Priest 1",
        "Diamond",
        "Diet Cola",
        "Digital",
        "Doh",
        "Doom",
        "DOS Rebel",
        "Dot Matrix",
        "Double Shorts",
        "Double",
        "Dr Pepper",
        "DWhistled",
        "Efti Chess",
        "Efti Font",
        "Efti Italic",
        "Efti Piti",
        "Efti Robot",
        "Efti Wall",
        "Efti Water",
        "Electronic",
        "Elite",
        "Epic",
        "Fender",
        "Filter",
        "Fire Font-k",
        "Fire Font-s",
        "Flipped",
        "Flower Power",
        "Four Tops",
        "Fraktur",
        "Fun Face",
        "Fun Faces",
        "Fuzzy",
        "Georgi16",
        "Georgia11",
        "Ghost",
        "Ghoulish",
        "Glenyn",
        "Goofy",
        "Gothic",
        "Graceful",
        "Gradient",
        "Graffiti",
        "Greek",
        "Heart Left",
        "Heart Right",
        "Henry 3D",
        "Hex",
        "Hieroglyphs",
        "Hollywood",
        "Horizontal Left",
        "Horizontal Right",
        "ICL-1900",
        "Impossible",
        "Invita",
        "Isometric1",
        "Isometric2",
        "Isometric3",
        "Isometric4",
        "Italic",
        "Ivrit",
        "Jacky",
        "Jazmine",
        "Jerusalem",
        "JS Block Letters",
        "JS Bracket Letters",
        "JS Capital Curves",
        "JS Cursive",
        "JS Stick Letters",
        "Katakana",
        "Kban",
        "Keyboard",
        "Knob",
        "Konto Slant",
        "Konto",
        "Larry 3D 2",
        "Larry 3D",
        "LCD",
        "Lean",
        "Letters",
        "Lil Devil",
        "Line Blocks",
        "Linux",
        "Lockergnome",
        "Madrid",
        "Marquee",
        "Maxfour",
        "Merlin1",
        "Merlin2",
        "Mike",
        "Mini",
        "Mirror",
        "Mnemonic",
        "Modular",
        "Morse",
        "Morse2",
        "Moscow",
        "Mshebrew210",
        "Muzzle",
        "Nancyj-Fancy",
        "Nancyj-Improved",
        "Nancyj-Underlined",
        "Nancyj",
        "Nipples",
        "NScript",
        "NT Greek",
        "NV Script",
        "O8",
        "Octal",
        "Ogre",
        "Old Banner",
        "OS2",
        "Patorjk's Cheese",
        "Patorjk-HeX",
        "Pawp",
        "Peaks Slant",
        "Peaks",
        "Pebbles",
        "Pepper",
        "Poison",
        "Puffy",
        "Puzzle",
        "Pyramid",
        "Rammstein",
        "Rectangles",
        "Red Phoenix",
        "Relief",
        "Relief2",
        "Reverse",
        "Roman",
        "Rot13",
        "Rotated",
        "Rounded",
        "Rowan Cap",
        "Rozzo",
        "Runic",
        "Runyc",
        "S Blood",
        "Santa Clara",
        "Script",
        "Serifcap",
        "Shadow",
        "Shimrod",
        "Short",
        "SL Script",
        "Slant Relief",
        "Slant",
        "Slide",
        "Small Caps",
        "Small Isometric1",
        "Small Keyboard",
        "Small Poison",
        "Small Script",
        "Small Shadow",
        "Small Slant",
        "Small Tengwar",
        "Small",
        "Soft",
        "Speed",
        "Spliff",
        "Stacey",
        "Stampate",
        "Stampatello",
        "Standard",
        "Star Strips",
        "Star Wars",
        "Stellar",
        "Stforek",
        "Stick Letters",
        "Stop",
        "Straight",
        "Stronger Than All",
        "Sub-Zero",
        "Swamp Land",
        "Swan",
        "Sweet",
        "Tanja",
        "Tengwar",
        "Term",
        "Test1",
        "The Edge",
        "Thick",
        "Thin",
        "THIS",
        "Thorned",
        "Three Point",
        "Ticks Slant",
        "Ticks",
        "Tiles",
        "Tinker-Toy",
        "Tombstone",
        "Train",
        "Trek",
        "Tsalagi",
        "Tubular",
        "Twisted",
        "Two Point",
        "Univers",
        "USA Flag",
        "Varsity",
        "Wavy",
        "Weird",
        "Wet Letter",
        "Whimsy",
        "Wow"
    )

    enum class Layout(val displayName: String) {
        DEFAULT("Default"),
        FULL("Full"),
        FITTED("Fitted"),
        CONTROLLED_SMUSHING("Controlled Smushing"),
        UNIVERSAL_SMUSHING("Universal Smushing")
    }

    fun generate(text: String, figFont: FigFont, horizontalLayout: Layout, verticalLayout: Layout): String {
        val renderer = Renderer(figFont).apply {
            val hSmushing = getHorizontalSmushingMode(horizontalLayout)
            val vSmushing = getVerticalSmushingMode(verticalLayout)
            smushMode = hSmushing or vSmushing
        }

        return renderer.renderText(text)
    }

    private fun Renderer.getHorizontalSmushingMode(layout: Layout): Int {
        return when (layout) {
            DEFAULT -> smushMode.horizontalSmushingMode
            FULL -> 0
            FITTED -> LayoutOptions.HORIZONTAL_FITTING_BY_DEFAULT
            CONTROLLED_SMUSHING -> LayoutOptions.HORIZONTAL_SMUSHING_BY_DEFAULT or
                    LayoutOptions.HORIZONTAL_EQUAL_CHARACTER_SMUSHING or
                    LayoutOptions.HORIZONTAL_UNDERSCORE_SMUSHING or
                    LayoutOptions.HORIZONTAL_HIERARCHY_SMUSHING or
                    LayoutOptions.HORIZONTAL_OPPOSITE_PAIR_SMUSHING or
                    LayoutOptions.HORIZONTAL_BIG_X_SMUSHING or
                    LayoutOptions.HORIZONTAL_HARDBLANK_SMUSHING
            UNIVERSAL_SMUSHING -> LayoutOptions.HORIZONTAL_SMUSHING_BY_DEFAULT
        }
    }

    private fun Renderer.getVerticalSmushingMode(layout: Layout): Int {
        return when (layout) {
            DEFAULT -> smushMode.verticalSmushingMode
            FULL -> 0
            FITTED -> LayoutOptions.VERTICAL_FITTING_BY_DEFAULT
            CONTROLLED_SMUSHING -> LayoutOptions.VERTICAL_SMUSHING_BY_DEFAULT or
                    LayoutOptions.VERTICAL_EQUAL_CHARACTER_SMUSHING or
                    LayoutOptions.VERTICAL_UNDERSCORE_SMUSHING or
                    LayoutOptions.VERTICAL_HIERARCHY_SMUSHING or
                    LayoutOptions.VERTICAL_HORIZONTAL_LINE_SMUSHING or
                    LayoutOptions.VERTICAL_VERTICAL_LINE_SMUSHING
            UNIVERSAL_SMUSHING -> LayoutOptions.VERTICAL_SMUSHING_BY_DEFAULT
        }
    }

    fun loadFigFont(fontName: String): FigFont {
        return FIGlet::class.java
            .getResourceAsStream("/fonts/$fontName.flf")
            .use { FigFont.loadFigFont(it) }
    }

    private class Renderer(private val figFont: FigFont) : FigletRenderer(figFont) {

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
            private const val RESULT_VALID = 0
            private const val RESULT_END = 1
            private const val RESULT_INVALID = 2

            private const val RULE2_STR = "|/\\[]{}()<>"
            private const val RULE3_CLASSES = "| /\\ [] {} () <>"

            private fun <T> List<T>.chunked(count: Int): List<MutableList<T>> {
                val result = ArrayList<MutableList<T>>()
                for (i in 0 until size step count) {
                    result.add(subList(i, min(i + count, size)).toMutableList())
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
                val piece = figLine1.take(max(0, length1 - overlap)).toMutableList()
                val piece1 = figLine1.takeLast(overlap)
                val piece2 = figLine2.take(min(overlap, length2))

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
                for (i in 0 until min(line1.length, line2.length)) {
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
                    val lines1 = figLine1.takeLast(max(curDist, 0))
                    val lines2 = figLine2.take(min(curDist, maxDist))
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

                return min(maxDist, curDist)
            }

            private fun checkVerticalSmush(line1: String, line2: String, smushMode: Int): Int {
                val vSmushMode = smushMode.verticalSmushingMode
                if (vSmushMode == 0) {
                    return RESULT_INVALID
                }

                val length = min(line1.length, line2.length)
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

}

