
package com.jovision.activities;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.commons.JVNetConst;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.ConfigUtil;
import com.jovision.views.popSaveManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;

public class JVSaveManagerActivity extends BaseActivity {

    private int window;
    private float TotalSize;
    private float UsedSize;
    private float Percent;
    private TextView save_TotalSize;
    private TextView save_RecodeStatus;
    private TextView save_UsedStatus;
    private TextView save_nMode;
    private ImageView save_img;
    private popSaveManager popupWindow; // 声明PopupWindow对象；
    private RelativeLayout recode_chose;
    private TextView savemanager_save;
    private TextView format;
    private int recodeCount;
    private boolean isstop = false;
    private boolean ispull = true;
    private boolean isfind = true;
    private boolean isget = true;

    private HashMap<String, String> streamMap;
    private Dialog resetDialog;// 显示弹出框
    private TextView resetCancel;// 取消按钮
    private TextView resetCompleted;// 确定按钮

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stubz
        switch (what) {
            case Consts.CALL_TEXT_DATA:
                switch (arg2) {
                    case JVNetConst.JVN_RSP_TEXTDATA:// 文本数据
                        String allStr = obj.toString();
                        MyLog.v("JVSaveManager", "文本数据--" + allStr);
                        JSONObject dataObj;
                        try {
                            dataObj = new JSONObject(allStr);
                            switch (dataObj.getInt("flag")) {
                                case JVNetConst.JVN_STREAM_INFO:// 3-- 码流配置请求
                                    MyLog.i("JVSaveManager", "JVN_STREAM_INFO:TEXT_DATA: "
                                            + what + ", " + arg1 + ", " + arg2 + ", " + obj);
                                    String streamJSON = dataObj.getString("msg");
                                    // HashMap<String, String> streamCH1 =
                                    // ConfigUtil.getCH1("CH1",streamJSON);
                                    // HashMap<String, String>
                                    streamMap = ConfigUtil.genMsgMap(streamJSON);
                                    if (null != streamMap) {
                                        if (null != streamMap.get("bRecEnable")
                                                && !"".equalsIgnoreCase(streamMap
                                                        .get("bRecEnable"))) {
                                            int bRecEnable = Integer.parseInt(streamMap
                                                    .get("bRecEnable"));
                                            MyLog.v("JVSaveManager", "bRecEnable = "
                                                    + streamMap.get("bRecEnable"));
                                            MySharedPreference.putInt("bRecEnable",
                                                    bRecEnable);
                                        }
                                    }
                                    if (null != streamMap) {
                                        if (null != streamMap.get("bRecAlarmEnable")
                                                && !"".equalsIgnoreCase(streamMap
                                                        .get("bRecAlarmEnable"))) {
                                            int bRecAlarmEnable = Integer
                                                    .parseInt(streamMap
                                                            .get("bRecAlarmEnable"));
                                            MyLog.v("JVSaveManager", "bRecAlarmEnable = "
                                                    + streamMap.get("bRecAlarmEnable"));
                                            MySharedPreference.putInt("bRecAlarmEnable",
                                                    bRecAlarmEnable);
                                        }
                                    }
                                    if (null != streamMap.get("storageMode")
                                            && !"".equalsIgnoreCase(streamMap
                                                    .get("storageMode"))) {
                                        MyLog.v("JVSaveManager",
                                                "storageMode="
                                                        + streamMap.get("storageMode"));
                                        MySharedPreference.putInt("storageMode", Integer
                                                .parseInt(streamMap.get("storageMode")));
                                    }
                                    if (0 != (MySharedPreference.getInt("bRecEnable") + MySharedPreference
                                            .getInt("bRecAlarmEnable"))) {
                                        if (1 == MySharedPreference.getInt("storageMode")) {
                                            save_nMode.setText(getResources().getString(
                                                    R.string.video_normal));
                                            recodeCount = 1;
                                        }
                                        if (2 == MySharedPreference.getInt("storageMode")) {
                                            save_nMode.setText(getResources().getString(
                                                    R.string.video_alarm));
                                            recodeCount = 2;
                                        }
                                    } else {
                                        save_nMode.setText(getResources().getString(
                                                R.string.video_stop));
                                        recodeCount = 0;
                                        isstop = true;
                                    }
                                    break;
                                case JVNetConst.JVN_WIFI_IS_SETTING:// -- 6
                                    dismissDialog();
                                    showTextToast(getResources().getString(
                                            R.string.str_formatsuccess));
                                    finish();
                                    break;
                                case JVNetConst.JVN_RECORD_RESULT:// -- 100
                                    isget = false;
                                    if (recodeCount == 1) {
                                        MySharedPreference.putInt("storageMode", 1);
                                        MySharedPreference.putInt("bRecAlarmEnable", 0);
                                        MySharedPreference.putInt("bRecEnable", 1);
                                    } else if (recodeCount == 2) {
                                        MySharedPreference.putInt("storageMode", 2);
                                        MySharedPreference.putInt("bRecAlarmEnable", 1);
                                        MySharedPreference.putInt("bRecEnable", 0);
                                    }
                                    dismissDialog();
                                    finish();
                                    break;
                                case JVNetConst.JVN_DEVICE_SDCARD_STATE: {// --
                                    isget = false;
                                    dismissDialog();
                                    // 7
                                    // SD卡存储容量状态
                                    // 2015.5.6
                                    MyLog.i("JVN_DEVICE_SDCARD",
                                            "JVN_DEVICE_SDCARD_STATE: " + what + ", "
                                                    + arg1 + ", " + arg2 + ", " + obj);
                                    // JVN_DEVICE_SDCARD_STATE: 165, 0, 81,
                                    // {"extend_arg1":76,"extend_arg2":0,
                                    // "extend_arg3":0,"extend_type":1,"flag":7,
                                    // "msg":"nStorage=1;[STORAGE1];nTotalSize=7623;nUsedSize=254;nStatus=3;storageMode=1;",
                                    // "packet_count":3,"packet_id":0,"packet_length":0,"packet_type":6}

                                    String sdCardJSON = dataObj.getString("msg");
                                    if ("".equals(sdCardJSON)) {
                                        save_TotalSize.setText(getResources().getString(
                                                R.string.str_noSD));
                                        ispull = false;
                                        isstop = false;
                                        isfind = false;
                                    }
                                    HashMap<String, String> sdCardMap = ConfigUtil
                                            .genMsgMap(sdCardJSON);
                                    DecimalFormat fnum = new DecimalFormat("##0.00");
                                    if (null != sdCardMap) {
                                        if (null != sdCardMap.get("nStorage")
                                                && !"".equalsIgnoreCase(sdCardMap
                                                        .get("nStorage"))) {
                                            // 1
                                            // 有SD卡
                                            // 0
                                            // 没有SD卡
                                            if ("1".equalsIgnoreCase(sdCardMap
                                                    .get("nStorage"))) {
                                                if (null != sdCardMap.get("nTotalSize")
                                                        && !"".equalsIgnoreCase(sdCardMap
                                                                .get("nTotalSize"))) {
                                                    float num = Float.valueOf(sdCardMap
                                                            .get("nTotalSize"));
                                                    TotalSize = Float.valueOf(fnum
                                                            .format(num / 1024));
                                                    save_TotalSize.setText(TotalSize
                                                            + "G"
                                                            + getResources().getString(
                                                                    R.string.str_storage));
                                                }

                                                if (null != sdCardMap.get("nUsedSize")
                                                        && !"".equalsIgnoreCase(sdCardMap
                                                                .get("nUsedSize"))) {
                                                    float num = Float.valueOf(sdCardMap
                                                            .get("nUsedSize"));
                                                    UsedSize = Float.valueOf(fnum
                                                            .format(num / 1024));
                                                }
                                                if (null != sdCardMap.get("storageMode")
                                                        && !"".equalsIgnoreCase(sdCardMap
                                                                .get("storageMode"))) {
                                                    int storageMode = Integer
                                                            .valueOf(sdCardMap
                                                                    .get("storageMode"));
                                                    switch (storageMode) {
                                                        case 0:
                                                            save_nMode
                                                                    .setText(getResources()
                                                                            .getString(
                                                                                    R.string.video_stop));
                                                            recodeCount = 0;
                                                            isstop = true;
                                                            break;
                                                        case 1:
                                                            save_nMode
                                                                    .setText(getResources()
                                                                            .getString(
                                                                                    R.string.video_normal));
                                                            recodeCount = 1;
                                                            break;
                                                        case 2:
                                                            save_nMode
                                                                    .setText(getResources()
                                                                            .getString(
                                                                                    R.string.video_alarm));
                                                            recodeCount = 2;
                                                            break;

                                                        default:
                                                            break;
                                                    }
                                                }
                                                if (null != sdCardMap.get("nStatus")
                                                        && !"".equalsIgnoreCase(sdCardMap
                                                                .get("nStatus"))) {
                                                    int status = Integer.valueOf(sdCardMap
                                                            .get("nStatus"));

                                                    switch (status) {
                                                        case 0:
                                                            save_RecodeStatus
                                                                    .setText(getResources()
                                                                            .getString(
                                                                                    R.string.str_recodestatusfour));
                                                            break;
                                                        case 2:
                                                            save_RecodeStatus
                                                                    .setText(getResources()
                                                                            .getString(
                                                                                    R.string.str_recodestatusthree));
                                                            break;
                                                        case 3:
                                                            if (2 == MySharedPreference
                                                                    .getInt("DeviceType")) {
                                                                if (0 == MySharedPreference
                                                                        .getInt("HomeIPC")) {
                                                                    save_nMode
                                                                            .setText(getResources()
                                                                                    .getString(
                                                                                            R.string.str_start_record));
                                                                    recodeCount = 2;
                                                                    isstop = false;
                                                                }
                                                            }
                                                            save_RecodeStatus
                                                                    .setText(getResources()
                                                                            .getString(
                                                                                    R.string.str_recodestatusone));
                                                            break;
                                                        case 4:
                                                            if (2 == MySharedPreference
                                                                    .getInt("DeviceType")) {
                                                                if (0 == MySharedPreference
                                                                        .getInt("HomeIPC")) {
                                                                    save_nMode
                                                                            .setText(getResources()
                                                                                    .getString(
                                                                                            R.string.video_stop));
                                                                    recodeCount = 0;
                                                                    isstop = true;
                                                                }
                                                            }
                                                            save_RecodeStatus
                                                                    .setText(getResources()
                                                                            .getString(
                                                                                    R.string.str_recodestatustwo));
                                                            break;
                                                        default:
                                                            break;
                                                    }
                                                    Percent = (UsedSize * 100 / TotalSize);
                                                    Percent = (float) (Math
                                                            .round(Percent * 100)) / 100;
                                                    save_UsedStatus.setText(UsedSize
                                                            + "GB/"
                                                            + TotalSize
                                                            + "GB,"
                                                            + getResources().getString(
                                                                    R.string.str_hasused)
                                                            + Percent + "%");
                                                }
                                            } else if ("0".equalsIgnoreCase(sdCardMap
                                                    .get("nStorage"))) {
                                                save_TotalSize.setText(getResources()
                                                        .getString(R.string.str_noSD));
                                                ispull = false;
                                                isstop = false;
                                                isfind = false;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /** 是否确定重置设备 */
    private void ResetDialog() {
        resetDialog = new Dialog(JVSaveManagerActivity.this, R.style.mydialog);
        View view = LayoutInflater.from(JVSaveManagerActivity.this).inflate(
                R.layout.dialog_format, null);
        resetDialog.setContentView(view);

        resetCancel = (TextView) view.findViewById(R.id.reset_cancel);
        resetCompleted = (TextView) view.findViewById(R.id.reset_completed);

        resetCancel.setOnClickListener(MyOnClickListener);
        resetCompleted.setOnClickListener(MyOnClickListener);
        resetDialog.show();
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stub
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {
        // TODO Auto-generated method stub
        setContentView(R.layout.savemanager_layout);
        Bundle extras = getIntent().getExtras();
        window = extras.getInt("window");
    }

    @Override
    protected void initUi() {
        // TODO Auto-generated method stub
        leftBtn = (Button) findViewById(R.id.btn_left);
        rightBtn = (Button) findViewById(R.id.btn_right);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        rightBtn.setVisibility(View.GONE);
        currentMenu.setText(getResources().getText(R.string.str_savemanager));

        recode_chose = (RelativeLayout) findViewById(R.id.recode_chose);
        save_TotalSize = (TextView) findViewById(R.id.save_TotalSize);
        save_RecodeStatus = (TextView) findViewById(R.id.recode_status);
        save_UsedStatus = (TextView) findViewById(R.id.hasused_status);
        save_img = (ImageView) findViewById(R.id.savemanager_img);
        save_nMode = (TextView) findViewById(R.id.save_nmode);
        format = (TextView) findViewById(R.id.format);

        savemanager_save = (TextView) findViewById(R.id.savemanager_save);
        popupWindow = new popSaveManager(JVSaveManagerActivity.this,
                MyOnClickListener);

        savemanager_save.setOnClickListener(MyOnClickListener);
        save_img.setOnClickListener(MyOnClickListener);
        leftBtn.setOnClickListener(MyOnClickListener);
        format.setOnClickListener(MyOnClickListener);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        isget = true;
        Jni.sendTextData(window, JVNetConst.JVN_RSP_TEXTDATA, 8,
                JVNetConst.JVN_STREAM_INFO);
        Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true,
                JVNetConst.RC_EX_STORAGE, JVNetConst.EX_STORAGE_REFRESH, null);
        createDialog("", false);
        handler.postDelayed(runnableout, 40000);
    }

    private void SetView(RelativeLayout relativeone,
            RelativeLayout relativetwo, RelativeLayout relativethree,
            TextView textViewone, TextView textViewtwo, TextView textViewthree) {
        relativeone.setBackgroundColor(getResources().getColor(
                R.color.welcome_blue));
        relativethree
                .setBackgroundColor(getResources().getColor(R.color.white));
        relativetwo.setBackgroundColor(getResources().getColor(R.color.white));
        textViewone.setTextColor(getResources().getColor(R.color.white));
        textViewtwo.setTextColor(getResources().getColor(
                R.color.more_fragment_color2));
        textViewthree.setTextColor(getResources().getColor(
                R.color.more_fragment_color2));

    }

    OnClickListener MyOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.reset_completed:
                    Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true,
                            JVNetConst.RC_EX_STORAGE, JVNetConst.EX_STORAGE_FORMAT,
                            null);
                    resetDialog.dismiss();
                    createDialog("", false);
                    break;
                case R.id.reset_cancel:
                    resetDialog.dismiss();
                    break;
                case R.id.format:
                    if (isfind && isstop) {
                        ResetDialog();
                    } else if (!isfind) {
                        showTextToast(R.string.str_noSD);
                    } else if (!isstop) {
                        showTextToast(R.string.str_notformat);
                    }
                    break;
                case R.id.btn_left:
                    finish();
                    overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
                    break;
                case R.id.savemanager_img:
                    if (ispull) {
                        if (null != popupWindow && popupWindow.isShowing()) {
                            popupWindow.dismiss();
                            recode_chose
                                    .setBackgroundResource(R.drawable.savemanage_normal_icon);
                        } else {
                            recode_chose
                                    .setBackgroundResource(R.drawable.savemanager_pull);
                            popupWindow.setBackgroundDrawable(null);
                            popupWindow.setOutsideTouchable(true);
                            if (2 == MySharedPreference.getInt("DeviceType")) {
                                if (0 == MySharedPreference.getInt("HomeIPC")) {
                                    popupWindow.Normal_Recode
                                            .setVisibility(View.GONE);
                                    popupWindow.Alarm_Recodetext
                                            .setText(getResources().getString(
                                                    R.string.str_start_record));
                                }
                            }
                            popupWindow.showAsDropDown(recode_chose);
                            switch (recodeCount) {
                                case 0:
                                    SetView(popupWindow.Stop_Recode,
                                            popupWindow.Normal_Recode,
                                            popupWindow.Alarm_Recode,
                                            popupWindow.Stop_Recodetext,
                                            popupWindow.Normal_Recodetext,
                                            popupWindow.Alarm_Recodetext);
                                    break;
                                case 1:
                                    SetView(popupWindow.Normal_Recode,
                                            popupWindow.Alarm_Recode,
                                            popupWindow.Stop_Recode,
                                            popupWindow.Normal_Recodetext,
                                            popupWindow.Alarm_Recodetext,
                                            popupWindow.Stop_Recodetext);
                                    break;
                                case 2:
                                    SetView(popupWindow.Alarm_Recode,
                                            popupWindow.Normal_Recode,
                                            popupWindow.Stop_Recode,
                                            popupWindow.Alarm_Recodetext,
                                            popupWindow.Normal_Recodetext,
                                            popupWindow.Stop_Recodetext);
                                    break;

                                default:
                                    break;
                            }
                        }
                    }
                    break;
                case R.id.pop_outside:
                    popupWindow.dismiss();
                    recode_chose
                            .setBackgroundResource(R.drawable.savemanage_normal_icon);
                    break;
                case R.id.Alarm_recode:
                    if (2 == MySharedPreference.getInt("DeviceType")) {
                        if (0 == MySharedPreference.getInt("HomeIPC")) {
                            save_nMode.setText(getResources().getString(
                                    R.string.str_start_record));
                            SetView(popupWindow.Alarm_Recode,
                                    popupWindow.Normal_Recode,
                                    popupWindow.Stop_Recode,
                                    popupWindow.Alarm_Recodetext,
                                    popupWindow.Normal_Recodetext,
                                    popupWindow.Stop_Recodetext);
                            recode_chose
                                    .setBackgroundResource(R.drawable.savemanage_normal_icon);
                            recodeCount = 2;
                            popupWindow.dismiss();
                        } else {
                            save_nMode.setText(getResources().getString(
                                    R.string.video_alarm));
                            SetView(popupWindow.Alarm_Recode,
                                    popupWindow.Normal_Recode,
                                    popupWindow.Stop_Recode,
                                    popupWindow.Alarm_Recodetext,
                                    popupWindow.Normal_Recodetext,
                                    popupWindow.Stop_Recodetext);
                            recode_chose
                                    .setBackgroundResource(R.drawable.savemanage_normal_icon);
                            recodeCount = 2;
                            popupWindow.dismiss();
                        }
                    } else {
                        save_nMode.setText(getResources().getString(
                                R.string.video_alarm));
                        SetView(popupWindow.Alarm_Recode,
                                popupWindow.Normal_Recode, popupWindow.Stop_Recode,
                                popupWindow.Alarm_Recodetext,
                                popupWindow.Normal_Recodetext,
                                popupWindow.Stop_Recodetext);
                        recode_chose
                                .setBackgroundResource(R.drawable.savemanage_normal_icon);
                        recodeCount = 2;
                        popupWindow.dismiss();
                    }
                    break;
                case R.id.Normal_recode:
                    save_nMode.setText(getResources().getString(
                            R.string.video_normal));
                    SetView(popupWindow.Normal_Recode, popupWindow.Alarm_Recode,
                            popupWindow.Stop_Recode, popupWindow.Normal_Recodetext,
                            popupWindow.Alarm_Recodetext,
                            popupWindow.Stop_Recodetext);
                    recode_chose
                            .setBackgroundResource(R.drawable.savemanage_normal_icon);
                    recodeCount = 1;
                    popupWindow.dismiss();
                    break;
                case R.id.Stop_recode:
                    save_nMode.setText(getResources()
                            .getString(R.string.video_stop));
                    SetView(popupWindow.Stop_Recode, popupWindow.Normal_Recode,
                            popupWindow.Alarm_Recode, popupWindow.Stop_Recodetext,
                            popupWindow.Normal_Recodetext,
                            popupWindow.Alarm_Recodetext);
                    recode_chose
                            .setBackgroundResource(R.drawable.savemanage_normal_icon);
                    recodeCount = 0;
                    popupWindow.dismiss();
                    break;
                case R.id.savemanager_save:
                    switch (recodeCount) {
                        case 0:
                            if (2 == MySharedPreference.getInt("DeviceType")) {
                                if (1 == MySharedPreference.getInt("HomeIPC")) {
                                    // // 融合后逻辑
                                    String params = "bRecEnable=0;RecFileLength=600;bRecDisconEnable=0;bRecTimingEnable=0;RecTimingStart=0;RecTimingStop=0;bRecAlarmEnable=0;";
                                    // TODO 5sd卡停止录像
                                    Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA,
                                            true, JVNetConst.RC_EX_STORAGE,
                                            JVNetConst.EX_STORAGE_REC, params);

                                } else if (0 == MySharedPreference.getInt("HomeIPC")) {
                                    // // 融合前逻辑
                                    // TODO 6sd卡停止录像

                                    Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA,
                                            true, JVNetConst.RC_EX_STORAGE,
                                            JVNetConst.EX_STORAGE_REC_OFF, null);
                                }
                            } else {
                                // 融合后逻辑
                                String params = "bRecEnable=0;RecFileLength=600;bRecDisconEnable=0;bRecTimingEnable=0;RecTimingStart=0;RecTimingStop=0;bRecAlarmEnable=0;";
                                // TODO 5sd卡停止录像
                                Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA,
                                        true, JVNetConst.RC_EX_STORAGE,
                                        JVNetConst.EX_STORAGE_REC, params);
                            }

                            createDialog("", true);
                            handler.postDelayed(runnable, 3000);
                            break;
                        case 1:
                            Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true,
                                    Consts.COUNT_EX_STORAGE,
                                    Consts.TYPE_EX_STORAGE_SWITCH, String.format(
                                            Consts.FORMATTER_STORAGE_MODE,
                                            Consts.STORAGEMODE_NORMAL));
                            createDialog("", true);
                            handler.postDelayed(runnable, 10000);
                            break;
                        case 2:
                            if (2 == MySharedPreference.getInt("DeviceType")) {
                                if (0 == MySharedPreference.getInt("HomeIPC")) {
                                    Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA,
                                            true, JVNetConst.RC_EX_STORAGE,
                                            JVNetConst.EX_STORAGE_REC_ON, null);
                                    Runnable runnables = new Runnable() {
                                        @Override
                                        public void run() {
                                            dismissDialog();
                                            finish();
                                        }
                                    };
                                    handler.postDelayed(runnables, 3000);
                                } else {
                                    Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA,
                                            true, Consts.COUNT_EX_STORAGE,
                                            Consts.TYPE_EX_STORAGE_SWITCH,
                                            String.format(
                                                    Consts.FORMATTER_STORAGE_MODE,
                                                    Consts.STORAGEMODE_ALARM));
                                }
                            } else {
                                Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA,
                                        true, Consts.COUNT_EX_STORAGE,
                                        Consts.TYPE_EX_STORAGE_SWITCH, String.format(
                                                Consts.FORMATTER_STORAGE_MODE,
                                                Consts.STORAGEMODE_ALARM));
                            }

                            createDialog("", true);
                            handler.postDelayed(runnableout, 40000);
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    };
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            MySharedPreference.putInt("bRecAlarmEnable", 0);
            MySharedPreference.putInt("bRecEnable", 0);
            dismissDialog();
            finish();
        }
    };
    Runnable runnableout = new Runnable() {
        @Override
        public void run() {
            if (isget) {
                showTextToast(R.string.str_video_load_failed);
                dismissDialog();
                finish();
            }
        }
    };

    @Override
    protected void saveSettings() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void freeMe() {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            Log.i("TAG", "nihao");
            dismissDialog();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
