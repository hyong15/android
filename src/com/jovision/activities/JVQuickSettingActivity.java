
package com.jovision.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.InputType;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.adapters.IpcWifiAdapter;
import com.jovision.adapters.MobileWifiAdapter;
import com.jovision.bean.Device;
import com.jovision.bean.WifiAdmin;
import com.jovision.commons.CommonInterface;
import com.jovision.commons.JVNetConst;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.BitmapCache;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.DeviceUtil;
import com.jovision.utils.PlayUtil;
import com.jovision.views.RefreshableListView;
import com.jovision.views.RefreshableListView.OnRefreshListener;
import com.jovision.views.SearchDevicesView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class JVQuickSettingActivity extends ShakeActivity implements
        CommonInterface {

    Dialog errorDialog = null;
    private final String TAG = "Qick_Set";
    private String setIpcName;// 待设置的IPC
    private String deviceNum;// 待设置的IPC云视通号
    private String desWifiSSID;//
    private String desWifiPWD;//
    private Boolean haveSet = false;// 是否点过快速配置

    private boolean local = true;
    private boolean isBack = false;// 是否要退出
    private Device ipcDevice;
    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private boolean hasBroadIP = false;// 是否已经广播到IP
    private boolean manuDiscon = false;// 手动调用断开

    LayoutInflater layoutFlater = null;
    protected RelativeLayout.LayoutParams reParamstop2;

    /** IPCwifi列表 */
    private LinearLayout ipcLayout;
    private Timer ipcTimer;
    private TimerTask ipcTimerTask;

    /** IPC网络 */
    private ArrayList<ScanResult> scanIpcWifiList = new ArrayList<ScanResult>();
    private RefreshableListView ipcWifiListView;
    private IpcWifiAdapter ipcAdapter;

    /** 手机wifi列表 */
    private LinearLayout mobileLayout;
    private EditText desWifiName;
    private EditText desWifiPass;
    private ToggleButton destWifiEye;

    /** webview帮助 */
    private LinearLayout helpLayout;
    private ImageView helpIV;

    /** 手机wifi列表（除IPC） */
    private ArrayList<ScanResult> scanMobileWifiList = new ArrayList<ScanResult>();
    private RefreshableListView mobileWifiListView;
    private MobileWifiAdapter mobileAdapter;
    //
    // /** 手动配置无线 */
    // private LinearLayout manualWifiLayout;
    // private Button settingManualConnectBt;// 手动设置WiFi
    // private EditText manualWifiNameEt, manualWifiPasswordEt;
    // private Spinner manualWifiEncryptSpinner, manualWifiAuthSpinner;
    // private Button addEditSaveBt;// 保存
    // // 认证，加密Spinner的适配器
    // private ArrayAdapter manualWifiEncryptSpinnerAdapter;// 加密类型
    // private ArrayAdapter manualWifiAuthSpinnerAdapter;// 加密方式
    //
    // private int wifiManualEncrypt = 0;
    // private int wifiManualAuth = 0;
    //
    // private Boolean isManual = false;

    // private String oldWifiSSID;
    private WifiAdmin wifiAdmin;
    private int connectFailedCounts = 0;// 连接设备失败次数
    // private Boolean hasLogout = false;// 是否已经注销账号

    /** 声波配置 */
    private SearchDevicesView searchView = null;
    public MediaPlayer myPlayer = new MediaPlayer();
    private PopupWindow quickSetPop = null;// 快速设置弹出框
    private LinearLayout quickSetParentLayout = null;// 父窗体布局
    private ImageView quickSetDeviceImg = null;// 搜索动画上的小摄像头图标
    private Button quickSetBackImg = null;// 快速配置动画弹出框上的返回按钮
    private boolean isSearching = false;// 正在循环配置设备
    private WifiManager wifiManager = null;

    private boolean stopTask = false;// 是否停止线程
    private boolean disConnected = false;// 视频是否断开

    // 播放操作步骤音频
    protected AssetManager assetMgr = null;
    protected MediaPlayer mediaPlayer = new MediaPlayer();

    @Override
    protected void initSettings() {
        // 播放声音
        playSound(Consts.TAG_SOUNDONE);
        MySharedPreference.putBoolean(Consts.AP_SETTING, true);
        local = Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN));
        // 首次提示设置步骤
        if (!MySharedPreference.getBoolean("AP_TIPS")) {
            alertTipsDialog();
        }
        deviceList = CacheUtil.getDevList();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void initUi() {
        setContentView(R.layout.quicksetting_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Intent intent = getIntent();
        if (null != intent) {
            oldWifiSSID = intent.getStringExtra("OLD_WIFI");
            scanIpcWifiList = intent.getParcelableArrayListExtra("IPC_LIST");
            scanMobileWifiList = intent
                    .getParcelableArrayListExtra("MOBILE_LIST");
        }

        wifiAdmin = new WifiAdmin(JVQuickSettingActivity.this);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.setWifiEnabled(true);

        leftBtn = (Button) findViewById(R.id.btn_left);
        rightBtn = (Button) findViewById(R.id.btn_right);
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        leftBtn.setVisibility(View.VISIBLE);
        rightBtn.setTextColor(Color.WHITE);
        rightBtn.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.setting_save));
        rightBtn.setText(getResources().getString(
                R.string.str_quick_setting_connect));
        rightBtn.setVisibility(View.VISIBLE);
        reParamstop2 = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        reParamstop2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        reParamstop2.addRule(RelativeLayout.CENTER_VERTICAL);
        reParamstop2.setMargins(0, 0, 30, 0);
        rightBtn.setLayoutParams(reParamstop2);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        currentMenu.setText(R.string.str_quick_setting);
        leftBtn.setOnClickListener(onClickListener);
        rightBtn.setOnClickListener(onClickListener);

        /** IPCwifi列表 */
        ipcLayout = (LinearLayout) findViewById(R.id.ipcwifilayout);
        ipcWifiListView = (RefreshableListView) findViewById(R.id.ipcwifilistview);

        layoutFlater = JVQuickSettingActivity.this.getLayoutInflater();

        RelativeLayout layout = (RelativeLayout) layoutFlater.inflate(
                R.layout.quiksettinglayoutheader, null);
        ImageView imgView = (ImageView) layout
                .findViewById(R.id.quicksetting_tips_img);
        RelativeLayout.LayoutParams reParams = new RelativeLayout.LayoutParams(
                disMetrics.widthPixels, (int) (0.5 * disMetrics.widthPixels));
        imgView.setLayoutParams(reParams);

        ipcWifiListView.addHeaderView(layout);
        ipcWifiListView.setOnRefreshListener(new WifiRefreshListener(true));
        ipcWifiListView.setOnItemClickListener(mOnItemClickListener);
        ipcAdapter = new IpcWifiAdapter(JVQuickSettingActivity.this);
        if (null != scanIpcWifiList && 0 != scanIpcWifiList.size()) {
            ipcAdapter.setData(scanIpcWifiList, oldWifiSSID);
            ipcWifiListView.setAdapter(ipcAdapter);
        }

        /** 手机wifi列表 */
        mobileLayout = (LinearLayout) findViewById(R.id.mobilewifilayout);
        desWifiName = (EditText) findViewById(R.id.deswifiname);
        desWifiPass = (EditText) findViewById(R.id.deswifipwd);
        desWifiPass
                .setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);// 显示密码
        destWifiEye = (ToggleButton) findViewById(R.id.deswifieye);
        mobileWifiListView = (RefreshableListView) findViewById(R.id.mobilewifilistview);
        mobileWifiListView.setOnRefreshListener(new WifiRefreshListener(false));
        mobileWifiListView.setOnItemClickListener(mOnItemClickListener);
        mobileAdapter = new MobileWifiAdapter(JVQuickSettingActivity.this);
        if (null != scanMobileWifiList && 0 != scanMobileWifiList.size()) {
            mobileAdapter.setData(scanMobileWifiList, oldWifiSSID);
            mobileWifiListView.setAdapter(mobileAdapter);
        }
        destWifiEye.setChecked(true);
        destWifiEye.setOnCheckedChangeListener(myOnCheckedChangeListener);

        /** 手动设置wifi */
        // // TODO
        // manualWifiLayout = (LinearLayout)
        // findViewById(R.id.manualwifilayout);
        // settingManualConnectBt = (Button)
        // findViewById(R.id.bt_setting_manualconnect);
        // settingManualConnectBt.setOnClickListener(onClickListener);
        // manualWifiNameEt = (EditText) findViewById(R.id.manual_wifiname_et);
        // manualWifiPasswordEt = (EditText)
        // findViewById(R.id.manual_wifipassword_et);
        // manualWifiAuthSpinner = (Spinner)
        // findViewById(R.id.manual_wifiauth_spinner);
        // manualWifiEncryptSpinner = (Spinner)
        // findViewById(R.id.manual_wifiencrypt_spinner);
        // addEditSaveBt = (Button) findViewById(R.id.manual_addeditsave);
        // manualWifiAuthSpinnerAdapter = ArrayAdapter.createFromResource(this,
        // R.array.array_wifiauth, android.R.layout.simple_spinner_item);// 方式
        // manualWifiEncryptSpinnerAdapter = ArrayAdapter
        // .createFromResource(this, R.array.array_wifiencrypt,
        // android.R.layout.simple_spinner_item);// 类型
        // // 设置下拉列表的风格
        // manualWifiAuthSpinnerAdapter
        // .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // manualWifiEncryptSpinnerAdapter
        // .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // // 将adapter 添加到spinner中
        // manualWifiAuthSpinner.setAdapter(manualWifiAuthSpinnerAdapter);
        // manualWifiEncryptSpinner.setAdapter(manualWifiEncryptSpinnerAdapter);
        // // 添加事件Spinner事件监听
        // manualWifiEncryptSpinner
        // .setOnItemSelectedListener(new SpinnerSelectedListener());
        // manualWifiAuthSpinner
        // .setOnItemSelectedListener(new SpinnerSelectedListener());
        //
        // addEditSaveBt.setOnClickListener(onClickListener);
        /** 帮助 */
        helpLayout = (LinearLayout) findViewById(R.id.helplayout);
        helpIV = (ImageView) findViewById(R.id.helpimg);
        // if (Consts.LANGUAGE_ZH == ConfigUtil
        // .getLanguage2(JVQuickSettingActivity.this)) {// 中文
        // helpIV.setBackgroundDrawable(getResources().getDrawable(
        // R.drawable.h400_update));
        // } else {
        // helpIV.setBackgroundDrawable(getResources().getDrawable(
        // R.drawable.h400_update_en));
        // }

        showIpcLayout(true);

        /************* 以下声波所需 **************/
        quickSetParentLayout = (LinearLayout) findViewById(R.id.quickSetParentLayout);
        // try {
        // LayoutInflater layoutInflater = LayoutInflater.from(this);
        // View view = layoutInflater
        // .inflate(R.layout.quicksetsearching, null);
        // Display display = getWindowManager().getDefaultDisplay();
        // int screenWidth = display.getWidth();
        // int screenHeight = display.getHeight();
        // quickSetPop = new PopupWindow(view, screenWidth, screenHeight);
        //
        // /**
        // * 为控件添加事件
        // *
        // */
        // quickSetBackImg = (Button) view.findViewById(R.id.quickSetBack);
        // quickSetBackImg.setOnClickListener(quickSetOnClick);
        //
        // quickSetDeviceImg = (ImageView) view
        // .findViewById(R.id.quickSetDeviceImg);
        // quickSetDeviceImg.setOnClickListener(quickSetOnClick);
        // searchView = (SearchDevicesView) view.findViewById(R.id.searchView);
        // searchView.setWillNotDraw(false);
        //
        // // 播放声音
        // playSound(Consts.TAG_SOUNDONE);
        //
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

    }

    @SuppressWarnings("deprecation")
    private void setHelpBackground(boolean is) {
        if (is) {
            if (Consts.LANGUAGE_ZH == ConfigUtil
                    .getLanguage2(JVQuickSettingActivity.this)) {// 中文
                helpIV.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.h400_update));
            } else {
                helpIV.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.h400_update_en));
            }
        } else {
            helpIV.setBackgroundDrawable(null);
        }
    }

    /**
     * wifi列表刷新事件
     * 
     * @author Administrator
     */
    private class WifiRefreshListener implements OnRefreshListener {
        boolean isIpc = false;

        public WifiRefreshListener(boolean b) {
            isIpc = b;
        }

        @Override
        public void onRefresh(RefreshableListView listView) {
            createDialog(R.string.quick_setting_searching, true);
            RefreshWifiThread giwt = new RefreshWifiThread(
                    JVQuickSettingActivity.this, isIpc);
            giwt.start();
        }

    }

    /**
     * 显示ipc或手机界面
     * 
     * @param show
     */
    private void showIpcLayout(boolean show) {
        if (show) {
            ipcLayout.setVisibility(View.VISIBLE);
            mobileLayout.setVisibility(View.GONE);
            rightBtn.setVisibility(View.GONE);
            if (null != oldWifiSSID && !"".equalsIgnoreCase(oldWifiSSID)
                    && !"0x".equalsIgnoreCase(oldWifiSSID)) {
                desWifiName.setText(oldWifiSSID);
            } else {
                desWifiName.setText("");
            }
            desWifiPass.setText("");
            startRefreshWifiTimer();
        } else {
            ipcLayout.setVisibility(View.GONE);
            mobileLayout.setVisibility(View.VISIBLE);
            rightBtn.setVisibility(View.VISIBLE);
            stopRefreshWifiTimer();
        }
    }

    /**
     * 密码显示隐藏
     */
    OnCheckedChangeListener myOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            if (arg1) {
                desWifiPass
                        .setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);// 显示密码
            } else {
                desWifiPass.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);// 隐藏密码
            }
            desWifiPass.setSelection(desWifiPass.getText().toString().length());
            destWifiEye.setChecked(arg1);
        }

    };

    /**
     * 连接wifi 30s超时
     * 
     * @param wifi
     * @param password
     * @return
     */
    public boolean connectWifi(String wifi, String password) {

        boolean flag = wifiAdmin.getWifiState(wifi);
        int errorCount = 0;
        while (!flag) {
            MyLog.v(TAG, "wifi=" + wifi + ";errorCount=" + errorCount
                    + ";flag=" + flag);
            if (errorCount > 30) {
                break;
            }
            if (errorCount == 0 || errorCount == 10 || errorCount == 20
                    || errorCount == 4 || errorCount == 14 || errorCount == 18
                    || errorCount == 24 || errorCount == 28) {
                WifiConfiguration desWifi = wifiAdmin.isExsits(wifi);

                if (null != desWifi) {// 目标Ap已存在wifi列表里，需要移除重新连接，防止连接已存在的一直连接不上
                    wifiAdmin.disconnectWifi(desWifi, true);// 关掉，移除,不移除会记住原来的密码
                    try {
                        Thread.sleep(1 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    desWifi = wifiAdmin.isExsits(wifi);
                }

                if (null == desWifi) {
                    int enc = 0;
                    if ("".equalsIgnoreCase(password)) {
                        enc = 1;
                    } else {
                        enc = 3;
                    }
                    desWifi = wifiAdmin.CreateWifiInfo(wifi, password, enc);
                }

                wifiAdmin.ConnectWifiByConfig(desWifi);
            }

            errorCount += 2;

            flag = wifiAdmin.getWifiState(wifi);
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return flag;
    }

    /**
     * AP设置线程
     */
    class ApSetThread extends Thread {
        @Override
        public void run() {
            Looper.prepare();
            // 验证wifi流程
            // // 和待配置的网络一样关闭网络并移除
            // if (null != oldWifiSSID
            // && desWifiSSID.equalsIgnoreCase(oldWifiSSID)) {
            // WifiConfiguration oldWifi = wifiAdmin.isExsits(oldWifiSSID);
            // if (null != oldWifi) {
            // wifiAdmin.disconnectWifi(oldWifi, true);// 关掉，移除,不移除会记住原来的密码
            // }
            // }
            // boolean mobileFlag = connectWifi(desWifiSSID, desWifiPWD);
            //
            // if (!mobileFlag) {
            // handler.sendMessage(handler.obtainMessage(
            // JVConst.MOBILE_WIFI_CON_FAILED, -1, 0));
            // return;
            // }

            // 和待配置的网络一样关闭网络并移除
            WifiConfiguration oldWifi = wifiAdmin.isExsits(desWifiSSID);
            if (null != oldWifi) {
                wifiAdmin.disconnectWifi(oldWifi, true);// 关掉，移除,不移除会记住原来的密码
            }

            boolean ipcFlag = connectWifi(setIpcName,
                    getResources().getString(R.string.str_wifi_pwd));

            if (!ipcFlag) {
                handler.sendMessage(handler.obtainMessage(
                        Consts.WHAT_QUICK_SETTING_ERROR, 1001, 0));
            } else {
                MyLog.v(TAG, "开始连接AP视频--" + ipcDevice.getFullNo());
                ipcDevice.setIp(Consts.IPC_DEFAULT_IP);
                ipcDevice.setPort(Consts.IPC_DEFAULT_PORT);
                PlayUtil.connectDevice(ipcDevice);
            }
            super.run();
        }

    };

    private String getlocalip() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        MyLog.d(TAG, "int ip " + ipAddress);
        if (ipAddress == 0)
            return null;
        return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
    }

    private boolean wifiIsConnect() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // NetworkInfo mobNetInfo = connectivityManager
        // .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        NetworkInfo wifiNetInfo = connectivityManager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (!wifiNetInfo.isConnected()) {
            MyLog.e("MyRecevier", "网络不可以用");
            return false;
            // 改变背景或者 处理网络的全局变量
        } else {
            // 改变背景或者 处理网络的全局变量
            return true;
        }
    }

    // 设置三种类型参数分别为String,Integer,String
    class ConnectAPTask extends AsyncTask<String, Integer, Integer> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {
            int connRes = -1;// 0：成功 1：失败

            // // 非本地登录,切未注销账号，调用注销
            // if (!local && !hasLogout) {
            // if (0 != AccountUtil.userLogout()) {
            // AccountUtil.userLogout();
            // }
            // hasLogout = true;
            // }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // 和待配置的网络一样直接连接视频
            if (null != oldWifiSSID && setIpcName.equalsIgnoreCase(oldWifiSSID)) {// 当前连接的网路就是要配置ap网络
                connRes = 0;
            } else {
                boolean ipcFlag = connectWifi(setIpcName, getResources()
                        .getString(R.string.str_wifi_pwd));

                if (ipcFlag) {
                    connRes = 0;
                } else {
                    connRes = 1;
                }
            }
            if (stopTask) {// 停止线程
                return -1;
            }

            int count = 1;
            while (true) {
                String ip = null;
                if (null != (ip = getlocalip()) && wifiIsConnect()) {
                    count += 1;
                    MyLog.e(TAG, "ip= " + ip);
                    break;
                } else {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count += 1;
                }

                if (count == 10)
                    break;

            }
            return connRes;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            // 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
            if (0 == result) {
                ArrayList<Device> devList = new ArrayList<Device>();
                devList.add(ipcDevice);
                PlayUtil.prepareConnect(devList, 0);
                String devJsonString = Device.listToString(devList);
                Intent apIntent = new Intent(JVQuickSettingActivity.this,
                        JVPlayActivity.class);
                apIntent.putExtra("PlayFlag", Consts.PLAY_AP);
                apIntent.putExtra("DeviceIndex", 0);
                apIntent.putExtra("ChannelofChannel", 1);
                apIntent.putExtra(Consts.KEY_PLAY_AP, devJsonString);
                startActivityForResult(apIntent, Consts.WHAT_AP_CONNECT_REQUEST);
            } else if (1 == result) {
                showTextToast(R.string.str_quick_setting_ap_net_timeout);
            }
            dismissDialog();
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
            createDialog(R.string.quick_setting_connecting, false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新进度,此方法在主线程执行，用于显示任务执行的进度。
        }
    }

    /**
     * 获取IPC待配置设备列表线程
     * 
     * @author Administrator
     */
    private class RefreshWifiThread extends Thread {
        private final WeakReference<JVQuickSettingActivity> mActivity;
        private boolean isIpc = false;

        public RefreshWifiThread(JVQuickSettingActivity activity, boolean ipc) {
            mActivity = new WeakReference<JVQuickSettingActivity>(activity);
            isIpc = ipc;
        }

        @Override
        public void run() {
            super.run();
            try {
                JVQuickSettingActivity activity = mActivity.get();
                if (null != activity) {
                    if (isIpc) {// 刷新ipcwifi
                        if (wifiAdmin.getWifiState()) {
                            scanIpcWifiList = wifiAdmin.startScanIPC();
                        }
                        if (null == scanIpcWifiList
                                || 0 == scanIpcWifiList.size()) {
                            scanIpcWifiList = wifiAdmin.startScanIPC();
                        }

                        if (null != scanIpcWifiList
                                && 0 != scanIpcWifiList.size()) {
                            handler.sendMessage(handler.obtainMessage(
                                    Consts.WHAT_QUICK_SETTING_IPC_WIFI_SUCCESS,
                                    -1, 0));
                        } else {
                            handler.sendMessage(handler.obtainMessage(
                                    Consts.WHAT_QUICK_SETTING_IPC_WIFI_FAILED,
                                    -1, 0));
                        }
                    } else {// 刷新手机wifi
                        if (wifiAdmin.getWifiState()) {
                            scanMobileWifiList = wifiAdmin.startScanWifi();
                        }
                        if (null == scanMobileWifiList
                                || 0 == scanMobileWifiList.size()) {
                            scanMobileWifiList = wifiAdmin.startScanWifi();
                        }
                        handler.sendMessage(handler.obtainMessage(
                                Consts.WHAT_QUICK_SETTING_MOBILE_WIFI_SUCC, -1,
                                0));
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 配置流程Dialog
     * 
     * @param tag
     */
    private void alertTipsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                JVQuickSettingActivity.this);
        builder.setCancelable(false);

        builder.setTitle(getResources().getString(R.string.str_device_ap_pro));
        LayoutInflater li = JVQuickSettingActivity.this.getLayoutInflater();
        LinearLayout layout = (LinearLayout) li.inflate(R.layout.ap_set_tips,
                null);
        ToggleButton noAlert = (ToggleButton) layout.findViewById(R.id.noalert);
        noAlert.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                MySharedPreference.putBoolean("AP_TIPS", arg1);
            }

        });

        builder.setView(layout);

        // builder.setPositiveButton(R.string.cancel,
        // new DialogInterface.OnClickListener() {
        // @Override
        // public void onClick(DialogInterface dialog, int which) {
        // dialog.dismiss();
        // }
        //
        // });
        builder.setNegativeButton(R.string.str_device_ap_start,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
        Dialog dialog = builder.create();
        dialog.show();
    }

    // /**
    // * 网络连接失败Dialog
    // *
    // * @param tag
    // */
    // private void netErrorDialog1() {
    // AlertDialog.Builder builder = new AlertDialog.Builder(
    // JVQuickSettingActivity.this);
    // builder.setCancelable(true);
    //
    // builder.setTitle(getResources().getString(
    // R.string.str_quick_setting_alert2));
    // builder.setMessage(R.string.net_errors);
    //
    // builder.setPositiveButton(R.string.sure,
    // new DialogInterface.OnClickListener() {
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // dialog.dismiss();
    // }
    //
    // });
    // builder.create().show();
    // }

    /**
     * onclick事件
     */
    OnClickListener onClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left:
                    backMethod();
                    break;
                case R.id.manual_addeditsave:
                case R.id.btn_right: // 保存按钮
                    // TODO
                    // MyLog.i(TAG, "标志"+isManual);
                    // if (isManual) {
                    // desWifiSSID = manualWifiNameEt.getText().toString();
                    // if ("".equalsIgnoreCase(desWifiSSID)) {
                    // showTextToast(R.string.str_wifiname_not_null);
                    // break;
                    // }
                    // desWifiPWD = manualWifiPasswordEt.getText().toString();
                    // } else {
                    MyLog.i(TAG, "不是手动WiFi");
                    desWifiSSID = desWifiName.getText().toString();
                    if ("".equalsIgnoreCase(desWifiSSID)) {
                        showTextToast(R.string.str_wifiname_not_null);
                        break;
                    }
                    desWifiPWD = desWifiPass.getText().toString();
                    // }
                    haveSet = true;
                    // 播放提示声音
                    // playSound(Consts.TAG_SOUNDFIVE);
                    isSearching = true;

                    ApSetThread apSetThread = new ApSetThread();
                    apSetThread.start();

                    showQuickPopWindow();
                    if (null != myPlayer && myPlayer.isPlaying()) {
                        myPlayer.stop();
                        myPlayer.release();
                        myPlayer = null;
                    }
                    showSearch(true);
                    break;
            // case R.id.bt_setting_manualconnect:// 手动设置wifi
            // manualWifiLayout.setVisibility(View.VISIBLE);
            // helpLayout.setVisibility(View.GONE);
            // ipcLayout.setVisibility(View.GONE);
            // mobileLayout.setVisibility(View.GONE);
            // rightBtn.setVisibility(View.GONE);
            // currentMenu.setText(R.string.str_quick_setting_wifibymanul);
            // manualWifiNameEt.setText("");
            // manualWifiPasswordEt.setText("");
            // isManual = true;
            // break;
            }
        }
    };

    @Override
    public void onBackPressed() {
        backMethod();
    }

    // // Spinner 的点击操作
    // class SpinnerSelectedListener implements OnItemSelectedListener {
    //
    // public void onItemSelected(AdapterView<?> view, View arg1, int index,
    // long arg3) {
    // if (view.getId() == R.id.manual_wifiauth_spinner) {
    // wifiManualAuth = index;
    // MyLog.i(TAG, "加密方式" + index);
    // } else if (view.getId() == R.id.manual_wifiencrypt_spinner) {
    // wifiManualEncrypt = index;
    // MyLog.i(TAG, "加密类型" + index);
    // }
    // }
    //
    // public void onNothingSelected(AdapterView<?> view) {
    // }
    // }

    /**
     * 返回方法
     * 
     * @param id
     */
    public void backMethod() {

        if (null != myPlayer) {
            myPlayer.stop();
        }

        try {
            if (isSearching) {// 正在搜索设备是，提示是否退出
                Log.e("tags", "back isSearching");
                backDialog();
            } else {
                manuDiscon = true;
                // PlayUtil.disconnectDevice();
                disConnectVideo();
                if (View.VISIBLE == helpLayout.getVisibility()) {// 更新设备帮助文档
                    helpLayout.setVisibility(View.GONE);
                    setHelpBackground(false);
                    rightBtn.setVisibility(View.VISIBLE);
                    currentMenu.setText(R.string.str_quick_setting);
                } else if (ipcLayout.getVisibility() == View.VISIBLE) {// 显示IPC网络信息列表（一级级wifi列表）
                    stopTask = true;
                    createDialog(R.string.quick_setting_exiting, false);
                    isBack = true;
                    ResetWifiTask task = new ResetWifiTask();
                    String[] params = new String[3];
                    params[0] = "false";
                    task.execute(params);
                    // } else if (View.VISIBLE ==
                    // manualWifiLayout.getVisibility()) {// 手动设置WiFi
                    // manualWifiLayout.setVisibility(View.GONE);
                    // ipcLayout.setVisibility(View.GONE);
                    // mobileLayout.setVisibility(View.VISIBLE);
                    // rightBtn.setVisibility(View.VISIBLE);
                    // currentMenu.setText(R.string.str_quick_setting);
                } else {// 显示主控wifi信息列表（二级wifi列表）
                    showIpcLayout(true);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 两个wifi列表点击事件
     */
    OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            try {
                if (ipcLayout.getVisibility() == View.VISIBLE) {// IPC设备列表
                    if (arg2 <= 0) {
                        return;
                    }
                    if (null != scanIpcWifiList && 0 != scanIpcWifiList.size()
                            && arg2 - 1 < scanIpcWifiList.size()) {
                        ipcAdapter.init(arg2);
                        ipcAdapter.notifyDataSetChanged();
                        Log.i("TAG", arg2 + "SDSD");
                        setIpcName = scanIpcWifiList.get(arg2 - 1).SSID
                                .replace("\"", "");

                        String str1 = setIpcName.replace("\"", "");
                        String str2 = str1.substring(6, str1.length());
                        deviceNum = str2;

                        ipcDevice = new Device(Consts.IPC_DEFAULT_IP,
                                Consts.IPC_DEFAULT_PORT,
                                ConfigUtil.getGroup(deviceNum),
                                ConfigUtil.getYST(deviceNum),
                                Consts.IPC_DEFAULT_USER,
                                Consts.IPC_DEFAULT_PWD, true, 1, 1, null);

                        stopRefreshWifiTimer();

                        playSound(Consts.TAG_SOUNDTOW);

                        ConnectAPTask task = new ConnectAPTask();
                        String[] params = new String[3];
                        task.execute(params);
                        // showIpcLayout(false);

                    }
                } else {// 手机wifi信息列表
                    if (null != scanMobileWifiList
                            && 0 != scanMobileWifiList.size()) {
                        desWifiName.setText(scanMobileWifiList.get(arg2).SSID);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    @Override
    protected void saveSettings() {
        CacheUtil.saveDevList(deviceList);
    }

    @Override
    protected void freeMe() {
        if (null != quickSetPop) {
            quickSetPop.dismiss();
        }
        try {
            stopTask = true;
            searchView.stopPlayer();
            if (null != myPlayer) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = null;
            }
            stopRefreshWifiTimer();
            MySharedPreference.putBoolean(Consts.AP_SETTING, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        alarmnet = null;
        Log.v(TAG, "freeMe");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (Consts.WHAT_AP_CONNECT_REQUEST == requestCode) {
            switch (resultCode) {
                case Consts.WHAT_AP_CONNECT_FINISHED:// AP检测视频完成
                    boolean back = data.getBooleanExtra("AP_Back", false);
                    if (back) {
                        showIpcLayout(true);// ipc列表切换Wi-Fi列表
                    } else {
                        showIpcLayout(false);// ipc列表切换Wi-Fi列表
                        // 播放提示声音
                        playSound(Consts.TAG_SOUNDFOUR);
                    }
                    break;
            }
        }

    }

    public void startRefreshWifiTimer() {
        ipcTimerTask = new TimerTask() {

            @Override
            public void run() {
                try {

                    Log.v("IPC—wifi—自动刷新—", "10秒时间到");
                    if (wifiAdmin.getWifiState()) {
                        scanIpcWifiList = wifiAdmin.startScanIPC();
                    }

                    // 万一取失败了，接着再取一次
                    if (null == scanIpcWifiList || 0 == scanIpcWifiList.size()) {
                        scanIpcWifiList = wifiAdmin.startScanIPC();
                    }
                    handler.sendMessage(handler.obtainMessage(
                            Consts.WHAT_QUICK_SETTING_IPC_WIFI_SUCCESS, -1, 1));

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        };

        if (null == ipcTimer) {
            ipcTimer = new Timer();
            if (null != ipcTimerTask) {
                ipcTimer.schedule(ipcTimerTask, 10 * 1000, 10 * 1000);
            }
        }
    }

    public void stopRefreshWifiTimer() {
        if (null != ipcTimer) {
            ipcTimer.cancel();
            ipcTimer = null;
        }
        if (null != ipcTimerTask) {
            ipcTimerTask.cancel();
            ipcTimerTask = null;
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.CALL_CONNECT_CHANGE: {
                switch (arg2) {
                // 2 -- 断开连接成功
                    case JVNetConst.DISCONNECT_OK:
                        // 4 -- 连接失败
                    case JVNetConst.CONNECT_FAILED:
                        // 6 -- 连接异常断开
                    case JVNetConst.ABNORMAL_DISCONNECT:
                        // 7 -- 服务停止连接，连接断开
                    case JVNetConst.SERVICE_STOP:
                        break;
                    case Consts.BAD_NOT_CONNECT: {
                        disConnected = true;
                        MyLog.e(TAG, "线程断开成功");
                        break;
                    }
                }
            }
            default:
                handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
                break;
        }

    }

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        MyLog.e(TAG, "onHandler: " + what + ", " + arg1 + ", " + arg2 + ", "
                + obj);
        switch (what) {
            case Consts.WHAT_QUICK_SETTING_IPC_WIFI_SUCCESS:// 获取IPC 列表成功

                if (1 != arg2) {
                    ipcWifiListView.completeRefreshing();
                }
                if (null != scanIpcWifiList && 0 != scanIpcWifiList.size()) {
                    ipcAdapter.setData(scanIpcWifiList, oldWifiSSID);
                    ipcWifiListView.setAdapter(ipcAdapter);
                }
                dismissDialog();
                break;
            case Consts.WHAT_QUICK_SETTING_IPC_WIFI_FAILED:// 获取IPC 列表失败
                ipcWifiListView.completeRefreshing();
                dismissDialog();
                break;
            case Consts.WHAT_QUICK_SETTING_DEV_ONLINE: {// 网络恢复成功
                try {
                    playSound(Consts.TAG_SOUNDSIX);// 播放“叮”的一声
                    showSearch(false);
                    // // 设置全屏
                    // JVQuickSettingActivity.this.getWindow().setFlags(
                    // WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    // WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    quickSetDeviceImg.setVisibility(View.VISIBLE);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
            case Consts.WHAT_QUICK_SETTING_ERROR:// 配置出错
                if (null != errorDialog && errorDialog.isShowing()) {
                    errorDialog.dismiss();
                    errorDialog = null;
                }

                errorDialog(arg1);
                break;
            case Consts.WHAT_QUICK_SETTING_MOBILE_WIFI_SUCC:// 快速设置获取主控WIFI信息数据
                mobileWifiListView.completeRefreshing();
                if (null != scanIpcWifiList && 0 != scanMobileWifiList.size()) {
                    mobileAdapter.setData(scanMobileWifiList, oldWifiSSID);
                    mobileWifiListView.setAdapter(mobileAdapter);
                }
                dismissDialog();
                break;
            case Consts.WHAT_AP_SET_SUCCESS:// AP配置成功
                dismissDialog();
                oldWifiSSID = desWifiSSID;
                // 断开连接
                manuDiscon = true;
                // PlayUtil.disconnectDevice();
                disConnectVideo();
                ResetWifiTask task = new ResetWifiTask();
                String[] params = new String[3];
                params[0] = "true";
                task.execute(params);
                break;
            case Consts.WHAT_AP_SET_FAILED:// AP配置失败
                dismissDialog();
                // 断开连接
                manuDiscon = true;
                // PlayUtil.disconnectDevice();
                disConnectVideo();
                AlertDialog.Builder builder5 = new AlertDialog.Builder(
                        JVQuickSettingActivity.this);

                builder5.setTitle(R.string.tips);
                builder5.setMessage(R.string.str_quick_setting_failed);

                builder5.setPositiveButton(R.string.str_quick_setting_reset,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.sendMessage(handler
                                        .obtainMessage(Consts.WHAT_QUICK_SETTING_WIFI_CONTINUE_SET));
                                dialog.dismiss();
                            }
                        });
                builder5.setNegativeButton(R.string.str_quick_setting_finish,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                handler.sendMessage(handler
                                        .obtainMessage(Consts.WHAT_QUICK_SETTING_WIFI_FINISH_SET));
                                dialog.dismiss();
                            }
                        });
                AlertDialog dialog5 = builder5.create();
                dialog5.setCancelable(false);
                dialog5.show();
                break;

            case Consts.WHAT_QUICK_SETTING_CONNECT_FAILED:// 快速设置设备连接失败,失败一次再连接一次，直到第十次失败停止
                MyLog.v("快速设置设备连接失败", "第-----" + connectFailedCounts + "-----次");
                if (connectFailedCounts < 10) {
                    connectFailedCounts++;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ipcDevice.setIp(Consts.IPC_DEFAULT_IP);
                    ipcDevice.setPort(Consts.IPC_DEFAULT_PORT);
                    PlayUtil.connectDevice(ipcDevice);
                } else {
                    connectFailedCounts = 0;// 连接失败次数复位
                    dismissDialog();

                    if (isSearching) {
                        handler.sendMessage(handler.obtainMessage(
                                Consts.WHAT_QUICK_SETTING_ERROR, 1001, 0));
                    } else {
                        AlertDialog.Builder builder1 = new AlertDialog.Builder(
                                JVQuickSettingActivity.this);

                        builder1.setTitle(R.string.tips);
                        builder1.setMessage(R.string.str_host_connect_failed);

                        builder1.setPositiveButton(R.string.sure,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        dialog.dismiss();
                                    }
                                });
                        builder1.create().show();
                    }

                }

                break;

            case Consts.WHAT_DEVICE_MOST_COUNT:// 设备添加超过最大数
                showTextToast(R.string.str_device_most_count);
                break;
            case Consts.WHAT_QUICK_SETTING_WIFI_CHANGED_FAIL:// 切换到原来的wifi失败,回到登陆界面
                showTextToast(R.string.str_wifi_reset_fail);
                dismissDialog();
                // MyActivityManager.getActivityManager().popAllActivityExceptOne(
                // JVLoginActivity.class);
                // Intent intent = new Intent();
                // String userName = JVQuickSettingActivity.this.statusHashMap
                // .get(Consts.KEY_USERNAME);
                // intent.putExtra("UserName", userName);
                // intent.setClass(JVQuickSettingActivity.this,
                // JVLoginActivity.class);
                // JVQuickSettingActivity.this.startActivity(intent);
                Intent intent = new Intent();
                setResult(Consts.DEVICE_ADD_SUCCESS_RESULT, intent);
                JVQuickSettingActivity.this.finish();
                break;
            // case Consts.CALL_QUERY_DEVICE: {// 广播到设备IP
            // hasBroadIP = true;
            // JSONObject broadObj;
            // try {
            // broadObj = new JSONObject(obj.toString());
            // if (1 == broadObj.optInt("type")) {// 广播到了
            // ipcDevice.setOnlineState(1);
            // ipcDevice.setIp(broadObj.optString("ip"));
            // ipcDevice.setPort(broadObj.optInt("port"));
            //
            // for (Device dev : deviceList) {
            // if (dev.getFullNo().equalsIgnoreCase(
            // ipcDevice.getFullNo())) {
            // dev.setOnlineState(1);
            // dev.setIp(broadObj.optString("ip"));
            // dev.setPort(broadObj.optInt("port"));
            // }
            // }
            //
            // } else {
            // ipcDevice.setOnlineState(0);
            // ipcDevice.setIp("");
            // ipcDevice.setPort(0);
            // }
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            // // hasBroadIP: 174, 0, 0,
            // //
            // {"gid":"S","ip":"172.26.97.3","no":52942216,"port":9101,"type":1}
            // MyLog.e(TAG, "hasBroadIP9999999999999999999999999999: " + what
            // + ", " + arg1 + ", " + arg2 + ", " + obj);
            //
            // break;
            // }
            // 广播回调
            case Consts.CALL_QUERY_DEVICE: {
                if (hasBroadIP) {
                    break;
                }
                MyLog.v(TAG, "CALL_QUERY_DEVICE = what=" + what + ";arg1=" + arg1
                        + ";arg2=" + arg1 + ";obj=" + obj.toString());
                // MyLog.v("广播回调", "onTabAction2:what=" + what + ";arg1=" + arg1
                // + ";arg2=" + arg1 + ";obj=" + obj.toString());
                // onTabAction:what=168;arg1=0;arg2=0;obj={"count":1,"curmod":0,"gid":"A","ip":"192.168.21.238","netmod":0,"no":283827713,"port":9101,"timeout":0,"type":59162,"variety":3}
                JSONObject broadObj;
                try {
                    broadObj = new JSONObject(obj.toString());
                    int netmod = broadObj.optInt("netmod");
                    int timeOut = broadObj.optInt("timeout");
                    String ip = broadObj.optString("ip");
                    int port = broadObj.optInt("port");
                    if (0 == timeOut) {
                        // 防止广播到设备没ip
                        if (!"".equalsIgnoreCase(ip) && 0 != port) {
                            String broadDevNum = broadObj.optString("gid")
                                    + broadObj.optInt("no");
                            MyLog.v("广播回调", "未超时--" + hasBroadIP + "--"
                                    + broadDevNum);
                            if (null != ipcDevice) {
                                if (broadDevNum.equalsIgnoreCase(ipcDevice
                                        .getFullNo())) {// 同一个设备
                                    ipcDevice.setOnlineStateLan(1);
                                    ipcDevice.setIp(broadObj.optString("ip"));
                                    ipcDevice.setPort(broadObj.optInt("port"));
                                    ipcDevice.setHasWifi(broadObj.optInt("netmod"));
                                    hasBroadIP = true;
                                } else {
                                    ipcDevice.setOnlineStateLan(0);
                                    ipcDevice.setIp("");
                                    ipcDevice.setPort(0);
                                }
                            }
                        }

                    } else {
                        MyLog.v("广播回调", "超时--" + hasBroadIP);
                        hasBroadIP = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                MyLog.e(TAG, "hasBroadIP" + what + ", " + arg1 + ", " + arg2 + ", "
                        + obj);

                break;
            }
            case Consts.CALL_CONNECT_CHANGE: // 连接回调
                MyLog.e(TAG, "CONNECT_CHANGE: " + what + ", " + arg1 + ", " + arg2
                        + ", " + obj);

                if (Consts.BAD_NOT_CONNECT == arg2) {
                    break;
                } else if (JVNetConst.CONNECT_OK == arg2) {
                    // // 暂停视频
                    // Jni.sendBytes(Consts.CHANNEL_JY,
                    // JVNetConst.JVN_CMD_VIDEOPAUSE,
                    // new byte[0], 8);
                    // 请求文本聊天
                    Jni.sendBytes(1, JVNetConst.JVN_REQ_TEXT, new byte[0], 8);
                } else {
                    if (manuDiscon) {
                        manuDiscon = false;
                        // handler.sendMessage(handler.obtainMessage(
                        // Consts.QUICK_SETTING_CONNECT_FAILED,
                        // R.string.connect_failed, 0));
                    } else {
                        handler.sendMessage(handler.obtainMessage(
                                Consts.WHAT_QUICK_SETTING_CONNECT_FAILED,
                                R.string.connect_failed, 0));
                    }

                }

                break;
            case Consts.CALL_TEXT_DATA:// 文本回调
                MyLog.e(TAG, "TEXT_DATA: " + what + ", " + arg1 + ", " + arg2
                        + ", " + obj);
                switch (arg2) {
                    case JVNetConst.JVN_RSP_TEXTACCEPT:// 同意文本聊天
                        // 获取基本文本信息
                        Jni.sendTextData(1, JVNetConst.JVN_RSP_TEXTDATA, 8,
                                JVNetConst.JVN_REMOTE_SETTING);
                        break;
                    case JVNetConst.JVN_CMD_TEXTSTOP:// 不同意文本聊天
                        handler.sendMessage(handler.obtainMessage(
                                Consts.WHAT_QUICK_SETTING_ERROR, 1002, 0));
                        // dismissDialog();
                        // // 断开连接
                        // PlayUtil.disconnectDevice();
                        // showTextToast(R.string.str_only_administator_use_this_function);
                        break;

                    case JVNetConst.JVN_RSP_TEXTDATA:// 文本数据
                        try {
                            JSONObject dataObj = new JSONObject(obj.toString());
                            switch (dataObj.getInt("flag")) {
                            // 远程配置请求，获取到配置文本数据
                                case JVNetConst.JVN_REMOTE_SETTING: {
                                    String settingJSON = dataObj.getString("msg");
                                    HashMap<String, String> settingMap = ConfigUtil
                                            .genMsgMap(settingJSON);

                                    if (null != settingMap
                                            && null != settingMap.get("DEV_VERSION")) {
                                        String dvStr = settingMap.get("DEV_VERSION");
                                        int dvInt = Integer.parseInt(dvStr);
                                        if (dvInt == 1) {// 当前最新版本
                                            // MyLog.i(TAG, "save标志"+isManual);
                                            // if (isManual) {
                                            // saveManualWifi(manualWifiNameEt.getText()
                                            // .toString(), manualWifiPasswordEt
                                            // .getText().toString(),
                                            // wifiManualAuth,
                                            // wifiManualEncrypt);
                                            // } else {
                                            saveWifi();
                                            // }

                                        } else {// 旧版
                                            dismissDialog();
                                            updateDialog1();
                                            break;
                                        }
                                    } else {
                                        dismissDialog();
                                        updateDialog1();
                                        break;
                                    }
                                    break;
                                }
                                case JVNetConst.JVN_WIFI_INFO:// 2-- AP,WIFI热点请求
                                    // 获取主控码流信息请求
                                    Jni.sendTextData(1, JVNetConst.JVN_RSP_TEXTDATA, 8,
                                            JVNetConst.JVN_STREAM_INFO);
                                    break;
                                case JVNetConst.JVN_STREAM_INFO:// 3-- 码流配置请求

                                    break;
                                case JVNetConst.EX_WIFI_AP_CONFIG:// 11
                                                                  // ---新wifi配置流程
                                    if (0 == dataObj.getInt("result")) {
                                        handler.sendMessage(handler.obtainMessage(
                                                Consts.WHAT_AP_SET_SUCCESS, 0, 0));
                                    } else {
                                        // handler.sendMessage(handler.obtainMessage(
                                        // JVConst.AP_SET_FAILED, 0, 0));
                                        handler.sendMessage(handler.obtainMessage(
                                                Consts.WHAT_QUICK_SETTING_ERROR, 1003, 0));
                                    }

                                    break;
                                case JVNetConst.JVN_WIFI_SETTING_SUCCESS:// 4--
                                    // wifi配置成功
                                    handler.sendMessage(handler.obtainMessage(
                                            Consts.WHAT_AP_SET_SUCCESS, 0, 0));
                                    break;
                                case JVNetConst.JVN_WIFI_SETTING_FAILED:// 5--
                                    // WIFI配置失败
                                    handler.sendMessage(handler.obtainMessage(
                                            Consts.WHAT_AP_SET_FAILED, 0, 0));
                                    break;
                                case JVNetConst.JVN_WIFI_IS_SETTING:// -- 6
                                                                    // 正在配置wifi

                                    break;

                                default:
                                    break;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;

                    default:
                        break;
                }
                break;

        }
    }

    /**
     * 连接wifi 30s超时
     * 
     * @param wifi
     * @param password
     * @return
     */
    public boolean resetWifi(String wifi, String password) {
        boolean flag = wifiAdmin.getWifiState(wifi);
        int errorCount = 0;
        while (!flag) {
            MyLog.v(TAG, "wifi=" + wifi + ";errorCount=" + errorCount
                    + ";flag=" + flag);
            if (errorCount > 30) {
                break;
            }
            if (errorCount == 0 || errorCount == 10 || errorCount == 20) {
                WifiConfiguration desWifi = wifiAdmin.isExsits(wifi);

                if (!"".equalsIgnoreCase(wifi)) {
                    if (null != desWifi) {// 目标Ap已存在wifi列表里，需要移除重新连接，防止连接已存在的一直连接不上
                        wifiAdmin.disconnectWifi(desWifi, true);// 关掉，移除,不移除会记住原来的密码
                        try {
                            Thread.sleep(1 * 1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        desWifi = wifiAdmin.isExsits(wifi);
                    }

                    if (null == desWifi) {
                        int enc = 0;
                        if ("".equalsIgnoreCase(password)) {
                            enc = 1;
                        } else {
                            enc = 3;
                        }
                        desWifi = wifiAdmin.CreateWifiInfo(wifi, password, enc);
                    }
                }

                if (null != desWifi) {
                    wifiAdmin.ConnectWifiByConfig(desWifi);
                }

            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            errorCount += 2;

            flag = wifiAdmin.getWifiState(wifi);
        }

        return flag;
    }

    // 设置三种类型参数分别为String,Integer,String
    class ResetWifiTask extends AsyncTask<String, Integer, Integer> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {

            stopRefreshWifiTimer();
            int resetRes = -1;// 0：成功 1：失败:2：不需要添加设备直接退出
            boolean addFlag = Boolean.valueOf(params[0]);
            boolean changeRes = false;
            // desWifiName;
            // private EditText desWifiPass;
            // if ("".equalsIgnoreCase(desWifiName.getText().toString())) {

            if (oldWifiState) {
                if (!haveSet) {
                    changeRes = wifiAdmin.changeWifi(setIpcName, oldWifiSSID,
                            oldWifiState);
                } else {
                    changeRes = resetWifi(oldWifiSSID, desWifiPWD);
                }
            } else {// 原wifi关着
                if (!haveSet) {// 而且没配置过
                    wifiAdmin.closeWifi();
                    changeRes = true;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    changeRes = resetWifi(oldWifiSSID, desWifiPWD);
                }
            }

            MyLog.v("网络恢复完成", changeRes + "");

            // 网络恢复成功
            if (changeRes) {
                if (addFlag) {

                    if (!ConfigUtil.is3G(JVQuickSettingActivity.this, false)) {
                        hasBroadIP = false;
                        Jni.queryDevice(
                                ConfigUtil.getGroup(ipcDevice.getFullNo()),
                                ConfigUtil.getYST(ipcDevice.getFullNo()),
                                40 * 1000);
                        while (!hasBroadIP) {// 未广播到IP
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            Log.v("未广播到IP---" + hasBroadIP, "休眠1秒");
                        }
                    }
                    int counts = 0;
                    boolean heartState = false;
                    while (!heartState) {
                        if (counts > 20) {
                            break;
                        }
                        int errorCode = 0;
                        if (null != statusHashMap.get(Consts.ACCOUNT_ERROR)) {
                            errorCode = Integer.parseInt(statusHashMap
                                    .get(Consts.ACCOUNT_ERROR));
                        }
                        if (errorCode == Consts.WHAT_ACCOUNT_NORMAL) {// 心跳成功
                            heartState = true;
                        } else {
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        counts++;
                    }
                    // 重新登陆成功 ,或者本地登陆
                    if (heartState || local) {
                        boolean addRes = addDevice(local);
                        if (!addRes) {
                            // handler.sendMessage(handler.obtainMessage(
                            // Consts.QUICK_SETTING_ERROR, 1006, 0));
                            resetRes = 1006;
                        } else {
                            resetRes = 0;
                        }

                    } else {
                        // handler.sendMessage(handler.obtainMessage(
                        // Consts.QUICK_SETTING_ERROR, 1005, 0));
                        resetRes = 1005;
                    }
                } else {
                    resetRes = 2;
                }
            } else {
                resetRes = 1004;
            }

            // if (0 == resetRes && !local && addFlag) {
            // int timeCount = 0;
            // int onlineState = DeviceUtil.getDevOnlineState(ipcDevice
            // .getFullNo());
            //
            // while (0 == onlineState) {// 0 没上线,1 上线
            //
            // if (timeCount > 40) {
            // onlineState = DeviceUtil.getDevOnlineState(ipcDevice
            // .getFullNo());
            // break;
            // } else {
            // onlineState = DeviceUtil.getDevOnlineState(ipcDevice
            // .getFullNo());
            // try {
            // Thread.sleep(5 * 1000);
            // } catch (InterruptedException e) {
            // e.printStackTrace();
            // }
            // timeCount += 5;
            // }
            //
            // }
            //
            // if (0 == onlineState) {
            // resetRes = 1007;
            // }
            // }
            handler.sendMessage(handler.obtainMessage(
                    Consts.WHAT_QUICK_SETTING_DEV_ONLINE, 0, 0));

            return resetRes;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            // 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
            if (isBack) {
                JVQuickSettingActivity.this.finish();
                // if (1004 == result) {
                // showTextToast(R.string.wifi_reset_failed);
                // dismissDialog();
                // MyActivityManager.getActivityManager()
                // .popAllActivityExceptOne(JVLoginActivity.class);
                // Intent intent = new Intent();
                // String userName = statusHashMap.get(Consts.KEY_USERNAME);
                // intent.putExtra("UserName", userName);
                // intent.setClass(JVQuickSettingActivity.this,
                // JVLoginActivity.class);
                // startActivity(intent);
                // JVQuickSettingActivity.this.finish();
                // } else {
                // JVQuickSettingActivity.this.finish();
                // }

            } else {
                if (0 == result) {
                    handler.postDelayed(new Thread() {// 播放，配置完成声音

                                @Override
                                public void run() {
                                    super.run();
                                    playSound(Consts.TAG_SOUNDSEVINE);
                                }

                            }, 200);

                    showSearch(false);
                    // // 设置全屏
                    // JVQuickSettingActivity.this.getWindow().setFlags(
                    // WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    // WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    quickSetDeviceImg.setVisibility(View.VISIBLE);
                } else if (2 == result) {
                    Intent intent = new Intent();
                    setResult(Consts.DEVICE_ADD_SUCCESS_RESULT, intent);
                    JVQuickSettingActivity.this.finish();
                } else {
                    handler.sendMessage(handler.obtainMessage(
                            Consts.WHAT_QUICK_SETTING_ERROR, result, 0));
                }
            }

        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新进度,此方法在主线程执行，用于显示任务执行的进度。
        }
    }

    private boolean addDevice(boolean local) {

        boolean find = false;
        for (Device dev : deviceList) {
            if (dev.getFullNo().equalsIgnoreCase(ipcDevice.getFullNo())) {
                find = true;
                return find;
            }
        }

        String temIp = "";
        if (!Consts.IPC_DEFAULT_IP.equalsIgnoreCase(ipcDevice.getIp())) {
            temIp = ipcDevice.getIp();
        }
        int temPort = ipcDevice.getPort();
        ipcDevice.setDeviceType(2);
        boolean addSucc = false;
        ipcDevice.setUser(Consts.DEFAULT_USERNAME);
        ipcDevice.setPwd(Consts.DEFAULT_PASSWORD);
        if (local) {
            addSucc = true;
        } else {
            ipcDevice.setIp("");
            ipcDevice.setPort(0);
            int addRes = -1;

            ipcDevice = DeviceUtil.addDevice2(ipcDevice,
                    statusHashMap.get(Consts.KEY_USERNAME),
                    ipcDevice.getNickName());
            if (null != ipcDevice) {
                addRes = 0;
                addSucc = true;
            } else {
                addSucc = false;
            }
        }

        if (addSucc) {
            ipcDevice.setIp(temIp);
            ipcDevice.setPort(temPort);
            deviceList.add(0, ipcDevice);
            CacheUtil.saveDevList(deviceList);
        }

        return addSucc;
    }

    /**
     * 保存wifi配置信息
     */
    public void saveWifi() {
        JSONObject newObj = new JSONObject();
        try {
            newObj.put("wifiSsid", desWifiSSID);
            newObj.put("wifiPwd", desWifiPWD);
            newObj.put("nPacketType", JVNetConst.RC_EXTEND);
            newObj.put("packetCount", JVNetConst.RC_EX_NETWORK);
            newObj.put("nType", JVNetConst.EX_WIFI_AP_CONFIG);
            int[] data = wifiAdmin.getWifiAuthEnc(desWifiSSID);
            newObj.put("wifiAuth", data[0]);
            newObj.put("wifiEncryp", data[1]);
            newObj.put("wifiIndex", 0);
            newObj.put("wifiChannel", 0);
            newObj.put("wifiRate", 0);// 0成功，其他失败
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        Jni.setAccessPoint(1, JVNetConst.JVN_RSP_TEXTDATA, newObj.toString());
        MyLog.i("配置wifi请求", newObj.toString());
    }

    /**
     * 保存手动设置wifi配置信息
     */
    public void saveManualWifi(String name, String pass, int auth, int encrypt) {
        JSONObject newObj = new JSONObject();
        try {
            newObj.put("wifiSsid", name);
            newObj.put("wifiPwd", pass);
            newObj.put("nPacketType", JVNetConst.RC_EXTEND);
            newObj.put("packetCount", JVNetConst.RC_EX_NETWORK);
            newObj.put("nType", JVNetConst.EX_WIFI_AP_CONFIG);
            newObj.put("wifiAuth", auth);
            newObj.put("wifiEncryp", encrypt);
            newObj.put("wifiIndex", 0);
            newObj.put("wifiChannel", 0);
            newObj.put("wifiRate", 0);// 0成功，其他失败
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        Jni.setAccessPoint(1, JVNetConst.JVN_RSP_TEXTDATA, newObj.toString());
        MyLog.i("手动配置wifi请求", newObj.toString());
        // Toast.makeText(JVQuickSettingActivity.this, newObj.toString(),
        // Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        BitmapCache.getInstance().clearCache();
    }

    /************************************************ 以下为声波配置所需 ******************************/
    /**
     * 播放声音
     */
    @Override
    public void playSound(int soundType) {
        try {
            // 打开指定音乐文件
            String file = "";
            if (Consts.LANGUAGE_ZH == ConfigUtil
                    .getLanguage2(JVQuickSettingActivity.this)
                    || Consts.LANGUAGE_ZHTW == ConfigUtil
                            .getLanguage2(JVQuickSettingActivity.this)) {
                switch (soundType) {
                    case Consts.TAG_SOUNDONE:// 选择设备，并进入预览
                        file = "quickset_step1_zh.mp3";
                        break;
                    case Consts.TAG_SOUNDTOW:// 正在连接设备
                        file = "quickset_step2_zh.mp3";
                        break;
                    case Consts.TAG_SOUNDTHREE:// 请点击下一步，配置无线网络
                        file = "quickset_step3_zh.mp3";
                        break;
                    case Consts.TAG_SOUNDFOUR:// 请选择无线路由，输入密码，并点击右上角声波配置按钮
                        file = "quickset_step4_zh.mp3";
                        break;
                    case Consts.TAG_SOUNDFIVE:// 请将手机靠近设备
                        file = "quickset_step5_zh.mp3";
                        break;
                    case Consts.TAG_SOUNDSIX:// 叮
                        file = "search_end.mp3";
                        break;
                    case Consts.TAG_SOUNDSEVINE:// 配置完成，请点击图标查看设备
                        file = "quickset_step6_zh.mp3";
                        break;
                    case Consts.TAG_SOUNDEIGHT:// 搜索声音
                        file = "searching.mp3";
                        break;
                    default:
                        break;
                }
            } else if (Consts.LANGUAGE_ZHTW == ConfigUtil
                    .getLanguage2(JVQuickSettingActivity.this)) {
                switch (soundType) {
                    case Consts.TAG_SOUNDONE:// 选择设备，并进入预览
                        file = "quickset_step1_zhtw.mp3";
                        break;
                    case Consts.TAG_SOUNDTOW:// 正在连接设备
                        file = "quickset_step2_zhtw.mp3";
                        break;
                    case Consts.TAG_SOUNDTHREE:// 请点击下一步，配置无线网络
                        file = "quickset_step3_zhtw.mp3";
                        break;
                    case Consts.TAG_SOUNDFOUR:// 请选择无线路由，输入密码，并点击右上角声波配置按钮
                        file = "quickset_step4_zhtw.mp3";
                        break;
                    case Consts.TAG_SOUNDFIVE:// 请将手机靠近设备
                        file = "quickset_step5_zhtw.mp3";
                        break;
                    case Consts.TAG_SOUNDSIX:// 叮
                        file = "search_end.mp3";
                        break;
                    case Consts.TAG_SOUNDSEVINE:// 配置完成，请点击图标查看设备
                        file = "quickset_step6_zhtw.mp3";
                        break;
                    case Consts.TAG_SOUNDEIGHT:// 搜索声音
                        file = "searching.mp3";
                        break;
                    default:
                        break;
                }
            } else {
                switch (soundType) {
                    case Consts.TAG_SOUNDONE:// 选择设备，并进入预览
                        file = "quickset_step1_en.mp3";
                        break;
                    case Consts.TAG_SOUNDTOW:// 正在连接设备
                        file = "quickset_step2_en.mp3";
                        break;
                    case Consts.TAG_SOUNDTHREE:// 请点击下一步，配置无线网络
                        file = "quickset_step3_en.mp3";
                        break;
                    case Consts.TAG_SOUNDFOUR:// 请选择无线路由，输入密码，并点击右上角声波配置按钮
                        file = "quickset_step4_en.mp3";
                        break;
                    case Consts.TAG_SOUNDFIVE:// 请将手机靠近设备
                        file = "quickset_step5_en.mp3";
                        break;
                    case Consts.TAG_SOUNDSIX:// 叮
                        file = "search_end.mp3";
                        break;
                    case Consts.TAG_SOUNDSEVINE:// 配置完成，请点击图标查看设备
                        file = "quickset_step6_en.mp3";
                        break;
                    case Consts.TAG_SOUNDEIGHT:// 搜索声音
                        file = "searching.mp3";
                        break;
                    default:
                        break;
                }
            }

            int curVolume = 20; // 当前音量值

            AudioManager audioMgr = null; // Audio管理器，用了控制音量
            audioMgr = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            // 初始化音量大概为最大音量的1/2
            curVolume = audioMgr.getStreamVolume(AudioManager.STREAM_MUSIC);
            audioMgr.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume,
                    AudioManager.FLAG_PLAY_SOUND);
            AssetManager assetMgr = this.getAssets();
            // 资源管理器
            AssetFileDescriptor afd = assetMgr.openFd(file);

            if (null == myPlayer) {
                myPlayer = new MediaPlayer();
            }
            myPlayer.reset();

            // 使用myPlayer.myPlayer加载指定的声音文件。
            myPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            // 准备声音
            myPlayer.prepare();
            // 播放
            myPlayer.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示
     */
    private void showQuickPopWindow() {
        if (null == quickSetPop) {
            initPopwindow();
        }
        quickSetPop.showAtLocation(quickSetParentLayout, Gravity.CENTER, 0, 0);
    }

    /**
     * 关闭对话框
     */
    private void dismisQuickPopWindow() {
        if (null != quickSetPop && quickSetPop.isShowing()) {
            quickSetPop.dismiss();
            quickSetPop = null;
            if (null != myPlayer) {
                myPlayer.stop();
                myPlayer.release();
                myPlayer = null;
            }
        }
    }

    /**
     * 配置设备弹出框单机事件
     */
    OnClickListener quickSetOnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.quickSetBack:// 返回
                    // 暂停扫瞄器
                    showSearch(false);
                    searchView.stopPlayer();
                    if (null != myPlayer) {
                        myPlayer.stop();
                        myPlayer.release();
                        myPlayer = null;
                    }
                    dismisQuickPopWindow();
                    isBack = true;
                    createDialog(R.string.quick_setting_exiting, false);
                    ResetWifiTask task = new ResetWifiTask();
                    String[] params = new String[3];
                    params[0] = "false";
                    task.execute(params);
                    break;
                case R.id.quickSetDeviceImg:
                    Intent intent = new Intent();
                    setResult(Consts.DEVICE_ADD_SUCCESS_RESULT, intent);
                    JVQuickSettingActivity.this.finish();
                    break;
            }
        }
    };

    /**
     * 弹出配置搜索对话框
     */
    private void initPopwindow() {
        try {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            View view = layoutInflater
                    .inflate(R.layout.quicksetsearching, null);
            Display display = getWindowManager().getDefaultDisplay();
            int screenWidth = display.getWidth();
            int screenHeight = display.getHeight();
            quickSetPop = new PopupWindow(view, screenWidth, screenHeight);

            /**
             * 为控件添加事件
             */
            quickSetBackImg = (Button) view.findViewById(R.id.quickSetBack);
            quickSetBackImg.setOnClickListener(quickSetOnClick);

            quickSetDeviceImg = (ImageView) view
                    .findViewById(R.id.quickSetDeviceImg);
            quickSetDeviceImg.setOnClickListener(quickSetOnClick);
            searchView = (SearchDevicesView) view.findViewById(R.id.searchView);
            searchView.setWillNotDraw(false);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void backDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                JVQuickSettingActivity.this);
        builder.setCancelable(false);

        // builder.setTitle(getResources().getString(
        // R.string));
        builder.setMessage(getResources().getString(R.string.pressback_exit));

        builder.setPositiveButton(R.string.str_quick_setting_waiting,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                });
        builder.setNegativeButton(R.string.exit,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 暂停扫瞄器
                        showSearch(false);
                        searchView.stopPlayer();
                        if (null != myPlayer) {
                            myPlayer.stop();
                            myPlayer.release();
                            myPlayer = null;
                        }
                        dismisQuickPopWindow();
                        isBack = true;
                        createDialog(R.string.quick_setting_exiting, false);
                        ResetWifiTask task = new ResetWifiTask();
                        String[] params = new String[3];
                        params[0] = "false";
                        task.execute(params);
                    }

                });

        builder.show();
    }

    // 配制出错，错误dialog
    public void errorDialog(final int errorCode) {
        if (null != quickSetDeviceImg) {
            quickSetDeviceImg.setVisibility(View.GONE);
        }
        // 断开连接
        manuDiscon = true;
        if (1000 != errorCode && 1001 != errorCode) {
            // PlayUtil.disconnectDevice();
            disConnectVideo();
        }

        if (null != errorDialog && errorDialog.isShowing()) {

        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(
                    JVQuickSettingActivity.this);
            builder.setCancelable(false);

            builder.setTitle(getResources().getString(R.string.tips));
            String errorMsg = "";
            switch (errorCode) {
            // case 1000: {
            // errorMsg = getResources().getString(R.string.set_error_1000);
            // break;
            // }
                case 1001: {
                    errorMsg = getResources().getString(R.string.set_error_1001);
                    break;
                }
                case 1002: {
                    errorMsg = getResources().getString(R.string.set_error_1002);
                    break;
                }
                case 1003: {
                    errorMsg = getResources().getString(R.string.set_error_1003);
                    break;
                }
                case 1004: {
                    errorMsg = getResources().getString(R.string.set_error_1004);
                    break;
                }
                case 1005: {
                    errorMsg = getResources().getString(R.string.set_error_1005);
                    break;
                }
                case 1006: {
                    errorMsg = getResources().getString(R.string.set_error_1006);
                    break;
                }
                case 1007: {
                    errorMsg = getResources().getString(R.string.set_error_1007);
                    break;
                }

            }
            builder.setMessage(errorMsg);
            // builder.setMessage(getResources().getString(R.string.set_error)
            // + errorCode);

            // builder.setPositiveButton(R.string.try_again,
            // new DialogInterface.OnClickListener() {
            // @Override
            // public void onClick(DialogInterface dialog, int which) {
            // // 暂停扫瞄器
            // showSearch(false);
            // if (null != searchView.myPlayer) {
            // searchView.myPlayer.stop();
            // }
            // dismisQuickPopWindow();
            // showIpcLayout(false);
            // }
            //
            // });
            builder.setNegativeButton(R.string.exit,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            // if (1004 == errorCode || 1005 == errorCode) {
                            // MyActivityManager.getActivityManager()
                            // .popAllActivityExceptOne(
                            // JVLoginActivity.class);
                            // Intent intent = new Intent();
                            // String userName =
                            // JVQuickSettingActivity.this.statusHashMap
                            // .get(Consts.KEY_USERNAME);
                            // intent.putExtra("UserName", userName);
                            // intent.setClass(JVQuickSettingActivity.this,
                            // JVLoginActivity.class);
                            // JVQuickSettingActivity.this
                            // .startActivity(intent);
                            // JVQuickSettingActivity.this.finish();
                            // } else {
                            // 暂停扫瞄器
                            showSearch(false);
                            if (null != myPlayer) {
                                myPlayer.stop();
                            }
                            dismisQuickPopWindow();
                            isBack = true;
                            createDialog(R.string.quick_setting_exiting, false);
                            ResetWifiTask task = new ResetWifiTask();
                            String[] params = new String[3];
                            params[0] = "false";
                            task.execute(params);
                            // }

                        }

                    });

            if (!JVQuickSettingActivity.this.isFinishing()) {
                errorDialog = builder.create();
                errorDialog.show();
            }
        }

    }

    /**
     * 显示雷达扫描
     * 
     * @param show
     */
    private void showSearch(boolean show) {
        if (null != searchView) {
            if (show) {
                isSearching = true;
                // 开始声波配置，弹出配置对话框
                searchView.setSearching(true);
                // // 设置全屏
                // JVQuickSettingActivity.this.getWindow().setFlags(
                // WindowManager.LayoutParams.FLAG_FULLSCREEN,
                // WindowManager.LayoutParams.FLAG_FULLSCREEN);
                quickSetDeviceImg.setVisibility(View.GONE);// 弹出设备
                quickSetBackImg.setVisibility(View.VISIBLE);
            } else {
                searchView.setSearching(false);
                // getWindow().setFlags(
                // disMetrics.widthPixels
                // - getStatusHeight(JVQuickSettingActivity.this),
                // WindowManager.LayoutParams.FLAG_FULLSCREEN);
                isSearching = false;
                if (null != myPlayer) {
                    myPlayer.stop();
                    myPlayer.release();
                    myPlayer = null;
                }
                quickSetBackImg.setVisibility(View.GONE);
            }
        }

    }

    /**
     * 设备升级Dialog
     * 
     * @param tag
     */
    private void updateDialog1() {
        manuDiscon = true;
        // PlayUtil.disconnectDevice();
        disConnectVideo();
        AlertDialog.Builder builder = new AlertDialog.Builder(
                JVQuickSettingActivity.this);
        builder.setCancelable(false);

        builder.setTitle(getResources().getString(
                R.string.str_quick_setting_new_dev));
        builder.setMessage(getResources().getString(
                R.string.str_quick_setting_new_dev_tips));

        builder.setPositiveButton(R.string.str_check_now,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        helpLayout.setVisibility(View.VISIBLE);
                        setHelpBackground(true);
                        rightBtn.setVisibility(View.GONE);
                        currentMenu
                                .setText(R.string.str_quick_setting_devupdate_order);
                        // 暂停扫瞄器
                        showSearch(false);
                        if (null != searchView && null != myPlayer) {
                            myPlayer.stop();
                        }

                        dismisQuickPopWindow();

                    }

                });
        builder.setNegativeButton(R.string.login_str_close,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 暂停扫瞄器
                        showSearch(false);
                        if (null != searchView && null != myPlayer) {
                            myPlayer.stop();
                        }

                        dismisQuickPopWindow();

                        backMethod();
                        dialog.dismiss();
                    }

                });

        builder.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BitmapCache.getInstance().clearCache();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public void disConnectVideo() {

        if (PlayUtil.disconnectDevice()) {
            createDialog("", true);
            while (!disConnected) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            dismissDialog();
        }

    }
}
