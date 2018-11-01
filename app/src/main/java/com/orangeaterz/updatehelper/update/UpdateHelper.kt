package com.orangeaterz.updatehelper.update

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.text.TextUtils
import android.util.Log
import com.orangeaterz.updatehelper.R

class UpdateHelper private constructor(private val activity: Activity) {
    private var checkBy = CHECK_BY_VERSION_CODE
    private var downloadBy = DOWNLOAD_BY_APP
    private var serverVersionCode = 0
    private var apkPath = ""
    private var serverVersionName = ""
    private var isForce = false //是否强制更新
    private var localVersionCode = 0
    private var localVersionName = ""
    private var updateInfo = ""
    private var showNotification = true

    companion object {
        const val CHECK_BY_VERSION_NAME = 1001
        const val CHECK_BY_VERSION_CODE = 1002
        const val DOWNLOAD_BY_APP = 1003
        const val DOWNLOAD_BY_BROWSER = 1004
        const val CONFIRM = 1005
        const val CANCEL = 1006
        var needFitAndroidN = true //提供给 整个工程不需要适配到7.0的项目 置为false

        fun from(activity: Activity): UpdateHelper {
            return UpdateHelper(activity)
        }


        /**
         * 检测wifi是否连接
         */
        private fun isWifiConnected(context: Context): Boolean {
            val cm: ConnectivityManager? = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val networkInfo = cm?.activeNetworkInfo
            return networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI
        }
    }

    private val isNeedUpdate: Boolean
        get() = serverVersionCode > localVersionCode


    fun needFitAndroidN(needFitAndroidN: Boolean): UpdateHelper {
        UpdateHelper.needFitAndroidN = needFitAndroidN
        return this
    }

    init {
        getAPPLocalVersion(activity)
    }

    fun checkBy(checkBy: Int): UpdateHelper {
        this.checkBy = checkBy
        return this
    }

    fun apkPath(apkPath: String): UpdateHelper {
        this.apkPath = apkPath
        return this
    }

    fun downloadBy(downloadBy: Int): UpdateHelper {
        this.downloadBy = downloadBy
        return this
    }

    fun showNotification(showNotification: Boolean): UpdateHelper {
        this.showNotification = showNotification
        return this
    }

    fun updateInfo(updateInfo: String): UpdateHelper {
        this.updateInfo = updateInfo
        return this
    }


    fun serverVersionCode(serverVersionCode: Int): UpdateHelper {
        this.serverVersionCode = serverVersionCode
        return this
    }

    fun serverVersionName(serverVersionName: String): UpdateHelper {
        this.serverVersionName = serverVersionName
        return this
    }

    fun isForce(isForce: Boolean): UpdateHelper {
        this.isForce = isForce
        return this
    }

    //获取apk的版本号 currentVersionCode
    private fun getAPPLocalVersion(ctx: Context) {

        val manager = ctx.packageManager
        try {
            val info = manager.getPackageInfo(ctx.packageName, 0)
            localVersionName = info.versionName // 版本名
            localVersionCode = info.versionCode // 版本号
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

    }

    private fun update() {
        when (checkBy) {
            CHECK_BY_VERSION_CODE -> if (serverVersionCode > localVersionCode) {
                showUpdateDialog()
            } else {

            }
            CHECK_BY_VERSION_NAME -> if (serverVersionName != localVersionName) {
                showUpdateDialog()
            } else {
            }
        }
    }

    fun start() {
        if (!isNeedUpdate) return
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            update()
        } else {
            if (ContextCompat.checkSelfPermission(
                            activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
            ) {
                update()
            } else {//申请权限
                ActivityCompat.requestPermissions(
                        activity,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1
                )
            }
        }
    }

    private fun showUpdateDialog() {
        val dialog = UpdateDialog(activity, Callback { position ->
            when (position) {
                CANCEL -> if (isForce) {
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    activity.startActivity(intent)
                    System.exit(0)
                }
                CONFIRM -> {
                    if (TextUtils.isEmpty(apkPath)) {
                        return@Callback
                    }
                    if (downloadBy == DOWNLOAD_BY_APP) {
                        if (isWifiConnected(activity)) {
                            //                                DownloadHelper.downloadForAutoInstall(activity, apkPath, "demo.apk", serverVersionName);
                            DownloadHelper.downloadInApp(activity, apkPath, serverVersionCode.toString())
                        } else {
                            UpdateDialog(activity, Callback { }).setContent("目前手机不是WiFi状态\n" + "确认是否继续下载更新？").show()
                        }
                    } else if (downloadBy == DOWNLOAD_BY_BROWSER) {
                        DownloadHelper.downloadByBrowser(activity, apkPath)
                    }
                }
            }
        })

        var content =
                activity.getString(R.string.new_version_found) + serverVersionName + "\n" + activity.getString(R.string.whether_to_update)
        if (!TextUtils.isEmpty(updateInfo)) {
            content = activity.getString(R.string.new_version_found) + serverVersionName +
                    activity.getString(R.string.whether_to_update) + "\n\n" + updateInfo
        }
        dialog.setContent(content)
        dialog.setCancelable(false)
        dialog.show()
    }


}
