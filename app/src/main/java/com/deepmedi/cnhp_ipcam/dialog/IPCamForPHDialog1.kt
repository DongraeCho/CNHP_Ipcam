package com.deepmedi.cnhp_ipcam.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView

import com.deepmedi.cnhp_ipcam.R

class IPCamForPHDialog1 (private val activity: Activity){

    private var mListener: IPCamForPHDialog1Listener? = null
    private val width:Int = activity.windowManager.defaultDisplay.width
    private val height:Int = activity.windowManager.defaultDisplay.height


    interface IPCamForPHDialog1Listener{
        fun okayClick()
    }

    fun setOnIPCamForPHDialog1Listener(listener: IPCamForPHDialog1Listener) {
        mListener = listener
    }

    fun dialogStart(context:Context,content:String, notice:String){
        Dialog(context).run{
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.sdk_fragment_ipcam_ph_dialog1)
            setCancelable(false)

            val params = window?.attributes?.also{
                it.width = width.times(3f).div(4f).toInt()
                it.height = height.times(2f).div(5f).toInt()
            }

            this.window?.attributes = params as WindowManager.LayoutParams

            findViewById<TextView>(R.id.dialog_title).text = content
            findViewById<TextView>(R.id.dialog_noti).text = notice
            findViewById<Button>(R.id.okay).setOnClickListener {
                mListener?.okayClick()
                dismiss()
            }
            show()
        }
    }
}