package com.artemus.inlineCompletionApi

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.credentialStore.createSecureRandom
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class KeyPressHandler: TypedHandlerDelegate() {
    override fun charTyped(c: Char, project: Project, editor: Editor, file: PsiFile): Result {

//        CompletionPreview.clear(editor)

        // TODO: Check for matching completions. (only if current preview exists) otherwise trigger new completion
        var currentPreview = CompletionPreview.getInstance(editor)
        if(currentPreview!=null) {
            var completions = currentPreview.getCompletions()
            var currentCompletionItem = currentPreview.currentCompletion
            var currentIndex = currentPreview.getCurrentIndex()

            println(completions)
            println(currentCompletionItem)
//            currentCompletionItem.insertText
//            currentCompletionItem.startOffset
//            currentCompletionItem.endOffset
        }
        else{
            println("Trigger Inline Completion reason: TYPING")
        }

        return Result.CONTINUE
    }
}