package xyz.monkeytong.hongbao.wxapi;

import org.json.JSONArray;
import org.json.JSONObject;

import com.tencent.mm.sdk.modelmsg.SendAuth;

import android.content.Context;
import android.util.Log;
import cn.dianyou.nets.DYHttpUtils;
import cn.dianyou.nets.DYStringCallback;
import cn.dianyou.utils.SharedPreferencesUtils;
import cn.dianyou.utils.TimeMgUtils;
import dyhelper.com.bean.WXLoginAuthorize;
import dyhelper.com.bean.WXUserInfo;
import dyhelper.com.ui.XYZApplication;
import dyhelper.com.util.Constants;
import okhttp3.Call;
import okhttp3.Response;

public class WXAuthorize {

	private static final String LAST_TIME_ACCESS_TOKEN = "last_time_access_token";

	private static final String LAST_TIME_REFRESH_TOKEN = "last_time_refresh_token";

	private static final int ACCESS_TOKEN_SPACETIME = 60;// 单位:秒

	private static final int REFRESH_TOKEN_SPACETIME = 24 * 60;// 单位:分钟 1天

	private static final int REFRESH_TOKEN_EXPIRE = 30 * 24 * 60;// 单位:分钟 30天

	private static final String FILENAME_WX_AUTHORIZE = "filename_wx_authorize";

	private Context context;
	
	public IAuthorizeCall authorizeCall;
	
	public void setAuthorizeCall(IAuthorizeCall authorizeCall) {
		this.authorizeCall = authorizeCall;
	}
	
	public interface IAuthorizeCall {
		void onNewLoginRequest();
		void onAuthorize();
		void onObtainUserInfo();
		void onFinished(WXUserInfo userInfo, String message);
		
	}

	public WXAuthorize(Context context) {
		this.context = context;
	}
	
	private void loginRequestListenerProxy(boolean isNew) {
		if(authorizeCall != null) {
			if(isNew) {
				authorizeCall.onNewLoginRequest();
			} else {
			}
		}
	}
	

	// 获取access_token
	public void handleAuthorize(IWXAuthorizeListener wxAuthorizeListener) {
		Log.i("INFO", "handleAuthorize");
		WXLoginAuthorize authorizeBean = getAccess_tokenFromSP();
		Log.i("INFO", "authorizeBean:" + authorizeBean);
		if (authorizeBean != null) {

			// 正真判断access_token是否已经失效
			boolean isValid = isAccessTokenValid(authorizeBean.getAccess_token(), authorizeBean.getOpenid());
			if (!isValid) {
				handleUnableAccessToken(authorizeBean, wxAuthorizeListener);
			} else {
				// 1.获取access_token过期的时间值
				int expires_in = authorizeBean.getExpires_in();
				Log.i("INFO", "authorizeBean != null---> expires_in:" + expires_in);
				// 2.获取保存access_token的时间和当前时间
				String nowTimeStr = TimeMgUtils.getTimeStrByMillis(System.currentTimeMillis());
				String lastTimeStr = getLastTime_accessToken(nowTimeStr);
				// 3.比较两个时间差,单位是s,实际上是参数一减去参数二
				long timeDistance = TimeMgUtils.getSecondsDistance(nowTimeStr, lastTimeStr);
				// 4.比较access_token的过期值和时间差值,并给6秒缓冲时间
				final int time_cache = 6;
				if (expires_in - time_cache > timeDistance) {
					// 在过期时间值内，是否刷新是否延长refresh_token的时间
					if (expires_in - ACCESS_TOKEN_SPACETIME <= timeDistance) {
						// 这个时候，可以进行刷新延长access_token的生命过期值
						// 1.判断RefreshToken是否过期
						boolean isRefreshTokenExpire = isRefreshTokenExpire();

						if (isRefreshTokenExpire) {
							// 过期，重新授权
							Log.i("INFO", "过期，重新授权！");
							sendLoginRequest();
						} else {
							// 没有过期，延长access_token
							Log.i("INFO", "没有过期，延长access_token");
							expendAccessTokenExpire(Constants.WeiXin.APP_ID, authorizeBean.getAccess_token(),
									authorizeBean.getRefresh_token(), wxAuthorizeListener);
						}

					} else {
						//没有过期，直接用原来的数据
						Log.i("INFO", "没有过期，直接用原来的数据");
						onLoginAuthorizeVisitor(wxAuthorizeListener, true, authorizeBean);
					}
				} else {
					handleUnableAccessToken(authorizeBean, wxAuthorizeListener);
				}
			}

		} else {
			// 最后尝试发送一下登录请求
			// 这里其实就已经再次准备更新用户状态了
			sendLoginRequest();
			Log.i("INFO", "sendLoginRequest");
		}

	}
	
	//处理没有能力的accessToken
	private void handleUnableAccessToken(WXLoginAuthorize authorizeBean, IWXAuthorizeListener wxAuthorizeListener) {
		Log.i("INFO", "access_token几秒之后就过期,直接刷新一下");
		// 1.判断RefreshToken是否过期
		boolean isRefreshTokenExpire = isRefreshTokenExpire();
		if (isRefreshTokenExpire) {
			// 过期，重新授权
			sendLoginRequest();
		} else if(isRefreshTokenNearExpire()) {
			// 如果是在过期附近，则刷新refreshToken
			expendAccessTokenExpire(Constants.WeiXin.APP_ID, authorizeBean.getAccess_token(),
					authorizeBean.getRefresh_token(), wxAuthorizeListener);
		} else {
			// 还是重新授权重新授权
			sendLoginRequest();
		}
	}

	// 发送微信登录请求
	private boolean sendLoginRequest() {
		loginRequestListenerProxy(true);
		Log.i("INFO", "sendLoginRequest");
		final SendAuth.Req req = new SendAuth.Req();
		req.scope = "snsapi_userinfo";
		req.state = "carjob_wx_login";
		boolean isReSu = XYZApplication.api.sendReq(req);
		Log.i("INFO", "isReSu: " + isReSu);
		return isReSu;
	}

	private void expendAccessTokenExpire(String appid, String access_token, String refresh_token,
			IWXAuthorizeListener wxAuthorizeListener) {

		final String tmepAccess_token = access_token;
		final IWXAuthorizeListener tempWXAuthorizeListener = wxAuthorizeListener;

		String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=" + appid
				+ "&grant_type=refresh_token&refresh_token=" + refresh_token;

		DYHttpUtils.getInstance().get().url(url).build().execute(new DYStringCallback() {

			@Override
			public void onResponse(String data, int arg1) {
				try {
					JSONObject jsonData = new JSONObject(data);
					String access_token_ = jsonData.getString("access_token");
					int expires_in = jsonData.getInt("expires_in");
					String refresh_token_ = jsonData.getString("refresh_token");
					String openid = jsonData.getString("openid");
					String scope = jsonData.getString("scope");
					if (!tmepAccess_token.equals(access_token_))
						saveLastTime_accessToken();
					if (!tmepAccess_token.equals(refresh_token_))
						saveLastTime_refreshToken();

					WXLoginAuthorize wxLoginAuthorize = new WXLoginAuthorize(access_token_, expires_in, refresh_token_,
							openid, scope);
					saveAccess_token(wxLoginAuthorize);
					Log.i("INFO", "expendAccessTokenExpire success!");
					Log.i("INFO", "expendAccessTokenExpire access_token:" + access_token_);
					Log.i("INFO", "expendAccessTokenExpire refresh_token:" + refresh_token_);
					Log.i("INFO", "expendAccessTokenExpire openid:" + openid);

					onLoginAuthorizeVisitor(tempWXAuthorizeListener, true, wxLoginAuthorize);

				} catch (Exception e) {
					Log.i("INFO", "expendAccessTokenExpire ex:" + e.toString());
					onLoginAuthorizeVisitor(tempWXAuthorizeListener, false, null);
					//防止用户恶意修改时间，如果失效，重新发送授权请求!
					sendLoginRequest();
				}
			}

			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				Log.i("INFO", "expendAccessTokenExpire request ex:" + arg1);
				onLoginAuthorizeVisitor(tempWXAuthorizeListener, false, null);
			}
		});

	}

	private boolean isRefreshTokenExpire() {
		String nowTimeStr = TimeMgUtils.getTimeStrByMillis(System.currentTimeMillis());
		String refreshTimeStr = getLastTime_refreshToken(nowTimeStr);
		long timeDistance = TimeMgUtils.getMinutesDistance(nowTimeStr, refreshTimeStr);
		if (REFRESH_TOKEN_EXPIRE - 1 > timeDistance) {
			// 没有过期
			return true;
		} else {
			// 已经过期
			return false;
		}
	}

	private boolean isRefreshTokenNearExpire() {
		String nowTimeStr = TimeMgUtils.getTimeStrByMillis(System.currentTimeMillis());
		String refreshTimeStr = getLastTime_refreshToken(nowTimeStr);
		long timeDistance = TimeMgUtils.getMinutesDistance(nowTimeStr, refreshTimeStr);
		if (REFRESH_TOKEN_EXPIRE - 1 > timeDistance) {
			// REFRESH_TOKEN_SPACETIME REFRESH_TOKEN_EXPIRE
			// 在过期附近，也就是超过了29天
			return REFRESH_TOKEN_EXPIRE - REFRESH_TOKEN_SPACETIME <= timeDistance;
		} else {
			// 不是过期附近
			return false;
		}
	}

	// 判断access_token是否有效-->同步
	private boolean isAccessTokenValid(String access_token, String openid) {
		final String url = "https://api.weixin.qq.com/sns/auth?access_token=" + access_token + "&openid=" + openid;
		final boolean[] isValid = new boolean[] { false, false };
		try {
			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Response response = DYHttpUtils.getInstance().get().url(url).build().execute();
						if (response.isSuccessful()) {
							int errcode = new JSONObject(response.body().string()).optInt("errcode");
							Log.i("INFO", "isAccessTokenValid-->errcode: " + errcode);
							isValid[0] = (errcode == 0);
						} else
							Log.i("INFO", "isAccessTokenValid request fail!");
					} catch (Exception e) {
					} finally {
						synchronized (isValid) {
							isValid[1] = true;
							isValid.notify();
						}
					}

				}

			}).start();

			synchronized (isValid) {
				// 或者用Thread.join也可以,但是必须保证子线程是启动状态
				if (!isValid[1])
					isValid.wait();
				Log.i("INFO", "isAccessTokenValid: wait finished--> isValid=" + isValid[0]);
			}

		} catch (Exception e) {
			Log.i("INFO", "isAccessTokenValid ex:" + e.toString());
		}
		return isValid[0];
	}

	public void obtainNewAccess_token(String code, IWXAuthorizeListener wxAuthorizeListener) {
		final IWXAuthorizeListener tempWxAuthorizeListener = wxAuthorizeListener;
		String url = "https://api.weixin.qq.com/sns/oauth2/access_token" + "?appid=" + Constants.WeiXin.APP_ID
				+ "&secret=" + Constants.WeiXin.APP_SECRET + "&code=" + code + "&grant_type=authorization_code";
		Log.i("INFO", "obtainNewAccess_token->" + url);
		DYHttpUtils.getInstance().get().url(url).build().execute(new DYStringCallback() {

			@Override
			public void onResponse(String data, int arg1) {
				try {
					JSONObject jsonData = new JSONObject(data);
					String access_token = jsonData.optString("access_token");
					int expires_in = jsonData.getInt("expires_in");
					String refresh_token = jsonData.getString("refresh_token");
					String openid = jsonData.getString("openid");
					String scope = jsonData.getString("scope");
					Log.i("INFO", "refresh_token:" + refresh_token);
					// 保存新的access_token->这些信息最好保存到数据库，防止用户删除
					WXLoginAuthorize wxLoginAuthorize = new WXLoginAuthorize(access_token, expires_in, refresh_token,
							openid, scope);
					saveAccess_token(wxLoginAuthorize);
					// 保存当前时间
					saveLastTime_accessToken();
					saveLastTime_refreshToken();
					
					onLoginAuthorizeVisitor(tempWxAuthorizeListener, true, wxLoginAuthorize);
				} catch (Exception e) {
					Log.i("INFO", "obtainNewAccess_token json ex:" + e.toString());
					onLoginAuthorizeVisitor(tempWxAuthorizeListener, false, null);
				}
			}

			@Override
			public void onError(Call arg0, Exception arg1, int arg2) {
				Log.i("INFO", "obtainNewAccess_token request ex:" + arg1);
				onLoginAuthorizeVisitor(tempWxAuthorizeListener, false, null);
			}
		});
	}
	
	private void onLoginAuthorizeVisitor(IWXAuthorizeListener wxAuthorizeListener, boolean flag, WXLoginAuthorize wxLoginAuthorize) {
		final IWXAuthorizeListener tempWxAuthorizeListener = wxAuthorizeListener;
		final WXLoginAuthorize tempWXLoginAuthorize = wxLoginAuthorize;
		final boolean tempFlag = flag;
		if(tempWxAuthorizeListener != null) {
			Log.i("INFO", "onLoginAuthorizeVisitor-->onLoginAuthorize");
			tempWxAuthorizeListener.onLoginAuthorize(tempFlag, tempWXLoginAuthorize);
		}
	}
	
	private void onUserInfoVisitor(IWXUserInfoListener wxUserInfoListener, boolean flag, WXUserInfo wxUserInfo) {
		final IWXUserInfoListener tempWxUserInfoListener = wxUserInfoListener;
		final WXUserInfo tempWXUserInfo = wxUserInfo;
		final boolean tempFlag = flag;
		if(tempWxUserInfoListener != null) {
			tempWxUserInfoListener.onUserInfo(tempFlag, tempWXUserInfo);
		}
	}

	private void saveLastTime_accessToken() {
		SharedPreferencesUtils.newInstance(context, FILENAME_WX_AUTHORIZE)
				.saveString(LAST_TIME_ACCESS_TOKEN, TimeMgUtils.getTimeStrByMillis(System.currentTimeMillis()))
				.commit();
	}

	private String getLastTime_accessToken(String defTime) {
		return SharedPreferencesUtils.newInstance(context, FILENAME_WX_AUTHORIZE).getString(LAST_TIME_ACCESS_TOKEN,
				defTime);
	}

	private void saveLastTime_refreshToken() {
		SharedPreferencesUtils.newInstance(context, FILENAME_WX_AUTHORIZE)
				.saveString(LAST_TIME_REFRESH_TOKEN, TimeMgUtils.getTimeStrByMillis(System.currentTimeMillis()))
				.commit();
	}

	private String getLastTime_refreshToken(String defTime) {
		return SharedPreferencesUtils.newInstance(context, FILENAME_WX_AUTHORIZE).getString(LAST_TIME_REFRESH_TOKEN,
				defTime);
	}

	private void saveAccess_token(WXLoginAuthorize wxLoginAuthorize) {
		SharedPreferencesUtils.newInstance(context, FILENAME_WX_AUTHORIZE)
				.saveString(Constants.Keys.WEIXIN_ACCESS_TOKEN_KEY, wxLoginAuthorize.getAccess_token())
				.saveString(Constants.Keys.WEIXIN_OPEN_ID_KEY, wxLoginAuthorize.getOpenid())
				.saveString(Constants.Keys.WEIXIN_REFRESH_TOKEN_KEY, wxLoginAuthorize.getRefresh_token())
				.saveInt(Constants.Keys.WEIXIN_EXPIRES_IN_KEY, wxLoginAuthorize.getExpires_in())
				.saveString(Constants.Keys.WEIXIN_SCOPE, wxLoginAuthorize.getScope()).commit();

	}

	private WXLoginAuthorize getAccess_tokenFromSP() {
		final SharedPreferencesUtils spu = SharedPreferencesUtils.newInstance(context, FILENAME_WX_AUTHORIZE);
		String access_token = spu.getString(Constants.Keys.WEIXIN_ACCESS_TOKEN_KEY, null);
		if (access_token != null) {
			String openid = spu.getString(Constants.Keys.WEIXIN_OPEN_ID_KEY, null);
			String refresh_token = spu.getString(Constants.Keys.WEIXIN_REFRESH_TOKEN_KEY, null);
			int expires_in = spu.getInt(Constants.Keys.WEIXIN_EXPIRES_IN_KEY, -1);
			String scope = spu.getString(Constants.Keys.WEIXIN_SCOPE, null);
			return new WXLoginAuthorize(access_token, expires_in, refresh_token, openid, scope);
		} else
			return null;
	}

	// 获取用户个人信息
	public void obtainUserInfo(String access_token, String openid, IWXUserInfoListener wxUserInfoListener) {

		final IWXUserInfoListener tempWxUserInfoListener = wxUserInfoListener;

		String url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + access_token + "&openid=" + openid;

		DYHttpUtils.getInstance().get().url(url).build().execute(new DYStringCallback() {

			@Override
			public void onResponse(String data, int arg1) {
				try {
					JSONObject jsonData = new JSONObject(data);
					String openid_ = jsonData.getString("openid");
					String nickname = jsonData.getString("nickname");
					int sex = jsonData.getInt("sex");
					String province = jsonData.getString("province");
					String city = jsonData.getString("city");
					String country = jsonData.getString("country");
					String headimgurl = jsonData.getString("headimgurl");
					JSONArray jsonArray = jsonData.getJSONArray("privilege");
					String[] privilege = new String[0];
					if (jsonArray.length() > 0) {
						privilege = new String[jsonArray.length()];
						for (int i = 0; i < jsonArray.length(); i++) {
							String p = jsonArray.getString(i);
							privilege[i] = p;
						}
					}
					
					Log.i("INFO", "user info - Thread:" + Thread.currentThread().toString());
					
					String unionid = jsonData.getString("unionid");

					WXUserInfo wxUserInfo = new WXUserInfo(openid_, nickname, sex, province, city, country, headimgurl,
							privilege, unionid);
					
					onUserInfoVisitor(tempWxUserInfoListener, true, wxUserInfo);
					
				} catch (Exception e) {
					Log.i("INFO", "obtainUserInfo json ex:" + e.toString());
					
					onUserInfoVisitor(tempWxUserInfoListener, false, null);
					
				}
			}

			@Override
			public void onError(Call arg0, Exception ex, int arg2) {
				Log.i("INFO", "obtainUserInfo request ex:" + ex.toString());
				onUserInfoVisitor(tempWxUserInfoListener, false, null);
			}
		});

	}
}
