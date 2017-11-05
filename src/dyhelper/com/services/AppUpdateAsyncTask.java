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

	// �÷���������UI�̵߳���,����������UI�̵߳��� ���Զ�UI�ռ��������
	@Override
	protected void onPreExecute() {
		Log.i("qhb", "onPreExecute");
		dialog = new ProgressDialog(context);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// ����ˮƽ������
		dialog.setMax(100);
		dialog.setCancelable(false);
		dialog.setProgress(0);
		dialog.setTitle("���������°汾����" + AppUpdateManager.fileSize + "MB");
		dialog.show();
	}

	@Override
	protected String doInBackground(Integer... params) {

		File filesDir;
		filesDir = context.getFilesDir();
		HttpDownloader hdl = new HttpDownloader();
		String PlugPathTmp = filesDir.getAbsolutePath() + "/" + "android_helper"
				+ ".apk";// ��ֹ������ʱ�ļ������Ŀ¼������
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
	 * �����Integer������ӦAsyncTask�еĵڶ�������
	 * ��doInBackground�������У���ÿ�ε���publishProgress�������ᴥ��onProgressUpdateִ��
	 * onProgressUpdate����UI�߳���ִ�У����п��Զ�UI�ռ���в���
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {
		Log.i("qhb", "onProgressUpdate");
		int percent = values[0];
		dialog.setProgress(percent);
	}

	/**
	 * �����String������ӦAsyncTask�еĵ�����������Ҳ���ǽ���doInBackground�ķ���ֵ��
	 * ��doInBackground����ִ�н���֮�������У�����������UI�̵߳��� ���Զ�UI�ռ��������
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
