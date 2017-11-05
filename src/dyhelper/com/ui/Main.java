package dyhelper.com.ui;

import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;
import dyhelper.com.services.AppUpdateManager;
import dyhelper.com.fragment.ApplicationFragment;
import dyhelper.com.bean.ConfigInfo;
import cn.dianyou.advert.adEntry.DianYouAdvert;
import dyhelper.com.fragment.IndexFragment;
import android.view.View;
import android.view.View.OnClickListener;

import java.util.List;
import android.graphics.Bitmap;
import dyhelper.com.broadcasts.NetworkChangeReceiver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import dyhelper.com.fragment.MailFragment;
import android.support.v4.app.FragmentManager;
import dyhelper.com.fragment.NewsFragment;
import dyhelper.com.listeners.IOnBackListener;
import android.widget.Toast;
import dyhelper.com.fragment.ToolsFragment;
import android.os.Build;
import dyhelper.com.util.Tools;
import okhttp3.Call;
import xyz.monkeytong.hongbao.R;
import android.view.Window;
import android.view.WindowManager;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import dyhelper.com.bean.ImageInfo;
import dyhelper.com.bean.PageActLogBag;
import dyhelper.com.bean.PageActLogInfo;
import cn.dianyou.utils.FileManageUtils;
import android.graphics.BitmapFactory;
import java.util.Arrays;
import dyhelper.com.util.DialogUtils;
import android.app.Dialog;
import android.content.IntentFilter;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import android.util.Log;
import android.support.v4.app.FragmentTransaction;
import android.content.Intent;
import cn.dianyou.advert.adFactory.HolderAdvertFactory;
import cn.dianyou.advert.adFactory.AdvertFactory;
import cn.dianyou.advert.adFactory.AdvertType;
import cn.dianyou.nets.DYHttpUtils;
import cn.dianyou.nets.DYStringCallback;
import cn.dianyou.nets.DYBitmapCallback;
import java.util.HashMap;
import java.util.LinkedList;
import android.content.Context;
import android.os.Bundle;
import dyhelper.com.util.ActivityCollector;
import dyhelper.com.util.Constants;

public class Main extends FragmentActivity {
    private ViewGroup adsParent;
    private AppUpdateManager appUpdateManager;
    private ApplicationFragment applicationFragment;
    private volatile ConfigInfo configInfo = new ConfigInfo();
    private int currentFragment;
    private DianYouAdvert dianYouAdvert;
    private IndexFragment indexFragment;
    //空指针
    private List<Bitmap> listHeadBmp = new ArrayList<Bitmap>();
    private ImageView mApplicationImage;
    private LinearLayout mApplicationLay;
    private TextView mApplicationText;
    private ImageView mIndexImage;
    private LinearLayout mIndexLay;
    private TextView mIndexText;
    private ImageView mMailImage;
    private LinearLayout mMailLay;
    private TextView mMailText;
    private ImageView mNewsImage;
    private LinearLayout mNewsLay;
    private TextView mNewsText;
    private ViewGroup mRootChildView;
    private ImageView mToolsImage;
    private LinearLayout mToolsLay;
    private TextView mToolsText;
    private MailFragment mailFragment;
    private FragmentManager manager;
    private NetworkChangeReceiver networkReceiver;
    private NewsFragment newsFragment;
    private IOnBackListener onBackListener;
    private Toast toast;
    private ToolsFragment toolsFragment;
    private DianYouAdvert.ViewPagerAds viewPagerAds;
    public static int statusBarHeight = 0;
    private static boolean isCreate = false;
    
    private PageActLogBag pageActLogBag;
    
    protected void onDestroy() {
        super.onDestroy();
        if(dianYouAdvert != null) {
            dianYouAdvert.destroy();
        }
        if(!isCreate) {
            DialogUtils.clear();
        }
        ActivityCollector.removeActivity(this);
        if((listHeadBmp != null) && (!listHeadBmp.isEmpty())) {
            for(Bitmap bmp : listHeadBmp) {
            	if(!bmp.isRecycled()) {
                    bmp.recycle();
                }
            }
            listHeadBmp.clear();
        }
        
        
        
        if(networkReceiver != null) {
            unregisterReceiver(networkReceiver);
            networkReceiver = null;
        }
        listHeadBmp = null;
        viewPagerAds = null;
        dianYouAdvert = null;
        onBackListener = null;
        applicationFragment = null;
        newsFragment = null;
        indexFragment = null;
        mailFragment = null;
        toolsFragment = null;
        appUpdateManager = null;
        System.gc();
        isCreate = false;
    }
    
    public void performBack(View v) {
        onBackPressed();
    }
    
    private void changeStatusBarHeight(int height) {
        if(mRootChildView == null) {
            return;
        }
        mRootChildView.setPadding(0, height, 0, 0);
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        pageActLogBag = PageActLogBag.create(this);
        ActivityCollector.addActivity(this); 
        toast = Toast.makeText(this, "", 0);
        adjustStatusBar();
        isCreate = true;
        manager = getSupportFragmentManager();
        initDianYouAds();
        initView();
        handleData();
    }
    
    private void adjustStatusBar() {
        if(Build.VERSION.SDK_INT >= 0x15) {
            statusBarHeight = Tools.getStatusHeight(this);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mRootChildView = (ViewGroup)getWindow().findViewById(0x1020002);
            changeStatusBarHeight(statusBarHeight);
        }
    }
    
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        Log.i("INFO", "onWindowFocusChanged...");
    }
    
    public static int[] obtrainOpenAnims(Context context) {
       return new int[]{R.anim.hongbao_setting_open_enter, R.anim.hongbao_setting_open_exit};
    }
    
    public static int[] obtrainExitAnims(Context context) {
    	return new int[]{R.anim.hongbao_setting_close_enter, R.anim.hongbao_setting_close_exit};
    }
    
    private void initFail(int step, String failMsg) {
        Log.i("INFO", step + ", initFail:" + failMsg);
        switch(step) {
            case 1:
            {
                toast.setText(failMsg);
                toast.show();
                break;
            }
            case 0:
            case 2:
            case 3:
            {
                break;
            }
        }
        List<Bitmap> listBmp = Arrays.asList(new Bitmap[] {BitmapFactory.decodeResource(getResources(), R.drawable.pre_default_headimg)});
        listHeadBmp.clear();
        listHeadBmp.addAll(listBmp);
        indexFragment.updateTitleHeadImg(listBmp);
        if((currentFragment == 1) && (toolsFragment != null)) {
            toolsFragment.updateTitleHeadImg(listBmp);
        }
        DialogUtils.closeDialog(this);
    }
    
    private void obtainUniqueId() {
        boolean exits = XYZApplication.checkExistsId(this);
        if(!exits) {
            XYZApplication.obtainUniqueIdByPhoneInfo(this, new XYZApplication.SimpleOnInitListener() {
                
                public void uniqueIdSuccess(String currentId, String uniqueIdType) {
                    Log.i("INFO", "..........success:");
                    //存在
                    initInterface(currentId, uniqueIdType);
                }
                
                public void uniqueIdFail(String currentId, String uniqueIdType) {
                    Log.i("INFO", Thread.currentThread().toString());
                    if(currentId != null) {
						//存在
						initInterface(currentId, uniqueIdType);
					} else {
						//不存在
						initFail(1, "Obtain uniqueId fail.");
					}
                }
            });
            return;
        }
        String currentId = XYZApplication.obtainStringInfo(this, "dianyou_login_current_id");
        String currentIdType = XYZApplication.obtainStringInfo(this, "dianyou_login_current_id_type");
        if(currentId == null) {
            currentId = XYZApplication.obtainStringInfo(this, "dianyou_unique_id");
            currentIdType = XYZApplication.obtainStringInfo(this, "dianyou_unique_id_type");
        }
        initInterface(currentId, currentIdType);
    }
    
    private void initInterface(String currentId, String currentType) {
        if(appUpdateManager == null) {
            appUpdateManager = new AppUpdateManager(this, currentId, currentType);
            appUpdateManager.work();
        }
        Log.i("INFO", currentId);
        Log.i("INFO", currentType);
        if(viewPagerAds == null) {
            viewPagerAds = dianYouAdvert.createViewPagerAds(adsParent).setFailListImage(getDianYouAdsFailImage());
        }
        viewPagerAds.show();
        try {
            DYHttpUtils.getInstance().encoding("UTF-8").post().url(Constants.Dianyou_url.URL_INIT).paramJSONType().addParams("Id", currentId).addParams("Type", currentType).build().execute(new DYStringCallback() {
                
                public void onResponse(String data, int id) {
                	try {
						data = URLDecoder.decode(data, "UTF-8");
						Log.i("INFO", "initInterface->Thread:" + Thread.currentThread().toString());
						Log.i("INFO", "initInterface->data:" + data);
						JSONObject jsonObj = new JSONObject(data);
						int code = jsonObj.getInt("Code");
						if(code == 0) {
							String _id = jsonObj.getString("Id");
			                String _type = jsonObj.getInt("Type") + "";
							List<ImageInfo> listImageInfo = new LinkedList<ImageInfo>();
							JSONArray jsonArr = jsonObj.getJSONArray("ImgInfo");
							for(int i = 0; i < jsonArr.length(); i++) {
								JSONObject jsonImgObj = jsonArr.getJSONObject(i);
								String title = jsonImgObj.getString("Title");
								String url = jsonImgObj.getString("Url");
								int sort = jsonImgObj.getInt("Sort");
								listImageInfo.add(new ImageInfo(title, url, sort));
							}
							
							JSONObject configObj = jsonObj.getJSONObject("Config");
							String weiXinHongBao = configObj.getString("WeiXinHongBao");
							String shopUrl = configObj.getString("ShopUrl");
							String gameUrl = configObj.getString("GameUrl");
							String newsUrl = configObj.getString("NewUrl");
							ConfigInfo configInfo = new ConfigInfo(weiXinHongBao, shopUrl, gameUrl, newsUrl);
							configInfo.saveToShare(Main.this);
							Main.this.configInfo = configInfo;
							
							if (Main.this.mailFragment != null)
			                {
			                  Main.this.mailFragment.setUrl(shopUrl);
			                  Main.this.mailFragment.flush();
			                }
			                if (Main.this.applicationFragment != null)
			                {
			                  Main.this.applicationFragment.setUrl(gameUrl);
			                  Main.this.applicationFragment.flush();
			                }
			                if (Main.this.newsFragment != null)
			                {
			                  Main.this.newsFragment.setUrl(newsUrl);
			                  Main.this.newsFragment.flush();
			                }
			                
			                XYZApplication.saveInfo(Main.this, Constants.Login.LOGIN_FEEUSER_ID, _id);
			                XYZApplication.saveInfo(Main.this, Constants.Login.LOGIN_FEEUSER_ID_TYPE, _type);
			                XYZApplication.saveInfo(Main.this, Constants.Login.LOGIN_CURRENT_ID, _id);
			                XYZApplication.saveInfo(Main.this, Constants.Login.LOGIN_CURRENT_ID_TYPE, _type);
			                
			                Log.i("INFO", "initInterface->listImageInfo:" + listImageInfo);
			                
			                if(listImageInfo != null && !listImageInfo.isEmpty()) {
			                	Collections.sort(listImageInfo);
								//执行第3步
								obtainTitleImg(listImageInfo);
			                } else {
			                	initFail(2, "the heads images is empty whitch is from server!");
			                }
							
						} else {
							initFail(2, "the Code is not 0 which is from initInterface.");
						}
					} catch (Exception e) {
						initFail(2, "InitInterface happend a exception that is \"" + e.toString() + "\".");
					}
                }
                
                public void onError(Call call, Exception ex, int id) {
                	initFail(2, "InitInterface happend a exception that is \"" + ex.toString() + "\".");
                }
            });
        } catch(Exception e) {
            initFail(2, e.toString() + "\".");
        }
    }
    
    private void obtainTitleImg(List<ImageInfo> listImgInfo) {
        if((listImgInfo == null) || (listImgInfo.isEmpty())) {
            return;
        }
        HashMap<String, String> titleUrlMap = new HashMap<String, String>();
        for(int i = 0; i < listImgInfo.size(); ++i) {
        	ImageInfo imageInfo = (ImageInfo)listImgInfo.get(i);
            String filename = FileManageUtils.getFileName(imageInfo.getUrl());
            if(!FileManageUtils.isExistsFile_b2(this, Constants.FileInfo.CURRENT_SIMPLE_HEADER_IMAGE_DIR, filename)) {
                titleUrlMap.put(imageInfo.getSort() + "", listImgInfo.get(i).getUrl());
                Log.i("sort", "not exits->" + filename);
            } else {
            	 Log.i("sort", "exits->" + filename);
            }
           
        }
        if(titleUrlMap.isEmpty()) {
            handleTitleImg(listImgInfo);
            Log.i("sort", "main..is Empty");
            return;
        }
        final LinkedList<ImageInfo> finalImageInfo = new LinkedList<ImageInfo>(listImgInfo);
        try {
            DYHttpUtils.getInstance().post().urlMap(titleUrlMap).buildMap().execute(new DYBitmapCallback() {
                
                public void onResponseMap(Map<String, Bitmap> map) {
                	doFilterImage(finalImageInfo, map);
                    handleTitleImg(finalImageInfo);
                    
                    
                }
                
                public void onErrorMap(Map<String, Call> map) {
                	Main.this.initFail(3, "TitleHead images is empty.");
                }
                
                public void onResponse(Bitmap arg0, int arg1) {
                }
                
                public void onError(Call arg0, Exception arg1, int arg2) {
                }
            });
            return;
        } catch(Exception e) {
            initFail(3, e.toString() + "\".");
        }
    }
    
    private void doFilterImage(List<ImageInfo> listImgInfo, Map<String, Bitmap> map) {
        if((listImgInfo != null) && (map == null)) {
            return;
        }
        
        for(int i = listImgInfo.size() - 1; i >= 0; --i) {
        	ImageInfo imgInfo = (ImageInfo)listImgInfo.get(i);
            String key = imgInfo.getSort() + "";
            Bitmap bmp = (Bitmap)map.get(key);
            String filename = FileManageUtils.getFileName(imgInfo.getUrl());
            if((!map.containsKey(key)) || (bmp == null)) {
                if(!FileManageUtils.isExistsFile_b2(this, Constants.FileInfo.CURRENT_SIMPLE_HEADER_IMAGE_DIR, filename)) {
                    listImgInfo.remove(i);
                    continue;
                }
                bmp = FileManageUtils.obtainBitmap(this, Constants.FileInfo.CURRENT_SIMPLE_HEADER_IMAGE_DIR, filename);
            }
            imgInfo.setBitmap(bmp);
        }
    }
    
    private void handleTitleImg(List<ImageInfo> imageInfoList) {
        if((imageInfoList == null) || (imageInfoList.isEmpty())) {
            initFail(4, "TitleHead images is empty.");
            return;
        }
        
        FileManageUtils.deleteFilesAtDir(this, Constants.FileInfo.CURRENT_SIMPLE_HEADER_IMAGE_DIR, 30, 5);
        ArrayList<Bitmap> bmpList = new ArrayList<Bitmap>();
        for(ImageInfo imgInfo : imageInfoList) {
        	 Bitmap bmp = imgInfo.getBitmap();
             String filename = FileManageUtils.getFileName(imgInfo.getUrl());
             
             if(bmp != null) {
                 if(!FileManageUtils.isExistsFile_b2(this, Constants.FileInfo.CURRENT_SIMPLE_HEADER_IMAGE_DIR, filename)) {
                     FileManageUtils.saveBitmap(this, bmp, Constants.FileInfo.CURRENT_SIMPLE_HEADER_IMAGE_DIR, filename);
                 }
             } else {
            	 if(FileManageUtils.isExistsFile_b2(this, Constants.FileInfo.CURRENT_SIMPLE_HEADER_IMAGE_DIR, filename)) {
            		 bmp = FileManageUtils.obtainBitmap(this, Constants.FileInfo.CURRENT_SIMPLE_HEADER_IMAGE_DIR, filename);
                 }
             }
             
             if(bmp != null) {
                 bmpList.add(bmp);
             }
        }
       
        listHeadBmp.clear();
        listHeadBmp.addAll(bmpList);
        if((currentFragment == 1) && (toolsFragment != null)) {
            toolsFragment.updateTitleHeadImg(bmpList);
        }
        indexFragment.updateTitleHeadImg(bmpList);
        DialogUtils.closeDialog(this);
       
    }
    
    private NetworkChangeReceiver.IOnNetworkChangeListener networkListener = new NetworkChangeReceiver.IOnNetworkChangeListener() {
		
		@Override
		public void openNetwork() {
			configInfo.recoverFromShare(Main.this);
			switch (currentFragment) {
			case 1:
				if (!XYZApplication.checkExistsId(Main.this)) {
					handleData();
				}
				break;
				
			case 2:
				if (!XYZApplication.checkExistsId(Main.this) && Main.this.configInfo.isErrorUrl(ConfigInfo.TYPE_SHOP_URL)) {
					handleData();
		         } else {
		        	 mailFragment.setUrl(Main.this.configInfo.getShopUrl());
		             mailFragment.flush();
		         }
				break;
				
			case 3:
				if (!XYZApplication.checkExistsId(Main.this)) {
					Main.this.handleData();
		        }
				break;
				
			case 4:
				if (!XYZApplication.checkExistsId(Main.this) && Main.this.configInfo.isErrorUrl(ConfigInfo.TYPE_NEW_URL)) {
					handleData();
		         } else {
		        	 newsFragment.setUrl(Main.this.configInfo.getNewUrl());
		        	 newsFragment.flush();
		         }
				break;
				
			case 5:
				if (!XYZApplication.checkExistsId(Main.this) && Main.this.configInfo.isErrorUrl(ConfigInfo.TYPE_GAME_URL)) {
					handleData();
		         } else {
		        	 applicationFragment.setUrl(Main.this.configInfo.getGameUrl());
		        	 applicationFragment.flush();
		         }
				break;
			}
			
		}
		
		@Override
		public void closeNetwork() {
			// TODO Auto-generated method stub
			
		}
	};
    
    private void handleData() {
        if(Tools.isAvailableNetWork(this)) {
            Dialog dialog = DialogUtils.getPrgressDialog(this, null, "正在初始化数据...");
            dialog.setCancelable(false);
            dialog.show();
            obtainUniqueId();
        } else {
            initFail(0, "The network is not available!");
            toast.setText("您当前的网络没有连接或不可用！");
            toast.show();
        }
        if(networkReceiver == null) {
            networkReceiver = new NetworkChangeReceiver(networkListener);
            IntentFilter networkFilter = new IntentFilter();
            networkFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            registerReceiver(networkReceiver, networkFilter);
        }
    }
    
    private List<Bitmap> getDianYouAdsFailImage() {
        return Arrays.asList(new Bitmap[] {BitmapFactory.decodeResource(getResources(), R.drawable.pre_default_advert)});
    }
    
    private void initDianYouAds() {
        HolderAdvertFactory factory = (HolderAdvertFactory)AdvertFactory.newInstance(this, HolderAdvertFactory.class);
        dianYouAdvert = (DianYouAdvert)factory.obtainAdvert(AdvertType.HolderType.DianYou, null, true);
        dianYouAdvert.init();
    }
    
    protected void onStart() {
        if(viewPagerAds != null) {
            viewPagerAds.handleOnStart();
        }
        if((currentFragment == 1) && (toolsFragment != null)) {
            toolsFragment.awakeAnimation();
        }
        if((currentFragment == 3) && (indexFragment != null)) {
            indexFragment.awakeAnimation();
        }
        super.onStart();
    }
    
    protected void onStop() {
        if(viewPagerAds != null) {
            viewPagerAds.handleOnStop();
        }
        if((currentFragment == 1) && (toolsFragment != null)) {
            toolsFragment.cancelAnimation();
        }
        if((currentFragment == 3) && (indexFragment != null)) {
            indexFragment.cancelAnimation();
        }
        super.onStop();
    }
    
    private OnClickListener lay_listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.ll_tools_tab:
				getFragment(1);
				break;
				
			case R.id.ll_mail_tab:
				pageActLogBag.addBag(new PageActLogInfo(Tools.getTimeStemp_10(), Constants.OperationLogPage.indiana[0], Constants.OperationLogPage.indiana[1]));
				getFragment(2);
				break;
				
			case R.id.ll_index_tab:
				getFragment(3);
				break;
			case R.id.ll_news_tab:
				pageActLogBag.addBag(new PageActLogInfo(Tools.getTimeStemp_10(), Constants.OperationLogPage.discover[0], Constants.OperationLogPage.discover[1]));
				getFragment(4);
				break;
				
			case R.id.ll_application_tab:
				pageActLogBag.addBag(new PageActLogInfo(Tools.getTimeStemp_10(), Constants.OperationLogPage.application[0], Constants.OperationLogPage.application[1]));
				getFragment(5);
				break;
			}
			
		}
    	
    };
    
    private void initView() {
        adsParent = (ViewGroup)findViewById(R.id.id_ads);
        mToolsLay = (LinearLayout)findViewById(R.id.ll_tools_tab);
        mMailLay = (LinearLayout)findViewById(R.id.ll_mail_tab);
        mIndexLay = (LinearLayout)findViewById(R.id.ll_index_tab);
        mNewsLay = (LinearLayout)findViewById(R.id.ll_news_tab);
        mApplicationLay = (LinearLayout)findViewById(R.id.ll_application_tab);
        mToolsImage = (ImageView)findViewById(R.id.iv_tools_tab);
        mMailImage = (ImageView)findViewById(R.id.iv_mail_tab);
        mIndexImage = (ImageView)findViewById(R.id.iv_index_tab);
        mNewsImage = (ImageView)findViewById(R.id.iv_news_tab);
        mApplicationImage = (ImageView)findViewById(R.id.iv_application_tab);
        mToolsText = (TextView)findViewById(R.id.tv_tools_tab);
        mMailText = (TextView)findViewById(R.id.tv_mail_tab);
        mIndexText = (TextView)findViewById(R.id.tv_index_tab);
        mNewsText = (TextView)findViewById(R.id.tv_news_tab);
        mApplicationText = (TextView)findViewById(R.id.tv_application_tab);
        mToolsLay.setOnClickListener(lay_listener);
        mMailLay.setOnClickListener(lay_listener);
        mIndexLay.setOnClickListener(lay_listener);
        mNewsLay.setOnClickListener(lay_listener);
        mApplicationLay.setOnClickListener(lay_listener);
        getFragment(3);
    }
    
    public void getFragment(int i) {
        if(currentFragment == i) {
            return;
        }
        resetImageAndText();
        FragmentTransaction transaction = manager.beginTransaction();
        hideFragment(transaction);
        if(i == 1) {
            mToolsImage.setImageResource(R.drawable.tools_light);
            mToolsText.setTextColor(getResources().getColor(0x7f08003a));
            if(toolsFragment == null) {
                toolsFragment = new ToolsFragment(listHeadBmp);
                transaction.add(R.id.fl_content, toolsFragment);
                toolsFragment.jumpFragment(new Main.MyCommunication() {
                    
                    public void getResultFragment(int postion) {
                        getFragment(postion);
                    }
                });
            } else {
                transaction.attach(toolsFragment);
                toolsFragment.awakeAnimation();
            }
            onBackListener = null;
            if(!XYZApplication.checkExistsId(this)) {
                handleData();
            }
            setStatusColor(0x0);
        }
        if(i == 2) {
            mMailImage.setImageResource(R.drawable.mail_light);
            mMailText.setTextColor(getResources().getColor(0x7f08003a));
            if(mailFragment == null) {
                mailFragment = new MailFragment(this, configInfo.getShopUrl());
                transaction.add(R.id.fl_content, mailFragment);
            } else {
                transaction.attach(mailFragment);
            }
            onBackListener = mailFragment;
            if((!XYZApplication.checkExistsId(this)) && (configInfo.isErrorUrl(0x1))) {
                handleData();
            }
        }
        if(i == 3) {
            mIndexImage.setImageResource(R.drawable.index_light);
            mIndexText.setTextColor(getResources().getColor(0x7f08003a));
            if(indexFragment == null) {
                indexFragment = new IndexFragment();
                transaction.add(R.id.fl_content, indexFragment);
                indexFragment.jumpFragment(new Main.MyCommunication() {
                    
                    
                    public void getResultFragment(int postion) {
                        getFragment(postion);
                    }
                });
            }
            indexFragment.awakeAnimation();
            transaction.show(indexFragment);
            
            onBackListener = null;
            if(!XYZApplication.checkExistsId(this)) {
                handleData();
            }
            setStatusColor(0);
        }
        if(i == 4) {
            mNewsImage.setImageResource(R.drawable.news_light);
            mNewsText.setTextColor(getResources().getColor(0x7f08003a));
            if(newsFragment == null) {
                newsFragment = new NewsFragment(this, configInfo.getNewUrl());
                transaction.add(R.id.fl_content, newsFragment);
            } else {
                transaction.attach(newsFragment);
            }
            onBackListener = newsFragment;
            if((!XYZApplication.checkExistsId(this)) && (configInfo.isErrorUrl(0x3))) {
                handleData();
            }
        }
        if(i == 5) {
            mApplicationImage.setImageResource(R.drawable.application_light);
            mApplicationText.setTextColor(getResources().getColor(0x7f08003a));
            if(applicationFragment == null) {
                applicationFragment = new ApplicationFragment(this, configInfo.getGameUrl());
                transaction.add(R.id.fl_content, applicationFragment);
            } else {
                transaction.attach(applicationFragment);
            }
            onBackListener = applicationFragment;
            if((!XYZApplication.checkExistsId(this)) && (configInfo.isErrorUrl(0x2))) {
                handleData();
            }
        }
        transaction.commit();
        currentFragment = i;
    }
    
    public void setStatusColor(int color) {
        if(mRootChildView != null) {
            mRootChildView.setBackgroundColor(color);
        }
    }
    
    public void onBackPressed() {
        Log.i("chaochao", "onback.....");
        if((onBackListener != null) && (onBackListener.onBack())) {
            return;
        }
        if(currentFragment != 0x3) {
            getFragment(0x3);
            return;
        }
        super.onBackPressed();
    }
    
    private void hideFragment(FragmentTransaction transaction) {
        stopService(new Intent("com.ywb.action.StartWebViewService"));
        if(toolsFragment != null) {
        	toolsFragment.clearAllAnimation();
            transaction.detach(toolsFragment);
        }
        if(mailFragment != null) {
            transaction.detach(mailFragment);
        }
        if(indexFragment != null) {
            transaction.hide(indexFragment);
            indexFragment.cancelAnimation();
        }
        if(newsFragment != null) {
            transaction.detach(newsFragment);
        }
        if(applicationFragment != null) {
            transaction.detach(applicationFragment);
        }
    }
    
    private void resetImageAndText() {
        mToolsImage.setImageResource(R.drawable.tools_dark);
        mToolsText.setTextColor(getResources().getColor(0x7f08003b));
        mMailImage.setImageResource(R.drawable.mail_dark);
        mMailText.setTextColor(getResources().getColor(0x7f08003b));
        mIndexImage.setImageResource(R.drawable.index_dark);
        mIndexText.setTextColor(getResources().getColor(0x7f08003b));
        mNewsImage.setImageResource(R.drawable.news_dark);
        mNewsText.setTextColor(getResources().getColor(0x7f08003b));
        mApplicationImage.setImageResource(R.drawable.application_dark);
        mApplicationText.setTextColor(getResources().getColor(0x7f08003b));
    }
    
    public interface MyCommunication {
    	void getResultFragment(int postion);
    }
}
