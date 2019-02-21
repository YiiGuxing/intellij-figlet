package cn.yiiguxing.plugin.figlet

import com.intellij.codeInsight.editorActions.PasteHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.util.Producer
import com.intellij.util.ui.TextTransferable
import java.awt.datatransfer.Transferable


class GenerateASCIIArtAction : EditorAction(GenerateASCIIArtHandler()) {

    private class GenerateASCIIArtHandler : PasteHandler(null) {

        override fun isEnabledForCaret(editor: Editor, caret: Caret, dataContext: DataContext?): Boolean {
            return !editor.isViewer && editor.document.isWritable
        }

        override fun doExecute(editor: Editor, caret: Caret?, dataContext: DataContext?) {
            val project = editor.project ?: return
            val selectedText = editor.selectionModel.selectedText ?: ""
            var asciiArtText = GenerateASCIIArtDialog(project, selectedText).showAndGetResult() ?: return

            if (Settings.instance.trimOutput) {
                asciiArtText = asciiArtText.trimArtText()
            }

            execute(editor, dataContext, Producer<Transferable> { TextTransferable(asciiArtText) })
        }

        private fun String.trimArtText(): String {
            if (isBlank()) {
                return ""
            }

            val figLines = lines()
            if (figLines.size == 1) {
                return this
            }

            var start = 0
            var end = figLines.size
            for (i in figLines.indices) {
                if (figLines[i].isBlank()) start++ else break
            }
            for (i in figLines.indices.reversed()) {
                if (figLines[i].isBlank()) end-- else break
            }

            return figLines.subList(start, end).joinToString("\n")
        }
    }
}