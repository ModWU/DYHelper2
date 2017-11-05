package dyhelper.com.broadcasts;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.text.TextUtils;

public class HomeKeyReceiver extends BroadcastReceiver {
    private HomeKeyReceiver.OnHomeKeyListener listener;
    private String SYSTEM_REASON = "reason";
    private String SYSTEM_HOME_KEY = "homekey";
    private String SYSTEM_HOME_KEY_LONG = "recentapps";
    
    public HomeKeyReceiver(OnHomeKeyListener listener) {
        this.listener = listener;
    }
    
    public void onReceive(Context context, Intent intent) {
        Log.i("chaochao", "...............HomeKeyReceiver................");
        String action = intent.getAction();
        if(action.equals("android.intent.action.CLOSE_SYSTEM_DIALOGS")) {
            String reason = intent.getStringExtra(SYSTEM_REASON);
            if(TextUtils.equals(reason, SYSTEM_HOME_KEY)) {
                if(listener != null) {
                    listener.onClickHome();
                }
                return;
            }
            if(TextUtils.equals(reason, SYSTEM_HOME_KEY_LONG)) {
                if(listener != null) {
                    listener.onLongClickHome();
                }
            }
        }
    }
    
    public void setOnHomeKeyListener(HomeKeyReceiver.OnHomeKeyListener listener) {
        this.listener = listener;
    }
    
    public interface OnHomeKeyListener {
    	void onClickHome();
    	void onLongClickHome();
    }
}
