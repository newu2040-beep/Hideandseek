package com.example

import android.app.Application
import com.example.data.db.VaultDatabase

class OneLockApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Room Database
        VaultDatabase.getInstance(this)
    }
}
