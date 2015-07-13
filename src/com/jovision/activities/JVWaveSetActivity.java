
package com.jovision.activities;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.support.v4.view.ViewPager.LayoutParams;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.adapters.WaveDevlListAdapter;
import com.jovision.bean.Device;
import com.jovision.bean.WifiAdmin;
import com.jovision.commons.MyAudio;
import com.jovision.commons.MyLog;
import com.jovision.utils.BitmapCache;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.DeviceUtil;
import com.jovision.utils.PlayUtil;
import com.jovision.views.ProgressWheel;
import com.mediatek.elian.ElianNative;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JVWaveSetActivity extends BaseActivity {

    private static final String TAG = "JVWaveSetActivity";

    String[] stepSoundZH = {
            "voice_next_zh.mp3", "voice_info_zh.mp3",
            "voice_send_zh.mp3", "searching.mp3", "search_end.mp3",
            "wave_show_zh.mp3"
    };

    String[] stepSoundZHTW = {
            "voice_next_zhtw.mp3", "voice_info_zhtw.mp3",
            "voice_send_zhtw.mp3", "searching.mp3", "search_end.mp3",
            "wave_show_zhtw.mp3"
    };

    String[] stepSoundEN = {
            "voice_next_en.mp3", "voice_info_en.mp3",
            "voice_send_en.mp3", "searching.mp3", "search_end.mp3",
            "wave_show_en.mp3"
    };
    int[] titleID = {
            R.string.prepare_step, R.string.prepare_set,
            R.string.wave_set, R.string.show_demo, R.string.search_list,
            R.string.str_quick_setting_wifibymanul
    };
    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private ArrayList<Device> broadList = new ArrayList<Device>();

    protected WifiAdmin wifiAdmin;
    protected String oldWifiSSID = "";

    // 最大音量
    int maxVolume = 0;
    // 当前音量
    int currentVolume = 0;

    /** topBar */
    protected LinearLayout topBar;
    protected RelativeLayout stepLayout1;
    protected RelativeLayout stepLayout2;
    protected RelativeLayout stepLayout3;
    protected RelativeLayout stepLayout4;
    protected RelativeLayout stepLayout5;
    protected RelativeLayout stepLayout6;
    protected RelativeLayout.LayoutParams reParamstop2;

    private ProgressWheel pw_two;
    int progress = 0;
    private boolean isshow = false;

    protected ImageView stepImage1;
    protected ImageView waveImage;// 声波动画按钮
    protected ImageView pressToSendWave;// 点击发送声波按钮
    protected ImageView instruction;//
    protected EditText desWifiName;
    protected EditText desWifiPwd;
    protected ToggleButton desPwdEye;
    protected ListView devListView;// 广播到的设备列表
    protected ProgressBar loading;
    protected WaveDevlListAdapter wdListAdapter;// 设备列表adaper

    ArrayList<RelativeLayout> layoutList = new ArrayList<RelativeLayout>();

    protected Button nextBtn1;
    protected Button nextBtn2;
    protected Button showDemoBtn;// 观看操作演示
    protected Button nextBtn3;// 只有发送完声波此按钮才管用
    protected Button manualBt;// 手动配置wifi

    protected int currentStep = 0;

    // 播放操作步骤音频
    protected AssetManager assetMgr = null;
    protected MediaPlayer mediaPlayer = new MediaPlayer();

    // 声波
    protected int animTime = 1000;
    // protected int sendCounts = 0;
    protected String params = "";
    protected MyAudio playAudio;
    protected static int audioSampleRate = 48000;
    protected static int playBytes = 16;

    ScaleAnimation waveScaleAnim = null;// 发送声波动画
    AlphaAnimation waveAlphaAnim = null;// 发送声波动画

    /* 智联路由SDK */
    private byte AuthModeOpen = 0x00;
    private byte AuthModeShared = 0x01;
    private byte AuthModeAutoSwitch = 0x02;
    private byte AuthModeWPA = 0x03;
    private byte AuthModeWPAPSK = 0x04;
    private byte AuthModeWPANone = 0x05;
    private byte AuthModeWPA2 = 0x06;
    private byte AuthModeWPA2PSK = 0x07;
    private byte AuthModeWPA1WPA2 = 0x08;
    private byte AuthModeWPA1PSKWPA2PSK = 0x09;
    private WifiManager mWifiManager;
    private ElianNative elian;
    private final static int WAVE_FLAG = 0;
    private final static int SMART_CONNECT_FLAG = 1;
    private int func_flag = WAVE_FLAG;// 默认进来是声波配置
    private byte mAuthMode = 0;
    private String mConnectedSsid;
    private String mPassword;
    private Button btn_smart_connect;
    private RelativeLayout rl_smart_conn;
    private Dialog initDialog;// 显示弹出框
    private ImageView dialogCancel;// 取消按钮

    // /** 手动配置无线 */
    // // private RelativeLayout manualWifiLayout;
    // private EditText manualWifiNameEt, manualWifiPasswordEt;
    // private Spinner manualWifiAuthSpinner, manualWifiEncryptSpinner;
    // private Button addEditSaveBt;// 保存
    // // 加密方式，加密类型Spinner的适配器
    // private ArrayAdapter manualWifiAuthSpinnerAdapter;// 加密方式
    // private ArrayAdapter manualWifiEncryptSpinnerAdapter;// 加密类型
    //
    // private Boolean isManual = false;
    //
    // private static int wifiManualAuth = 0;
    // private static int wifiManualEncrypt = 0;

    @SuppressWarnings("deprecation")
    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.VOLUME_CHANGE_FINISHED: {
                // 当前音量
                currentVolume = mAudioManager
                        .getStreamVolume(AudioManager.STREAM_MUSIC);
                MyLog.v(TAG, "currentVolume=" + currentVolume);
                break;
            }
            case Consts.WHAT_WHEEL_DISMISS:
                pw_two.setVisibility(View.GONE);
                stepLayout6.setVisibility(View.GONE);
                isshow = false;
                break;
            case Consts.WHAT_SEND_WAVE_FINISHED: {// 声波发送完毕
                // sendCounts = 0;
                nextBtn3.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.blue_bg));
                nextBtn3.setClickable(true);
                /* 智联路由按钮 */
                // btn_smart_connect.setBackgroundDrawable(getResources().getDrawable(
                // R.drawable.blue_bg));
                // btn_smart_connect.setClickable(true);
                waveScaleAnim.cancel();
                break;
            }
            case Consts.WHAT_BROAD_FINISHED: {// 广播超时
                if (4 != currentStep) {
                    break;
                }
                dismissDialog();
                if (null == broadList || 0 == broadList.size()) {
                    showTextToast(R.string.broad_zero);
                } else {
                    wdListAdapter = new WaveDevlListAdapter(JVWaveSetActivity.this);
                    wdListAdapter.setData(broadList);
                    devListView.setAdapter(wdListAdapter);
                }
                playSoundStep(4);
                loading.setVisibility(View.GONE);
                rightBtn.setVisibility(View.VISIBLE);
                if (func_flag == SMART_CONNECT_FLAG) {
                    new Thread(new onStopSmartConnect()).start();
                }
                break;
            }
            case Consts.WHAT_BROAD_DEVICE: {// 广播到一个设备
                if (null != broadList) {
                    wdListAdapter = new WaveDevlListAdapter(JVWaveSetActivity.this);
                    wdListAdapter.setData(broadList);
                    devListView.setAdapter(wdListAdapter);
                }
                break;
            }
            case Consts.WHAT_ADD_DEVICE: {// 添加设备
                alertAddDialog(arg1);
                break;
            }
            case Consts.WHAT_SEND_WAVE: {// 发送声波命令
                waveScaleAnim.start();
                Jni.genVoice(params, 3);
                break;
            }

        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.WHAT_PLAY_AUDIO_WHAT: {
                switch (arg2) {
                    case MyAudio.ARG2_START: {
                        MyLog.v(TAG, "ARG2_START");
                        break;
                    }
                    case MyAudio.ARG2_FINISH: {
                        MyLog.v(TAG, "ARG2_FINISH");
                        break;
                    }
                    case MyAudio.ARG2_WAVE_FINISH: {// 声波播放完毕
                        MyLog.v(TAG, "ARG2_WAVE_FINISH");
                        // sendCounts++;
                        // if (sendCounts < 3) {
                        // handler.sendMessageDelayed(
                        // handler.obtainMessage(Consts.WHAT_SEND_WAVE), 500);
                        // } else {
                        handler.sendMessageDelayed(
                                handler.obtainMessage(Consts.WHAT_SEND_WAVE_FINISHED),
                                500);
                        // }
                        break;
                    }
                }
                break;
            }
            // 获取到声波音频
            case Consts.CALL_GEN_VOICE: {
                if (1 == arg2) {// 数据
                    if (null != obj && null != playAudio) {
                        byte[] data = (byte[]) obj;
                        playAudio.put(data);
                    }
                } else if (0 == arg2) {// 结束
                    byte[] data = {
                            'F', 'i', 'n'
                    };
                    playAudio.put(data);
                }
                break;
            }
            // 广播回调
            case Consts.CALL_QUERY_DEVICE: {// nNetMod 设备是否带wifi nCurMod
                // 设备是否正在使用wifi

                MyLog.v(TAG, "CALL_LAN_SEARCH = what=" + what + ";arg1=" + arg1
                        + ";arg2=" + arg1 + ";obj=" + obj.toString());

                // * @param nNetMod
                // * 设备是否带wifi 有无wifi
                // * @param nCurMod
                // * 设备是否正在使用wifi
                // CALL_LAN_SEARCH =
                // what=168;arg1=0;arg2=0;obj={"count":1,"curmod":0,"gid":"S","ip":"192.168.7.139","netmod":1,"no":224356522,"port":9101,"timeout":0,"type":59162,"variety":3}

                JSONObject broadObj;
                try {
                    broadObj = new JSONObject(obj.toString());
                    if (0 == broadObj.optInt("timeout")) {
                        String gid = broadObj.optString("gid");
                        int no = broadObj.optInt("no");
                        String ip = broadObj.optString("ip");
                        int port = broadObj.optInt("port");
                        int channelCount = broadObj.optInt("count");
                        int count = channelCount > 0 ? channelCount : 1;
                        String broadDevNum = gid + no;
                        int netmod = broadObj.optInt("netmod");
                        // 防止广播到设备没ip
                        if (!"".equalsIgnoreCase(ip) && 0 != port) {
                            Boolean hasAdded = PlayUtil.hasDev(deviceList,
                                    broadDevNum, ip, port, netmod);
                            if (1 == broadObj.optInt("netmod")) {// &&
                                                                 // !hasAdded)
                                // {//
                                // 带wifi设备且不在设备列表里面
                                Device addDev = new Device(ip, port, gid, no,
                                        Consts.DEFAULT_USERNAME,
                                        Consts.DEFAULT_PASSWORD, false, count, 0,
                                        null);
                                addDev.setHasAdded(hasAdded);
                                if (!PlayUtil.addDev(broadList, addDev)) {
                                    broadList.add(addDev);
                                    handler.sendMessage(handler
                                            .obtainMessage(Consts.WHAT_BROAD_DEVICE));
                                }
                            }

                        }

                    } else if (1 == broadObj.optInt("timeout")) {
                        CacheUtil.saveDevList(deviceList);
                        handler.sendMessage(handler
                                .obtainMessage(Consts.WHAT_BROAD_FINISHED));
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
            }
            default: {
                handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
                break;
            }
        }

    }

    @Override
    protected void onDestroy() {
        if (func_flag == SMART_CONNECT_FLAG) {
            new Thread(new onStopSmartConnect()).start();
        }
        super.onDestroy();
    }

    AudioManager mAudioManager;

    @Override
    protected void initSettings() {
        mAudioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        // 最大音量
        maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        // 当前音量
        currentVolume = mAudioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        MyLog.v(TAG, "maxVolume=" + maxVolume + ";currentVolume="
                + currentVolume);

        deviceList = CacheUtil.getDevList();
        assetMgr = this.getAssets();
        playAudio = MyAudio.getIntance(Consts.WHAT_PLAY_AUDIO_WHAT,
                JVWaveSetActivity.this, audioSampleRate);
        /* 智联路由 */
        boolean result = ElianNative.LoadLib();
        if (!result) {
            Log.e(TAG, "can't load elianjni lib");
            return;
        }
        elian = new ElianNative();
    }

    private void setCurrentWifi() {
        if (null == wifiAdmin) {
            wifiAdmin = new WifiAdmin(JVWaveSetActivity.this);
        }
        // wifi打开的前提下,获取oldwifiSSID
        if (wifiAdmin.getWifiState()) {
            if (null != wifiAdmin.getSSID()) {
                oldWifiSSID = wifiAdmin.getSSID().replace("\"", "");
            }
        }
        desWifiName.setText(oldWifiSSID);

        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager.isWifiEnabled()) {
            WifiInfo WifiInfo = mWifiManager.getConnectionInfo();
            mConnectedSsid = WifiInfo.getSSID();
            int iLen = mConnectedSsid.length();

            if (iLen == 0) {
                return;
            }

            if (mConnectedSsid.startsWith("\"")
                    && mConnectedSsid.endsWith("\"")) {
                mConnectedSsid = mConnectedSsid.substring(1, iLen - 1);
            }
            List<ScanResult> ScanResultlist = mWifiManager.getScanResults();
            for (int i = 0, len = ScanResultlist.size(); i < len; i++) {
                ScanResult AccessPoint = ScanResultlist.get(i);

                if (AccessPoint.SSID.equals(mConnectedSsid)) {
                    boolean WpaPsk = AccessPoint.capabilities
                            .contains("WPA-PSK");
                    boolean Wpa2Psk = AccessPoint.capabilities
                            .contains("WPA2-PSK");
                    boolean Wpa = AccessPoint.capabilities.contains("WPA-EAP");
                    boolean Wpa2 = AccessPoint.capabilities
                            .contains("WPA2-EAP");

                    if (AccessPoint.capabilities.contains("WEP")) {
                        mAuthMode = AuthModeOpen;
                        break;
                    }

                    if (WpaPsk && Wpa2Psk) {
                        mAuthMode = AuthModeWPA1PSKWPA2PSK;
                        break;
                    } else if (Wpa2Psk) {
                        mAuthMode = AuthModeWPA2PSK;
                        break;
                    } else if (WpaPsk) {
                        mAuthMode = AuthModeWPAPSK;
                        break;
                    }

                    if (Wpa && Wpa2) {
                        mAuthMode = AuthModeWPA1WPA2;
                        break;
                    } else if (Wpa2) {
                        mAuthMode = AuthModeWPA2;
                        break;
                    } else if (Wpa) {
                        mAuthMode = AuthModeWPA;
                        break;
                    }

                    mAuthMode = AuthModeOpen;

                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void initUi() {
        setContentView(R.layout.soundwave_layout);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        topBar = (LinearLayout) findViewById(R.id.top_bar);
        leftBtn = (Button) findViewById(R.id.btn_left);
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        rightBtn = (Button) findViewById(R.id.btn_right);
        currentMenu.setText(R.string.prepare_step);
        rightBtn.setText(getResources().getString(R.string.try_again));
        rightBtn.setTextColor(getResources().getColor(R.color.white));
        rightBtn.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.feedback_bg));
        rightBtn.setVisibility(View.GONE);
        reParamstop2 = new RelativeLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        reParamstop2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        reParamstop2.addRule(RelativeLayout.CENTER_VERTICAL);
        reParamstop2.setMargins(0, 0, 30, 0);
        rightBtn.setLayoutParams(reParamstop2);

        stepLayout1 = (RelativeLayout) findViewById(R.id.step_layout1);
        stepLayout2 = (RelativeLayout) findViewById(R.id.step_layout2);
        stepLayout3 = (RelativeLayout) findViewById(R.id.step_layout3);
        stepLayout4 = (RelativeLayout) findViewById(R.id.step_layout4);
        stepLayout5 = (RelativeLayout) findViewById(R.id.step_layout5);
        stepLayout6 = (RelativeLayout) findViewById(R.id.step_layout6);
        // manualWifiLayout = (RelativeLayout)
        // findViewById(R.id.manualwifilayout);

        stepImage1 = (ImageView) findViewById(R.id.step_img1);
        waveImage = (ImageView) findViewById(R.id.wavebg);
        instruction = (ImageView) findViewById(R.id.instruction);
        pressToSendWave = (ImageView) findViewById(R.id.press_sendwave);
        stepImage1.setImageResource(R.drawable.reset_bg);
        if (Consts.LANGUAGE_ZH == ConfigUtil
                .getLanguage2(JVWaveSetActivity.this)) {
            instruction.setImageResource(R.drawable.instruction_ch);
        } else if (Consts.LANGUAGE_ZHTW == ConfigUtil
                .getLanguage2(JVWaveSetActivity.this)) {
            instruction.setImageResource(R.drawable.instruction_chtw);
        } else {
            instruction.setImageResource(R.drawable.instruction_en);
        }
        layoutList.add(0, stepLayout1);
        layoutList.add(1, stepLayout2);
        layoutList.add(2, stepLayout3);
        layoutList.add(3, stepLayout4);
        layoutList.add(4, stepLayout5);
        // layoutList.add(5, manualWifiLayout);// 手动设置界面

        desWifiName = (EditText) findViewById(R.id.deswifiname);
        desWifiPwd = (EditText) findViewById(R.id.deswifipwd);
        setCurrentWifi();
        desPwdEye = (ToggleButton) findViewById(R.id.despwdeye);
        devListView = (ListView) findViewById(R.id.devlistview);
        pw_two = (ProgressWheel) findViewById(R.id.progressBarTwo);
        loading = (ProgressBar) findViewById(R.id.loading);
        loading.setVisibility(View.GONE);

        desPwdEye.setChecked(true);
        desPwdEye.setOnCheckedChangeListener(myOnCheckedChangeListener);

        nextBtn1 = (Button) findViewById(R.id.step_btn1);
        nextBtn2 = (Button) findViewById(R.id.step_btn2);
        nextBtn3 = (Button) findViewById(R.id.step_btn3);
        showDemoBtn = (Button) findViewById(R.id.showdemo);
        manualBt = (Button) findViewById(R.id.bt_setting_manualconnect);// 手动配置

        /* 智联路由 */
        rl_smart_conn = (RelativeLayout) findViewById(R.id.smart_conn_layout);
        btn_smart_connect = (Button) findViewById(R.id.btn_smart_conn);

        stepLayout6.setOnClickListener(myOnClickListener);
        rightBtn.setOnClickListener(myOnClickListener);
        leftBtn.setOnClickListener(myOnClickListener);
        nextBtn1.setOnClickListener(myOnClickListener);
        nextBtn2.setOnClickListener(myOnClickListener);
        showDemoBtn.setOnClickListener(myOnClickListener);
        nextBtn3.setOnClickListener(myOnClickListener);
        pressToSendWave.setOnClickListener(myOnClickListener);
        waveImage.setOnClickListener(myOnClickListener);
        manualBt.setOnClickListener(myOnClickListener);

        /* 智联路由 */
        btn_smart_connect.setOnClickListener(myOnClickListener);
        if (!Consts.SMART_CONN_ENABLED) {
            rl_smart_conn.setVisibility(View.INVISIBLE);
        }
        /** 设置缩放动画 */
        waveScaleAnim = new ScaleAnimation(0.0f, 5.0f, 0.0f, 5.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        waveScaleAnim.setDuration(800);// 设置动画持续时间
        waveScaleAnim.setRepeatCount(99999);// 设置重复次数
        // animation.setFillAfter(boolean);//动画执行完后是否停留在执行完的状态
        // animation.setStartOffset(long startOffset);//执行前的等待时间
        waveAlphaAnim = new AlphaAnimation(0.1f, 1.0f);
        waveAlphaAnim.setDuration(animTime);// 设置动画持续时间
        waveAlphaAnim.setRepeatCount(3);// 设置重复次数
        waveAlphaAnim.setStartOffset(0);// 执行前的等待时间
        waveImage.setAnimation(waveScaleAnim);
        showLayoutAtIndex(currentStep);
        /** 手动设置wifi */
        // TODO
        //
        // manualWifiNameEt = (EditText) findViewById(R.id.manual_wifiname_et);
        // manualWifiPasswordEt = (EditText)
        // findViewById(R.id.manual_wifipassword_et);
        // manualWifiAuthSpinner = (Spinner)
        // findViewById(R.id.manual_wifiauth_spinner);
        // manualWifiEncryptSpinner = (Spinner)
        // findViewById(R.id.manual_wifiencrypt_spinner);
        // addEditSaveBt = (Button) findViewById(R.id.manual_addeditsave);
        // manualWifiEncryptSpinnerAdapter = ArrayAdapter
        // .createFromResource(this, R.array.array_wifiencrypt,
        // android.R.layout.simple_spinner_item);// 加密类型
        // manualWifiAuthSpinnerAdapter = ArrayAdapter.createFromResource(this,
        // R.array.array_wifiauth, android.R.layout.simple_spinner_item);// 加密方式
        // // 设置下拉列表的风格
        // manualWifiEncryptSpinnerAdapter
        // .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // manualWifiAuthSpinnerAdapter
        // .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // // 将adapter 添加到spinner中
        // manualWifiEncryptSpinner.setAdapter(manualWifiEncryptSpinnerAdapter);
        // manualWifiAuthSpinner.setAdapter(manualWifiAuthSpinnerAdapter);
        // // 添加事件Spinner事件监听
        // manualWifiEncryptSpinner
        // .setOnItemSelectedListener(new SpinnerSelectedListener());
        // manualWifiAuthSpinner
        // .setOnItemSelectedListener(new SpinnerSelectedListener());
        //
        // addEditSaveBt.setOnClickListener(myOnClickListener);

    }

    /**
     * 40秒倒计时
     **/
    final Runnable r = new Runnable() {
        public void run() {
            while (progress < 361) {
                pw_two.incrementProgress();
                progress++;
                if (progress == 361) {
                    handler.sendEmptyMessage(Consts.WHAT_WHEEL_DISMISS);
                }
                try {
                    Thread.sleep(110);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    /**
     * 密码显示隐藏
     */
    OnCheckedChangeListener myOnCheckedChangeListener = new OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
            if (arg1) {
                desWifiPwd
                        .setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);// 显示密码
            } else {
                desWifiPwd.setInputType(InputType.TYPE_CLASS_TEXT
                        | InputType.TYPE_TEXT_VARIATION_PASSWORD);// 隐藏密码
            }
            desWifiPwd.setSelection(desWifiPwd.getText().toString().length());
            desPwdEye.setChecked(arg1);
        }

    };

    /**
     * 显示哪个布局
     * 
     * @param showIndex
     */
    @SuppressWarnings("deprecation")
    private void showLayoutAtIndex(int showIndex) {
        // btn_smart_connect.setClickable(false);

        nextBtn3.setClickable(false);
        nextBtn3.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.login_blue_bg));
        waveScaleAnim.cancel();
        if (showIndex < 0) {
            JVWaveSetActivity.this.finish();
        } else {
            currentMenu.setText(titleID[showIndex]);
            int length = layoutList.size();

            for (int i = 0; i < length; i++) {
                RelativeLayout layout = layoutList.get(i);
                if (null != layout) {
                    if (i == showIndex) {
                        layout.setVisibility(View.VISIBLE);
                    } else {
                        layout.setVisibility(View.GONE);
                    }
                }

            }
            if (showIndex >= 0 && showIndex < 3) {
                playSoundStep(showIndex);
            }
        }

        // if (2 == showIndex) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(desWifiPwd, InputMethodManager.SHOW_FORCED);
        imm.hideSoftInputFromWindow(desWifiPwd.getWindowToken(), 0); // 强制隐藏键盘
        // }

    }

    private void backMethod() {
        resetVolume();
        // MyLog.e(TAG, "当前步骤：" + currentStep);
        // if (currentStep == 5) {// 手动配置返回
        // currentStep = 1;
        // }
        if (currentStep == (layoutList.size() - 1)) {
            Intent intent = new Intent();
            setResult(Consts.DEVICE_ADD_SUCCESS_RESULT, intent);
            JVWaveSetActivity.this.finish();
        } else {
            showLayoutAtIndex(--currentStep);

        }

    }

    @Override
    public void onBackPressed() {
        backMethod();
    }

    /**
     * 点击事件
     */
    OnClickListener myOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.waveshow_cancle:
                    initDialog.dismiss();
                    if (null != mediaPlayer && mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                    }
                    break;
                case R.id.btn_left:
                    backMethod();
                    break;
                case R.id.step_btn1:
                    currentStep = 1;
                    mPassword = desWifiPwd.getText().toString();
                    // if (mPassword.length() < 8) {
                    // showTextToast("请输入合法的wifi密码");
                    // break;
                    // }
                    showLayoutAtIndex(currentStep);
                    break;
                case R.id.step_btn2:
                    currentStep = 2;
                    showLayoutAtIndex(currentStep);
                    initSummaryDialog();
                    break;
                case R.id.btn_smart_conn:// 智联路由
                    func_flag = SMART_CONNECT_FLAG;
                case R.id.btn_right:// 发局域网广播搜索局域网设备
                case R.id.step_btn3:// 发局域网广播搜索局域网设备
                    resetVolume();
                    // createDialog("", false);
                    if (func_flag == SMART_CONNECT_FLAG) {
                        Log.e(TAG, "开始智联路由...StartSmartConnection");
                        elian.InitSmartConnection(null, 1, 0);// V1
                        elian.StartSmartConnection(mConnectedSsid, mPassword,
                                "android smart custom", mAuthMode);
                    }
                    isshow = true;
                    pw_two.setVisibility(View.VISIBLE);
                    stepLayout6.setVisibility(View.VISIBLE);
                    progress = 0;
                    pw_two.resetCount();
                    Thread s = new Thread(r);
                    s.start();
                    loading.setVisibility(View.GONE);
                    playSoundStep(3);
                    broadList.clear();
                    Jni.queryDevice("", 0, 40 * 1000);
                    currentStep = 4;
                    showLayoutAtIndex(currentStep);
                    break;
                case R.id.step_layout6:
                    break;
                case R.id.press_sendwave:
                    // 点击发送声波就把音量自动调节到最大
                    changeToMaxVolume();
                    func_flag = WAVE_FLAG;
                    try {
                        if (null != mediaPlayer) {
                            mediaPlayer.stop();
                        }
                        playAudio.startPlay(playBytes, true);
                        waveScaleAnim.start();
                        // if (isManual) {
                        // params = manualWifiNameEt.getText() + ";"
                        // + manualWifiPasswordEt.getText() + ";"
                        // + wifiManualAuth + ";" + wifiManualEncrypt;
                        // Toast.makeText(JVWaveSetActivity.this, params,
                        // Toast.LENGTH_LONG).show();
                        // } else {
                        params = desWifiName.getText() + ";" + desWifiPwd.getText();
                        // }
                        MyLog.e(TAG, "params:" + params);
                        Jni.genVoice(params, 3);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;
                case R.id.showdemo:
                    // currentStep = 3;
                    // showLayoutAtIndex(currentStep);
                    // TODO
                    initSummaryDialog();
                    break;
                case R.id.bt_setting_manualconnect:// 手动配置界面
                    // TODO
                    // 显示手动设置，隐先准备步骤界面
                    // isManual = true;
                    // currentStep = 5;
                    // showLayoutAtIndex(currentStep);
                    break;
                case R.id.manual_addeditsave:
                    // TODO
                    // currentStep = 1;
                    // showLayoutAtIndex(currentStep);
                    // }
                    break;
                default:
                    break;
            }
        }
    };

    /** 弹出框初始化 */
    private void initSummaryDialog() {
        initDialog = new Dialog(JVWaveSetActivity.this, R.style.mydialog);
        View view = LayoutInflater.from(JVWaveSetActivity.this).inflate(
                R.layout.dialog_wave, null);
        initDialog.setContentView(view);
        dialogCancel = (ImageView) view.findViewById(R.id.waveshow_cancle);
        dialogCancel.setOnClickListener(myOnClickListener);
        initDialog.show();
        playSoundStep(5);
    }

    @Override
    protected void saveSettings() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        setCurrentWifi();
        BitmapCache.getInstance().clearCache();
    }

    @Override
    protected void onPause() {
        super.onPause();
        BitmapCache.getInstance().clearCache();
        CacheUtil.saveDevList(deviceList);
        if (null != mediaPlayer) {
            mediaPlayer.stop();
        }
    }

    @Override
    protected void freeMe() {
        // 退出界面，将音量回复到之前的音量
        mediaPlayer.release();
    }

    private void playSoundStep(int index) {
        try {
            String file = "";
            if (Consts.LANGUAGE_ZH == ConfigUtil
                    .getLanguage2(JVWaveSetActivity.this)) {
                file = stepSoundZH[index];
            } else if (Consts.LANGUAGE_ZHTW == ConfigUtil
                    .getLanguage2(JVWaveSetActivity.this)) {
                file = stepSoundZHTW[index];
            } else {
                file = stepSoundEN[index];
            }

            AssetFileDescriptor afd = assetMgr.openFd(file);
            mediaPlayer.reset();

            // 使用MediaPlayer加载指定的声音文件。
            mediaPlayer.setDataSource(afd.getFileDescriptor(),
                    afd.getStartOffset(), afd.getLength());
            // 准备声音
            mediaPlayer.prepare();
            // 播放
            mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 设置三种类型参数分别为String,Integer,String
    class AddDevTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {
            int index = Integer.parseInt(params[0]);
            Device addDevice = broadList.get(index);
            String ip = addDevice.getIp();
            int port = addDevice.getPort();
            int addRes = -1;

            try {
                for (Device dev : deviceList) {
                    if (dev.getFullNo() == addDevice.getFullNo()) {
                        addRes = 0;
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (0 != addRes) {
                boolean localFlag = Boolean.valueOf(statusHashMap
                        .get(Consts.LOCAL_LOGIN));
                try {
                    if (null != addDevice) {
                        if (localFlag) {// 本地添加
                            addRes = 0;
                        } else {
                            addDevice = DeviceUtil.addDevice2(addDevice,
                                    statusHashMap.get(Consts.KEY_USERNAME),
                                    addDevice.getNickName());
                            if (null != addDevice) {
                                addRes = 0;
                            }
                        }
                    }

                    if (0 == addRes) {
                        broadList.remove(index);
                        handler.sendMessage(handler
                                .obtainMessage(Consts.WHAT_BROAD_DEVICE));
                        addDevice.setOnlineStateLan(1);
                        addDevice.setIp(ip);
                        addDevice.setPort(port);
                        addDevice.setHasAdded(true);
                        deviceList.add(0, addDevice);
                        CacheUtil.saveDevList(deviceList);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return addRes;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            dismissDialog();
            // 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
            if (0 == result) {
                showTextToast(R.string.add_device_succ);

                if (!hasNewDevice()) {
                    Intent intent = new Intent();
                    setResult(Consts.DEVICE_ADD_SUCCESS_RESULT, intent);
                    JVWaveSetActivity.this.finish();
                }

            } else {
                showTextToast(R.string.add_device_failed);
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

    /**
     * 检查是否还有新设备
     * 
     * @return
     */
    public boolean hasNewDevice() {
        boolean hasNewDev = false;
        if (null == broadList || 0 == broadList.size()) {
        } else {
            for (Device dev : broadList) {
                if (!dev.isHasAdded()) {
                    hasNewDev = true;
                    break;
                }
            }
        }
        return hasNewDev;
    }

    /**
     * 弹出添加设备界面
     */
    public void alertAddDialog(final int index) {
        // 提示对话框
        AlertDialog.Builder builder = new Builder(this);
        builder.setTitle(R.string.tips)
                .setMessage(
                        getResources().getString(R.string.wave_add_dev) + "   "
                                + broadList.get(index).getFullNo())
                .setPositiveButton(R.string.sure,
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                createDialog("", false);
                                dialog.dismiss();
                                AddDevTask task = new AddDevTask();
                                String[] params = new String[3];
                                params[0] = String.valueOf(index);
                                task.execute(params);
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
    }

    /**
     * 2015.5.27 将音量调节到最大
     */
    public void changeToMaxVolume() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume, 0);
    }

    /**
     * 2015.5.27 将音量恢复到原来
     */
    public void resetVolume() {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume,
                0);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
                || keyCode == KeyEvent.KEYCODE_VOLUME_UP
                || keyCode == KeyEvent.KEYCODE_VOLUME_MUTE) {
            handler.sendMessageDelayed(
                    handler.obtainMessage(Consts.VOLUME_CHANGE_FINISHED), 500);
            return false;
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isshow) {
                backMethod();
            }
            return true;
        } else {
            return false;
        }
    }

    private class onStopSmartConnect implements Runnable {

        @Override
        public void run() {
            // TODO Auto-generated method stub
            if (null != elian) {
                elian.StopSmartConnection();
            }
        }

    }

    // Spinner 的点击操作
    // class SpinnerSelectedListener implements OnItemSelectedListener {
    //
    // public void onItemSelected(AdapterView<?> view, View arg1, int index,
    // long arg3) {
    //
    // if (view.getId() == R.id.manual_wifiencrypt_spinner) {// 加密类型
    // wifiManualEncrypt = index;
    // } else if (view.getId() == R.id.manual_wifiauth_spinner) {// 加密方式
    // wifiManualAuth = index;
    // }
    // }
    //
    // public void onNothingSelected(AdapterView<?> view) {
    // }
    // }
}
