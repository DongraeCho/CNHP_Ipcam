package com.deepmedi.cnhp_ipcam.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.deepmedi.cnhp_ipcam.R
import com.deepmedi.deepmedimeasure.utils.PrefUtil

class IPCamForPHDialog0 (private val activity: Activity){

    private var mListener: IPCamForPHDialog0Listener? = null
    private val width:Int = activity.windowManager.defaultDisplay.width
    private val height:Int = activity.windowManager.defaultDisplay.height


    interface IPCamForPHDialog0Listener{
        fun okayClick(ipAddress:String, port:Int)
        fun exceptions(e:String)
        fun cancelClick()
    }

    fun setOnIPCamForPHDialog0Listener(listener: IPCamForPHDialog0Listener) {
        mListener = listener
    }

    fun dialogStart(context:Context,content:String){
        Dialog(context).run{
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.sdk_fragment_ipcam_ph_dialog0)
            setCancelable(false)

            val params = window?.attributes?.also{
                it.width = width.times(3f).div(4f).toInt()
                it.height = height.times(1f).div(3f).toInt()
            }

            this.window?.attributes = params as WindowManager.LayoutParams

            PrefUtil.getStrValue(context, "chungnam_ipaddress","").let {
                if(it.isNotEmpty()) findViewById<EditText>(R.id.edit_ip_address).setText(it)
            }
            PrefUtil.getIntValue(context, "chungnam_gate",0).let{
                if(it != 0) findViewById<EditText>(R.id.edit_port).setText(it.toString())
            }

            findViewById<TextView>(R.id.dialog_title).text = content
            findViewById<Button>(R.id.okay).setOnClickListener {
                try{
                    val ipAddress = findViewById<EditText>(R.id.edit_ip_address).text.toString()
                    val port = findViewById<EditText>(R.id.edit_port).text.toString().toInt()
                    PrefUtil.setStrValue(context, "chungnam_ipaddress", ipAddress)
                    PrefUtil.setIntValue(context, "chungnam_gate", port)
                    mListener?.okayClick(ipAddress, port)
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