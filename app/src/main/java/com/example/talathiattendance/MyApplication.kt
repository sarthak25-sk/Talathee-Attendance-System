package com.example.talathiattendance

import android.app.Application
import com.example.talathiattendance.domain.utils.SessionManager

class MyApplication:Application(){
    val sessionManager by lazy { SessionManager(this) }
}