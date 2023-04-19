package com.example.nfcapp

import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var mNfcAdapter: NfcAdapter? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        Toast.makeText(applicationContext, NFCUtil.retrieveNFCMessage(this.intent), Toast.LENGTH_LONG).show()
    }

    override fun onResume() {
        super.onResume()
        mNfcAdapter?.let {
            NFCUtil.enableNFCInForeground(it, this, javaClass)
        }
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter?.let {
            NFCUtil.disableNFCInForeground(it, this)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val messageEditText = findViewById<EditText>(R.id.messageEditText)
        val messageWrittenSuccessfully = NFCUtil.createNFCMessage(messageEditText.text.toString(), intent)
        Toast.makeText(applicationContext,
            messageWrittenSuccessfully.ifElse("Successful Written to Tag", "Something When wrong Try Again"),
            Toast.LENGTH_LONG).show()
    }
}

fun <T> Boolean.ifElse(primaryResult: T, secondaryResult: T) = if (this) primaryResult else secondaryResult
