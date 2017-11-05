package xyz.monkeytong.hongbao.wxapi;


import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import dyhelper.com.bean.WXLoginAuthorize;
import dyhelper.com.bean.WXUserInfo;
import dyhelper.com.ui.XYZApplication;
import dyhelper.com.util.DialogUtils;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_weixin_entry);
		XYZApplication.api.handleIntent(getIntent(), this);
		
		Log.i("INFO", "WXEntryActivity-->onCreate");
	}
	
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
		XYZApplication.api.handleIntent(intent, this);
	}

	@Override
	public void onReq(BaseReq req) {
		Log.i("INFO", "onReq-->");
	}
	
	//授权认证
	private void doAuthorize(String code, String openId, String state) {
		Log.i("INFO", "doAuthorize-->code:" + code);
		Log.i("INFO", "doAuthorize-->openId:" + openId);
		Log.i("INFO", "doAuthorize-->state:" + state);
		XYZApplication.authorize.obtainNewAccess_token(code, new IWXAuthorizeListener() {
			
			@Override
			public void onLoginAuthorize(boolean isSuccess, WXLoginAuthorize wxLoginAuthorize) {
				if(isSuccess && wxLoginAuthorize != null) {
					Log.i("INFO", "doAuthorize-->" + wxLoginAuthorize);
					
					//设置进度条的信息"正在获取用户信息"
					if(XYZApplication.authorize.authorizeCall != null)
						XYZApplication.authorize.authorizeCall.onObtainUserInfo();
					
					XYZApplication.authorize.obtainUserInfo(wxLoginAuthorize.getAccess_token(), wxLoginAuthorize.getOpenid(), new IWXUserInfoListener() {
						
						@Override
						public void onUserInfo(boolean isSuccess, WXUserInfo userInfo) {
							String message = "";
							if(isSuccess && userInfo != null) {
								//在这里保存用户信息
								message = "获取用户信息成功！";
										
							} else {
								message = "无法获取用户信息!";
							}
							//提醒关闭进度条
							if(XYZApplication.authorize.authorizeCall != null)
								XYZApplication.authorize.authorizeCall.onFinished(userInfo, message);
						}
					});
					
				} else {
					//提醒关闭进度条
					if(XYZApplication.authorize.authorizeCall != null)
						XYZApplication.authorize.authorizeCall.onFinished(null, "授权失败，请重试！");
				}
				
			}
		});
				
		
				
	}

	@Override
	public void onResp(BaseResp resp) {
		
		Log.i("INFO", "onResp-->");
		Log.i("INFO", "Thread onResp:" + Thread.currentThread().toString());
		switch(resp.errCode) {
			case BaseResp.ErrCode.ERR_OK:
				SendAuth.Resp respTmp = (SendAuth.Resp) resp;
				String code = respTmp.code;
				String openId = respTmp.openId;
				String state = respTmp.state;
				if(XYZApplication.authorize.authorizeCall != null)
					XYZApplication.authorize.authorizeCall.onAuthorize();
				doAuthorize(code, openId, state);
				
				break;
			case BaseResp.ErrCode.ERR_AUTH_DENIED:
				Log.i("INFO", "ERR_AUTH_DENIED");
				DialogUtils.closeAllDialog();
				break;
				
			case BaseResp.ErrCode.ERR_BAN:
				Log.i("INFO", "ERR_BAN");
				DialogUtils.closeAllDialog();
				break;
				
			case BaseResp.ErrCode.ERR_COMM:
				Log.i("INFO", "ERR_COMM");
				DialogUtils.closeAllDialog();
				break;
				
			case BaseResp.ErrCode.ERR_SENT_FAILED:
				Log.i("INFO", "ERR_SENT_FAILED");
				DialogUtils.closeAllDialog();
				break;
				
			case BaseResp.ErrCode.ERR_UNSUPPORT:
				Log.i("INFO", "ERR_UNSUPPORT");
				DialogUtils.closeAllDialog();
				break;
			case BaseResp.ErrCode.ERR_USER_CANCEL:
				Log.i("INFO", "ERR_USER_CANCEL");
				DialogUtils.closeAllDialog();
				break;
		}
		
		finish();
	}
	
	@Override
	public void onBackPressed() {
		Log.i("INFO", "weixin activity onBack!");
	}

}
