package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.application.ApplicationManager
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
import org.slf4j.LoggerFactory
import javax.swing.JComponent

class TestAllFontsDialog(project: Project, private val text: String = "Test") : DialogWrapper(project) {

    private val testModel = CollectionListModel<TestItem>(ArrayList(NUMBER_OF_FONTS))
    private val testList = JBList<TestItem>(testModel)

    private var disposed = false

    init {
        init()
        title = "Test All Fonts"
        setOKButtonText("Use Font")
        isOKActionEnabled = false

        testList.addListSelectionListener {
            isOKActionEnabled = testList.selectedValue != null
        }
        testList.setPaintBusy(true)

        val task = TestTask(project)
        val indicator = BackgroundableProcessIndicator(task)
        Disposer.register(disposable, indicator)
        ApplicationManager.getApplication().invokeLater {
            ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, indicator)
        }
    }

    override fun dispose() {
        super.dispose()
        disposed = true
    }


    override fun createCenterPanel(): JComponent =
        JBScrollPane(testList).apply { preferredSize = JBDimension(600, 750) }

    fun showAndGetResult(): String? {
        val isOk = showAndGet()
        return testList.selectedValue?.takeIf { isOk }?.fontName
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
                    onThrowable(error)
                }

                indicator.fraction = (i + 1.0) / NUMBER_OF_FONTS
            }
        }

        private fun onTestFont(fontName: String) {
            // TODO: test font
        }

        override fun onThrowable(error: Throwable) {
            LOGGER.error("Some fault occurred.", error)
        }

        override fun onFinished() {
            if (!disposed) {
                testList.setPaintBusy(false)
            }
        }
    }
}