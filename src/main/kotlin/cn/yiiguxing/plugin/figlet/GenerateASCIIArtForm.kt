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
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.EditorTextField
import com.intellij.ui.ListCellRendererWrapper
import com.intellij.util.Alarm
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JList
import javax.swing.JPanel

class GenerateASCIIArtForm(private val project: Project, private val defaultInputText: String) : Disposable {

    private lateinit var contentPanel: JPanel
    private lateinit var inputTextField: EditorTextField
    private lateinit var fontComboBoxButton: FigFontComboBoxButton
    private lateinit var verticalLayoutComboBox: ComboBox<FIGlet.Layout>
    private lateinit var horizontalLayoutComboBox: ComboBox<FIGlet.Layout>
    private lateinit var previewComponent: JComponent

    private val updater: Alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)

    lateinit var callback: Callback

    val currentFont: String get() = fontComboBoxButton.currentFont

    val component: JComponent get() = contentPanel

    val preferredFocusedComponent: JComponent? get() = inputTextField.takeIf { defaultInputText.isBlank() }

    init {
        val renderer = LayoutRenderer()
        verticalLayoutComboBox.renderer = renderer
        horizontalLayoutComboBox.renderer = renderer

        val layouts = FIGlet.Layout.values().asList()
        verticalLayoutComboBox.model = CollectionComboBoxModel(layouts)
        horizontalLayoutComboBox.model = CollectionComboBoxModel(layouts)

        initListeners()
    }

    private fun createUIComponents() {
        contentPanel = object : JPanel() {
            override fun addNotify() {
                super.addNotify()
                update()
            }
        }
        inputTextField = TextField(defaultInputText, project)

        val state = DataManager.instance.state
        fontComboBoxButton = FigFontComboBoxButton(state.lastUsedFont, state.commonFonts)
        previewComponent = JButton("Hello!")
    }

    private fun initListeners() {
        object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                IdeFocusManager.findInstance().requestFocus(fontComboBoxButton, true)
            }
        }.registerCustomShortcutSet(CustomShortcutSet.fromString("shift TAB"), inputTextField)

        inputTextField.addDocumentListener(object : DocumentAdapter() {
            override fun documentChanged(e: DocumentEvent) = update()
        })
        fontComboBoxButton.onFontChanged { update() }
        val itemListener = ItemListener {
            if (it.stateChange == ItemEvent.SELECTED) {
                update()
            }
        }
        verticalLayoutComboBox.addItemListener(itemListener)
        horizontalLayoutComboBox.addItemListener(itemListener)
    }

    private fun update() {
        callback.onUpdate()
        val transactionId = TransactionGuard.getInstance().contextTransaction
        updater.cancelAllRequests()
        if (!updater.isDisposed) {
            updater.addRequest({
                var errorMsg: String? = null
                val asciiArtText: String? = try {
                    callback.generateASCIIArtText()
                } catch (e: Throwable) {
                    errorMsg = "Cannot generate ASCII art text: ${e.message}"
                    null
                }

                TransactionGuard.getInstance().submitTransaction(project, transactionId, Runnable {
                    asciiArtText?.takeIf { it.isNotBlank() }?.let { setResult(it) }
                    errorMsg?.let { callback.onError(it) }
                })
            }, 200)
        }
    }

    private fun Callback.generateASCIIArtText(): String {
        val inputText = inputTextField.text
        if (inputText.isBlank()) {
            return inputText
        }

        val verticalLayout = verticalLayoutComboBox.selectedItem as FIGlet.Layout
        val horizontalLayout = horizontalLayoutComboBox.selectedItem as FIGlet.Layout

        return onGenerateASCIIArtText(inputText, currentFont, verticalLayout, horizontalLayout)
    }

    private fun setResult(asciiArtText: String) {
        contentPanel.revalidate()
        callback.onResult(asciiArtText)
    }

    override fun dispose() {
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
        fun onUpdate()

        fun onGenerateASCIIArtText(
            inputText: String,
            fontName: String,
            verticalLayout: FIGlet.Layout,
            horizontalLayout: FIGlet.Layout
        ): String

        fun onResult(asciiArtText: String)

        fun onError(msg: String)
    }

}
