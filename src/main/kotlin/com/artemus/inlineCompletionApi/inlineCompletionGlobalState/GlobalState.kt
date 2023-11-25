package com.artemus.inlineCompletionApi.inlineCompletionGlobalState

object GlobalState {
    var isCreatingPreview = false
    var isKeyPressHandlerTriggered = false
    var clearedByLookupItemChange = false
    var clearedByKeyPress = false
    var isArtemusUndoInProgress = false
}