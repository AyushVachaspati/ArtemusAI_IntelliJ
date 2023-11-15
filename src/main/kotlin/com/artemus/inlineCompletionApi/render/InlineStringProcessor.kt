package com.artemus.inlineCompletionApi.render


enum class FirstLineRendering {
    None,
    BeforeSubstring,
    AfterSubstring,
    BeforeAndAfterSubstring,
}

data class RenderingInstructions(val firstLine: FirstLineRendering, val shouldRenderBlock: Boolean, val shouldRenderLastLine: Boolean)

class InlineStringProcessor {
    companion object {
        fun determineRendering(textLines: List<String>,replaceSubstring: String,extraSuffix: String
        ): RenderingInstructions {
            if (textLines.isEmpty()) return RenderingInstructions(
                FirstLineRendering.None,
                shouldRenderBlock = false,
                shouldRenderLastLine = false
            )

            val shouldRenderBlock = textLines.size > 1
            val shouldRenderLastLine = extraSuffix.trimEnd().isNotEmpty()

            if (textLines[0].trim().isNotEmpty()) {
                if (replaceSubstring.trim().isNotEmpty()) {
                    val endIndex = textLines[0].indexOf(replaceSubstring)

                    if (endIndex == 0) return RenderingInstructions(
                        FirstLineRendering.AfterSubstring,
                        shouldRenderBlock,
                        shouldRenderLastLine
                    )
                    else if (endIndex > 0) return RenderingInstructions(
                        FirstLineRendering.BeforeAndAfterSubstring,
                        shouldRenderBlock,
                        shouldRenderLastLine
                    )
                }
                return RenderingInstructions(
                    FirstLineRendering.BeforeSubstring,
                    shouldRenderBlock,
                    shouldRenderLastLine
                )
            }

            return RenderingInstructions(FirstLineRendering.None, shouldRenderBlock, shouldRenderLastLine)
        }
    }
}