package com.azzan.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var imamSection: View
    private val PREFS_NAME = "ImamPrefs"
    private val PIN_KEY = "imam_pin"

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(this, "Notifications required for Azaan", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imamSection = findViewById(R.id.imamSection)
        
        checkPermissions()
        setupUI()
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun setupUI() {
        findViewById<TextView>(R.id.tvMasjidName).setOnLongClickListener {
            showPinDialog()
            true
        }

        findViewById<Button>(R.id.btnTestAzaan).setOnClickListener {
            startAzaanService()
        }

        findViewById<Button>(R.id.btnStop).setOnClickListener {
            stopAzaanService()
        }

        findViewById<Button>(R.id.btnBroadcast).setOnClickListener {
            lifecycleScope.launch {
                val success = AzaanBroadcaster.sendAzaanBroadcast()
                Toast.makeText(this@MainActivity, if(success) "Broadcast Sent" else "Failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showPinDialog() {
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val storedPin = prefs.getString(PIN_KEY, null)
        val input = EditText(this).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }

        MaterialAlertDialogBuilder(this)
            .setTitle(if (storedPin == null) "Set PIN" else "Enter PIN")
            .setView(input)
            .setPositiveButton("OK") { _, _ ->
                val pinInput = input.text.toString()
                if (storedPin == null || pinInput == storedPin) {
                    if (storedPin == null) prefs.edit().putString(PIN_KEY, pinInput).apply()
                    imamSection.visibility = View.VISIBLE
                }
            }.show()
    }

    private fun startAzaanService() {
        val intent = Intent(this, PrayerAudioService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(intent) else startService(intent)
    }

    private fun stopAzaanService() {
        startService(Intent(this, PrayerAudioService::class.java).apply { action = "STOP_AZAAN" })
    }
}