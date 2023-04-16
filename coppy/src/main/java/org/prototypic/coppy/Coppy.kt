package org.prototypic.coppy

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.flow.MutableStateFlow
import org.json.JSONObject
import java.io.*
import java.net.URL
import java.util.concurrent.TimeUnit
import javax.net.ssl.HttpsURLConnection

class CoppyUpdateWorker(val appContext: Context, workerParams: WorkerParameters): Worker(appContext, workerParams) {
    override fun doWork(): Result {
        val config = CoppyUtils.getConfig(appContext)
        if (config == null) {
            return Result.success()
        }
        val properties = this.appContext.getSharedPreferences(config.propertiesKey, Context.MODE_PRIVATE)
        val eTag = properties.getString("eTag", null)
        val url = URL(config.contentUrl)

        if (eTag != null) {
            val connection = url.openConnection() as HttpsURLConnection
            connection.requestMethod = "HEAD"
            val contentETag = connection.getHeaderField("Etag")
            if (contentETag == eTag) return Result.success()
        }

        if (Coppy._contentClass != null) {
            val connection = url.openConnection() as HttpsURLConnection
            val contentETag = connection.getHeaderField("Etag")
            val content = readUrl(url)

            val newContent = Coppy._contentClass!!.newInstance() as Updatable
            newContent.update(content)
            this.saveContent(newContent as Serializable, config)
            properties.edit().putString("eTag", contentETag).apply()

            if (config.updateType == "foreground") {
                if (Coppy._content != null) {
                    Coppy._content?.value = newContent
                } else {
                    Coppy._content = MutableStateFlow(newContent)
                }
            }
        }

        return Result.success()
    }
    private fun readUrl(url: URL): JSONObject {
        val input = url.openStream()
        val reader = BufferedReader(InputStreamReader(input))
        val result = JSONObject(reader.readText())
        reader.close()
        input.close();

        return result;
    }
    private fun saveContent(content: Serializable, config: CoppyConfig) {
        val dir = appContext.getDir(config.contentDir, Context.MODE_PRIVATE)
        this.clearFiles(dir)
        val file = FileOutputStream("${dir.path}/${config.contentFileName}")
        val outStream = ObjectOutputStream(file)

        outStream.writeObject(content)
        outStream.close()
        file.close()
    }
    private fun clearFiles(dir: File) {
        val dirFiles = dir.listFiles()
        if (dirFiles == null || dirFiles.isEmpty()) return
        for (file in dirFiles) {
            file.delete()
        }
    }
}
interface Updatable {
    fun update(obj: JSONObject?) {}
}

class CoppyConfig (
    val contentUrl: String,
    val contentDir: String,
    val contentFileName: String,
    val propertiesKey: String,
    val updateType: String
)

object CoppyUtils {
    fun getConfig(context: Context): CoppyConfig? {
        val applicationInfo = context.packageManager.getApplicationInfoCompat(context.packageName, PackageManager.GET_META_DATA)
        val metaData = applicationInfo.metaData
        if (metaData == null) return null

        val spaceKey = metaData.getString("org.prototypic.coppy.spaceKey")
        val updateType = metaData.getString("org.prototypic.coppy.updateType")
        if (spaceKey == null) return null

        return CoppyConfig(
            "https://content.coppy.app/${spaceKey}/content",
            spaceKey,
            context.packageManager.getPackageVersionCompat(context.packageName).toString(),
            "${context.packageName}.coppy.${spaceKey}",
            updateType ?: "default")
    }
}

internal fun PackageManager.getPackageVersionCompat(packageName: String): Int =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0.toLong())).longVersionCode.toInt()
    } else {
        @Suppress("DEPRECATION") getPackageInfo(packageName, 0).versionCode
    }

internal fun PackageManager.getApplicationInfoCompat(packageName: String, flags: Int = 0): ApplicationInfo =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getApplicationInfo(packageName, PackageManager.ApplicationInfoFlags.of(flags.toLong()))
    } else {
        @Suppress("DEPRECATION") getApplicationInfo(packageName, flags)
    }


@Suppress("unused")
object Coppy {
    internal var _content: MutableStateFlow<Any>? = null
    internal var _contentClass: Class<Any>? = null


    fun <T>useContent(contentClass: Class<T>): MutableStateFlow<T> {
        val c = _content
        if (c != null) {
            return c as MutableStateFlow<T>
        }

        val initialContent = contentClass.newInstance()
        val mutableContent = MutableStateFlow(initialContent)
        _content = mutableContent as MutableStateFlow<Any>

        return mutableContent
    }

    fun initialize(appContext: Context, contentClass: Class<*>) {
        _contentClass = contentClass as Class<Any>
        val content = this.readContent(appContext)

        if (content != null) {
            if (_content != null) {
                _content?.value = content
            } else {
                _content = MutableStateFlow(content)
            }
        }

        val config = CoppyUtils.getConfig(appContext)
        if (config == null) {
            return
        }

        if (config.updateType == "background") {
            ProcessLifecycleOwner.get().lifecycle.addObserver(CoppyObserver(appContext))
        }

        val constraints = Constraints.Builder().setRequiresBatteryNotLow(true).build()
        val coppyWorkRequest: PeriodicWorkRequest = PeriodicWorkRequestBuilder<CoppyUpdateWorker>(3, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(appContext).enqueueUniquePeriodicWork(config.propertiesKey, ExistingPeriodicWorkPolicy.UPDATE, coppyWorkRequest)
    }

    internal fun readContent(appContext: Context): Any? {
        val config = CoppyUtils.getConfig(appContext)
        if (config == null) {
            return null
        }

        val dir = appContext.getDir(config.contentDir, Context.MODE_PRIVATE)

        var content: Any? = null
        try {
            val file = FileInputStream("${dir.path}/${config.contentFileName}")
            val inStream = ObjectInputStream(file)
            content = inStream.readObject()

            inStream.close()
            file.close()
        } catch (err: Exception) {}
        return content
    }
}

private class CoppyObserver(private val appContext: Context): DefaultLifecycleObserver {
    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        WorkManager.getInstance(this.appContext).enqueue(OneTimeWorkRequestBuilder<CoppySwapWorker>().build())
    }
}

class CoppySwapWorker(val appContext: Context, workerParams: WorkerParameters):
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val config = CoppyUtils.getConfig(this.appContext)
        if (config == null) return Result.success()

        val properties = this.appContext.getSharedPreferences(config.propertiesKey, Context.MODE_PRIVATE)
        val eTag = properties.getString("eTag", null)
        val appliedContentETag = properties.getString("appliedETag", null)

        if (eTag != null && appliedContentETag != null && eTag == appliedContentETag) return Result.success()

        val contentFromFile = Coppy.readContent(this.appContext)
        if (contentFromFile != null) {
            if (Coppy._content != null) {
                Coppy._content?.value = contentFromFile
            } else {
                Coppy._content = MutableStateFlow(contentFromFile)
            }

            if (eTag != null) {
                properties.edit().putString("appliedETag", eTag).apply()
            }
        }
        return Result.success()
    }
}
