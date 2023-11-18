package com.artemus.inlineCompletionApi.listeners

import com.artemus.inlineCompletionApi.CompletionPreview
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.util.ObjectUtils

class CaretMoveListener(private var completionPreview: CompletionPreview?) : CaretListener {
    init {
        if(completionPreview!!.editor is EditorEx){
            val e = completionPreview!!.editor as EditorEx
            e.caretModel.addCaretListener(this, completionPreview!!)
        }
    }

    override fun caretPositionChanged(event: CaretEvent) {
        println("Caret position changed")

        // The undo operation to remove \n causes recursive call to caretPositionChanged,
        // which causes issues. So this condition handles that extra call.
        if (completionPreview == null) return

        if(isSingleOffsetChange(event)){
            println("Is single offset change")
            // TODO: Should we use this to handle typing by the user? or can we use the bulk document change handler or
            // User has typed something which can be handled by KeyPressHandler
            return
        }

        completionPreview = null  // set to null to return from next recursive call instantly (recursion induced by undo in clear preview)

        event.editor.caretModel.moveToLogicalPosition(event.oldPosition) // move to old position so undo works correctly
        CompletionPreview.clear(event.editor)
        event.editor.caretModel.moveToLogicalPosition(event.newPosition) // move to new position to restore correct caret position
    }

    private fun isSingleOffsetChange(event: CaretEvent): Boolean {
        return event.oldPosition.line == event.newPosition.line && event.oldPosition.column + 1 == event.newPosition.column
    }

}
