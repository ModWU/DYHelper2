package dyhelper.com.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;

public class BitmapMeshLayout extends FrameLayout {

	/*private static int WIDTH = 40;
	private static int HEIGHT = 40;*/

	private Paint mPaint = new Paint();
	
	private ArrayList<RectF> showRectFs = new ArrayList<RectF>();
	private List<BitmapInfo> bitmapInfos = Collections.synchronizedList(new LinkedList<BitmapInfo>());
	private LinkedList<BitmapInfo> tempBmpInfos = new LinkedList<BitmapInfo>();
	private static final int BITMAP_MAX_COUNT = 18;
	private static final int BITMAP_MIN_COUNT = 16;
	
	private float mDensity;
	
	private int width;
	private int height;
	
	
	
	private static class BitmapInfo {
		public float currentValue;
		public float totalValue;
		public float alpha;
		public int color;
		public float totalSize;
		public float currrentSize;
		public float currentLeft;
		public float currentTop;
		public float startLeft, startTop;
		
		
		public BitmapInfo(float currentValue, float totalValue, float alpha, float totalSize, float currentSize, float currentLeft,
				float currentTop, float startLeft, float startTop, int color) {
			this.currentValue = currentValue;
			this.totalValue = totalValue;
			this.alpha = alpha;
			this.totalSize = totalSize;
			this.currrentSize = currentSize;
			this.currentLeft = currentLeft;
			this.currentTop = currentTop;
			this.startLeft = startLeft;
			this.startTop = startTop;
			this.color = color;
		}

		public BitmapInfo() {}
		BitmapInfo copy() {
			return new BitmapInfo(currentValue, totalValue, alpha, totalSize, currrentSize, currentLeft, currentTop, startLeft, startTop, color);
		}
	}
	
	public BitmapMeshLayout(Context context) {
		this(context, null);
		
	}
	
	public BitmapMeshLayout(Context context, AttributeSet attrSet) {
		this(context, attrSet, 0);
	}
	
	public BitmapMeshLayout(Context context, AttributeSet attSet, int style) {
		super(context, attSet, style);

		obtainScreenInfo();
		
		initData();
		
	}
	
	public void initData() {
		setFocusable(true);
		
		mPaint.setStyle(Paint.Style.FILL);
		mPaint.setColor(Color.RED);
		mPaint.setAntiAlias(true);
		mPaint.setDither(true);
	}
	
	private void obtainShowRectFs() {
		showRectFs.clear();
		
		float r_5_1 = 2.0f/3;
		float r_5_2 = 1.0f/6;
		
		float size = Math.min(width, height);
		float offsetX = 0;
		float offsetY = 0;
		if(width > height) {
			offsetX = (width - height) * 0.5f;
		} else if(width < height) {
			offsetY = (height - width) * 0.5f;
		}
		
		RectF rectF = new RectF(offsetX, offsetY, offsetX + size * r_5_2, offsetY + size * r_5_2);
		RectF rectF2 = new RectF(rectF.right, rectF.top, rectF.right + size * r_5_1, rectF.bottom);
		RectF rectF3 = new RectF(rectF2.right, rectF2.top, rectF2.right + size * r_5_2, rectF2.bottom);
		RectF rectF4 = new RectF(rectF.left, rectF.bottom, rectF.right, rectF.bottom + size * r_5_1);
		RectF rectF5 = new RectF(rectF3.left, rectF3.bottom, rectF3.right, rectF3.bottom + size * r_5_1);
		RectF rectF6 = new RectF(rectF4.left, rectF4.bottom, rectF4.right, rectF4.bottom + size * r_5_2);
		RectF rectF7 = new RectF(rectF6.right, rectF6.top, rectF6.right + size * r_5_1, rectF6.bottom);
		RectF rectF8 = new RectF(rectF7.right, rectF7.top, rectF7.right + size * r_5_2, rectF7.bottom);
		showRectFs.add(rectF);
		showRectFs.add(rectF2);
		showRectFs.add(rectF3);
		showRectFs.add(rectF4);
		showRectFs.add(rectF5);
		showRectFs.add(rectF6);
		showRectFs.add(rectF7);
		showRectFs.add(rectF8);
		
	}

	private void obtainScreenInfo() {
		mDensity = getResources().getDisplayMetrics().density;
		
	}

	private void offerDifferentBitmaps() {
		
		
		final int count = (int) (Math.random() * (BITMAP_MAX_COUNT + 1 - BITMAP_MIN_COUNT) + BITMAP_MIN_COUNT);
		for(int i = 0; i < count; i++) {
			BitmapInfo bmpInfo = offerOneBitmapInfo();
			
			//Æ«ÒÆ•rég
			bmpInfo.currentValue = (float) (Math.random() * -1);
			
			bitmapInfos.add(bmpInfo);
		}
		
		
	}
	
	private BitmapInfo offerOneBitmapInfo() {
		BitmapInfo bmpInfo = new BitmapInfo();
		getBitmapPosition(bmpInfo);
		bmpInfo.alpha = 0.5f;
		bmpInfo.totalSize = bmpInfo.currrentSize = (float) (Math.random() * (8 * mDensity - 4 * mDensity) + 4 * mDensity);
		bmpInfo.color = 0xffffffff;
		return bmpInfo;
	}
	
	private void getBitmapPosition(BitmapInfo bmpInfo) {
		int index = (int)(Math.random() * showRectFs.size());
		RectF rectf = showRectFs.get(index);
		
		float left = (float) (Math.random() * (rectf.right - rectf.left) + rectf.left);
		float top = (float) (Math.random() * (rectf.bottom - rectf.top) + rectf.top);
		bmpInfo.startLeft = left;
		bmpInfo.startTop = top;
		bmpInfo.currentLeft = left;
		bmpInfo.currentTop = top;
	}



	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		
		width = w;
		height = h;
		
		obtainShowRectFs();
		
		//buildPaths(getWidth() * 0.5f, getHeight() * 0.5f);
	}
	
	private void readyBuildBitmaps() {
		offerDifferentBitmaps();
	}
	
	
	
	
	private static final int STATE_RUN = 0;
	private static final int STATE_STOP = 1;
	private volatile boolean isStop = false;
	
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch(msg.what) {
			case STATE_RUN:
				LinkedList<BitmapInfo> bmpList = (LinkedList<BitmapInfo>) msg.obj;
				tempBmpInfos = bmpList;
				invalidate();
				break;
				
			case STATE_STOP:
				synchronized (stopLock) {
					mHandler.removeMessages(STATE_RUN);
					isStop = true;
					if(timer != null) {
						timer.cancel();
						timer = null;
					}
					if(timer != null) {
						timer.cancel();
						timer = null;
					}
					bitmapInfos.clear();
					tempBmpInfos.clear();
					invalidate();
				}
				break;
			}
			
		};
	};
	
	private Timer timer;
	
	public void stop() {
		if(isStop) {
			return;
		}
		
		isStop = true;
		
		Message msg = new Message();
		msg.what = STATE_STOP;
		mHandler.sendMessage(msg);
	}
	
	
	
	private long delayTime = 5;
	
	private final Object stopLock = new Object();
	
	
	public void run(long firstDelayTime, long delayTime) {
		if(timer != null) 
			timer.cancel();
		isStop = false;
		readyBuildBitmaps();
		timer = new Timer();
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				doTask();
			}
			
		}, firstDelayTime, delayTime);
		
		this.delayTime = delayTime;
	}
	
	private void doTask() {
		if(isStop || timer == null || bitmapInfos == null)
			return;
		
		synchronized (stopLock) {
			if(isStop || timer == null || bitmapInfos == null) {
				return;
			}
			int removeCount = 0;
			LinkedList<BitmapInfo> copyBitmapInfo = new LinkedList<BitmapInfo>();
			for(int i = bitmapInfos.size() - 1; i >= 0; i--) {
				BitmapInfo bmpInfo = bitmapInfos.get(i);
				if(bmpInfo.currentValue >= 1.0f) {
					bitmapInfos.remove(i);
					removeCount++;
					continue;
				}
				
				measureBitmapInfo(bmpInfo);
				
				copyBitmapInfo.add(bmpInfo.copy());
				
			}
			
			while(removeCount-- > 0) {
				BitmapInfo bitmapInfo = offerOneBitmapInfo();
				bitmapInfos.add(bitmapInfo);
				measureBitmapInfo(bitmapInfo);
				copyBitmapInfo.add(bitmapInfo.copy());
			}
			
			Message msg = new Message();
			msg.what = STATE_RUN;
			msg.obj = copyBitmapInfo;
			
			mHandler.sendMessage(msg);
		}
	}
	
	private void measureBitmapInfo(BitmapInfo bmpInfo) {
		if(bmpInfo.currentValue > 1.0f) {
			bmpInfo.currentValue = 1.0f;
		}
		bmpInfo.currrentSize = (1 - bmpInfo.currentValue) * bmpInfo.totalSize;
		
		
		float centerX = getWidth() * 0.5f - bmpInfo.currrentSize * 0.5f;
		float centerY = getHeight() * 0.5f - bmpInfo.currrentSize * 0.5f;
		//´¦Àí¾àÀë
		float x = (float) (bmpInfo.startLeft + (centerX - bmpInfo.startLeft) * bmpInfo.currentValue);
		
		float value1 = x * (centerY - bmpInfo.startTop) + (centerX *bmpInfo.startTop - centerY * bmpInfo.startLeft);
		float value2 = centerX - bmpInfo.startLeft;
		
		float y = value2 == 0 ? centerY : (value1/value2);
		
		bmpInfo.currentLeft = x;
		bmpInfo.currentTop = y;
		
		
		Log.i("chaochao", "currentValue: " + bmpInfo.currentValue);
		Log.i("chaochao", "centerX: " + centerX);
		Log.i("chaochao", "x: " + x);
		
		if(bmpInfo.currentValue >= 0.0f && bmpInfo.currentValue < 0.5f) {
			bmpInfo.alpha = 0.5f + bmpInfo.currentValue;
			//bmpInfo.alpha = 0.5f + bmpInfo.currentValue;
			//bmpInfo.alpha = 0.8f + bmpInfo.currentValue;
		} else {
			bmpInfo.alpha = 2 * (1.0f - bmpInfo.currentValue);
			//bmpInfo.alpha = 2 * (1.0f - bmpInfo.currentValue);
			//bmpInfo.alpha = 1.25f * (1.0f - bmpInfo.currentValue);
		}
		
		if(bmpInfo.currentValue < 0) {
			bmpInfo.alpha = 0;
		}
		
		if(bmpInfo.alpha < 0) {
			bmpInfo.alpha = 0;
		}
		
		bmpInfo.currentValue += 0.004f;
	}
	
	public void run() {
		run(0, 3);
	}
	
	
	@SuppressLint("NewApi")
	@Override
	protected void dispatchDraw(Canvas canvas) {
		
		if(!isStop) {
			
			while(!tempBmpInfos.isEmpty()) {
				BitmapInfo bmpInfo = tempBmpInfos.pop();
				mPaint.setColor(bmpInfo.color);
				mPaint.setAlpha((int)(bmpInfo.alpha * 255));
				
				float centerX = bmpInfo.currentLeft + bmpInfo.currrentSize * 0.5f;
				float centerY = bmpInfo.currentTop + bmpInfo.currrentSize * 0.5f;
				
				canvas.drawCircle(centerX, centerY, bmpInfo.currrentSize, mPaint);
				
			}
			
		}
		
		super.dispatchDraw(canvas);
	}

}
