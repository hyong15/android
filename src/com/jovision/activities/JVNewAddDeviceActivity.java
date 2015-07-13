
package com.jovision.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Build;
import android.test.JVACCOUNT;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.MainApplication;
import com.jovision.bean.Device;
import com.jovision.commons.MyLog;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.DeviceUtil;
import com.jovision.utils.PlayUtil;
import com.tencent.stat.StatService;

import org.json.JSONObject;

import java.util.ArrayList;

@SuppressLint("SetJavaScriptEnabled")
public class JVNewAddDeviceActivity extends ShakeActivity {
    private static final String TAG = "JVNewAddDeviceActivity";

    /** add device layout */
    private EditText devNumET;
    private ImageButton editimg_clearn;
    private ImageView tab_erweima_icon;
    private Button save_icon;
    private LinearLayout soundwave_button, apset_button;// 声波配置按钮，AP配置按钮
    private LinearLayout devsetLayout;
    private LinearLayout foreign_apset_button;// 国外添加设备界面
    private TextView tab_erweima_title;
    private LinearLayout ipDnsBtn, localNetworkBtn;
    // private WebView addDeviceWebView;
    private TextView subject_detail;
    // private String url = "http://test.cloudsee.net/mobile/";
    private String url = "";
    private Boolean isLoadUrlfail = false;
    private LinearLayout loadinglayout;
    private ImageView loadingBar;
    private View line_view;// 分割线

    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private Device addDevice;
    private boolean hasBroadIP = false;// 是否广播完IP
    private int onLine = 0;
    private String ip = "";
    private int port = 0;
    private int channelCount = -1;
    public int broadTag = 0;
    private ArrayList<Device> broadList = new ArrayList<Device>();// 广播到的设备列表

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
        // 广播回调
            case Consts.CALL_LAN_SEARCH: {
                JSONObject broadObj;
                try {
                    broadObj = new JSONObject(obj.toString());
                    if (0 == broadObj.optInt("timeout")) {
                        String broadDevNum = broadObj.optString("gid")
                                + broadObj.optInt("no");
                        int netmod = broadObj.optInt("netmod");
                        if (broadDevNum.equalsIgnoreCase(devNumET.getText()
                                .toString())) {// 同一个设备
                            // addDevice.setOnlineState(1);
                            // addDevice.setIp(broadObj.optString("ip"));
                            // addDevice.setPort(broadObj.optInt("port"));
                            onLine = 1;
                            ip = broadObj.optString("ip");
                            port = broadObj.optInt("port");
                            channelCount = broadObj.optInt("count");
                        }

                    } else if (1 == broadObj.optInt("timeout")) {
                        hasBroadIP = true;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {
        deviceList = CacheUtil.getDevList();
    }

    @Override
    protected void initUi() {
        super.initUi();
        setContentView(R.layout.new_adddevice_layout);
        if (null != statusHashMap.get(Consts.MORE_ADDDEVICEURL)) {
            url = statusHashMap.get(Consts.MORE_ADDDEVICEURL);
            MyLog.v(TAG, "addUrl=" + url);
            // url = "http://www.cloudsee.net/UI/mobile/devicetypelist.html";
        } else {// 还没有获取过添加设备的url
        }

        // 拼接sid appv等字段
        if (null != url && !"".equalsIgnoreCase(url)) {
            url += getJoinUrl();
        }

        /** top bar */
        leftBtn = (Button) findViewById(R.id.btn_left);
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        rightBtn = (Button) findViewById(R.id.btn_right);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        currentMenu.setText(R.string.str_new_add_device);
        rightBtn.setVisibility(View.GONE);
        leftBtn.setOnClickListener(myOnClickListener);

        /** add device layout */
        devNumET = (EditText) findViewById(R.id.new_adddevice_et);
        editimg_clearn = (ImageButton) findViewById(R.id.editimg_clearn);
        tab_erweima_title = (TextView) findViewById(R.id.tab_erweima_title);
        tab_erweima_icon = (ImageView) findViewById(R.id.tab_erweima_icon);
        save_icon = (Button) findViewById(R.id.save_icon);
        loadingBar = (ImageView) findViewById(R.id.loadingbars);
        loadinglayout = (LinearLayout) findViewById(R.id.loadinglayout);
        // addDeviceWebView = (WebView) findViewById(R.id.add_device_wv);
        devsetLayout = (LinearLayout) findViewById(R.id.devsetlayout);
        apset_button = (LinearLayout) findViewById(R.id.apset_button);
        foreign_apset_button = (LinearLayout) findViewById(R.id.foreign_apset_button);// 国外添加设备
        soundwave_button = (LinearLayout) findViewById(R.id.soundwave_button);
        ipDnsBtn = (LinearLayout) findViewById(R.id.ip_dns_btn);
        localNetworkBtn = (LinearLayout) findViewById(R.id.local_network_button);
        line_view = (View) findViewById(R.id.line_view);// 设备分割线

        tab_erweima_icon.setOnClickListener(myOnClickListener);
        editimg_clearn.setOnClickListener(myOnClickListener);
        save_icon.setOnClickListener(myOnClickListener);
        devNumET.addTextChangedListener(new TextWatcherImpl());
        devNumET.setOnFocusChangeListener(new FocusChangeListenerImpl());

        // if (ConfigUtil.isConnected(JVNewAddDeviceActivity.this)) {// 已联网
        // if (null == url || "".equalsIgnoreCase(url)) {
        // addDeviceWebView.setVisibility(View.GONE);
        // devsetLayout.setVisibility(View.VISIBLE);
        // } else {
        // loadinglayout.setVisibility(View.VISIBLE);
        // addDeviceWebView.loadUrl(url);
        // addDeviceWebView.setVisibility(View.VISIBLE);
        // devsetLayout.setVisibility(View.GONE);
        // }
        // } else {
        // addDeviceWebView.setVisibility(View.GONE);
        // devsetLayout.setVisibility(View.VISIBLE);
        // }
        // 无论是否联网都显示按钮不用webview、nihy
        // addDeviceWebView.setVisibility(View.GONE);
        // if (ConfigUtil.getServerLanguage() != Consts.LANGUAGE_ZH) {//
        // 国外版本只显示显示无线配置
        // foreign_apset_button.setVisibility(View.VISIBLE);
        // devsetLayout.setVisibility(View.GONE);
        // } else {// 国内
        foreign_apset_button.setVisibility(View.GONE);
        devsetLayout.setVisibility(View.VISIBLE);
        // }
        // addDeviceWebView.getSettings().setJavaScriptEnabled(true);
        // addDeviceWebView.getSettings().setDomStorageEnabled(true);
        // addDeviceWebView.requestFocus(View.FOCUS_DOWN);
        // addDeviceWebView.setWebViewClient(myWebviewClient);
        apset_button.setOnClickListener(myOnClickListener);
        soundwave_button.setOnClickListener(myOnClickListener);
        foreign_apset_button.setOnClickListener(myOnClickListener);
        if (Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN))) {
            ipDnsBtn.setVisibility(View.VISIBLE);
            ipDnsBtn.setOnClickListener(myOnClickListener);
        } else {// 分割线和IP配置隐藏
            line_view.setVisibility(View.GONE);
            ipDnsBtn.setVisibility(View.GONE);
        }
        localNetworkBtn.setOnClickListener(myOnClickListener);

    }

    /** webview的操作 */
    // WebViewClient myWebviewClient = new WebViewClient() {
    //
    // @Override
    // public void onPageStarted(WebView view, String url, Bitmap favicon) {
    // // MyLog.e("添加设备", "页面开始加载");
    // if (!MySharedPreference.getBoolean("webfirst")) {
    // MySharedPreference.putBoolean("webfirst", true);
    // loadinglayout.setVisibility(View.VISIBLE);
    // loadingBar.setAnimation(AnimationUtils.loadAnimation(
    // JVNewAddDeviceActivity.this, R.anim.rotate));
    // addDeviceWebView.setVisibility(View.GONE);
    // }
    // super.onPageStarted(view, url, favicon);
    // }
    //
    // @Override
    // public void onReceivedError(WebView view, int errorCode, String
    // description,
    // String failingUrl) {
    // // MyLog.e("添加设备", "页面加载失败");
    // isLoadUrlfail = true;
    // loadinglayout.setVisibility(View.GONE);
    // soundwave_button.setVisibility(View.VISIBLE);
    // apset_button.setVisibility(View.VISIBLE);
    // devsetLayout.setVisibility(View.VISIBLE);
    // addDeviceWebView.setVisibility(View.GONE);
    // JVNewAddDeviceActivity.this.statusHashMap.put(Consts.HAS_LOAD_DEMO,
    // "false");
    // super.onReceivedError(view, errorCode, description, failingUrl);
    // }
    //
    // @Override
    // public void onPageFinished(WebView view, String url) {
    // // TODO 自动生成的方法存根
    // MyLog.e("添加设备", "页面开始完成");
    //
    // if (null == url || "".equalsIgnoreCase(url)) {
    // // MyLog.e("添加设备", "页面开始完成失败");
    // loadinglayout.setVisibility(View.GONE);
    // soundwave_button.setVisibility(View.VISIBLE);
    // apset_button.setVisibility(View.VISIBLE);
    // devsetLayout.setVisibility(View.VISIBLE);
    // addDeviceWebView.setVisibility(View.GONE);
    // } else {
    // if (isLoadUrlfail) {// 加载失败或者url为空
    // // MyLog.e("添加设备", "页面开始完成失败");
    // loadinglayout.setVisibility(View.GONE);
    // soundwave_button.setVisibility(View.VISIBLE);
    // apset_button.setVisibility(View.VISIBLE);
    // devsetLayout.setVisibility(View.VISIBLE);
    // addDeviceWebView.setVisibility(View.GONE);
    // } else {
    // loadinglayout.setVisibility(View.GONE);
    // devsetLayout.setVisibility(View.GONE);
    // addDeviceWebView.setVisibility(View.VISIBLE);
    // }
    // }
    // super.onPageFinished(view, url);
    // }
    //
    // @Override
    // public boolean shouldOverrideUrlLoading(WebView view, String newurl) {
    //
    // if (!newurl.contains("addmode")) {// 不含添加设备类型字段，打开一个新网页
    // if (newurl.contains("open")) {// 打开新的WebView模式
    // Intent intentAD2 = new Intent(JVNewAddDeviceActivity.this,
    // JVWebViewActivity.class);
    // intentAD2.putExtra("URL", newurl);
    // intentAD2.putExtra("title", -2);
    // JVNewAddDeviceActivity.this.startActivity(intentAD2);
    // } else if (newurl.contains("close")) {// 关闭当前webview
    // JVNewAddDeviceActivity.this.finish();
    // } else {
    // addDeviceWebView.loadUrl(newurl);
    // }
    // return true;
    // }
    //
    // String param_array[] = newurl.split("\\?");
    // HashMap<String, String> resMap;
    // resMap = ConfigUtil.genMsgMapFromhpget(param_array[1]);
    // String addmode = resMap.get("addmode");
    // if (null != addmode) {
    // int devType = Integer.parseInt(addmode);
    //
    // switch (devType) {
    // case Consts.NET_DEVICE_TYPE_YST_NUMBER: {// 云视通号添加
    // StatService.trackCustomEvent(JVNewAddDeviceActivity.this,
    // "Add by CloudSEE ID", JVNewAddDeviceActivity.this.getResources()
    // .getString(R.string.census_addcloudseeid));
    // Intent addIntent = new Intent();
    // addIntent.setClass(JVNewAddDeviceActivity.this,
    // JVAddDeviceActivity.class);
    // addIntent.putExtra("QR", false);
    // JVNewAddDeviceActivity.this.startActivityForResult(addIntent,
    // Consts.DEVICE_ADD_REQUEST);
    // break;
    // }
    // case Consts.NET_DEVICE_TYPE_SOUND_WAVE: {// 声波配置添加
    // StatService.trackCustomEvent(
    // JVNewAddDeviceActivity.this,
    // "SoundWave",
    // JVNewAddDeviceActivity.this.getResources().getString(
    // R.string.census_soundwave));
    // Intent intent = new Intent();
    // intent.setClass(JVNewAddDeviceActivity.this, JVWaveSetActivity.class);
    // JVNewAddDeviceActivity.this.startActivityForResult(intent,
    // Consts.DEVICE_ADD_REQUEST);
    // break;
    // }
    // case Consts.NET_DEVICE_TYPE_AP_SET: {// AP设置添加
    // StatService.trackCustomEvent(
    // JVNewAddDeviceActivity.this,
    // "Add Wi_Fi Device",
    // JVNewAddDeviceActivity.this.getResources().getString(
    // R.string.census_addwifidev));
    // JVNewAddDeviceActivity.this.startSearch(false,
    // Consts.DEVICE_ADD_REQUEST);
    //
    // break;
    // }
    // }
    // }
    //
    // return true;
    //
    // }
    //
    // };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Consts.DEVICE_ADD_REQUEST) {
            if (resultCode == Consts.DEVICE_ADD_SUCCESS_RESULT) {// 设备添加成功
                JVNewAddDeviceActivity.this.finish();
            }
        }
    }

    OnClickListener myOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left:
                    backMethod();
                    break;
                case R.id.tab_erweima_icon:
                    StatService.trackCustomEvent(
                            JVNewAddDeviceActivity.this,
                            "Scan QR Code",
                            JVNewAddDeviceActivity.this.getResources().getString(
                                    R.string.census_scanqrcod));
                    Intent addIntent = new Intent();
                    addIntent.setClass(JVNewAddDeviceActivity.this,
                            JVAddDeviceActivity.class);
                    addIntent.putExtra("QR", true);
                    JVNewAddDeviceActivity.this.startActivityForResult(addIntent,
                            Consts.DEVICE_ADD_REQUEST);

                    break;
                case R.id.editimg_clearn:
                    devNumET.setText("");
                    break;
                case R.id.save_icon:
                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(JVNewAddDeviceActivity.this
                                    .getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    Boolean checkResult = checkYstNum(
                            devNumET.getText().toString(), Consts.DEFAULT_USERNAME,
                            Consts.DEFAULT_PASSWORD, devNumET.getText().toString());
                    if (checkResult) {
                        Intent addDeviceIntent = new Intent();
                        addDeviceIntent.setClass(JVNewAddDeviceActivity.this,
                                JVAddDeviceActivity.class);
                        addDeviceIntent.putExtra("devNumET_cloudseeid", devNumET
                                .getText().toString());
                        JVNewAddDeviceActivity.this.startActivityForResult(
                                addDeviceIntent, Consts.DEVICE_ADD_REQUEST);
                        return;
                    }
                    break;
                case R.id.refreshimg:
                    if (ConfigUtil.isConnected(JVNewAddDeviceActivity.this)) {
                        MyLog.e("添加设备", "判断手机是否联网成功");
                        JVNewAddDeviceActivity.this.statusHashMap.put(
                                Consts.HAS_LOAD_DEMO, "false");
                        loadinglayout.setVisibility(View.VISIBLE);
                        loadingBar.setAnimation(AnimationUtils.loadAnimation(
                                JVNewAddDeviceActivity.this, R.anim.rotate));
                        isLoadUrlfail = false;
                        if (null == url || "".equalsIgnoreCase(url)) {
                            if ("false"
                                    .equals(JVNewAddDeviceActivity.this.statusHashMap
                                            .get(Consts.KEY_INIT_ACCOUNT_SDK))) {
                                MyLog.e("Login", "初始化账号SDK失败");
                                ConfigUtil
                                        .initAccountSDK(((MainApplication) JVNewAddDeviceActivity.this
                                                .getApplication()));// 初始化账号SDK
                            }
                        }
                        // addDeviceWebView.loadUrl(url);
                    } else {
                        MyLog.e("添加设备", "判断手机是否联网失败");
                        JVNewAddDeviceActivity.this.alertNetDialog();
                    }
                    break;
                case R.id.apset_button:
                    StatService.trackCustomEvent(
                            JVNewAddDeviceActivity.this,
                            "Add Wi_Fi Device",
                            JVNewAddDeviceActivity.this.getResources().getString(
                                    R.string.census_addwifidev));
                    JVNewAddDeviceActivity.this.startSearch(false,
                            Consts.DEVICE_ADD_REQUEST);
                    break;
                case R.id.foreign_apset_button:// 国外添加设备
                    StatService.trackCustomEvent(
                            JVNewAddDeviceActivity.this,
                            "Add Wi_Fi Device",
                            JVNewAddDeviceActivity.this.getResources().getString(
                                    R.string.census_addwifidev));
                    JVNewAddDeviceActivity.this.startSearch(false,
                            Consts.DEVICE_ADD_REQUEST);
                    break;
                case R.id.soundwave_button:
                    StatService.trackCustomEvent(JVNewAddDeviceActivity.this,
                            "SoundWave", JVNewAddDeviceActivity.this.getResources()
                                    .getString(R.string.census_soundwave));
                    Intent intent = new Intent();
                    intent.setClass(JVNewAddDeviceActivity.this,
                            JVWaveSetActivity.class);
                    JVNewAddDeviceActivity.this.startActivityForResult(intent,
                            Consts.DEVICE_ADD_REQUEST);
                    break;
                case R.id.ip_dns_btn:
                    StatService.trackCustomEvent(JVNewAddDeviceActivity.this,
                            "IP/DNS", JVNewAddDeviceActivity.this.getResources()
                                    .getString(R.string.census_ipdns));
                    Intent intent_ip = new Intent();
                    intent_ip.setClass(JVNewAddDeviceActivity.this,
                            JVAddIpDeviceActivity.class);
                    JVNewAddDeviceActivity.this.startActivityForResult(intent_ip,
                            Consts.DEVICE_ADD_REQUEST);
                    break;
                case R.id.local_network_button:
                    Intent intentScan = new Intent();
                    setResult(Consts.SCAN_IN_LINE_RESULT, intentScan);
                    JVNewAddDeviceActivity.this.finish();
                    break;
            }
        }
    };

    @Override
    protected void saveSettings() {

    }

    @Override
    protected void freeMe() {

    }

    private class FocusChangeListenerImpl implements OnFocusChangeListener {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (devNumET.getText().toString().length() >= 1) {
                editimg_clearn.setVisibility(View.VISIBLE);
                save_icon.setVisibility(View.VISIBLE);
            } else {
                editimg_clearn.setVisibility(View.GONE);
                save_icon.setVisibility(View.GONE);
            }
        }

    }

    // 当输入结束后判断是否显示右边clean的图标
    private class TextWatcherImpl implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {
            if (devNumET.getText().toString().length() >= 1) {
                editimg_clearn.setVisibility(View.VISIBLE);
                save_icon.setVisibility(View.VISIBLE);
                tab_erweima_title.setVisibility(View.GONE);
                tab_erweima_icon.setVisibility(View.GONE);
            } else {
                editimg_clearn.setVisibility(View.GONE);
                save_icon.setVisibility(View.GONE);
                tab_erweima_icon.setVisibility(View.VISIBLE);
                tab_erweima_title.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {

        }

    }

    /**
     * 保存设备信息
     * 
     * @param devNum
     * @param userName
     * @param userPwd
     */
    public Boolean checkYstNum(String devNum, String userName, String userPwd,
            String nickName) {
        boolean islegal = true;
        if (null == deviceList) {
            deviceList = new ArrayList<Device>();
        }
        int size = deviceList.size();
        if ("".equalsIgnoreCase(devNum)) {// 云视通号不可为空
            showTextToast(R.string.login_str_device_ytnum_notnull);
            islegal = false;
        } else if (!ConfigUtil.checkYSTNum(devNum)) {// 验证云视通号是否合法
            showTextToast(R.string.increct_yst_tips);
            islegal = false;
        } else if (size >= 100
                && !Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN))) {// 非本地多于100个设备不让再添加
            showTextToast(R.string.str_device_most_count);
            islegal = false;
        } else {
            // 判断一下是否已存在列表中
            boolean find = false;
            if (null != deviceList && 0 != deviceList.size()) {
                for (Device dev : deviceList) {
                    if (devNum.equalsIgnoreCase(dev.getFullNo())) {
                        find = true;
                        break;
                    }
                }
            }

            if (find) {
                devNumET.setText("");
                showTextToast(R.string.str_device_exsit);
                islegal = false;
            }
        }

        return islegal;
    }

    // 设置三种类型参数分别为String,Integer,String
    class AddDevTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {

            String nickName = params[3];
            int addRes = -1;
            boolean localFlag = Boolean.valueOf(statusHashMap
                    .get(Consts.LOCAL_LOGIN));
            try {
                MyLog.e(TAG, "getChannelCount E = ");

                // 非3G广播获取通道数量
                if (PlayUtil.broadCast(JVNewAddDeviceActivity.this)) {
                    int errorCount = 0;
                    while (!hasBroadIP) {
                        Thread.sleep(1000);
                        errorCount++;
                        if (errorCount >= 10) {
                            break;
                        }
                    }
                }

                MyLog.e(TAG, "getChannelCount C1 = " + channelCount);
                if (channelCount <= 0) {
                    channelCount = Jni.getChannelCount(params[0],
                            Integer.parseInt(params[1]),
                            Integer.parseInt(params[2]));
                }

                MyLog.e(TAG, "getChannelCount C2 = " + channelCount);
                if (channelCount <= 0) {
                    channelCount = 4;
                }

                addDevice = new Device("", 0, params[0],
                        Integer.parseInt(params[1]), Consts.DEFAULT_USERNAME,
                        Consts.DEFAULT_PASSWORD, false, channelCount, 0,
                        nickName);
                // MyLog.v(TAG, "dev = " + addDev.toString());
                if (null != addDevice) {
                    if (localFlag) {// 本地添加
                        addRes = 0;
                        if (!"".equals(nickName)) {
                            addDevice.setNickName(nickName);
                        }
                    } else {
                        addDevice = DeviceUtil.addDevice2(addDevice,
                                statusHashMap.get(Consts.KEY_USERNAME),
                                nickName);
                        if (null != addDevice) {
                            addRes = 0;
                        }

                    }
                }
                MyLog.e(TAG, "addRes X = " + addRes);
                if (0 == addRes) {
                    addDevice.setOnlineStateLan(onLine);
                    addDevice.setIp(ip);
                    addDevice.setPort(port);
                    deviceList.add(0, addDevice);
                    CacheUtil.saveDevList(deviceList);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return addRes;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            // 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
            dismissDialog();
            if (0 == result) {
                showTextToast(R.string.add_device_succ);
                JVNewAddDeviceActivity.this.finish();

            } else {
                showTextToast(R.string.add_device_failed);
            }
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
            createDialog("", true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新进度,此方法在主线程执行，用于显示任务执行的进度。
        }
    }

    public void backMethod() {
        CacheUtil.saveDevList(deviceList);
        // addDeviceWebView.clearCache(true);
        this.finish();
    }

    @Override
    public void onBackPressed() {
        backMethod();
        super.onBackPressed();
    }

    public String getJoinUrl() {
        String joinUlr = "";
        String appVersion = "";
        try {
            appVersion = JVNewAddDeviceActivity.this.getPackageManager()
                    .getPackageInfo(
                            JVNewAddDeviceActivity.this.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        String lan = "";
        if (Consts.LANGUAGE_ZH == ConfigUtil
                .getLanguage2(JVNewAddDeviceActivity.this)) {
            lan = "zh_cn";
        } else if (Consts.LANGUAGE_ZHTW == ConfigUtil
                .getLanguage2(JVNewAddDeviceActivity.this)) {
            lan = "zh_tw";
        } else {
            lan = "en_us";
        }

        String sid = "";
        if (!Boolean.valueOf(JVNewAddDeviceActivity.this.statusHashMap
                .get(Consts.LOCAL_LOGIN))) {// 在线
            sid = JVACCOUNT.GetSession();
        } else {
            sid = "";
        }

        joinUlr = "?" + "plat=android&platv=" + Build.VERSION.SDK_INT
                + "&lang=" + lan + "&appv=" + appVersion + "&sid=" + sid;
        MyLog.v(TAG, "addDevUrl=" + url);
        return joinUlr;
    }

}
