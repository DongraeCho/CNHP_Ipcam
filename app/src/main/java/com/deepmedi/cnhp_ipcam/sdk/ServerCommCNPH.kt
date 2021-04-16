package com.deepmedi.cnhp_ipcam.sdk

import com.deepmedi.cnhp_ipcam.utils.UserDataCNPH
import com.deepmedi.deepmedimeasure.utils.SaveUtil
import com.google.gson.JsonObject
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.util.concurrent.TimeUnit

class ServerCommCNPH {

    private interface ApiService {

        companion object {
            const val BASEURL = "http://app.deep-medi.com/"
        }

        @Multipart
        @POST("cnh_history")
        fun saveCHData(
            @Part("id") id: RequestBody,
            @Part("age") age: RequestBody,
            @Part("height") height: RequestBody,
            @Part("weight") weight: RequestBody,
            @Part("gender") gender: RequestBody,
            @Part("label") label: RequestBody,
            @Part("ppg\"; filename=\"test.txt\" ") ppg: RequestBody,
            @Part("acc\"; filename=\"test.txt\" ") acc: RequestBody,
            @Part("gyro\"; filename=\"test.txt\" ") gyro: RequestBody
        ): Call<JsonObject>
    }

    init {

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.MINUTES)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()

        val retrofit = Retrofit.Builder().addConverterFactory(GsonConverterFactory.create())
            .baseUrl(ApiService.BASEURL)
            .client(okHttpClient)
            .build()


        retrofit.create(ApiService::class.java).saveCHData(
            id = RequestBody.create(MediaType.parse("text/plain"), UserDataCNPH.patient.toString()),
            age = RequestBody.create(MediaType.parse("text/plain"), UserDataCNPH.age.toString()),
            height = RequestBody.create(MediaType.parse("text/plain"), UserDataCNPH.height.toString()),
            weight = RequestBody.create(MediaType.parse("text/plain"), UserDataCNPH.weight.toString()),
            gender = RequestBody.create(MediaType.parse("text/plain"), UserDataCNPH.gender.toString()),
            label = RequestBody.create(MediaType.parse("text/plain"), UserDataCNPH.label.toString()),
            ppg = RequestBody.create(MediaType.parse("text/*"), File(SaveUtil.filePath, SaveUtil.ppgName)),
            acc = RequestBody.create(MediaType.parse("text/*"), File(SaveUtil.filePath, SaveUtil.accName)),
            gyro = RequestBody.create(MediaType.parse("text/*"), File(SaveUtil.filePath, SaveUtil.gyroName))
        ).run{
            enqueue(object : Callback<JsonObject> {
                override fun onResponse(call: Call<JsonObject>, response: Response<JsonObject>){
                    if (response.isSuccessful) {
                        response.body()?.run {
                            if (get("result").asString.matches("200".toRegex()))
                                mListener?.onSucess(this.get("message").asString)
                            else
                                mListener?.onFailure(this.toString())
                        } ?: {
                            mListener?.onEmpty("not response body..")
                        }()
                    }
                }

                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
                    mListener?.onFailure(t.message!!)
                }
            })
        }
    }

    private var mListener: ServerCommCNPHListener? = null

    interface ServerCommCNPHListener {
        fun onSucess(str: String)
        fun onEmpty(e:String)
        fun onFailure(e:String)
    }

    fun setOnmServerCommCNPHListener(mServerCommCNPHListener: ServerCommCNPHListener) {
        this.mListener = mServerCommCNPHListener
    }
}