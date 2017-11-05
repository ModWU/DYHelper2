/**
  * Generated by smali2java 1.0.0.558
  * Copyright (C) 2013 Hensence.com
  */

package dyhelper.com.weather;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.content.Context;
import android.widget.ImageView;
import java.util.List;
import android.widget.LinearLayout;
import android.widget.Button;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;
import xyz.monkeytong.hongbao.R;
import android.view.View;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.content.res.Resources;
import android.graphics.BitmapFactory;

public class ViewAdapter {
    private BaiduWeather bd;
    private Context context;
    private ImageView[] imageView;
    List<perWeather> list;
    private LinearLayout main;
    private Button refresh;
    private RefreshableView refreshableView;
    private TextView[] textView;
    private RotateAnimation rotateAnimation = new RotateAnimation(0.0f,
			+720.0f, Animation.RELATIVE_TO_SELF, 0.5f,
			Animation.RELATIVE_TO_SELF, 0.5f);
    private String city = "����";
    
    ViewAdapter(View view, Context context) {
    	imageView = new ImageView[4];
		textView = new TextView[11];
		refresh = (Button) view.findViewById(R.id.button);
		main = (LinearLayout) view.findViewById(R.id.main);
		imageView[0] = (ImageView) view.findViewById(R.id.imageView);
		imageView[1] = (ImageView) view.findViewById(R.id.imageView2);
		imageView[2] = (ImageView) view.findViewById(R.id.imageView3);
		imageView[3] = (ImageView) view.findViewById(R.id.imageView4);
		textView[0] = (TextView) view.findViewById(R.id.textView);
		textView[1] = (TextView) view.findViewById(R.id.textView2);
		textView[2] = (TextView) view.findViewById(R.id.textView3);
		textView[3] = (TextView) view.findViewById(R.id.textView4);
		textView[4] = (TextView) view.findViewById(R.id.textView5);
		textView[5] = (TextView) view.findViewById(R.id.textView6);
		textView[6] = (TextView) view.findViewById(R.id.textView7);
		textView[7] = (TextView) view.findViewById(R.id.textView8);
		textView[8] = (TextView) view.findViewById(R.id.textView9);
		textView[9] = (TextView) view.findViewById(R.id.textView10);
		textView[10] = (TextView) view.findViewById(R.id.textView19);
		refreshableView = (RefreshableView) view
				.findViewById(R.id.refreshable_view);

		this.context = context;
		bd = new BaiduWeather(city, context);
		// refresh.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// updateWeather();
		// }
		// });
		refreshableView.setOnRefreshListener(
				new RefreshableView.PullToRefreshListener() {
					@Override
					public void onRefresh() {
						try {
							updateWeather();
							Thread.sleep(2000);

						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						refreshableView.finishRefreshing();
					}
				}, 1);
    }
    
    public void setCity(String city) {
    	this.city = city;
        bd.setCity(city);
    }
    
    public String getCity() {
        return city;
    }
    
    private void updateUI() {
    	if (list.size() > 0) {
			for (int i = 0; i <= 2; i++) {
				textView[i].setText(list.get(i + 1).date);
			}
			for (int i = 0; i <= 2; i++) {
				textView[i + 3].setText(list.get(i + 1).tem
						.replace(" ~", "�� /").replace("��", "��"));
			}
			String tmpnow;
			String tmp = "���� "
					+ list.get(0).date.replace("ʵʱ", "��ǰ").replace("��", "��")
							.replace("(", "").replace(")", "");
			tmpnow = tmp.substring(tmp.indexOf("��ǰ") + 3, tmp.indexOf("��"));
			tmp = tmp.substring(0, tmp.indexOf("��ǰ") - 1);

			textView[6].setText(tmp);
			textView[7].setText(list.get(0).weather);
			textView[8].setText(list.get(0).wind);
			textView[9].setText(" " + bd.getCity() + "  "
					+ list.get(0).tem.replace(" ~", "�� /").replace("��", "��"));
			textView[10].setText(tmpnow);
			
			for (int i = 0; i < imageView.length; i++) {
				 imageView[i].setImageResource(R.drawable.weather_w100);
		           if(i == 0) {
		               Bitmap bmp = convertToWhite(BitmapFactory.decodeResource(context.getResources(), getWeatherImg(list.get(i).weather)));
		               imageView[i].setImageBitmap(bmp);
		           } else {
		        	   imageView[i]
								.setImageResource(getWeatherImg(list.get(i).weather));
		           }
			}
        }
    }
    
    public static Bitmap convertToWhite(Bitmap bmp) {
    	Bitmap localBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), bmp.getConfig());
        Canvas localCanvas = new Canvas(localBitmap);
        Paint localPaint = new Paint();
        ColorMatrix localColorMatrix = new ColorMatrix();
        float[] arrayOfFloat = new float[20];
        arrayOfFloat[0] = 1.0F;
        arrayOfFloat[1] = 1.0F;
        arrayOfFloat[2] = 1.0F;
        arrayOfFloat[3] = 0.0F;
        arrayOfFloat[4] = 0.0F;
        arrayOfFloat[5] = 1.0F;
        arrayOfFloat[6] = 1.0F;
        arrayOfFloat[7] = 1.0F;
        arrayOfFloat[8] = 0.0F;
        arrayOfFloat[9] = 0.0F;
        arrayOfFloat[10] = 1.0F;
        arrayOfFloat[11] = 1.0F;
        arrayOfFloat[12] = 1.0F;
        arrayOfFloat[13] = 0.0F;
        arrayOfFloat[14] = 0.0F;
        arrayOfFloat[15] = 0.0F;
        arrayOfFloat[16] = 0.0F;
        arrayOfFloat[17] = 0.0F;
        arrayOfFloat[18] = 1.0F;
        arrayOfFloat[19] = 0.0F;
        localColorMatrix.set(arrayOfFloat);
        localPaint.setColorFilter(new ColorMatrixColorFilter(localColorMatrix));
        localCanvas.drawBitmap(bmp, 0.0F, 0.0F, localPaint);
    	return localBitmap;
    }
    
    class sMyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// TODOAuto-generatedmethodstub
			super.handleMessage(msg);
			if (msg.what == 0x1)// ˢ���������� ���½���
			{
				// rotateAnimation.setDuration(1200);
				// refresh.startAnimation(rotateAnimation);
				updateUI();
			} else if (msg.what == 0x2)// ��ȡ�������� ���½���
			{

				updateUI();
			}
		}
	}
    private ViewAdapter.sMyHandler mHandler = new sMyHandler();
    
    public void updateWeather() {
		new Thread() {
			public void run() {
				Message msg;
				// Ԥ�ȶ�ȡ��������

				// ��ȡ��������
				list = bd.getWeather();// ��ȡ��������
				
				Log.i("event123", "list size:" + list.size());
				
				if (list.size() == 0)// ����������ݼ���ʧ��
				{
					// bd.readXMLfromLocal();
					// list = bd.getWeather();//���ñ��������ٴ���ȡ��Ϣ
					return;
				} else {
					// bd.saveXMLtoLocal();//ˢ�±�������Ϊ������������
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				// UPDATE��һ���Լ��������������������ϢID
				msg = mHandler.obtainMessage(0x1);
				mHandler.sendMessage(msg);
			}
		}.start();
	}

	private int getWeatherImg(String weather) {
		int img = 0;
		if (weather.contains("ת")) {
			weather = weather.substring(0, weather.indexOf("ת"));
		}
		if (weather.contains("��")) {
			img = R.drawable.weather5;
		} else if (weather.contains("����")) {
			img = R.drawable.weather3;
		} else if (weather.contains("��")) {
			img = R.drawable.weather2;
		} else if (weather.contains("��")) {
			img = R.drawable.weather21;
		} else if (weather.contains("����")) {
			img = R.drawable.weather0;
		} else if (weather.contains("С��")) {
			img = R.drawable.weather12;
		} else if (weather.contains("����")) {
			img = R.drawable.weather12;
		} else if (weather.contains("����")) {
			img = R.drawable.weather23;
		} else if (weather.contains("����")) {
			img = R.drawable.weather23;
		} else if (weather.contains("���ѩ")) {
			img = R.drawable.weather18;
		} else if (weather.contains("����")) {
			img = R.drawable.weather18;
		} else if (weather.contains("Сѩ")) {
			img = R.drawable.weather19;
		} else if (weather.contains("��ѩ")) {
			img = R.drawable.weather19;
		} else if (weather.contains("��ѩ")) {
			img = R.drawable.weather33;
		} else if (weather.contains("��ѩ")) {
			img = R.drawable.weather33;
		} else if (weather.contains("����")) {
			img = R.drawable.weather33;
		} else if (weather.contains("��") || weather.contains("��")) {
			img = R.drawable.weather11;
		} else if (weather.contains("ɳ����") || weather.contains("����")
				|| weather.contains("��ɳ")) {
			img = R.drawable.weather28;
		} else {
			img = R.drawable.weather5;
		}
		return img;
	}
}