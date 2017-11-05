package dyhelper.com.fragment;

import com.tencent.smtt.sdk.DownloadListener;
import android.net.Uri;
import android.content.Intent;
import android.support.v4.app.Fragment;
import dyhelper.com.listeners.IOnBackListener;
import android.app.Activity;
import android.widget.ImageView;
import cn.dianyou.views.ProgressWebView;
import android.widget.LinearLayout;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;
import dyhelper.com.util.Tools;
import xyz.monkeytong.hongbao.R;
import android.util.Log;
import dyhelper.com.ui.Main;

public class MailFragment extends Fragment implements IOnBackListener {
    private Activity activity;
    private ImageView iv;
    private ProgressWebView webView;
    private LinearLayout webViewLay;
    private String url = "file:///android_asset/network_error.html";
    
    public MailFragment(Activity activity, String url) {
        this.activity = activity;
        this.url = url;
    }
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }
    
    public void onStart() {
        super.onStart();
        Main main = (Main) activity;
        main.setStatusColor(-0x74b7);
    }
    
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_mail, container, false);
        webViewLay = (LinearLayout)view.findViewById(R.id.id_webview_lay);
        webView = new ProgressWebView(getActivity().getApplicationContext());
        ViewGroup.LayoutParams webview_lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        webView.setLayoutParams(webview_lp);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setCacheMode(-1);
        webView.setWebViewClient(new WebViewClient() {
            
            
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
            
            public void onPageFinished(WebView view, String url) {
            }
            
            public void onReceivedError(WebView arg0, int arg1, String arg2, String arg3) {
                super.onReceivedError(arg0, arg1, arg2, arg3);
            }
        });
        webViewLay.addView(webView);
        webView.setDownloadListener(new MailFragment.MyWebViewDownLoadListener());
        webView.loadUrl(url);
        return view;
    }
    
    class MyWebViewDownLoadListener implements DownloadListener {
        
        public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent("android.intent.action.VIEW", uri);
            startActivity(intent);
        }
    }
    
    public void flush() {
        if(webView != null) {
            if(!webView.canGoBack()) {
                webView.clearHistory();
                webView.loadUrl(url);
                return;
            }
            webView.reload();
        }
    }
    
    public void setUrl(String url) {
        url = url;
    }
    
    public void onDestroyView() {
        super.onDestroyView();
        Tools.releaseAllWebViewCallback();
        if(webView != null) {
            webView.removeAllViews();
            webViewLay.removeView(webView);
            webView.onPause();
            webView.destroy();
            webView.setVisibility(0x8);
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
            webView.setVisibility(0x8);
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
            webView.setVisibility(0x8);
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
