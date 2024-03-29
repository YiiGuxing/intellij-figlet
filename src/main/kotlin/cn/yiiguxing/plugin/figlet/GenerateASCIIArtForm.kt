package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CustomShortcutSet
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.ui.CollectionComboBoxModel
import com.intellij.ui.EditorTextField
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.util.Alarm
import com.intellij.util.ui.JBUI
import java.awt.Dimension
import java.awt.event.ItemEvent
import java.awt.event.ItemListener
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
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

    private lateinit var previewViewer: Editor

    private val updater: Alarm = Alarm(Alarm.ThreadToUse.POOLED_THREAD, this)

    lateinit var callback: Callback

    val currentFont: String get() = fontComboBoxButton.currentFont

    val component: JComponent get() = contentPanel

    val preferredFocusedComponent: JComponent get() = inputTextField

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
        fontComboBoxButton = FigFontComboBoxButton(project, state.lastUsedFont, state.commonFonts)

        previewViewer = Previews.createPreviewViewer(project, "\n\n\n\n\n\n\n\n\n")
        previewComponent = previewViewer.component.apply {
            val preSize = preferredSize
            val maxPreferredHeight = JBUI.scale(300)
            preSize.height = minOf(preSize.height, maxPreferredHeight)
            minimumSize = Dimension(0, preSize.height)
            preferredSize = Dimension(preSize)
        }
    }

    private fun initListeners() {
        object : AnAction() {
            override fun actionPerformed(e: AnActionEvent) {
                IdeFocusManager.findInstance().requestFocus(fontComboBoxButton, true)
            }
        }.registerCustomShortcutSet(CustomShortcutSet.fromString("shift TAB"), inputTextField)

        inputTextField.addDocumentListener(object : DocumentListener {
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

        val listener = object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                if (e.keyCode == KeyEvent.VK_ENTER) {
                    (e.source as ComboBox<*>).apply { isPopupVisible = !isPopupVisible }
                    e.consume()
                }
            }
        }
        verticalLayoutComboBox.addKeyListener(listener)
        horizontalLayoutComboBox.addKeyListener(listener)
    }

    private fun update() {
        callback.onUpdate()
        updater.cancelAllRequests()
        if (!updater.isDisposed) {
            updater.addRequest({
                var error: Throwable? = null
                val asciiArtText: String? = try {
                    callback.generateASCIIArtText()
                } catch (e: Throwable) {
                    error = e
                    null
                }

                ApplicationManager.getApplication().invokeLater({
                    asciiArtText?.let { setResult(it) }
                    error?.let { callback.onError(it) }
                }, ModalityState.stateForComponent(contentPanel))
            }, 200)
        }
    }

    private fun Callback.generateASCIIArtText(): String {
        val inputText = inputTextField.text
        if (inputText.isEmpty()) {
            return inputText
        }

        val horizontalLayout = horizontalLayoutComboBox.selectedItem as FIGlet.Layout
        val verticalLayout = verticalLayoutComboBox.selectedItem as FIGlet.Layout

        return onGenerateASCIIArtText(inputText, currentFont, horizontalLayout, verticalLayout)
    }

    private fun setResult(asciiArtText: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            (previewViewer.document as DocumentEx).apply {
                setReadOnly(false)
                replaceString(0, textLength, asciiArtText)
                clearLineModificationFlags()
                setReadOnly(true)
            }
        }
        contentPanel.revalidate()
        callback.onResult(asciiArtText)
    }

    override fun dispose() {
        Previews.releasePreviewViewer(previewViewer)
    }


    private class TextField(text: String, project: Project?) :
        EditorTextField(text, project, PlainTextFileType.INSTANCE) {

        init {
            isOneLineMode = false
        }

        override fun createEditor(): EditorEx {
            return super.createEditor().apply {
                setPlaceholder("Write something here.")
            }
        }

        override fun updateBorder(editor: EditorEx) {
            setupBorder(editor)
        }
    }

    private class LayoutRenderer : SimpleListCellRenderer<FIGlet.Layout>() {

        override fun customize(
            list: JList<out FIGlet.Layout>,
            value: FIGlet.Layout?,
            index: Int,
            selected: Boolean,
            hasFocus: Boolean
        ) {
            text = value?.displayName
        }
    }

    interface Callback {
        fun onUpdate()

        fun onGenerateASCIIArtText(
            inputText: String,
            fontName: String,
            horizontalLayout: FIGlet.Layout,
            verticalLayout: FIGlet.Layout
        ): String

        fun onResult(asciiArtText: String)

        fun onError(throwable: Throwable)
    }

}
