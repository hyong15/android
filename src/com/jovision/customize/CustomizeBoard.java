
package com.jovision.customize;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.activities.BaseActivity;
import com.jovision.activities.JVMoreFragment;
import com.jovision.activities.JVMyDeviceFragment;
import com.jovision.activities.JVTabActivity;
import com.jovision.customize.CustomizePageView.OnTabTouchedListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 中间加号定制面板
 */
public class CustomizeBoard extends PopupWindow implements OnClickListener,
        OnTabTouchedListener {

    protected static final String TAG = "CustomizeBoard";
    private JVTabActivity mJVTabActivity;
    private BaseActivity mActivity;
    private JVMoreFragment mLastFragment;
    private ImageView mClose;
    private LinearLayout mPanelHolder;
    private CustomizePageView mCustomizePageView;

    // 功能菜单项数组
    private String menuText[];
    // 功能菜单Tag数组
    private String menuTag[];
    // 功能菜单列表
    private List<Map<String, String>> menuList = new ArrayList<Map<String, String>>();
    // 开关-打开为1
    private int mOpenSwitch = 1;

    // 布局中子元素的动画延时时间
    private float mAnimationDelay = 0.1F;

    public CustomizeBoard(JVTabActivity activity) {
        super(activity);
        mJVTabActivity = activity;
        mActivity = (BaseActivity) activity;
        mLastFragment = (JVMoreFragment) activity.getLastFragment();

        initDatas();

        // 初始化自定义分享面板
        initViews(activity);

        initEvents();
    }

    private void initDatas() {
        // 初始化菜单数组
        menuText = mActivity.getResources().getStringArray(
                R.array.array_customize_board);
        menuTag = mActivity.getResources().getStringArray(
                R.array.array_customize_item_tag);
        for (int i = 0, length = menuText.length; i < length; i++) {
            Map<String, String> map = new HashMap<String, String>();

            // 判断功能是否显示
            if (!isDisplay(menuTag[i])) {
                continue;
            }
            map.put("item_tag", menuTag[i]);
            map.put("item_text", menuText[i]);
            // 获取图片资源
            int iconResId = getItemImageByTag(menuTag[i]);
            map.put("item_image", String.valueOf(iconResId));
            menuList.add(map);
        }
    }

    @SuppressWarnings("deprecation")
    private void initViews(Context context) {
        View rootView = LayoutInflater.from(context).inflate(
                R.layout.customize_board, null);
        setContentView(rootView);
        setWidth(LayoutParams.MATCH_PARENT);
        setHeight(LayoutParams.WRAP_CONTENT);
        setAnimationStyle(R.style.CustomizeBoardWindowAnim);
        setFocusable(true);
        setBackgroundDrawable(new BitmapDrawable());
        setTouchable(true);

        mClose = (ImageView) rootView.findViewById(R.id.btn_cancel);
        mPanelHolder = (LinearLayout) rootView.findViewById(R.id.panel_holder);
        mCustomizePageView = new CustomizePageView(mActivity);
        mCustomizePageView.setIndicator(menuList);
        mCustomizePageView.setVisibility(View.GONE);

        ViewGroup.LayoutParams localLayoutParams = new ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        mPanelHolder.addView(mCustomizePageView, localLayoutParams);

    }

    private void initEvents() {
        mClose.setOnClickListener(this);
        mCustomizePageView.setOnTabTouchedListener(this);

        mCustomizePageView.setVisibility(View.VISIBLE);// 显示面板上的元素
        mPanelHolder.setVisibility(View.VISIBLE);// 显示面板布局
        // mPanelHolder.startAnimation(CustomizeAnimation.enterAnimation(0L));
        // 开始动画
        // 得到一个LayoutAnimationController对象；
        LayoutAnimationController lac = new LayoutAnimationController(
                CustomizeAnimation.enterAnimation(0L));
        // 设置控件显示间隔时间；
        lac.setDelay(mAnimationDelay);
        // 为ListView设置LayoutAnimationController属性；
        mCustomizePageView.setLayoutAnimation(lac);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_cancel:
                // 关闭popupwindow
                CustomizeBoard.this.dismiss();
                break;
            default:
                break;
        }
    }

    @Override
    public void dismiss() {
        // // 加载退出动画
        // final Animation exit = CustomizeAnimation.exitAnimation(0L);
        // exit.setAnimationListener(new AnimationListener() {
        //
        // @Override
        // public void onAnimationStart(Animation animation) {
        // }
        //
        // @Override
        // public void onAnimationEnd(Animation animation) {
        // // 关闭popupwindow
        // mPanelHolder.post(new Runnable() {
        // @Override
        // public void run() {
        // CustomizeBoard.super.dismiss();
        // }
        // });
        // }
        //
        // @Override
        // public void onAnimationRepeat(Animation animation) {
        // }
        //
        // });
        // mPanelHolder.setVisibility(View.GONE);// 取出布局
        // mPanelHolder.startAnimation(exit);// 开始退出动画
        // 由于布局动画只在显示的时候加载,所以先隐藏再显示
        mCustomizePageView.setVisibility(View.GONE);
        mCustomizePageView.setVisibility(View.VISIBLE);
        // 开始动画
        // 得到一个LayoutAnimationController对象；
        LayoutAnimationController lac = new LayoutAnimationController(
                CustomizeAnimation.exitAnimation(0L));
        // 设置控件显示的顺序；
        lac.setOrder(LayoutAnimationController.ORDER_REVERSE);
        // 设置控件显示间隔时间；
        lac.setDelay(mAnimationDelay);
        // 为ListView设置LayoutAnimationController属性；
        mCustomizePageView.setLayoutAnimation(lac);
        mCustomizePageView.setLayoutAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // 关闭popupwindow
                mPanelHolder.post(new Runnable() {
                    @Override
                    public void run() {
                        CustomizeBoard.super.dismiss();
                    }
                });
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

        });
    }

    // 元素是否已经被放大
    private boolean mIsScaleOut;

    /**
     * 处理自定义面板上的元素的onTouch事件
     */
    @Override
    public boolean onItemTouch(int position, String tag, View v,
            MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                v.startAnimation(CustomizeAnimation.ScaleOutAnimation());
                mIsScaleOut = true;
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();
                if ((x > 0) && (x < v.getWidth()) && (y > 0) && (y < v.getHeight())) {
                    if (!mIsScaleOut) {
                        v.startAnimation(CustomizeAnimation.ScaleOutAnimation());
                        mIsScaleOut = true;
                    }
                } else {
                    if (mIsScaleOut) {
                        v.startAnimation(CustomizeAnimation.ScaleInAnimation());
                        mIsScaleOut = false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                int i = (int) event.getX();
                int j = (int) event.getY();
                if ((i <= 0) || (i >= v.getWidth()) || (j <= 0)
                        || (j >= v.getHeight())) {
                } else {
                    v.startAnimation(CustomizeAnimation.ScaleInAnimation());
                    // 点击事件
                    clickItemEvents(tag);
                }
                break;
        }

        return true;
    }

    /**
     * 判断功能是否显示
     * 
     * @return
     */
    private boolean isDisplay(String pTag) {
        char tag = pTag.charAt(0);
        int switchFlag = 1;// 默认为1显示
        switch (tag) {
            case 'c':// 小维商城
                try {
                    switchFlag = Integer.parseInt(mActivity.statusHashMap
                            .get(Consts.MORE_SHOP_SWITCH));
                } catch (Exception e) {
                    return false;// 异常情况不显示
                }
                break;
            case 'd':// 小维工程
                try {
                    switchFlag = Integer.parseInt(mActivity.statusHashMap
                            .get(Consts.MORE_GCS_SWITCH));
                } catch (Exception e) {
                    return false;// 异常情况不显示
                }
                break;
            default:
                break;
        }

        // 没有启用,不显示
        if (mOpenSwitch != switchFlag) {
            return false;
        }

        return true;
    }

    /**
     * 通过元素的标记获取相应的图片
     * 
     * @param tag 标记
     * @return iconResId 图片资源
     */
    private int getItemImageByTag(String pTag) {
        char tag = pTag.charAt(0);
        int iconResId = R.drawable.customize_item_no_image;
        switch (tag) {
            case 'a':// 小维社区
                iconResId = R.drawable.tabbar_compose_community;
                break;
            case 'b':// 小维知道
                iconResId = R.drawable.tabbar_compose_knowledge;
                break;
            case 'c':// 小维商城
                iconResId = R.drawable.tabbar_compose_shop;
                break;
            case 'd':// 小维工程
                iconResId = R.drawable.tabbar_compose_engineering;
                break;
            case 'e':// 局域网搜索设备
            default:

        }

        return iconResId;
    }

    /**
     * 处理自定义面板上的元素的click事件
     */
    private void clickItemEvents(String pTag) {
        char tag = pTag.charAt(0);
        switch (tag) {
            case 'a':// 小维社区
                bbsurl();
                break;
            case 'b':// 小维知道
                knowledgeurl();
                break;
            case 'c':// 小维商城
                shopurl();
                break;
            case 'd':// 小维工程
                gcsurl();
                break;
            case 'e':// 局域网搜索设备
                searchLocalNetworkDevice();
                break;
            default:

        }
        // 关闭面板
        CustomizeBoard.super.dismiss();
    }

    // ------------------------------------------------------
    // ## 面板上元素的click事件对应的操作
    // ## 操作代码来自JVMoreFragment的listViewClick()
    // ------------------------------------------------------
    /**
     * 小维商城
     */
    private void shopurl() {
        mJVTabActivity.jumpShop();
    }

    /**
     * 小维知道
     */
    private void knowledgeurl() {
        Toast.makeText(mActivity, "unknown", Toast.LENGTH_SHORT).show();
    }

    /**
     * 工程商入驻
     */
    private void gcsurl() {
        mLastFragment.gcsurl(mActivity);
    }

    /**
     * 小维社区
     */
    private void bbsurl() {
        mLastFragment.bbsurl(mActivity);
    }

    /**
     * 局域网扫描设备
     */
    private void searchLocalNetworkDevice() {
        char charTag = (char) mJVTabActivity.getCurrentIndex();
        // 我的设备(局域网扫描设备需要先跳转到我的设备中)
        switch (charTag) {
            case 'a':// 当前Tab是我的设备时(a表示我的设备),直接执行扫描
                break;
            default:
                mJVTabActivity.jumpFragmentByTag('a');
        }

        JVMyDeviceFragment deviceFragment = (JVMyDeviceFragment) mJVTabActivity
                .getCurrentFragment();
        deviceFragment.searchLocalNetworkDevice();
    }

}
