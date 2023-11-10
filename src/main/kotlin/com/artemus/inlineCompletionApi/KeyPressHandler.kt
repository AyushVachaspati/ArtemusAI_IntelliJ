package com.artemus.inlineCompletionApi

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class KeyPressHandler: TypedHandlerDelegate() {
    override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
        CompletionPreview.clear(editor)
        return Result.CONTINUE
    }

    companion object {
        const val ACTION_ID = "KeyPressHandler"
    }
}