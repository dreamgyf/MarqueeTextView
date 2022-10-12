package com.dreamgyf.android.ui.widget.textview.marquee;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Choreographer;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dreamgyf.android.utils.ui.ScreenUtilsKt;

/**
 * @Author: dreamgyf
 * @Data: 2021/5/19
 */
public class MarqueeTextView extends androidx.appcompat.widget.AppCompatTextView {

    /**
     * Unit: PX
     */
    private final static int DEFAULT_SPACE = 100;

    /**
     * Unit: DP
     */
    private final static float DEFAULT_SPEED = 0.5f;

    private final static float BASE_FPS = 60f;

    private int mFps = 60;

    private TextView mTextView;

    private Bitmap mBitmap;

    private final Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
            mLeftX -= (BASE_FPS / mFps) * mSpeed;
            invalidate();
            Choreographer.getInstance().postFrameCallback(this);
        }
    };

    private float mLeftX = 0f;

    /**
     * 文字滚动时，头尾的最小间隔距离
     */
    private int mSpace = DEFAULT_SPACE;

    /**
     * 文字滚动速度
     */
    private float mSpeed = DEFAULT_SPEED * 2;

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
            float speedDp = typedArray.getFloat(R.styleable.MarqueeTextView_speed, DEFAULT_SPEED);
            mSpeed = ScreenUtilsKt.dp2pxF(speedDp, getContext());
            typedArray.recycle();
        } else {
            mSpeed = ScreenUtilsKt.dp2pxF(DEFAULT_SPEED, getContext());
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

    private void updateFps() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mFps = (int) getContext().getDisplay().getRefreshRate();
        } else {
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            mFps = (int) windowManager.getDefaultDisplay().getRefreshRate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Choreographer.getInstance().removeFrameCallback(mFrameCallback);
    }

    public void startScroll() {
        updateFps();
        Choreographer.getInstance().postFrameCallback(mFrameCallback);
    }

    public void pauseScroll() {
        Choreographer.getInstance().removeFrameCallback(mFrameCallback);
    }

    public void stopScroll() {
        mLeftX = 0;
        Choreographer.getInstance().removeFrameCallback(mFrameCallback);
    }

    public void restartScroll() {
        stopScroll();
        startScroll();
    }
}
