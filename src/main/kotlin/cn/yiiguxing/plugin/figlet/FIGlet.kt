package cn.yiiguxing.plugin.figlet

import cn.yiiguxing.plugin.figlet.FIGlet.Layout.*
import com.github.dtmo.jfiglet.FigFont
import com.github.dtmo.jfiglet.FigFontReader
import com.github.dtmo.jfiglet.LayoutOptions
import java.io.InputStreamReader

object FIGlet {

    const val DEFAULT_FONT = "Graffiti"

    val FEATURED_FONTS: List<String> = listOf(
        DEFAULT_FONT,
        "Rectangles",
        "Slant",
        "Standard",
        "ANSI Shadow"
    )

    val fonts: List<String> = listOf(
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

    private val fontCache: MutableMap<String, FigFont> = HashMap()

    enum class Layout(val displayName: String) {
        DEFAULT("Default"),
        FULL("Full"),
        FITTED("Fitted"),
        CONTROLLED_SMUSHING("Controlled Smushing"),
        UNIVERSAL_SMUSHING("Universal Smushing")
    }

    fun generate(
        text: String,
        figFont: FigFont,
        horizontalLayout: Layout,
        verticalLayout: Layout,
        direction: FigFont.PrintDirection = FigFont.PrintDirection.LEFT_TO_RIGHT
    ): String {
        val renderer = FixedFigletRenderer(figFont).apply {
            val hSmushing = getHorizontalSmushingMode(horizontalLayout)
            val vSmushing = getVerticalSmushingMode(verticalLayout)
            smushMode = hSmushing or vSmushing
            printDirection = direction
        }

        return renderer.renderText(text)
    }

    private fun FixedFigletRenderer.getHorizontalSmushingMode(layout: Layout): Int {
        return when (layout) {
            DEFAULT -> horizontalSmushingMode
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

    private fun FixedFigletRenderer.getVerticalSmushingMode(layout: Layout): Int {
        return when (layout) {
            DEFAULT -> verticalSmushingMode
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
        val inputStream = FIGlet::class.java.getResourceAsStream("/fonts/$fontName.flf")
        return InputStreamReader(inputStream, Charsets.UTF_8).use { FigFontReader(it).readFont() }
    }


    @Synchronized
    fun getFigFont(name: String): FigFont {
        return fontCache.getOrPut(name) { FIGlet.loadFigFont(name) }
    }

    @Synchronized
    fun clearFontCache(reserved: String? = null) {
        val reservedFont = reserved?.let { fontCache[it] }
        fontCache.clear()
        reservedFont?.let { fontCache[reserved] = it }
    }

}

