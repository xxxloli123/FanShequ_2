package com.fanhong.cn.tools

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.spec.PKCS8EncodedKeySpec

object SignUtils {

    private val ALGORITHM = "RSA"

    private val SIGN_ALGORITHMS = "SHA1WithRSA"

    private val SIGN_SHA256RSA_ALGORITHMS = "SHA256WithRSA"

    private val DEFAULT_CHARSET = "UTF-8"

    private fun getAlgorithms(rsa2: Boolean): String {
        return if (rsa2) SIGN_SHA256RSA_ALGORITHMS else SIGN_ALGORITHMS
    }

    fun sign(content: String, privateKey: String, rsa2: Boolean): String? {
        try {
            val priPKCS8 = PKCS8EncodedKeySpec(
                    Base64.decode(privateKey))
            val keyf = KeyFactory.getInstance(ALGORITHM)
            val priKey = keyf.generatePrivate(priPKCS8)

            val signature = java.security.Signature
                    .getInstance(getAlgorithms(rsa2))

            signature.initSign(priKey)
            signature.update(content.toByteArray(charset(DEFAULT_CHARSET)))

            val signed = signature.sign()

            return Base64.encode(signed)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

}
