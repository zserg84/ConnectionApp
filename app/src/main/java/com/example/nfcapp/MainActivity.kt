package com.example.nfcapp

import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.example.nfcapp.databinding.ActivityBinder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

public class MainActivity : AppCompatActivity, NfcAdapter.ReaderCallback {

    companion object {
        private val TAG = MainActivity::class.java.getSimpleName()
    }

    private var binder : ActivityBinder? = null
    private val viewModel : MainViewModel by lazy { ViewModelProvider(this).get(MainViewModel::class.java) }

    constructor() {

    }

    override fun onCreate(savedInstanceState : Bundle?) {
        binder = DataBindingUtil.setContentView(this@MainActivity, R.layout.activity_main)
        binder?.setViewModel(viewModel)
        binder?.setLifecycleOwner(this@MainActivity)
        super.onCreate(savedInstanceState)
        Coroutines.main(this@MainActivity, { scope ->
            scope.launch( block = { binder?.getViewModel()?.observeNFCStatus()?.collectLatest ( action = { status -> Log.d(TAG, "observeNFCStatus $status")
                NFCManager.enableReaderMode(this@MainActivity, this@MainActivity, this@MainActivity, viewModel.getNFCFlags(), viewModel.getExtras())
            }) })
            scope.launch( block = { binder?.getViewModel()?.observeToast()?.collectLatest ( action = { message -> Log.d(TAG, "observeToast $message")
                Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
            }) })
            scope.launch( block = { binder?.getViewModel()?.observeTag()?.collectLatest ( action = { tag -> Log.d(TAG, "observeTag $tag")
                binder?.textViewExplanation?.setText(tag)
            }) })
        })
    }

    override fun onTagDiscovered(tag : Tag?) {
        binder?.getViewModel()?.readTag(tag)
    }
}