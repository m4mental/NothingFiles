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

    private var cachedRootAvailable: Boolean? = null
    private var mountMasterFlag: String? = null
    private var lastRootCheckTime: Long = 0L

    fun isRootGranted(): Boolean = cachedRootAvailable == true

    suspend fun isRootAvailable(forceCheck: Boolean = false): Boolean = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        if (!forceCheck) {
            if (cachedRootAvailable == true) return@withContext true
            // If recently checked as false within 3 seconds, avoid hammering su prompt
            if (cachedRootAvailable == false && (now - lastRootCheckTime < 3000L)) {
                return@withContext false
            }
        }
        lastRootCheckTime = now

        try {
            // Priority 1: Check with -M (KernelSU-Next / Magisk global mount namespace)
            val pM = Runtime.getRuntime().exec(arrayOf("su", "-M", "-c", "id"))
            val readerM = BufferedReader(InputStreamReader(pM.inputStream))
            val outputM = readerM.readLine() ?: ""
            pM.waitFor()
            if (pM.exitValue() == 0 && outputM.contains("uid=0")) {
                mountMasterFlag = "-M"
                cachedRootAvailable = true
                return@withContext true
            }

            // Priority 2: Check with -mm
            val pMm = Runtime.getRuntime().exec(arrayOf("su", "-mm", "-c", "id"))
            val readerMm = BufferedReader(InputStreamReader(pMm.inputStream))
            val outputMm = readerMm.readLine() ?: ""
            pMm.waitFor()
            if (pMm.exitValue() == 0 && outputMm.contains("uid=0")) {
                mountMasterFlag = "-mm"
                cachedRootAvailable = true
                return@withContext true
            }

            // Priority 3: Standard su
            val process = Runtime.getRuntime().exec(arrayOf("su", "-c", "id"))
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = reader.readLine() ?: ""
            process.waitFor()
            val ok = output.contains("uid=0")
            if (ok) {
                mountMasterFlag = ""
                cachedRootAvailable = true
            } else {
                cachedRootAvailable = false
            }
            ok
        } catch (_: Exception) {
            cachedRootAvailable = false
            false
        }
    }

    suspend fun executeCommand(command: String, runAsRoot: Boolean = true): ShellResult = withContext(Dispatchers.IO) {
        try {
            val cmdArgs = if (runAsRoot) {
                if (mountMasterFlag == null) isRootAvailable()
                val flag = mountMasterFlag ?: ""
                if (flag.isNotEmpty()) {
                    arrayOf("su", flag, "-c", command)
                } else {
                    arrayOf("su", "-c", command)
                }
            } else {
                arrayOf("sh", "-c", command)
            }
            val process = ProcessBuilder(*cmdArgs)
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
