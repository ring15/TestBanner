package com.founq.sdk.testbanner;

import android.content.Context;
import android.widget.Scroller;

/**
 * Created by ring on 2019/7/22.
 */
public class BannerScroller extends Scroller {

    private int mDuration = 1000;

    public BannerScroller(Context context, int duration) {
        super(context);
        mDuration = duration;
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}
