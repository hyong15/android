
package com.jovision.activities;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.MainApplication;
import com.jovision.adapters.FragmentAdapter;
import com.jovision.bean.Device;
import com.jovision.bean.MoreFragmentBean;
import com.jovision.commons.CheckUpdateTask;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.JsonFileReader;
import com.jovision.utils.ListViewUtil;
import com.jovision.utils.PlayUtil;
import com.jovision.views.AlarmDialog;
import com.tencent.stat.StatService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 个人中心(原来的更多)中所有的功能对应的二级页面类 例:新消息,图像查看,设置相关,我的服务,关于软件
 */
public class JVProfileFunctionActivity extends ShakeActivity {

    private final String TAG = "JVProfileFunctionActivity";
    // 当前对象
    private JVProfileFunctionActivity mInstance;
    // 本地登陆标志位
    private boolean mLocalFlag = false;
    // 模块listView
    private ListView mListView;

    // debug按钮
    private Button debugMode;

    // Adapter 存储模块文字和图标
    private List<MoreFragmentBean> mDataList;
    // listView 适配器
    private FragmentAdapter mAdapter;
    // assets/profile.json中个人中心部分对应的标志
    private String JSONTAG;
    private MainApplication mApp;
    private int littlenum = 0;
    // json文件
    private final String JSONFILE = "profile.json";
    private Resources mResources;// 资源
    // 弹出框
    private Dialog mtuDialog;
    private ImageView dialogCancelImg;
    private TextView dialogCancel;// 取消按钮
    private TextView dialogCompleted;// 确定按钮
    private final int RESULT_OK = 1;
    ArrayList<Device> deviceList;// 设备列表

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.WHAT_DISMISS_DIALOG: {
                dismissDialog();
                break;
            }
            case Consts.WHAT_PUSH_MESSAGE:// 报警信息通知
                // 通知显示报警信息条数
                int new_alarm_nums = mApp.getNewPushCnt();
                mAdapter.setNewNums(new_alarm_nums);
                mAdapter.notifyDataSetChanged();
                new AlarmDialog(mInstance).Show(obj);
                break;
            default:
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {
        mInstance = this;
        mApp = (MainApplication) getApplication();
        mResources = getResources();
        mLocalFlag = Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN));
        JSONTAG = getIntent().getStringExtra("jsontag");
    }

    @Override
    protected void initUi() {
        setContentView(R.layout.profile_functions_layout);
        initViews();
        initListeners();
        initDatas();
    }

    /**
     * 实例化布局
     */
    private void initViews() {
        /** top bar **/
        rightBtn = (Button) findViewById(R.id.btn_right);
        rightBtn.setVisibility(View.GONE);
        leftBtn = (Button) findViewById(R.id.btn_left);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        /*** other **/
        mListView = (ListView) findViewById(R.id.listView);
        debugMode = (Button) findViewById(R.id.debugswitch);
    }

    /**
     * 初始化监听事件
     */
    private void initListeners() {
        leftBtn.setOnClickListener(myOnClickListener);
        mListView.setOnItemClickListener(myOnItemClickListener);
        debugMode.setOnClickListener(myOnClickListener);
    }

    /**
     * 初始化数据及一些设置操作
     */
    private void initDatas() {
        // 标题
        String title = null;

        debugMode.setVisibility(View.GONE);
        if (JSONTAG.equals(Consts.PROFILE_ITEM_MSG)) {
            title = getResources().getString(R.string.person_item_msg);
        } else if (JSONTAG.equals(Consts.MORE_SHOWMEDIA)) {
            title = getResources().getString(R.string.person_item_imgview);
        } else if (JSONTAG.equals(Consts.PROFILE_ITEM_SETTING)) {
            title = getResources().getString(R.string.person_item_setting);
        } else if (JSONTAG.equals(Consts.PROFILE_ITEM_SERVICE)) {
            title = getResources().getString(R.string.person_item_service);
        } else if (JSONTAG.equals(Consts.PROFILE_ITEM_ABOUT)) {
            title = getResources().getString(R.string.person_item_about);
            debugMode.setVisibility(View.VISIBLE);
        }
        currentMenu.setText(title);
        // 加载json数据
        new DataThread().start();
    }

    @Override
    public void onBackPressed() {
        executeAnimLeftinRightout();
    }

    @Override
    protected void onResume() {
        // 刷新报警消息
        if (mAdapter != null && JSONTAG.equals(Consts.PROFILE_ITEM_MSG)) {
            // 通知显示报警信息条数
            int new_alarm_nums = mApp.getNewPushCnt();
            mAdapter.setNewNums(new_alarm_nums);
            mAdapter.notifyDataSetChanged();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        // 重置点击的次数
        if (littlenum < 20) {
            littlenum = 0;
        }
        super.onPause();
    }

    /**
     * 加载数据线程
     */
    class DataThread extends Thread {

        @Override
        public void run() {
            mDataList = new ArrayList<MoreFragmentBean>();
            String jsonStr = JsonFileReader.getJson(mInstance.getBaseContext(),
                    JSONFILE);
            mDataList = JsonFileReader.getDataListByJson(
                    mInstance.getBaseContext(), jsonStr, JSONTAG);
            // 数据检查,条件判断等操作
            checkDatas();
            mInstance.runOnUiThread(new UiThreadRunnable());
        }

    }

    /**
     * 处理最终的结果(主线程)
     */
    class UiThreadRunnable implements Runnable {

        @Override
        public void run() {
            mAdapter = new FragmentAdapter(mInstance, mDataList);

            // 关于
            if (JSONTAG.equals(Consts.PROFILE_ITEM_ABOUT)
                    && MySharedPreference.getBoolean(Consts.MORE_LITTLE)) {
                showLittleHelper();
                // 报警消息
            } else if (JSONTAG.equals(Consts.PROFILE_ITEM_MSG)) {
                int new_alarm_nums = mApp.getNewPushCnt();
                mAdapter.setNewNums(new_alarm_nums);
            }
            mListView.setAdapter(mAdapter);
        }
    }

    /**
     * 数据检查,条件判断重置等操作
     */
    private void checkDatas() {
        if (JSONTAG.equals(Consts.PROFILE_ITEM_SETTING)
                || JSONTAG.equals(Consts.PROFILE_ITEM_SERVICE)) {

            for (MoreFragmentBean bean : mDataList) {
                if (bean.getItemFlag().equals(Consts.MORE_PLAYMODE)) {
                    /** 观看模式的名称 **/
                    String playmode = mResources
                            .getString(R.string.person_setting_playmode);
                    if (MySharedPreference.getBoolean(Consts.MORE_PLAYMODE)) {
                        playmode = String
                                .format(playmode,
                                        mResources
                                                .getString(R.string.person_setting_playmode_multiple));
                    } else {
                        playmode = String
                                .format(playmode,
                                        mResources
                                                .getString(R.string.person_setting_playmode_single));
                    }
                    bean.setName(playmode);
                }
            }
        }
    }

    /**
     * 隐藏小助手
     */
    private void dismissLittleHelper() {
        for (MoreFragmentBean bean : mDataList) {
            if (bean.getItemFlag().equals(Consts.MORE_LITTLEHELP)
                    || bean.getItemFlag().equals(Consts.MORE_BROADCAST)
                    || bean.getItemFlag().equals(Consts.MORE_TESTSWITCH)
                    || bean.getItemFlag().equals(Consts.MORE_VERSION)
                    || bean.getItemFlag().equals(Consts.MORE_FOREIGNSWITCH)) {
                bean.setDismiss(true);
            }
        }
    }

    /**
     * 显示小助手
     */
    private void showLittleHelper() {
        for (MoreFragmentBean bean : mDataList) {
            if (bean.getItemFlag().equals(Consts.MORE_LITTLEHELP)
                    || bean.getItemFlag().equals(Consts.MORE_BROADCAST)
                    || bean.getItemFlag().equals(Consts.MORE_TESTSWITCH)
                    || bean.getItemFlag().equals(Consts.MORE_FOREIGNSWITCH)
                    || bean.getItemFlag().equals(Consts.MORE_VERSION)) {
                bean.setDismiss(false);
            }
        }
    }

    /**
     * click事件处理
     */
    OnClickListener myOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left: {
                    executeAnimLeftinRightout();
                    break;
                }
                case R.id.dialog_cancle_img: {// 功能设置-MTU设置-右上解关闭
                    mtuDialog.dismiss();
                    break;
                }
                case R.id.debugswitch: {// 调试开关
                    // 关于
                    if (!MySharedPreference.getBoolean(Consts.MORE_LITTLE)) {
                        littlenum++;
                        System.out
                                .println("=========================littlenum======"
                                        + littlenum);
                        if (littlenum < 20) {
                            if (littlenum >= 17) {
                                mInstance.showTextToast((20 - littlenum) + " ");
                            }
                        } else if (littlenum == 20) {
                            MySharedPreference.putBoolean(Consts.MORE_LITTLEHELP,
                                    true);
                            MySharedPreference.putBoolean(Consts.MORE_BROADCAST,
                                    true);
                            MySharedPreference.putBoolean(Consts.MORE_LITTLE, true);
                            showLittleHelper();
                            ListViewUtil
                                    .setListViewHeightBasedOnChildren(mListView);
                        }
                    } else {
                        littlenum = 0;
                        MySharedPreference
                                .putBoolean(Consts.MORE_LITTLEHELP, false);
                        MySharedPreference.putBoolean(Consts.MORE_BROADCAST, false);
                        MySharedPreference.putBoolean(Consts.MORE_LITTLE, false);
                        dismissLittleHelper();
                        ListViewUtil.setListViewHeightBasedOnChildren(mListView);
                    }
                    break;
                }
                default:
            }
        }
    };

    /**
     * ListView click事件处理
     */
    OnItemClickListener myOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            if (mDataList.get(position).getItemFlag().equals(Consts.MORE_HELP)) {
                // 帮助-废弃
                if (MySharedPreference.getBoolean(Consts.MORE_HELP)) {
                    MySharedPreference.putBoolean(Consts.MORE_HELP, false);
                    MySharedPreference.putBoolean(Consts.MORE_PAGEONE, true);
                    MySharedPreference.putBoolean(Consts.MORE_PAGETWO, true);
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_REMEMBER)) {
                // 功能设置-自动登陆
                if (MySharedPreference.getBoolean(Consts.MORE_REMEMBER)) {
                    MySharedPreference.putBoolean(Consts.MORE_REMEMBER, false);
                } else {
                    MySharedPreference.putBoolean(Consts.MORE_REMEMBER, true);
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_ALARMSWITCH)) {
                // 功能设置-报警设置
                // AlarmTask task = new AlarmTask();
                // Integer[] params = new Integer[3];
                // if (!MySharedPreference.getBoolean(
                // Consts.MORE_ALARMSWITCH, true)) {// 1是关//
                // // 0是开
                // params[0] = JVAlarmConst.ALARM_ON;// 关闭状态，去打开报警
                // } else {
                // params[0] = JVAlarmConst.ALARM_OFF;// 已经打开了，要去关闭
                // }
                // task.execute(params);
                if (mLocalFlag)// 本地登录
                {
                    mInstance.showTextToast(R.string.more_nologin);
                } else {
                    Intent intent = new Intent(mInstance,
                            AlarmSettingsActivity.class);
                    mInstance.startActivity(intent);
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_ALARMMSG)) {
                // 新消息-报警消息
                if (mLocalFlag) {
                    mInstance.showTextToast(R.string.more_nologin);
                } else {
                    if (!ConfigUtil.isConnected(mInstance)) {
                        mInstance.alertNetDialog();
                    } else {
                        // 重置消息数量
                        mApp.setNewPushCnt(0);
                        mAdapter.setNewNums(0);
                        Intent intent2 = new Intent(mInstance,
                                AlarmInfoActivity.class);
                        startActivity(intent2);
                    }
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_PLAYMODE)) {
                // 功能设置-观看模式(单设备,多设备)
                if (MySharedPreference.getBoolean(Consts.MORE_PLAYMODE)) {
                    MySharedPreference.putBoolean(Consts.MORE_PLAYMODE, false);
                    mDataList.get(position).setName(
                            mInstance.getResources().getString(
                                    R.string.str_video_modetwo));
                } else {
                    MySharedPreference.putBoolean(Consts.MORE_PLAYMODE, true);
                    mDataList.get(position).setName(
                            mInstance.getResources().getString(
                                    R.string.str_video_more_modetwo));
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_DEVICE_SCENESWITCH)) {
                // 功能设置-场景图开关
                if (MySharedPreference.getBoolean(
                        Consts.MORE_DEVICE_SCENESWITCH, true)) {
                    File sceneFile = new File(Consts.SCENE_PATH);
                    if (sceneFile.exists()) {// 如果场景图关闭清掉所有
                        ConfigUtil.deleteFile(sceneFile);
                    }
                    MySharedPreference.putBoolean(
                            Consts.MORE_DEVICE_SCENESWITCH, false);
                } else {
                    MySharedPreference.putBoolean(
                            Consts.MORE_DEVICE_SCENESWITCH, true);
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_LITTLEHELP)) {
                // 关于软件-小助手
                if (MySharedPreference.getBoolean(Consts.MORE_LITTLEHELP)) {
                    MySharedPreference
                            .putBoolean(Consts.MORE_LITTLEHELP, false);
                } else {
                    MySharedPreference.putBoolean(Consts.MORE_LITTLEHELP, true);
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_BROADCAST)) {
                // 关于软件-广播
                if (MySharedPreference.getBoolean(Consts.MORE_BROADCAST)) {
                    MySharedPreference.putBoolean(Consts.MORE_BROADCAST, false);
                } else {
                    MySharedPreference.putBoolean(Consts.MORE_BROADCAST, true);
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_TESTSWITCH)) {
                // 关于软件-测试服务器开关
                if (MySharedPreference.getBoolean(Consts.MORE_FOREIGNSWITCH)) {
                    MyLog.e(TAG, "can not open together");
                } else {
                    MySharedPreference.putString("ChannelIP", "");
                    MySharedPreference.putString("OnlineIP", "");
                    MySharedPreference.putString("ChannelIP_en", "");
                    MySharedPreference.putString("OnlineIP_en", "");
                    MySharedPreference.putString(Consts.AD_LIST, "");
                    if (MySharedPreference.getBoolean(Consts.MORE_TESTSWITCH)) {
                        MySharedPreference.putBoolean(Consts.MORE_TESTSWITCH,
                                false);
                    } else {
                        // MySharedPreference.clearAll();
                        // 打开测试开关要关闭记住密码功能
                        MySharedPreference.putBoolean(Consts.MORE_TESTSWITCH,
                                true);

                        MySharedPreference.putBoolean(Consts.MORE_REMEMBER,
                                false);
                    }
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_FOREIGNSWITCH)) {
                // 关于软件-国外服务器开关
                if (MySharedPreference.getBoolean(Consts.MORE_TESTSWITCH)) {
                    MyLog.e(TAG, "can not open together");
                } else {
                    MySharedPreference.putString("ChannelIP", "");
                    MySharedPreference.putString("OnlineIP", "");
                    MySharedPreference.putString("ChannelIP_en", "");
                    MySharedPreference.putString("OnlineIP_en", "");

                    if (MySharedPreference
                            .getBoolean(Consts.MORE_FOREIGNSWITCH)) {
                        MySharedPreference.putBoolean(
                                Consts.MORE_FOREIGNSWITCH, false);
                    } else {
                        // MySharedPreference.clearAll();
                        // 打开国外服务器开关要关闭记住密码功能
                        MySharedPreference.putBoolean(
                                Consts.MORE_FOREIGNSWITCH, true);
                        MySharedPreference.putBoolean(Consts.MORE_REMEMBER,
                                false);
                    }
                }

            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_VERSION)) {
                // 关于软件-版本号
                // 获取用户未读消息
                // v.php?mod=api&act=user_pm&sid=<>
                // sid 用户标识
                // return:
                // {"success":true,"msg":null,"errCode":null,"data":[{"url":"","count":""}]}
                // count:消息数量
                // url:消息页面
                // 现在success一直返回false
                Intent intentVersion = new Intent(mInstance,
                        JVVersionActivity.class);
                mInstance.startActivity(intentVersion);
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_DEVICESHARE)) {
                // 设备分享-废弃
            }
            // else if (mDataList.get(position).getItemFlag()
            // .equals(Consts.MORE_STATURL)) {
            // // 云视通指数
            // if (!MySharedPreference
            // .getBoolean(Consts.MORE_STATURL)) {
            // MySharedPreference.putBoolean(
            // Consts.MORE_STATURL, true);
            // mListener.OnFuncEnabled(0, 1);
            // }
            // if (!ConfigUtil.isConnected(mInstance)) {
            // mInstance.alertNetDialog();
            // } else {
            // if (null != ((BaseActivity) mInstance).statusHashMap
            // .get(Consts.MORE_STATURL)) {
            // Intent intentAD0 = new Intent(mInstance,
            // JVWebViewActivity.class);
            // intentAD0
            // .putExtra(
            // "URL",
            // ((BaseActivity) mInstance).statusHashMap
            // .get(Consts.MORE_STATURL));
            // intentAD0.putExtra("title", -2);
            // mInstance.startActivity(intentAD0);
            // } else {
            // if ("false".equals(mInstance.statusHashMap
            // .get(Consts.KEY_INIT_ACCOUNT_SDK))) {
            // MyLog.e("Login", "初始化账号SDK失败");
            // ConfigUtil
            // .initAccountSDK(((MainApplication) mInstance
            // .getApplication()));// 初始化账号SDK
            // }
            // GetDemoTask UrlTask2 = new GetDemoTask(
            // mInstance);
            // String[] demoParams2 = new String[3];
            // demoParams2[1] = "2";
            // UrlTask2.execute(demoParams2);
            // }
            // }
            // }
            else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_SYSTEMMESSAGE)) {
                // 新消息-系统消息
                if (!MySharedPreference.getBoolean(Consts.MORE_SYSTEMMESSAGE)) {
                    MySharedPreference.putBoolean(Consts.MORE_SYSTEMMESSAGE,
                            true);
                }
                if (!ConfigUtil.isConnected(mInstance)) {
                    mInstance.alertNetDialog();
                } else {
                    StatService.trackCustomEvent(
                            mInstance,
                            "MoreMessage",
                            mInstance.getResources().getString(
                                    R.string.census_moremessage));
                    Intent infoIntent = new Intent();
                    infoIntent.setClass(mInstance, JVSystemInfoActivity.class);
                    mInstance.startActivity(infoIntent);
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_SHOWMEDIA)) {
                // 图像查看
                StatService.trackCustomEvent(mInstance, "Media", mInstance
                        .getResources().getString(R.string.census_media));
                Intent intentMedia = new Intent(mInstance,
                        JVMediaActivity.class);
                mInstance.startActivity(intentMedia);
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_FEEDBACK)) {
                // 关于软件-意见反馈
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_UPDATE)) {
                // 关于软件-检查更新
                if (!ConfigUtil.isConnected(mInstance)) {
                    mInstance.alertNetDialog();
                } else {
                    mInstance.createDialog("", false);
                    CheckUpdateTask taskf = new CheckUpdateTask(mInstance);
                    String[] strParams = new String[3];
                    strParams[0] = "1";// 1,手动检查更新
                    taskf.execute(strParams);
                }
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.MORE_LITTLE)) {
                // // 关于
                // if (!MySharedPreference.getBoolean(Consts.MORE_LITTLE)) {
                // littlenum++;
                // System.out
                // .println("=========================littlenum======"
                // + littlenum);
                // if (littlenum < 20) {
                // if (littlenum >= 17) {
                // mInstance.showTextToast((20 - littlenum) + " ");
                // }
                // } else if (littlenum == 20) {
                // MySharedPreference.putBoolean(Consts.MORE_LITTLEHELP,
                // true);
                // MySharedPreference.putBoolean(Consts.MORE_BROADCAST,
                // true);
                // MySharedPreference.putBoolean(Consts.MORE_LITTLE, true);
                // showLittleHelper();
                // ListViewUtil
                // .setListViewHeightBasedOnChildren(mListView);
                // }
                // } else {
                // littlenum = 0;
                // MySharedPreference
                // .putBoolean(Consts.MORE_LITTLEHELP, false);
                // MySharedPreference.putBoolean(Consts.MORE_BROADCAST, false);
                // MySharedPreference.putBoolean(Consts.MORE_LITTLE, false);
                // dismissLittleHelper();
                // ListViewUtil.setListViewHeightBasedOnChildren(mListView);
                // }
            } else if (mDataList.get(position).getItemFlag()
                    .equals("MediaImage")) {
                // 图像查看-图像
                executeImageView("image");
            } else if (mDataList.get(position).getItemFlag()
                    .equals("MediaVideo")) {
                // 图像查看-视频
                executeImageView("video");
            } else if (mDataList.get(position).getItemFlag()
                    .equals("MediaDownload")) {
                // 图像查看-下载
                executeImageView("downVideo");
            } else if (mDataList.get(position).getItemFlag()
                    .equals(Consts.PROFILE_SETTING_MTU)) {
                // MTU设置
                showMtuDialog();
            }

            mAdapter.notifyDataSetChanged();
        }
    };

    /**
     * 初始化并显示MTU对话框
     */
    private void showMtuDialog() {
        mtuDialog = new Dialog(mInstance, R.style.mydialog);
        View view = LayoutInflater.from(mInstance).inflate(R.layout.dialog_mtu,
                null);
        mtuDialog.setContentView(view);
        dialogCancelImg = (ImageView) view.findViewById(R.id.dialog_cancle_img);
        dialogCancel = (TextView) view.findViewById(R.id.dialog_cancel);
        dialogCompleted = (TextView) view.findViewById(R.id.dialog_completed);
        final RadioGroup radioGroup = (RadioGroup) view
                .findViewById(R.id.group);
        RadioButton btn_one = (RadioButton) view.findViewById(R.id.mtu_btn_one);
        RadioButton btn_two = (RadioButton) view.findViewById(R.id.mtu_btn_two);
        resetRadioButtonSize(btn_one);
        resetRadioButtonSize(btn_two);

        // 获取mtu选中的值
        int selectedValue = MySharedPreference
                .getInt(Consts.PROFILE_SETTING_MTU_RADIO_BTN);
        switch (selectedValue) {
            case 0:// mtu -> 700
                btn_one.setChecked(true);
                break;
            case 1:// mtu -> 1400
                btn_two.setChecked(true);
                break;
            default:
        }

        mtuDialog.show();

        // 相应的点击事件
        dialogCancelImg.setOnClickListener(myOnClickListener);
        dialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mtuDialog.dismiss();
            }
        });
        dialogCompleted.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int mtu = 0;
                switch (radioGroup.getCheckedRadioButtonId()) {
                    case R.id.mtu_btn_one:// 700
                        mtu = Consts.MTU_700;
                        break;
                    case R.id.mtu_btn_two:// 1400
                        mtu = Consts.MTU_1400;
                        break;
                    default:

                }

                MyLog.v(TAG, "Mtu set ok, mtu:" + mtu);
                if (mtu == Consts.MTU_700) {
                    MySharedPreference.putInt(
                            Consts.PROFILE_SETTING_MTU_RADIO_BTN, 0);
                } else if (mtu == Consts.MTU_1400) {
                    MySharedPreference.putInt(
                            Consts.PROFILE_SETTING_MTU_RADIO_BTN, 1);
                }
                final int mtuParam = mtu;
                deviceList = CacheUtil.getDevList();
                if (null != deviceList && 0 != deviceList.size()) {

                    createDialog("", false);
                    Thread helperThread = new Thread() {

                        @Override
                        public void run() {
                            Jni.StopHelp();
                            int setRes = Jni.SetMTU(mtuParam);
                            // 设置成功
                            if (setRes == RESULT_OK) {

                            } else {
                                MyLog.v(TAG, "Mtu set failed, mtu:" + mtuParam);

                                // 切换到主线程处理最终结果
                                JVProfileFunctionActivity.this
                                        .runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                showTextToast(R.string.person_setting_mtu_failed);
                                            }
                                        });

                            }
                            if (MySharedPreference.getBoolean(
                                    Consts.MORE_LITTLEHELP, true)) {
                                boolean enableHelper = Jni.enableLinkHelper(
                                        true, 3, 10);// 开小助手
                                MyLog.v(TAG, "setMTU-enableHelper="
                                        + enableHelper);
                            }
                            PlayUtil.setHelperToList(deviceList);
                            handler.sendMessage(handler
                                    .obtainMessage(Consts.WHAT_DISMISS_DIALOG));
                            super.run();
                        }

                    };
                    helperThread.start();

                }

                mtuDialog.dismiss();
            }
        });
    }

    /**
     * 重新设置radiobutton的图标大小
     */
    private void resetRadioButtonSize(RadioButton button) {
        Drawable[] drawables = button.getCompoundDrawables();
        int width = (int) mResources
                .getDimension(R.dimen.person_setting_mtu_width);
        int height = (int) mResources
                .getDimension(R.dimen.person_setting_mtu_height);
        drawables[2].setBounds(0, 0, width, height);
        button.setCompoundDrawables(drawables[0], drawables[1], drawables[2],
                drawables[3]);
    }

    /**
     * Activity切换动画 - 左进右出
     */
    private void executeAnimLeftinRightout() {
        mInstance.finish();
        // 设置切换动画，从左边进入,右边退出
        overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
    }

    /**
     * Activity切换动画 - 右进左退
     */
    private void executeAnimRightinLeftout() {
        // 设置切换动画，从右边进入,左边退出
        overridePendingTransition(R.anim.enter_right, R.anim.exit_left);
    }

    /**
     * 图像查看相关处理
     * 
     * @param tag 分类标志 image:图像 video:视频 downVideo:下载
     */
    private void executeImageView(String tag) {
        Intent intent = new Intent();
        intent.setClass(mInstance, JVMediaListActivity.class);
        intent.putExtra("Media", tag);
        mInstance.startActivity(intent);
        executeAnimRightinLeftout();
    }

}
