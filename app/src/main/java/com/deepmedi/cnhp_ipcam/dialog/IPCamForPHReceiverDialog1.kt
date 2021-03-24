package com.deepmedi.cnhp_ipcam.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.deepmedi.deepmedimeasure.utils.PrefUtil

import com.deepmedi.cnhp_ipcam.R

class IPCamForPHReceiverDialog1 (private val activity: Activity){

    private var mListener: IPCamForPHReceiverDialog1Listener? = null
    private val width:Int = activity.windowManager.defaultDisplay.width
    private val height:Int = activity.windowManager.defaultDisplay.height

    interface IPCamForPHReceiverDialog1Listener{
        fun okayClick(port:Int)
        fun exceptions(e:String)
        fun cancelClick()
    }

    fun setOnIPCamForPHReceiverDialog1Listener(listener: IPCamForPHReceiverDialog1Listener) {
        mListener = listener
    }

    fun dialogStart(context: Context, content:String){
        Dialog(context).run{
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.sdk_fragment_ipcam_ph_reciever_dialog1)
            setCancelable(false)

            val params = window?.attributes?.also{
                it.width = width.times(1f).div(2f).toInt()
                it.height = height.times(1f).div(2f).toInt()
            }

            this.window?.attributes = params as WindowManager.LayoutParams

            PrefUtil.getIntValue(context, "chungnam_port", 0).run {
                if(this != 0) findViewById<EditText>(R.id.edit_port).setText(this.toString())
            }

            findViewById<TextView>(R.id.dialog_title).text = content
            findViewById<Button>(R.id.okay).setOnClickListener {
                try{
                    val port = findViewById<EditText>(R.id.edit_port).text.toString().toInt()
                    PrefUtil.setIntValue(context, "chungnam_port", port)
                    mListener?.okayClick(port)
                }catch (e:Exception){
                    mListener?.exceptions(e.toString())
                }
                dismiss()
            }

            findViewById<Button>(R.id.cancel).setOnClickListener {
                dismiss()
                mListener?.cancelClick()
            }
            show()
        }
    }

}