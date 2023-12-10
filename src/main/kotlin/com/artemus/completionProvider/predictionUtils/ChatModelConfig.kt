package com.artemus.completionProvider.predictionUtils

enum class ChatModelName{
    SantaCoderChat,
    StarCoderChat,
}

object ChatModelConfig {
    private val currentModel = ChatModelName.SantaCoderChat

    fun getName(): String{
        return when(currentModel){
            ChatModelName.SantaCoderChat -> "santacoder_huggingface_stream"
            ChatModelName.StarCoderChat -> "starcoder_chat"
        }
    }
}