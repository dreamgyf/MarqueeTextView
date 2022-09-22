package com.dreamgyf.android.ui.widget.textview.marquee;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @Author: dreamgyf
 * @Data: 2021/5/19
 */
public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {

	private final static int DEFAULT_SPACE = 100;

	private final static int DEFAULT_SPEED = 1;

	private TextView mTextView;

	private Bitmap mBitmap;

	private final Handler mHandler = new Handler(Looper.getMainLooper());

	private final Runnable mMarqueeRunnable = new Runnable() {
		@Override
		public void run() {
			invalidate();
			mLeftX -= mSpeed;
			mHandler.postDelayed(this, 15);
		}
	};

	private int mLeftX = 0;

	/**
	 * 文字滚动时，头尾的最小间隔距离
	 */
	private int mSpace = DEFAULT_SPACE;

	/**
	 * 文字滚动速度
	 */
	private int mSpeed = DEFAULT_SPEED;

	public MarqueeTextView(@NonNull Context context) {
		super(context);
		init(null);
	}

	public MarqueeTextView(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	private void init(AttributeSet attrs) {
		if (attrs != null) {
			TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.MarqueeTextView);
			mSpace = typedArray.getDimensionPixelSize(R.styleable.MarqueeTextView_space, DEFAULT_SPACE);
			mSpeed = typedArray.getInteger(R.styleable.MarqueeTextView_speed, DEFAULT_SPEED);
			typedArray.recycle();
		}

		mTextView = new TextView(getContext(), attrs);
		mTextView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		mTextView.setMaxLines(1);
		setMaxLines(1);

		mTextView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				updateBitmap();
				restartScroll();
			}
		});
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		super.setText(text, type);
		//执行父类构造函数时，如果AttributeSet中有text参数会先调用setText，此时mTextView尚未初始化
		if (mTextView != null) {
			mTextView.setText(text);
			requestLayout();
		}
	}

	@Override
	public void setTextSize(int unit, float size) {
		super.setTextSize(unit, size);
		//执行父类构造函数时，如果AttributeSet中有textSize参数会先调用setTextSize，此时mTextView尚未初始化
		if (mTextView != null) {
			mTextView.setTextSize(size);
			requestLayout();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mTextView.measure(MeasureSpec.UNSPECIFIED, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		mTextView.layout(left, top, left + mTextView.getMeasuredWidth(), bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (mBitmap != null) {
			if (mTextView.getMeasuredWidth() <= getWidth()) {
				int space = mSpace - (getWidth() - mTextView.getMeasuredWidth());
				if (space < 0) {
					space = 0;
				}

				if (mLeftX < -getWidth() - space) {
					mLeftX += getWidth() + space;
				}

				canvas.drawBitmap(mBitmap, mLeftX, 0, getPaint());
				if (mLeftX < 0) {
					canvas.drawBitmap(mBitmap, getWidth() + mLeftX + space, 0, getPaint());
				}
			} else {
				if (mLeftX < -mTextView.getMeasuredWidth() - mSpace) {
					mLeftX += mTextView.getMeasuredWidth() + mSpace;
				}

				canvas.drawBitmap(mBitmap, mLeftX, 0, getPaint());
				if (mLeftX + (mTextView.getMeasuredWidth() - getWidth()) < 0) {
					canvas.drawBitmap(mBitmap, mTextView.getMeasuredWidth() + mLeftX + mSpace, 0, getPaint());
				}
			}
		}
	}

	private void updateBitmap() {
		mBitmap = Bitmap.createBitmap(mTextView.getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(mBitmap);
		mTextView.draw(canvas);
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mHandler.removeCallbacksAndMessages(null);
	}

	public void startScroll() {
		mHandler.post(mMarqueeRunnable);
	}

	public void pauseScroll() {
		mHandler.removeCallbacks(mMarqueeRunnable);
	}

	public void stopScroll() {
		mLeftX = 0;
		mHandler.removeCallbacks(mMarqueeRunnable);
	}

	public void restartScroll() {
		stopScroll();
		startScroll();
	}
}
