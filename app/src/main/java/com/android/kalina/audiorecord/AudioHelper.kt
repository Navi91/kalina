package com.android.kalina.audiorecord

import android.content.Context
import android.os.Environment
import java.io.*

/**
 * Created by Dmitriy on 08.02.2018.
 */
class AudioHelper {

    companion object {
        fun fileToBytes(file: File?): ByteArray? {
            if (file == null) return null

            val size = file.length().toInt()
            val bytes = ByteArray(size)

            try {
                val buf = BufferedInputStream(FileInputStream(file))
                buf.read(bytes, 0, bytes.size)
                buf.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                return null
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }

            return bytes
        }

        fun getAudioFile(context: Context, fileName: String): File? {
            val path = createFileName(context, fileName)
            val file = File(path)

            if (file.exists()) {
                return file
            }

            return null
        }

        fun audioFileExist(context: Context, fileName: String) = getAudioFile(context, fileName) != null

        fun createFileName(context: Context, fileName: String) = "${getAudioDirectory(context).absolutePath}/$fileName.m4a"

        private fun getAudioDirectory(context: Context): File {
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "audio")
            file.mkdir()

            return file
        }
    }
}