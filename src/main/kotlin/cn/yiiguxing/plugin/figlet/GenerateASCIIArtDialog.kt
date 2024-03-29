package cn.yiiguxing.plugin.figlet

import cn.yiiguxing.plugin.figlet.FIGlet.clearFontCache
import cn.yiiguxing.plugin.figlet.FIGlet.getFigFont
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.util.Disposer
import org.slf4j.LoggerFactory
import javax.swing.Action
import javax.swing.JComponent
import kotlin.collections.set

class GenerateASCIIArtDialog(project: Project, defaultInputText: String = "") : DialogWrapper(project) {

    private val form = GenerateASCIIArtForm(project, defaultInputText)

    private var lastUsedFont: String = DataManager.instance.state.lastUsedFont

    var result: String? = null
        private set

    init {
        init()
        title = "Generate ASCII Art"
        setOKButtonText("Generate ASCII Art Text")

        isOKActionEnabled = false
        form.callback = Callback()

        Disposer.register(disposable, form)
    }

    override fun createCenterPanel(): JComponent = form.component

    override fun createActions(): Array<Action> = arrayOf(okAction, cancelAction)

    override fun getPreferredFocusedComponent(): JComponent {
        return form.preferredFocusedComponent
    }

    fun showAndGetResult(): String? {
        val isOk = showAndGet()
        if (isOk) {
            saveCommonFontsAndLastUsedFont()
        }
        clearFontCache(DataManager.instance.state.lastUsedFont)

        return result?.takeIf { isOk }
    }

    private fun saveCommonFontsAndLastUsedFont() {
        val state = DataManager.instance.state
        val map = LinkedHashMap<String, Unit>(8, 0.75f, true)
        for (font in state.commonFonts.reversed()) {
            map[font] = Unit
        }
        map[lastUsedFont] = Unit

        state.commonFonts = map.keys.reversed().take(5)
        state.lastUsedFont = lastUsedFont
    }

    private inner class Callback : GenerateASCIIArtForm.Callback {
        override fun onUpdate() {
            validate()
            window.revalidate()
        }

        override fun onGenerateASCIIArtText(
            inputText: String,
            fontName: String,
            horizontalLayout: FIGlet.Layout,
            verticalLayout: FIGlet.Layout
        ): String {
            val figFont = getFigFont(fontName)
            return FIGlet.generate(inputText, figFont, horizontalLayout, verticalLayout)
        }

        override fun onResult(asciiArtText: String) {
            val hasContent = asciiArtText.isNotBlank()
            if (hasContent) {
                result = asciiArtText
                lastUsedFont = form.currentFont
            }

            isOKActionEnabled = hasContent
            setErrorText(null)
        }

        override fun onError(throwable: Throwable) {
            setErrorText("Cannot generate ASCII art text: ${throwable.message}")
            LOGGER.error("Cannot generate ASCII art text", throwable)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(GenerateASCIIArtDialog::class.java)
    }
}