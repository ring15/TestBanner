package com.founq.sdk.testbanner;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ring on 2019/7/22.
 */
public class Banner extends RelativeLayout implements ViewPager.OnPageChangeListener {

    private static final int VEL_THRESHOLD = 400;

    private Context mContext;
    //存放轮循视图集合
    private List<View> mViews;
    //存放数据
    private List<? extends Object> mModels;
    //存放tips（暂时先不做）
    private List<String> mTips;

    //设置圆点之间的左右间距
    private int mPointLeftRightMargin;

    private int mPointTopBottomMargin;
    private int mPointContainerLeftRightPadding;

    private int mContentBottomMargin = 0;

    //自定义viewpager
    private BannerViewPager mViewPager;

    //放置指示圆点
    private LinearLayout mPointRealContainerLl;

    //监听事件
    private Delegate mDelegate;

    //填充数据回调
    private Adapter mAdapter;

    //viewpager滑动位置
    private int mPageScrollPosition;
    //viewpager滑动偏移量
    private float mPageScrollPositionOffset;

    //duration
    private int mPageChangeDuration = 800;

    //背景
    private Drawable mPointContainerBackgroundDrawable;

    //异步开启轮循
    private AutoPlayTask mAutoPlayTask;
    private int mAutoPlayInterval = 3000;

    public Banner(Context context) {
        this(context, null);
    }

    public Banner(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Banner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setSize(context, attrs, defStyleAttr);
        init(context);
    }

    private void setSize(Context context, AttributeSet attrs, int defStyleAttr) {

        mPointLeftRightMargin = dp2px(context, 3);
        mPointTopBottomMargin = dp2px(context, 6);
        mPointContainerLeftRightPadding = dp2px(context, 10);
        mPointContainerBackgroundDrawable = new ColorDrawable(Color.parseColor("#44aaaaaa"));
    }

    private void init(Context context) {
        mContext = context;

        mAutoPlayTask = new AutoPlayTask(this);

        //这边应该有一层外边的RelativeLayout的，但是我没有写tips，就没有必要了，直接将小圆点的加到整个banner这个RelativeLayout的底部就OK了
        //先不设置字符显示的，所以只有小圆点
        mPointRealContainerLl = new LinearLayout(context);
        //设置id是为了后边设置对齐方式，使字符和圆点公用一个id
        mPointRealContainerLl.setId(R.id.banner_indicatorId);
        mPointRealContainerLl.setOrientation(LinearLayout.HORIZONTAL);
        //设置竖直方向上居中
        mPointRealContainerLl.setGravity(Gravity.CENTER);
        mPointRealContainerLl.setBackground(mPointContainerBackgroundDrawable);
        mPointRealContainerLl.setPadding(0, mPointTopBottomMargin, 0, mPointTopBottomMargin);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(mPointRealContainerLl, layoutParams);

    }

    public void setData(@LayoutRes int layoutResId, List<? extends Object> models, List<String> tips) {
        mViews = new ArrayList<>();
        if (models == null) {
            models = new ArrayList<>();
            tips = new ArrayList<>();
        }
        for (int i = 0; i < models.size(); i++) {
            mViews.add(View.inflate(mContext, layoutResId, null));
        }
        setData(mViews, models, tips);
    }

    public void setData(List<View> views, List<? extends Object> models, List<String> tips) {
        if (views == null || views.size() <= 0) {
            views = new ArrayList<>();
            models = new ArrayList<>();
            tips = new ArrayList<>();
        }

        mViews = views;
        mModels = models;
        mTips = tips;

        //初始化指示标志
        initIndicator();
        //初始化viewpager，轮循
        initViewPager();
    }

    private void initViewPager() {
        if (mViewPager != null && this.equals(mViewPager.getParent())) {
            this.removeView(mViewPager);
            mViewPager = null;
        }
        mViewPager = new BannerViewPager(mContext);
        //缓存数量来着吧,这里设置1我觉得和下边adapter中的预加载有关系，如果不设置1，一次性预加载多了，就不执行其中的语句，或者错乱了吧，可以试一下
        mViewPager.setOffscreenPageLimit(1);
        mViewPager.setAdapter(new MyPagerAdapter());
        mViewPager.addOnPageChangeListener(this);
        setPageChangeDuration(mPageChangeDuration);

        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT);
        layoutParams.setMargins(0, 0, 0, mContentBottomMargin);
        addView(mViewPager, 0, layoutParams);
        for (int i = 0; i < mViews.size(); i++) {
            mViewPager.addView(mViews.get(i));
        }

//        int zeroItem = Integer.MAX_VALUE / 2 - (Integer.MAX_VALUE / 2) % mViews.size();
//        mViewPager.setCurrentItem(zeroItem);
        switchToPoint(0);
        startAutoPlay();
    }

    /**
     * 设置页码切换过程的时间长度
     *
     * @param duration 页码切换过程的时间长度
     */
    public void setPageChangeDuration(int duration) {
        if (duration >= 0 && duration <= 2000) {
            mPageChangeDuration = duration;
            if (mViewPager != null) {
                mViewPager.setPageChangeDuration(duration);
            }
        }
    }

    private void initIndicator() {
        if (mPointRealContainerLl != null) {
            mPointRealContainerLl.removeAllViews();
            if (mViews.size() > 0) {
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT);
                layoutParams.setMargins(mPointLeftRightMargin, 0, mPointLeftRightMargin, 0);
                ImageView imageView;
                for (int i = 0; i < mViews.size(); i++) {
                    imageView = new ImageView(mContext);
                    imageView.setLayoutParams(layoutParams);
                    imageView.setImageResource(R.drawable.bga_banner_selector_point_solid);
                    mPointRealContainerLl.addView(imageView);
                }
            }
        }
    }

    @Override
    public void onPageScrolled(int i, float v, int i1) {
        mPageScrollPosition = i;
        mPageScrollPositionOffset = v;
    }

    @Override
    public void onPageSelected(int i) {
        i = i % mViews.size();
        //跳转，指示标志更新
        switchToPoint(i);
    }

    private void switchToPoint(int i) {
        if (mPointRealContainerLl != null) {
            if (mViews != null && mViews.size() > 0 && i < mViews.size()) {
                mPointRealContainerLl.setVisibility(VISIBLE);
                for (int j = 0; j < mPointRealContainerLl.getChildCount(); j++) {
                    mPointRealContainerLl.getChildAt(j).setSelected(j == i);
                    mPointRealContainerLl.getChildAt(j).requestLayout();
                }
            } else {
                mPointRealContainerLl.setVisibility(GONE);
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    private class MyPagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return mViews == null ? 0 : mViews.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            if (mViews == null || mViews.size() <= 0) {
                return super.instantiateItem(container, position);
            }

            int finalPosition = position % mViews.size();
            final View view = mViews.get(finalPosition);
            if (mDelegate != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int currentPosition = mViewPager.getCurrentItem() % mViews.size();
                        if (mModels != null && currentPosition < mModels.size()) {
                            mDelegate.onBannerItemClick(Banner.this, view, mModels.get(currentPosition), currentPosition);
                        } else {
                            mDelegate.onBannerItemClick(Banner.this, view, null, currentPosition);
                        }
                    }
                });
            }

            if (mAdapter != null) {
                if (mModels != null && finalPosition < mModels.size()) {
                    mAdapter.fillBannerItem(Banner.this, view, mModels.get(finalPosition), finalPosition);
                } else {
                    mAdapter.fillBannerItem(Banner.this, view, null, finalPosition);
                }
            }

            ViewParent viewParent = view.getParent();
            if (viewParent != null) {
                ((ViewGroup) viewParent).removeView(view);
            }

            container.addView(view);
            return view;
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            return POSITION_NONE;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        }
    }

    public void setDelegate(Delegate delegate) {
        mDelegate = delegate;
    }

    public void setAdapter(Adapter adapter) {
        mAdapter = adapter;
    }

    public static int dp2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context, float spValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, spValue, context.getResources().getDisplayMetrics());
    }

    /**
     * 点击事件监听器，没有处理重复点击事件
     *
     * @param <V>
     * @param <M>
     */
    public interface Delegate<V extends View, M> {
        void onBannerItemClick(Banner banner, V itemView, M model, int position);
    }

    /**
     * 在fillBannerItem方法中填充数据，加载网络图片等
     *
     * @param <V>
     * @param <M>
     */
    public interface Adapter<V extends View, M> {
        void fillBannerItem(Banner banner, V itemView, M model, int position);
    }

//    @Override
//    public void handleAutoPlayActionUpOrCancel(float xVelocity) {
//        if (mViewPager != null) {
//            if (mPageScrollPosition < mViewPager.getCurrentItem()) { // 往右滑
//                if (xVelocity > VEL_THRESHOLD || (mPageScrollPositionOffset < 0.7f && xVelocity > -VEL_THRESHOLD)) {
//                    // 已达到右滑到接下来展示左边一页的条件，展示左边一页
//                    mViewPager.setBannerCurrentItemInternal(mPageScrollPosition, true);
//                } else {
//                    // 未达到右滑到接下来展示左边一页的条件，展示当前页
//                    mViewPager.setBannerCurrentItemInternal(mPageScrollPosition + 1, true);
//                }
//            } else if (mPageScrollPosition == mViewPager.getCurrentItem()) { // 往左滑
//                if (xVelocity < -VEL_THRESHOLD || (mPageScrollPositionOffset > 0.3f && xVelocity < VEL_THRESHOLD)) {
//                    // 已达到左滑到接下来展示右边一页的条件，展示右边一页
//                    mViewPager.setBannerCurrentItemInternal(mPageScrollPosition + 1, true);
//                } else {
//                    // 未达到左滑到接下来展示右边一页的条件，展示当前页
//                    mViewPager.setBannerCurrentItemInternal(mPageScrollPosition, true);
//                }
//            } else {
//                // 快速左滑优化异常场景。感谢 https://blog.csdn.net/lylddingHFFW/article/details/89212664
//                mViewPager.setBannerCurrentItemInternal(mPageScrollPosition, true);
//            }
//        }
//    }

    /**
     * 切换到下一页
     */
    private void switchToNextPage() {
        if (mViewPager != null) {
            if (mViewPager.getCurrentItem() + 1 < mViews.size()) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
            } else {
                mViewPager.setCurrentItem(0);
            }
        }
    }


    public void startAutoPlay() {
        stopAutoPlay();
        postDelayed(mAutoPlayTask, mAutoPlayInterval);
    }

    public void stopAutoPlay() {
        if (mAutoPlayTask != null) {
            removeCallbacks(mAutoPlayTask);
        }
    }

    private static class AutoPlayTask implements Runnable {
        private final WeakReference<Banner> mBanner;

        private AutoPlayTask(Banner banner) {
            mBanner = new WeakReference<>(banner);
        }

        @Override
        public void run() {
            Banner banner = mBanner.get();
            if (banner != null) {
                banner.switchToNextPage();
                banner.startAutoPlay();
            }
        }
    }
}
