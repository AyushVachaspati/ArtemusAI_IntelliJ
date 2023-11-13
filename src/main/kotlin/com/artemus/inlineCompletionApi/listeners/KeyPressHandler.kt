package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile


class KeyPressHandler: TypedHandlerDelegate() {
    override fun beforeCharTyped(typedChar: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {

        // TODO: Check for matching completions. (only if current preview exists) otherwise trigger new completion
        var currentPreview = CompletionPreview.getInstance(editor)
        if(currentPreview!=null) {
            var completions = currentPreview.getCompletions()
            var currentCompletionItem = currentPreview.currentCompletion
//            var currentIndex = currentPreview.getCurrentIndex()


            CompletionPreview.clear(editor)

            // filter and adjust the completions which match the typed character
            completions = completions.filter{
                it.insertText.startsWith(typedChar)
            }
            completions = adjustCompletions(completions, typedChar, currentCompletionItem)

            if(completions.isEmpty()){
                // TODO: no completions start with the current types letter
                // TODO: Trigger new completion here (inside invoke later)
                return Result.CONTINUE
            }

            // Create new preview with the Adjusted completions
            val r = Runnable {
                CompletionPreview.createInstance(editor, completions)
            }
            ApplicationManager.getApplication().invokeLater(r)
        }
        else{
            // TODO: Trigger completion as the user has started typing without any prior completions shown
        }


        return Result.CONTINUE
    }

    override fun charTyped(typedChar: Char, project: Project, editor: Editor, file: PsiFile): Result {

//        CompletionPreview.clear(editor)

//        // TODO: Check for matching completions. (only if current preview exists) otherwise trigger new completion
//        var currentPreview = CompletionPreview.getInstance(editor)
//        if(currentPreview!=null) {
//            var completions = currentPreview.getCompletions()
//            var currentCompletionItem = currentPreview.currentCompletion
//            var currentIndex = currentPreview.getCurrentIndex()
//
//
//            // filter and adjust the completions which match the typed character
//            completions = completions.filter{
//                it.insertText.startsWith(typedChar)
//            }
//            completions = adjustCompletions(completions, typedChar)
//
//            // Remove the current preview. So we can show the adjusted preview after this
//            CompletionPreview.clear(editor)
//
//            if(completions.isEmpty()){
//                // TODO: no completions start with the current types letter
//                // TODO: Trigger new completion here (inside invoke later)
//                return Result.CONTINUE
//            }
//
//            // Create new preview with the Adjusted completions
//            CompletionPreview.createInstance(editor,completions)
//
//
//        }
//        else{
//            // TODO: Trigger completion as the user has started typing without any prior completions shown
//        }

        return Result.CONTINUE
    }

    private fun adjustCompletions(completions: List<InlineCompletionItem>, typedChar: Char,
                                  currentCompletionItem: InlineCompletionItem
    ): List<InlineCompletionItem> {
        val result = ArrayList<InlineCompletionItem>()
        for (it in completions) {
            val newInsertText = it.insertText.substring(typedChar.toString().length)
            if (newInsertText.isNotEmpty()) {
                if (it.insertText == currentCompletionItem.insertText)
                    // if the currently shown item matches the prefix. It should continue to be shown
                    result.add(0, InlineCompletionItem(newInsertText, it.startOffset + 1, it.endOffset + 1) )
                else {
                    result.add(InlineCompletionItem(newInsertText, it.startOffset + 1, it.endOffset + 1))
                }
            }
        }
        if(result.isEmpty()) return emptyList()
        return result.toList()
    }
}