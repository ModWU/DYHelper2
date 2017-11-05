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

	private static final int ACCESS_TOKEN_SPACETIME = 60;// ��λ:��

	private static final int REFRESH_TOKEN_SPACETIME = 24 * 60;// ��λ:���� 1��

	private static final int REFRESH_TOKEN_EXPIRE = 30 * 24 * 60;// ��λ:���� 30��

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
	

	// ��ȡaccess_token
	public void handleAuthorize(IWXAuthorizeListener wxAuthorizeListener) {
		Log.i("INFO", "handleAuthorize");
		WXLoginAuthorize authorizeBean = getAccess_tokenFromSP();
		Log.i("INFO", "authorizeBean:" + authorizeBean);
		if (authorizeBean != null) {

			// �����ж�access_token�Ƿ��Ѿ�ʧЧ
			boolean isValid = isAccessTokenValid(authorizeBean.getAccess_token(), authorizeBean.getOpenid());
			if (!isValid) {
				handleUnableAccessToken(authorizeBean, wxAuthorizeListener);
			} else {
				// 1.��ȡaccess_token���ڵ�ʱ��ֵ
				int expires_in = authorizeBean.getExpires_in();
				Log.i("INFO", "authorizeBean != null---> expires_in:" + expires_in);
				// 2.��ȡ����access_token��ʱ��͵�ǰʱ��
				String nowTimeStr = TimeMgUtils.getTimeStrByMillis(System.currentTimeMillis());
				String lastTimeStr = getLastTime_accessToken(nowTimeStr);
				// 3.�Ƚ�����ʱ���,��λ��s,ʵ�����ǲ���һ��ȥ������
				long timeDistance = TimeMgUtils.getSecondsDistance(nowTimeStr, lastTimeStr);
				// 4.�Ƚ�access_token�Ĺ���ֵ��ʱ���ֵ,����6�뻺��ʱ��
				final int time_cache = 6;
				if (expires_in - time_cache > timeDistance) {
					// �ڹ���ʱ��ֵ�ڣ��Ƿ�ˢ���Ƿ��ӳ�refresh_token��ʱ��
					if (expires_in - ACCESS_TOKEN_SPACETIME <= timeDistance) {
						// ���ʱ�򣬿��Խ���ˢ���ӳ�access_token����������ֵ
						// 1.�ж�RefreshToken�Ƿ����
						boolean isRefreshTokenExpire = isRefreshTokenExpire();

						if (isRefreshTokenExpire) {
							// ���ڣ�������Ȩ
							Log.i("INFO", "���ڣ�������Ȩ��");
							sendLoginRequest();
						} else {
							// û�й��ڣ��ӳ�access_token
							Log.i("INFO", "û�й��ڣ��ӳ�access_token");
							expendAccessTokenExpire(Constants.WeiXin.APP_ID, authorizeBean.getAccess_token(),
									authorizeBean.getRefresh_token(), wxAuthorizeListener);
						}

					} else {
						//û�й��ڣ�ֱ����ԭ��������
						Log.i("INFO", "û�й��ڣ�ֱ����ԭ��������");
						onLoginAuthorizeVisitor(wxAuthorizeListener, true, authorizeBean);
					}
				} else {
					handleUnableAccessToken(authorizeBean, wxAuthorizeListener);
				}
			}

		} else {
			// ����Է���һ�µ�¼����
			// ������ʵ���Ѿ��ٴ�׼�������û�״̬��
			sendLoginRequest();
			Log.i("INFO", "sendLoginRequest");
		}

	}
	
	//����û��������accessToken
	private void handleUnableAccessToken(WXLoginAuthorize authorizeBean, IWXAuthorizeListener wxAuthorizeListener) {
		Log.i("INFO", "access_token����֮��͹���,ֱ��ˢ��һ��");
		// 1.�ж�RefreshToken�Ƿ����
		boolean isRefreshTokenExpire = isRefreshTokenExpire();
		if (isRefreshTokenExpire) {
			// ���ڣ�������Ȩ
			sendLoginRequest();
		} else if(isRefreshTokenNearExpire()) {
			// ������ڹ��ڸ�������ˢ��refreshToken
			expendAccessTokenExpire(Constants.WeiXin.APP_ID, authorizeBean.getAccess_token(),
					authorizeBean.getRefresh_token(), wxAuthorizeListener);
		} else {
			// ����������Ȩ������Ȩ
			sendLoginRequest();
		}
	}

	// ����΢�ŵ�¼����
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
					//��ֹ�û������޸�ʱ�䣬���ʧЧ�����·�����Ȩ����!
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
			// û�й���
			return true;
		} else {
			// �Ѿ�����
			return false;
		}
	}

	private boolean isRefreshTokenNearExpire() {
		String nowTimeStr = TimeMgUtils.getTimeStrByMillis(System.currentTimeMillis());
		String refreshTimeStr = getLastTime_refreshToken(nowTimeStr);
		long timeDistance = TimeMgUtils.getMinutesDistance(nowTimeStr, refreshTimeStr);
		if (REFRESH_TOKEN_EXPIRE - 1 > timeDistance) {
			// REFRESH_TOKEN_SPACETIME REFRESH_TOKEN_EXPIRE
			// �ڹ��ڸ�����Ҳ���ǳ�����29��
			return REFRESH_TOKEN_EXPIRE - REFRESH_TOKEN_SPACETIME <= timeDistance;
		} else {
			// ���ǹ��ڸ���
			return false;
		}
	}

	// �ж�access_token�Ƿ���Ч-->ͬ��
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
				// ������Thread.joinҲ����,���Ǳ��뱣֤���߳�������״̬
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
					// �����µ�access_token->��Щ��Ϣ��ñ��浽���ݿ⣬��ֹ�û�ɾ��
					WXLoginAuthorize wxLoginAuthorize = new WXLoginAuthorize(access_token, expires_in, refresh_token,
							openid, scope);
					saveAccess_token(wxLoginAuthorize);
					// ���浱ǰʱ��
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

	// ��ȡ�û�������Ϣ
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
