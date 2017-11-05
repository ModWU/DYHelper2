/*
 * Copyright (C) 2012 Guangzhou CooguoSoft Co.,Ltd.
 * cn.douwan.sdk.entityDeviceProperties.java
 */
package dyhelper.com.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;
import dyhelper.com.util.ImsiUtil.IMSInfo;

public class DeviceUtils {

	/**
	 * 1.0.0 鎵嬫�?绯荤粺鐗堟湰
	 */
	public String deviceSoftwareVersion;

	/**
	 * 璁惧纭欢淇℃�?
	 */
	public String product;

	/**
	 * 1.0.0 Sim鍗�?�簭鍒楀�?
	 */
	public String imsi_1 = "";
	public String imsi_2 = "";
	/**
	 * 1.0.0 鎵嬫�?搴忓垪鍙�?
	 */
	public String imei;

	/**
	 * 1.0.0 搴旂敤鐗堟湰鍚嶇О
	 */
	public String appVersionName;

	/**
	 * 1.0.0 搴旂敤鐗堟湰鍙�
	 */
	public String appVersionCode;

	/**
	 * 1.0.0 搴旂敤鍚�?
	 */
	public String appName;

	/**
	 * 1.0.0 鎵嬫�?灞忓箷�?�嗙�?
	 */
	public int densityDpi;

	/**
	 * 1.0.1 鎵嬫�?灞忓箷�?�藉�?
	 */
	public int displayScreenWidth;

	/**
	 * 1.0.0 鎵嬫�?灞忓箷楂樺害
	 */
	public int displayScreenHeight;

	/**
	 * 1.0.0 缃戠粶绫诲�??
	 */
	public int networkInfo;

	/**
	 * 1.0.0 杩愯�?鍟�
	 */
	public String operator;

	/**
	 * 1.0.0 娓犻亾鍙�?
	 */
	public String letuChannelId;
	/**
	 * 1.0.0 娓告垙鍖呭悕
	 */
	public String packageName;
	/**
	 * 1.0.0 sdk鍗忚鐗堟湰鍙�
	 */
	public String sdkProtocolVer = "1.0.4";

	/**
	 * 1.0.0 phone Number
	 */
	public String phoneNumber = "";

	/**
	 * 鎵嬫�?iccid
	 * */
	public String iccid;

	public String mcc;

	// 缁忕含搴�?
	private double latitude = 0.0;
	private double longitude = 0.0;

	private Bundle metaData = null;

	public String packageId = "";

	// 妯珫灞�?
	public int orientation = 0;

	public String ip = "";
	public Context mContext;

	@SuppressWarnings("deprecation")
	public DeviceUtils(Context ctx) {
		mContext = ctx.getApplicationContext();
		product = android.os.Build.MODEL + " " + android.os.Build.PRODUCT + " "
				+ Build.BRAND;
		deviceSoftwareVersion = android.os.Build.VERSION.SDK;

		TelephonyManager tm = (TelephonyManager) ctx
				.getSystemService(Context.TELEPHONY_SERVICE);
		imei = tm.getDeviceId();

		iccid = tm.getSimSerialNumber();
		// imsi = Utils.getIMSI(ctx);
		getIMSI();
		if (tm != null) {
			phoneNumber = tm.getLine1Number();
		}

		// --鑾峰彇鎵嬫満鍒嗚鲸鐜�?
		WindowManager wm = (WindowManager) ctx
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(metrics);
		densityDpi = metrics.densityDpi;
		displayScreenWidth = metrics.widthPixels;
		displayScreenHeight = metrics.heightPixels;

		PackageManager pm = ctx.getPackageManager();
		packageName = ctx.getPackageName();
		PackageInfo info;
		try {
			info = pm.getPackageInfo(ctx.getPackageName(), 0);
			appVersionName = info.versionName;
			appVersionCode = "" + info.versionCode;
			appName = info.applicationInfo.loadLabel(pm).toString();
		} catch (NameNotFoundException e1) {
		}

		// networkInfo = NetworkUtils.getNetworkTypeString(ctx);
		// if (networkInfo == null) {
		// networkInfo = "unknown";
		// }

		// getNetWorkInfo(ctx);
		// mcc = tm.getNetworkCountryIso();
		// operator = tm.getNetworkOperator();
		// getLocation(ctx);
		// getMainfest(ctx);
		// getOrientation(ctx);
		// getLocalIpAddress();
	}

	private void getNetWorkInfo(Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = cm.getActiveNetworkInfo();
		if (info == null) {
			// "NULL"
			networkInfo = 0;
		} else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
			// "WIFI"
			networkInfo = 1;
		} else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
			int subType = info.getSubtype();
			if (subType == TelephonyManager.NETWORK_TYPE_CDMA
					|| subType == TelephonyManager.NETWORK_TYPE_GPRS
					|| subType == TelephonyManager.NETWORK_TYPE_EDGE) {
				// "2G"
				networkInfo = 2;
			} else if (subType == TelephonyManager.NETWORK_TYPE_UMTS
					|| subType == TelephonyManager.NETWORK_TYPE_HSDPA
					|| subType == TelephonyManager.NETWORK_TYPE_EVDO_A
					|| subType == TelephonyManager.NETWORK_TYPE_EVDO_0
					|| subType == 12)// TelephonyManager.NETWORK_TYPE_EVDO_B
			{
				// "3G"
				networkInfo = 3;
			} else if (subType == 13)// TelephonyManager.NETWORK_TYPE_LTE
			{// LTE鏄�3g鍒�4g鐨勮繃娓★紝鏄�3.9G鐨勫叏鐞冩爣鍑�
				// "4G"
				networkInfo = 4;
			}
		}
	}

	private void getMainfest(Context ctx) {
		PackageManager pm = ctx.getPackageManager();
		try {
			ApplicationInfo appinfo = pm.getApplicationInfo(
					ctx.getPackageName(), PackageManager.GET_META_DATA);
			metaData = appinfo.metaData;

			// 娉ㄦ剰锛氳繖閲宮eteData.getInt
			// Manifest.xml閲岄潰閰嶇疆鐨刴eteData鐨勬爣绛剧殑value=鈥�1001鈥濓紝浣嗘槸绯荤粺浼氳嚜鍔ㄥ垽�?��1001鏄竴涓猧nt绫诲�?
			packageId = String.valueOf(metaData
					.getString("COOL_PAY_PACKAGE_ID"));
			letuChannelId = String.valueOf(metaData
					.getString("lltt_cpchannelid"));
		} catch (Exception e) {
		}
	}

	private void getOrientation(Context ctx) {
		Configuration configuration = ctx.getResources().getConfiguration();
		if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			orientation = 0;// 妯�?
		} else {
			orientation = 1;// 绔栧�?
		}
	}

	private void getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()
							&& inetAddress instanceof Inet4Address) {
						ip = inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("WifiPreference IpAddress", ex.toString());
		}
	}

	private void getLocation(Context ctx) {
		LocationManager locationManager = (LocationManager) ctx
				.getSystemService(Context.LOCATION_SERVICE);
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Location location = locationManager
					.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if (location != null) {
				latitude = location.getLatitude();
				longitude = location.getLongitude();
			}
		} else {
			Location location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			if (location != null) {
				latitude = location.getLatitude(); // 缁忓�?
				longitude = location.getLongitude(); // 绾�?
			}
		}
	}

	/**
	 * 
	 * @param mContext
	 * @return
	 */
	private void getIMSI() {
		if (isFirstSimValid()) {
			imsi_1 = getIMSI(0);
		}
		if (isSecondSimValid()) {
			imsi_2 = getIMSI(1);
		}
	}

	public String getIMSI(int simCard) {
		String imsi = "";
		try {
			if (simCard == 1 && isGaoTong()) {
				imsi = getGTImsi2();
			} else {
				imsi = (String) invokeInTelephonyManager(simCard,
						"getSubscriberId", new Class<?>[0], new Object[0]);
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (imsi == null || imsi.equals("")) {
			try {
				imsi = getIMSIOther(simCard);
			} catch (Throwable e) {
				e.printStackTrace();
			}
		}
		if (imsi == null) {
			IMSInfo info = new ImsiUtil(mContext).getIMSInfo();
			if (info != null) {
				if (simCard == 0)
					imsi = info.imsi_1;
				else
					imsi = info.imsi_2;
			}

		}
		if (imsi == null)
			imsi = "";
		return imsi;
	}

	private static class LocalTelephonyManager {
		private Object telephonyManager;

		public LocalTelephonyManager(int simCardIndex) {
			String serviceType = null;

			serviceType = simCardIndex == 1 ? "iphonesubinfo2"
					: "iphonesubinfo";
			try {
				Method svcMgr_getService = Class.forName(
						"android.os.ServiceManager").getDeclaredMethod(
						"getService", new Class[] { String.class });

				svcMgr_getService.setAccessible(true);
				Object service = svcMgr_getService.invoke(null,
						new Object[] { serviceType });

				if ((service == null) && (simCardIndex == 1))
					service = svcMgr_getService.invoke(null,
							new Object[] { "iphonesubinfo1" });

				if (service == null) {
					return;
				}

				Method asInterface = Class.forName(
						"com.android.internal.telephony.IPhoneSubInfo$Stub")
						.getDeclaredMethod("asInterface",
								new Class[] { IBinder.class });

				asInterface.setAccessible(true);
				telephonyManager = asInterface.invoke(null,
						new Object[] { service });
			} catch (Exception e) {
				return;
			}
		}

		public Object invoke(String name, Class<?>[] parameters, Object... args) {
			if (telephonyManager == null)
				return null;
			Method methodToInvoke;
			try {
				methodToInvoke = telephonyManager.getClass().getMethod(name,
						parameters == null ? new Class[0] : parameters);
				return methodToInvoke.invoke(telephonyManager, args);
			} catch (Exception e) {
				// e.printStackTrace();
				return null;
			}

		}
	}

	private static LocalTelephonyManager[] localTelephonyManager = new LocalTelephonyManager[] {
			new LocalTelephonyManager(0), new LocalTelephonyManager(1) };

	public static Object invokeInTelephonyManager(int simCardIndex,
			String name, Class<?>[] parameters, Object... args) {
		Object ret = localTelephonyManager[simCardIndex].invoke(name,
				parameters, args);
		return ret;
	}

	private String getIMSIOther(int simCard) {
		// 鑾峰彇鍗�?1涓插�?
		String imsi = "";
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		do {
			imsi = tm.getSubscriberId();
			if (imsi != null && imsi.length() > 0) {
				break;
			}

			imsi = getImsiByReflect();
			if (imsi != null && imsi.length() > 0) {
				break;
			}

			imsi = getMtkDoubleSim(mContext, simCard);
			if (imsi != null && imsi.length() > 0) {
				break;
			}
		} while (false);

		if (imsi == null)
			imsi = "";

		return imsi;
	}

	private static String getImsiByReflect() {
		String imsi = "";
		try {
			Class<?> clazz = Class
					.forName("android.telephony.MSimTelephonyManager");
			Class<?> classes[] = null;
			Method method = clazz.getDeclaredMethod("getDefault", classes);
			Object objs[] = null;
			Object defalutInstance = method.invoke(clazz, objs);
			Method isMulMehod = clazz.getDeclaredMethod("isMultiSimEnabled",
					classes);
			boolean isMultiEnabled = ((Boolean) isMulMehod.invoke(
					defalutInstance, objs)).booleanValue();
			classes = new Class[1];
			classes[0] = Integer.TYPE;
			if (isMultiEnabled) {
				method = clazz.getDeclaredMethod("getSubscriberId", classes);
				objs = new Object[1];
				objs[0] = Integer.valueOf(0);
				imsi = (String) method.invoke(defalutInstance, objs);
				if (imsi == null || imsi.length() == 0) {
					objs[0] = Integer.valueOf(1);
					imsi = (String) method.invoke(defalutInstance, objs);
				}
			}
		} catch (Exception e) {
		}
		return imsi;
	}

	// mtk 鍙屽崱鑾峰彇
	public String getMtkDoubleSim(Context mContext, int simCard) {
		String imsi1 = null;
		String imsi2 = null;

		try {
			TelephonyManager tm = (TelephonyManager) mContext
					.getSystemService(Context.TELEPHONY_SERVICE);
			Class<?> c = Class.forName("com.android.internal.telephony.Phone");
			Field fields1 = c.getField("GEMINI_SIM_1");
			Field fields2 = c.getField("GEMINI_SIM_2");
			fields1.setAccessible(true);
			fields2.setAccessible(true);

			int simNo1 = (Integer) fields1.get(null);
			int simNo2 = (Integer) fields2.get(null);

			Method m = TelephonyManager.class.getDeclaredMethod(
					"getSubscriberIdGemini", int.class);
			imsi1 = (String) m.invoke(tm, simNo1);
			imsi2 = (String) m.invoke(tm, simNo2);
		} catch (Exception e) {
			return "";
		}

		if (simCard == 0) {
			if (imsi1 != null && imsi1.length() != 0) {
				return imsi1;
			} else {
				if (imsi2 != null && imsi2.length() > 0) {
					return imsi2;
				}
			}
		} else {
			if (imsi2 != null && imsi2.length() > 0) {
				return imsi2;
			} else {
				if (imsi1 != null && imsi1.length() != 0) {
					return imsi1;
				}
			}
		}
		return "";
	}

	public boolean isDualMode() {
		boolean result = false;
		try {
			Method method = Class.forName("android.os.ServiceManager")
					.getDeclaredMethod("getService",
							new Class[] { String.class });
			method.setAccessible(true);
			if (method.invoke(null, new Object[] { "phone" }) != null
					&& method.invoke(null, new Object[] { "phone2" }) != null) {
				result = true;
			} else if (method.invoke(null, new Object[] { "isms" }) != null
					&& method.invoke(null, new Object[] { "isms2" }) != null) {// 鍦�4.0涔嬪悗MTK鍙屽崱鏈哄櫒涓嶈兘鑾峰彇鍒皃hone2
				result = true;
			} else {
				result = isGaoTongDualMode();
			}
		} catch (Exception e) {
			result = false;
		}

		if (!result) {
			return false;
		}

		result = false;

		for (int i = 0; i < 2; i++) {
			if (getSimState(i) != TelephonyManager.SIM_STATE_READY) {
				result = false;
			} else {
				result = true;
			}
		}
		return result;
	}

	// 鍒ゆ柇楂�?�氭槸鍚︿负鍙屽崱鐗堟湰
	private boolean isGaoTongDualMode() {
		try {

			Class<?> cx = Class
					.forName("android.telephony.MSimTelephonyManager");
			Object obj = mContext.getSystemService("phone_msim");
			Method md = cx.getMethod("getDeviceId", int.class);
			// Method ms = cx.getMethod("getSubscriberId", int.class);
			String imei1 = (String) md.invoke(obj, 0);
			String imei2 = (String) md.invoke(obj, 1);
			// String imsi1 = (String) ms.invoke(obj, 0);
			// String imsi2 = (String) ms.invoke(obj, 1);
			if (!TextUtils.isEmpty(imei1) && !TextUtils.isEmpty(imei2)) {
				return true;
			}

		} catch (Throwable e) {
			// e.printStackTrace();
		}
		return false;
	}

	private static int getSimState(String simState) {
		try {
			String prop;
			Method method = Class.forName("android.os.SystemProperties")
					.getDeclaredMethod("get", new Class[] { String.class });
			method.setAccessible(true);
			prop = (String) method.invoke(null, new Object[] { simState });
			if ("".equals(prop))
				return 5;
			if (prop != null)
				prop = prop.split(",")[0];
			if ("ABSENT".equals(prop))
				return 1;
			if ("PIN_REQUIRED".equals(prop))
				return 2;
			if ("PUK_REQUIRED".equals(prop))
				return 3;
			if ("NETWORK_LOCKED".equals(prop))
				return 4;
			return !"READY".equals(prop) ? 0 : 5;
		} catch (Exception e) {
			return 0;
		}
	}

	public boolean isGaoTong() {
		try {
			Class<?> cx = Class
					.forName("android.telephony.MSimTelephonyManager");
			Object obj = mContext.getSystemService("phone_msim");

			Method md = cx.getMethod("getDeviceId", int.class);
			Method ms = cx.getMethod("getSubscriberId", int.class);

			String str = (String) md.invoke(obj, 0);
			str = (String) md.invoke(obj, 1);
			str = (String) ms.invoke(obj, 0);
			str = (String) ms.invoke(obj, 1);
			return true;
		} catch (Throwable e) {
		}
		return false;
	}

	public String getGTImsi2() {
		try {

			Class<?> cx = Class
					.forName("android.telephony.MSimTelephonyManager");
			Object obj = mContext.getSystemService("phone_msim");
			Method ms = cx.getMethod("getSubscriberId", int.class);
			return (String) ms.invoke(obj, 1);
		} catch (Exception e) {

		}
		return "";
	}

	public int getSimState(int simId) {
		try {
			if (simId == 1 && isGaoTong()) {
				try {
					return getGTSimState(simId);
				} catch (Throwable e) {
				}
			}
			String str = simId == 0 ? getSystemProperty("gsm.sim.state")
					: getSystemProperty("gsm.sim.state.2");
			if (str != null)
				str = str.split(",")[0];

			if ("ABSENT".equals(str))
				return TelephonyManager.SIM_STATE_ABSENT;
			if ("PIN_REQUIRED".equals(str))
				return TelephonyManager.SIM_STATE_PIN_REQUIRED;
			if ("PUK_REQUIRED".equals(str))
				return TelephonyManager.SIM_STATE_PUK_REQUIRED;
			if ("NETWORK_LOCKED".equals(str))
				return TelephonyManager.SIM_STATE_NETWORK_LOCKED;
			if ("READY".equals(str)) {
				return TelephonyManager.SIM_STATE_READY;
			}

			if (simId == 0) {
				TelephonyManager tm = (TelephonyManager) mContext
						.getSystemService(Context.TELEPHONY_SERVICE);
				return tm.getSimState();
			}
			return TelephonyManager.SIM_STATE_UNKNOWN;
		} catch (Throwable e) {
			return TelephonyManager.SIM_STATE_UNKNOWN;
		}
	}

	private Method systemPropertiesGetter;

	private String getSystemProperty(String paramString) {
		try {
			if (systemPropertiesGetter == null) {
				systemPropertiesGetter = Class.forName(
						"android.os.SystemProperties").getDeclaredMethod("get",
						new Class[] { String.class });
				systemPropertiesGetter.setAccessible(true);
			}
			String str = (String) systemPropertiesGetter.invoke(null,
					new Object[] { paramString });
			return str;
		} catch (Exception e) {
		}
		return "";
	}

	public int getGTSimState(int index) {
		try {
			String str = getGTTelephonyProperty("gsm.sim.state", index, "");
			if ("ABSENT".equals(str))
				return TelephonyManager.SIM_STATE_ABSENT;
			if ("PIN_REQUIRED".equals(str))
				return TelephonyManager.SIM_STATE_PIN_REQUIRED;
			if ("PUK_REQUIRED".equals(str))
				return TelephonyManager.SIM_STATE_PUK_REQUIRED;
			if ("NETWORK_LOCKED".equals(str))
				return TelephonyManager.SIM_STATE_NETWORK_LOCKED;
			if ("READY".equals(str)) {
				return TelephonyManager.SIM_STATE_READY;
			}

		} catch (Throwable e) {
		}
		return TelephonyManager.SIM_STATE_UNKNOWN;
	}

	public String getGTTelephonyProperty(String name, int index, String defa)
			throws Throwable {
		Class<?> cx = Class.forName("android.telephony.MSimTelephonyManager");
		Object obj = mContext.getSystemService("phone_msim");
		Method md = cx.getMethod("getTelephonyProperty", String.class,
				int.class, String.class);
		;
		String str = (String) md.invoke(obj, name, index, defa);
		return str;
	}

	public int getFirstSimState() {
		return getSimState("gsm.sim.state");
	}

	public int getSecondSimState() {
		int state = getSimState("gsm.sim.state_2");
		if (state == 0)
			return getSimState("gsm.sim.state_1");
		else
			return state;
	}

	public boolean isFirstSimValid() {
		return getFirstSimState() == 5;
	}

	public boolean isSecondSimValid() {
		return getSecondSimState() == 5;
	}
	
	/**
	 * 香港
	 * @param str
	 * @return
	 */
	public static boolean isChinaPhoneLegal(String str) {  
		if(str == null) {
			return false;
		}
        String regExp = "^((13[0-9])|(15[^4])|(18[0,2,3,5-9])|(17[0-8])|(147))\\d{8}$";  
        Pattern p = Pattern.compile(regExp);  
        Matcher m = p.matcher(str);  
        return m.matches();  
    }  
  
    /** 
     * 香港手机号码8位数�?5|6|8|9�?�?+7位任意数 
     */  
    public static boolean isHKPhoneLegal(String str) {  
    	if(str == null) {
    		return false;
    	}
        String regExp = "^(5|6|8|9)\\d{7}$";  
        Pattern p = Pattern.compile(regExp);  
        Matcher m = p.matcher(str);  
        return m.matches();  
    }  
    
    public static boolean isMobile(String str) {
    	return isChinaPhoneLegal(str) || isHKPhoneLegal(str);
    }
}
