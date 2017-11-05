package dyhelper.com.filemanager.explorer;

import android.content.Context;
import android.telephony.TelephonyManager;
import xyz.monkeytong.hongbao.R;

import com.adutil.AppVo;


public class AppVoUtil {
	

	private static AppVo appVo = null;
	

	
    public static AppVo getAppVo(Context context)
    {
    	if(appVo == null)
    	{
    		appVo = new AppVo();
    		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    		appVo.setId(Integer.parseInt(context.getResources().getString(R.string.filemanager_app_id)));
    		appVo.setMac(tm.getDeviceId());
    		appVo.setAppName(context.getResources().getString(R.string.app_name));
    		appVo.setUrl(context.getResources().getString(R.string.filemanager_url));
    	}
    	return appVo;
    }
}
