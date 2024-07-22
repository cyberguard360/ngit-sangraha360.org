package com.example.sg360.utils

import android.util.Log
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

fun encrypt_data(data: String, key: String): String {
    val secretKey = SecretKeySpec(Base64.getDecoder().decode(key), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val encryptedData = cipher.doFinal(data.toByteArray())
    return Base64.getEncoder().encodeToString(encryptedData)
}

fun decrypt_data(encryptedData: String, key: String): String {
    val secretKey = SecretKeySpec(Base64.getDecoder().decode(key), "AES")
    val cipher = Cipher.getInstance("AES")
    cipher.init(Cipher.DECRYPT_MODE, secretKey)
    val decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData))
    return String(decryptedData)
}

fun jsonSerializableToTensor(strings: String, key: String): TensorBuffer {
    val rows: List<FloatArray> = strings.lines().map { line ->
        decrypt_data(line, key).split(',').map { value ->
            value.toFloat()
        }.toFloatArray()
    }

    val numRows = rows.size
    val numCols = rows[0].size

    val flatData = FloatArray(numRows * numCols)
    var index = 0
    for (row in rows) {
        for (value in row) {
            flatData[index++] = value
        }
    }

    val tensorBuffer = TensorBuffer.createFixedSize(intArrayOf(numRows, numCols), DataType.FLOAT32)
    tensorBuffer.loadArray(flatData, intArrayOf(numRows, numCols))
    Log.d("Row Size", numRows.toString())
    Log.d("Col Size", numCols.toString())
    Log.d("Tensor Buffer", tensorBuffer.toString())
    for (i in tensorBuffer.floatArray)
    {
        Log.d("Tensor", i.toString())
    }
    return tensorBuffer
}