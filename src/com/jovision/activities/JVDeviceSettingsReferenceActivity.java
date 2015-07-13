
package com.jovision.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.bean.Device;
import com.jovision.commons.JVNetConst;
import com.jovision.commons.MyLog;
import com.jovision.utils.CacheUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class JVDeviceSettingsReferenceActivity extends ShakeActivity {

    private final String TAG = "JVDeviceSettingsReferenceActivity";

    protected static JVDeviceSettingsReferenceActivity mInstance;
    private FragmentManager mFragmentManager;
    // 本地登陆标志位
    private boolean mLocalFlag = false;

    /** intent 传递过来的信息 **/
    private Bundle extras;
    private int window = 0;
    public int deviceIndex = 0;
    public static boolean isadmin;
    public String descript;
    public static String fullno;
    private int power;
    private HashMap<String, String> streamMap;
    private boolean update_flag = false;
    public Device device;
    public ArrayList<Device> deviceList;

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
        // 连接结果
            case Consts.CALL_CONNECT_CHANGE:
                if (window != arg1) {
                    MyLog.e("DeviceSetting", "---------不是本window的回调---------->"
                            + arg1);
                    return;
                }
                switch (arg2) {
                    case JVNetConst.CONNECT_FAILED:
                    case JVNetConst.ABNORMAL_DISCONNECT:
                    case JVNetConst.DISCONNECT_OK:
                    case JVNetConst.SERVICE_STOP:
                        showTextToast(R.string.str_alarm_connect_except);
                        // 关闭设备设置的索引页
                        finish();
                        break;
                    default:
                        showTextToast(R.string.str_alarm_connect_except);
                        // finish();
                        break;
                }
                break;
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {
        mInstance = this;
        mFragmentManager = getSupportFragmentManager();
        mLocalFlag = Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN));

        // intent 传递过来信息
        getIntentDatas();
    }

    /**
     * 获取从intent传递过来的信息
     */
    private void getIntentDatas() {
        extras = getIntent().getExtras();
        window = extras.getInt("window");
        deviceIndex = extras.getInt("deviceIndex");
        isadmin = extras.getBoolean("isadmin");
        descript = extras.getString("descript");
        fullno = extras.getString("fullno");
        power = extras.getInt("power");
        streamMap = (HashMap<String, String>) extras
                .getSerializable("streamMap");
        update_flag = extras.getBoolean("updateflag");

    }

    @Override
    protected void initUi() {
        setContentView(R.layout.dev_settings_main);
        initViews();
        initListeners();
        initDatas();
        deviceList = CacheUtil.getDevList();
        if (0 != deviceList.size()) {
            device = deviceList.get(deviceIndex);
        }
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
    }

    /**
     * 初始化监听事件
     */
    private void initListeners() {
        leftBtn.setOnClickListener(myOnClickListener);
    }

    /**
     * 初始化数据及一些设置操作
     */
    private void initDatas() {
        // 标题
        currentMenu.setText(R.string.str_audio_monitor);
        // 加载fragment
        JVDeviceSettingsReferenceFragment fragment = new JVDeviceSettingsReferenceFragment();
        fragment.setArguments(extras);
        replaceFragment(fragment);
    }

    @Override
    public void onBackPressed() {
        executeAnimLeftinRightout();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * click事件处理
     */
    OnClickListener myOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left:
                    executeAnimLeftinRightout();
                    break;
                default:
            }
        }
    };

    // --------------------------------------
    // ## 存储管理
    // --------------------------------------
    protected void functionStorage() {
        Intent SaveIntent = new Intent(JVDeviceSettingsReferenceActivity.this,
                JVSaveManagerActivity.class);
        SaveIntent.putExtra("window", window);
        startActivity(SaveIntent);
        executeAnimRightinLeftout();
    }

    // --------------------------------------
    // ## 从二级界面返回的时候会更新码流信息
    // --------------------------------------
    protected void updateStream(JSONObject object) {
        if (object != null) {
            try {
                streamMap.put("bAlarmSound", object.getString("bAlarmSound"));
                streamMap.put("bAlarmEnable", object.getString("bAlarmEnable"));
                streamMap.put("bMDEnable", object.getString("bMDEnable"));
                streamMap.put("alarmTime0", object.getString("alarmTime0"));
                streamMap.put("alarmWay", object.getString("alarmWay"));
            } catch (JSONException e) {
                MyLog.e(TAG, "--json parse error--");
            }
        } else {
            MyLog.e(TAG, "--json object is null--");
        }
    }

    /**
     * 替换fragment
     * 
     * @param fragment
     */
    private void replaceFragment(BaseFragment fragment) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Activity切换动画 - 左进右出
     */
    protected void executeAnimLeftinRightout() {
        mInstance.finish();
        // 设置切换动画，从左边进入,右边退出
        overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
    }

    /**
     * Activity切换动画 - 右进左退
     */
    protected void executeAnimRightinLeftout() {
        // 设置切换动画，从右边进入,左边退出
        overridePendingTransition(R.anim.enter_right, R.anim.exit_left);
    }

}
