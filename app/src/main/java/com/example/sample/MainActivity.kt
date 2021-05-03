package com.example.sample

import android.Manifest
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity() {
    companion object {
        const val PERMISSION_REQUEST_STORAGE = 0
    }

    lateinit var downloadController: DownloadController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager: PackageManager = this.packageManager
        val info: PackageInfo = manager.getPackageInfo(
            this.packageName, 0
        )
        val version = info.longVersionCode
        Log.i("info", "!!!App current version $version ")
        val isLatest = isLatestVersion(version)



        setContentView(R.layout.activity_main)
        // This apk is taking pagination sample app
        val apkUrl = "https://github.com/furiously-Curious/SampleAPK/raw/main/Sample.apk"
        downloadController = DownloadController(this, apkUrl)
        findViewById<Button>(R.id.buttonDownload).setOnClickListener {
            // check storage permission granted if yes then start downloading file
            checkStoragePermission()
        }
    }

    private fun isLatestVersion(currentVersion: Long): Boolean{
        val apkVersionUrl =
            "https://raw.githubusercontent.com/furiously-Curious/SampleAPKConfig/main/SampleAppVersion.json"
        var result: Boolean
        GlobalScope.launch(Dispatchers.IO) {
            val jsonText = httpGet(apkVersionUrl)
            val apkVersionJson = JSONObject(jsonText)
            Log.i("info", jsonText)
            result = apkVersionJson.getLong("latestVersionCode") == currentVersion
        }
        return result

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

                Snackbar.make(
                    View(this),
                    getString(R.string.storage_permission_denied),
                    Snackbar.LENGTH_SHORT
                )
            }
        }
    }

    private fun checkStoragePermission() {
        // Check if the storage permission has been granted
        if (checkSelfPermissionCompat(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            // start downloading
            downloadController.enqueueDownload()
        } else {
            // Permission is missing and must be requested.
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

    private fun httpGetReq(myURL: String?): String {

        val inputStream: InputStream
        val result: String

        // create URL
        val url: URL = URL(myURL)

        // create HttpURLConnection
        val conn: HttpURLConnection = url.openConnection() as HttpURLConnection

        // make GET request to the given URL
        conn.connect()

        // receive response as inputStream
        inputStream = conn.inputStream

        // convert inputstream to string
        result = if (inputStream != null) inputStreamToString(inputStream)
        else "Did not work!"

        return result
    }

    private fun inputStreamToString(inputStream: InputStream): String {
        val reader = BufferedReader(inputStream.reader())
        var content: String
        try {
            content = reader.readText()
        } finally {
            reader.close()
        }
        return content
    }

    private suspend fun httpGet(myURL: String?): String {

        return withContext(Dispatchers.IO) {

             httpGetReq(myURL)

        }
    }
}