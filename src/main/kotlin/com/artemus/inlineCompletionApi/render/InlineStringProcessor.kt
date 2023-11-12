package com.artemus.inlineCompletionApi.render


enum class FirstLineRendering {
    None,
    BeforeSubstring,
    AfterSubstring,
    BeforeAndAfterSubstring,
}

data class RenderingInstructions(val firstLine: FirstLineRendering, val shouldRenderBlock: Boolean)

object inlineStringProcessor {
    fun determineRendering(textLines: List<String>, replaceSubstring: String): RenderingInstructions {
        if (textLines.isEmpty()) return RenderingInstructions(FirstLineRendering.None, false)

        val shouldRenderBlock = textLines.size > 1

        if (textLines[0].trim().isNotEmpty()) {
            if (replaceSubstring.trim().isNotEmpty()) {
                val endIndex = textLines[0].indexOf(replaceSubstring)

                if (endIndex == 0) return RenderingInstructions(FirstLineRendering.AfterSubstring, shouldRenderBlock)
                else if (endIndex > 0) return RenderingInstructions(
                    FirstLineRendering.BeforeAndAfterSubstring,
                    shouldRenderBlock
                )
            }
            return RenderingInstructions(FirstLineRendering.BeforeSubstring, shouldRenderBlock)
        }

        return RenderingInstructions(FirstLineRendering.None, shouldRenderBlock)
    }
}