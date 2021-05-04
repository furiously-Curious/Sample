package com.example.sample

import android.Manifest
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    lateinit var downloadController: DownloadController

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)
        val button = findViewById<Button>(R.id.buttonDownload)
        val version = "Version "+getCurrentAppVersion().toString()
        button.text = version
        button.setOnClickListener {
            updateAppIfAvailable()
        }
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private fun updateAppIfAvailable() {
        val apkVersionUrl =
            "https://raw.githubusercontent.com/furiously-Curious/SampleAPKConfig/main/SampleAppVersion.json"

        val request = StringRequest(apkVersionUrl, Response.Listener<String> {
            val apkVersionJson = JSONObject(it)
            downloadController = DownloadController(this, apkVersionJson.getString("apkUrl"))

            Log.i("info", it)
            if (!isLatestVersion(apkVersionJson.getLong("latestVersionCode"))) {
                Toast.makeText(
                    applicationContext,
                    "Initiate download of latest app version",
                    Toast.LENGTH_SHORT
                ).show();
                Log.i("info", "Initiate download of latest app version")
                checkStoragePermission()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Already running latest app version",
                    Toast.LENGTH_SHORT
                ).show();
                Log.i("info", "Already running latest app version")
            }
        }, Response.ErrorListener {
            Toast.makeText(applicationContext, "Some error occurred!!", Toast.LENGTH_SHORT).show();
            Log.e("Error", "Error fetching data ${it.message}")
        });
        Volley.newRequestQueue(this).add(request);
    }


    @RequiresApi(Build.VERSION_CODES.P)
    private fun isLatestVersion(latestVersion: Long): Boolean {
        val version = getCurrentAppVersion()
        Log.i("info", "!!!App current version $version ")
        return (latestVersion == version)
    }

    private fun getCurrentAppVersion(): Long {
        val manager: PackageManager = this.packageManager
        val info: PackageInfo = manager.getPackageInfo(
            this.packageName, 0
        )
        val version = info.longVersionCode
        return version
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_STORAGE) {
            // Request for camera permission.
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // start downloading
                downloadController.enqueueDownload()
            } else {
                // Permission request was denied.
                Toast.makeText(applicationContext, "Permission Denied", Toast.LENGTH_SHORT).show();
                Log.i("info", getString(R.string.storage_permission_denied))
            }
        }
    }

    private fun checkStoragePermission() {
        // Check if the storage permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // start downloading
            Log.i("info","Permission granted, initiate download !!!!!!")
            downloadController.enqueueDownload()
        } else {
            // Permission is missing and must be requested.
            Log.i("info","Requesting permission")
            requestStoragePermission()
        }
    }

    private fun requestStoragePermission() {
        if (shouldShowRequestPermissionRationaleCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            val snackbar = Snackbar.make(
                View(this),
                getString(R.string.storage_access_required),
                Snackbar.LENGTH_INDEFINITE
            )
            snackbar.setAction("") {
                requestPermissionsCompat(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_STORAGE
                )
            }.show()

        } else {
            requestPermissionsCompat(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_STORAGE
            )
        }
    }
}