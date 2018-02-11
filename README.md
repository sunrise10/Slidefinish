# slidefinish
###### 手势滑动关闭activity(不适合fragment)

> 先上图

![](https://github.com/sunrise10/slidefinish/blob/a57d0ffda94844ad384f65a805fdd93b4a2f7949/app/src/main/screenshot/wechat.gif)
![](https://github.com/sunrise10/slidefinish/blob/a57d0ffda94844ad384f65a805fdd93b4a2f7949/app/src/main/screenshot/scale.gif)
![](https://github.com/sunrise10/slidefinish/blob/a57d0ffda94844ad384f65a805fdd93b4a2f7949/app/src/main/screenshot/ratate.gif)

#### 用法
1. 在你app的build.gradle中添加依赖

```
compile 'com.github.sunrise:slidefinish:1.0.0'
```
 
2. 在styles.xml在你的theme加上，也就是设置透明背景

```
<item name="android:windowIsTranslucent">true</item>
```

3. 在你的`BaseActivity`的`onCreate()`和`onDestroy()`中加上如下代码

```
@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
   	...
    SlideFinishManager.getInstance().bind(this);
}

@Override
protected void onDestroy() {
    super.onDestroy();
    ...
    SlideFinishManager.getInstance().unbind();
}
```
到此就就可看到上图效果,如果不想某个activity滑动，可在当前activity中的onCreate()中加上

```
SlideFinishManager.getInstance().setSlideEnable(false);
```
你还可以设置
你还可以设置滑动的效果(0:无效果 1:微信效果 2:酷狗或今日头条效果 3:酷狗旋转效果)，设置activity的左边缘是否有阴影，设置activity的左边缘阴影大小等，如：

```
SlideFinishManager.getInstance().setEdgeShadow(false).setSlideEffect(SlideFinishManager.ROTATE).setShadowOrientation(SlideFinishManager.LEFT);
```

详细请看`SlideFinishManager`

### 主要代码
主要处理了`onInterceptTouchEvent`和`onTouchEvent`方法

```
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
```
