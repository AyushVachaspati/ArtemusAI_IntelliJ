package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.CompletionPreview
import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.inlineCompletionGlobalState.GlobalState
import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.InvalidDataException
import com.intellij.psi.PsiFile


class KeyPressHandler: TypedHandlerDelegate() {
    override fun beforeCharTyped(typedChar: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {

        // TODO: Check for matching completions. (only if current preview exists) otherwise trigger new completion
        var currentPreview = CompletionPreview.getInstance(editor)
        if(currentPreview!=null) {
            var completions = currentPreview.getCompletions()
            var currentCompletionItem = currentPreview.currentCompletion
//            var currentIndex = currentPreview.getCurrentIndex()


            // filter and adjust the completions which match the typed character
            completions = completions.filter{
                it.insertText.startsWith(typedChar)
            }
            completions = adjustCompletions(completions, typedChar, currentCompletionItem)

            if(completions.isEmpty()){
                CompletionPreview.clear(editor)
                // TODO: no completions start with the current types letter
                // TODO: Trigger new completion here (inside invoke later)
                return Result.CONTINUE
            }


            // Create new preview with the Adjusted completions
            CompletionPreview.clear(editor)
            GlobalState.clearedByKeyPress = true
            val r = Runnable {
                GlobalState.clearedByKeyPress = false
                try {
                    CompletionPreview.createInstance(editor, completions)
                }
                catch(e:InvalidDataException){
                    // TODO: Trigger a new completion here
                    // TODO: Need to work on offset correction in adjustCompletion function to avoid this.
                }
            }
            ApplicationManager.getApplication().invokeLater(r)
        }
        else{
            // Don't think we need this trigger here
            // TODO: Trigger completion as the user has started typing without any prior completions shown
        }

        return Result.CONTINUE
    }


    private fun adjustCompletions(completions: List<InlineCompletionItem>, typedChar: Char,
                                  currentCompletionItem: InlineCompletionItem
    ): List<InlineCompletionItem> {
        //TODO: Correct offset changes so typing to the end of completions and completions with new line triggers new completion
        //TODO: i.e it does not trigger the InvalidDataException in CompltionPreview constructor.
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