package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.ui.CollectionListModel
import com.intellij.ui.JBColor
import com.intellij.ui.SingleSelectionModel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.JBEmptyBorder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.components.BorderLayoutPanel
import org.slf4j.LoggerFactory
import java.awt.Component
import javax.swing.*

class TestAllFontsDialog(project: Project, parent: Component) : DialogWrapper(parent, false) {

    private val testModel = CollectionListModel<TestItem>(ArrayList(NUMBER_OF_FONTS))
    private val testList = JBList<TestItem>(testModel)

    private var disposed = false

    init {
        init()
        title = "Fonts"
        setOKButtonText("Use Font")
        isOKActionEnabled = false

        initList(project)
        runTestTask(project)
    }

    private fun initList(project: Project) = with(testList) {
        setPaintBusy(true)
        background = JBColor(0xE8E8E8, 0x4C5052)
        selectionModel = SingleSelectionModel()
        addListSelectionListener {
            isOKActionEnabled = testList.selectedValue != null
        }

        val viewer = Previews.createPreviewViewer(project).apply {
            settings.isLineNumbersShown = false
            setBorder(JBEmptyBorder(JBUI.insets(10)))
            component.border = JBEmptyBorder(2)
        }
        Disposer.register(disposable, Disposable { Previews.releasePreviewViewer(viewer) })

        val title = JLabel().apply {
            border = JBEmptyBorder(JBUI.insets(8))
        }
        val item = BorderLayoutPanel().apply {
            isOpaque = true
            addToTop(title)
            addToCenter(viewer.component)
        }

        cellRenderer = object : ListCellRenderer<TestItem> {
            override fun getListCellRendererComponent(
                list: JList<out TestItem>,
                value: TestItem,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                title.text = value.fontName
                WriteCommandAction.runWriteCommandAction(project) {
                    (viewer.document as DocumentEx).apply {
                        setReadOnly(false)
                        replaceString(0, textLength, value.effectText)
                        clearLineModificationFlags()
                        setReadOnly(true)
                    }
                }

                if (isSelected) {
                    title.foreground = list.selectionForeground
                    item.foreground = list.selectionForeground
                    item.background = list.selectionBackground
                    viewer.component.background = list.selectionBackground
                } else {
                    title.foreground = list.foreground
                    item.foreground = list.foreground
                    item.background = list.background
                    viewer.component.background = list.background
                }

                item.revalidate()

                return item
            }
        }
    }

    private fun runTestTask(project: Project) {
        val task = TestTask(project)
        val indicator = BackgroundableProcessIndicator(task)
        Disposer.register(disposable, indicator)
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, indicator)
    }

    override fun createActions(): Array<Action> = arrayOf(okAction, cancelAction)

    override fun createCenterPanel(): JComponent {
        return JBScrollPane(testList)
            .apply {
                preferredSize = JBDimension(600, 750)
                //border = JBUI.Borders.customLine(JBColor(0xD0D0D0, 0x555555), 1)
            }
    }

    fun showAndGetResult(): String? {
        val isOk = showAndGet()
        return testList.selectedValue?.takeIf { isOk }?.fontName
    }

    override fun dispose() {
        super.dispose()
        disposed = true
    }


    companion object {
        private val NUMBER_OF_FONTS = FIGlet.fonts.size

        private val LOGGER = LoggerFactory.getLogger(TestAllFontsDialog::class.java)
    }

    private data class TestItem(val fontName: String, val effectText: String)

    private inner class TestTask(project: Project?) : Task.Backgroundable(project, "FIGlet") {

        override fun run(indicator: ProgressIndicator) {
            val fonts = FIGlet.fonts
            for (i in fonts.indices) {
                val fontName = fonts[i]

                indicator.checkCanceled()
                indicator.text = "Testing $fontName..."

                try {
                    onTestFont(fontName)
                } catch (error: Throwable) {
                    onThrowable(IllegalStateException("Test failed: $fontName.", error))
                }

                indicator.fraction = (i + 1.0) / NUMBER_OF_FONTS
            }

            finish()
        }

        private fun onTestFont(fontName: String) {
            val font = FIGlet.getFigFont(fontName)
            val artText = FIGlet.generate(fontName, font)
            val effectText = FIGlet.trimArtText(artText)
            val testItem = TestItem(fontName, effectText)

            ApplicationManager.getApplication().invokeAndWait({
                if (!disposed) {
                    testModel.add(testItem)
                }
            }, ModalityState.any())
        }

        override fun onThrowable(error: Throwable) {
            LOGGER.error("Some fault occurred.", error)
        }

        private fun finish() {
            ApplicationManager.getApplication().invokeLater({
                if (!disposed) {
                    testList.setPaintBusy(false)
                }
            }, ModalityState.any())
        }
    }
}