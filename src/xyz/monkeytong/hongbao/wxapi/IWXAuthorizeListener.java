package xyz.monkeytong.hongbao.wxapi;

import dyhelper.com.bean.WXLoginAuthorize;

public interface IWXAuthorizeListener {
	void onLoginAuthorize(boolean isSuccess, WXLoginAuthorize wxLoginAuthorize);
}
