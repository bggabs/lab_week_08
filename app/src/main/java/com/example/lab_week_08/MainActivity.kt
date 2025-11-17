package com.example.lab_week_08

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.work.*
import com.example.lab_week_08.worker.FirstWorker
import com.example.lab_week_08.worker.SecondWorker
import com.example.lab_week_08.worker.ThirdWorker

class MainActivity : AppCompatActivity() {

    private val wm by lazy { WorkManager.getInstance(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Permission for notification
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        startSequence()
    }

    private fun startSequence() {

        // 1. FirstWorker
        val first = OneTimeWorkRequest.Builder(FirstWorker::class.java).build()

        // 2. SecondWorker
        val second = OneTimeWorkRequest.Builder(SecondWorker::class.java).build()

        // 4. ThirdWorker
        val third = OneTimeWorkRequest.Builder(ThirdWorker::class.java).build()

        wm.beginWith(first)
            .then(second)
            .enqueue()

        // 3. NotificationService → setelah SecondWorker
        wm.getWorkInfoByIdLiveData(second.id).observe(this) { info ->
            if (info?.state == WorkInfo.State.SUCCEEDED) {
                startFirstNotificationService()
            }
        }

        // ThirdWorker → setelah NotificationService selesai
        NotificationService.trackingCompletion.observe(this) {
            wm.enqueue(third)
        }

        // SecondNotificationService → setelah ThirdWorker
        wm.getWorkInfoByIdLiveData(third.id).observe(this) { info ->
            if (info?.state == WorkInfo.State.SUCCEEDED) {
                startSecondNotificationService()
            }
        }
    }


    private fun startFirstNotificationService() {
        val intent = Intent(this, NotificationService::class.java)
            .putExtra("Id", "001")
        ContextCompat.startForegroundService(this, intent)
    }

    private fun startSecondNotificationService() {
        val intent = Intent(this, SecondNotificationService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
