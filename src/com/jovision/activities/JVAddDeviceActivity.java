
package com.jovision.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import barcode.zxing.activity.MipcaActivityCapture;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.bean.Device;
import com.jovision.commons.MyLog;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.DeviceUtil;
import com.jovision.utils.PlayUtil;
import com.umeng.socialize.utils.Log;

import org.json.JSONObject;

import java.util.ArrayList;

public class JVAddDeviceActivity extends BaseActivity {

    private static final String TAG = "JVAddDeviceActivity";
    /** add device layout */
    private EditText devNumET;
    private EditText nickET;
    private EditText userET;
    private EditText pwdET;
    private String nickString;
    private Button saveBtn;

    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private Device addDevice;
    private Boolean qrAdd = false;// 是否二维码扫描添加设备

    private boolean hasBroadIP = false;// 是否广播完IP
    private int onLine = 0;
    private String ip = "";
    private int port = 0;
    int channelCount = -1;
    /** 传递过来的云视通账号 nihy */
    private String devNumET_cloudseeId = "";

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
        Intent intent = getIntent();
        if (null != intent) {
            devNumET_cloudseeId = intent.getStringExtra("devNumET_cloudseeid");
            if (null == devNumET_cloudseeId) {
                devNumET_cloudseeId = "";
            }
        }

        deviceList = CacheUtil.getDevList();
        qrAdd = intent.getBooleanExtra("QR", false);
        if (qrAdd) {
            Intent openCameraIntent = new Intent(JVAddDeviceActivity.this,
                    MipcaActivityCapture.class);
            openCameraIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            JVAddDeviceActivity.this
                    .startActivityForResult(openCameraIntent, 0);
        }
    }

    @Override
    protected void initUi() {
        setContentView(R.layout.adddevice_layout);
        /** top bar */
        leftBtn = (Button) findViewById(R.id.btn_left);
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        rightBtn = (Button) findViewById(R.id.btn_right);
        rightBtn.setBackgroundResource(R.drawable.qr_icon);
        currentMenu.setText(R.string.str_help1_1);
        leftBtn.setOnClickListener(mOnClickListener);
        rightBtn.setOnClickListener(mOnClickListener);
        rightBtn.setVisibility(View.GONE);

        /** add device layout */
        devNumET = (EditText) findViewById(R.id.ystnum_et);
        userET = (EditText) findViewById(R.id.user_et);
        pwdET = (EditText) findViewById(R.id.pwd_et);
        nickET = (EditText) findViewById(R.id.nick_et);
        saveBtn = (Button) findViewById(R.id.save_btn);

        Log.i("TAG", devNumET_cloudseeId);
        devNumET.setText(devNumET_cloudseeId);
        if (null != devNumET_cloudseeId
                && !"".equalsIgnoreCase(devNumET_cloudseeId)) {
            devNumET.setEnabled(false);
            devNumET.setBackgroundColor(getResources().getColor(R.color.my_bg));
        }
        userET.setText(Consts.DEFAULT_USERNAME);
        pwdET.setText(Consts.DEFAULT_PASSWORD);
        saveBtn.setBackgroundResource(R.drawable.blue_bg);
        saveBtn.setOnClickListener(mOnClickListener);
    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_left: {
                    CacheUtil.saveDevList(deviceList);
                    JVAddDeviceActivity.this.finish();
                    break;
                }
                /** 二维码扫描添加设备 */
                case R.id.btn_right: {
                    Intent openCameraIntent = new Intent(JVAddDeviceActivity.this,
                            MipcaActivityCapture.class);
                    openCameraIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    JVAddDeviceActivity.this.startActivityForResult(
                            openCameraIntent, 0);
                    break;
                }
                /** 保存 */
                case R.id.save_btn: {
                    String devNum = devNumET.getText().toString().toUpperCase();
                    String userName = userET.getText().toString();
                    String userPwd = pwdET.getText().toString();
                    nickString = nickET.getText().toString();
                    saveMethod(devNum, userName, userPwd, nickString);
                    break;
                }
                default:
                    break;
            }
        }

    };

    /**
     * 保存设备信息
     * 
     * @param devNum
     * @param userName
     * @param userPwd
     */
    public void saveMethod(String devNum, String userName, String userPwd,
            String nickName) {
        if (null == deviceList) {
            deviceList = new ArrayList<Device>();
        }
        int size = deviceList.size();
        if ("".equalsIgnoreCase(devNum)) {// 云视通号不可为空
            showTextToast(R.string.login_str_device_ytnum_notnull);
            return;
        } else if (!ConfigUtil.checkYSTNum(devNum)) {// 验证云视通号是否合法
            showTextToast(R.string.increct_yst_tips);
            return;
        } else if (!"".equals(nickName) && !ConfigUtil.checkNickName(nickName)) {// 昵称不合法
            showTextToast(R.string.login_str_nike_name_order);
            return;
        } else if ("".equalsIgnoreCase(userName)) {// 用户名不可为空
            showTextToast(R.string.login_str_device_account_notnull);
            return;
        } else if (!ConfigUtil.checkDeviceUsername(userName)) {// 用户名是否合法
            showTextToast(R.string.login_str_device_account_error);
            return;
        } else if (!ConfigUtil.checkDevicePwd(userPwd)) {
            showTextToast(R.string.login_str_device_pass_error);
            return;
        } else if (size >= 100
                && !Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN))) {// 非本地多于100个设备不让再添加
            showTextToast(R.string.str_device_most_count);
            return;
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
                return;
            }
        }

        AddDevTask task = new AddDevTask();
        String[] strParams = new String[4];
        strParams[0] = ConfigUtil.getGroup(devNum);
        strParams[1] = String.valueOf(ConfigUtil.getYST(devNum));
        strParams[2] = String.valueOf(2);
        strParams[3] = nickName;
        task.execute(strParams);
    }

    @Override
    public void onBackPressed() {
        // Intent intent = new Intent();
        // setResult(ADD_DEV_FAILED, intent);
        CacheUtil.saveDevList(deviceList);
        JVAddDeviceActivity.this.finish();
        super.onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Consts.WHAT_BARCODE_RESULT) {
            Bundle bundle = data.getExtras();
            String scanResult = bundle.getString("result");
            devNumET.setText(scanResult);
            if (!"".equals(scanResult)) {
                devNumET.setEnabled(false);
                devNumET.setBackgroundColor(getResources().getColor(
                        R.color.my_bg));
            }
        } else {
            JVAddDeviceActivity.this.finish();
        }

    }

    @Override
    protected void saveSettings() {

    }

    @Override
    protected void freeMe() {

    }

    // 设置三种类型参数分别为String,Integer,String
    class AddDevTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {

            boolean isright = false;
            String nickName = params[3];
            int addRes = -1;
            boolean localFlag = Boolean.valueOf(statusHashMap
                    .get(Consts.LOCAL_LOGIN));
            try {
                MyLog.e(TAG, "getChannelCount E = ");

                // 非3G广播获取通道数量
                if (PlayUtil.broadCast(JVAddDeviceActivity.this)) {
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
                    isright = false;
                } else {
                    isright = true;
                }

                addDevice = new Device("", 0, params[0],
                        Integer.parseInt(params[1]), userET.getText()
                                .toString(), pwdET.getText().toString(), false,
                        channelCount, 0, nickName);
                if (isright) {
                    addDevice.setChannelBindFlag(0);
                } else {
                    addDevice.setChannelBindFlag(1);
                }
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
                Intent intent = new Intent();
                setResult(Consts.DEVICE_ADD_SUCCESS_RESULT, intent);
                JVAddDeviceActivity.this.finish();

            } else {
                showTextToast(R.string.add_device_failed);
            }
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
            createDialog("", false);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新进度,此方法在主线程执行，用于显示任务执行的进度。
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
