package dyhelper.com.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;
import dyhelper.com.services.ProgressUpdate;

public class HttpDownloader {
	private final static String TAG = "HttpDownloader";
	private URL url = null;
	public int mDownloadLen = 0;

	// �����ı��ļ���.txt .xml�ȶ����ԣ�
	public String downLoadText(String strurl) {
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader br = null;
		try {
			url = new URL(strurl);
			HttpURLConnection urlConn = (HttpURLConnection) url
					.openConnection();
			// getInputStream�õ������ֽ�������װ��InputStreamReader�������ַ�����
			// �ٷ�װ��BufferedReader������Ե�����readLine������һ��һ�н��ж�ȡ
			br = new BufferedReader(new InputStreamReader(
					urlConn.getInputStream()));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	// ����������ʽ���ļ�,������д��SD����
	// ����ֵ��-1���������ļ�����0�����������أ�1�����ļ��Ѵ�����SD����
	public String downLoadFile(Context context, String strurl, String fileName,
			ProgressUpdate progressUpdate) {
		int len = 0;
		int startPos = 0;
		String filefullpath = null;
		HttpURLConnection urlConn = null;
		InputStream inputstream = null;
		FileUtils fileutils = new FileUtils(progressUpdate);
		filefullpath = fileutils.isFileExist(fileName);
		if (filefullpath != null
				&& getApplicationInfo(context,
						Uri.parse("file://" + filefullpath)) == null) {// add by
																		// wangfuda
			File fl = new File(filefullpath);
			if (fl.exists()) {
				fl.delete();
			}

		}

		try {
			url = new URL(strurl);
			urlConn = (HttpURLConnection) url.openConnection();
			urlConn.setConnectTimeout(20000);
			urlConn.setReadTimeout(120000);

			Log.i("qhb", "get content-startPos:" + startPos);
			urlConn.setRequestProperty("Range", "bytes=" + startPos + "-");// ���û�ȡʵ�����ݵķ�Χ

			inputstream = urlConn.getInputStream();
			len = urlConn.getContentLength();
			Log.i("qhb", "get content-length:" + len);
			if (len <= 0) {
				filefullpath = null;
			} else {
				File file = fileutils.inputSD(fileName, inputstream, len);
				Log.i("qhb", "downLoadFile inputSD end");
				if (file == null)
					filefullpath = null;
				else
					filefullpath = new String(file.getAbsolutePath());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				Log.i("qhb", "downLoadFile end");
				inputstream.close();
				urlConn.disconnect();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		}

		return filefullpath;
	}

	public static PackageInfo getApplicationInfo(Context context, Uri packageURI) {
		final String archiveFilePath = packageURI.getPath();
		PackageInfo info = null;

		try {
			PackageManager pm = context.getPackageManager();
			info = pm.getPackageArchiveInfo(archiveFilePath,
					PackageManager.GET_ACTIVITIES);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return info;
	}

}
