package dyhelper.com.bean;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.content.Context;
import android.util.Log;
import cn.dianyou.nets.DYHttpUtils;
import cn.dianyou.nets.DYStringCallback;
import cn.dianyou.utils.DEncodingUtils;
import cn.dianyou.utils.SharedPreferencesUtils;
import dyhelper.com.ui.XYZApplication;
import dyhelper.com.util.Constants;
import dyhelper.com.util.HttpUtils;
import okhttp3.Call;

public class PageActLogBag {
	
	private static SharedPreferencesUtils spu;
	private static final int MAX_COUNT = 10;
	
	private static final String SP_COUNT_FLAG = "current_count";
	private static final String SP_DATA_FLAG = "current_allbag";
	
	private Context context;
	
	private PageActLogBag(Context context) {
		this.context = context;
		spu = SharedPreferencesUtils.newInstance(context, "PageActLog");
	}
	
	public static PageActLogBag create(Context context) {
		return new PageActLogBag(context);
	}
	
	//是否存满了
	public void addBag(PageActLogInfo bag) {
		int allCount = spu.getInt(SP_COUNT_FLAG, 0);
		if(allCount < MAX_COUNT) {
			//继续存放
			putBag(bag, allCount);
		} else {
			sendBag();
			clearBag();
		}
	}
	

	private void clearBag() {
		spu.saveString(SP_DATA_FLAG, "").saveInt(SP_COUNT_FLAG, 0).commit();
	}

	private void sendBag() {
		String data = spu.getString(SP_DATA_FLAG, "");
		
		if(!"".equals(data)) {
			data = "[" + data + "]";
			sendToServer(data);
		}
	}

	private void sendToServer(String data) {
		String Id = XYZApplication.obtainStringInfo(context, "dianyou_login_current_id");
        String Type = XYZApplication.obtainStringInfo(context, "dianyou_login_current_id_type");
        if(Id == null) {
        	Id = XYZApplication.obtainStringInfo(context, "dianyou_unique_id");
        	Type = XYZApplication.obtainStringInfo(context, "dianyou_unique_id_type");
        }
        
        String dataParams = "{\"Id\":\"" + Id + "\", \"Type\":" + Type + ", \"OL\":" + data + "}";
        
        Log.i("chaochao", "sendToServer->dataParams: " + dataParams);
        		
        dataParams = DEncodingUtils.encoding(dataParams, "UTF-8");
        
        HttpUtils.sendPostDataUTF8_sync(Constants.Dianyou_url.URL_OPERATION_LOG, dataParams, new DYStringCallback() {

			@Override
			public void onError(Call call, Exception e, int id) {
				Log.i("chaochao", "sendToServer->error: " + e.toString());
				
			}

			@Override
			public void onResponse(String response, int id) {
				Log.i("chaochao", "sendToServer->onResponse: " + response);
				
				String result = DEncodingUtils.decoding(response, "UTF-8");
				Log.i("chaochao", "sendToServer->result: " + result);
				
				try {
					JSONObject jsonObj = new JSONObject(result);
					int code = jsonObj.getInt("Code");
					String message = jsonObj.getString("Message");
					Log.i("chaochao", "---------result--------");
					Log.i("chaochao", "sendToServer->code: " + code);
					Log.i("chaochao", "sendToServer->message: " + message);
				} catch(Exception e) {
					Log.i("chaochao", "sendToServer->ex: " + e.toString());
				}
			}
        	
        	
        });
        
		
	}

	private void putBag(PageActLogInfo bag, int count) {
		if(bag != null) {
			String data = spu.getString(SP_DATA_FLAG, "");
			data = "".equals(data) ? bag.toString() : data + ", " + bag.toString();
			Log.i("chaochao", "sendToServer->putBag: " + (count+1));
			Log.i("chaochao", "sendToServer->putBag: " + data);
			spu.saveString(SP_DATA_FLAG, data).saveInt(SP_COUNT_FLAG, count+1).commit();
		}
	}
}
