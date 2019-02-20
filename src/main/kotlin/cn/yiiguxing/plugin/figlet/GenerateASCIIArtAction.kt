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
            val asciiArtText = GenerateASCIIArtDialog(project, selectedText).showAndGetResult() ?: return

            execute(editor, dataContext, Producer<Transferable> { TextTransferable(asciiArtText) })
        }
    }
}