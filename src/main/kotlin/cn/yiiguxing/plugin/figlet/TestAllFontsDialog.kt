package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import com.intellij.ui.CollectionListModel
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBDimension
import com.intellij.util.ui.SwingHelper
import org.slf4j.LoggerFactory
import java.awt.Component
import javax.swing.Action
import javax.swing.DefaultListCellRenderer
import javax.swing.JComponent
import javax.swing.JList

class TestAllFontsDialog(
    project: Project,
    parent: Component,
    private val text: String = "Test"
) : DialogWrapper(parent, false) {

    private val testModel = CollectionListModel<TestItem>(ArrayList(NUMBER_OF_FONTS))
    private val testList = JBList<TestItem>(testModel)

    private var disposed = false

    init {
        init()
        title = "Test All Fonts"
        setOKButtonText("Use Font")
        isOKActionEnabled = false

        initList()
        runTestTask(project)
    }

    private fun initList() = with(testList) {
        setPaintBusy(true)
        addListSelectionListener {
            isOKActionEnabled = testList.selectedValue != null
        }
        cellRenderer = object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

                text = (value as TestItem).effectText

                return this
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
            val artText = FIGlet.generate(text, font)
            val effectText = SwingHelper.buildHtml("", FIGlet.trimArtText(artText))
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