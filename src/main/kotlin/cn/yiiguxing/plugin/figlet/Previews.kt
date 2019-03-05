package cn.yiiguxing.plugin.figlet

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.project.Project
import com.intellij.util.ui.JBUI

object Previews {

    fun createPreviewViewer(project: Project?, text: String = ""): Editor {
        val editorFactory = EditorFactory.getInstance()
        val editorDocument = editorFactory.createDocument(text)
            .apply { setReadOnly(true) }
        val editor = editorFactory.createViewer(editorDocument, project)
        editor.colorsScheme.editorFontSize = JBUI.scale(Settings.instance.previewFontSize)
        editor.settings.apply {
            isCaretRowShown = false
            isLineNumbersShown = true
            isWhitespacesShown = true
            isLineMarkerAreaShown = false
            isIndentGuidesShown = false
            isRightMarginShown = true
            isFoldingOutlineShown = false
            isAutoCodeFoldingEnabled = false
            additionalColumnsCount = 0
            additionalLinesCount = 0
            setWrapWhenTypingReachesRightMargin(false)
        }

        return editor
    }

    fun releasePreviewViewer(viewer: Editor) {
        EditorFactory.getInstance().releaseEditor(viewer)
    }

}