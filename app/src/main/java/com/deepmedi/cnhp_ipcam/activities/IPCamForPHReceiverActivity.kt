package com.deepmedi.cnhp_ipcam.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.deepmedi.deepmedialgorithms.utils.CamDataConv
import com.deepmedi.deepmediipcam.tcp_ip.ServerTCP
import com.deepmedi.cnhp_ipcam.App
import com.deepmedi.cnhp_ipcam.R
import com.deepmedi.cnhp_ipcam.dialog.IPCamForPHReceiverDialog0
import com.deepmedi.cnhp_ipcam.dialog.IPCamForPHReceiverDialog1
import com.deepmedi.cnhp_ipcam.utils.RTChart
import com.google.android.material.progressindicator.LinearProgressIndicator
import java.net.InetAddress
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressLint("SetTextI18n")
class IPCamForPHReceiverActivity : AppCompatActivity() {

    private val serverTCP = ServerTCP()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sdk_fragment_ipcam_ph_receiver)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        val wifiManager =
            App.instance.context()
                .applicationContext
                .getSystemService(Context.WIFI_SERVICE)
                    as WifiManager

        findViewById<TextView>(R.id.state_txt).run {
            text = "IP Address : ${getIPAddress(wifiManager)}"
            setTextColor(Color.BLACK)
        }
        RTChart.setLineChart(findViewById(R.id.ppg_graph), "PPG Signal",15 * 6)
        RTChart.setLineChart(findViewById(R.id.accx_graph), "AccX Signal",500 * 6)
        RTChart.setLineChart(findViewById(R.id.accy_graph), "AccY Signal",500 * 6)
        RTChart.setLineChart(findViewById(R.id.accz_graph), "AccZ Signal",500 * 6)

        findViewById<Button>(R.id.btn_conn).setOnClickListener {
            IPCamForPHReceiverDialog1(this@IPCamForPHReceiverActivity).run {
                dialogStart(this@IPCamForPHReceiverActivity, "PORT SELECTION")
                setOnIPCamForPHReceiverDialog1Listener(object :
                    IPCamForPHReceiverDialog1.IPCamForPHReceiverDialog1Listener {
                    override fun okayClick(port: Int) {
                        findViewById<Button>(R.id.btn_conn).isEnabled = false
                        findViewById<Button>(R.id.btn_rec).isEnabled = false
                        serverTCP.setPort(8080)
                    }

                    override fun exceptions(e: String) {
                        Toast.makeText(App.instance.context(), e, Toast.LENGTH_LONG).show()
                    }

                    override fun cancelClick() = Unit
                })
            }
        }

        findViewById<Button>(R.id.btn_rec).setOnClickListener {
            IPCamForPHReceiverDialog0(this@IPCamForPHReceiverActivity).run {
                dialogStart(this@IPCamForPHReceiverActivity).run {
                    setOnIPCamForPHReceiverDialog0Listener(object :
                        IPCamForPHReceiverDialog0.IPCamForPHReceiverDialog0Listener {
                        override fun okayClick(patient: String, recTime: Int, age: Int, gender: Int, label: Int, height: Int, weight: Int) {
                            val sendString = """{ "patient":$patient, 
                                                  "recTime":$recTime, 
                                                  "age":$age, 
                                                  "gender":$gender,
                                                  "label":$label,
                                                  "height":$height,
                                                  "weight":$weight}"""
                            serverTCP.write(sendString.toByteArray(Charsets.UTF_8))
                            findViewById<LinearProgressIndicator>(R.id.state_progress).max = recTime * 30
                        }

                        override fun exceptions(e: String) {
                            Toast.makeText(this@IPCamForPHReceiverActivity, e, Toast.LENGTH_LONG).show()
                        }

                        override fun cancelClick() {}
                    })
                }
            }
        }

        serverTCP.setOnServerTCPListener(object : ServerTCP.ServerTCPListener {
            override fun getPortSetInit(port: Int) {
                runOnUiThread {
                    findViewById<TextView>(R.id.state_txt).run {
                        setTextColor(Color.BLUE)
                        text = "IP Address : ${getIPAddress(wifiManager)} (Port Binding....)"
                    }
                    Toast.makeText(this@IPCamForPHReceiverActivity, "Port : $port Complete...", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun getByteArray(data: ByteArray) {
                runOnUiThread {
                    CamDataConv.getDataArrange(data, 10)?.let {
                        if(it.getState()){
                            findViewById<ImageView>(R.id.server_view).setImageBitmap(it.getBitmap())
                            findViewById<LinearProgressIndicator>(R.id.state_progress).progress = it.getDataLen()
                            RTChart.addEntryVal(findViewById(R.id.ppg_graph), it.getPPGSig(), Color.RED,15 * 6)
                            RTChart.addEntryMtx(findViewById(R.id.accx_graph), it.getAccSig()[0], Color.RED,500 * 6)
                            RTChart.addEntryMtx(findViewById(R.id.accy_graph), it.getAccSig()[1], Color.RED,500 * 6)
                            RTChart.addEntryMtx(findViewById(R.id.accz_graph), it.getAccSig()[2], Color.RED,500 * 6)
                        }
                    }
                }
            }

            override fun connecting() {
                runOnUiThread {
                    findViewById<Button>(R.id.btn_conn).isEnabled = false
                    findViewById<Button>(R.id.btn_rec).isEnabled = true
                    findViewById<TextView>(R.id.state_txt).run {
                        setTextColor(Color.RED)
                        text = "IP Address : ${getIPAddress(wifiManager)} (Connecting....)"
                    }
                }
            }

            override fun disConnected(e: String) {
                runOnUiThread {
                    RTChart.clearData(findViewById(R.id.ppg_graph))
                    RTChart.clearData(findViewById(R.id.accx_graph))
                    RTChart.clearData(findViewById(R.id.accy_graph))
                    RTChart.clearData(findViewById(R.id.accz_graph))
                    findViewById<Button>(R.id.btn_conn).isEnabled = true
                    findViewById<Button>(R.id.btn_rec).isEnabled = false
                    findViewById<TextView>(R.id.state_txt).run {
                        setTextColor(Color.BLACK)
                        text = "IP Address : ${getIPAddress(wifiManager)}"
                    }
                    findViewById<LinearProgressIndicator>(R.id.state_progress).progress = 0
                    Handler(Looper.getMainLooper())
                        .postDelayed(Runnable { recreate() }, 3000)
                }
            }

        })

    }

    fun getIPAddress(wifiManager: WifiManager): String {
        return InetAddress.getByAddress(
            ByteBuffer
                .allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(wifiManager.connectionInfo.ipAddress)
                .array()
        ).hostAddress
    }




}