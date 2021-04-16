package com.deepmedi.cnhp_ipcam.sdk

import androidx.lifecycle.LifecycleOwner
import com.deepmedi.deepmedialgorithms.dsp.Butterworth
import com.deepmedi.deepmedialgorithms.dsp.Filter
import com.deepmedi.deepmedialgorithms.structure.DataACC
import com.deepmedi.deepmedialgorithms.structure.DataGYRO
import com.deepmedi.deepmedialgorithms.structure.DataPPG
import com.deepmedi.deepmedialgorithms.utils.EventObserver
import com.deepmedi.deepmedimeasure.repository.sensor.RepositorySensor
import com.deepmedi.deepmedimeasure.viewmodel.Cam30ViewModel
import com.deepmedi.deepmedimeasure.viewmodel.RecordViewModel
import com.deepmedi.deepmedimeasure.viewmodel.SensorViewModel

class IPCamForPhSDK (
    private val viewLifecycleOwner: LifecycleOwner,
    private val mMesureViewModel: Cam30ViewModel,
    private val mSensorViewModel: SensorViewModel,
    private val mRecordViewModel: RecordViewModel,
    private val repositorySensor: RepositorySensor){

    private var mLightState = false

    private var filterActive = false
    private var postureDetect = false
    private var fingerDetect = false
    private var proxiDetect = false
    private var pramFixState = false

    private var timestamp = 0f
    private var recState = false
    private var preRecord = false
    private var isRecord = false
    private var endRecord = false
    private var complete = false

    private var toggle = false
    private var ppg:DataPPG? = null

    private val acc =
        ArrayList<DataACC>()

    private val coeff1 =
        Butterworth.getCoeffBandpass(0.5, 8.0, 30.0)

    private var mListener: IPCamForPhSDKListener? = null

    interface IPCamForPhSDKListener {
        fun getDisplay(data:ByteArray?,
                       ppg:DataPPG?,
                       acc:Array<FloatArray>?,
                       recLength:Int)
        fun recordingComplete()
    }

    fun setOnIPCamForPhSDKListener(listener: IPCamForPhSDKListener) {
        mListener = listener
    }

    init{

        mMesureViewModel.run{
            measureStart()
            setFlashActive(false)
            mMainCamByteArray.observe(viewLifecycleOwner, EventObserver{
                if(toggle){
                    mListener?.getDisplay(
                        data = it,
                        ppg = ppg,
                        acc = convArray(acc),
                        recLength = mRecordViewModel.ppgList.size
                    )
                    acc.clear()
                }
                toggle = !toggle
            })

            mFingerDetect.observe(viewLifecycleOwner, EventObserver {
                fingerDetect = it
            })

            mPPGsignal.observe(viewLifecycleOwner, EventObserver {ppgData->
                mRecordViewModel.recordCheck(
                    isRecord = recState,
                    fingerDetect = fingerDetect,
                    postureDetect = postureDetect,
                    proxiDetect = proxiDetect
                )

                if (isRecord && !endRecord)
                    mRecordViewModel.ppgList.add(ppgData)

                val data =
                    if(filterActive)
                        filterDisp(ppgData)
                    else
                        ppgData

                ppg = data
            })
        }

        mRecordViewModel.run{
            preRecording.observe(viewLifecycleOwner, EventObserver {
                preRecord = it
                if (preRecord && !pramFixState) {
                    pramFixState = true
                    mMesureViewModel.camParamFix(pramFixState)
                }

            })

            isRecording.observe(viewLifecycleOwner, EventObserver {
                isRecord = it
                if (!isRecord && timestamp > 0 && pramFixState) {
                    pramFixState = false
                    mMesureViewModel.camParamFix(pramFixState)
                }
            })

            endRecording.observe(viewLifecycleOwner, EventObserver {
                endRecord = it
                if (!complete && endRecord) {
                    complete = true
                    recState = false
                    if (pramFixState) {
                        pramFixState = false
                        mMesureViewModel.camParamFix(pramFixState)
                    }
                    saveData()
                }
            })

            saveComplete.observe(viewLifecycleOwner, EventObserver{
                if(it) {
                    mMesureViewModel.setFlashActive(false)
                    mListener?.recordingComplete()
                }
            })

            recTimeing.observe(viewLifecycleOwner, EventObserver {
                timestamp = it
            })

        }

        mSensorViewModel.run{
            mACCSignal.observe(viewLifecycleOwner, EventObserver {
//                mMesureViewModel.deviceMovingDetect(it[0].getX(), it[0].getZ())
                mRecordViewModel.accList.addAll(it)
            })

            mGYROsignal.observe(viewLifecycleOwner, EventObserver{
                mRecordViewModel.gyroList.addAll(it)
            })

            mPosition.observe(viewLifecycleOwner, EventObserver {
                postureDetect = true
            })

            mProximity.observe(viewLifecycleOwner, EventObserver {
                proxiDetect = it
            })

            mLightSignal.observe(viewLifecycleOwner, EventObserver {
                if(!mLightState) {
                    repositorySensor.setProxiValue(it.getVal())
                    mLightState = true
                }
            })
            setOnSensorViewModelListener(object : SensorViewModel.SensorViewModelListener{
                override fun getACCForDisp(dataACC: DataACC) {
                    acc.add(dataACC)
                }

                override fun getGYROForDisp(dataGYRO: DataGYRO) = Unit

            })
        }

    }

    fun recStart(){
        recState = true
        mMesureViewModel.setFlashActive(true)
    }

    fun setFlash(boolean: Boolean){
        mMesureViewModel.setFlashActive(boolean)
    }

    private fun convArray(data: ArrayList<DataACC>):Array<FloatArray>{
        val accX = FloatArray(data.size)
        val accY = FloatArray(data.size)
        val accZ = FloatArray(data.size)

        data.forEachIndexed { index, dataACC ->
            accX[index] = dataACC.getX()
            accY[index] = dataACC.getY()
            accZ[index] = dataACC.getZ()
        }
        return arrayOf(accX, accY, accZ)
    }

    private fun filterDisp(data: DataPPG): DataPPG {
        val dataBuffer =
            Filter.filter(data.getSig()[1].toDouble(), coeff1[0], coeff1[1])
        return DataPPG(
            floatArrayOf(
                dataBuffer.toFloat(),
                dataBuffer.toFloat(),
                dataBuffer.toFloat()
            ), data.getTime()
        )
    }

}