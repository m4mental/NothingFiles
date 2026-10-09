package com.nothing.files.engine

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import com.nothing.files.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class AppManagerEngine(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    suspend fun getInstalledApps(includeSystem: Boolean = false): List<AppItem> = withContext(Dispatchers.IO) {
        val appList = mutableListOf<AppItem>()
        try {
            val flags = PackageManager.GET_META_DATA
            val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(flags)
            }

            for (pkg in packages) {
                val appInfo = pkg.applicationInfo ?: continue
                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                if (!includeSystem && isSystem) {
                    continue
                }

                val apkFile = File(appInfo.sourceDir)
                val appName = try {
                    packageManager.getApplicationLabel(appInfo).toString()
                } catch (_: Exception) {
                    pkg.packageName
                }

                val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pkg.longVersionCode
                } else {
                    @Suppress("DEPRECATION")
                    pkg.versionCode.toLong()
                }

                val minSdk = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    appInfo.minSdkVersion
                } else {
                    0
                }

                appList.add(
                    AppItem(
                        packageName = pkg.packageName,
                        appName = appName,
                        versionName = pkg.versionName ?: "1.0",
                        versionCode = versionCode,
                        sourceDir = appInfo.sourceDir,
                        apkSize = if (apkFile.exists()) apkFile.length() else 0L,
                        isSystemApp = isSystem,
                        minSdk = minSdk,
                        targetSdk = appInfo.targetSdkVersion,
                        uid = appInfo.uid,
                        firstInstallTime = pkg.firstInstallTime,
                        lastUpdateTime = pkg.lastUpdateTime
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        appList.sortedBy { it.appName.lowercase() }
    }

    suspend fun extractApk(app: AppItem): File? = withContext(Dispatchers.IO) {
        try {
            val srcFile = File(app.sourceDir)
            if (!srcFile.exists()) return@withContext null

            val targetDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                "NothingFiles/Apks"
            )
            if (!targetDir.exists()) targetDir.mkdirs()

            val sanitizedAppName = app.appName.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
            val targetFile = File(targetDir, "${sanitizedAppName}_v${app.versionName}.apk")

            srcFile.copyTo(targetFile, overwrite = true)
            targetFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun launchApp(packageName: String): Boolean {
        return try {
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(launchIntent)
                true
            } else false
        } catch (_: Exception) {
            false
        }
    }

    fun openAppDetails(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
