package dyhelper.com.util;

import java.util.List;

import android.annotation.SuppressLint;
import android.graphics.Rect;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import dyhelper.com.services.HongbaoService;

/**
 * Created by Zhongyi on 1/21/16.
 */
public class HongbaoSignature {
	public String sender, content, time, contentDescription = "",
			commentString;
	public boolean others;

	@SuppressLint("NewApi")
	public boolean generateSignature(AccessibilityNodeInfo node,
			String excludeWords, AccessibilityEvent event) {
		try {
			Log.i("qhb", "0");
			/*
			 * The hongbao container node. It should be a LinearLayout. By
			 * specifying that, we can avoid text messages.
			 */
			AccessibilityNodeInfo hongbaoNode = node.getParent();
			if (!"android.widget.LinearLayout".equals(hongbaoNode
					.getClassName()))
				return false;
			Log.i("qhb", "1");
			/* The text in the hongbao. Should mean something. */
			String hongbaoContent = hongbaoNode.getChild(0).getText()
					.toString();
			if (hongbaoContent == null || "查看红包".equals(hongbaoContent))
				return false;
			Log.i("qhb", "2");
			/* Check the user's exclude words list. */
			String[] excludeWordsArray = excludeWords.split(" +");
			for (String word : excludeWordsArray) {
				if (word.length() > 0 && hongbaoContent.contains(word))
					return false;
			}
			Log.i("qhb", "3");
			/*
			 * The container node for a piece of message. It should be inside
			 * the screen. Or sometimes it will get opened twice while
			 * scrolling.
			 */
			AccessibilityNodeInfo messageNode = hongbaoNode.getParent();

			Rect bounds = new Rect();
			messageNode.getBoundsInScreen(bounds);
			if (bounds.top < 0)
				return false;
			Log.i("qhb", "4");

			/* The sender and possible timestamp. Should mean something too. */
			// String[] hongbaoInfo =
			// getSenderContentDescriptionFromNode(messageNode);
			// String getSignature = this.getSignature(hongbaoInfo[0],
			// hongbaoContent, hongbaoInfo[1]);
			// Log.i("qhb", "getSignature:" + getSignature);
			// Log.i("qhb", "toString:" + this.toString());
			// if (getSignature.equals(this.toString()))
			// return false;
			Log.i("qhb", "getEventType:" + event.getEventType());

			// if (!checkPosition()) {
			// return false;
			// }

			Log.i("qhb", "5");
			/* So far we make sure it's a valid new coming hongbao. */
			// this.sender = hongbaoInfo[0];
			// this.time = hongbaoInfo[1];
			this.content = hongbaoContent;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@SuppressLint("NewApi")
	private boolean checkPosition() {
		AccessibilityNodeInfo info = HongbaoService.hongbaoService
				.getRootInActiveWindow();
		if (info == null)
			return false;
		int hongbaoBottom = 0;
		int lingquBottom = 0;
		List<AccessibilityNodeInfo> lingqu = info
				.findAccessibilityNodeInfosByText("你领取了");
		if (lingqu != null && lingqu.size() > 0) {
			AccessibilityNodeInfo lastlingqu = lingqu.get(lingqu.size() - 1);
			Rect lastlingqubounds = new Rect();
			lastlingqu.getBoundsInScreen(lastlingqubounds);
			lingquBottom = lastlingqubounds.bottom;
		}
		List<AccessibilityNodeInfo> hongbao = info
				.findAccessibilityNodeInfosByText("领取红包");
		if (hongbao != null && hongbao.size() > 0) {
			AccessibilityNodeInfo lasthongbao = hongbao.get(hongbao.size() - 1);
			Rect lasthongbaobounds = new Rect();
			lasthongbao.getBoundsInScreen(lasthongbaobounds);
			hongbaoBottom = lasthongbaobounds.bottom;

		}
		Log.i("qhb", "lingquBottom:" + lingquBottom);
		Log.i("qhb", "hongbaoBottom:" + hongbaoBottom);
		if (lingquBottom > hongbaoBottom) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.getSignature(this.sender, this.content, this.time);
	}

	private String getSignature(String... strings) {
		String signature = "";
		for (String str : strings) {
			if (str == null)
				return null;
			signature += str + "|";
		}

		return signature.substring(0, signature.length() - 1);
	}

	public String getContentDescription() {
		return this.contentDescription;
	}

	public void setContentDescription(String description) {
		this.contentDescription = description;
	}

	@SuppressLint("NewApi")
	private String[] getSenderContentDescriptionFromNode(
			AccessibilityNodeInfo node) {
		int count = node.getChildCount();
		String[] result = { "unknownSender", "unknownTime" };
		for (int i = 0; i < count; i++) {
			AccessibilityNodeInfo thisNode = node.getChild(i);
			if ("android.widget.ImageView".equals(thisNode.getClassName())
					&& "unknownSender".equals(result[0])) {
				CharSequence contentDescription = thisNode
						.getContentDescription();
				if (contentDescription != null)
					result[0] = contentDescription.toString().replaceAll("头像$",
							"");
			} else if ("android.widget.TextView"
					.equals(thisNode.getClassName())
					&& "unknownTime".equals(result[1])) {
				CharSequence thisNodeText = thisNode.getText();
				if (thisNodeText != null)
					result[1] = thisNodeText.toString();
			}
		}
		return result;
	}

	public void cleanSignature() {
		this.content = "";
		this.time = "";
		this.sender = "";
	}

}
