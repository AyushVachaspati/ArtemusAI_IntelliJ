package com.artemus.completionProvider

import com.artemus.inlineCompletionApi.CompletionType
import com.artemus.inlineCompletionApi.InlineCompletionItem
import com.artemus.inlineCompletionApi.InlineCompletionProvider
import com.artemus.completionProvider.lruCache.LRUCache
import com.artemus.completionProvider.predictionUtils.InlineModelConfig
import com.artemus.completionProvider.predictionUtils.PredictionUtils
import com.google.gson.Gson
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.TextRange
import java.security.MessageDigest
import java.util.regex.Pattern


data class CachePrompt(val prefix: String, val completionType: CompletionType )

class ArtemusCompletionProvider: InlineCompletionProvider {
    companion object{
        val globalCache = LRUCache(1000)
    }

    override suspend fun getInlineCompletion(editor: Editor, triggerOffset: Int):List<InlineCompletionItem> {
        val currentOffset = editor.caretModel.offset

        if(currentOffset!=triggerOffset) return emptyList()

        // isValidMidlinePostion is not needed for FIM task for the LLM
//        if(!isValidMidlinePosition(editor.document, currentOffset)) return emptyList()

        val document = editor.document
        val prefix = document.getText(TextRange(0,currentOffset))
        val postfix = document.getText(TextRange(currentOffset, document.getLineEndOffset(document.lineCount-1)))

        val startToken = InlineModelConfig.getPrefixToken()
        val endToken = InlineModelConfig.getSuffixToken()
        val middleToken = InlineModelConfig.getMiddleToken()

        val prompt:String
        val fillInMiddle = postfix.trim().isNotEmpty()
//        val lineEndOffset = document.getLineEndOffset(document.getLineNumber(triggerOffset))  //Lookahead replace until end of line

        if(fillInMiddle){
            prompt = "${startToken}${prefix}${endToken}${postfix}${middleToken}"
        }
        else{
            prompt = prefix
        }

        val cacheItem = CachePrompt(prefix = prompt, completionType = CompletionType.INLINE_COMPLETION)

//        println(prompt)

        val key = MessageDigest.getInstance("SHA1")
            .digest(Gson().toJson(cacheItem)
                .toByteArray())
            .decodeToString()

        var inlineCompletion = globalCache.get(key)
        if(inlineCompletion == null) {
            val prediction = PredictionUtils.debouncedInlineCompletion(prompt)
            inlineCompletion = prediction?.result
            if(inlineCompletion != null)
            {
                inlineCompletion = inlineCompletion.substring(prompt.length)
                globalCache.set(key, inlineCompletion)
            }
        }

        if(inlineCompletion!=null){
            return listOf(InlineCompletionItem(inlineCompletion, triggerOffset, triggerOffset))
        }

        return emptyList()
    }


    override suspend fun getLookAheadCompletion(
        editor: Editor,
        userPrefix: String,
        lookAheadItem: String,
        triggerOffset: Int):List<InlineCompletionItem> {

        val currentOffset = editor.caretModel.offset

        if(currentOffset!=triggerOffset) return emptyList()

        // isValidMidlinePostion is not needed for FIM task for the LLM
//        if(!isValidMidlinePosition(editor.document, currentOffset)) return emptyList()

        val document = editor.document
        var prefix = document.getText(TextRange(0,currentOffset))
        val postfix = document.getText(TextRange(currentOffset, document.getLineEndOffset(document.lineCount-1)))


        val startToken = InlineModelConfig.getPrefixToken()
        val endToken = InlineModelConfig.getSuffixToken()
        val middleToken = InlineModelConfig.getMiddleToken()

        val prompt:String
        val fillInMiddle = postfix.trim().isNotEmpty()
//        val lineEndOffset = document.getLineEndOffset(document.getLineNumber(triggerOffset))  //Lookahead replace until end of line

        // TODO: This is bad place to fix this.. Make a proper fix in InlineCompletionManager to handle
        //  when the lookahead item does start with what the user typed
        if(!lookAheadItem.startsWith(userPrefix)){
            return emptyList()
        }

        prefix = prefix.removeSuffix(userPrefix)
        prefix = prefix + lookAheadItem //get prefix as if popup suggestion was accepted

        if(fillInMiddle){
            prompt = "${startToken}${prefix}${endToken}${postfix}${middleToken}"
        }
        else{
            prompt = prefix;
        }

        val cacheItem = CachePrompt(prefix = prompt, completionType = CompletionType.LOOK_AHEAD_COMPLETION)

//        println(prompt)

        val key = MessageDigest.getInstance("SHA1")
            .digest(Gson().toJson(cacheItem)
                .toByteArray())
            .decodeToString()

        var inlineCompletion = globalCache.get(key)

        if(inlineCompletion == null) {
            val prediction = PredictionUtils.debouncedInlineCompletion(prompt)
            inlineCompletion = prediction?.result
            if(inlineCompletion != null)
            {
                inlineCompletion = lookAheadItem.removePrefix(userPrefix) + inlineCompletion.substring(prompt.length)
                globalCache.set(key, inlineCompletion)
            }

            // Also cache inlineSuggestion, this will be shown when user accepts LookAheadSuggestion in order to maintain a seamless experience.
            val ifAcceptedLookAheadSuggestion = prediction?.result?.substring(prompt.length)
            val inlineCacheItem = CachePrompt(prefix = prompt, completionType = CompletionType.INLINE_COMPLETION)
            val inlineKey = MessageDigest.getInstance("SHA1")
                .digest(Gson().toJson(inlineCacheItem)
                .toByteArray())
                .decodeToString()
            if (ifAcceptedLookAheadSuggestion!=null) globalCache.set(inlineKey, ifAcceptedLookAheadSuggestion)
        }

        if(inlineCompletion!=null){
            return listOf(InlineCompletionItem(inlineCompletion, triggerOffset, triggerOffset))
        }

        return emptyList()
    }



    private fun isValidMidlinePosition(document: Document, offset: Int): Boolean {
        val END_OF_LINE_VALID_PATTERN = Pattern.compile("^\\s*[)}\\]\"'`]*\\s*[:{;,]?\\s*$")
        val lineIndex = document.getLineNumber(offset)
        val suffix = document.getText(TextRange(offset, document.getLineEndOffset(lineIndex)))
        return END_OF_LINE_VALID_PATTERN.matcher(suffix).matches()
    }
}


/** Code to make grpc stream client */
//    val managedChannel = ManagedChannelBuilder.forTarget("127.0.0.1:81")
//        .usePlaintext()
//        .build()
//    try {
//        val inferenceStub = GRPCInferenceServiceGrpcKt.GRPCInferenceServiceCoroutineStub(managedChannel)
//        val modelRequest = flowOf(
//            GrpcService.ModelInferRequest.newBuilder()
//                .setModelName("santacoder_huggingface_stream")
//                .addInputs(
//                    InferInputTensor.newBuilder()
//                        .setName("input")
//                        .setDatatype("BYTES")
//                        .addShape(1).addShape(1)
//                        .setContents(
//                            GrpcService.InferTensorContents.newBuilder()
//                                .addBytesContents("Complete this string".toByteStringUtf8())
//                        )
//                ).build()
//        )
//        val modelOutput = inferenceStub.modelStreamInfer(modelRequest)
//        modelOutput.collectIndexed { index, value ->
//            print(
//                value.inferResponse
//                .getRawOutputContents(0)
//                .asReadOnlyByteBuffer()
//                .toByteString()
//                .toStringUtf8()
//                .substring(4)
//            )
//            if (value.inferResponse.parametersMap["triton_final_response"]!!.boolParam) {
//                println()
//                printlnError("Final Response Received")
//            }
//        }
//    }
//    catch(e: CancellationException){
//        printlnError("Request Cancelled")
//    }
//    catch(e:Exception){
//        printlnError(e.stackTraceToString())
//    }
//    finally {
//        managedChannel.shutdown()
//    }
/**********************************************/