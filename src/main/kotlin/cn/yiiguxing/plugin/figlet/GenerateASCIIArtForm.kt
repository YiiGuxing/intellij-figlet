package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.application.TransactionGuard
import com.intellij.openapi.editor.event.DocumentAdapter
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.EditorTextField
import com.intellij.ui.ListCellRendererWrapper
import com.intellij.util.Alarm
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

class GenerateASCIIArtForm(private val project: Project, private val defaultInputText: String) {

    private lateinit var contentPanel: JPanel
    private lateinit var inputTextField: EditorTextField
    private lateinit var fontComboBoxButton: FigFontComboBoxButton
    private lateinit var verticalLayoutComboBox: ComboBox<FIGlet.Layout>
    private lateinit var horizontalLayoutComboBox: ComboBox<FIGlet.Layout>
    private lateinit var previewComponent: JComponent

    var callback: Callback? = null

    val component: JComponent get() = contentPanel

    val preferredFocusedComponent: JComponent? get() = inputTextField.takeIf { defaultInputText.isBlank() }

    init {
        val renderer = LayoutRenderer()
        verticalLayoutComboBox.renderer = renderer
        horizontalLayoutComboBox.renderer = renderer

        val layouts = FIGlet.Layout.values().asList()
        verticalLayoutComboBox.model = CollectionComboBoxModel(layouts)
        horizontalLayoutComboBox.model = CollectionComboBoxModel(layouts)
    }

    private fun createUIComponents() {
        contentPanel = ContentPanel()
        inputTextField = TextField(defaultInputText, project)

        val state = DataManager.instance.state
        fontComboBoxButton = FigFontComboBoxButton(state.lastUsedFont, state.commonFonts)
        previewComponent = JButton("Hello!")
    }

    private fun setResult(asciiArtText: String) {
        contentPanel.revalidate()
        callback?.onResult(asciiArtText)
    }

    private class TextField(text: String, project: Project?) :
        EditorTextField(text, project, PlainTextFileType.INSTANCE) {

        init {
            isOneLineMode = false
        }

        override fun updateBorder(editor: EditorEx) {
            setupBorder(editor)
        }
    }

    private inner class ContentPanel : JPanel() {
        private val disposable: Disposable = Disposer.newDisposable()
        private val updater: Alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, disposable)

        override fun addNotify() {
            super.addNotify()
            object : AnAction() {
                override fun actionPerformed(e: AnActionEvent) {
                    IdeFocusManager.findInstance().requestFocus(fontComboBoxButton, true)
                }
            }.registerCustomShortcutSet(CustomShortcutSet.fromString("shift TAB"), inputTextField)

            inputTextField.addDocumentListener(object : DocumentAdapter() {
                override fun documentChanged(e: DocumentEvent) = update()
            })
            update()
        }

        fun update() {
            callback?.onUpdate()
            val transactionId = TransactionGuard.getInstance().contextTransaction
            updater.cancelAllRequests()
            if (!updater.isDisposed) {
                updater.addRequest({
                    TransactionGuard.getInstance().submitTransaction(project, transactionId, Runnable {
                        setResult("asciiArtText")
                    })
                }, 200)
            }
        }

        override fun removeNotify() {
            super.removeNotify()
            Disposer.dispose(disposable)
        }
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

    interface Callback {
        fun onUpdate() {}

        fun onResult(asciiArtText: String) {}

        fun onError(msg: String) {}
    }

}
