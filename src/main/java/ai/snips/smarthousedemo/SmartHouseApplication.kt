package ai.snips.smarthousedemo

import android.app.Application
import java.io.BufferedInputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipInputStream


class SmartHouseApplication : Application() {

    companion object {
        lateinit var appContext: SmartHouseApplication
        val rootDir : File by lazy { appContext.filesDir }
        val assistantDir by lazy { File(rootDir, "assistant") }
        val versionFile by lazy { File(assistantDir, "android_version_${BuildConfig.VERSION_NAME}") }
    }


    override fun onCreate() {
        super.onCreate()
        appContext = this

        if (!versionFile.exists()) {
            try {
                assistantDir.deleteRecursively()
            } catch (e: Exception) {
                println("could not clean previous assistant dir")
            }
            println("unzipping assets")
            unzip(assets.open("assistant.zip"), rootDir)
            versionFile.createNewFile()
            println("unzipping done")

        }
    }

    private fun unzip(zipFile: InputStream, targetDirectory: File) {
        ZipInputStream(BufferedInputStream(zipFile)).use { zis ->
            var ze = zis.nextEntry
            var count: Int
            val buffer = ByteArray(8192)

            while (ze != null) {
                val file = File(targetDirectory, ze.name)
                println("unzipping ${file.absoluteFile}")
                val dir = if (ze.isDirectory) file else file.parentFile
                if (!dir.isDirectory && !dir.mkdirs())
                    throw FileNotFoundException("Failed to ensure directory: " + dir.absolutePath)
                if (ze.isDirectory) {
                    ze = zis.nextEntry
                    continue
                }
                FileOutputStream(file).use { fout ->
                    count = zis.read(buffer)
                    while (count != -1) {
                        fout.write(buffer, 0, count)
                        count = zis.read(buffer)
                    }
                }
                ze = zis.nextEntry
            }
        }
    }
}
