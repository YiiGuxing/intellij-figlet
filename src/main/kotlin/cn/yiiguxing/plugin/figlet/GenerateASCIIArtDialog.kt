package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import javax.swing.Action
import javax.swing.JComponent

class GenerateASCIIArtDialog(project: Project, defaultInputText: String = "") : DialogWrapper(project) {

    private val form = GenerateASCIIArtForm(project, defaultInputText)

    init {
        init()
        title = "Generate ASCII Art"
        setOKButtonText("Generate ASCII Art Text")

        isOKActionEnabled = false
        form.callback = object : GenerateASCIIArtForm.Callback {
            override fun onUpdate() {
                isOKActionEnabled = false
                pack()
            }

            override fun onResult(asciiArtText: String) {
                updateErrorMessage(null)
            }

            override fun onError(msg: String) {
                updateErrorMessage(ValidationInfo(msg))
            }
        }
    }

    override fun createCenterPanel(): JComponent? = form.component

    override fun createActions(): Array<Action> = arrayOf(okAction, cancelAction)

    override fun getPreferredFocusedComponent(): JComponent? {
        return form.preferredFocusedComponent ?: super.getPreferredFocusedComponent()
    }

}