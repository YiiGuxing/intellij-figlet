package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent

class GenerateASCIIArtAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        GenerateASCIIArtDialog(e.project!!).show()
    }
}