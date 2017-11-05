package dyhelper.com.fragment;

import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import cn.dianyou.views.ProgressWebView;
import dyhelper.com.listeners.IOnBackListener;
import dyhelper.com.ui.Main;
import dyhelper.com.util.Tools;
import xyz.monkeytong.hongbao.R;

public class NewsFragment extends Fragment implements IOnBackListener {
    private Activity activity;
    private ProgressWebView webView;
    private LinearLayout webViewLay;
    private String url = "file:///android_asset/network_error.html";
    
    public NewsFragment(Activity activityb, String url) {
        activity = activityb;
        this.url = url;
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public void flush() {
        Log.i("chaochao", url);
        if(webView != null) {
            if(!webView.canGoBack()) {
                Log.i("chaochao", "NewsFragment..2");
                webView.clearHistory();
                webView.loadUrl(url);
                return;
            }
            Log.i("chaochao", "NewsFragment..3");
            webView.reload();
        }
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public void onStart() {
        super.onStart();
        Main main = (Main) activity;
        main.setStatusColor(-0x74b7);
    }
    
    class MyWebViewDownLoadListener implements DownloadListener {
        
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent("android.intent.action.VIEW", uri);
            startActivity(intent);
        }
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        webViewLay = (LinearLayout)view.findViewById(R.id.id_webview_lay);
        webView = new ProgressWebView(getActivity().getApplicationContext());
        ViewGroup.LayoutParams webview_lp = new ViewGroup.LayoutParams(-0x1, -0x1);
        webView.setLayoutParams(webview_lp);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(-0x1);
        webView.setWebViewClient(new WebViewClient() {
            
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            
            public void onPageFinished(WebView view, String url) {
            }
        });
        webViewLay.addView(webView);
        webView.setDownloadListener(new NewsFragment.MyWebViewDownLoadListener());
        webView.loadUrl(url);
        return view;
    }
    
    public void onDestroyView() {
        super.onDestroyView();
        Tools.releaseAllWebViewCallback();
        if(webView != null) {
            webView.removeAllViews();
            webViewLay.removeView(webView);
            webView.onPause();
            webView.destroy();
            webView.setVisibility(View.GONE);
            webView = null;
            Log.i("chaochao", "onDestroyView-->application");
        }
        System.gc();
    }
    
    public void onDestroy() {
        super.onDestroy();
        Tools.releaseAllWebViewCallback();
        if(webView != null) {
            webView.removeAllViews();
            webViewLay.removeView(webView);
            webView.onPause();
            webView.destroy();
            webView.setVisibility(View.GONE);
            webView = null;
            Log.i("chaochao", "onDestroy-->application");
        }
        System.gc();
    }
    
    public void onDetach() {
        super.onDetach();
        Tools.releaseAllWebViewCallback();
        if(webView != null) {
            webView.removeAllViews();
            webViewLay.removeView(webView);
            webView.onPause();
            webView.destroy();
            webView.setVisibility(View.GONE);
            webView = null;
            Log.i("chaochao", "onDetach-->application");
        }
        System.gc();
    }
    
    public boolean onBack() {
        if((webView != null) && (webView.canGoBack())) {
            webView.goBack();
            return true;
        }
        return false;
    }
}
