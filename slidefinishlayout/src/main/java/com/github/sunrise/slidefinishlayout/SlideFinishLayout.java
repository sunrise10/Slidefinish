package com.github.sunrise.slidefinishlayout;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import java.util.Stack;


/**
 * Created by yf on 2017/12/19.
 * 描述：可滑动finish的activity的根布局
 * 注意：只适用于activity的布局
 */
public class SlideFinishLayout extends FrameLayout {
    private String TAG = "SlideFinishLayout";
    private static final float FINALALPHA = 0.6f;
    private static final float FINALSCALE = 0.99f;
    private Activity mActivity;
    private Scroller mScroller;

    private SlideFinishLayout mPreviousSlideLayout;
    private View mPreviousChild;
    private View mContentView;

    private Paint mShadowPaint;
    private Drawable mEdgeShadow;

    private float mAlpha;
    private float mPreWidth;
    private int mUpScrollX;
    private float mUpAlpha;
    private float mScale;
    private float mRotation;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mLastDownX;
    private float mLastDownToX;
    //滑动效果
    private int slideEffect = 1;
    //是否有边缘阴影
    private boolean edgeShadow = true;
    //边缘阴影大小
    private int edgeShadowSize;
    //阴影方向
    private int shadowOrientation;
    //是否可以滑动
    private boolean mSlideEnable = true;

    public SlideFinishLayout(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public SlideFinishLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public SlideFinishLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        mActivity = (Activity) context;
        mScroller = new Scroller(context);
        mShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setColor(Color.BLACK);
        mEdgeShadow = ContextCompat.getDrawable(mActivity, R.drawable.left_shadow);
        init();
    }

    /**
     * 设置滑动的效果
     *
     * @param slideEffectCode 0:无效果 1:微信效果 2:酷狗或今日头条效果 3:酷狗旋转效果
     */
    public void setSlideEffect(int slideEffectCode) {
        if (slideEffectCode < 0 || slideEffectCode > 3) {
            throw new IllegalArgumentException("滑动效果参数有误(0~3)");
        } else {
            slideEffect = slideEffectCode;
        }
    }

    /**
     * 设置activity的左边缘是否有阴影，增加层次感
     *
     * @param edgeShadow
     */
    public void setEdgeShadow(boolean edgeShadow) {
        this.edgeShadow = edgeShadow;
    }

    /**
     * 设置activity的左边缘阴影大小
     *
     * @param edgeShadowSize
     */
    public void setEdgeShadowSize(int edgeShadowSize) {
        this.edgeShadowSize = edgeShadowSize;
    }

    /**
     * 设置阴影的方向
     *
     * @param shadowOrientation 0:无阴影 1:左边 2:右边
     */
    public void setShadowOrientation(int shadowOrientation) {
        if (shadowOrientation < 0 || shadowOrientation > 2) {
            throw new IllegalArgumentException("阴影的方向参数有误(0~2)");
        } else {
            this.shadowOrientation = shadowOrientation;
        }
    }

    /**
     * 设置当前activity是否可以滑动退出
     *
     * @param enable
     */
    public void setSlideEnable(boolean enable) {
        mSlideEnable = enable;
    }

    private void init() {
        Stack<SlideFinishLayout> slideFinishLayoutList = SlideFinishManager.mSlideFinishLayoutList;
        if (slideFinishLayoutList.size() != 0) {
            mPreviousSlideLayout = slideFinishLayoutList.lastElement();
            mPreviousChild = mPreviousSlideLayout.getChildAt(0);
        }
        getScreenResolution();
        edgeShadowSize = mScreenWidth / 20;
    }

    /**
     * 获取屏幕分辨率
     */
    private void getScreenResolution() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    /**
     * 绑定
     */
    public void bind() {
        if (getParent() != null) {
            return;
        }
        ViewGroup decor = (ViewGroup) mActivity.getWindow().getDecorView();
        View decorChild = decor.findViewById(android.R.id.content);
        while (decorChild.getParent() != decor) {
            decorChild = (View) decorChild.getParent();
        }
        decor.removeView(decorChild);
        addView(decorChild);
        decor.addView(this);
        mContentView = getChildAt(0);
    }

    /**
     * 解绑
     */
    public void unBind() {
        if (getParent() == null) {
            return;
        }
        ViewGroup decorChild = (ViewGroup) getChildAt(0);
        ViewGroup decor = (ViewGroup) mActivity.getWindow().getDecorView();
        decor.removeView(this);
        removeView(decorChild);
        decor.addView(decorChild);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean intercept = true;
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastDownX = (int) ev.getX();
                if (mLastDownX <= mScreenWidth / 5 && mSlideEnable)
                    processDownEvent();
                intercept = false;
                break;
            case MotionEvent.ACTION_MOVE:
                //down事件在距离屏幕左边1/6区域拦截事件，其他区域交给子View处理，最大程度下减轻子View有ViewPager等时造成的水平方向滑动冲突影响
                if (mLastDownX <= mScreenWidth / 6 && ev.getRawX() - mLastDownX > 5 && mSlideEnable) {
                    intercept = true;
                } else {
                    intercept = false;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                intercept = false;
                break;
        }
        return intercept;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mSlideEnable) {
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    processMoveEvent((int) ev.getX());
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    processUpEvent();
                    break;
            }
            return true;
        } else {
            return false;
        }
    }

    private void processDownEvent() {
        if (mPreviousSlideLayout != null) {
            mLastDownToX = mScreenWidth - mLastDownX;
            switch (slideEffect) {
                case 1:
                    mPreWidth = mScreenWidth / 4;
                    break;
                case 2:
                    mPreviousSlideLayout.setBackgroundColor(Color.BLACK);
                    break;
                case 3:
                    mContentView.setPivotX(mScreenWidth * 0.6f);
                    mContentView.setPivotY(2 * mScreenHeight);
                    break;
            }
        }
    }

    private void processMoveEvent(final int x) {
        if (mPreviousSlideLayout != null) {
            switch (slideEffect) {
                case 0:
                    move(x);
                    break;
                case 1:
                    move(x);
                    mPreviousSlideLayout.scrollTo((int) ((1 + (float) getScrollX() / mLastDownToX) * mPreWidth), 0);
                    mAlpha = Math.max((float) (x - mLastDownX) / mLastDownToX * FINALALPHA, 0);
                    invalidate();
                    break;
                case 2:
                    move(x);
                    mScale = (float) (x - mLastDownX) / mLastDownToX * (1 - FINALSCALE) + FINALSCALE;
                    if (mPreviousChild != null) {
                        mPreviousChild.setScaleX(mScale);
                        mPreviousChild.setScaleY(mScale);
                    }
                    mAlpha = Math.max((float) (x - mLastDownX) / mLastDownToX * FINALALPHA, 0);
                    invalidate();
                    break;
                case 3:
                    if (mLastDownToX != 0) {
                        mRotation = Math.max((float) (x - mLastDownX) / mLastDownToX * 30, 0);
                        mContentView.setRotation(mRotation);
                    }
                    break;
            }
        }
    }

    private void processUpEvent() {
        if (slideEffect == 3) {
            if (mRotation < 12) {
                animation(2);
            } else {
                animation(3);
            }
        } else {
            mUpScrollX = getScrollX();
            mUpAlpha = mAlpha;
            //屏幕宽度一半处为分界线
            if (-getScrollX() < mScreenWidth / 2) {
                moveBack();
            } else {
                moveClose();
            }
        }
    }

    private void move(int x) {
        if (getScrollX() + mLastDownX - x >= 0) {
            scrollTo(0, 0);
        } else {
            scrollTo(mLastDownX - x, 0);
        }
    }

    private void moveBack() {
        scrollBack();
        if (mPreviousSlideLayout != null) {
            switch (slideEffect) {
                case 1:
                    mPreviousSlideLayout.preScrollBack();
                    break;
                case 2:
                    animation(0);
                    break;
            }
        }
    }

    private void moveClose() {
        scrollClose();
        if (mPreviousSlideLayout != null) {
            switch (slideEffect) {
                case 1:
                    mPreviousSlideLayout.preScrollClose();
                    break;
                case 2:
                    animation(1);
                    break;
            }
        }
    }


    /**
     * 画背景
     *
     * @param canvas
     */
    private void drawBackground(Canvas canvas) {
        if (slideEffect == 3) {
            mShadowPaint.setColor(Color.WHITE);
            return;
        }
        //在作用的activity上还是finish后的activity上画背景(mAlpha:0~0.4)
        //阴影效果
        switch (shadowOrientation) {
            case 0:
                mShadowPaint.setColor(Color.WHITE);
                break;
            case 1:
                mShadowPaint.setAlpha((int) ((FINALALPHA - mAlpha) * 255));
                canvas.drawRect(-mScreenWidth, 0, 0, mScreenHeight, mShadowPaint);
                break;
            case 2:
                mShadowPaint.setAlpha((int) (mAlpha * 255));
                canvas.drawRect(mScreenWidth, 0, 0, mScreenHeight, mShadowPaint);
                break;
        }
        //是否画边缘阴影
        if (edgeShadow) {
            mEdgeShadow.setBounds(0, 0, edgeShadowSize, mScreenHeight);
            canvas.save();
            canvas.translate(-edgeShadowSize, 0);
            mEdgeShadow.draw(canvas);
            canvas.restore();
        }
    }

    /**
     * 滑动返回
     */
    private void scrollBack() {
        int startX = getScrollX();
        int dx = -getScrollX();
        mScroller.startScroll(startX, 0, dx, 0, 500);
        invalidate();
    }

    /**
     * 滑动关闭
     */
    private void scrollClose() {
        int startX = getScrollX();
        int dx = -getScrollX() - mScreenWidth;
        mScroller.startScroll(startX, 0, dx, 0, 500);
        invalidate();
    }

    /**
     * 前一个activity的滑动关闭
     */
    private void preScrollClose() {
        mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0, 500);
        invalidate();
    }

    /**
     * 前一个activity的滑动返回
     */
    private void preScrollBack() {
        mScroller.startScroll(getScrollX(), 0, (int) (getScrollX() + mPreWidth), 0, 500);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            int currX = mScroller.getCurrX();
            switch (shadowOrientation) {
                case 2:
                    if (currX < 0) {
                        if (currX >= -mScreenWidth / 2) {
                            mAlpha = (float) currX / mUpScrollX * mUpAlpha;
                        }
                    }
                    break;
                case 1:
                    if (currX < 0) {
                        if (currX >= -mScreenWidth / 2) {
                            //back
                            mAlpha = (float) currX / mUpScrollX * mUpAlpha;
                        } else {
                            //close
                            mAlpha = (float) (mUpScrollX - currX) * (FINALALPHA - mUpAlpha) / (mScreenWidth + mUpScrollX) + mUpAlpha;
                        }
                    }
                    break;
            }
            postInvalidate();
        } else if (-getScrollX() >= mScreenWidth) {
            mPreviousSlideLayout.setBackground(null);
            mActivity.finish();
        }
    }

    /**
     * 动画
     *
     * @param type
     */
    private void animation(int type) {
        switch (type) {
            case 0:
                ObjectAnimator backAnimatorX = ObjectAnimator.ofFloat(mPreviousChild, "scaleX", mScale, FINALSCALE);
                ObjectAnimator backAnimatorY = ObjectAnimator.ofFloat(mPreviousChild, "scaleY", mScale, FINALSCALE);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(backAnimatorX, backAnimatorY);
                animatorSet.setDuration(500).start();
                break;
            case 1:
                ObjectAnimator closeAnimatorX = ObjectAnimator.ofFloat(mPreviousChild, "scaleX", mScale, 1f);
                ObjectAnimator closeAnimatorY = ObjectAnimator.ofFloat(mPreviousChild, "scaleY", mScale, 1f);
                AnimatorSet animatorSet1 = new AnimatorSet();
                animatorSet1.playTogether(closeAnimatorX, closeAnimatorY);
                animatorSet1.setDuration(500).start();
                break;
            case 2:
                ObjectAnimator back2 = ObjectAnimator.ofFloat(mContentView, "rotation", mRotation, 0);
                back2.setDuration(250).start();
                break;
            case 3:
                ObjectAnimator close3 = ObjectAnimator.ofFloat(mContentView, "rotation", mRotation, 30);
                close3.setDuration(250).start();
                close3.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mActivity.finish();
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                break;
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        drawBackground(canvas);
    }
}
