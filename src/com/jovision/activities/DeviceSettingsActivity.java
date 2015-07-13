
package com.jovision.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.activities.DevSettingsAlarmTimeFragment.OnAlarmTimeActionListener;
import com.jovision.activities.DeviceSettingsMainFragment.OnFuncActionListener;
import com.jovision.bean.Device;
import com.jovision.commons.JVDeviceConst;
import com.jovision.commons.JVNetConst;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.DeviceUtil;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceSettingsActivity extends BaseActivity implements
        OnFuncActionListener, OnClickListener, OnAlarmTimeActionListener {

    private static final String TAG = "DeviceSettingsActivity";

    protected boolean bConnectedFlag = true;
    private ProgressDialog waitingDialog;
    private DeviceSettingsMainFragment deviceSettingsMainFragment;
    private int fragment_tag = 0;// 0，设置主界面； 1设置时间
    private int alarmEnabled = -1;
    private int mdEnabled = -1;
    private int alarmEnabling = -1;
    private int mdEnabling = -1;
    private int alarmSoundEnabled = -1;
    private int alarmSoundEnabling = -1;
    private int window = 0;
    private OnMainListener mainListener;
    private JSONObject initDevParamObject;
    private MyHandler myHandler;
    private String startTimeSaved, endTimeSaved;
    private String startTimeSetting, endTimeSetting;
    private String alarmTime0;
    private String alarmTime0ing;
    private int alarm_way_flag = -1; // 报警的方式，走云视通还是报警服务器 0:报警服务器 1:云视通
    private Device device;
    private ArrayList<Device> deviceList;
    private int deviceIndex = 0;
    private DeviceSettingsActivity activity;
    private boolean onFuncOperationFlag = false;// 标志进入到这个界面后用户是否有过操作标志
    private int funcIndex = -1;// 标示当前点击的那个设备功能
    private String[] funcParamArray;
    private HashMap<String, String> streamMap;
    public static boolean isadmin;
    private int power;
    public String descript;
    public static String fullno;
    public String timezones;
    public int nTimeFormat;
    private boolean bGetStreamInfoRes = false;
    private int effect_flag;

    // private boolean update_flag = false;

    // ------------------------------------
    // assets/device_setting.json中相应功能对应的标志
    private String JSONTAG;
    private String mCurrentMenuTitle;

    // ------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dev_settings_main);
        Bundle extras = getIntent().getExtras();
        activity = this;
        if (null == extras) {
            finish();
            return;
        }
        myHandler = new MyHandler();
        // ------------------------------------
        JSONTAG = extras.getString("jsontag");
        // ------------------------------------
        window = extras.getInt("window");
        deviceIndex = extras.getInt("deviceIndex");
        isadmin = extras.getBoolean("isadmin");
        descript = extras.getString("descript");
        fullno = extras.getString("fullno");
        power = extras.getInt("power");
        streamMap = (HashMap<String, String>) extras
                .getSerializable("streamMap");
        Log.i("TAG", streamMap.toString());
        deviceList = CacheUtil.getDevList();
        // update_flag = extras.getBoolean("updateflag");// 如果播放界面没取到码流数据，再取一遍
        funcParamArray = new String[10];// 目前就3个功能，安全防护、移动侦测、防护时间段，当然这个只要比功能大就行

        effect_flag = MySharedPreference.getInt("effect_flag");
        if (0 != deviceList.size()) {
            device = deviceList.get(deviceIndex);
        }
        // mainListener.onMainAction(100,
        // 0, window, deviceIndex);
        InitViews();
        if (null == waitingDialog) {
            waitingDialog = new ProgressDialog(this);
            waitingDialog.setCancelable(false);
            waitingDialog
                    .setMessage(getResources().getString(R.string.waiting));
        }

    }

    private void InitViews() {
        leftBtn = (Button) findViewById(R.id.btn_left);
        rightBtn = (Button) findViewById(R.id.btn_right);
        rightBtn.setVisibility(View.GONE);
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        leftBtn.setOnClickListener(this);

        // ------------------------------------
        initDatas();
        // ------------------------------------

        ResolveStreamInfo(streamMap);
    }

    /**
     * 初始化数据及一些设置操作
     */
    private void initDatas() {
        // 标题
        if (JSONTAG.equals(Consts.DEVICE_SETTING_ALARM)) {
            mCurrentMenuTitle = getResources().getString(
                    R.string.device_setting_alarm);
        } else if (JSONTAG.equals(Consts.DEVICE_SETTING_TIMEZONE)) {
            mCurrentMenuTitle = getResources().getString(
                    R.string.device_setting_timezone);
        } else if (JSONTAG.equals(Consts.DEVICE_SETTING_SYSTEMMANAGE)) {
            mCurrentMenuTitle = getResources().getString(
                    R.string.device_setting_systemmanage);
        } else if (JSONTAG.equals(Consts.DEVICE_SETTING_OTHER)) {
            mCurrentMenuTitle = getResources().getString(
                    R.string.device_setting_other);
        }
        currentMenu.setText(mCurrentMenuTitle);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        if (null != streamMap.get("timezone")
                && "".equals(MySharedPreference.getString("TIMEZONE"))) {
            int index = Integer.valueOf(streamMap.get("timezone"));
            Log.i("TAG", streamMap.toString());
            mainListener
                    .onMainAction(
                            JVNetConst.RC_EXTEND,
                            JVNetConst.TIME_ZONE,
                            0,
                            0,
                            0,
                            getString(R.string.str_timezone)
                                    + "("
                                    + getResources().getStringArray(
                                            R.array.time_zone)[12 - index]
                                    + ")");
            timezones = getResources().getStringArray(R.array.time_zone)[12 - index];
        } else if (!"".equals(MySharedPreference.getString("TIMEZONE"))) {
            mainListener.onMainAction(
                    JVNetConst.RC_EXTEND,
                    JVNetConst.TIME_ZONE,
                    0,
                    0,
                    0,
                    getString(R.string.str_timezone) + "("
                            + MySharedPreference.getString("TIMEZONE") + ")");
            timezones = MySharedPreference.getString("TIMEZONE");
        } else {
            mainListener.onMainAction(JVNetConst.RC_EXTEND,
                    JVNetConst.TIME_ZONE, 1, 0, 0, null);
        }

        if (null != streamMap.get("MobileQuality")) {
            mainListener.onMainAction(JVNetConst.RC_EXTEND,
                    JVNetConst.SET_TIME, 1, 0, 0, null);
        } else {
            mainListener.onMainAction(JVNetConst.RC_EXTEND,
                    JVNetConst.SET_TIME, 2, 0, 0, null);
        }

        // if (null != streamMap.get("bSntp")) {
        // intentnum = Integer.valueOf(streamMap.get("bSntp"));
        // }
        if (null != streamMap.get("nTimeFormat")) {
            nTimeFormat = Integer.valueOf(streamMap.get("nTimeFormat"));
        }
        // if (null != streamMap.get("effect_flag")
        // && !"".equalsIgnoreCase(streamMap
        // .get("effect_flag"))) {
        //
        // int effect_flags = Integer.parseInt(streamMap
        // .get("effect_flag"));
        // if (0 == effect_flags) {
        // MySharedPreference.putInt("screenover", 0);
        // mainListener.onMainAction(
        // JVNetConst.OVERTURNIMG, 0, 0,
        // 0, 0, null);
        // } else if (4 == effect_flags) {
        // MySharedPreference.putInt("screenover", 4);
        // mainListener.onMainAction(
        // JVNetConst.OVERTURNIMG, 4, 0,
        // 0, 0, null);
        // }
        // }
    }

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {

        // TODO Auto-generated method stub
        switch (what) {
        // 连接结果
            case Consts.CALL_CONNECT_CHANGE:
                if (window != arg1) {
                    MyLog.e("DeviceSetting",
                            "---------不是不是不是不是本window的回调---------->" + arg1);
                    return;
                }
                switch (arg2) {
                // case JVNetConst.DISCONNECT_OK: {
                // bConnectedFlag = false;
                // break;
                // }

                // 4 -- 连接失败
                // case JVNetConst.CONNECT_FAILED: {
                // bConnectedFlag = false;
                // if (waitingDialog != null && waitingDialog.isShowing())
                // waitingDialog.dismiss();
                // try {
                // JSONObject connectObj = new JSONObject(obj.toString());
                // String errorMsg = connectObj.getString("msg");
                // if ("password is wrong!".equalsIgnoreCase(errorMsg)
                // || "pass word is wrong!".equalsIgnoreCase(errorMsg)) {//
                // 密码错误时提示身份验证失败
                // showTextToast(R.string.connfailed_auth);
                // } else if ("channel is not open!"
                // .equalsIgnoreCase(errorMsg)) {// 无该通道服务
                // showTextToast(R.string.connfailed_channel_notopen);
                // } else if ("connect type invalid!"
                // .equalsIgnoreCase(errorMsg)) {// 连接类型无效
                // showTextToast(R.string.connfailed_type_invalid);
                // } else if ("client count limit!".equalsIgnoreCase(errorMsg))
                // {//
                // 超过主控最大连接限制
                // showTextToast(R.string.connfailed_maxcount);
                // } else if ("connect timeout!".equalsIgnoreCase(errorMsg)) {//
                // showTextToast(R.string.connfailed_timeout);
                // } else {// "Connect failed!"
                // showTextToast(R.string.connect_failed);
                // }
                //
                // } catch (JSONException e) {
                // e.printStackTrace();
                // }
                // }
                // finish();
                // break;
                    case JVNetConst.CONNECT_FAILED:
                    case JVNetConst.ABNORMAL_DISCONNECT:
                    case JVNetConst.DISCONNECT_OK:
                    case JVNetConst.SERVICE_STOP:

                        if (waitingDialog != null && waitingDialog.isShowing())
                            waitingDialog.dismiss();
                        bConnectedFlag = false;
                        showTextToast(R.string.str_alarm_connect_except);
                        // 关闭设备设置的索引页
                        JVDeviceSettingsReferenceActivity.mInstance.finish();
                        finish();

                        break;
                    default:
                        if (waitingDialog != null && waitingDialog.isShowing())
                            waitingDialog.dismiss();
                        bConnectedFlag = false;
                        showTextToast(R.string.str_alarm_connect_except);
                        // finish();
                        break;
                }
                break;
            case Consts.CALL_TEXT_DATA:
                MyLog.i(TAG, "CALL_TEXT_DATA: " + what + ", " + arg1 + ", " + arg2
                        + ", " + obj);
                switch (arg2) {
                    case JVNetConst.JVN_RSP_TEXTDATA:// 文本数据
                        // if (waitingDialog != null &&
                        // waitingDialog.isShowing()) {
                        // waitingDialog.dismiss();
                        // }

                        String allStr = obj.toString();

                        MyLog.v("Alarm", "文本数据--" + allStr);
                        try {
                            JSONObject dataObj = new JSONObject(allStr);
                            int flag = dataObj.getInt("flag");
                            switch (flag) {
                                case JVNetConst.JVN_GET_USERINFO: {
                                    dismissDialog();
                                    int extend_type = dataObj.getInt("extend_type");
                                    if (Consts.EX_ACCOUNT_MODIFY == extend_type) {
                                        // --修改设备的用户名密码，只要走回调就修改成功了
                                        mainListener.onMainAction(
                                                JVNetConst.JVN_GET_USERINFO, 0, 0,
                                                Consts.DEV_MOD_USERINFO, 0, null);
                                    }
                                    break;
                                }
                                case JVNetConst.JVN_STREAM_INFO:// 码流
                                    myHandler.removeMessages(JVNetConst.JVN_STREAM_INFO);
                                    bGetStreamInfoRes = true;
                                    HashMap<String, String> map = ConfigUtil
                                            .genMsgMap(dataObj.getString("msg"));
                                    streamMap = map;
                                    UpdateSettingsRes(funcIndex, map);
                                    if (null != map.get("timezone")) {
                                        int index = Integer.valueOf(map.get("timezone"));
                                        mainListener
                                                .onMainAction(
                                                        JVNetConst.RC_EXTEND,
                                                        JVNetConst.TIME_ZONE,
                                                        0,
                                                        0,
                                                        0,
                                                        getString(R.string.str_timezone)
                                                                + "("
                                                                + getResources()
                                                                        .getStringArray(
                                                                                R.array.time_zone)[12 - index]
                                                                + ")");
                                    }

                                    if (null != streamMap) {
                                        if (null != streamMap.get("effect_flag")
                                                && !"".equalsIgnoreCase(streamMap
                                                        .get("effect_flag"))) {
                                            dismissDialog();
                                            int effect_flags = Integer.parseInt(streamMap
                                                    .get("effect_flag"));
                                            effect_flag = effect_flags;
                                            MySharedPreference.putInt("effect_flag",
                                                    effect_flag);
                                            if (0 == (0x04 & effect_flag)) {
                                                MySharedPreference.putInt("screentag",
                                                        Consts.SCREEN_NORMAL);
                                                mainListener.onMainAction(
                                                        JVNetConst.OVERTURNIMG, 0, 0, 0, 0,
                                                        null);
                                            } else {
                                                MySharedPreference.putInt("screentag",
                                                        Consts.SCREEN_OVERTURN);
                                                mainListener.onMainAction(
                                                        JVNetConst.OVERTURNIMG, 4, 0, 0, 0,
                                                        null);
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }

                            // CALL_TEXT_DATA: 165, 0, 81,
                            // {"extend_arg1":0,"extend_arg2":0,"extend_arg3":0,
                            // "extend_type":2,"flag":0,"packet_count":7,
                            // "packet_id":0,"packet_length":0,"packet_type":6}
                            int packet_type = dataObj.getInt("packet_type");
                            switch (packet_type) {
                            // case JVNetConst.RC_GETPARAM:
                            //
                            // break;// end of JVNetConst.RC_GETPARAM
                                case JVNetConst.RC_EXTEND:
                                    myHandler.removeMessages(Consts.DEV_SETTINGS_ALARM);

                                    int packet_subtype = dataObj.getInt("packet_count");
                                    int ex_type = dataObj.optInt("extend_type");
                                    if (packet_subtype == JVNetConst.RC_EX_MD
                                            && ex_type == JVNetConst.EX_MD_SUBMIT) {
                                        // if(funcIndex ==
                                        // Consts.DEV_SETTINGS_MD){
                                        // 移动侦测 ok
                                        Log.e("Alarm", "EX_MD_SUBMIT---:" + mdEnabled
                                                + "--" + mdEnabling);
                                        mdEnabled = mdEnabling;
                                        initDevParamObject.put("bMDEnable", mdEnabled);
                                        mainListener.onMainAction(packet_type,
                                                packet_subtype, ex_type, funcIndex,
                                                mdEnabling, null);
                                        // }
                                    } else if (packet_subtype == JVNetConst.RC_EX_ALARM
                                            && ex_type == JVNetConst.EX_ALARM_SUBMIT) {
                                        if (funcIndex == Consts.DEV_ALARAM_SOUND) {
                                            Log.e("Alarm", "EX_ALARM_SOUND_SUBMIT---:"
                                                    + alarmSoundEnabled + "--"
                                                    + alarmSoundEnabling);
                                            alarmSoundEnabled = alarmSoundEnabling;
                                            initDevParamObject.put("bAlarmSound",
                                                    alarmSoundEnabled);
                                            mainListener.onMainAction(packet_type,
                                                    packet_subtype, ex_type, funcIndex,
                                                    alarmSoundEnabling, null);
                                        } else {
                                            // 安全防护或者设置安全防护时间ok
                                            Log.e("Alarm", "EX_ALARM_SUBMIT---:"
                                                    + alarmEnabled + "--" + alarmEnabling);
                                            alarmEnabled = alarmEnabling;
                                            initDevParamObject.put("bAlarmEnable",
                                                    alarmEnabled);
                                            mainListener.onMainAction(packet_type,
                                                    packet_subtype, ex_type, funcIndex,
                                                    alarmEnabling, null);// 67231
                                        }

                                    } else {
                                        Log.e("Alarm", "RC_EXTEND Case else Error");
                                        return;
                                    }

                                    break;
                                default:
                                    break;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (waitingDialog != null && waitingDialog.isShowing()) {
                            waitingDialog.dismiss();
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stub
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void initUi() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void saveSettings() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void freeMe() {
        // TODO Auto-generated method stub
        isadmin = false;
    }

    @Override
    public void OnFuncEnabled(int func_index, int enabled) {
        // TODO Auto-generated method stub
        waitingDialog.show();
        funcIndex = func_index;
        switch (func_index) {
            case Consts.DEV_SETTINGS_CLOUD:// 云存储
                CloudSwitchTask cloudtask = new CloudSwitchTask();
                String[] params1 = new String[3];
                cloudtask.execute(params1);
                break;
            case Consts.DEV_SETTINGS_ALARM:// 安全防护
                alarmEnabling = enabled;
                if (alarm_way_flag == 1) {
                    Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true, 0x07,
                            0x02,
                            String.format(Consts.FORMATTER_SET_ALARM_ONLY, enabled));
                    new Thread(new TimeOutProcess(Consts.DEV_SETTINGS_ALARM))
                            .start();
                } else {
                    AlarmSwitchTask task = new AlarmSwitchTask();
                    String[] params = new String[3];
                    task.execute(params);
                }
                break;
            case Consts.DEV_SETTINGS_MD:// 移动侦测
                mdEnabling = enabled;
                Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true, 0x06,
                        0x02, String.format(Consts.FORMATTER_SET_MDENABLE, enabled));
                new Thread(new TimeOutProcess(Consts.DEV_SETTINGS_ALARM)).start();
                break;
            case Consts.DEV_ALARAM_SOUND: {// 设备报警声音
                alarmSoundEnabling = enabled;
                MyLog.e("Alarm", "alarm sound start...>>" + enabled);
                Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true, 0x07,
                        0x02,
                        String.format(Consts.FORMATTER_SET_ALARM_SOUND, enabled));
                new Thread(new TimeOutProcess(Consts.DEV_ALARAM_SOUND)).start();// by
                // lkp@15.04.20
                break;
            }
            default:
                break;
        }
    }

    // 设置三种类型参数分别为String,Integer,String
    class AlarmSwitchTask extends AsyncTask<String, Integer, Integer> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {
            int switchRes = -1;// 0成功， 1失败，1000离线
            try {
                if (null == device) {
                    device = deviceList.get(deviceIndex);
                }
                if (JVDeviceConst.DEVICE_SERVER_ONLINE == device
                        .getServerState()) {
                    int sendTag = 0;
                    if (JVDeviceConst.DEVICE_SWITCH_OPEN == device
                            .getAlarmSwitch()) {
                        sendTag = JVDeviceConst.DEVICE_SWITCH_CLOSE;
                    } else if (JVDeviceConst.DEVICE_SWITCH_CLOSE == device
                            .getAlarmSwitch()) {
                        sendTag = JVDeviceConst.DEVICE_SWITCH_OPEN;
                    }
                    switchRes = DeviceUtil.saveSettings(
                            JVDeviceConst.DEVICE_ALARTM_SWITCH, sendTag,
                            activity.statusHashMap.get(Consts.KEY_USERNAME),
                            device.getFullNo());
                } else {
                    switchRes = 1000;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return switchRes;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            // 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
            waitingDialog.dismiss();
            if (0 == result) {
                try {
                    activity.initDevParamObject.put("bAlarmEnable",
                            alarmEnabled);
                    // TODO
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                StatService.trackCustomEvent(activity, "Alarm", activity
                        .getResources().getString(R.string.census_alarm2));
                if (JVDeviceConst.DEVICE_SWITCH_OPEN == device.getAlarmSwitch()) {
                    device.setAlarmSwitch(JVDeviceConst.DEVICE_SWITCH_CLOSE);
                    MySharedPreference.putBoolean("Safeprotect", false);
                    mainListener.onMainAction(JVNetConst.RC_EXTEND,
                            JVNetConst.RC_EX_ALARM, JVNetConst.EX_ALARM_SUBMIT,
                            funcIndex, 0, null);
                    showTextToast(R.string.protect_close_succ);
                } else if (JVDeviceConst.DEVICE_SWITCH_CLOSE == device
                        .getAlarmSwitch()) {
                    device.setAlarmSwitch(JVDeviceConst.DEVICE_SWITCH_OPEN);
                    MySharedPreference.putBoolean("Safeprotect", true);
                    mainListener.onMainAction(JVNetConst.RC_EXTEND,
                            JVNetConst.RC_EX_ALARM, JVNetConst.EX_ALARM_SUBMIT,
                            funcIndex, 1, null);
                    showTextToast(R.string.protect_open_succ);
                }
                deviceList.set(deviceIndex, device);
                // [{"fullNo":"S52942216","port":0,"hasWifi":1,"isDevice":0,"no":52942216,"is05":false,"onlineState":-1182329167,"channelList":[{"channel":1,"channelName":"S52942216_1","index":0}],"isHomeProduct":false,"ip":"","pwd":"123","nickName":"S52942216","deviceType":2,"alarmSwitch":1,"gid":"S","user":"abc","serverState":1,"doMain":""}]
                CacheUtil.saveDevList(deviceList);

                deviceList = CacheUtil.getDevList();
                device = deviceList.get(deviceIndex);
                Log.e("Alarm", "alarm enable : " + device.getAlarmSwitch());
            } else if (1000 == result) {
                // showTextToast(R.string.device_offline);
                showTextToast(R.string.str_operation_failed);
            } else {
                if (JVDeviceConst.DEVICE_SWITCH_OPEN == device.getAlarmSwitch()) {
                    // showTextToast(R.string.protect_close_fail);
                    showTextToast(R.string.str_operation_failed);
                } else if (JVDeviceConst.DEVICE_SWITCH_CLOSE == device
                        .getAlarmSwitch()) {
                    // showTextToast(R.string.protect_open_fail);
                    showTextToast(R.string.str_operation_failed);
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

    @Override
    public void OnFuncSelected(int func_index, String params) {
        // TODO Auto-generated method stub
        funcIndex = func_index;
        switch (func_index) {
            case JVNetConst.ALARMCRY:
                Intent Cryintent = new Intent(DeviceSettingsActivity.this,
                        JVAlarmCryActivity.class);
                Cryintent.putExtra("window", window);
                startActivity(Cryintent);
                break;
            case JVNetConst.JVN_GET_USERINFO:
                createDialog("", false);
                JSONObject paraObject;
                try {
                    paraObject = new JSONObject(params);
                    String userName = paraObject.getString("userName");
                    String userPwd = paraObject.getString("userPwd");
                    String desUTF8 = descript;

                    // String desGBK = "";
                    // try {
                    // desGBK = new String(desUTF8.getBytes(), "GBK");
                    // } catch (UnsupportedEncodingException e) {
                    // e.printStackTrace();
                    // }

                    byte[] paramByte = new byte[Consts.SIZE_ID + Consts.SIZE_PW
                            + Consts.SIZE_DESCRIPT];
                    byte[] userNameByte = userName.getBytes();
                    byte[] userPwdByte = userPwd.getBytes();
                    byte[] desByte = desUTF8.getBytes("GBK");// 设备端是GBK编码
                    MyLog.e("byte-1", "userNameByte.length=" + userNameByte.length);
                    MyLog.e("byte-2", "userPwdByte.length=" + userPwdByte.length);
                    MyLog.e("byte-3", "desByte.length=" + desByte.length);
                    System.arraycopy(userNameByte, 0, paramByte, 0,
                            userNameByte.length);
                    System.arraycopy(userPwdByte, 0, paramByte, Consts.SIZE_ID,
                            userPwdByte.length);
                    System.arraycopy(desByte, 0, paramByte, Consts.SIZE_ID
                            + Consts.SIZE_PW, desByte.length);
                    MyLog.e("byte-4", "paramByte.length=" + paramByte.length);
                    MyLog.e("byte-5", "paramByte=" + paramByte.toString());

                    int sendPower = 0x04 | power;

                    MyLog.e("power-send", "" + power);
                    // 2014-12-25 修改设备用户名密码
                    // //CALL_TEXT_DATA: 165, 0, 81,
                    // {"extend_arg1":58,"extend_arg2":0,"extend_arg3":0,"extend_type":6,"flag":0,"packet_count":4,"packet_id":0,"packet_length":0,"packet_type":6,"type":81}
                    Jni.sendSuperBytes(window, JVNetConst.JVN_RSP_TEXTDATA, true,
                            Consts.RC_EX_ACCOUNT, Consts.EX_ACCOUNT_MODIFY, power,
                            0, 0, paramByte, paramByte.length);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                break;
            case Consts.DEV_SETTINGS_ALARMTIME:// 防护时间段
                currentMenu.setText(R.string.str_protected_time);
                DevSettingsAlarmTimeFragment alarmTimeFragment = new DevSettingsAlarmTimeFragment();
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                // Replace whatever is in the fragment_container
                // view
                // with this
                // fragment,
                // and add the transaction to the back stack so the
                // user
                // can
                // navigate back
                JSONObject paramObject = null;
                try {
                    paramObject = new JSONObject(params);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    if (paramObject == null) {
                        paramObject = new JSONObject();
                    }
                    try {
                        paramObject.put("st", "00:00");
                        paramObject.put("et", "23:59");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }

                }
                Bundle bundle1 = new Bundle();
                bundle1.putString("START_TIME",
                        paramObject.optString("st", "00:00"));
                bundle1.putString("END_TIME", paramObject.optString("et", "23:59"));
                alarmTimeFragment.setArguments(bundle1);
                transaction.replace(R.id.fragment_container, alarmTimeFragment);
                // transaction.addToBackStack(null);
                transaction.remove(deviceSettingsMainFragment);
                fragment_tag = 1;
                // Commit the transaction
                transaction.commitAllowingStateLoss();
                break;
            case Consts.DEV_RESET_DEVICE: // 重置设备功能
                Jni.sendSuperBytes(window, JVNetConst.JVN_RSP_TEXTDATA, true,
                        Consts.RC_EX_FIRMUP, Consts.EX_FIRMUP_RESTORE,
                        Consts.FIRMUP_HTTP, 0, 0, new byte[0], 0);
                // 关闭播放界面
                JVPlayActivity.mInstance.finish();
                // 关闭设备设置的索引页
                JVDeviceSettingsReferenceActivity.mInstance.finish();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
                break;
            case Consts.DEV_RESTART_DEVICE: // 重启设备功能
                Jni.sendSuperBytes(window, JVNetConst.JVN_RSP_TEXTDATA, true,
                        Consts.RC_EX_FIRMUP, Consts.EX_FIRMUP_REBOOT,
                        Consts.FIRMUP_HTTP, 0, 0, new byte[0], 0);
                // 关闭播放界面
                JVPlayActivity.mInstance.finish();
                // 关闭设备设置的索引页
                JVDeviceSettingsReferenceActivity.mInstance.finish();
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                finish();
                break;
            case JVNetConst.TIME_ZONE:
                MySharedPreference.putString("TIMEZONE", "");
                Intent timeIntent = new Intent(activity, JVTimeZoneActivity.class);
                timeIntent.putExtra("timezone", timezones);
                timeIntent.putExtra("window", window);
                startActivity(timeIntent);
                break;
            case JVNetConst.SET_TIME:
                Intent timesIntent = new Intent(activity, JVSetTimeActivity.class);
                timesIntent.putExtra("timetype", params);
                timesIntent.putExtra("window", window);
                timesIntent.putExtra("nTimeFormat", nTimeFormat);
                startActivity(timesIntent);
                break;
            case JVNetConst.OVERTURN:
                // Intent RecodeIntent = new Intent(activity,
                // JVOverTurnActivity.class);
                // RecodeIntent.putExtra("window", window);
                // RecodeIntent.putExtra("streamMap", streamMap);
                // startActivity(RecodeIntent);
                break;
            case JVNetConst.OVERTURNIMG:
                int send = 0;
                String turnParam = "";
                if (Consts.SCREEN_NORMAL == MySharedPreference.getInt("screentag")) {
                    send = 0x04 | effect_flag;
                } else if (Consts.SCREEN_OVERTURN == MySharedPreference
                        .getInt("screentag")) {
                    send = (~0x04) & effect_flag;// + Consts.SCREEN_NORMAL;
                }
                turnParam = "effect_flag=" + send;
                MyLog.i("OverTurn", "turnParam=" + turnParam);
                createDialog("", false);
                Jni.rotateVideo(window, JVNetConst.JVN_RSP_TEXTDATA, turnParam);
                Jni.sendTextData(window, JVNetConst.JVN_RSP_TEXTDATA, 8,
                        JVNetConst.JVN_STREAM_INFO);

                break;
            case JVNetConst.DECODETYPE:
                Intent DecodeIntent = new Intent(activity,
                        JVDecodeTypeActivity.class);
                DecodeIntent.putExtra("window", window);
                startActivity(DecodeIntent);
                break;

            case JVNetConst.SAVEMANAGER:
                Intent SaveIntent = new Intent(activity,
                        JVSaveManagerActivity.class);
                SaveIntent.putExtra("window", window);
                startActivity(SaveIntent);
                break;
            case JVNetConst.MOVESETTING:
                Intent MoveIntent = new Intent(activity,
                        JVMoveSettingActivity.class);
                MoveIntent.putExtra("window", window);
                startActivity(MoveIntent);
                break;
            default:
                break;
        }
    }

    // 绑定接口
    @Override
    public void onAttachFragment(Fragment fragment) {
        try {
            mainListener = (OnMainListener) fragment;
        } catch (Exception e) {
            throw new ClassCastException(this.toString()
                    + " must implement OnMainListener");
        }
        super.onAttachFragment(fragment);
    }

    public interface OnMainListener {
        public void onMainAction(int packet_type, int packet_subtype,
                int ex_type, int func_index, int destFlag, String Content);

        public void onFuncAction(int func_index, int nowFlag, int destFlag);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_left:
                switch (fragment_tag) {
                    case 0:
                        // 更新一级页面(索引界面)的码流信息
                        JVDeviceSettingsReferenceActivity.mInstance
                                .updateStream(initDevParamObject);
                        finish();
                        // 设置切换动画，从左边进入,右边退出
                        overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
                        break;
                    case 1:
                        currentMenu.setText(mCurrentMenuTitle);
                        // onBackPressed();
                        deviceSettingsMainFragment = new DeviceSettingsMainFragment();
                        Bundle bundle1 = new Bundle();
                        bundle1.putString("KEY_PARAM", initDevParamObject.toString());
                        bundle1.putString("jsontag", JSONTAG);
                        deviceSettingsMainFragment.setArguments(bundle1);
                        FragmentManager fm = getSupportFragmentManager();
                        FragmentTransaction ft = getSupportFragmentManager()
                                .beginTransaction();
                        ft.replace(R.id.fragment_container, deviceSettingsMainFragment)
                                .commitAllowingStateLoss();
                        fragment_tag = 0;
                        break;
                    default:
                        break;
                }

                break;

            default:
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            switch (fragment_tag) {
                case 0:
                    // 更新一级页面(索引界面)的码流信息
                    JVDeviceSettingsReferenceActivity.mInstance
                            .updateStream(initDevParamObject);
                    finish();
                    // 设置切换动画，从左边进入,右边退出
                    overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
                    break;
                case 1:
                    currentMenu.setText(R.string.str_audio_monitor);
                    // onBackPressed();
                    deviceSettingsMainFragment = new DeviceSettingsMainFragment();
                    Bundle bundle1 = new Bundle();
                    bundle1.putString("KEY_PARAM", initDevParamObject.toString());
                    bundle1.putString("jsontag", JSONTAG);
                    deviceSettingsMainFragment.setArguments(bundle1);
                    FragmentManager fm = getSupportFragmentManager();
                    FragmentTransaction ft = getSupportFragmentManager()
                            .beginTransaction();
                    ft.replace(R.id.fragment_container, deviceSettingsMainFragment)
                            .commitAllowingStateLoss();
                    fragment_tag = 0;
                    break;
                default:
                    break;
            }
        }
        return true;
    }

    class MyHandler extends Handler {
        @Override
        public void dispatchMessage(Message msg) {
            String strDescString = "";
            switch (msg.what) {
                case Consts.DEV_ALARAM_SOUND:
                case Consts.DEV_SETTINGS_ALARM:
                    strDescString = getResources().getString(
                            R.string.str_setdev_params_timeout);
                    bGetStreamInfoRes = false;
                    Jni.sendTextData(window, JVNetConst.JVN_RSP_TEXTDATA, 8,
                            JVNetConst.JVN_STREAM_INFO);

                    new Thread(new TimeOutProcess(JVNetConst.JVN_STREAM_INFO))
                            .start();// by lkp这地方不知道为啥屏蔽了。先放开吧
                    return;
                case JVNetConst.JVN_STREAM_INFO:
                    if (!bGetStreamInfoRes) {// 已这个为准
                        strDescString = getResources().getString(
                                R.string.str_setdev_params_timeout);// str_getdev_params_timeout
                    } else {
                        if (waitingDialog != null && waitingDialog.isShowing())
                            waitingDialog.dismiss();
                        return;
                    }
                    break;
                default:
                    break;
            }
            if (waitingDialog != null && waitingDialog.isShowing())
                waitingDialog.dismiss();
            showTextToast(strDescString);
            finish();
        }
    }

    class TimeOutProcess implements Runnable {
        private int tag;

        public TimeOutProcess(int arg1) {
            tag = arg1;
        }

        @Override
        public void run() {
            if (tag == JVNetConst.JVN_STREAM_INFO) {
                Message msg = myHandler.obtainMessage(tag);
                // myHandler.sendMessageDelayed(msg, 15000);
                myHandler.sendEmptyMessageDelayed(tag, 12000);
            } else {
                onFuncOperationFlag = true;
                Message msg = myHandler.obtainMessage(tag);
                myHandler.sendMessageDelayed(msg, 8000);
                // myHandler.sendEmptyMessageDelayed(tag, 5000);
            }

        }
    }

    @Override
    public void OnAlarmTimeSaved(String startTime, String endTime) {
        // TODO Auto-generated method stub
        if (startTime.equals(endTime)) {
            startTime = "00:00";
            endTime = "23:59";
        }
        startTimeSetting = startTime;
        endTimeSetting = endTime;

        waitingDialog.show();
        if (startTime.equals("00:00") && endTime.equals("23:59")) {
            // 全天
            alarmTime0ing = "00:00:00-23:59:59";
            Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true, 0x07,
                    0x02, String.format(Consts.FORMATTER_SET_ALARM_TIME,
                            "00:00:00-23:59:59"));
            new Thread(new TimeOutProcess(Consts.DEV_SETTINGS_ALARM)).start();
        } else {
            alarmTime0ing = String.format("%s:00-%s:00", startTime, endTime);
            MyLog.e("Alarm", "alarmTime0ing:" + alarmTime0ing);
            Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true, 0x07,
                    0x02, String.format(Consts.FORMATTER_SET_ALARM_TIME,
                            alarmTime0ing));
            new Thread(new TimeOutProcess(Consts.DEV_SETTINGS_ALARM)).start();
        }
    }

    @Override
    public void OnAlarmTimeSavedResult(int ret) {
        // TODO Auto-generated method stub
        startTimeSaved = startTimeSetting;
        endTimeSaved = endTimeSetting;
        alarmTime0 = alarmTime0ing;
        Bundle bundle1 = new Bundle();
        try {
            initDevParamObject.put("alarmTime0", startTimeSaved + "-"
                    + endTimeSaved);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        bundle1.putString("KEY_PARAM", initDevParamObject.toString());
        bundle1.putString("jsontag", JSONTAG);
        deviceSettingsMainFragment.setArguments(bundle1);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, deviceSettingsMainFragment)
                .commitAllowingStateLoss();
        fragment_tag = 0;
    }

    /**
     * 设置参数后，重新去获取设备当前参数与要设置的参数去比较，然后做出判断是否成功
     */
    void UpdateSettingsRes(int func_index, HashMap<String, String> map) {
        // 兼容没有返回值的设备
        if (onFuncOperationFlag) {
            if (funcIndex != Consts.DEV_SETTINGS_CLOUD) {// 云存储不需要
                myHandler.removeMessages(Consts.DEV_SETTINGS_ALARM);
                String alarm_enable = map.get("bAlarmEnable");
                funcParamArray[Consts.DEV_SETTINGS_ALARM] = alarm_enable;

                String md_enable = map.get("bMDEnable");
                funcParamArray[Consts.DEV_SETTINGS_MD] = md_enable;

                alarmTime0 = map.get("alarmTime0");// alarmTime0
                funcParamArray[Consts.DEV_SETTINGS_ALARMTIME] = alarmTime0;

                String alarmsound_enable = map.get("bAlarmSound");
                funcParamArray[Consts.DEV_ALARAM_SOUND] = alarmsound_enable;
                onFuncOperationFlag = false;
                switch (func_index) {
                    case Consts.DEV_SETTINGS_ALARM:// 安全防护
                        // alarmEnabling已经是即将要改变的状态,详情见onFuncEnabled
                        if (alarmEnabling == Integer
                                .valueOf(funcParamArray[func_index])) {
                            // 成功
                            // 安全防护或者设置安全防护时间ok
                            alarmEnabled = alarmEnabling;
                            try {
                                initDevParamObject
                                        .put("bAlarmEnable", alarmEnabled);
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            mainListener.onMainAction(JVNetConst.RC_EXTEND,
                                    JVNetConst.RC_EX_ALARM,
                                    JVNetConst.EX_ALARM_SUBMIT, funcIndex,
                                    alarmEnabling, null);
                            // showTextToast("安全防护成功");
                        } else {
                            // 失败
                            showTextToast(getResources().getString(
                                    R.string.str_operation_failed));
                            finish();
                        }
                        break;
                    case Consts.DEV_SETTINGS_MD:// 移动侦测
                        // mdEnabling已经是即将要改变的状态,详情见onFuncEnabled
                        if (mdEnabling == Integer
                                .valueOf(funcParamArray[func_index])) {
                            // 成功
                            // 移动侦测ok
                            mdEnabled = mdEnabling;
                            try {
                                initDevParamObject.put("bMDEnable", mdEnabled);
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            mainListener.onMainAction(JVNetConst.RC_EXTEND,
                                    JVNetConst.RC_EX_MD, JVNetConst.EX_MD_SUBMIT,
                                    funcIndex, mdEnabling, null);
                            // showTextToast("移动侦测成功");
                        } else {
                            // 失败
                            showTextToast(getResources().getString(
                                    R.string.str_operation_failed));
                            finish();
                        }
                        break;
                    case Consts.DEV_SETTINGS_ALARMTIME:
                        // alarmTime0ing已经是即将要改变的状态,详情见OnAlarmTimeSaved
                        if (alarmTime0ing.equals(funcParamArray[func_index])) {
                            // 成功
                            mainListener.onMainAction(JVNetConst.RC_EXTEND,
                                    JVNetConst.RC_EX_ALARM,
                                    JVNetConst.EX_ALARM_SUBMIT, funcIndex, 0, null);
                            // showTextToast("防护时间段成功");
                        } else {
                            // 失败
                            showTextToast(getResources().getString(
                                    R.string.str_operation_failed));
                            finish();
                        }
                        break;
                    case Consts.DEV_ALARAM_SOUND:// 报警声音开关
                        // alarmSoundEnabling已经是即将要改变的状态,详情见onFuncEnabled
                        if (alarmSoundEnabling == Integer
                                .valueOf(funcParamArray[func_index])) {
                            // 成功
                            // 报警声音开关ok
                            alarmSoundEnabled = alarmSoundEnabling;
                            try {
                                initDevParamObject.put("bAlarmSound",
                                        alarmSoundEnabled);
                            } catch (JSONException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            // mainListener.onMainAction(JVNetConst.RC_EXTEND,
                            // JVNetConst.RC_EX_MD, JVNetConst.EX_MD_SUBMIT,
                            // funcIndex, alarmSoundEnabling, null);
                            // showTextToast("移动侦测成功");
                        } else {
                            // 失败
                            showTextToast(getResources().getString(
                                    R.string.str_operation_failed));
                            finish();
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private int ResolveStreamInfo(HashMap<String, String> map) {
        if (map == null) {
            return -1;
        }
        // 先判断MobileQuality
        String MobileQuality = map.get("MobileQuality");
        String MobileCH = null;
        if (null == MobileQuality) {// 没这个字段说明是老设备，再判断MobileCH是否为2
            MobileCH = map.get("MobileCH");
            if (MobileCH != null)// 这种情况，直接不让进设备设置界面
            {
                if (!(MobileCH.equals("2"))) {
                    showTextToast(R.string.not_support_this_func);
                    this.finish();
                    return -1;
                } else {
                    // 只有安全防护，走设备服务器
                    alarm_way_flag = 0;
                }
            } else {
                showTextToast(R.string.not_support_this_func);
                this.finish();
                return -1;
            }
        } else {
            // 如果有，走云视通
            alarm_way_flag = 1;
        }
        if (alarm_way_flag == 1) {
            String alarm_enable = map.get("bAlarmEnable");
            funcParamArray[Consts.DEV_SETTINGS_ALARM] = alarm_enable;

            String md_enable = map.get("bMDEnable");
            funcParamArray[Consts.DEV_SETTINGS_MD] = md_enable;

            alarmTime0 = map.get("alarmTime0");// alarmTime0
            funcParamArray[Consts.DEV_SETTINGS_ALARMTIME] = alarmTime0;

            String alarmsound_enable = map.get("bAlarmSound");
            funcParamArray[Consts.DEV_ALARAM_SOUND] = alarmsound_enable;
            Log.e("Alarm", "ResolveStreamInfo >> " + alarm_enable + "--"
                    + md_enable + "--" + alarmTime0 + "--" + alarmsound_enable);

            if (alarm_enable == null) {
                alarmEnabled = -1;
            } else {
                alarmEnabled = Integer.valueOf(alarm_enable);
            }
            alarmEnabling = alarmEnabled;

            if (md_enable == null) {
                // showTextToast(R.string.not_support_this_func);
                mdEnabled = -1;
            } else {
                mdEnabled = Integer.valueOf(md_enable);
            }
            mdEnabling = mdEnabled;

            if (alarm_enable == null) {
                // showTextToast(R.string.not_support_this_func);
                alarmTime0 = "";
            }
            alarmTime0ing = alarmTime0;

            // 报警声音开关
            if (alarmsound_enable == null) {
                // showTextToast(R.string.not_support_this_func);
                alarmSoundEnabled = -1;
            } else {
                alarmSoundEnabled = Integer.valueOf(alarmsound_enable);
            }
            alarmSoundEnabling = alarmSoundEnabled;
        } else {
            alarmEnabled = device.getAlarmSwitch();
        }

        initDevParamObject = new JSONObject();
        try {
            initDevParamObject.put("bAlarmSound", alarmSoundEnabled);
            initDevParamObject.put("bAlarmEnable", alarmEnabled);
            initDevParamObject.put("bMDEnable", mdEnabled);
            initDevParamObject.put("alarmTime0", alarmTime0);
            initDevParamObject.put("alarmWay", alarm_way_flag);
            /*
            int iCloudSwitch = Integer.valueOf(statusHashMap
                    .get(Consts.MORE_CLOUD_SWITCH));
            initDevParamObject.put("bCloudEnabled",
                    iCloudSwitch == 1 ? device.getCloudEnabled() : -1);// 云存储
                    */
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Check that the activity is using the layout version
        // with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous
            // state,
            // then we don't need to do anything and should
            // return or else
            // we could end up with overlapping fragments.
            // if (savedInstanceState != null) {
            // return;
            // }
            deviceSettingsMainFragment = new DeviceSettingsMainFragment();
            Bundle bundle1 = new Bundle();
            bundle1.putString("KEY_PARAM", initDevParamObject.toString());
            bundle1.putString("fullno", fullno);
            bundle1.putBoolean("isadmin", isadmin);
            bundle1.putInt("power", power);
            bundle1.putString("jsontag", JSONTAG);
            deviceSettingsMainFragment.setArguments(bundle1);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();
            ft.add(R.id.fragment_container, deviceSettingsMainFragment)
                    .commitAllowingStateLoss();
            fragment_tag = 0;
        }
        return 0;

    }

    class CloudSwitchTask extends AsyncTask<String, Integer, Integer> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {
            int switchRes = -1;// 0成功， 1失败，1000离线
            try {
                if (null == device) {
                    device = deviceList.get(deviceIndex);
                }
                // if (JVDeviceConst.DEVICE_SERVER_ONLINE == device
                // .getServerState()) {
                int sendTag = 0;
                if (1 == device.getCloudEnabled()) {
                    sendTag = JVDeviceConst.DEVICE_SWITCH_CLOSE;
                } else {
                    sendTag = JVDeviceConst.DEVICE_SWITCH_OPEN;
                }
                switchRes = DeviceUtil.saveCloudSettings(0, sendTag,
                        statusHashMap.get(Consts.KEY_USERNAME),
                        device.getFullNo());
                // Thread.sleep(1000);
                // switchRes = 0;
                // } else {
                // switchRes = 1000;
                // }

            } catch (Exception e) {
                e.printStackTrace();
            }

            return switchRes;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            // 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
            // dismissDialog();
            waitingDialog.dismiss();
            if (0 == result) {
                // StatService.trackCustomEvent(mActivity, "Alarm", mActivity
                // .getResources().getString(R.string.census_alarm2));
                if (1 == device.getCloudEnabled()) {
                    device.setCloudEnabled(0);
                    mainListener.onFuncAction(Consts.DEV_SETTINGS_CLOUD, 0, 0);
                    showTextToast(R.string.cloud_close_succ);// 关闭OK
                } else {
                    device.setCloudEnabled(1);
                    mainListener.onFuncAction(Consts.DEV_SETTINGS_CLOUD, 1, 1);
                    showTextToast(R.string.cloud_open_succ);
                }
                // [{"fullNo":"S52942216","port":0,"hasWifi":1,"isDevice":0,"no":52942216,"is05":false,"onlineState":-1182329167,"channelList":[{"channel":1,"channelName":"S52942216_1","index":0}],"isHomeProduct":false,"ip":"","pwd":"123","nickName":"S52942216","deviceType":2,"alarmSwitch":1,"gid":"S","user":"abc","serverState":1,"doMain":""}]
                CacheUtil.saveDevList(deviceList);
                statusHashMap.put(Consts.HAG_GOT_DEVICE, "true");
            } else if (1000 == result) {
                showTextToast(R.string.device_offline);
            } else {
                if (1 == device.getCloudEnabled()) {
                    showTextToast(R.string.cloud_close_fail);
                } else {
                    showTextToast(R.string.cloud_open_fail);
                }
            }
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
            // createDialog("", true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新进度,此方法在主线程执行，用于显示任务执行的进度。

        }
    }

}
