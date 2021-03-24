package com.deepmedi.cnhp_ipcam.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.deepmedi.cnhp_ipcam.activities.IPCamForPHActivity
import com.deepmedi.cnhp_ipcam.R
import com.deepmedi.cnhp_ipcam.utils.Permissions
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        Permissions(this@MainActivity).run {
            prePermissioned.observe(this@MainActivity, Observer {
                if(it){
                    Handler(Looper.getMainLooper()).post{
                        findViewById<MaterialButton>(R.id.sensor).isEnabled = true
                        findViewById<MaterialButton>(R.id.monitor).isEnabled = true
                    }
                }
            })
            postPremissioned.observe(this@MainActivity, Observer {
                this@MainActivity.recreate()
            })
        }

        findViewById<MaterialButton>(R.id.sensor).setOnClickListener {
            Intent(this@MainActivity, IPCamForPHActivity::class.java).run{
                startActivity(this)
            }
        }
        findViewById<MaterialButton>(R.id.monitor).setOnClickListener {
            Intent(this@MainActivity, IPCamForPHReceiverActivity::class.java).run{
                startActivity(this)
            }
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
        finishAndRemoveTask()
        android.os.Process.killProcess(android.os.Process.myPid())
    }

}