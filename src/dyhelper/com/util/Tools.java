package dyhelper.com.util;

import java.lang.reflect.Field;

import com.tencent.smtt.sdk.CookieManager;
import com.tencent.smtt.sdk.CookieSyncManager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;

public class Tools {
	/**
	 * 检查是否存在SD卡
	 */
	public static boolean hasSdcard() {
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static String getTimeStemp_10(){

		 long time = System.currentTimeMillis()/1000;//获取系统时间的10位的时间戳

		 String str = String.valueOf(time);

		 return str;

	}
	
	
	public static void updateCookies(Context context, String p2, String p3) {
        if(Build.VERSION.SDK_INT <= 0xa) {
            CookieSyncManager.createInstance(context.getApplicationContext());
        }
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setCookie(p2, p3);
        if(Build.VERSION.SDK_INT <= 10) {
            CookieSyncManager.getInstance().sync();
        }
    }
	
	public static Boolean isAvailableNetWork(Context context) {
        ConnectivityManager conn = (ConnectivityManager)context.getSystemService("connectivity");
        if(conn == null) {
            return Boolean.valueOf(false);
        }
        NetworkInfo[] netinfo = conn.getAllNetworkInfo();
        boolean pingFlag = ping();
        if((netinfo != null) && (pingFlag)) {
            for(int i = 0; i < netinfo.length; ++i) {
            	 if((netinfo[i].getState() == NetworkInfo.State.CONNECTED) && (netinfo[i].isAvailable())) {
                     return Boolean.valueOf(true);
                 }
            }
           
        }
        return Boolean.valueOf(false);
    }

	private static boolean ping() {
		return true;
	}
	
	@TargetApi(Build.VERSION_CODES.DONUT)
	public static void releaseAllWebViewCallback() {
		int currentApiLevel = 1;
		try {
			currentApiLevel = android.os.Build.VERSION.SDK_INT;
		} catch(Exception e) {
		} catch(Error e) {}
		
		
		if (currentApiLevel < 16) {
			try {
				Field field = WebView.class.getDeclaredField("mWebViewCore");
				field = field.getType().getDeclaredField("mBrowserFrame");
				field = field.getType().getDeclaredField("sConfigCallback");
				field.setAccessible(true);
				field.set(null, null);
			} catch (NoSuchFieldException e) {
			} catch (IllegalAccessException e) {
			}
		} else {
			try {
				Field sConfigCallback = Class.forName("android.webkit.BrowserFrame")
						.getDeclaredField("sConfigCallback");
				if (sConfigCallback != null) {
					sConfigCallback.setAccessible(true);
					sConfigCallback.set(null, null);
				}
			} catch (NoSuchFieldException e) {
			} catch (ClassNotFoundException e) {
			} catch (IllegalAccessException e) {
			}
		}
	}
	
	public static void setTranslucentStatus(Activity activity, boolean p2) {
        Window win = activity.getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        int bits = 0;
        if(p2) {
            winParams.flags = (winParams.flags | 0x0);
        } else {
            winParams.flags = winParams.flags;
        }
        win.setAttributes(winParams);
    }
	
	 @TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public static void setWindowStatusBarColor(Activity activity, int p2) {
	        try {
	            if(Build.VERSION.SDK_INT >= 0x15) {
	                Window window = activity.getWindow();
	                window.addFlags(0);
	                window.setStatusBarColor(activity.getResources().getColor(p2));
	                return;
	            }
	        } catch(Exception e) {
	            e.printStackTrace();
	        }
	    }
	
	
	public static int getStatusHeight(Context context) {
		 int statusHeight = 0;  
		    try {  
		        Class<?> clazz = Class.forName("com.android.internal.R$dimen");  
		        Object object = clazz.newInstance();  
		        int height = Integer.parseInt(clazz.getField("status_bar_height")  
		                .get(object).toString());  
		        statusHeight = context.getResources().getDimensionPixelSize(height);  
		    } catch (Exception e) {  
		        e.printStackTrace();  
		    }  
		    return statusHeight;
    }
}
