package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.ListPopup
import com.intellij.openapi.ui.popup.ListSeparator
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.geom.GeneralPath
import javax.swing.JButton
import javax.swing.SwingConstants

class FigFontComboBoxButton(currentFont: String, commonFonts: List<String>) : JButton(currentFont) {

    private val isUnderDarcula = UIUtil.isUnderDarcula()
    private val arrowShape = createArrowShape()
    private val popupStep = FigFontStep(commonFonts.toMutableList()
        .apply {
            add(MORE)
            add(TEST_ALL)
        })
    private var popup: ListPopup? = null
    private var popupLocation: Int? = null
    private var onFontChangedHandler: ((String) -> Unit)? = null
    private var onTestHandler: (() -> String?)? = null

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

        addActionListener { showPopup() }
    }

    override fun processMouseEvent(e: MouseEvent) {
        try {
            popupLocation = e.x.takeIf { e.id == MouseEvent.MOUSE_RELEASED }
            super.processMouseEvent(e)
        } finally {
            popupLocation = null
        }
    }

    fun onFontChanged(handler: ((font: String) -> Unit)?) {
        onFontChangedHandler = handler
    }

    fun onTestAllFont(handler: (() -> String?)?) {
        onTestHandler = handler
    }

    private fun showPopup() {
        if (popup?.isDisposed == false) {
            return
        }

        val factory = JBPopupFactory.getInstance()
        popup = factory.createListPopup(popupStep, 30)
            .apply {
                popupLocation?.let { location ->
                    show(RelativePoint(this@FigFontComboBoxButton, Point(location, visibleRect.height)))
                } ?: show(factory.guessBestPopupLocation(this@FigFontComboBoxButton))
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
        private const val TEST_ALL = "Test All..."

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
            return if (value == TEST_ALL) ListSeparator() else null
        }

        override fun onChosen(selectedValue: String, finalChoice: Boolean): PopupStep<*>? {
            return when {
                selectedValue == MORE -> AllFigFontStep().apply {
                    defaultOptionIndex = FIGlet.fonts.indexOf(currentFont)
                }

                selectedValue == TEST_ALL -> doFinalStep {
                    ApplicationManager.getApplication().invokeLater {
                        onTestHandler?.invoke()?.let { currentFont = it }
                    }
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