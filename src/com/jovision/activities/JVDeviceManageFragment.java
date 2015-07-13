
package com.jovision.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.IHandlerLikeNotify;
import com.jovision.activities.JVTabActivity.OnMainListener;
import com.jovision.adapters.ManageListAdapter;
import com.jovision.adapters.TabPagerAdapter;
import com.jovision.bean.Device;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.DeviceUtil;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 设备管理
 */
public class JVDeviceManageFragment extends BaseFragment implements
        OnMainListener {

    private String TAG = "JVDeviceManageFragment";

    /** scroll layout */
    private HorizontalScrollView mHorizontalScrollView;
    private LinearLayout mLinearLayout;
    private ViewPager managePager;
    private ImageView mImageView;
    private int mScreenWidth;
    private int item_width;

    private int endPosition;
    private int beginPosition;
    private int currentFragmentIndex;
    private boolean isEnd;
    private RelativeLayout devmorere;
    private RelativeLayout devmore_hie;
    private TextView device_num;
    private ListView devicemanage_listView;
    private ManageListAdapter adapter;
    /** 两个列表界面 和加载失败界面 */
    private LinearLayout dataLayout;// 数据界面
    private RelativeLayout refreshlayout;// 加载失败页面
    private RelativeLayout relalist;
    private RelativeLayout relative;
    /** 帮助引导 */
    private LinearLayout quickSetSV; // 快速配置界面
    // private ImageView quickinstall_img_bg;
    // private ImageView unwire_device_img_bg;
    // private Button quickSet;
    // private Button addDevice;

    /** 没有设备时的提示语 */
    private TextView no_device_lead;
    private TextView no_device_lead_text2;
    private ArrayList<Fragment> fragments;

    /** intent传递过来的设备和通道下标 */
    public static int deviceIndex;

    public static int clickIndex = -1;// 点击的功能 2015.5.6

    private ArrayList<Device> manageDeviceList = new ArrayList<Device>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        deviceIndex = 0;
        currentFragmentIndex = 0;
        View view = inflater.inflate(R.layout.fragment_devicemanage, container,
                false);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {
            mParent = getView();
            mActivity = (BaseActivity) getActivity();

            currentMenu.setText(R.string.str_help1_2);
            rightBtn.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.mydevice_cancale_icon));
            rightBtn.setVisibility(View.VISIBLE);
            rightBtn.setOnClickListener(mOnClickListener);
            leftBtn.setVisibility(View.GONE);
            manageDeviceList = CacheUtil.getDevList();

            mScreenWidth = mActivity.disMetrics.widthPixels;
            mHorizontalScrollView = (HorizontalScrollView) mActivity
                    .findViewById(R.id.hsv_view);
            mLinearLayout = (LinearLayout) mActivity
                    .findViewById(R.id.hsv_content);
            mImageView = (ImageView) mActivity.findViewById(R.id.img);
            item_width = (int) ((mScreenWidth / 3.0 + 0.5f));
            mImageView.getLayoutParams().width = item_width;

            managePager = (ViewPager) mParent.findViewById(R.id.manage_pagerer);

            /** 设备加载失败 */
            refreshlayout = (RelativeLayout) mParent
                    .findViewById(R.id.refreshlayout);
            device_num = (TextView) mParent.findViewById(R.id.device_num);
            dataLayout = (LinearLayout) mParent.findViewById(R.id.datalayout);
            relalist = (RelativeLayout) mParent.findViewById(R.id.relalist);
            relative = (RelativeLayout) mParent.findViewById(R.id.relative);

            /** 引导加设备 */
            quickSetSV = (LinearLayout) mParent
                    .findViewById(R.id.quickinstalllayout);
            // quickinstall_img_bg = (ImageView) mParent
            // .findViewById(R.id.quickinstall_img_bg);
            // unwire_device_img_bg = (ImageView) mParent
            // .findViewById(R.id.unwire_device_img_bg);
            // quickSet = (Button) mParent.findViewById(R.id.quickinstall);
            // addDevice = (Button) mParent.findViewById(R.id.adddevice);
            //
            // quickinstall_img_bg.setOnClickListener(mOnClickListener);
            // unwire_device_img_bg.setOnClickListener(mOnClickListener);
            // quickSet.setOnClickListener(mOnClickListener);
            // addDevice.setOnClickListener(mOnClickListener);

            /** 没有设备时的提示语 */
            no_device_lead = (TextView) mParent
                    .findViewById(R.id.no_device_lead);
            no_device_lead_text2 = (TextView) mParent
                    .findViewById(R.id.no_device_lead_text2);
            no_device_lead.setText(myGettext());
            rightBtn.setOnClickListener(mOnClickListener);
            if (Consts.LANGUAGE_ZH == ConfigUtil.getServerLanguage()
                    && Consts.LANGUAGE_ZH == ConfigUtil.getLanguage2(mActivity)) {// 中文中国
                if (null != mActivity.statusHashMap
                        && null != mActivity.statusHashMap
                                .get(Consts.MORE_SHOP_SWITCH)) {
                    if (1 == Integer.parseInt(mActivity.statusHashMap
                            .get(Consts.MORE_SHOP_SWITCH))) {
                        no_device_lead_text2.setVisibility(View.VISIBLE);
                    } else {
                        no_device_lead_text2.setVisibility(View.GONE);
                    }
                } else {
                    no_device_lead_text2.setVisibility(View.GONE);
                }
            } else {
                no_device_lead_text2.setVisibility(View.GONE);
            }
            no_device_lead_text2.setOnClickListener(mOnClickListener);

            // if (mActivity.statusHashMap.get(Consts.NEUTRAL_VERSION).equals(
            // "false")) {// CloudSEE
            // quickinstall_img_bg.setImageDrawable(mActivity.getResources()
            // .getDrawable(R.drawable.wire_device_img));
            // unwire_device_img_bg.setImageDrawable(mActivity.getResources()
            // .getDrawable(R.drawable.unwire_device_img));
            // } else {
            // quickinstall_img_bg.setImageDrawable(mActivity.getResources()
            // .getDrawable(R.drawable.wire_devicen_img));
            // unwire_device_img_bg.setImageDrawable(mActivity.getResources()
            // .getDrawable(R.drawable.unwire_devicen_img));
            // }

            devicemanage_listView = (ListView) mParent
                    .findViewById(R.id.device_listView);
            devmorere = (RelativeLayout) mParent.findViewById(R.id.devmorere);
            devmore_hie = (RelativeLayout) mParent
                    .findViewById(R.id.devmore_hie);

            devmorere.setOnClickListener(mOnClickListener);
            devmore_hie.setOnClickListener(mOnClickListener);
            ListViewClick();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // 设备列表的点击事件
    private void ListViewClick() {
        devicemanage_listView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                    int position, long id) {
                adapter.setSelectIndex(position);
                adapter.notifyDataSetChanged();
                deviceIndex = position;
                ((ManageFragment) fragments.get(position))
                        .setDevIndex(deviceIndex);
                managePager.setCurrentItem(position);
                relalist.setVisibility(View.GONE);
                devicemanage_listView.setVisibility(View.GONE);
                managePager.setVisibility(View.VISIBLE);
                relative.setVisibility(View.VISIBLE);
            }
        });
    }

    private void initNav() {
        adapter = new ManageListAdapter(
                (BaseFragment) JVDeviceManageFragment.this);
        adapter.setData(manageDeviceList);
        devicemanage_listView.setAdapter(adapter);
        int size = manageDeviceList.size();
        fragments = new ArrayList<Fragment>();
        mLinearLayout.removeAllViews();
        for (int i = 0; i < size; i++) {
            // [Neo] viewpager
            Bundle data = new Bundle();
            data.putInt("DeviceIndex", i);
            // 设备分组
            data.putString("DeviceGroup", manageDeviceList.get(i).getGid());
            ManageFragment fragment = new ManageFragment();
            fragment.setArguments(data);
            fragments.add(fragment);

            // [Neo] nav
            RelativeLayout layout = new RelativeLayout(mActivity);
            TextView view = new TextView(mActivity);
            view.setText(manageDeviceList.get(i).getNickName());
            view.setSingleLine(true);
            view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            view.setId(i);
            view.setTextColor(mActivity.getResources().getColor(
                    R.color.devicemanagename));
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.CENTER_IN_PARENT);
            layout.addView(view, params);
            mLinearLayout.addView(layout, (int) (mScreenWidth / 3 + 0.5f), 60);
            layout.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    int index = (Integer) view.getTag();
                    deviceIndex = index;
                    ((ManageFragment) fragments.get(index))
                            .setDevIndex(deviceIndex);
                    managePager.setCurrentItem(index);
                }
            });
            layout.setTag(i);
        }

    }

    private void initViewPager() {
        TabPagerAdapter fragmentPagerAdapter = new TabPagerAdapter(
                getChildFragmentManager(), fragments);
        managePager.setAdapter(fragmentPagerAdapter);
        managePager.setOnPageChangeListener(new ManagePageChangeListener());
        // [Neo] no need to reset this one
        // fragmentPagerAdapter.setFragments(fragments);
    }

    // 清空所有设备线程
    class DelAllDevTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {
            int delRes = -1;
            boolean localFlag = Boolean.valueOf(mActivity.statusHashMap
                    .get(Consts.LOCAL_LOGIN));
            try {
                if (localFlag) {// 本地删除
                    delRes = 0;
                    int size = manageDeviceList.size();
                    for (int i = 0; i < size; i++) {
                        ConfigUtil.deleteSceneFolder(manageDeviceList.get(i)
                                .getFullNo());
                        manageDeviceList.remove(i);
                        i--;
                        size = manageDeviceList.size();
                    }
                } else {
                    int size = manageDeviceList.size();
                    for (int i = 0; i < size; i++) {
                        delRes = DeviceUtil.unbindDevice(
                                (mActivity.statusHashMap.get("KEY_USERNAME")),
                                manageDeviceList.get(i).getFullNo());
                        if (0 == delRes) {
                            ConfigUtil.deleteSceneFolder(manageDeviceList
                                    .get(i).getFullNo());
                            manageDeviceList.remove(i);
                            i--;
                            size = manageDeviceList.size();
                        }

                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return delRes;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            // 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
            mActivity.dismissDialog();
            if (0 == result) {
                if (0 == manageDeviceList.size()) {
                    dataLayout.setVisibility(View.GONE);
                    refreshlayout.setVisibility(View.GONE);
                    quickSetSV.setVisibility(View.VISIBLE);
                } else {
                    adapter.notifyDataSetChanged();
                }
                mActivity.showTextToast(R.string.del_device_succ);
                CacheUtil.saveDevList(manageDeviceList);
            } else {
                mActivity.showTextToast(R.string.del_device_failed);
            }
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
            mActivity.createDialog("", false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新进度,此方法在主线程执行，用于显示任务执行的进度。
        }
    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_right:
                    if (null == manageDeviceList || 0 == manageDeviceList.size()) {
                        break;
                    }
                    // 提示对话框
                    AlertDialog.Builder builder = new Builder(mActivity);
                    builder.setTitle(R.string.tips)
                            .setMessage(
                                    getResources().getString(
                                            R.string.delete_all_device))
                            .setPositiveButton(R.string.sure,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                int which) {
                                            DelAllDevTask task = new DelAllDevTask();
                                            String[] strParams = new String[3];
                                            task.execute(strParams);
                                        }
                                    })
                            .setNegativeButton(R.string.cancel,
                                    new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog,
                                                int which) {
                                            dialog.dismiss();
                                        }
                                    }).create().show();

                    break;
                case R.id.devmorere:
                    if (null != adapter) {
                        adapter.setSelectIndex(deviceIndex);
                    }
                    relalist.setVisibility(View.VISIBLE);
                    devicemanage_listView.setVisibility(View.VISIBLE);
                    managePager.setVisibility(View.GONE);
                    relative.setVisibility(View.GONE);
                    break;
                case R.id.devmore_hie:
                    adapter.notifyDataSetChanged();
                    relalist.setVisibility(View.GONE);
                    devicemanage_listView.setVisibility(View.GONE);
                    managePager.setVisibility(View.VISIBLE);
                    relative.setVisibility(View.VISIBLE);
                    break;
                // case R.id.quickinstall_img_bg:
                // case R.id.quickinstall:
                // ((ShakeActivity) mActivity).startSearch(false);
                // break;
                // case R.id.unwire_device_img_bg:
                // case R.id.adddevice:
                // Intent addIntent = new Intent();
                // addIntent.setClass(mActivity, JVAddDeviceActivity.class);
                // addIntent.putExtra("QR", false);
                // mActivity.startActivity(addIntent);
                // break;
                case R.id.no_device_lead_text2:
                    ((JVTabActivity) mActivity).jumpShop();
                    break;
                default:
                    break;
            }

        }
    };

    public class ManagePageChangeListener implements OnPageChangeListener {

        @SuppressLint("ResourceAsColor")
        @Override
        public void onPageSelected(final int position) {
            // MyLog.v(TAG, "onPageSelected---position="+position);
            Animation animation = new TranslateAnimation(endPosition, position
                    * item_width, 0, 0);

            beginPosition = position * item_width;
            currentFragmentIndex = position;
            if (animation != null) {
                animation.setFillAfter(true);
                animation.setDuration(0);
                mImageView.startAnimation(animation);
                mHorizontalScrollView.smoothScrollTo((currentFragmentIndex)
                        * item_width, 0);
            }
            for (int i = 0; i < manageDeviceList.size(); i++) {
                TextView view = (TextView) mLinearLayout.getChildAt(i)
                        .findViewById(i);
                if (position == i) {
                    // adapter.setSelectIndex(position);
                    // manageDeviceList.get(i).setIsselect(true);
                    view.setTextColor(mActivity.getResources().getColor(
                            R.color.welcome_blue));
                } else {
                    // manageDeviceList.get(i).setIsselect(false);
                    view.setTextColor(mActivity.getResources().getColor(
                            R.color.devicemanagename));
                }
            }
            deviceIndex = position;
            ((ManageFragment) fragments.get(position)).setDevIndex(deviceIndex);
            managePager.setCurrentItem(position);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset,
                int positionOffsetPixels) {
            // MyLog.v(TAG, "onPageScrolled---position="+position);
            if (!isEnd) {
                if (currentFragmentIndex == position) {
                    endPosition = item_width * currentFragmentIndex
                            + (int) (item_width * positionOffset);
                }
                if (currentFragmentIndex == position + 1) {
                    endPosition = item_width * currentFragmentIndex
                            - (int) (item_width * (1 - positionOffset));
                }

                Animation mAnimation = new TranslateAnimation(beginPosition,
                        endPosition, 0, 0);
                mAnimation.setFillAfter(true);
                mAnimation.setDuration(0);
                mImageView.startAnimation(mAnimation);
                mHorizontalScrollView.invalidate();
                beginPosition = endPosition;
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_DRAGGING) {
                isEnd = false;
            } else if (state == ViewPager.SCROLL_STATE_SETTLING) {
                isEnd = true;
                beginPosition = currentFragmentIndex * item_width;
                if (managePager.getCurrentItem() == currentFragmentIndex) {
                    // 未跳入下一个页面
                    mImageView.clearAnimation();
                    Animation animation = null;
                    // 恢复位置
                    animation = new TranslateAnimation(endPosition,
                            currentFragmentIndex * item_width, 0, 0);
                    animation.setFillAfter(true);
                    animation.setDuration(1);
                    mImageView.startAnimation(animation);
                    mHorizontalScrollView.invalidate();
                    endPosition = currentFragmentIndex * item_width;
                }
                // MyLog.v(TAG,
                // "onPageScrollStateChanged---currentFragmentIndex="+currentFragmentIndex+"---deviceIndex="+deviceIndex);
            }
        }

    }

    @Override
    public void onResume() {
        manageDeviceList = CacheUtil.getDevList();

        if (Consts.LANGUAGE_EN == ConfigUtil.getLanguage2(mActivity)) {
            device_num.setText(mActivity.getResources().getString(
                    R.string.str_fre)
                    + "   "
                    + manageDeviceList.size()
                    + "   "
                    + mActivity.getResources().getString(R.string.str_aft));
        } else {
            device_num.setText(mActivity.getResources().getString(
                    R.string.str_fre)
                    + manageDeviceList.size()
                    + mActivity.getResources().getString(R.string.str_aft));
        }
        String stateStr = ((BaseActivity) mActivity).statusHashMap
                .get(Consts.DATA_LOADED_STATE);
        if (null != stateStr) {
            dataLayout.setVisibility(View.GONE);
            quickSetSV.setVisibility(View.GONE);
            refreshlayout.setVisibility(View.VISIBLE);
        } else {
            if (0 == manageDeviceList.size()) {
                dataLayout.setVisibility(View.GONE);
                refreshlayout.setVisibility(View.GONE);
                quickSetSV.setVisibility(View.VISIBLE);
            } else if (manageDeviceList.size() > 0) {
                dataLayout.setVisibility(View.VISIBLE);
                refreshlayout.setVisibility(View.GONE);
                quickSetSV.setVisibility(View.GONE);
                try {
                    // 初始化导航
                    initNav();
                    // 初始化viewPager
                    initViewPager();

                    ManageFragment fragement = ((ManageFragment) fragments
                            .get(deviceIndex));
                    if (null != fragement) {
                        fragement.setDevIndex(deviceIndex);
                    }
                    managePager.setCurrentItem(deviceIndex);
                    mHorizontalScrollView.smoothScrollTo((deviceIndex - 1)
                            * item_width, 0);
                    mHorizontalScrollView.invalidate();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        super.onResume();
    }

    @Override
    public void onPause() {
        // CacheUtil.saveDevList(manageDeviceList);
        super.onPause();
    }

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        // TODO 以后增加过滤
        try {
            Fragment currentFrag = fragments.get(deviceIndex);
            if (null != currentFrag) {
                ((IHandlerLikeNotify) currentFrag).onNotify(what, arg1, arg2,
                        obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public SpannableStringBuilder myGettext() {
        CharSequence text = getResources().getString(
                R.string.str_no_device_lead);
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        String rexgString = "@";
        Pattern pattern = Pattern.compile(rexgString);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            Drawable drawable = this.getResources().getDrawable(R.drawable.bjd);
            drawable.setBounds(0, 0, 60, 60);// 这里设置图片的大小
            ImageSpan imageSpan = new ImageSpan(drawable,
                    ImageSpan.ALIGN_BOTTOM);
            builder.setSpan(imageSpan, matcher.start(), matcher.end(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        return builder;
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        fragHandler.sendMessage(fragHandler
                .obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    public void onMainAction(int packet_type) {
        // TODO Auto-generated method stub

    }

}
