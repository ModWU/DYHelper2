/**
  * Generated by smali2java 1.0.0.558
  * Copyright (C) 2013 Hensence.com
  */

package dyhelper.com.ui;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import cn.dianyou.nets.DYBitmapCallback;
import cn.dianyou.nets.DYHttpUtils;
import cn.dianyou.nets.DYStringCallback;
import cn.dianyou.utils.FileManageUtils;
import dyhelper.com.bean.WXLoginAuthorize;
import dyhelper.com.bean.WXUserInfo;
import dyhelper.com.broadcasts.HomeKeyReceiver;
import dyhelper.com.util.ActivityCollector;
import dyhelper.com.util.ActivityCollectorUtils;
import dyhelper.com.util.Constants;
import dyhelper.com.util.DialogUtils;
import okhttp3.Call;
import xyz.monkeytong.hongbao.R;
import xyz.monkeytong.hongbao.wxapi.IWXAuthorizeListener;
import xyz.monkeytong.hongbao.wxapi.IWXUserInfoListener;
import xyz.monkeytong.hongbao.wxapi.WXAuthorize;

public class Login extends Activity {
    private int activityCloseEnterAnimation;
    private int activityCloseExitAnimation;
    private Button btn_login_phone;
    private Button btn_login_weixin;
    private HomeKeyReceiver homeKeyReceiver;
    private Toast toast;
    
    private HomeKeyReceiver.OnHomeKeyListener onHomeKeyListener = new HomeKeyReceiver.OnHomeKeyListener()
    {
      public void onClickHome()
      {
        DialogUtils.closeAllDialog();
      }

      public void onLongClickHome()
      {
      }
    };
    
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ActivityCollector.addActivity(this);
        initViews();
        initEvents();
        int[] activityAniValues = Main.obtrainExitAnims(this);
        activityCloseEnterAnimation = activityAniValues[0];
        activityCloseExitAnimation = activityAniValues[1];
        ActivityCollectorUtils.addActivity("dianyou_activity_login", this);
        toast = Toast.makeText(this, "", 0);
        if(homeKeyReceiver == null) {
            homeKeyReceiver = new HomeKeyReceiver(onHomeKeyListener);
            IntentFilter filter = new IntentFilter("android.intent.action.CLOSE_SYSTEM_DIALOGS");
            registerReceiver(homeKeyReceiver, filter);
        }
    }
    
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
        if(homeKeyReceiver != null) {
            unregisterReceiver(homeKeyReceiver);
            homeKeyReceiver = null;
        }
    }
    
    protected void onStart() {
        super.onStart();
    }
    
    private DialogInterface.OnClickListener isExitlistener = new DialogInterface.OnClickListener()
    {

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				// ȷ��
				ActivityCollector.finishAll();
				break;
			case AlertDialog.BUTTON_NEGATIVE:

				break;
			default:
				break;
			}
		}
      
    };
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == 0x4) {
            AlertDialog isExit = new AlertDialog.Builder(this).create();
            isExit.setTitle("ϵͳ��ʾ");
            isExit.setMessage("��ȷ���˳���");
            isExit.setButton("ȷ��", isExitlistener);
            isExit.setButton2("ȡ��", isExitlistener);
            isExit.show();
        }
        return false;
    }
    
    private void initViews() {
        btn_login_weixin = (Button)findViewById(R.id.btn_login_weixin);
        btn_login_phone = (Button)findViewById(R.id.btn_login_phone);
    }
    
    private OnClickListener onLoginClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch(v.getId()) {
			case R.id.btn_login_phone:
				loginByPhone();
				break;
				
			case R.id.btn_login_weixin:
				loginByWeixin();
				break;
			}
		}
    	
    };
    
    private void initEvents() {
        btn_login_weixin.setOnClickListener(onLoginClickListener);
        btn_login_phone.setOnClickListener(onLoginClickListener);
    }
    
    public void onStop() {
        super.onStop();
        DialogUtils.closeDialog(this);
        Log.i("INFO", "login onstop");
    }
    
    private void loginByPhone() {
        DialogUtils.showDialog(this, null, "���Ժ�...", false);
        Log.i("INFO", "loginByPhone");
        if(!XYZApplication.checkUniqueId(this)) {
            Log.i("INFO", "It has not a guid for user, and can\'t login by phone.");
            Log.i("INFO", "Again try obtain unique user id....");
            XYZApplication.authorize.setAuthorizeCall(null);
            tryAgainObtainUniqueId(new XYZApplication.SimpleOnInitListener() {
                
                public void uniqueIdSuccess(String uniqueId, String uniqueIdType) {
                    Log.i("INFO", "Again try obtain unique user id success!");
                }
                
                public void uniqueIdFail(String uniqueId, String uniqueIdType) {
                    if(uniqueId != null) {
                        Log.i("INFO", "Again try obtain unique user id success!");
                        authorizeForPhone();
                    } else {
                    	Log.i("INFO", "Again try obtain unique user id fail!");
                        toast.setText("����ǰ������û�����ӻ򲻿���!");
                        toast.show();
                    }
                    DialogUtils.closeDialog(Login.this);
                }
            });
            return;
        }
        authorizeForPhone();
    }
    
    private void authorizeForPhone() {
        Log.i("INFO", "authorizeForPhone");
        String id = obtainUniqueIdByUTF_8();
        Intent itent = new Intent(this, PhoneVerification.class);
        itent.putExtra("userId", id);
        startActivity(itent);
        DialogUtils.closeDialog(this);
    }
    
    private void tryAgainObtainUniqueId(XYZApplication.IOnInitListener listener) {
        XYZApplication.obtainUniqueIdByPhoneInfo(this, listener);
    }
    
    private void loginByWeixin() {
        DialogUtils.showDialog(this, null, "���Ժ�...", false);
        if(XYZApplication.api.isWXAppInstalled()) {
            Log.i("INFO", "loginWeixin");
            if(!XYZApplication.checkUniqueId(this)) {
                Log.i("INFO", "It has not a guid for user, and can\'t login weixin.");
                Log.i("INFO", "Again try obtain unique user id....");
                tryAgainObtainUniqueId(new XYZApplication.SimpleOnInitListener() {
                    
                    public void uniqueIdSuccess(String uniqueId, String uniqueIdType) {
                        Log.i("INFO", "Again try obtain unique user id success!");
                    }
                    
                    public void uniqueIdFail(String uniqueId, String uniqueIdType) {
                        if(uniqueId != null) {
                            Log.i("INFO", "Again try obtain unique user id success!");
                            authorizeForWeiXin();
                        } else {
                        	 Log.i("INFO", "Again try obtain unique user id fail!");
                             toast.setText("����ǰ������û�����ӻ򲻿���!");
                             toast.show();
                        }
                       
                        DialogUtils.closeDialog(Login.this);
                    }
                });
                return;
            }
            authorizeForWeiXin();
            return;
        }
        toast.setText("�����ֻ���δ��װ΢�ţ����Ȱ�װ΢��");
        toast.show();
        DialogUtils.closeDialog(this);
    }
    
    private void authorizeForWeiXin() {
        XYZApplication.authorize.setAuthorizeCall(new WXAuthorize.IAuthorizeCall() {
        	
            public void onObtainUserInfo() {
                DialogUtils.showDialog(Login.this, null, "���ڻ�ȡ�û���Ϣ...", false);
            }
            
            public void onAuthorize() {
                DialogUtils.showDialog(Login.this, null, "������Ȩ...", false);
            }
            
            public void onFinished(WXUserInfo userInfo, String message) {
            	commitWeiXinData(userInfo, "��ȡ�û���Ϣ�ɹ���");
            }
            
            public void onNewLoginRequest() {
                DialogUtils.showDialog(Login.this, null, "���ڷ���΢�ŵ�¼����...", false);
            }
        });
        XYZApplication.authorize.handleAuthorize(new IWXAuthorizeListener() {
            
            public void onLoginAuthorize(boolean isSuccess, WXLoginAuthorize wxLoginAuthorize) {
                if((isSuccess) && (wxLoginAuthorize != null)) {
                    XYZApplication.authorize.obtainUserInfo(wxLoginAuthorize.getAccess_token(), wxLoginAuthorize.getOpenid(), new IWXUserInfoListener() {
                        
                        public void onUserInfo(boolean isSuccess, WXUserInfo userInfo) {
                            if((isSuccess) && (userInfo != null)) {
                            	commitWeiXinData(userInfo, "��ȡ�û���Ϣ�ɹ���");
                            } else {
                            	commitWeiXinData(null, "��ȡ�û���Ϣʧ�ܣ�");
                            }
                        }
                    });
                    return;
                }
                toast.setText("\u6709\u53ef\u80fdexpendAccessTokenExpire\u5931\u8d25");
                toast.show();
                DialogUtils.closeDialog(Login.this);
            }
        });
    }
    
    private void commitWeiXinData(WXUserInfo userInfo, String message) {
        WXUserInfo tempUserInfo = userInfo;
        final WXUserInfo finalUserInfo = new WXUserInfo(userInfo);
        if(tempUserInfo == null) {
            DialogUtils.closeDialog(this);
            toast.setText(message);
            toast.show();
            return;
        }
        
        DialogUtils.showDialog(this, null, "�����ύ����...", false);
        String GuId = obtainUniqueIdByUTF_8();
        tempUserInfo.urlEncoding("UTF-8");
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("openid", tempUserInfo.getOpenid());
        params.put("nickname", tempUserInfo.getNickname());
        params.put("sex", tempUserInfo.getSex() + "");
        params.put("province", tempUserInfo.getProvince());
        params.put("city", tempUserInfo.getCity());
        params.put("country", tempUserInfo.getCountry());
        params.put("headimgurl", tempUserInfo.getHeadimgurl());
        params.put("unionid", tempUserInfo.getUnionid());
        params.put("GuId", GuId);
        DYHttpUtils.getInstance().post().url(Constants.Dianyou_url.URL_WEIXIN).paramJSONType().params(params).build().execute(new DYStringCallback() {
            
            
            public void onResponse(String data, int id) {
                Log.i("INFO", data);
                try {
                    data = URLDecoder.decode(data, "UTF-8");
                    JSONObject jsonObj = new JSONObject(data);
                    int code = jsonObj.optInt("Code", -0x1);
                    if(code == 0) {
                        final String weixinId = jsonObj.getString("Id");
                        final String weixinType = jsonObj.getString("Type");
                        XYZApplication.saveInfo(Login.this, Constants.Login.LOGIN_WEIXIN_ID, weixinId);
                        XYZApplication.saveInfo(Login.this, Constants.Login.LOGIN_WEIXIN_ID_TYPE, weixinType);
                        XYZApplication.saveInfo(Login.this, Constants.Login.LOGIN_CURRENT_ID, weixinId);
                        XYZApplication.saveInfo(Login.this, Constants.Login.LOGIN_CURRENT_ID_TYPE, weixinType);
                        XYZApplication.saveLoginState(Login.this, true);
                        XYZApplication.saveInfo(Login.this, Constants.UserInfo.CURRENT_USER_NAME, finalUserInfo.getNickname());
                        DYHttpUtils.getInstance().get().url(finalUserInfo.getHeadimgurl()).build().execute(new DYBitmapCallback() {
                            
                            public void onError(Call arg0, Exception arg1, int arg2) {
                            	Login.this.jumpToUserCenter(weixinType, null);
                            }
                            
                            public void onResponse(Bitmap bmp, int id) {
                                String filename = FileManageUtils.getFileName(finalUserInfo.getHeadimgurl());
                                String filepath = FileManageUtils.isExistsFile_p2(Login.this, Constants.FileInfo.CURRENT_SIMPLE_ICON_DIR, filename);
                                if(filepath == null) {
                                    filepath = FileManageUtils.saveBitmap(Login.this, bmp, Constants.FileInfo.CURRENT_SIMPLE_ICON_DIR, filename);
                                }
                                if(filepath != null) {
                                    XYZApplication.saveInfo(Login.this, Constants.UserInfo.CURRENT_USER_ICON_STR, filepath);
                                }
                                
                                Login.this.jumpToUserCenter(weixinType, bmp);
                            }
                        });
                    } else {
                        DialogUtils.closeDialog(Login.this);
                        toast.setText("������:" + code + ",��¼ʧ��!");
                        toast.show();
                    }
                } catch(Exception e) {
                    DialogUtils.closeDialog(Login.this);
                    toast.setText(e.getMessage());
                    toast.show();
                    Log.i("INFO", e.toString());
                }
                Log.i("INFO", data);
            }

			@Override
			public void onError(Call call, Exception e, int id) {
				DialogUtils.closeDialog(Login.this);
                toast.setText(e.getMessage());
                toast.show();
                Log.i("INFO", e.toString());
			}
        });
    }
    
    private void jumpToUserCenter(String type, Bitmap bmp) {
        DialogUtils.closeDialog(this);
        toast.setText("��¼�ɹ�!");
        toast.show();
        Intent it = new Intent(this, UserCenter.class);
        startActivity(it);
        ActivityCollectorUtils.finishAndClear(Constants.Activitys.ACTIVITY_LOGIN);
    }
    
    public void loginBack(View v) {
        onBackPressed();
        overridePendingTransition(activityCloseEnterAnimation, activityCloseExitAnimation);
    }
    
    private String obtainUniqueIdByUTF_8() {
    	String id = XYZApplication.obtainStringInfo(this, Constants.Keys.DIANYOU_UNIQUE_ID);
		try {
			id = URLEncoder.encode(id, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.i("INFO", "uniqueId����ʧ��:UTF-8");
		}
		return id;
    }
    
    public void finish() {
        super.finish();
    }
    
    public void onBackPressed() {
        if((DialogUtils.getDialog(this) != null) && (DialogUtils.getDialog(this).isShowing())) {
            return;
        }
        super.onBackPressed();
    }
}