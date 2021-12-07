package cn.yiiguxing.plugin.figlet

import com.intellij.codeInsight.editorActions.PasteHandler
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Caret
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.EditorAction
import com.intellij.util.ui.TextTransferable


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
                asciiArtText = FIGlet.trimArtText(asciiArtText)
            }

            execute(editor, dataContext) { TextTransferable(asciiArtText as CharSequence) }
        }
    }
}