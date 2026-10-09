package com.nothing.files.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Arrays
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class EncryptionEngine {

    companion object {
        private val MAGIC_HEADER = "NOTHING_AES256".toByteArray(Charsets.UTF_8) // 14 bytes
    }

    private fun deriveKey(password: String): SecretKeySpec {
        val digest = MessageDigest.getInstance("SHA-256")
        val keyBytes = digest.digest(password.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    suspend fun encryptFile(
        source: File,
        destination: File,
        password: String,
        deleteSource: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val key = deriveKey(password)
            val iv = ByteArray(16)
            SecureRandom().nextBytes(iv)
            val ivSpec = IvParameterSpec(iv)

            val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec)

            FileOutputStream(destination).use { fos ->
                // Write magic header + IV
                fos.write(MAGIC_HEADER)
                fos.write(iv)

                CipherOutputStream(fos, cipher).use { cos ->
                    FileInputStream(source).use { fis ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (fis.read(buffer).also { read = it } != -1) {
                            cos.write(buffer, 0, read)
                        }
                    }
                }
            }

            if (deleteSource && destination.exists() && destination.length() > 0) {
                source.delete()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            if (destination.exists()) destination.delete()
            false
        }
    }

    suspend fun decryptFile(
        source: File,
        destination: File,
        password: String,
        deleteSource: Boolean = false
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            FileInputStream(source).use { fis ->
                val magic = ByteArray(MAGIC_HEADER.size)
                val readMagic = fis.read(magic)
                if (readMagic != MAGIC_HEADER.size || !Arrays.equals(magic, MAGIC_HEADER)) {
                    // Not encrypted with this engine or corrupted
                    return@withContext false
                }

                val iv = ByteArray(16)
                val readIv = fis.read(iv)
                if (readIv != 16) return@withContext false

                val key = deriveKey(password)
                val ivSpec = IvParameterSpec(iv)

                val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
                cipher.init(Cipher.DECRYPT_MODE, key, ivSpec)

                CipherInputStream(fis, cipher).use { cis ->
                    FileOutputStream(destination).use { fos ->
                        val buffer = ByteArray(8192)
                        var read: Int
                        while (cis.read(buffer).also { read = it } != -1) {
                            fos.write(buffer, 0, read)
                        }
                    }
                }
            }

            if (deleteSource && destination.exists() && destination.length() > 0) {
                source.delete()
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            if (destination.exists()) destination.delete()
            false
        }
    }

    fun isEncryptedFile(file: File): Boolean {
        if (!file.exists() || file.length() < MAGIC_HEADER.size + 16) return false
        return try {
            FileInputStream(file).use { fis ->
                val magic = ByteArray(MAGIC_HEADER.size)
                fis.read(magic)
                Arrays.equals(magic, MAGIC_HEADER)
            }
        } catch (_: Exception) {
            false
        }
    }
}
