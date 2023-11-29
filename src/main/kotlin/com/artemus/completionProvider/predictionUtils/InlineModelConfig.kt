package com.artemus.completionProvider.predictionUtils

enum class InlineModelName{
    SantaCoder,
    StarCoder,
}

object InlineModelConfig {
    private val currentModel = InlineModelName.SantaCoder

    fun getPrefixToken(): String{
        return when(currentModel){
            InlineModelName.SantaCoder -> "<fim-prefix>"
            InlineModelName.StarCoder -> "<fim_prefix>"
        }
    }

    fun getSuffixToken(): String{
        return when(currentModel){
            InlineModelName.SantaCoder -> "<fim-suffix>"
            InlineModelName.StarCoder -> "<fim_suffix>"
        }
    }

    fun getMiddleToken(): String{
        return when(currentModel){
            InlineModelName.SantaCoder -> "<fim-middle>"
            InlineModelName.StarCoder -> "<fim_middle>"
        }
    }

    fun getName(): String{
        return when(currentModel){
            InlineModelName.SantaCoder -> "santacoder_huggingface"
            InlineModelName.StarCoder -> "starcoder_huggingface"
        }
    }
}