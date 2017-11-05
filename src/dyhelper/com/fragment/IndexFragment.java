package dyhelper.com.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import android.app.Activity;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.Toast;
import dyhelper.com.adapter.GuidePageAdapter;
import dyhelper.com.adapter.ToolsAdapter;
import dyhelper.com.appsmanager.AppsMain;
import dyhelper.com.bean.PageActLogBag;
import dyhelper.com.bean.PageActLogInfo;
import dyhelper.com.filemanager.explorer.FileManagerMain;
import dyhelper.com.ui.FlashlightMain;
import dyhelper.com.ui.HongbaoMain;
import dyhelper.com.ui.Login;
import dyhelper.com.ui.Main;
import dyhelper.com.ui.UserCenter;
import dyhelper.com.ui.XYZApplication;
import dyhelper.com.util.Constants;
import dyhelper.com.util.Tools;
import dyhelper.com.weather.WeatherMain;
import xyz.monkeytong.hongbao.R;

public class IndexFragment extends Fragment {
    public static final String TOOLS_IMAGE = "TOOLS_IMAGE";
    public static final String TOOLS_TITLE = "TOOLS_TITLE";
    int activityOpenEnterAnimation;
    int activityOpenExitAnimation;
    //空指针异常
    private GuidePageAdapter adapter;
    private List<View> ar;
    private AtomicInteger atomicInteger = new AtomicInteger();
    private ToolsAdapter brandAdapter;
    private List<Map<String, String>> brandList = new ArrayList<Map<String, String>>();
    private ViewGroup dotGroup;
    private View gv_brand_loading;
    private volatile boolean isNotify;
    private ImageButton loginBtn;
    private GridView mGvbrand;
    private ImageView mImage;
    private ImageView[] mImages;
    private Main.MyCommunication myCommunication;
    private Timer timer;
    private Toast toast;
    private View view;
    private ViewPager viewPager;
    private PageActLogBag pageActLogBag;
    
    
    Handler handler = new Handler() { 
		public void handleMessage(android.os.Message msg) {
			// 显示第几项
			viewPager.setCurrentItem(msg.what);

			if (atomicInteger.get() == ar.size()) {
				atomicInteger.set(0);
			}
		};
	};
    
    
    ViewPager.OnPageChangeListener vp_listener = new ViewPager.OnPageChangeListener()
    {
      public void onPageScrollStateChanged(int paramAnonymousInt)
      {
      }

      public void onPageScrolled(int paramAnonymousInt1, float paramAnonymousFloat, int paramAnonymousInt2)
      {
      }

      public void onPageSelected(int position)
      {
        atomicInteger.getAndSet(position);
        for (int i = 0; i < mImages.length; i++)
        {
          
          if (position != i)
        	  mImages[i].setBackgroundResource(R.drawable.small_bg);
          else 
        	  mImages[i].setBackgroundResource(R.drawable.small_bg1);
        }
      }
    };
    
    
    private  OnItemClickListener gv_listener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			Activity activity = getActivity();
			PageActLogInfo bag = new PageActLogInfo();
			bag.setTimeStamp(Tools.getTimeStemp_10());
			switch(position) {
			//抢红包
			case 0:
				bag.setFunctionId(Constants.OperationLogPage.hongbao[0]);
				bag.setRemark(Constants.OperationLogPage.hongbao[1]);
				pageActLogBag.addBag(bag);
				startActivity(new Intent(activity, HongbaoMain.class));
				break;
				
			//天气
			case 1:
				bag.setFunctionId(Constants.OperationLogPage.weather[0]);
				bag.setRemark(Constants.OperationLogPage.weather[1]);
				pageActLogBag.addBag(bag);
				startActivity(new Intent(activity, WeatherMain.class));
				break;
			//夺宝
			case 2:
				bag.setFunctionId(Constants.OperationLogPage.indiana[0]);
				bag.setRemark(Constants.OperationLogPage.indiana[1]);
				pageActLogBag.addBag(bag);
				startFragment(2);
				break;
				
			//文件系统
			case 3:
				bag.setFunctionId(Constants.OperationLogPage.filemanager[0]);
				bag.setRemark(Constants.OperationLogPage.filemanager[1]);
				pageActLogBag.addBag(bag);
				startActivity(new Intent(activity, FileManagerMain.class));
				break;
				
			//程序卸载
			case 4:
				bag.setFunctionId(Constants.OperationLogPage.delapp[0]);
				bag.setRemark(Constants.OperationLogPage.delapp[1]);
				pageActLogBag.addBag(bag);
				startActivity(new Intent(activity, AppsMain.class));
				break;
				
			//发现
			case 5:
				bag.setFunctionId(Constants.OperationLogPage.discover[0]);
				bag.setRemark(Constants.OperationLogPage.discover[1]);
				pageActLogBag.addBag(bag);
				startFragment(4);
				break;
			
			//应用
			case 6:
				bag.setFunctionId(Constants.OperationLogPage.application[0]);
				bag.setRemark(Constants.OperationLogPage.application[1]);
				pageActLogBag.addBag(bag);
				startFragment(5);
				break;
			
			//手电筒
			case 7:
				bag.setFunctionId(Constants.OperationLogPage.flashlight[0]);
				bag.setRemark(Constants.OperationLogPage.flashlight[1]);
				pageActLogBag.addBag(bag);
				startActivity(new Intent(activity, FlashlightMain.class));
				break;
				
			//添加
			case 8:
				toast.setText("该功能尚未开放");
				toast.show();
				break;
			}
			
			
			
		}
    	
    };
    
    public void onDestroy() {
        super.onDestroy();
        viewPager = null;
        mGvbrand = null;
        if((ar != null) && (!ar.isEmpty())) {
            ar.clear();
        }
        ar = null;
        adapter = null;
        atomicInteger = null;
        mImages = null;
        if((brandList != null) && (!brandList.isEmpty())) {
            brandList.clear();
        }
        brandList = null;
        brandAdapter = null;
        dotGroup = null;
        System.gc();
    }
    
    public void jumpFragment(Main.MyCommunication myCommunication) {
        this.myCommunication = myCommunication;
    }
    
    private void startFragment(int position) {
        myCommunication.getResultFragment(position);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(atomicInteger == null) {
            atomicInteger = new AtomicInteger();
        }
        view = inflater.inflate(R.layout.fragment_index, null);
        initView();
        initData();
        initEvent();
        obtrainOpenAnims();
        return view;
    }
    
    private OnClickListener loginOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (XYZApplication.checkIsLogin(IndexFragment.this.getActivity())) {
		        startActivity(new Intent(IndexFragment.this.getActivity(), UserCenter.class));
		        
			} else {
				startActivity(new Intent(IndexFragment.this.getActivity(), Login.class));
			}
			if (Build.VERSION.SDK_INT > 19)
	            IndexFragment.this.getActivity().overridePendingTransition(IndexFragment.this.activityOpenEnterAnimation, IndexFragment.this.activityOpenExitAnimation);
		}
    	
    };
    
    private void initEvent() {
        mGvbrand.setOnItemClickListener(gv_listener);
        loginBtn.setOnClickListener(loginOnClickListener);
    }
    
    private void initData() {
    	toast = Toast.makeText(getActivity(), "", 0);
    	pageActLogBag = PageActLogBag.create(getActivity());
    	ar = new ArrayList<View>();
    	adapter = new GuidePageAdapter(getActivity(), ar); 
    	viewPager.setAdapter(adapter);
        brandList = new ArrayList<Map<String, String>>();
        String[] toolsName = getActivity().getResources().getStringArray(R.array.android_tools_name);
        TypedArray tr = getActivity().getResources().obtainTypedArray(R.array.android_tools_images);
        for(int i = 0; i < tr.length(); ++i) {
        	HashMap<String, String> map = new HashMap<String, String>();
            map.put(TOOLS_TITLE, toolsName[i]);
            map.put(TOOLS_IMAGE, tr.getResourceId(i, 0) + "");
            brandList.add(map);
        }
        
        brandAdapter = new ToolsAdapter(getActivity(), brandList);
        mGvbrand.setAdapter(brandAdapter);
    }
    
    private void initView() {
    	loginBtn = (ImageButton) view.findViewById(R.id.id_login);
    	dotGroup = (ViewGroup) view.findViewById(R.id.rounddot);
    	gv_brand_loading = view.findViewById(R.id.gv_brand_loading);
    	mGvbrand = (GridView) view.findViewById(R.id.gv_brand);
    	viewPager = (ViewPager) view.findViewById(R.id.vp_advertise);
    	Log.i("chaochao", "initView..");
	    synchronized (this) {
	    	this.notify();
	    	isNotify = true;
		}
    }
    
    
    
    public void updateTitleHeadImg(List<Bitmap> listMap) {
        if((listMap == null) || (listMap.isEmpty())) {
            return;
        }
        Log.i("chaochao", "updateTitleHeadImg..");
        final List<Bitmap> finalListMap = listMap;
        
        Log.i("chaochao", "viewPager: " + viewPager);
        
        
        
        new Thread(new Runnable() {

			@Override
			public void run() {
				
				if(viewPager == null) {
		        	synchronized (IndexFragment.this) {
		        		if(viewPager == null) {
			        		try {
								IndexFragment.this.wait();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
		        		}
		        		
		        		
					}
		        }
				
				
				new Handler(Looper.getMainLooper()).post(new Runnable() {
					
					@Override
					public void run() {
						updateTitleHeadImg_pr(finalListMap);
						awakeAnimation();
					}
				});
			}
        	
        }).start();
        
	    
        
    }
    
    public void cancelAnimation() {
        if((ar == null) || (ar.size() <= 1)) {
            return;
        }
        if(timer != null) {
            timer.cancel();
        }
    }
    
    public void awakeAnimation() {
    	if((ar == null) || (ar.size() <= 1)) {
            return;
        }
        if(timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        TimerTask task = new TimerTask() {
            
            public void run() {
                handler.sendEmptyMessage((atomicInteger.incrementAndGet() - 1));
            }
        };
        timer.schedule(task, 2000, 2000);
    }
    
    private void updateTitleHeadImg_pr(List<Bitmap> listMap) {
    	 Log.i("chaochao", "----------------updateTitleHeadImg_pr--------------");
    	if(ar != null && listMap != null) {
			ar.clear();
			Log.i("chaochao", "listMap size:" + listMap.size());
			for(Bitmap bmp : listMap) {
				View v = getActivity().getLayoutInflater().inflate(
						R.layout.advertise_item, null);
				LinearLayout l = (LinearLayout) v.findViewById(R.id.advertise_item);
				ImageView iv = new ImageView(getActivity());
				LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
				iv.setLayoutParams(lp);
				iv.setScaleType(ScaleType.FIT_XY);
				iv.setImageBitmap(bmp);
				l.addView(iv);
				ar.add(l);
			}
			dotGroup.removeAllViews();
			if(ar.size() > 1) {
				this.mImages = new ImageView[ar.size()];
				 Log.i("chaochao", "mImages size: " + mImages.length);
				for(int i = 0; i < mImages.length; i++) {
					mImage = new ImageView(getActivity());
					// 设置图片宽和高
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(9, 9);
					layoutParams.setMargins(10, 5, 10, 5);
					mImage.setLayoutParams(layoutParams);
	
					mImages[i] = mImage;
	
					if (i == 0) {
						mImages[i].setBackgroundResource(R.drawable.small_bg1);
					} else {
						mImages[i].setBackgroundResource(R.drawable.small_bg);
					}
					dotGroup.addView(mImages[i]);
				}
			}
			
			
			gv_brand_loading.setVisibility(View.GONE);
			viewPager.setVisibility(View.VISIBLE);
			viewPager.setOnPageChangeListener(vp_listener);
			adapter.notifyDataSetChanged();
		}
    }
    
    private void obtrainOpenAnims() {
    	activityOpenEnterAnimation = R.anim.hongbao_setting_open_enter;
    	activityOpenExitAnimation = R.anim.hongbao_setting_open_exit;
    }
    
    public void onStop() {
        Log.i("chaochao", "index-onStop");
        super.onStop();
    }
}
