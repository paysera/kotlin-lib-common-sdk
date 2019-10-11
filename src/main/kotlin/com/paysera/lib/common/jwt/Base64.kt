package com.paysera.lib.common.jwt

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.util.zip.Deflater
import java.util.zip.Inflater

class Base64 {

    private var mString: String? = null
    private var mIndex = 0

    private val nextUsefulChar: Char
        get() {
            var result = '_'
            while (!isUsefulChar(result)) {
                result = this.mString!![this.mIndex++]
            }

            return result
        }

    fun internalEncode(data: ByteArray): String {
        val charCount = data.size * 4 / 3 + 4

        val result = StringBuffer(charCount * 77 / 76)

        val byteArrayLength = data.size
        var byteArrayIndex = 0
        var byteTriplet: Int
        while (byteArrayIndex < byteArrayLength - 2) {
            byteTriplet = convertUnsignedByteToInt(data[byteArrayIndex++])

            byteTriplet = byteTriplet shl 8
            byteTriplet = byteTriplet or convertUnsignedByteToInt(data[byteArrayIndex++])
            byteTriplet = byteTriplet shl 8
            byteTriplet = byteTriplet or convertUnsignedByteToInt(data[byteArrayIndex++])

            val b4 = (0x3F and byteTriplet).toByte()

            byteTriplet = byteTriplet shr 6
            val b3 = (0x3F and byteTriplet).toByte()
            byteTriplet = byteTriplet shr 6
            val b2 = (0x3F and byteTriplet).toByte()
            byteTriplet = byteTriplet shr 6
            val b1 = (0x3F and byteTriplet).toByte()

            result.append(mapByteToChar(b1))
            result.append(mapByteToChar(b2))
            result.append(mapByteToChar(b3))
            result.append(mapByteToChar(b4))
        }

        if (byteArrayIndex == byteArrayLength - 1) {
            byteTriplet = convertUnsignedByteToInt(data[byteArrayIndex++])

            byteTriplet = byteTriplet shl 4

            val b2 = (0x3F and byteTriplet).toByte()
            byteTriplet = byteTriplet shr 6
            val b1 = (0x3F and byteTriplet).toByte()

            result.append(mapByteToChar(b1))
            result.append(mapByteToChar(b2))

            result.append("==")
        }

        if (byteArrayIndex == byteArrayLength - 2) {
            byteTriplet = convertUnsignedByteToInt(data[byteArrayIndex++])
            byteTriplet = byteTriplet shl 8
            byteTriplet = byteTriplet or convertUnsignedByteToInt(data[byteArrayIndex++])

            byteTriplet = byteTriplet shl 2

            val b3 = (0x3F and byteTriplet).toByte()
            byteTriplet = byteTriplet shr 6
            val b2 = (0x3F and byteTriplet).toByte()
            byteTriplet = byteTriplet shr 6
            val b1 = (0x3F and byteTriplet).toByte()

            result.append(mapByteToChar(b1))
            result.append(mapByteToChar(b2))
            result.append(mapByteToChar(b3))

            result.append("=")
        }

        return result.toString()
    }

    fun internalDecode(data: String): ByteArray {
        this.mString = data
        this.mIndex = 0

        var mUsefulLength = 0
        val length = this.mString!!.length
        for (i in 0 until length) {
            if (isUsefulChar(this.mString!![i])) {
                mUsefulLength++
            }

        }

        val byteArrayLength = mUsefulLength * 3 / 4

        val result = ByteArray(byteArrayLength)

        var byteTriplet: Int
        var byteIndex = 0

        while (byteIndex + 2 < byteArrayLength) {
            byteTriplet = mapCharToInt(nextUsefulChar)
            byteTriplet = byteTriplet shl 6
            byteTriplet = byteTriplet or mapCharToInt(nextUsefulChar)
            byteTriplet = byteTriplet shl 6
            byteTriplet = byteTriplet or mapCharToInt(nextUsefulChar)
            byteTriplet = byteTriplet shl 6
            byteTriplet = byteTriplet or mapCharToInt(nextUsefulChar)

            result[byteIndex + 2] = (byteTriplet and 0xFF).toByte()
            byteTriplet = byteTriplet shr 8
            result[byteIndex + 1] = (byteTriplet and 0xFF).toByte()
            byteTriplet = byteTriplet shr 8
            result[byteIndex] = (byteTriplet and 0xFF).toByte()
            byteIndex += 3
        }

        if (byteIndex == byteArrayLength - 1) {
            byteTriplet = mapCharToInt(nextUsefulChar)
            byteTriplet = byteTriplet shl 6
            byteTriplet = byteTriplet or mapCharToInt(nextUsefulChar)

            byteTriplet = byteTriplet shr 4
            result[byteIndex] = (byteTriplet and 0xFF).toByte()
        }

        if (byteIndex == byteArrayLength - 2) {
            byteTriplet = mapCharToInt(nextUsefulChar)
            byteTriplet = byteTriplet shl 6
            byteTriplet = byteTriplet or mapCharToInt(nextUsefulChar)
            byteTriplet = byteTriplet shl 6
            byteTriplet = byteTriplet or mapCharToInt(nextUsefulChar)

            byteTriplet = byteTriplet shr 2
            result[byteIndex + 1] = (byteTriplet and 0xFF).toByte()
            byteTriplet = byteTriplet shr 8
            result[byteIndex] = (byteTriplet and 0xFF).toByte()
        }

        return result
    }

    private fun mapCharToInt(c: Char): Int {
        if (c >= 'A' && c <= 'Z') {
            return c - 'A'
        }

        if (c >= 'a' && c <= 'z') {
            return c - 'a' + 26
        }

        if (c >= '0' && c <= '9') {
            return c - '0' + 52
        }

        if (c == '+') {
            return 62
        }

        if (c == '/') {
            return 63
        }

        throw IllegalArgumentException("$c is not a valid Base64 character.")
    }

    private fun mapByteToChar(b: Byte): Char {
        if (b < 26) {
            return (65 + b).toChar()
        }

        if (b < 52) {
            return (97 + (b - 26)).toChar()
        }

        if (b < 62) {
            return (48 + (b - 52)).toChar()
        }

        if (b.toInt() == 62) {
            return '+'
        }

        if (b.toInt() == 63) {
            return '/'
        }

        throw IllegalArgumentException("Byte $b is not a valid Base64 value")
    }

    private fun isUsefulChar(c: Char): Boolean {
        return c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z' || c >= '0' && c <= '9' || c == '+' || c == '/'
    }

    private fun convertUnsignedByteToInt(b: Byte): Int {
        return if (b >= 0) {
            b.toInt()
        } else 256 + b

    }

    companion object {

        private val LOWER_CASE_A_VALUE = 26
        private val ZERO_VALUE = 52
        private val PLUS_VALUE = 62
        private val SLASH_VALUE = 63
        private val SIX_BIT_MASK = 63
        private val EIGHT_BIT_MASK = 255

        fun encode(data: ByteArray): String {
            return Base64().internalEncode(data)
        }

        fun decode(data: String): ByteArray {
            return Base64().internalDecode(data)
        }

        fun getEncodeMsg(tMessage: String): String {
            var returnStr = ""
            try {
                val byteStream = getCompressedStr(tMessage)

                if (byteStream != null)
                    returnStr = encode(byteStream.toByteArray())
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            return returnStr
        }

        private fun getCompressedStr(tMessage: String?): ByteArrayOutputStream? {
            var compressedStream: ByteArrayOutputStream? = null
            try {
                if (tMessage != null && "" != tMessage) {
                    val input = tMessage.toByteArray(charset("UTF-8"))

                    val compresser = Deflater()
                    compresser.setInput(input)
                    compresser.finish()

                    compressedStream = ByteArrayOutputStream()
                    val buf = ByteArray(2048)

                    while (!compresser.finished()) {
                        val got = compresser.deflate(buf)
                        if (got < 1)
                            break
                        compressedStream.write(buf, 0, got)
                    }
                    compresser.end()
                }
            } catch (ioex: Exception) {
                ioex.printStackTrace()
            } finally {
                if (compressedStream != null) {
                    try {
                        compressedStream.close()
                    } catch (ioex: IOException) {
                        ioex.printStackTrace()
                    }

                }

            }

            return compressedStream
        }

        fun getDecodeMsg(tMessage: String): String {
            var outputString = ""
            val inputStr: ByteArray?
            try {
                var newStr = tMessage
                newStr = newStr.replace(" ".toRegex(), "+")
                inputStr = decode(newStr)
                outputString = getDeCompressedStr(inputStr)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            return outputString
        }

        private fun getDeCompressedStr(tMessage: ByteArray?): String {
            var returnStr = ""
            var aDeCompressedStream: ByteArrayOutputStream? = null
            try {
                val decompresser = Inflater()
                decompresser.setInput(tMessage!!)
                aDeCompressedStream = ByteArrayOutputStream()

                val buf = ByteArray(2048)
                while (!decompresser.finished()) {
                    val got = decompresser.inflate(buf)
                    if (got < 1)
                        break
                    aDeCompressedStream.write(buf, 0, got)
                }
                decompresser.end()
            } catch (ioex: Exception) {
                ioex.printStackTrace()
            } finally {
                try {
                    aDeCompressedStream?.close()
                } catch (ioex: IOException) {
                    ioex.printStackTrace()
                }

            }
            try {
                if (aDeCompressedStream != null) {
                    returnStr = aDeCompressedStream.toString("UTF-8")
                }
            } catch (encodeEx: UnsupportedEncodingException) {
                encodeEx.printStackTrace()
            }

            return returnStr
        }
    }
}