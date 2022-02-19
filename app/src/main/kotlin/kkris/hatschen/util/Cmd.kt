package kkris.hatschen.util

import mu.KotlinLogging
import java.io.File
import java.lang.Exception
import java.util.concurrent.TimeUnit

object Cmd {
    private val logger = KotlinLogging.logger {}

    fun run(args: List<String>) {
        try {
            val proc = ProcessBuilder(*args.toTypedArray())
                .directory(File(System.getProperty("user.dir")).parentFile) // maps to root directory of the repo (ish)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()
            proc.waitFor(30, TimeUnit.SECONDS)

            println(proc.errorStream.bufferedReader().readText())
            println(proc.inputStream.bufferedReader().readText())
        } catch (e: Exception) {
            logger.error(e) {
                "Failed to run command"
            }
        }
    }
}

/*
fun String.runCommand(workingDir: File): String? {
    try {
        val parts = this.split("\\s".toRegex())
        val proc = ProcessBuilder(*parts.toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

        proc.waitFor(60, TimeUnit.MINUTES)
        return proc.inputStream.bufferedReader().readText()
    } catch(e: IOException) {
        e.printStackTrace()
        return null
    }
}
 */