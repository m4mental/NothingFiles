package com.nothing.files.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

data class ShellResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val isRoot: Boolean
)

object RootShellEngine {

    suspend fun isRootAvailable(): Boolean = withContext(Dispatchers.IO) {
        try {
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine() ?: ""
            process.waitFor()
            output.contains("uid=0")
        } catch (_: Exception) {
            false
        }
    }

    suspend fun executeCommand(command: String, runAsRoot: Boolean = true): ShellResult = withContext(Dispatchers.IO) {
        try {
            val shell = if (runAsRoot) "su" else "sh"
            val process = ProcessBuilder(shell, "-c", command)
                .redirectErrorStream(false)
                .start()

            val stdoutBuilder = StringBuilder()
            val stderrBuilder = StringBuilder()

            val outThread = Thread {
                try {
                    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stdoutBuilder.append(line).append("\n")
                        }
                    }
                } catch (_: Exception) {}
            }

            val errThread = Thread {
                try {
                    BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            stderrBuilder.append(line).append("\n")
                        }
                    }
                } catch (_: Exception) {}
            }

            outThread.start()
            errThread.start()

            val exitCode = process.waitFor()
            outThread.join(2000)
            errThread.join(2000)

            ShellResult(
                exitCode = exitCode,
                stdout = stdoutBuilder.toString().trimEnd(),
                stderr = stderrBuilder.toString().trimEnd(),
                isRoot = runAsRoot
            )
        } catch (e: Exception) {
            ShellResult(
                exitCode = -1,
                stdout = "",
                stderr = "Execution failed: ${e.message}",
                isRoot = runAsRoot
            )
        }
    }
}
