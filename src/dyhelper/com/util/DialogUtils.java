package dyhelper.com.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import xyz.monkeytong.hongbao.R;

public class DialogUtils {
	
	private static HashMap<Activity, Dialog> dialogMap;
	
	static {
		dialogMap = new HashMap<Activity, Dialog>();
	}
	
	public static Dialog getDialog_n_a_j(final Activity activity) {
		Dialog dialog = new AlertDialog.Builder(activity)
				.setIcon(android.R.drawable.btn_star).setTitle("使用说明")
				.setMessage(activity.getResources().getString(R.string.hongbao_remind))
				.setPositiveButton("知道了", new OnClickListener() {

					@Override
					public void onClick(DialogInterface arg0, int arg1) {
					}

				}).create();
		dialog.setCancelable(true);
		return dialog;
	}
	
	private static void clearSameActivity(Activity activity) {
		Set<Activity> activitySet = new HashSet<Activity>(dialogMap.keySet());
		Iterator<Activity> iterator = activitySet.iterator();
		while(iterator.hasNext()) {
			Activity next = iterator.next();
			if(next.getClass() == activity.getClass()) 
				dialogMap.remove(next);
		}
		
	}
	
	public static ProgressDialog getPrgressDialog(Activity activity, String title, String message) {
		ProgressDialog dialog = null;
		if(dialogMap.containsKey(activity)) {
			dialog = (ProgressDialog) dialogMap.get(activity);
		} else {
			dialog = new ProgressDialog(activity);
			clearSameActivity(activity);
			dialogMap.put(activity, dialog);
		}
		dialog.setTitle(title);
		dialog.setMessage(message);
		return dialog;
	}
	
	
	
	public static Dialog getDialog(Activity activity) {
		return dialogMap.get(activity);
	}
	
	public static void closeDialog(Activity activity) {
		Dialog dialog = getDialog(activity);
		if(dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialogMap.remove(activity);
		}
	}
	
	public static void closeAllDialog() {
		Collection<Dialog> dialogs = dialogMap.values();
		for(Dialog d : dialogs) {
			d.dismiss();
		}
		dialogMap.clear();
	}
	
	public static void showDialog(Activity activity, String title, String message, boolean isCancelable) {
		Dialog dialog = DialogUtils.getPrgressDialog(activity, title, message);
		dialog.setCancelable(isCancelable);
		if(dialog != null && !dialog.isShowing())
			dialog.show();
	}
	
	public static void removeKey(Activity activity) {
		dialogMap.remove(activity);
	}
	
	public static void clear() {
		dialogMap.clear();
	}
}
