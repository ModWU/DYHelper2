package dyhelper.com.services;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import cn.dianyou.nets.DYHttpUtils;
import cn.dianyou.nets.DYStringCallback;
import cn.dianyou.utils.DEncodingUtils;
import dyhelper.com.bean.DeviceInfo;
import dyhelper.com.ui.XYZApplication;
import dyhelper.com.util.Constants;
import dyhelper.com.util.Http;
import okhttp3.Call;

public class AppUpdateManager {
	public static final int DOWNLOAD_SUCCESS = 1;
	public static final int NEED_UPDATE = 2;
	public static Context context;
	public static String fileSize;
	public static String downloadURL;
	public static String apkTips;
	public static Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int what = msg.what;
			switch (what) {
			case DOWNLOAD_SUCCESS: {
				Bundle b = msg.getData();
				String filePath = b.getString("filePath");
				installApk(filePath);
				break;
			}
			case NEED_UPDATE: {
				showConfirmDialog();
				break;
			}
			}
		}
	};
	
	private String currentId;
	private String currentType;

	public AppUpdateManager(Context context, String id, String type) {
		AppUpdateManager.context = context;
		this.currentId = id;
		this.currentType = type;
	}

	public void work() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				checkNeedUpdate();
			}
		}).start();
	}

	public boolean checkNeedUpdate() {
		
		 Map<String, String> params = new HashMap<String, String>();
		 String str1 = DEncodingUtils.encoding(this.currentId, "utf-8");
		 String str2 = DEncodingUtils.encoding(this.currentType, "utf-8");
		 params.put("Id", str1);
		 params.put("Type", str2);
		
		DYHttpUtils.getInstance().encoding("UTF-8").post().url(Constants.Dianyou_url.URL_GET_VERSION).paramJSONType().params(params).build().execute(new DYStringCallback()
	    {
	      public void onError(Call call, Exception ex, int id)
	      {
	        Log.i("INFO", "the check apk update append a exception that is \"" + ex.toString() + "\"");
	      }

	      public void onResponse(String data, int id)
	      {
	        String str = DEncodingUtils.decoding(data, "utf-8");
	        Log.i("chaochao", "getVersionURL:http://azzs.mtkgame.com/User/GetVersion.ashx");
	        Log.i("chaochao", "result:" + str);
	        if (str != null)
	          try
	          {
	            JSONObject localJSONObject1 = new JSONObject(str);
	            if (localJSONObject1.optInt("Code", -1) == 0)
	            {
	              JSONObject jsonObj = localJSONObject1.optJSONObject("VersionInfo");
	              if (str != null)
	              {
	                int serverVersion = Integer.valueOf(jsonObj.optString("apk_version"));
	                fileSize = jsonObj.optString("apk_size");
	                downloadURL = jsonObj.optString("apk_download_url");
	                apkTips = jsonObj.optString("apk_version_tips");
	                fileSize = String.valueOf(new BigDecimal(Float.valueOf(AppUpdateManager.fileSize) / 1024.0F).setScale(2, 4).floatValue());
	                int localVersion = XYZApplication.getCurrentVersioncode(AppUpdateManager.context);
	                Log.i("chaochao", "apk_version: " + serverVersion);
	                Log.i("chaochao", "current_version: " + localVersion);
	                if (serverVersion > localVersion)
	                {
	                  AppUpdateManager.handler.sendEmptyMessage(NEED_UPDATE);
	                  Log.i("INFO", "需要升级");
	                  Log.i("chaochao", "需要升级");
	                }
	                else
	                {
	                  Log.i("INFO", "不需要升级");
	                  Log.i("chaochao", "不需要升级");
	                }
	              }
	            }
	          }
	          catch (Exception localException)
	          {
	            localException.printStackTrace();
	            Log.i("chaochao", "checkNeedUpdate ex-----: " + localException.toString());
	          }
	      }
	    });
		return false;
	}

	public static void showConfirmDialog() {

		Dialog dialog = new AlertDialog.Builder(AppUpdateManager.context)
				.setIcon(android.R.drawable.alert_dark_frame)
				.setTitle("版本升级，共" + fileSize + "MB")
				.setMessage("升级新版本后才能正常使用，确定升级吗?\n版本说明：\n" + apkTips)
				.setPositiveButton("确定", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						new Handler(Looper.getMainLooper())
								.post(new Runnable() {

									@Override
									public void run() {
										downloadAPK(downloadURL);
									}
								});
					}

				}).setNegativeButton("取消", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}

				}).create();
		dialog.setCancelable(false);
		dialog.show();
	}

	public static void downloadAPK(String downloadURL) {
		try {
			new AppUpdateAsyncTask(AppUpdateManager.context, downloadURL)
					.execute();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 安装APK文件
	 */
	private static void installApk(String path) {
		Runtime runtime = Runtime.getRuntime();
		try {
			runtime.exec("chmod 777 " + path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		File apkfile = new File(path);
		if (!apkfile.exists()) {
			return;
		}
		// 通过Intent安装APK文件
		Intent i = new Intent(Intent.ACTION_VIEW);
		i.setDataAndType(Uri.parse("file://" + apkfile.toString()),
				"application/vnd.android.package-archive");
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		if (context != null) {
			context.startActivity(i);
		}
	}

}
