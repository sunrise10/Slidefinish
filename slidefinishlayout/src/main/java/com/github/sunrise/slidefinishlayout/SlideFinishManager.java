package com.github.sunrise.slidefinishlayout;

import android.app.Activity;

import java.util.Stack;

/**
 * Created by yf on 2017/12/29.
 * 描述：滑动finish管理
 */

public class SlideFinishManager {
    private SlideFinishLayout mSlideFinishLayout;
    private static SlideFinishManager mSlideFinishManager;
    public static Stack<SlideFinishLayout> mSlideFinishLayoutList = new Stack<>();

    //阴影方向
    public static final int NON = 0;
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    //滑动效果
    public static final int FOLLOW = 1;
    public static final int SCALE = 2;
    public static final int ROTATE = 3;

    private SlideFinishManager() {
    }

    public static SlideFinishManager getInstance() {
        if (mSlideFinishManager == null) {
            synchronized (SlideFinishManager.class) {
                if (mSlideFinishManager == null) {
                    mSlideFinishManager = new SlideFinishManager();
                }
            }
        }
        return mSlideFinishManager;
    }

    /**
     * 与activity绑定
     *
     * @param activity
     */
    public SlideFinishManager bind(Activity activity) {
        mSlideFinishLayout = new SlideFinishLayout(activity);
        mSlideFinishLayout.bind();
        mSlideFinishLayoutList.push(mSlideFinishLayout);
        return this;
    }

    /**
     * 与activity解绑定
     */
    public void unbind() {
        mSlideFinishLayout.unBind();
        mSlideFinishLayoutList.pop();
    }

    /**
     * 设置当前activity是否可以滑动退出
     * @param enable
     */
    public SlideFinishManager setSlideEnable(boolean enable) {
        mSlideFinishLayout.setSlideEnable(enable);
        return this;
    }

    /**
     * 设置滑动的效果
     *
     * @param slideEffectCode 0:无效果 1:微信效果 2:酷狗或今日头条效果 3:酷狗旋转效果
     */
    public SlideFinishManager setSlideEffect(int slideEffectCode) {
        mSlideFinishLayout.setSlideEffect(slideEffectCode);
        return this;
    }

    /**
     * 设置activity的左边缘是否有阴影，增加层次感
     *
     * @param edgeShadow
     */
    public SlideFinishManager setEdgeShadow(boolean edgeShadow) {
        mSlideFinishLayout.setEdgeShadow(edgeShadow);
        return this;
    }

    /**
     * 设置activity的左边缘阴影大小
     *
     * @param edgeShadowSize
     */
    public SlideFinishManager setEdgeShadowSize(int edgeShadowSize) {
        mSlideFinishLayout.setEdgeShadowSize(edgeShadowSize);
        return this;
    }

    /**
     * 设置阴影的方向
     *
     * @param shadowOrientation 0:无阴影 1:左边 2:右边
     */
    public SlideFinishManager setShadowOrientation(int shadowOrientation) {
        mSlideFinishLayout.setShadowOrientation(shadowOrientation);
        return this;
    }
}
