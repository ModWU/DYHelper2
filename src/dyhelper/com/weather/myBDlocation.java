package dyhelper.com.weather;

import com.baidu.location.BDLocationListener;
import com.baidu.location.BDLocation;
import android.content.Context;
import com.baidu.location.GeofenceClient;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import android.util.Log;
import android.content.Intent;

import android.content.Context;
import android.content.Intent;
import com.baidu.location.*;

public class myBDlocation
{
	public LocationClient mLocationClient = null;
	public GeofenceClient mGeofenceClient;
	public MyLocationListenner myListener = new MyLocationListenner();
	public static String TAG = "myBDlocation";
	Context context;
	public myBDlocation(Context context)
	{
		this.context=context;
		
		mLocationClient = new LocationClient(context);

		/**
		 * ��Ŀ��key
		 */

		mLocationClient.setAK("mrkFduw900voCwAXWP2G5NOj");
		mLocationClient.registerLocationListener(myListener);
		mGeofenceClient = new GeofenceClient(context);
	}
	

	/**
	 * ֹͣ��λ
	 */
	public void stopLocationClient()
	{
		if (mLocationClient != null && mLocationClient.isStarted())
		{
			mLocationClient.stop();
		} 
	}

	/**
	 * ����λ
	 */
	public void requestLocationInfo()
	{
		setLocationOption();
		
		if (mLocationClient != null && !mLocationClient.isStarted())
		{
			mLocationClient.start();
		}

		if (mLocationClient != null && mLocationClient.isStarted())
		{
			mLocationClient.requestLocation();
		} 
	}
	
	/**
	 *  ������ز���
	 */
	private void setLocationOption()
	{
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true); // ��GPS
		option.setCoorType("bd09ll"); // ������������
		option.setServiceName("com.baidu.location.service_v2.9");//���ðٶȵ�ͼ��λ����
		option.setPoiExtraInfo(true);
		option.setAddrType("all");
		option.setPoiNumber(10);
		option.disableCache(true);
		mLocationClient.setLocOption(option);
	}

	/**
	 * �����������и���λ�õ�ʱ�򣬸�ʽ�����ַ������������Ļ��
	 */
	public class MyLocationListenner implements BDLocationListener
	{
		@Override
		public void onReceiveLocation(BDLocation location)
		{
			if (location == null)
			{
				sendBroadCast("��λʧ��!");
				return;
			}
			sendBroadCast(location.getCity());
		}

		public void onReceivePoi(BDLocation poiLocation)
		{
			if (poiLocation == null)
			{
				sendBroadCast("��λʧ��!");
				return;
			}
			sendBroadCast(poiLocation.getCity());

		}
		
	}
	
	/**
	 * �õ����͹㲥
	 * @param address
	 */
	public void sendBroadCast(String address)
	{
		stopLocationClient();
		
		Intent intent = new Intent(addcity.LOCATION_BCR);
		intent.putExtra("address", address);
		context.sendBroadcast(intent);
	}
}
