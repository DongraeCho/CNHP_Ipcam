package com.deepmedi.cnhp_ipcam.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import com.deepmedi.cnhp_ipcam.R
import com.deepmedi.deepmedimeasure.utils.PrefUtil


import com.google.android.material.button.MaterialButtonToggleGroup

class IPCamForPHReceiverDialog0 (private val activity: Activity){

    private var mListener: IPCamForPHReceiverDialog0Listener? = null
    private val width:Int = activity.windowManager.defaultDisplay.width
    private val height:Int = activity.windowManager.defaultDisplay.height

    interface IPCamForPHReceiverDialog0Listener{
        fun okayClick(patient:String,
                      recTime:Int,
                      age:Int,
                      gender:Int,
                      label:Int,
                      height:Int,
                      weight:Int)
        fun exceptions(e:String)
        fun cancelClick()
    }

    fun setOnIPCamForPHReceiverDialog0Listener(listener: IPCamForPHReceiverDialog0Listener) {
        mListener = listener
    }

    fun dialogStart(context: Context){
        Dialog(context).run{
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.sdk_fragment_ipcam_ph_reciever_dialog0)
            setCancelable(false)

            val params = window?.attributes?.also{
                it.width = width.times(2f).div(3f).toInt()
                it.height = height.times(9f).div(10f).toInt()
            }

            this.window?.attributes = params as WindowManager.LayoutParams

            var gender = 0
            var label = 0
            PrefUtil.getIntValue(context, "chunngnam_rectiem", 0).run {
                if(this != 0) findViewById<EditText>(R.id.edit_rec_time).setText(this.toString())
            }

            findViewById<Button>(R.id.okay).setOnClickListener {
                try{
                    val patient = findViewById<EditText>(R.id.edit_patient).text.toString()
                    val recTime = findViewById<EditText>(R.id.edit_rec_time).text.toString().toInt()
                    val height = findViewById<EditText>(R.id.edit_height).text.toString().toInt()
                    val weight = findViewById<EditText>(R.id.edit_weight).text.toString().toInt()
                    val age = findViewById<EditText>(R.id.edit_age).text.toString().toInt()

                    PrefUtil.setIntValue(context, "chunngnam_rectiem", recTime)
                    mListener?.okayClick(patient, recTime, age, gender, label, height, weight)
                }catch (e:Exception){
                    mListener?.exceptions("항목을 다 채워주세요...")
                }
                dismiss()
            }

            findViewById<Button>(R.id.cancel).setOnClickListener {
                dismiss()
                mListener?.cancelClick()
            }


            findViewById<MaterialButtonToggleGroup>(R.id.gender).addOnButtonCheckedListener { group, _, _ ->
                when(group.checkedButtonId){
                    findViewById<Button>(R.id.btn_man).id->{ gender = 0 }
                    findViewById<Button>(R.id.btn_woman).id->{ gender = 1 }
                }
            }
            findViewById<MaterialButtonToggleGroup>(R.id.labeling).addOnButtonCheckedListener { group, _, _ ->
                when(group.checkedButtonId){
                    findViewById<Button>(R.id.btn_yes).id->{ label = 1 }
                    findViewById<Button>(R.id.btn_no).id->{ label = 0 }
                }
            }
            show()
        }
    }
}