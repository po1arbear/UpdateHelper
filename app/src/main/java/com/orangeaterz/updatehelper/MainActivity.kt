package com.orangeaterz.updatehelper

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.orangeaterz.updatehelper.update.UpdateHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_download.setOnClickListener {
            UpdateHelper.from(this)
                .apkPath("https://b4230d05989abd4dae51b342c67c215f.dd.cdntips.com/download.sj.qq.com/upload/connAssitantDownload/upload/MobileAssistant_1.apk?mkey=5bd97d98b4a41d3d&f=1849&cip=180.164.59.200&proto=https")
                .serverVersionCode(2)
                .checkBy(UpdateHelper.CHECK_BY_VERSION_CODE)
                .start()
        }
    }
}
