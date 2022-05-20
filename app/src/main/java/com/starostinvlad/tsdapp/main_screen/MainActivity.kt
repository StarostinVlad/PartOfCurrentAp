package com.starostinvlad.tsdapp.main_screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.nfc.NfcAdapter
import android.os.Bundle
import android.os.Looper
import android.os.Parcelable
import android.provider.Settings
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.Navigation
import by.kirich1409.viewbindingdelegate.viewBinding
import com.starostinvlad.fan.Preferences
import com.starostinvlad.tsdapp.R
import com.starostinvlad.tsdapp.appComponent
import com.starostinvlad.tsdapp.databinding.ActivityMainBinding
import com.starostinvlad.tsdapp.rfid.RFIDHelper
import javax.inject.Inject


class MainActivity : AppCompatActivity(R.layout.activity_main), MainActivityContract {

    private var pendingIntent: PendingIntent? = null
    private lateinit var navController: NavController
    private val binding by viewBinding(ActivityMainBinding::bind, R.id.containerMainActivity)

    @Inject
    lateinit var preferences: Preferences

    @Inject
    lateinit var presenter: MainActivityPresenter

    @Inject
    lateinit var rfidHelper: RFIDHelper

    private lateinit var graph: NavGraph
    private var nfcAdapter: NfcAdapter? = null

    var listener: InterfaceName? = null

    // BroadcastReceiver to receiver scan data
    val receiver: BroadcastReceiver = object : BroadcastReceiver() {
        val TAG = javaClass.simpleName
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent!!.getByteArrayExtra("data")
            if (data != null) {
//                String barcode = Tools.Bytes2HexString(data, data.length);
                val barcode = String(data)
                listener?.onBarcodeReaded(barcode)
                Log.d(TAG, "onReceive: $data")
            }

        }
    }

    fun attachReaderListener(fragmentName: InterfaceName?) {
        listener = fragmentName
    }

    fun detachReaderListener() {
        listener = null
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        appComponent.inject(this)

        super.onCreate(savedInstanceState)

        val intent = Intent(this, javaClass).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }

        val filter = IntentFilter()
        filter.addAction("com.rfid.SCAN")
        registerReceiver(receiver, filter)

        pendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        navController = Navigation.findNavController(this, R.id.containerFragments);

        val navInflater = navController.navInflater
        graph = navInflater.inflate(R.navigation.navigation_graph)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        presenter.attachView(this)
        presenter.onLoaded()

        if (!checkPermissions())
            requestPermissions()

    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            42
        )
    }


    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }


    private val tickReceiver by lazy { makeBroadcastReceiver() }

    override fun onResume() {
        super.onResume()
        if (nfcAdapter != null) {
            if (!nfcAdapter!!.isEnabled) {
                Toast.makeText(this, getString(R.string.needPutOnNfc), Toast.LENGTH_LONG).show()
                showWirelessSettings()
            }
            nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
        }
        registerReceiver(tickReceiver, IntentFilter("android.rfid.FUN_KEY"))
    }

    override fun onDestroy() {
        rfidHelper.destroy()
        super.onDestroy()
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
        try {
            unregisterReceiver(tickReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e("Broadcast", "Time tick Receiver not registered", e)
        }
    }

    private fun showWirelessSettings() {
        val intent = Intent(Settings.ACTION_NFC_SETTINGS)
        startActivity(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let {
            val action = intent.action
            if (action == NfcAdapter.ACTION_NDEF_DISCOVERED ||
                action == NfcAdapter.ACTION_TAG_DISCOVERED ||
                action == NfcAdapter.ACTION_TECH_DISCOVERED
            ) {
                val tagId = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID)!!
                listener?.onTagReaded(tagId)
            }
        }
    }


    private fun makeBroadcastReceiver(): BroadcastReceiver {
        return object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent?) {
                val keyCode = intent?.getIntExtra("keyCode", 0)
//                Log.d("onReceive", "intent: $keyCode")
                if (keyCode == 133) {
                    rfidHelper.readRfidTag()
                } else if (keyCode == 4) {
                    onBackPressed()
                }
            }
        }
    }


    override fun openWorkScreen() {
        graph.startDestination = R.id.workFlowFragment
        navController.graph = graph
//        navController.popBackStack(R.id.loginFragment, true)
    }

    override fun openLoginScreen() {
        graph.startDestination = R.id.loginFragment
        navController.graph = graph
//        navController.popBackStack(R.id.loginFragment, true)
    }

    override fun openSettingScreen() {
//        navController.navigate(R.id.settingsFragment)
        navController.popBackStack(R.id.settingsFragment, true)
    }

    override fun showError(error: String?) {
        Toast.makeText(this, "$error", Toast.LENGTH_LONG).show()
    }

    companion object {
        fun start(caller: Context) {
            val intent = Intent(caller, MainActivity::class.java)
            caller.startActivity(intent)
        }
    }
}

interface InterfaceName {
    fun onTagReaded(tagId: ByteArray)
    fun onBarcodeReaded(barcode: String)
}