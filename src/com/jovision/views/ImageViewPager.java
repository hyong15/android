
package com.jovision.views;

import android.app.Activity;
import android.content.Context;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 图片滚动类
 * 
 * @author Administrator
 */
public class ImageViewPager extends ViewPager {
    private Activity mActivity; // 上下文
    private List<View> mListViews; // 图片组
    private int mScrollTime = 0;
    private Timer timer;
    private int oldIndex = 0;
    private int curIndex = 0;

    /** 默认方向 */
    public static final int GESTURE_TO_NULL = 0x00;
    /** 手势向左 */
    public static final int GESTURE_TO_LEFT = 0x01;
    /** 手势向上 */
    public static final int GESTURE_TO_UP = 0x02;
    /** 手势向右 */
    public static final int GESTURE_TO_RIGHT = 0x03;
    /** 手势向下 */
    public static final int GESTURE_TO_DOWN = 0x04;

    private static final int DEFAULT_BLIND_AERA_R_SQUARE = 60;

    private float lastDownX;
    private float lastDownY;
    private int currentDirection;
    private boolean hasReportGesture;
    private int blindAreaRSquare;

    public ImageViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 开始广告滚动
     * 
     * @param mainActivity 显示广告的主界面
     * @param imgList 图片列表, 不能为null ,最少一张
     * @param scrollTime 滚动间隔 ,0为不滚动
     * @param ovalLayout 圆点容器,可为空,LinearLayout类型
     * @param ovalLayoutId ovalLayout为空时 写0, 圆点layout XMl
     * @param ovalLayoutItemId ovalLayout为空时 写0,圆点layout XMl 圆点XMl下View ID
     * @param focusedId ovalLayout为空时 写0, 圆点layout XMl 选中时的动画
     * @param normalId ovalLayout为空时 写0, 圆点layout XMl 正常时背景
     */
    public void start(Activity mainActivity, List<View> imgList,
            int scrollTime, LinearLayout ovalLayout, int ovalLayoutId,
            int ovalLayoutItemId, int focusedId, int normalId) {
        mActivity = mainActivity;
        mListViews = imgList;
        mScrollTime = scrollTime;
        // 设置圆点
        setOvalLayout(ovalLayout, ovalLayoutId, ovalLayoutItemId, focusedId,
                normalId);
        this.setAdapter(new MyPagerAdapter());// 设置适配器
        if (scrollTime != 0 && imgList.size() > 1) {
            // 设置滑动动画时间 ,如果用默认动画时间可不用 ,反射技术实现
            new ImageScroller(mActivity).setDuration(this, 700);

            startTimer();
            // 触摸时停止滚动
            this.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        startTimer();
                    } else {
                        stopTimer();
                    }
                    return false;

                    // switch (event.getAction()) {
                    // case MotionEvent.ACTION_UP:
                    // startTimer();
                    // break;
                    //
                    // case MotionEvent.ACTION_MOVE:
                    // if (hasReportGesture) {
                    // break;
                    // }
                    //
                    // float upOffset = lastDownY - event.getY();
                    // float rightOffset = event.getX() - lastDownX;
                    //
                    // int RR = (int) (upOffset * upOffset + rightOffset
                    // * rightOffset);
                    //
                    // if (RR < blindAreaRSquare) {
                    // break;
                    // }
                    //
                    // if (upOffset + rightOffset > 0
                    // && upOffset - rightOffset < 0) {
                    // ImageViewPager.this.setCurrentItem(ImageViewPager.this
                    // .getCurrentItem() - 1);
                    // } else if (upOffset + rightOffset < 0
                    // && upOffset - rightOffset > 0) {
                    // ImageViewPager.this.setCurrentItem(ImageViewPager.this
                    // .getCurrentItem() + 1);
                    // }
                    // hasReportGesture = true;
                    // break;
                    //
                    // default:
                    // break;
                    // }
                    //
                    // return true;
                }
            });
        }
        if (mListViews.size() > 1) {
            this.setCurrentItem((Integer.MAX_VALUE / 2)
                    - (Integer.MAX_VALUE / 2) % mListViews.size());// 设置选中为中间/图片为和第0张一样
        }
    }

    // 设置圆点
    private void setOvalLayout(final LinearLayout ovalLayout, int ovalLayoutId,
            final int ovalLayoutItemId, final int focusedId, final int normalId) {
        if (ovalLayout != null) {
            LayoutInflater inflater = LayoutInflater.from(mActivity);

            if (ovalLayout.getChildCount() == mListViews.size()-2) {
                return;
            }
            for (int i = 0; i < mListViews.size()-2; i++) {
                ovalLayout.addView(inflater.inflate(ovalLayoutId, null));

            }
            // 选中第一个
            ovalLayout.getChildAt(0).findViewById(ovalLayoutItemId)
                    .setBackgroundResource(focusedId);
            this.setOnPageChangeListener(new OnPageChangeListener() {
                public void onPageSelected(int i) {
                    curIndex = i % (mListViews.size()-2);
                    // 取消圆点选中
                    ovalLayout.getChildAt(oldIndex)
                            .findViewById(ovalLayoutItemId)
                            .setBackgroundResource(normalId);
                    // 圆点选中
                    ovalLayout.getChildAt(curIndex)
                            .findViewById(ovalLayoutItemId)
                            .setBackgroundResource(focusedId);
                    oldIndex = curIndex;
                }

                public void onPageScrolled(int arg0, float arg1, int arg2) {
                }

                public void onPageScrollStateChanged(int arg0) {
                }
            });
        }
    }

    /**
     * 取得当前选中下标
     * 
     * @return
     */
    public int getCurIndex() {
        return curIndex;
    }

    /**
     * 停止滚动
     */
    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * 开始滚动
     */
    public void startTimer() {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            public void run() {
                mActivity.runOnUiThread(new Runnable() {
                    public void run() {
                        ImageViewPager.this.setCurrentItem(ImageViewPager.this
                                .getCurrentItem() + 1);
                    }
                });
            }
        }, mScrollTime, mScrollTime);
    }

    // 适配器 //循环设置
    private class MyPagerAdapter extends PagerAdapter {
        public void finishUpdate(View arg0) {
        }

        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        public int getCount() {
            if (mListViews.size() == 1) {// 一张图片时不用流动
                return mListViews.size();
            }
            return Integer.MAX_VALUE;
        }

        public Object instantiateItem(View v, int i) {
            try {
                if (((ViewPager) v).getChildCount() == mListViews.size()) {
                    ((ViewPager) v).removeView(mListViews.get(i
                            % mListViews.size()));
                }
                ((ViewPager) v).addView(mListViews.get(i % mListViews.size()),
                        0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (0 == mListViews.size()) {
                return 0;
            }

            return mListViews.get(i % mListViews.size());
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // int pos = position % mListViews.size();
            // if (null != mListViews && mListViews.size() > pos) {
            // container.removeView(mListViews.get(pos));
            // }
        }

        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }

        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }

        public Parcelable saveState() {
            return null;
        }

        public void startUpdate(View arg0) {
        }

        // @Override
        // public void destroyItem(View view, int arg1, Object arg2) {
        // // ((ViewPager) view).removeView(mListViews.get(arg1 %
        // // mListViews.size()));
        // // ((ViewPager) view).removeAllViews();
        // }

    }
}
