package com.artemus.inlineCompletionApi.general

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Document
import com.intellij.openapi.updateSettings.impl.UpdateSettings
import com.intellij.openapi.util.TextRange
import com.intellij.util.concurrency.AppExecutorUtil
import com.intellij.util.containers.ContainerUtil
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors
import kotlin.math.abs

object Utils {
    private const val UNKNOWN = "Unknown"
    fun endsWithADot(doc: Document, positionBeforeSuggestionPrefix: Int): Boolean {
        val begin = positionBeforeSuggestionPrefix - ".".length
        return if (begin < 0 || positionBeforeSuggestionPrefix > doc.textLength) {
            false
        } else {
            val tail = doc.getText(TextRange(begin, positionBeforeSuggestionPrefix))
            tail == "."
        }
    }

    @Throws(IOException::class)
    fun readContent(inputStream: InputStream): String {
        val result = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (inputStream.read(buffer).also { length = it } != -1) {
            result.write(buffer, 0, length)
        }
        return result.toString(StandardCharsets.UTF_8.name()).trim { it <= ' ' }
    }

    fun toInt(aLong: Long?): Int {
        return if (aLong == null) {
            0
        } else Math.toIntExact(aLong)
    }

    fun asLines(block: String): List<String> {
        return Arrays.stream(block.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            .collect(Collectors.toList())
    }

    fun cmdSanitize(text: String): String {
        return text.replace(" ", "")
    }

    fun wrapWithHtml(content: String): String {
        return wrapWithHtmlTag(content, "html")
    }

    fun wrapWithHtmlTag(content: String, tag: String): String {
        return "<$tag>$content</$tag>"
    }

    fun getDaysDiff(date1: Date?, date2: Date?): Long {
        return if (date1 != null && date2 != null) {
            TimeUnit.DAYS.convert(
                abs((date2.time - date1.time).toDouble()).toLong(), TimeUnit.MILLISECONDS
            )
        } else -1
    }

    fun getHoursDiff(date1: Date?, date2: Date?): Long {
        return if (date1 != null && date2 != null) {
            TimeUnit.HOURS.convert(
                date2.time - date1.time,
                TimeUnit.MILLISECONDS
            )
        } else -1
    }

    fun executeUIThreadWithDelay(
        runnable: Runnable?, delay: Long, timeUnit: TimeUnit?
    ): Future<*> {
        return executeThread(
            { ApplicationManager.getApplication().invokeLater(runnable!!) }, delay, timeUnit
        )
    }

    fun executeThread(runnable: Runnable): Future<*> {
        if (isUnitTestMode) {
            runnable.run()
            return CompletableFuture.completedFuture<Any?>(null)
        }
        return AppExecutorUtil.getAppExecutorService().submit(runnable)
    }

    fun executeThread(runnable: Runnable, delay: Long, timeUnit: TimeUnit?): Future<*> {
        if (isUnitTestMode) {
            runnable.run()
            return CompletableFuture.completedFuture<Any?>(null)
        }
        return AppExecutorUtil.getAppScheduledExecutorService().schedule(runnable, delay, timeUnit)
    }

    val isUnitTestMode: Boolean
        get() = (ApplicationManager.getApplication() == null
                || ApplicationManager.getApplication().isUnitTestMode)

    fun trimEndSlashAndWhitespace(text: String): String {
        return text.replace("/\\s*$", "")
    }

    fun setCustomRepository(url: String) {
        if (!url.trim { it <= ' ' }.isEmpty()) {
            val pluginHosts = UpdateSettings.getInstance().storedPluginHosts
            val newStore = String.format("%s/update/jetbrains/updatePlugins.xml", trimEndSlashAndWhitespace(url))
            pluginHosts.add(newStore)
            Logger.getInstance(Utils::class.java)
                .debug(String.format("Added custom repository to %s", newStore))
            ContainerUtil.removeDuplicates(pluginHosts)
        }
    }
}
