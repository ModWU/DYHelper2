package dyhelper.com.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.accessibilityservice.AccessibilityService;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import dyhelper.com.ui.HongbaoMain;
import dyhelper.com.util.HongbaoSignature;
import dyhelper.com.util.PowerUtil;
import dyhelper.com.util.SQLManager;

public class HongbaoService extends AccessibilityService implements
		SharedPreferences.OnSharedPreferenceChangeListener {
	private static final String WECHAT_DETAILS_EN = "Details";
	private static final String WECHAT_DETAILS_CH = "�������";
	private static final String WECHAT_BETTER_LUCK_EN = "Better luck next time!";
	private static final String WECHAT_BETTER_LUCK_CH = "������";
	private static final String WECHAT_EXPIRES_CH = "�ѳ���24Сʱ";
	private static final String WECHAT_VIEW_SELF_CH = "�鿴���";
	private static final String WECHAT_VIEW_OTHERS_CH = "��ȡ���";
	private static final String WECHAT_NOTIFICATION_TIP = "[΢�ź��]";
	private static final String WECHAT_LUCKMONEY_RECEIVE_ACTIVITY = "LuckyMoneyReceiveUI";
	private static final String WECHAT_LUCKMONEY_DETAIL_ACTIVITY = "LuckyMoneyDetailUI";
	private static final String WECHAT_LUCKMONEY_GENERAL_ACTIVITY = "LauncherUI";
	private static final String WECHAT_LUCKMONEY_CHATTING_ACTIVITY = "ChattingUI";
	private String currentActivityName = WECHAT_LUCKMONEY_GENERAL_ACTIVITY;

	private AccessibilityNodeInfo rootNodeInfo, mReceiveNode, mUnpackNode;
	private boolean mLuckyMoneyPicked, mLuckyMoneyReceived;
	private int mUnpackCount = 0;
	private boolean mMutex = false, mListMutex = false, mChatMutex = false;
	private HongbaoSignature signature = new HongbaoSignature();

	private PowerUtil powerUtil;
	private SharedPreferences sharedPreferences;
	public static HongbaoService hongbaoService = null;
	public static boolean isNeedBack = false;
	public static boolean thereIsOneHongbao = false;

	public HongbaoService() {
		if (hongbaoService == null) {
			hongbaoService = this;
		}
		
		Log.i("sort", "HongbaoService()--------");
	}
	
	
	private void seeEventType(AccessibilityEvent event) {
		int eventType = event.getEventType();  
        String eventText = "";  
		switch (eventType) {  
        case AccessibilityEvent.TYPE_VIEW_CLICKED:  
            eventText = "TYPE_VIEW_CLICKED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_FOCUSED:  
            eventText = "TYPE_VIEW_FOCUSED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:  
            eventText = "TYPE_VIEW_LONG_CLICKED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_SELECTED:  
            eventText = "TYPE_VIEW_SELECTED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:  
            eventText = "TYPE_VIEW_TEXT_CHANGED";  
            break;  
        case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:  
            eventText = "TYPE_WINDOW_STATE_CHANGED";  
            break;  
        case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:  
            eventText = "TYPE_NOTIFICATION_STATE_CHANGED";  
            break;  
        case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:  
            eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_END";  
            break;  
        case AccessibilityEvent.TYPE_ANNOUNCEMENT:  
            eventText = "TYPE_ANNOUNCEMENT";  
            break;  
        case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:  
            eventText = "TYPE_TOUCH_EXPLORATION_GESTURE_START";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:  
            eventText = "TYPE_VIEW_HOVER_ENTER";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:  
            eventText = "TYPE_VIEW_HOVER_EXIT";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_SCROLLED:  
            eventText = "TYPE_VIEW_SCROLLED";  
            break;  
        case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:  
            eventText = "TYPE_VIEW_TEXT_SELECTION_CHANGED";  
            break;  
        case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:  
            eventText = "TYPE_WINDOW_CONTENT_CHANGED";  
            break;  
        }  
        eventText = eventText + ":" + eventType;  
        Log.i("event123", "-------start--------");
        Log.i("event123", eventText);
        
        Log.i("event123", "packagename: " + event.getPackageName().toString());
        Log.i("event123", "classname: " + event.getClassName());
        Log.i("event123", "-------end--------");
	}

	/**
	 * AccessibilityEvent
	 * 
	 * @param event
	 *            �¼�
	 */
	@SuppressLint("NewApi")
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		Log.i("sort", "onAccessibilityEvent");
		
		if (HongbaoMain.pause) {
			return;
		}
		if (sharedPreferences == null)
			return;
		Log.i("qhb",
				"onAccessibilityEvent-------------------------------------------------------");
		setCurrentActivityName(event);
		if ("com.tencent.mm.ui.LauncherUI".equals(event.getClassName())) {
			if (isNeedBack) {
				isNeedBack = false;
				performGlobalAction(GLOBAL_ACTION_BACK);
			}
		}
		Log.i("qhb", "event.getEventType:" + event.getEventType());
		/* ���֪ͨ��Ϣ */
		if (!mMutex) {
			if (sharedPreferences.getBoolean("pref_watch_notification", false)
					&& watchNotifications(event))
				return;
			if (sharedPreferences.getBoolean("pref_watch_list", false)
					&& watchList(event))
				return;
			mListMutex = false;
		}

		if (!mChatMutex) {
			mChatMutex = true;
			Log.i("qhb",
					"pref_watch_chat:"
							+ sharedPreferences.getBoolean("pref_watch_chat",
									false));
			if (sharedPreferences.getBoolean("pref_watch_chat", false))
				watchChat(event);
			mChatMutex = false;
		}
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("sort", "onStartCommand44444444444444");
		return super.onStartCommand(intent, flags, startId);
		
		
	}
	
	@Override
	public boolean bindService(Intent service, ServiceConnection conn, int flags) {
		Log.i("sort", "bindServicefgfffff");
		return super.bindService(service, conn, flags);
	}
	

	@SuppressLint("NewApi")
	private void watchChat(AccessibilityEvent event) {
		Log.i("sort", "---watchChat---");
		seeEventType(event);
		this.rootNodeInfo = getRootInActiveWindow();
		Log.i("qhb", "rootNodeInfo is null:"
				+ (rootNodeInfo == null ? true : false));
		if (rootNodeInfo == null)
			return;

		mReceiveNode = null;
		mUnpackNode = null;

		checkNodeInfo(event.getEventType(), event);

		Log.i("qhb", "mLuckyMoneyReceived:" + mLuckyMoneyReceived);
		Log.i("qhb", "mLuckyMoneyPicked:" + mLuckyMoneyPicked);
		Log.i("qhb", "mReceiveNode is null:"
				+ (mReceiveNode == null ? true : false));
		/* ����Ѿ����յ�������һ�û�д��� */
		if (mLuckyMoneyReceived && !mLuckyMoneyPicked && (mReceiveNode != null)) {
			
			if (!thereIsOneHongbao) {
				Log.i("qhb", "û�к����������");
				Log.i("sort", "---event.getClassName(): " + event.getClassName());
				return;
			}
			
			Log.i("sort", "---�к�������д���---");
			
			Log.i("sort", "---event.getClassName(): " + event.getClassName());
			
			mMutex = true;
			Log.i("qhb", "��������Ϣ");
			Log.i("qhb", "event.getClassName():" + event.getClassName());
			// �����TYPE_WINDOW_STATE_CHANGED�����Ҳ���������Ϣ��˵�����������淵�صģ���ʱ��ȥ����
			mReceiveNode.getParent().performAction(
					AccessibilityNodeInfo.ACTION_CLICK);
			mLuckyMoneyReceived = false;
			mLuckyMoneyPicked = true;
		}
		Log.i("qhb", "mUnpackCount:" + mUnpackCount);
		Log.i("qhb", "mUnpackNode:" + mUnpackNode);
		/* �����������δ��ȡ */
		if (mUnpackCount == 1 && (mUnpackNode != null)) {
			int delayFlag = sharedPreferences.getInt("pref_open_delay", 0) * 1000;
			new android.os.Handler().postDelayed(new Runnable() {
				public void run() {
					try {
						Log.i("qhb", "��������");
						thereIsOneHongbao = false;
						mUnpackNode
								.performAction(AccessibilityNodeInfo.ACTION_CLICK);
					} catch (Exception e) {
						mMutex = false;
						mLuckyMoneyPicked = false;
						mUnpackCount = 0;
					}
				}
			}, delayFlag);
		}

		try {
			List<AccessibilityNodeInfo> nodes3 = this
					.findAccessibilityNodeInfosByTexts(event.getSource(),
							new String[] { "�����ˣ����������", "�Ѵ�����Ǯ", "����", "�������" });
			if (!nodes3.isEmpty()) {
				AccessibilityNodeInfo targetNode = nodes3
						.get(nodes3.size() - 1);
				targetNode = targetNode.getParent();

				Log.i("qhb", "0" + targetNode.getChild(0).getText().toString());
				Log.i("qhb", "1" + targetNode.getChild(1).getText().toString());
				Log.i("qhb", "2" + targetNode.getChild(2).getText().toString());
				Log.i("qhb", "3" + targetNode.getChild(3).getText().toString());
				Log.i("qhb", "4" + targetNode.getChild(4).getText().toString());
				String from = targetNode.getChild(0).getText().toString();
				//from = from.substring(0, from.length() - 3);
				String word = targetNode.getChild(1).getText().toString();
				String amount = targetNode.getChild(2).getText().toString();
				String timestamp = String.valueOf(System.currentTimeMillis());
				HashMap<String, String> oneRecord = new HashMap<String, String>();
				oneRecord.put("friend", from);
				oneRecord.put("amount", amount);
				oneRecord.put("word", word);
				oneRecord.put("timestamp", timestamp);
				SQLManager.getInstance(this).insertData("record", oneRecord);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void setCurrentActivityName(AccessibilityEvent event) {
		Log.i("sort", "setCurrentActivityName");
		if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			return;
		}

		try {
			ComponentName componentName = new ComponentName(event
					.getPackageName().toString(), event.getClassName()
					.toString());

			getPackageManager().getActivityInfo(componentName, 0);
			currentActivityName = componentName.flattenToShortString();
		} catch (PackageManager.NameNotFoundException e) {
			currentActivityName = WECHAT_LUCKMONEY_GENERAL_ACTIVITY;
		}
	}

	@SuppressLint("NewApi")
	private boolean watchList(AccessibilityEvent event) {
		Log.i("sort", "watchList");
		if (mListMutex)
			return false;
		mListMutex = true;
		AccessibilityNodeInfo eventSource = event.getSource();
		// Not a message
		if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
				|| eventSource == null)
			return false;

		List<AccessibilityNodeInfo> nodes = eventSource
				.findAccessibilityNodeInfosByText(WECHAT_NOTIFICATION_TIP);
		// ���������ж�currentActivityName.contains(WECHAT_LUCKMONEY_GENERAL_ACTIVITY)
		// ���⵱���ĺ��г��ֱ���Ϊ��[΢�ź��]������������ʵ���Ǻ��������Ϣʱ����
		if (!nodes.isEmpty()
				&& currentActivityName
						.contains(WECHAT_LUCKMONEY_GENERAL_ACTIVITY)) {
			AccessibilityNodeInfo nodeToClick = nodes.get(0);
			if (nodeToClick == null)
				return false;
			CharSequence contentDescription = nodeToClick
					.getContentDescription();
			if (contentDescription != null
					&& !signature.getContentDescription().equals(
							contentDescription)) {
				nodeToClick.performAction(AccessibilityNodeInfo.ACTION_CLICK);
				signature.setContentDescription(contentDescription.toString());
				return true;
			}
		}
		return false;
	}

	private boolean watchNotifications(AccessibilityEvent event) {
		Log.i("sort", "watchNotifications");
		// Not a notification
		if (event.getEventType() != AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED)
			return false;

		// Not a hongbao
		String tip = event.getText().toString();
		if (!tip.contains(WECHAT_NOTIFICATION_TIP))
			return true;

		Parcelable parcelable = event.getParcelableData();
		if (parcelable instanceof Notification) {
			Notification notification = (Notification) parcelable;
			try {
				Log.i("qhb", "�յ�һ�����֪ͨ");
				thereIsOneHongbao = true;
				/* ���signature,�������Ự������ */
				signature.cleanSignature();
				notification.contentIntent.send();
			} catch (PendingIntent.CanceledException e) {
				e.printStackTrace();
			}
		}
		return true;
	}

	@Override
	public void onInterrupt() {

	}

	@SuppressLint("NewApi")
	private AccessibilityNodeInfo findOpenButton(AccessibilityNodeInfo node) {
		if (node == null)
			return null;

		// ��layoutԪ��
		if (node.getChildCount() == 0) {
			if ("android.widget.Button".equals(node.getClassName()))
				return node;
			else
				return null;
		}

		// layoutԪ�أ�������button
		AccessibilityNodeInfo button;
		for (int i = 0; i < node.getChildCount(); i++) {
			button = findOpenButton(node.getChild(i));
			if (button != null)
				return button;
		}
		return null;
	}

	@SuppressLint("NewApi")
	private void checkNodeInfo(int eventType, AccessibilityEvent event) {
		Log.i("sort", "checkNodeInfo");
		if (this.rootNodeInfo == null)
			return;

		if (signature.commentString != null) {
			sendComment();
			signature.commentString = null;
		}
		Log.i("qhb", "checkNodeInfo start--------");
		/* ����Ự���ڣ������ڵ�ƥ�䡰��ȡ�������"�鿴���" */
		AccessibilityNodeInfo node1 = (sharedPreferences.getBoolean(
				"pref_watch_self", false)) ? this.getTheLastNode(
				WECHAT_VIEW_OTHERS_CH, WECHAT_VIEW_SELF_CH) : this
				.getTheLastNode(WECHAT_VIEW_OTHERS_CH);
		Log.i("qhb", "node1 is null:" + (node1 == null ? true : false));
		boolean contains = currentActivityName
				.contains(WECHAT_LUCKMONEY_CHATTING_ACTIVITY)
				|| currentActivityName
						.contains(WECHAT_LUCKMONEY_GENERAL_ACTIVITY);
		Log.i("qhb", "contains:" + contains);
		if (node1 != null && contains) {
			String excludeWords = sharedPreferences.getString(
					"pref_watch_exclude_words", "");
			boolean generateSignature = this.signature.generateSignature(node1,
					excludeWords, event);

			Log.i("qhb", "generateSignature:" + generateSignature);
			
			Log.i("sort", "generateSignature:" + generateSignature);
			Log.i("sort", "node1:" + node1);
			if (generateSignature) {
				mLuckyMoneyReceived = true;
				mReceiveNode = node1;
				// Log.d("sig", this.signature.toString());
			}
			return;
		}

		/* ��������������û���꣬�����ڵ�ƥ�䡰������ */
		AccessibilityNodeInfo node2 = findOpenButton(this.rootNodeInfo);
		if (node2 != null
				&& "android.widget.Button".equals(node2.getClassName())
				&& currentActivityName
						.contains(WECHAT_LUCKMONEY_RECEIVE_ACTIVITY)) {
			mUnpackNode = node2;
			mUnpackCount += 1;
			Log.i("qhb", "�����û���� ,return");
			return;
		}

		/* �������������ѱ����꣬�����ڵ�ƥ�䡰������顱�͡������ˡ� */
		boolean hasNodes = this.hasOneOfThoseNodes(WECHAT_BETTER_LUCK_CH,
				WECHAT_DETAILS_CH, WECHAT_BETTER_LUCK_EN, WECHAT_DETAILS_EN,
				WECHAT_EXPIRES_CH);
		boolean type = eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
		boolean contain = currentActivityName
				.contains(WECHAT_LUCKMONEY_DETAIL_ACTIVITY)
				|| currentActivityName
						.contains(WECHAT_LUCKMONEY_RECEIVE_ACTIVITY);
		Log.i("qhb", "............");
		Log.i("qhb", "mMutex:" + mMutex);
		Log.i("qhb", "type:" + type);
		Log.i("qhb", "hasNodes:" + hasNodes);
		Log.i("qhb", "contain:" + contain);
		Log.i("qhb", "............");
		type = true;
		if (mMutex && type && hasNodes && contain) {
			Log.i("qhb", "����");
			mMutex = false;
			mLuckyMoneyPicked = false;
			mUnpackCount = 0;
			isNeedBack = true;
			performGlobalAction(GLOBAL_ACTION_BACK);
			signature.commentString = generateCommentString();
		}
	}

	@SuppressLint("NewApi")
	private void sendComment() {
		Log.i("sort", "sendComment");
		try {
			AccessibilityNodeInfo outNode = getRootInActiveWindow().getChild(0)
					.getChild(0);
			AccessibilityNodeInfo nodeToInput = outNode
					.getChild(outNode.getChildCount() - 1).getChild(0)
					.getChild(1);

			if ("android.widget.EditText".equals(nodeToInput.getClassName())) {
				Bundle arguments = new Bundle();
				arguments
						.putCharSequence(
								AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE,
								signature.commentString);
				nodeToInput.performAction(
						AccessibilityNodeInfo.ACTION_SET_TEXT, arguments);
			}
		} catch (Exception e) {
			// Not supported
		}
	}

	@SuppressLint("NewApi")
	private boolean hasOneOfThoseNodes(String... texts) {
		Log.i("sort", "hasOneOfThoseNodes");
		List<AccessibilityNodeInfo> nodes;
		for (String text : texts) {
			if (text == null)
				continue;

			nodes = this.rootNodeInfo.findAccessibilityNodeInfosByText(text);

			if (nodes != null && !nodes.isEmpty())
				return true;
		}
		return false;
	}

	@SuppressLint("NewApi")
	private AccessibilityNodeInfo getTheLastNode(String... texts) {
		Log.i("sort", "getTheLastNode");
		int bottom = 0;
		AccessibilityNodeInfo lastNode = null, tempNode;
		List<AccessibilityNodeInfo> nodes;

		for (String text : texts) {
			if (text == null)
				continue;

			nodes = this.rootNodeInfo.findAccessibilityNodeInfosByText(text);

			if (nodes != null && !nodes.isEmpty()) {
				tempNode = nodes.get(nodes.size() - 1);
				if (tempNode == null)
					return null;
				Rect bounds = new Rect();
				tempNode.getBoundsInScreen(bounds);
				if (bounds.bottom > bottom) {
					bottom = bounds.bottom;
					lastNode = tempNode;
					signature.others = text.equals(WECHAT_VIEW_OTHERS_CH);
				}
			}
		}
		return lastNode;
	}

	@Override
	public void onServiceConnected() {
		super.onServiceConnected();
		this.watchFlagsFromPreference();
		Log.i("sort", "onServiceConnected--------");
	}

	private void watchFlagsFromPreference() {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		this.powerUtil = new PowerUtil(this);
		Boolean watchOnLockFlag = sharedPreferences.getBoolean(
				"pref_watch_on_lock", false);
		this.powerUtil.handleWakeLock(watchOnLockFlag);
		
		Log.i("sort", "watchFlagsFromPreference..");
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("pref_watch_on_lock")) {
			Log.i("sort", "------------tttttt--------------");
			 Boolean changedValue = Boolean.valueOf(sharedPreferences.getBoolean(key, false));
	         powerUtil.handleWakeLock(changedValue.booleanValue());
		}
		
        Log.i("sort", "onSharedPreferenceChanged()--");
	}

	@Override
	public void onDestroy() {
		this.powerUtil.handleWakeLock(false);
		super.onDestroy();
	}

	private String generateCommentString() {
		Log.i("sort", "generateCommentString");
		if (!signature.others)
			return null;

		Boolean needComment = sharedPreferences.getBoolean(
				"pref_comment_switch", false);
		if (!needComment)
			return null;

		String[] wordsArray = sharedPreferences.getString("pref_comment_words",
				"").split(" +");
		if (wordsArray.length == 0)
			return null;

		Boolean atSender = sharedPreferences.getBoolean("pref_comment_at",
				false);
		if (atSender) {
			return "@" + signature.sender + " "
					+ wordsArray[(int) (Math.random() * wordsArray.length)];
		} else {
			return wordsArray[(int) (Math.random() * wordsArray.length)];
		}
	}

	/**
	 * ������ִ��AccessibilityNodeInfo.findAccessibilityNodeInfosByText(text).
	 * �����������Ӱ������,��������Ҫƥ�������һ����,���緵��
	 * 
	 * @param nodeInfo
	 *            ���ڸ��ڵ�
	 * @param texts
	 *            ��Ҫƥ����ַ�����
	 * @return ƥ�䵽�Ľڵ�����
	 */
	@SuppressLint("NewApi")
	private List<AccessibilityNodeInfo> findAccessibilityNodeInfosByTexts(
			
			AccessibilityNodeInfo nodeInfo, String[] texts) {
		Log.i("sort", "findAccessibilityNodeInfosByTexts");
		try {
			for (String text : texts) {
				if (text == null)
					continue;

				List<AccessibilityNodeInfo> nodes = nodeInfo
						.findAccessibilityNodeInfosByText(text);

				if (!nodes.isEmpty())
					return nodes;
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new ArrayList<AccessibilityNodeInfo>();
	}
	
}
