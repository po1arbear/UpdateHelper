package com.orangeaterz.updatehelper.update;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadLargeFileListener;
import com.liulishuo.filedownloader.FileDownloader;

import java.io.File;


class DownloadHelper {
    private static final String TAG = DownloadHelper.class.getSimpleName();
    public static long downloadUpdateApkId = -1;//下载更新Apk 下载任务对应的Id
    public static String downloadUpdateApkFilePath;//下载更新Apk 文件路径
    private static ProgressDialog mProgressDialog;

    /**
     * 通过浏览器下载APK包
     *
     * @param context
     * @param url
     */
    public static void downloadByBrowser(Context context, String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    public static void downloadInApp(final Context context, String url, final String serverVersionName) {

        String packageName = context.getPackageName();
        String filePath;
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {//外部存储卡
            filePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            Log.i(TAG, "没有SD卡");
            return;
        }

        String apkLocalPath = filePath + File.separator + packageName + "_" + serverVersionName + ".apk";
        downloadUpdateApkFilePath = apkLocalPath;

        FileDownloader.setup(context);

        FileDownloader.getImpl().create(url)
                .setPath(apkLocalPath)
                .setListener(new FileDownloadLargeFileListener() {
                    @Override
                    protected void pending(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                        refreshProgress(context, (int) (soFarBytes * 100.0 / totalBytes));
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, long soFarBytes, long totalBytes) {
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {
                        refreshProgress(context, 100);

                    }

                    @Override
                    protected void error(BaseDownloadTask task, Throwable e) {
                        Toast.makeText(context, "更新失败", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {

                    }
                }).start();
    }


    private static void refreshProgress(Context context, int progress) {
        if (mProgressDialog == null) mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        if (progress != 100) {
            if (!mProgressDialog.isShowing()) mProgressDialog.show();
            mProgressDialog.setProgress(progress);
        }
        if (progress == 100) {
            mProgressDialog.dismiss();
            if (DownloadHelper.downloadUpdateApkFilePath != null) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                File apkFile = new File(DownloadHelper.downloadUpdateApkFilePath);
                if (UpdateHelper.Companion.getNeedFitAndroidN() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    i.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    Uri contentUri = FileProvider.getUriForFile(
                            context, context.getPackageName() + ".fileprovider", apkFile);
                    i.setDataAndType(contentUri, "application/vnd.android.package-archive");
                } else {
                    i.setDataAndType(Uri.fromFile(apkFile),
                            "application/vnd.android.package-archive");
                }
//                i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(i);
            }
        }
    }


}
