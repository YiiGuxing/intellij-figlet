package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.geom.GeneralPath
import javax.swing.JButton
import javax.swing.SwingConstants

class FigFontComboBoxButton(currentFont: String, commonFonts: List<String>) : JButton(currentFont) {

    private val isUnderDarcula = UIUtil.isUnderDarcula()
    private val arrowShape = createArrowShape()
    private val popupStep = FigFontStep(commonFonts.toMutableList().apply { add(MORE) })
    private var popup: ListPopup? = null
    private var onFontChangedHandler: ((String) -> Unit)? = null

    var currentFont: String
        get() = text ?: FIGlet.DEFAULT_FONT
        set(value) {
            if (text != value) {
                text = value
                onFontChangedHandler?.invoke(value)
                popupStep.updateDefaultOptionIndex()
            }
        }

    init {
        val margins = margin
        margin = JBUI.insets(margins.top, 10, margins.bottom, 30)
        horizontalAlignment = SwingConstants.LEFT

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(event: MouseEvent) {
                if (event.clickCount == 1) {
                    showPopupAt(event.x)
                    event.consume()
                }
            }
        })
    }

    fun onFontChanged(handler: ((font: String) -> Unit)?) {
        onFontChangedHandler = handler
    }

    private fun showPopupAt(position: Int) {
        if (popup?.isDisposed == false) {
            return
        }

        popup = JBPopupFactory.getInstance().createListPopup(popupStep, 30)
            .apply {
                show(RelativePoint(this@FigFontComboBoxButton, Point(position, visibleRect.height)))
            }
    }

    override fun paint(g: Graphics) {
        g as Graphics2D
        super.paint(g)

        val x = (width - JBUI.scale(20)).toDouble()
        val y = (height - JBUI.scale(5.0f)) * 0.5
        g.translate(x, y)

        if (!isUnderDarcula) {
            g.color = Color.DARK_GRAY
        }
        g.fill(arrowShape)
        g.translate(-x, -y)
    }

    companion object {
        private const val MORE = "more"

        private fun createArrowShape(): Shape {
            return GeneralPath().apply {
                moveTo(0.0f, 0.0f)
                lineTo(JBUI.scale(10.0f), 0.0f)
                lineTo(JBUI.scale(5.0f), 5.0f)
                closePath()
            }
        }
    }

    inner class FigFontStep(commonFonts: List<String>) : BaseListPopupStep<String>(null, commonFonts) {

        init {
            updateDefaultOptionIndex()
        }

        override fun isSpeedSearchEnabled(): Boolean = true

        override fun hasSubstep(selectedValue: String): Boolean {
            return selectedValue == MORE
        }

        override fun getSeparatorAbove(value: String): ListSeparator? {
            return if (value == MORE) ListSeparator() else null
        }

        override fun onChosen(selectedValue: String, finalChoice: Boolean): PopupStep<*>? {
            return when {
                selectedValue == MORE -> AllFigFontStep().apply {
                    defaultOptionIndex = FIGlet.fonts.indexOf(currentFont)
                }
                finalChoice -> doFinalStep { currentFont = selectedValue }
                else -> PopupStep.FINAL_CHOICE
            }
        }

        fun updateDefaultOptionIndex() {
            val values = values
            val indexOfCurrentFont = values.indexOf(currentFont)
            defaultOptionIndex = if (indexOfCurrentFont >= 0) indexOfCurrentFont else (values.size - 1)
        }
    }

    inner class AllFigFontStep : BaseListPopupStep<String>(null, FIGlet.fonts) {

        override fun isSpeedSearchEnabled(): Boolean = true

        override fun onChosen(selectedValue: String, finalChoice: Boolean): PopupStep<*>? {
            return doFinalStep { currentFont = selectedValue }
        }
    }

}