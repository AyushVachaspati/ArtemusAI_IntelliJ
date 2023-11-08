package com.artemus.inlineCompletionApi.render


enum class FirstLineRendering {
    None,
    NoSubstring,
    AfterSubstring,
    BeforeAndAfterSubstring,
}

data class RenderingInstructions(val firstLine: FirstLineRendering, val shouldRenderBlock: Boolean)

fun determineRendering(textLines: List<String>, oldSuffix: String): RenderingInstructions {
    if (textLines.isEmpty()) return RenderingInstructions(FirstLineRendering.None, false)

    val shouldRenderBlock = textLines.size > 1

    if (textLines[0].trim().isNotEmpty()) {
        if (oldSuffix.trim().isNotEmpty()) {
            val endIndex = textLines[0].indexOf(oldSuffix)

            if (endIndex == 0) return RenderingInstructions(FirstLineRendering.AfterSubstring, shouldRenderBlock)
            else if (endIndex > 0) return RenderingInstructions(
                FirstLineRendering.BeforeAndAfterSubstring,
                shouldRenderBlock
            )
        }

        return RenderingInstructions(FirstLineRendering.NoSubstring, shouldRenderBlock)
    }

    return RenderingInstructions(FirstLineRendering.None, shouldRenderBlock)
}
