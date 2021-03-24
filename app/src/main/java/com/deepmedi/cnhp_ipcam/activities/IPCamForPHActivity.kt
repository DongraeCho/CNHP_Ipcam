package com.deepmedi.cnhp_ipcam.activities

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deepmedi.deepmedialgorithms.structure.DataPPG
import com.deepmedi.deepmedialgorithms.utils.CamDataConv
import com.deepmedi.deepmediipcam.tcp_ip.ClientTCP
import com.deepmedi.deepmedimeasure.CamHardware
import com.deepmedi.deepmedimeasure.repository.cam30.RepositoryCam30
import com.deepmedi.deepmedimeasure.repository.cam30.RepositoryCam30Param
import com.deepmedi.deepmedimeasure.repository.record.RepositoryRecord
import com.deepmedi.deepmedimeasure.repository.sensor.RepositorySensor
import com.deepmedi.deepmedimeasure.viewmodel.Cam30ViewModel
import com.deepmedi.deepmedimeasure.viewmodel.RecordViewModel
import com.deepmedi.deepmedimeasure.viewmodel.SensorViewModel
import com.deepmedi.cnhp_ipcam.App
import com.deepmedi.cnhp_ipcam.R
import com.deepmedi.cnhp_ipcam.dialog.IPCamForPHDialog0
import com.deepmedi.cnhp_ipcam.dialog.IPCamForPHDialog1
import com.deepmedi.cnhp_ipcam.sdk.IPCamForPhSDK
import com.deepmedi.cnhp_ipcam.sdk.ServerCommCNPH
import com.deepmedi.cnhp_ipcam.utils.RTChart
import com.deepmedi.cnhp_ipcam.utils.UserDataCNPH
import com.deepmedi.cnhp_ipcam.utils.VibList
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class IPCamForPHActivity : AppCompatActivity(){

    private val mMesureViewModel: Cam30ViewModel by viewModel()
    private val mSensorViewModel: SensorViewModel by viewModel()
    private val mRecordViewModel: RecordViewModel by viewModel()

    private val repositorySensor: RepositorySensor by inject()
    private val repositoryRecord: RepositoryRecord by inject()
    private val repoiratoryCam30: RepositoryCam30 by inject()
    private val repoiratoryCam30Param: RepositoryCam30Param by inject()

    private val clientTCP = ClientTCP()
    private var ipCamForPhSDK: IPCamForPhSDK? = null
    private var flashToggle = false

    private val dialog0: IPCamForPHDialog0 by lazy {
        IPCamForPHDialog0(this@IPCamForPHActivity).also {
            it.setOnIPCamForPHDialog0Listener(object : IPCamForPHDialog0.IPCamForPHDialog0Listener {
                override fun okayClick(ipAddress: String, port: Int) {
                    clientTCP.connect(ipAddress, port)
                }

                override fun cancelClick() {
                    finish()
                }

                override fun exceptions(e: String) {
                    Toast.makeText(App.instance.context(), e, Toast.LENGTH_LONG).show()
                }
            })
        }
    }
    private val dialog1: IPCamForPHDialog1 by lazy {
        IPCamForPHDialog1(this@IPCamForPHActivity).also {
            it.setOnIPCamForPHDialog1Listener(object : IPCamForPHDialog1.IPCamForPHDialog1Listener {
                override fun okayClick() = finish()
            })
        }
    }

    override fun onPause() {
        super.onPause()
        clientTCP.run{ release() }
        mMesureViewModel.run { cameraStop() }
        mSensorViewModel.run { sensorStop() }
        mRecordViewModel.run { release() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sdk_fragment_ipcam_ph)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        RTChart.setLineChart(findViewById(R.id.ppg_graph),"PPG Signal",15 * 4)
        RTChart.setLineChart(findViewById(R.id.accx_graph),"AccX Signal",500 * 4)
        RTChart.setLineChart(findViewById(R.id.accy_graph),"AccY Signal",500 * 4)
        RTChart.setLineChart(findViewById(R.id.accz_graph),"AccZ Signal",500 * 4)

        repoiratoryCam30.run{
            setByteArrayMat(true, 10)
            setTextureView(findViewById(R.id.textureview))
        }

        repoiratoryCam30Param.run {
            CamHardware(this@IPCamForPHActivity, CamHardware.FRONT).let{
                setCompensation(it.getCompensation(0, 1.2f))
                setLevCam(it.getHardware(0))
                setSelCam(it.getId(0))
            }
        }

        repositorySensor.run{
            setSensorActive(
                accActive = true,
                gyroActive = true,
                lightActive = true)
        }

        repositoryRecord.run{
            setRecordTime(1000)
            setPreTime(2)
            setTotalTime(null)
            setFreqRate(30)
        }

        ipCamForPhSDK =
            IPCamForPhSDK(
                viewLifecycleOwner = this@IPCamForPHActivity,
                mMesureViewModel = mMesureViewModel,
                mSensorViewModel = mSensorViewModel,
                mRecordViewModel = mRecordViewModel,
                repositorySensor = repositorySensor
            ).apply{
                setOnIPCamForPhSDKListener(object : IPCamForPhSDK.IPCamForPhSDKListener {
                    override fun getDisplay(data: ByteArray?, ppg: DataPPG?, acc: Array<FloatArray>?, recLength:Int) {
                        clientTCP.let { client ->
                            client.isConnected()?.run{
                                if (this)
                                    CamDataConv.setDataArrange(data, ppg, acc, recLength)?.run { client.write(this) }
                                else{
                                    ppg?.run{ RTChart.addEntryVal(findViewById(R.id.ppg_graph), this.getSig()[1], Color.GREEN, 15*4) }
                                    acc?.run{
                                        RTChart.addEntryMtx(findViewById(R.id.accx_graph), this[0], Color.GREEN, 500*4)
                                        RTChart.addEntryMtx(findViewById(R.id.accy_graph), this[1],  Color.GREEN, 500*4)
                                        RTChart.addEntryMtx(findViewById(R.id.accz_graph), this[2],  Color.GREEN, 500*4)
                                    }
                                }
                            }?:{
                                ppg?.run{ RTChart.addEntryVal(findViewById(R.id.ppg_graph), this.getSig()[1], Color.GREEN, 15*4) }
                                acc?.run{
                                    RTChart.addEntryMtx(findViewById(R.id.accx_graph), this[0], Color.GREEN, 500*4)
                                    RTChart.addEntryMtx(findViewById(R.id.accy_graph), this[1], Color.GREEN, 500*4)
                                    RTChart.addEntryMtx(findViewById(R.id.accz_graph), this[2], Color.GREEN, 500*4)
                                }
                            }()
                        }
                    }

                    override fun recordingComplete() {
                        VibList(this@IPCamForPHActivity).vibMeausreEnd()
                        clientTCP.release()
                        ServerCommCNPH().setOnmServerCommCNPHListener(object : ServerCommCNPH.ServerCommCNPHListener{
                            override fun onSucess(str: String) {
                                runOnUiThread {
                                    dialog1.dialogStart(
                                        this@IPCamForPHActivity,
                                        "NOTICE",
                                        "Patient: ${UserDataCNPH.patient.toString()} \n" +
                                                "Age: ${UserDataCNPH.age.toString()} \n" +
                                                "Height: ${UserDataCNPH.height.toString()} \n" +
                                                "Weight: ${UserDataCNPH.weight.toString()} \n " +
                                                "Gender :" + if(UserDataCNPH.gender==0) "MALE" else "FEMALE"
                                    )
                                }
                            }

                            override fun onEmpty(e: String) {
                                runOnUiThread {
                                    dialog1.dialogStart(
                                        this@IPCamForPHActivity,
                                        "ERROR",
                                        "Failed to Save... \n$e")
                                }
                            }

                            override fun onFailure(e: String) {
                                runOnUiThread {
                                    dialog1.dialogStart(
                                        this@IPCamForPHActivity,
                                        "ERROR",
                                        "Failed to Save... \n$e")
                                }
                            }
                        })
                    }
                })
            }

        findViewById<Button>(R.id.btn_conn).setOnClickListener {
            dialog0.dialogStart(this@IPCamForPHActivity, "IP ADRESS SEARCH")
        }

        findViewById<Button>(R.id.btn_flash).setOnClickListener {
            flashToggle=!flashToggle
            ipCamForPhSDK?.setFlash(flashToggle)
        }

        clientTCP.setOnClientTCPListener(object : ClientTCP.ClientTCPListener {
            override fun getByteArray(data: ByteArray) {
                runOnUiThread {
                    JSONObject(String(data)).run{
                        UserDataCNPH.patient = get("patient") as String
                        UserDataCNPH.gender = get("gender") as Int
                        UserDataCNPH.age = get("age") as Int
                        UserDataCNPH.height = get("height") as Int
                        UserDataCNPH.weight = get("weight") as Int
                        (get("recTime") as Int).run{
                            repositoryRecord.setRecordTime(this)
                            ipCamForPhSDK?.recStart()
                            VibList(this@IPCamForPHActivity).vibMeausreInit()
                        }
                    }
                }
            }

            override fun getSearchPortFail() {
                runOnUiThread {
                    Toast.makeText(this@IPCamForPHActivity, "Type the port number in server App!!", Toast.LENGTH_LONG).show()
                }
            }

            override fun connecting() {
                runOnUiThread {
                    findViewById<TextView>(R.id.state_txt).run{
                        text = "Connecting..."
                        setTextColor(Color.RED)
                    }
                    findViewById<Button>(R.id.btn_conn).isEnabled = false
                }
            }

            override fun disConnected() {
                runOnUiThread {
                    findViewById<TextView>(R.id.state_txt).run{
                        text = "Disconnected."
                        setTextColor(Color.BLACK)
                    }
                    findViewById<Button>(R.id.btn_conn).isEnabled = true
                }
            }
        })
    }

}