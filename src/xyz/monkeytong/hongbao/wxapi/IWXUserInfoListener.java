package xyz.monkeytong.hongbao.wxapi;

import dyhelper.com.bean.WXUserInfo;

public interface IWXUserInfoListener {
	void onUserInfo(boolean isSuccess, WXUserInfo userInfo);
}
