package com.artemus.inlineCompletionApi

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class KeyPressHandler: TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {
//        CompletionPreview.clear(editor)
        println("Key pressed $c")
        val currentPreview = CompletionPreview.getInstance(editor)

        // TODO: Check for matching completions. (only if current preview exists, return without doing anything otherwise)

        return Result.CONTINUE
    }
}