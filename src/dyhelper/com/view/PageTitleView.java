package dyhelper.com.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import dyhelper.com.listeners.IOnScrollListener;

public class PageTitleView extends View implements IOnScrollListener {
	
	private Paint paint;
	
	private int width, height;
	
	private List<String> allTitles = new ArrayList<String>();
	
	private int currentPosition = 0;
	
	private TextView textView;
	
	private float mDensity;
	private float mScaleDensity;
	
	public static final int DEFAULT_TEXT_SIZE = 14;
	
	public static final float DEFAULT_MARGIN = 8;
	
	public static final int DEAFAULT_CAN_SEE_COUNT = 3;
	
	public static final float DEAFAULT_EXTRA_HEIGHT = 8.0f;
	
	public static final float DEAFAULT_EXTRA_WIDTH = 12.0f;
	
	public static final int DEFAULT_UNSELECTED_COLOR = Color.BLACK;
	
	public static final int DEFAULT_SELECTED_COLOR = Color.WHITE;
	
	private int selected_color = DEFAULT_SELECTED_COLOR;
	private int unselected_color = DEFAULT_UNSELECTED_COLOR;
	
	
	private float margin = DEFAULT_MARGIN;
	
	private float bigTxtSize;
	
	private float mIndicatorSize;
	
	private float txtSize = DEFAULT_TEXT_SIZE;
	
	private int canSeeCount = DEAFAULT_CAN_SEE_COUNT;
	
	private Path mPath_3;
	
	private int screenWidth = 0;
	
	private float extraHeight = DEAFAULT_EXTRA_HEIGHT;
	
	private float extraWidth = DEAFAULT_EXTRA_WIDTH;
	
	
	public void clear() {
		allTitles.clear();
	}
	
	
	public PageTitleView(Context context) {
		this(context, null);
	}

	
	public PageTitleView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	public PageTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initScreenInfo();
		initPaint();
		initData();
	}
	
	public void setExtraHeight(float extraHeight) {
		this.extraHeight = extraHeight * mDensity;
	}
	
	public void setExtraWidth(float extraWidth) {
		this.extraWidth = extraWidth * extraWidth;
	}
	
	private void initData() {
		textView = new TextView(getContext());
		textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		textView.getPaint().setColor(Color.WHITE);
		
		extraHeight *= mDensity;
		
		extraWidth *= mDensity;
		
		txtSize *= mScaleDensity;
		
		margin = margin * mDensity;
		
		bigTxtSize = txtSize + 1 * mScaleDensity;
		
		mIndicatorSize = bigTxtSize;
		
		mPath_3 = new Path();
	}


	private void initScreenInfo() {
		DisplayMetrics outMetrics = getResources().getDisplayMetrics();
		mDensity = outMetrics.density;
		mScaleDensity = outMetrics.scaledDensity;
		screenWidth = outMetrics.widthPixels;
	}


	public void setTitles(List<String> listTitles) {
		if(listTitles != null && !listTitles.isEmpty()) {
			allTitles.clear();
			allTitles.addAll(listTitles);
		}
	}
	
	public void setSelectedColor(int color) {
		selected_color = color;
	}
	
	public void setUnselectedColor(int color) {
		unselected_color = color;
	}
	
	public void setCanSeeCount(int count) {
		canSeeCount = count;
	}
	
	public void setSize(float size) {
		
		txtSize = size * mScaleDensity;
		
		bigTxtSize = txtSize + 1 * mScaleDensity;
		
		mIndicatorSize = bigTxtSize;
		
	}
	
	public float getSize() {
		return txtSize;
	}


	private void initPaint() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setDither(true);
		paint.setStyle(Paint.Style.FILL);
	}
	
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		width = w;
		height = h;
		float x = (float) (Math.cos(Math.PI/6) * mIndicatorSize);
		leftIndicatorRectF.set(0, 0, x + extraWidth, height);
		rightIndicatorRectF.set(width - x - extraWidth, 0, width, height);
		
	}
	
	private static final int NOT_INDICATOR = 0;
	private static final int LEFT_INDICATOR = 1;
	private static final int RIGHT_INDICATOR = 2;
	private int clickIndicator = 0;
	private RectF leftIndicatorRectF = new RectF();
	private RectF rightIndicatorRectF = new RectF();
	
	private boolean isLeftConfirm = false, isRightConfirm;
	
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if(!isIndicatorEnable) {
			return super.onTouchEvent(event);
		}
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		switch(action) {
		case MotionEvent.ACTION_DOWN:
			isLeftConfirm = false;
			isRightConfirm = false;
			if(currentPosition > 0 && leftIndicatorRectF.contains(x, y)) {
				clickIndicator = LEFT_INDICATOR;
				invalidate();
			} else if(currentPosition < allTitles.size() - 1 && rightIndicatorRectF.contains(x, y)) {
				clickIndicator = RIGHT_INDICATOR;
				invalidate();
			} else {
				clickIndicator = NOT_INDICATOR;
			}
			
			
			break;
			
		case MotionEvent.ACTION_MOVE:
			/*if(clickIndicator == NOT_INDICATOR) {
				if(leftIndicatorRectF.contains(x, y)) {
				clickIndicator = LEFT_INDICATOR;
					invalidate();
				} else if(rightIndicatorRectF.contains(x, y)) {
					clickIndicator = RIGHT_INDICATOR;
					invalidate();
				}
			}
			
			if(clickIndicator == LEFT_INDICATOR) {
				if(rightIndicatorRectF.contains(x, y)) {
					clickIndicator = RIGHT_INDICATOR;
					invalidate();
				} else if(!leftIndicatorRectF.contains(x, y)){
					clickIndicator = NOT_INDICATOR;
					invalidate();
				}
				
				
			}
			
			if(clickIndicator == RIGHT_INDICATOR) {
				if(leftIndicatorRectF.contains(x, y)) {
					clickIndicator = LEFT_INDICATOR;
					invalidate();
				} else if(!rightIndicatorRectF.contains(x, y)){
					clickIndicator = NOT_INDICATOR;
					invalidate();
				}
			}*/
			
			if(clickIndicator == LEFT_INDICATOR || clickIndicator == RIGHT_INDICATOR) {
				if(!leftIndicatorRectF.contains(x, y) && !rightIndicatorRectF.contains(x, y)) {
					clickIndicator = NOT_INDICATOR;
					invalidate();
				}
			}
			
			break;
			
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			if(currentPosition > 0 && clickIndicator == LEFT_INDICATOR && leftIndicatorRectF.contains(x, y)) {
				isLeftConfirm = true;
				int selectedPos = currentPosition - 1;
				setCurrentPosition(selectedPos);
				if(selectedPos >= 0 && selectedPos < allTitles.size() && onSelectListener != null) {
					onSelectListener.selected(selectedPos);
				}
			} else if(currentPosition < allTitles.size() - 1 && clickIndicator == RIGHT_INDICATOR && rightIndicatorRectF.contains(x, y)) {
				int selectedPos = currentPosition + 1;
				isRightConfirm = true;
				setCurrentPosition(selectedPos);
				if(selectedPos >= 0 && selectedPos < allTitles.size() && onSelectListener != null) {
					onSelectListener.selected(selectedPos);
				}
			} else {
				isLeftConfirm = false;
				isRightConfirm = false;
			}
			clickIndicator = NOT_INDICATOR;
			invalidate();
			break;
		}
		
		return true;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		int count = allTitles.size();
		if(count == 0) {
			super.onDraw(canvas);
			return;
		}
		final Paint paint = textView.getPaint();
		paint.setColor(Color.WHITE);
		float baseY = (height - (textView.getPaint().ascent() + textView.getPaint().descent()))/2;
		float startExtraOffsetSize = extraWidth * 0.5f;
		if(count == 1) {
			canvas.drawText(allTitles.get(0), startExtraOffsetSize, baseY, paint);
			return;
		}
		
		
		float x = (float) (Math.cos(Math.PI/6) * mIndicatorSize);
		if(isIndicatorEnable) {
			mPath_3.moveTo(startExtraOffsetSize, height * 0.5f);
			mPath_3.lineTo(startExtraOffsetSize + x, (height - mIndicatorSize) * 0.5f);
			mPath_3.lineTo(startExtraOffsetSize + x, mIndicatorSize + (height - mIndicatorSize) * 0.5f);
			mPath_3.close();
			
			if(clickIndicator == LEFT_INDICATOR) {
				paint.setAlpha(88);
			} else {
				paint.setAlpha(255);
			}
			canvas.drawPath(mPath_3, paint);
			
			
			mPath_3.reset();
			mPath_3.moveTo(width - startExtraOffsetSize, height * 0.5f);
			mPath_3.lineTo(width - x - startExtraOffsetSize, (height - mIndicatorSize) * 0.5f);
			mPath_3.lineTo(width - x - startExtraOffsetSize, mIndicatorSize + (height - mIndicatorSize) * 0.5f);
			mPath_3.close();
			
			if(clickIndicator == RIGHT_INDICATOR) {
				paint.setAlpha(88);
			} else {
				paint.setAlpha(255);
			}
			canvas.drawPath(mPath_3, paint);
			
			
			mPath_3.reset();
		}
		
		float currentWidth = 0;
		
		if(canSeeCount == 1) {
			paint.setColor(selected_color);
			paint.setTextSize(bigTxtSize);
			if(isIndicatorEnable) {
				currentWidth += x + startExtraOffsetSize;
				currentWidth += margin;
			}
			canvas.drawText(allTitles.get(currentPosition), currentWidth, baseY, paint);
		} else {
			for(int i = mPositions[0]; i < mPositions[0] + mPositions[1]; i++) {
				if(currentPosition == i) {
					paint.setColor(selected_color);
					paint.setTextSize(bigTxtSize);
				} else {
					paint.setColor(unselected_color);
					paint.setTextSize(txtSize);
				}
				if(i == mPositions[0]) {
					if(isIndicatorEnable) {
						currentWidth += x + startExtraOffsetSize;
						currentWidth += margin;
					}
				} else {
					currentWidth += margin;
				}
				canvas.drawText(allTitles.get(i), currentWidth, baseY, paint);
				float textWidth = paint.measureText(allTitles.get(i));
				currentWidth += textWidth;
			}
		}
		
	}
	
	private boolean isLeft, isLeftBorder, isRight, isRightBorder;
	
	public void setCurrentPosition(int position) {
		if(position < 0 || position > allTitles.size() - 1) {
			Log.i("aaaa", "setCurrentPosition: " + position);
			Log.i("aaaa", "allTitles.size(): " + allTitles.size());
			return;
		}
		Log.i("aaaa", "-----------------------------------");
		Log.i("aaaa", "--setCurrentPosition: " + position);
		Log.i("aaaa", "--allTitles.size(): " + allTitles.size());
		
		value = 0.0f;
		if(position > currentPosition) {
			isLeft = true;
			isRight = false;
			
			if(mPositions[0] + mPositions[1] - 1 == currentPosition) {
				isRightBorder = true;
			} else {
				isRightBorder = false;
			}
			
		} else if(position < currentPosition) {
			isRight = true;
			isLeft = false;
			
			if(mPositions[0] == currentPosition) {
				isLeftBorder = true;
			} else {
				isLeftBorder = false;
			}
			
		} else {
			isRight = false;
			isLeft = false;
		}
		currentPosition = changePosition = position;
		requestLayout();
		invalidate();
	}
	
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		
		if(parentWidth <= 0) {
			parentWidth = screenWidth;
		}
		
		int count = allTitles.size();
		if(count == 0) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		
		int exceptWidth = 0;
		int exceptHeight = (int) (bigTxtSize + extraHeight + 0.5f);
		
		if(count == 1) {
			textView.getPaint().setTextSize(bigTxtSize);
			float textWidth = textView.getPaint().measureText(allTitles.get(0));
			exceptWidth = (int) (textWidth + extraWidth + 0.5f);
			exceptHeight = (int) (getSize() + extraHeight + 0.5f);
		} else {
			
			int seeCount = canSeeCount;
			int[] positions = computerPosition(count, seeCount);
			exceptWidth = (int) (computerWidth(positions[0], positions[1]) + extraWidth + 0.5f);
			mPositions = positions;
			
			Log.i("chaochao", "canseeCount:" + canSeeCount);
			while(parentWidth < exceptWidth) {
				seeCount = --canSeeCount;
				if(seeCount <= 0) {
					exceptWidth = 0;
					break;
				}
				positions = computerPosition(count, seeCount);
				exceptWidth = (int) (computerWidth(positions[0], positions[1]) + extraWidth + 0.5f);
				mPositions = positions;
			}
			
			Log.i("chaochao", "mPositions[0]:" + mPositions[0]);
			Log.i("chaochao", "mPositions[1]:" + mPositions[1]);
			
			//´ý½â¾ö
			/*if(count >= 4) {
				if(currentPosition > 0 && currentPosition == startIndex && changePosition < currentPosition) {
					String seeFirthTitle = allTitles.get(startIndex);
					
				}
				
				if(currentPosition < allTitles.size() - 1 && currentPosition == startIndex + seeCount - 1 && changePosition >= currentPosition) {
					
				}
			}*/
			
		}
		setMeasuredDimension(exceptWidth, exceptHeight);
		
	}
	
	private int[] mPositions = {0, 0};
	private boolean isIndicatorEnable = true;
	
	private float computerWidth(int startIndex, int seeCount) {
		float resultWidth = 0;
		if(seeCount == 1) {
			String text = allTitles.get(currentPosition);
			textView.getPaint().setTextSize(bigTxtSize);
			float textWidth = textView.getPaint().measureText(text);
			resultWidth += textWidth;
		} else {
			for(int i = startIndex; i < startIndex + seeCount; i++) {
				String text = allTitles.get(i);
				if(i == currentPosition) {
					textView.getPaint().setTextSize(bigTxtSize);
				} else {
					textView.getPaint().setTextSize(txtSize);
				}
				float textWidth = textView.getPaint().measureText(text);
				resultWidth += textWidth;
			}
		}
		
		float indicatorLength = (float) (Math.cos(Math.PI/6) * mIndicatorSize);
		resultWidth = resultWidth + margin * (seeCount + 1) + (isIndicatorEnable ? 2 * indicatorLength : -2 * margin);
		return resultWidth;
	}
	
	public void setIndicatorEnable(boolean isEnable) {
		isIndicatorEnable = isEnable;
	}
	
	private int[] computerPosition(int totalCount, int canSeeCnt) {
		int startIndex = 0;
		int seeCount = canSeeCnt;
		
		if(canSeeCnt > totalCount)
			canSeeCnt = totalCount;
		
		seeCount = canSeeCnt;
		
		if(totalCount > 2) {
			
			startIndex = currentPosition - (seeCount - 1);
			if(startIndex <= 0) {
				startIndex = 0;
			}
			
			//¼Ó
			if(isLeft) {
				if(!isRightBorder) {
					startIndex = mPositions[0];
				}
			//¼õ
			} else if(isRight) {
				if(isLeftBorder) {
					startIndex = currentPosition;
				} else {
					startIndex = mPositions[0];
				}
			}
			
		}
		
		
		return new int[]{startIndex, seeCount};
	}


	private IOnSelectListener onSelectListener;
    
    public void setOnSelectListener(IOnSelectListener listener) {
    	onSelectListener = listener;
    }
    
    public interface IOnSelectListener {
    	public void selected(int position);
    }
    
    private float value = 0;
    private int changePosition = 0;

	@Override
	public void setValue(float value, int position) {
		this.value = value;
		changePosition = position;
		requestLayout();
		invalidate();
	}
	

}
