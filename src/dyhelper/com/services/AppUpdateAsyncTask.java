package dyhelper.com.services;

import java.io.File;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import dyhelper.com.util.HttpDownloader;

public class AppUpdateAsyncTask extends AsyncTask<Integer, Integer, String> {
	private Context context;
	public static String downloadURL;
	public static ProgressDialog dialog = null;

	public AppUpdateAsyncTask(Context context, String downloadURL) {
		super();
		AppUpdateAsyncTask.downloadURL = downloadURL;
		this.context = context;
	}

	// 该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
	@Override
	protected void onPreExecute() {
		Log.i("qhb", "onPreExecute");
		dialog = new ProgressDialog(context);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置水平进度条
		dialog.setMax(100);
		dialog.setCancelable(false);
		dialog.setProgress(0);
		dialog.setTitle("正在下载新版本，共" + AppUpdateManager.fileSize + "MB");
		dialog.show();
	}

	@Override
	protected String doInBackground(Integer... params) {

		File filesDir;
		filesDir = context.getFilesDir();
		HttpDownloader hdl = new HttpDownloader();
		String PlugPathTmp = filesDir.getAbsolutePath() + "/" + "android_helper"
				+ ".apk";// 禁止更改临时文件保存的目录及名字
		String filepath = hdl.downLoadFile(context, downloadURL, PlugPathTmp,
				new ProgressUpdate() {

					@Override
					public void setProgress(int value, int totalSize) {
						try {
							float per = (float) value / totalSize;
							if (per < 0.01) {
								per = (float) 0.01;
							}
							String perString = String.valueOf(per);
							String percent = perString.substring(2, 4);
							int percentInt = Integer.valueOf(percent);
							if (percentInt % 10 == 0) {
								publishProgress(percentInt, value, totalSize);
							}
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
				});
		return filepath;
	}

	/**
	 * 这里的Integer参数对应AsyncTask中的第二个参数
	 * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
	 * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {
		Log.i("qhb", "onProgressUpdate");
		int percent = values[0];
		dialog.setProgress(percent);
	}

	/**
	 * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
	 * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
	 */

	protected void onPostExecute(String result) {
		Log.i("qhb", "onPostExecute");
		Log.i("qhb", "result:" + result);
		dialog.dismiss();
		Message m = AppUpdateManager.handler.obtainMessage();
		m.what = AppUpdateManager.DOWNLOAD_SUCCESS;
		Bundle b = new Bundle();
		b.putString("filePath", result);
		m.setData(b);
		AppUpdateManager.handler.sendMessage(m);
	}

}
