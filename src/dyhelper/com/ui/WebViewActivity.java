package dyhelper.com.ui;


import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieSyncManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.dianyou.broadcasts.NetworkChangeReceiver;
import cn.dianyou.broadcasts.NetworkChangeReceiver.IOnNetworkChangeListener;
import cn.dianyou.utils.OtherUtils;
import cn.dianyou.utils.ResourceUtils;
import cn.dianyou.views.ProgressWebView;


public class WebViewActivity extends Activity {
	
	private ProgressWebView webview;
	
	private TextView webview_bar;
	
	private LinearLayout webViewLay;
	
	private NetworkChangeReceiver networkReceiver;
	
	private String webViewUrl = "https://www.baidu.com", webViewTitle = "网页";
	
	public static final String WEBVIEW_URL = "dianyou_advert_webview_url";
	public static final String WEBVIEW_TITLE = "dianyou_advert_webview_title";
	
	private static final String LAYOUT_NAME = "dianyou_advert_activity_webview";
	private static final String WIEW_BAR_RES_NAME = "dianyou_advert_webviewbar_style";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		int layoutId = ResourceUtils.getLayoutId(this, LAYOUT_NAME);
		if(layoutId == 0) {
			Log.i("INFO", "Not find the resource id of layout \"" + LAYOUT_NAME + "\".");
			Toast.makeText(this, "webview的布局文件没有找到,请放入到您的工程中!", Toast.LENGTH_LONG).show();
			finish();
		} else {
			setContentView(layoutId);
			initViews();
			setData();
			setWebView();
		}
		
		networkReceiver = new NetworkChangeReceiver(listener);
		IntentFilter networkFilter = new IntentFilter();
		networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(networkReceiver, networkFilter);
	}
	
	

	private void setData() {
		Intent intent = getIntent();
		
		String url = intent.getStringExtra(WEBVIEW_URL);
		String title = intent.getStringExtra(WEBVIEW_TITLE);
		if(url != null)
			webViewUrl = url;
		
		if(title != null)
			webViewTitle = title;
		
		
		webview_bar.setText(webViewTitle);
	}
	@TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
	private void setWebView() {
		WebSettings webSettings = webview.getSettings();
		webSettings.setBuiltInZoomControls(false);
		webSettings.setJavaScriptEnabled(true);
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR_MR1)
			webSettings.setDomStorageEnabled(true);
		webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
		
		webview.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}
			

			@Override
			public void onPageFinished(WebView view, String url) {
				CookieSyncManager.getInstance().sync();
				
			}
		});
		
		webview.setDownloadListener(new com.tencent.smtt.sdk.DownloadListener() {
			
			@Override
			public void onDownloadStart(String url, String arg1, String arg2, String arg3, long arg4) {
				Log.i("chaochao", "arg:" + url);
				Log.i("chaochao", "arg1:" + arg1);
				Log.i("chaochao", "arg2:" + arg2);
				Log.i("chaochao", "arg3:" + arg3);
				Log.i("chaochao", "arg4:" + arg4);
				if (url != null && url.startsWith("http://"))
	                   startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
				
			}
		});
		/*webview.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
                if (url != null && url.startsWith("http://"))
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });*/
		
		webview.loadUrl(webViewUrl);
		
	}
	

	private void initViews() {
		int webviewbarId = ResourceUtils.getId(this, "webview_bar");
		int webviewlayid = ResourceUtils.getId(this, "id_webview_lay");
		int webviewStyleId = ResourceUtils.getDrawableId(this, WIEW_BAR_RES_NAME);
		webview_bar = (TextView) findViewById(webviewbarId);
		
		webViewLay = (LinearLayout) findViewById(webviewlayid);
		webview = new ProgressWebView(getApplicationContext());
		LinearLayout.LayoutParams webview_lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
		webview.setLayoutParams(webview_lp);
		if(webviewStyleId != 0)
			webview.setBackgroundResource(webviewStyleId);
		webViewLay.addView(webview);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			switch (keyCode) {
			case KeyEvent.KEYCODE_BACK:
				if (webview.canGoBack()) {
					webview.goBack();
				} else {
					finish();
				}
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	@TargetApi(Build.VERSION_CODES.ECLAIR)
	public void performBack(View v) {
		if (webview.canGoBack()) {
			webview.goBack();
			return;
		}
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR)
			super.onBackPressed();
		else 
			finish();
	}
	
	public void openLink(View v) {
		Intent intent = new Intent(Intent.ACTION_VIEW,
				Uri.parse(this.webViewUrl));
		startActivity(intent);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		OtherUtils.releaseAllWebViewCallback();
		if(webview != null) {
			webview.removeAllViews();
			webViewLay.removeView(webview);
			webview.onPause();
			webview.destroy();
			webview.setVisibility(View.GONE);
			webview = null;
			Log.i("chaochao", "WebViewActivity-->onDestroy webview");
		}
		
		if(networkReceiver != null) {
			unregisterReceiver(networkReceiver);
			networkReceiver = null;
		}
	}
	
	private IOnNetworkChangeListener listener = new IOnNetworkChangeListener() {
		
		@Override
		public void openNetwork() {
			if(webview != null)
				webview.reload();
		}

		@Override
		public void closeNetwork() {
			// TODO Auto-generated method stub
			
		}
	};

}

