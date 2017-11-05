package dyhelper.com.weather;

import android.widget.Button;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.support.v4.view.ViewPager;
import dyhelper.com.view.PageTitleView;
import xyz.monkeytong.hongbao.R;
import java.util.ArrayList;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.graphics.Color;
import android.os.Build;
import dyhelper.com.ui.Main;
import android.content.SharedPreferences;
import android.util.Log;
import android.support.v4.view.PagerAdapter;
import java.util.List;

public class UIupdater {
    private static final String BEIJING = "北京市";
    private int activityOpenEnterAnimation;
    private int activityOpenExitAnimation;
    private Context context;
    private ArrayList<View> viewContainter = new ArrayList<View>();
    private ArrayList<String> titleContainer = new ArrayList<String>();
    private ArrayList<ViewAdapter> uiAdapterContainer = new ArrayList<ViewAdapter>();
    private ViewPager pager = null;
    private PageTitleView tabStrip = null;
    private View mview = null;
    private Button button = null;
    
    UIupdater(Context tempContext) {
        context = tempContext;
        LayoutInflater flater = LayoutInflater.from(context);
        mview = flater.inflate(R.layout.weather_mview, null);
        tabStrip = (PageTitleView)mview.findViewById(R.id.id_pageTitleView);
        tabStrip.setCanSeeCount(3);
        pager = (ViewPager)mview.findViewById(R.id.viewpager);
        button = (Button)mview.findViewById(R.id.buttonSet);
        tabStrip.setUnselectedColor(Color.parseColor("#0090b5"));
        
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(context, addcity.class);
                context.startActivity(intent);
                if(Build.VERSION.SDK_INT > 0x13) {
                	Activity a = (Activity) context;
                    a.overridePendingTransition(activityOpenEnterAnimation, activityOpenExitAnimation);
                }
            }
        });
        
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				Log.i("chaochao", "position: " + position);
				 UIupdater.this.saveScrollPosition(position);
				 tabStrip.setCurrentPosition(position);
			}
			
			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}
			
			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
       /* pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            
            public void onPageSelected(int position) {
               
            }
            
            public void onPageScrolled(int position, float value, int arg2) {
            }
            
            public void onPageScrollStateChanged(int arg0) {
            }
        });*/
        tabStrip.setOnSelectListener(new PageTitleView.IOnSelectListener() {
            
            public void selected(int position) {
                pager.setCurrentItem(position);
            }
        });
        int[] activityAniValues = Main.obtrainOpenAnims(context);
        activityOpenEnterAnimation = activityAniValues[0x0];
        activityOpenExitAnimation = activityAniValues[0x1];
    }
    
    public void clear() {
        viewContainter.clear();
        titleContainer.clear();
        uiAdapterContainer.clear();
        if(pager != null) {
            pager.removeAllViews();
            pager = null;
        }
        if(tabStrip != null) {
            tabStrip.clear();
            tabStrip = null;
        }
        System.gc();
    }
    
    public void setCurrentPosition(int position) {
        if(position < 0) {
            return;
        }
        if(pager != null) {
            pager.setCurrentItem(position);
        }
    }
    
    public static int getPositionByCity(Context context, String cityname) {
        if(cityname == null) {
            return -1;
        }
        SharedPreferences share = context.getSharedPreferences("citylist", 0);
        int size = share.getInt("size", 0);
        
        for(int i = 0; i < size; i++) {
        	String city = share.getString("city" + i, "null");
        	if(city.equals(cityname)) {
        		return i;
        	}
        }
        
        return -1;
    }
    
    public View update() {
        if(checkEmpty(context)) {
            saveCity(context, "北京市");
        }
        int size = getCityList();
        Log.i("event123", "--size: " + size);
        for (int i = 0; i < size; i++) {
			View view = LayoutInflater.from(context).inflate(
					R.layout.weather_otherweatherback, null);
			ViewAdapter u = new ViewAdapter(view, context);
			viewContainter.add(view);
			uiAdapterContainer.add(u);
			uiAdapterContainer.get(i).setCity(titleContainer.get(i));
			uiAdapterContainer.get(i).updateWeather();
		}
        tabStrip.setTitles(titleContainer);
        updatepage();
        return mview;
    }
    
    private void updatepage() {
        pager.setAdapter(new PagerAdapter() {
            
            
            public int getCount() {
                return viewContainter.size();
            }
            
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView((View)viewContainter.get(position));
            }
            
            public Object instantiateItem(ViewGroup container, int position) {
                container.addView((View)viewContainter.get(position));
                return viewContainter.get(position);
            }
            
            public boolean isViewFromObject(View arg0, Object arg1) {
                return (arg0 == arg1);
            }
            
            public int getItemPosition(Object object) {
                return super.getItemPosition(object);
            }
            
            public CharSequence getPageTitle(int position) {
                return (CharSequence)titleContainer.get(position);
            }
        });
        pager.setCurrentItem(getScrollPosition());
        tabStrip.setCurrentPosition(getScrollPosition());
    }
    
    private int getCityList() {
    	titleContainer.clear();
		SharedPreferences share = context.getSharedPreferences("citylist",
				Activity.MODE_PRIVATE);
		int size = share.getInt("size", 0);
		for (int i = 0; i < size; i++) {
			titleContainer.add(share.getString("city" + i, "null"));
		}

		return titleContainer.size();
    }
    
    public static boolean checkCity(Context context, String cityname) {
        if(cityname == null) {
            return false;
        }
        SharedPreferences share = context.getSharedPreferences("citylist", 0);
        int size = share.getInt("size", 0x0);
        
        if(!cityname.endsWith("市")) {
        	cityname += "市";
        }
        
        for(int i = 0; i < size; ++i) {
            String name = share.getString("city" + i, null);
            if(!name.endsWith("市")) name += "市";
            if((name != null) && name.equals(cityname)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean checkEmpty(Context context) {
        SharedPreferences share = context.getSharedPreferences("citylist", 0);
        int size = share.getInt("size", 0);
        return size <= 0 ? true : false;
    }
    
    /*public static boolean isOnlyBeijing(Context context) {
        SharedPreferences share = context.getSharedPreferences("citylist", 0x0);
        int size = share.getInt("size", 0);
        boolean flag = (size == 1) && (getPositionByCity(context, "北京市") != -1);
        return flag;
    }*/
    
    public ArrayList<String> getcityList() {
        getCityList();
        return titleContainer;
    }
    
    private void saveCityList() {
    	SharedPreferences share = context.getSharedPreferences("citylist",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor edit = share.edit();
		edit.putInt("size", titleContainer.size());
		for (int i = 0; i < titleContainer.size(); i++) {
			edit.putString("city" + i, titleContainer.get(i));
		}
		edit.commit();
    }
    
    private void saveScrollPosition(int position) {
        SharedPreferences share = context.getSharedPreferences("citylist", 0x0);
        SharedPreferences.Editor edit = share.edit();
        edit.putInt("scroll_position", position);
        edit.commit();
    }
    
    private int getScrollPosition() {
        SharedPreferences share = context.getSharedPreferences("citylist", 0x0);
        int position = share.getInt("scroll_position", 0x0);
        int size = share.getInt("size", 0x0);
        return size > position ? position : 0x0;
    }
    
    public void savecityList(List<String> list) {
    	SharedPreferences share = context.getSharedPreferences("citylist",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor edit = share.edit();
		edit.putInt("size", list.size());
		for (int i = 0; i < list.size(); i++) {
			edit.putString("city" + i, list.get(i));
		}
		edit.commit();
    }
    
    public static void saveCity(Context context, String cityName) {
        if((cityName == null) || (cityName.trim().equals(""))) {
            return;
        }
        SharedPreferences share = context.getSharedPreferences("citylist", 0);
        int size = share.getInt("size", 0);
        SharedPreferences.Editor edit = share.edit();
        edit.putString("city" + size, cityName);
        edit.putInt("size", (size + 1));
        edit.commit();
    }
    
    public void addCity(String city) {
    	int index = titleContainer.size();
		titleContainer.add(city);
		View view = LayoutInflater.from(context).inflate(
				R.layout.weather_otherweatherback, null);
		ViewAdapter u = new ViewAdapter(view, context);
		viewContainter.add(view);
		uiAdapterContainer.add(u);
		uiAdapterContainer.get(index).setCity(titleContainer.get(index));
		uiAdapterContainer.get(index).updateWeather();
		updatepage();
    }
    
    public static void saveRemoveCity(Context context, String cityName) {
        if((cityName == null) || (cityName.trim().equals(""))) {
            return;
        }
        SharedPreferences share = context.getSharedPreferences("removecitylist", 0);
        int size = share.getInt("size", 0);
        share.edit().putInt("size", (size + 1)).putString("city" + size, cityName).commit();
    }
    
    public static boolean checkRemoveCity(Context context, String cityName) {
        if((cityName == null) || (cityName.trim().equals(""))) {
            return false;
        }
        SharedPreferences share = context.getSharedPreferences("removecitylist", 0x0);
        int size = share.getInt("size", 0);
        for(int i = 0; i < size; ++i) {
            String cityN = share.getString("city" + i, null);
            if((cityN != null) && (cityN.equals(cityName))) {
                return true;
            }
        }
        return false;
    }
}
