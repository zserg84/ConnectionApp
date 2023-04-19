package com.example.nfcapp

import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NdefFormatable
import android.os.Build
import androidx.annotation.RequiresApi
import java.io.IOException

object NFCUtil {
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun createNFCMessage(payload: String, intent: Intent?): Boolean {
        if (NfcAdapter.ACTION_TECH_DISCOVERED == intent?.action
            || NfcAdapter.ACTION_NDEF_DISCOVERED == intent?.action
        ) {

            if (intent === null) {
                return false
            }

            val tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java) ?: return false
//            val ndef = Ndef.get(tag) ?: return false
////
////            if (ndef.isWritable) {
////                val message = NdefMessage(
////                    arrayOf(
////                        NdefRecord.createTextRecord("en", "132"),
////                        NdefRecord.createTextRecord("en", "asd"),
////                        NdefRecord.createTextRecord("en", payload)
////                    )
////                )
////
////                ndef.connect()
////                ndef.writeNdefMessage(message)
////                ndef.close()
                return true
            }
//            return false
//        }
        return false
//        val pathPrefix = "peterjohnwelcome.com:nfcapp"
//        val nfcRecord = NdefRecord(NdefRecord.TNF_EXTERNAL_TYPE, pathPrefix.toByteArray(), ByteArray(0), payload.toByteArray())
//        val nfcMessage = NdefMessage(arrayOf(nfcRecord))
//        intent?.let {
//            val tag = it.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
//            return writeMessageToTag(nfcMessage, tag)
//        }
//        return false
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun retrieveNFCMessage(intent: Intent?): String {
        intent?.let {
            if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
                val nDefMessages = getNDefMessages(intent)
                nDefMessages[0].records?.let {
                    it.forEach { it1 ->
                        it1?.payload.let { it2 ->
                            it2?.let {
                                return String(it2)
                            }
                        }
                    }
                }

            } else {
                return "Touch NFC tag to read data"
            }
        }
        return "Touch NFC tag to read data"
    }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getNDefMessages(intent: Intent): Array<NdefMessage> {

        val rawMessage = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES, Tag::class.java)
        rawMessage?.let {
            return rawMessage.map {
                it as NdefMessage
            }.toTypedArray()
        }
        // Unknown tag type
        val empty = byteArrayOf()
        val record = NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty)
        val msg = NdefMessage(arrayOf(record))
        return arrayOf(msg)
    }

    fun disableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity) {
        nfcAdapter.disableForegroundDispatch(activity)
    }

    fun <T> enableNFCInForeground(nfcAdapter: NfcAdapter, activity: Activity, classType: Class<T>) {
        val pendingIntent = PendingIntent.getActivity(activity, 0,
            Intent(activity, classType).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0)
        val nfcIntentFilter = IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED)
        val filters = arrayOf(nfcIntentFilter)

        val techLists = arrayOf(arrayOf(Ndef::class.java.name), arrayOf(NdefFormatable::class.java.name))

        nfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techLists)
    }


    private fun writeMessageToTag(nfcMessage: NdefMessage, tag: Tag?): Boolean {

        try {
            val nDefTag = Ndef.get(tag)

            nDefTag?.let {
                it.connect()
                if (it.maxSize < nfcMessage.toByteArray().size) {
                    //Message to large to write to NFC tag
                    return false
                }
                if (it.isWritable) {
                    it.writeNdefMessage(nfcMessage)
                    it.close()
                    //Message is written to tag
                    return true
                } else {
                    //NFC tag is read-only
                    return false
                }
            }

            val nDefFormatableTag = NdefFormatable.get(tag)

            nDefFormatableTag?.let {
                try {
                    it.connect()
                    it.format(nfcMessage)
                    it.close()
                    //The data is written to the tag
                    return true
                } catch (e: IOException) {
                    //Failed to format tag
                    return false
                }
            }
            //NDEF is not supported
            return false

        } catch (e: Exception) {
            //Write operation has failed
        }
        return false
    }
}