package dyhelper.com.util;

import java.io.IOException;
import java.util.Map;

import android.util.Log;
import cn.dianyou.nets.DYCallback;
import cn.dianyou.nets.DYHttpUtils;
import cn.dianyou.nets.DYStringCallback;
import cn.dianyou.utils.DEncodingUtils;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtils {
	
	static {
		DYHttpUtils.initClient(new OkHttpClient());
	}
	
	public static String sendPostUTF8(String url, Map<String, String> params) {
		String result = null;
		try {
			Response response = DYHttpUtils.getInstance().post().url(url).paramJSONType().params(params).build().execute();
			
			if(response.isSuccessful()) {
				result = DEncodingUtils.decoding(response.body().string(), "utf-8");
			} else {
				Log.i("chaochao", "HttpUtils-->sendPostUTF8: request is not Successful!");
			}
		} catch (Exception e) {
			Log.i("chaochao", "HttpUtils--sendPostUTF8->ex:" + e.toString());
		}
		return result;
		
	}
	
	public static String sendPostDataUTF8(String url, String data) {
		String result = null;
		try {
			Log.i("chaochao", "HttpUtils-->sendPostData->url:" + url);
			Log.i("chaochao", "HttpUtils-->sendPostData->data:" + data);
			OkHttpClient okHttpClient = DYHttpUtils.getInstance().getOkHttpClient();
			RequestBody formBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), data);
			Request request = new Request.Builder().url(url).post(formBody).build();
			Response response = okHttpClient.newCall(request).execute();
			if(response.isSuccessful()) {
				result = response.body().string();
			} else {
				Log.i("chaochao", "HttpUtils-->sendPostData->data:" + data);
			}
		} catch(Exception e) {
			Log.i("chaochao", "HttpUtils-->sendPostData->ex:" + e.toString());
		}
		Log.i("chaochao", "HttpUtils-->sendPostData->result: " + result);
		return result;
	}
	
	
	public static void sendPostDataUTF8_sync(String url, String data, DYStringCallback callback) {
		try {
			Log.i("chaochao", "HttpUtils-->sendPostData->url:" + url);
			Log.i("chaochao", "HttpUtils-->sendPostData->data:" + data);
			final DYStringCallback finalcallback = callback;
			OkHttpClient okHttpClient = DYHttpUtils.getInstance().getOkHttpClient();
			RequestBody formBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), data);
			Request request = new Request.Builder().url(url).post(formBody).build();
			okHttpClient.newCall(request).enqueue(new Callback() {
				
				@Override
				public void onResponse(Call arg0, Response response) throws IOException {
					if(finalcallback != null) {
						if(response.isSuccessful()) {
							finalcallback.onResponse(response.body().string(), 0);
						} else {
							finalcallback.onError(arg0, new Exception(response.body().string()), 0);
						}
						
					}
				}
				
				@Override
				public void onFailure(Call arg0, IOException ex) {
					if(finalcallback != null) {
						finalcallback.onError(arg0, ex, 0);
					}
					
				}
			});
		} catch(Exception e) {
			Log.i("chaochao", "HttpUtils-->sendPostData->ex:" + e.toString());
		}
	}
}
