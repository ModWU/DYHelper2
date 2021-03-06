/**
  * Generated by smali2java 1.0.0.558
  * Copyright (C) 2013 Hensence.com
  */

package dyhelper.com.weather;

import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.app.Activity;
import android.widget.Button;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.Window;

import java.util.ArrayList;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.content.IntentFilter;
import android.os.Bundle;
import dyhelper.com.ui.Main;
import xyz.monkeytong.hongbao.R;
import android.view.KeyEvent;

public class addcity extends Activity implements SlideCutListView.RemoveListener {
    private int activityCloseEnterAnimation;
    private int activityCloseExitAnimation;
    private ArrayAdapter<String> adapter;
    private Button back;
    private BroadcastReceiver broadcastReceiver;
    UIupdater data;
    private List<String> dataSourceList;
    private EditText edt;
    private Button locBtn;
    private Button sch;
    private SlideCutListView slideCutListView;
    private TextView tv;
    public static String TAG = "LocTestDemo";
    public static String LOCATION_BCR = "location_bcr";
    
    protected void onDestroy() {
        super.onDestroy();
        if(broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
        if(dataSourceList != null) {
            dataSourceList.clear();
        }
        if(data != null) {
            data.clear();
            data = null;
        }
        slideCutListView = null;
        adapter = null;
        System.gc();
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.weather_addcity);
        sch =(Button)findViewById(R.id.button2);
        edt =(EditText)findViewById(R.id.editText);
        locBtn = (Button) findViewById(R.id.button3);
        tv =(TextView)findViewById(R.id.textView12);
        back =(Button)findViewById(R.id.buttonback);
        init();
        int[] activityAniValues = Main.obtrainExitAnims(this);
        activityCloseEnterAnimation = activityAniValues[0];
        activityCloseExitAnimation = activityAniValues[1];
    }
    
    private void init() {
        initialize();
        initializeListeners();
        slideCutListView = (SlideCutListView)findViewById(R.id.slideCutListView);
        slideCutListView.setRemoveListener(this);
        slideCutListView.addUnenablePosition(0);
        data = new UIupdater(this);
        dataSourceList = data.getcityList();
        adapter = new ArrayAdapter(this, R.layout.weather_item, R.id.list_item, dataSourceList);
        slideCutListView.setAdapter(adapter);
        slideCutListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                //Toast.makeText(addcity.this, dataSourceList.get(position), Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    public void removeItem(SlideCutListView.RemoveDirection direction, int position) {
        adapter.remove((String)adapter.getItem(position));
        data.savecityList(dataSourceList);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if((keyCode == KeyEvent.KEYCODE_BACK) && (event.getRepeatCount() == 0)) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }
    
    class sMyHandler extends Handler {
        
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String city = msg.getData().getString("search_city");
            if(msg.what == 0x6) {
                tv.setText("\"" + city + "\"未找到");
                edt.setText("");
            }
            if(msg.what == 0x7) {
            	slideCutListView.setAdapter(adapter);
                tv.setText(city + " \u6210\u529f");
                edt.setText("");
            }
            
            
            isAdding = false;
        }
    }
    
    private sMyHandler mHandler = new sMyHandler();
    
    private void haveCity(final String city) {
        final BaiduWeather bd = new BaiduWeather(edt.getText().toString(), this);
        new Thread()
        {
            public void run()
            {
            	 Message msg = new Message();
            	 Bundle bundle_data = new Bundle();
            	 bundle_data.putString("search_city", city);
            	 msg.setData(bundle_data);
                 //读取网络数据
                 List<perWeather> list = bd.getWeather();//获取天气数据
                 
                 
                 if(list.size()==0)//如果网络数据加载失败
                 {
                	 msg.what = 0x6;
                 }
                 else
                 {
                	 
	               dataSourceList.add(city);
	               data.savecityList(dataSourceList);
	               msg.what = 0x7;
                 }

                 //UPDATE是一个自己定义的整数，代表了消息ID

                 mHandler.sendMessage(msg);
            }

    	}.start();
    }
    
    private boolean isExitCity(String city) {
    	if(city != null && dataSourceList != null) {
    		for(String perCity : dataSourceList) {
    			if(perCity.contains(city) || city.contains(perCity)) {
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }
    
    private void initialize() {
        registerBroadCastReceiver();
    }
    
    private boolean isAdding = false;
    
    private void initializeListeners() {
        back.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                finish();
            }
        });
        sch.setOnClickListener(new View.OnClickListener() {
            
            
            public void onClick(View v) {
            	
            	if(isAdding) {
            		return;
            	}
            	
            	isAdding = true;
            	
                if(TextUtils.isEmpty(edt.getText())) {
                    Toast.makeText(addcity.this, "请输入城市名", Toast.LENGTH_SHORT).show();
                    isAdding = false;
                    return;
                }
                String city = edt.getText().toString().trim();
                if(city.equals("")) {
                	Toast.makeText(addcity.this, "请输入城市名", Toast.LENGTH_SHORT).show();
                	isAdding = false;
                    return;
                }
                
                if(!city.endsWith("市")) city += "市";
                
                if(UIupdater.checkCity(addcity.this, city)) {
                    Toast.makeText(addcity.this, city + "\"已在列表中\"", Toast.LENGTH_SHORT).show();
                    isAdding = false;
                    return;
                }
                haveCity(city);
            }
        });
        locBtn.setOnClickListener(new View.OnClickListener() {
            
            
            public void onClick(View v) {
                tv.setText("正在定位中...");
                myBDlocation m = new myBDlocation(addcity.this);
                m.requestLocationInfo();
            }
        });
    }
    
    private void registerBroadCastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            
            public void onReceive(Context context, Intent intent) {
                String address = intent.getStringExtra("address");
                if(UIupdater.checkCity(addcity.this, address)) {
                    Toast.makeText(addcity.this, address, 0x1).show();
                    if(!dataSourceList.contains(address)) {
                        dataSourceList.add(address);
                    }
                    adapter.notifyDataSetChanged();
                    tv.setText(address);
                    return;
                }
                if(address != null) {
                    tv.setText(address);
                    edt.setText(address);
                    if(!dataSourceList.contains(address)) {
                        dataSourceList.add(address);
                    }
                    adapter.notifyDataSetChanged();
                    return;
                }
                tv.setText("定位失败,请检查网络是否开启?");
            }

        };
        IntentFilter intentToReceiveFilter = new IntentFilter();
        intentToReceiveFilter.addAction(LOCATION_BCR);
        registerReceiver(broadcastReceiver, intentToReceiveFilter);
    }
    
    public void finish() {
        super.finish();
        overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
    }
}
