package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.ListCellRendererWrapper
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

class FigPopupForm {

    private lateinit var contentPanel: JPanel
    private lateinit var figFontComboBoxButton1: FigFontComboBoxButton
    private lateinit var verticalLayoutComboBox: ComboBox<FIGlet.Layout>
    private lateinit var horizontalLayoutComboBox: ComboBox<FIGlet.Layout>
    private lateinit var previewComponent: JComponent

    val component: JComponent get() = contentPanel

    init {
        val renderer = LayoutRenderer()
        verticalLayoutComboBox.renderer = renderer
        horizontalLayoutComboBox.renderer = renderer

        val layouts = FIGlet.Layout.values().asList()
        verticalLayoutComboBox.model = CollectionComboBoxModel(layouts)
        horizontalLayoutComboBox.model = CollectionComboBoxModel(layouts)
    }

    private fun createUIComponents() {
        val state = DataManager.instance.state
        figFontComboBoxButton1 = FigFontComboBoxButton(state.lastUsedFont, state.commonFonts)
        previewComponent = JButton("Hello!")
    }

    private class LayoutRenderer : ListCellRendererWrapper<FIGlet.Layout>() {
        override fun customize(
            list: JList<*>?,
            value: FIGlet.Layout?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            setText(value?.displayName)
        }
    }

}
