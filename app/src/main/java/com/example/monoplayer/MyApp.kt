package com.example.monoplayer
import android.app.Application
import io.objectbox.BoxStore

class MyApp : Application() {

    companion object {
        // This allows you to access boxStore from anywhere using MyApp.boxStore
        lateinit var boxStore: BoxStore
            private set
    }

    override fun onCreate() {
        super.onCreate()

        // Initialize ObjectBox ONCE here
        boxStore = MyObjectBox.builder()
            .androidContext(this)
            .build()
    }
}