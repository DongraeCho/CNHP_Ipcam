package com.deepmedi.cnhp_ipcam.utils

import android.Manifest
import android.app.Activity
import android.graphics.Bitmap
import android.os.Build
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

class Permissions(private val activity: Activity) {

    private var _prePermissioned = MutableLiveData<Boolean>()
    val prePermissioned: LiveData<Boolean> get() = _prePermissioned
    private var _postPremissioned = MutableLiveData<Boolean>()
    val postPremissioned: LiveData<Boolean> get() = _postPremissioned

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) { // 마시멜로(안드로이드 6.0) 이상 권한 체크
            TedPermission.with(activity)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("카메라를 사용하기 위해서는 접근 권한이 필요합니다")
                .setDeniedMessage("앱에서 요구하는 권한설정이 필요합니다...\n [설정] > [권한] 에서 사용으로 활성화해주세요.")
                .setPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.INTERNET,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_WIFI_STATE
                )
                .check()
        } else {
            _prePermissioned.value = true
        }
    }

    private val permissionlistener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            _postPremissioned.value = true
        }

        override fun onPermissionDenied(deniedPermissions: List<String>) {
            activity.finish()
            Toast.makeText(activity, "권한 허용을 하지 않으면 서비스를 이용할 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    init {
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == 0 &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.INTERNET) == 0 &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == 0 &&
            ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) == 0) {
            _prePermissioned.value = true
        } else {
            checkPermissions()
        }
    }
}
