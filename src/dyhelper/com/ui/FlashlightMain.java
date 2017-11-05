package dyhelper.com.ui;

import android.app.Activity;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import xyz.monkeytong.hongbao.R;

public class FlashlightMain extends Activity {
    private ViewGroup currentRootView;
    Bitmap handlebarBmp;
    private boolean isFlashlightOn;
    private ImageView iv_handlebar;
    private ImageView iv_lightLine;
    private ImageButton lightBtn;
    Bitmap lineBmp;
    private Camera mCamera;
    private float mYDensity;
    Bitmap onBmp;
    
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	if(!checkFlashlight()) {
    		Toast.makeText(this, "未检测到任何摄像头", 0).show();
    		finish();
    		return;
    	}
    	
    	setContentView(R.layout.activity_tools_flashlight);
    	
    	currentRootView = ((ViewGroup)getWindow().getDecorView());
        View localView = this.currentRootView.findViewById(android.R.id.content);
        if (localView != null)
          currentRootView = ((ViewGroup)localView);
        
        mYDensity = (getResources().getDisplayMetrics().ydpi / 160.0F);
        lineBmp = BitmapFactory.decodeResource(getResources(), R.drawable.light_line);
        onBmp = BitmapFactory.decodeResource(getResources(), R.drawable.light_on);
        handlebarBmp = BitmapFactory.decodeResource(getResources(), R.drawable.light_handlebar);
    	
    	iv_lightLine = (ImageView) findViewById(R.id.light_line);
    	iv_handlebar = (ImageView) findViewById(R.id.light_handlebar);
    	lightBtn = (ImageButton) findViewById(R.id.light_onOff);
    	
    	lightBtn.setEnabled(false);
        iv_lightLine.setVisibility(4);
    	
    	lightBtn.postDelayed(new Runnable() {

			@Override
			public void run() {
				lightBtn.setEnabled(true);
				onLight();
				lightBtn.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(isFlashlightOn) {
							offLight();
						} else {
							onLight();
						}
					}
				});
			}
    		
    	}, 600);
    }
    
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        int totalHeight = currentRootView.getHeight();
        int lightLineHeight = measureSize(iv_lightLine, lineBmp, totalHeight, 2.0f, 0x0);
        iv_lightLine.setImageBitmap(lineBmp);
        int handleBarHeight = measureSize(iv_handlebar, handlebarBmp, totalHeight, 0.0f, lightLineHeight);
        iv_handlebar.setImageBitmap(handlebarBmp);
        int btnSize = (int)((((float)handleBarHeight / 10.0f) - (6.0f * mYDensity)) + 0.5f);
        RelativeLayout.LayoutParams btn_lp = (RelativeLayout.LayoutParams)lightBtn.getLayoutParams();
        btn_lp.height = btnSize;
        btn_lp.width = btnSize;
        btn_lp.bottomMargin = (int)(((float)(btnSize * 0x3) + (2.0f * mYDensity)) + 0.5f);
        lightBtn.setLayoutParams(btn_lp);
    }
    
    private int measureSize(View view, Bitmap bmp, int totalHeight, float frc, int preHeight) {
        int bmpHeight = bmp.getHeight();
        int bmpWidth = bmp.getWidth();
        int bmpNewHeight = 0;
        if(frc == 0) {
            bmpNewHeight = (int)((((float)(totalHeight - preHeight) + ((float)preHeight * 0.5f)) + (mYDensity * 5.0f)) + 0.5f);
        } else {
            bmpNewHeight = (int)(((double)totalHeight * 1.0) / (double)frc);
        }
        int bmpNewWidth = (int)((((float)bmpNewHeight * 1.0f) / (float)bmpHeight) * (float)bmpWidth);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams)view.getLayoutParams();
        lp.height = bmpNewHeight;
        if(frc != 2.0f) {
            lp.width = bmpNewWidth;
        }
        if(frc <= 0) {
            lp.topMargin = (totalHeight - bmpNewHeight);
        }
        view.setLayoutParams(lp);
        return bmpNewHeight;
    }
    
    private void onLight() {
        lightBtn.setSelected(true);
        isFlashlightOn = true;
        iv_lightLine.setVisibility(0x0);
        Camera.Parameters mParameters = mCamera.getParameters();
        mParameters.setFlashMode("torch");
        mCamera.setParameters(mParameters);
    }
    
    private void offLight() {
        lightBtn.setSelected(false);
        isFlashlightOn = false;
        iv_lightLine.setVisibility(0x4);
        Camera.Parameters mParameters = mCamera.getParameters();
        mParameters.setFlashMode("off");
        mCamera.setParameters(mParameters);
    }
    
    private boolean checkFlashlight() {
        getWindow().addFlags(0x80);
        PackageManager pm = getPackageManager();
        FeatureInfo[] features = pm.getSystemAvailableFeatures();
        if(features.length <= 0) {
        	 return false;
        }
        
        for(FeatureInfo f : features) {
        	if("android.hardware.camera.flash".equals(f.name)) {
                if(mCamera == null) {
                    try {
                        mCamera = Camera.open();
                    } catch(Exception e) {
                        mCamera = Camera.open((Camera.getNumberOfCameras() - 0x1));
                        Toast.makeText(this, "方法open有问题", 0).show();
                    }
                }
                Camera.Parameters parameters = mCamera.getParameters();
                parameters.setFlashMode("off");
                mCamera.setParameters(parameters);
                mCamera.startPreview();
                return true;
            }
        }
        
        return false;
    }
    
    public void onBack(View v) {
        finish();
    }
    
    protected void onDestroy() {
        super.onDestroy();
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.lock();
        mCamera.release();
        mCamera = null;
    }
}
