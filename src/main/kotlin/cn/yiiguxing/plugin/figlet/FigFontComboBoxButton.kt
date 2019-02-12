package cn.yiiguxing.plugin.figlet

import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Shape
import java.awt.geom.GeneralPath
import javax.swing.JButton
import javax.swing.SwingConstants

class FigFontComboBoxButton : JButton(FIGlet.DEFAULT_FONT) {

    private val isUnderDarcula = UIUtil.isUnderDarcula()
    private val arrowShape = createArrowShape()

    init {
        val margins = margin
        margin = JBUI.insets(margins.top, 10, margins.bottom, 30)
        horizontalAlignment = SwingConstants.LEFT

        addActionListener {
            // TODO: check if the popup is not showing.
            showPopup()
        }
    }

    private fun showPopup() {
        // TODO: show the fonts popup
    }

    override fun paint(g: Graphics) {
        g as Graphics2D
        super.paint(g)

        val x = (width - JBUI.scale(10) - insets.right + 1).toDouble()
        val y = (height - JBUI.scale(5.0f)) * 0.5
        g.translate(x, y)

        if (!isUnderDarcula) {
            g.color = Color.DARK_GRAY
        }
        g.fill(arrowShape)
        g.translate(-x, -y)
    }

    companion object {
        private fun createArrowShape(): Shape {
            return GeneralPath().apply {
                moveTo(0.0f, 0.0f)
                lineTo(JBUI.scale(10.0f), 0.0f)
                lineTo(JBUI.scale(5.0f), 5.0f)
                closePath()
            }
        }
    }

}