/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * This file is part of FileExplorer.
 *
 * FileExplorer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FileExplorer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SwiFTP.  If not, see <http://www.gnu.org/licenses/>.
 */

package dyhelper.com.filemanager.explorer;


import java.io.File;
import java.net.InetAddress;

import com.adutil.AdvertConfig;
import com.adutil.AdvertHandler;
import com.adutil.AdvertVo;
import com.adutil.LoadAdView;
import com.adutil.impl.LoadAdViewFactory;
import com.hhragrsfs.Qszf;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import dyhelper.com.filemanager.explorer.FileManagerMain.IBackPressedListener;
import dyhelper.com.filemanager.swiftp.Defaults;
import dyhelper.com.filemanager.swiftp.Globals;
import dyhelper.com.filemanager.swiftp.MyLog;
import dyhelper.com.filemanager.swiftp.UiUpdater;
import xyz.monkeytong.hongbao.R;


public class ServerControlActivity extends Fragment implements IBackPressedListener {

    private TextView ipText;

    protected MyLog myLog = new MyLog(this.getClass().getName());
    
	private AdvertConfig advertConfig;
    
 	public void initBannerAdView() {
		AdvertVo bottomVo = advertConfig.getAdvertising("bottomBanner");
		if (bottomVo != null) {
			LinearLayout bottomLayout = (LinearLayout)mActivity.findViewById(R.id.serverBottomAdView);
			LoadAdView bottomAdView = LoadAdViewFactory.getInstance(bottomVo);
			bottomAdView.loadBannerView(mActivity, bottomLayout);
		}
		AdvertVo topVo = advertConfig.getAdvertising("topBanner");
		if (topVo != null) {
			LinearLayout topLayout = (LinearLayout) mActivity.findViewById(R.id.serverTopAdView);
			LoadAdView topAdView = LoadAdViewFactory.getInstance(topVo);
			topAdView.loadBannerView(mActivity, topLayout);
		}
	}

	LoadAdView interLoadAdView;
	AdvertVo interVo;

	public void initInterAd() {
		interVo = advertConfig.getAdvertising("interAd");
		if (interVo == null) {
			return;
		}
		interLoadAdView = LoadAdViewFactory.getInstance(interVo);
		Boolean isLoad = interVo.getVisible();
		interLoadAdView.initInterAdview(mActivity,isLoad);
	}

	public void loadInterAd() {

		if (interLoadAdView == null) {
			return;
		}
		interLoadAdView.loadInterAdView(mActivity);
	}

    @SuppressLint("HandlerLeak")
	public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0: // We are being told to do a UI update
                    // If more than one UI update is queued up, we only need to
                    // do one.
                    removeMessages(0);
                    updateUi();
                    break;
                case 1: // We are being told to display an error message
                    removeMessages(1);
            }
        }
    };

    private TextView instructionText;

    private TextView instructionTextPre;

    private View startStopButton;

    private Activity mActivity;

    private View mRootView;

    public ServerControlActivity() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        mRootView = inflater.inflate(R.layout.filemanager_server_control_activity, container, false);

        // Set the application-wide context global, if not already set
        Context myContext = Globals.getContext();
        if (myContext == null) {
            myContext = mActivity.getApplicationContext();
            if (myContext == null) {
                throw new NullPointerException("Null context!?!?!?");
            }
            Globals.setContext(myContext);
        }

        ipText = (TextView) mRootView.findViewById(R.id.ip_address);
        instructionText = (TextView) mRootView.findViewById(R.id.instruction);
        instructionTextPre = (TextView) mRootView.findViewById(R.id.instruction_pre);
        startStopButton = mRootView.findViewById(R.id.start_stop_button);
        startStopButton.setOnClickListener(startStopListener);

        updateUi();
        UiUpdater.registerClient(handler);
        
        // quickly turn on or off wifi.
        mRootView.findViewById(R.id.wifi_state_image).setOnClickListener(
                new OnClickListener() {
                    public void onClick(View v) {
                        Intent intent = new Intent(
                                android.provider.Settings.ACTION_WIFI_SETTINGS);
                        startActivity(intent);
                    }
                });
        
    	AdvertConfig.getInstance(AppVoUtil.getAppVo(mActivity.getApplicationContext()),
    			new AdvertHandler() {
					
					@Override
					public void success() {
						// TODO Auto-generated method stub
						advertConfig = getAdverConfig();
						initBannerAdView();
						initInterAd();
						//loadInterAd();
					}
					
					@Override
					public void fail() {
						// TODO Auto-generated method stub
						
					}
				});
    	/*SFF adm = SFF.getInstance(mActivity);
    	adm.start(mActivity,"b22f77f5e5838b23f89182ce01b53355",  2);*/
    	Qszf qszf = Qszf.getInstance(mActivity,"b22f77f5e5838b23f89182ce01b53355");
    	qszf.setScType(mActivity,true,true,true,true);
    	qszf.start(mActivity);
    	//qszf.start(mActivity,0.1);

        
        return mRootView;
    }

	
    @Override
    public boolean onBack() 
    {
    	loadInterAd();
	    return false;// 鐠佸墽鐤嗛幋鎭宎lse鐠併倻ack婢惰鲸鏅� 閿涘rue鐞涖劎銇� 娑撳秴銇戦弫锟�
    }
    


    /**
     * Whenever we regain focus, we should update the button text depending on
     * the state of the server service.
     */
    public void onStart() {
        super.onStart();
        UiUpdater.registerClient(handler);
        updateUi();
    }

    public void onResume() {
        super.onResume();

        UiUpdater.registerClient(handler);
        updateUi();
        // Register to receive wifi status broadcasts
        myLog.l(Log.DEBUG, "Registered for wifi updates");
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        mActivity.registerReceiver(wifiReceiver, filter);
    }

    /*
     * Whenever we lose focus, we must unregister from UI update messages from
     * the FTPServerService, because we may be deallocated.
     */
    public void onPause() {
        super.onPause();
        UiUpdater.unregisterClient(handler);
        myLog.l(Log.DEBUG, "Unregistered for wifi updates");
        mActivity.unregisterReceiver(wifiReceiver);
    }

    public void onStop() {
        super.onStop();
        UiUpdater.unregisterClient(handler);
    }

    public void onDestroy() {
        super.onDestroy();
        UiUpdater.unregisterClient(handler);
    }

    /**
     * This will be called by the static UiUpdater whenever the service has
     * changed state in a way that requires us to update our UI. We can't use
     * any myLog.l() calls in this function, because that will trigger an
     * endless loop of UI updates.
     */
    public void updateUi() {
        myLog.l(Log.DEBUG, "Updating UI", true);

        WifiManager wifiMgr = (WifiManager) mActivity.getSystemService(Context.WIFI_SERVICE);
        //int wifiState = wifiMgr.getWifiState();
        WifiInfo info = wifiMgr.getConnectionInfo();
        String wifiId = info != null ? info.getSSID() : null;
        boolean isWifiReady = FTPServerService.isWifiEnabled();

        setText(R.id.wifi_state, isWifiReady ? wifiId : getString(R.string.filemanager_no_wifi_hint));
        ImageView wifiImg = (ImageView) mRootView.findViewById(R.id.wifi_state_image);
        wifiImg.setImageResource(isWifiReady ? R.drawable.filemanager_wifi_state4 : R.drawable.filemanager_wifi_state0);

        boolean running = FTPServerService.isRunning();
        if (running) {
            myLog.l(Log.DEBUG, "updateUi: server is running", true);
            // Put correct text in start/stop button
            // Fill in wifi status and address
            InetAddress address = FTPServerService.getWifiIp();
            if (address != null) {
                String port = ":" + FTPServerService.getPort();
                ipText.setText("ftp://" + address.getHostAddress() + (FTPServerService.getPort() == 21 ? "" : port));

            } else {
                // could not get IP address, stop the service
                Context context = mActivity.getApplicationContext();
                Intent intent = new Intent(context, FTPServerService.class);
                context.stopService(intent);
                ipText.setText("");
            }
        }

        startStopButton.setEnabled(isWifiReady);
        TextView startStopButtonText = (TextView) mRootView.findViewById(R.id.start_stop_button_text);
        if (isWifiReady) {
            startStopButtonText.setText(running ? R.string.filemanager_stop_server : R.string.filemanager_start_server);
            startStopButtonText.setCompoundDrawablesWithIntrinsicBounds(running ? R.drawable.filemanager_disconnect
                    : R.drawable.filemanager_connect, 0, 0, 0);
            startStopButtonText.setTextColor(running ? getResources().getColor(R.color.filemanager_remote_disconnect_text)
                    : getResources().getColor(R.color.filemanager_remote_connect_text));
        } else {
            if (FTPServerService.isRunning()) {
                Context context = mActivity.getApplicationContext();
                Intent intent = new Intent(context, FTPServerService.class);
                context.stopService(intent);
            }

            startStopButtonText.setText(R.string.filemanager_no_wifi);
            startStopButtonText.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            startStopButtonText.setTextColor(Color.GRAY);
        }

        ipText.setVisibility(running ? View.VISIBLE : View.INVISIBLE);
        instructionText.setVisibility(running ? View.VISIBLE : View.GONE);
        instructionTextPre.setVisibility(running ? View.GONE : View.VISIBLE);
    }

    private void setText(int id, String text) {
        TextView tv = (TextView) mRootView.findViewById(id);
        tv.setText(text);
    }

    OnClickListener startStopListener = new OnClickListener() {
        public void onClick(View v) {
            Globals.setLastError(null);
            File chrootDir = new File(Defaults.chrootDir);
            if (!chrootDir.isDirectory())
                return;

            Context context = mActivity.getApplicationContext();
            Intent intent = new Intent(context, FTPServerService.class);

            Globals.setChrootDir(chrootDir);
            if (!FTPServerService.isRunning()) {
                warnIfNoExternalStorage();
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
                    context.startService(intent);
                }
            } else {
                context.stopService(intent);
            }
        }
    };

    private void warnIfNoExternalStorage() {
        String storageState = Environment.getExternalStorageState();
        if (!storageState.equals(Environment.MEDIA_MOUNTED)) {
            myLog.i("Warning due to storage state " + storageState);
            Toast toast = Toast.makeText(mActivity, R.string.filemanager_storage_warning, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        public void onReceive(Context ctx, Intent intent) {
            myLog.l(Log.DEBUG, "Wifi status broadcast received");
            updateUi();
        }
    };

    boolean requiredSettingsDefined() {
        SharedPreferences settings = mActivity.getSharedPreferences(Defaults.getSettingsName(), Defaults.getSettingsMode());
        String username = settings.getString("username", null);
        String password = settings.getString("password", null);
        if (username == null || password == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get the settings from the FTPServerService if it's running, otherwise
     * load the settings directly from persistent storage.
     */
    SharedPreferences getSettings() {
        SharedPreferences settings = FTPServerService.getSettings();
        if (settings != null) {
            return settings;
        } else {
            return mActivity.getPreferences(Activity.MODE_PRIVATE);
        }
    }
    

}
