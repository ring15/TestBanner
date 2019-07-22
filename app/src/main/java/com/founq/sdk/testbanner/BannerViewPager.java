package com.founq.sdk.testbanner;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by ring on 2019/7/22.
 */
public class BannerViewPager extends ViewPager {

//    private AutoPlayDelegate mAutoPlayDelegate;
//    private boolean mAllowUserScrollable = true;

    public BannerViewPager(@NonNull Context context) {
        super(context);
    }

    public BannerViewPager(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 设置viewpager切换时长，基本都是固定用法
     * @param duration
     */
    public void setPageChangeDuration(int duration){
        try {
            Field scrollField = ViewPager.class.getDeclaredField("mScroller");
            scrollField.setAccessible(true);
            scrollField.set(this, new BannerScroller(getContext(), duration));
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

//    public void setBannerCurrentItemInternal(int position, boolean smoothScroll){
//        Class viewPagerClass = ViewPager.class;
//        try {
//            Method setCurrentItemInternalMethod = viewPagerClass.getDeclaredMethod("setCurrentItemInternal",
//                    int.class, boolean.class, boolean.class);
//            setCurrentItemInternalMethod.setAccessible(true);
//            setCurrentItemInternalMethod.invoke(this, position, smoothScroll, true);
//            ViewCompat.postInvalidateOnAnimation(this);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }
//
//
//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//        if (mAllowUserScrollable) {
//            if (mAutoPlayDelegate != null && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP)) {
//                mAutoPlayDelegate.handleAutoPlayActionUpOrCancel(getXVelocity());
//                return false;
//            } else {
//                return super.onTouchEvent(ev);
//            }
//        } else {
//            return false;
//        }
//    }
//
//    private float getXVelocity() {
//        float xVelocity = 0;
//        Class viewpagerClass = ViewPager.class;
//        try {
//            Field velocityTrackerField = viewpagerClass.getDeclaredField("mVelocityTracker");
//            velocityTrackerField.setAccessible(true);
//            VelocityTracker velocityTracker = (VelocityTracker) velocityTrackerField.get(this);
//
//            Field activePointerIdField = viewpagerClass.getDeclaredField("mActivePointerId");
//            activePointerIdField.setAccessible(true);
//
//            Field maximumVelocityField = viewpagerClass.getDeclaredField("mMaximumVelocity");
//            maximumVelocityField.setAccessible(true);
//            int maximumVelocity = maximumVelocityField.getInt(this);
//
//            velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
//            xVelocity = VelocityTrackerCompat.getXVelocity(velocityTracker, activePointerIdField.getInt(this));
//        } catch (Exception e) {
//        }
//        return xVelocity;
//    }
//
//    public void setAutoPlayDelegate(AutoPlayDelegate autoPlayDelegate) {
//        mAutoPlayDelegate = autoPlayDelegate;
//    }
//
//    public interface AutoPlayDelegate {
//        void handleAutoPlayActionUpOrCancel(float xVelocity);
//    }
}
