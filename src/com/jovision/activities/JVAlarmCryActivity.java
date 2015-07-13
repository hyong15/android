
package com.jovision.activities;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ToggleButton;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.commons.JVNetConst;
import com.jovision.commons.MyLog;
import com.jovision.utils.ConfigUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JVAlarmCryActivity extends BaseActivity {

    private int window;
    private ToggleButton togglebtn1;
    private ToggleButton togglebtn2;
    private ToggleButton togglebtn3;
    private TextView Alarm_save;
    private int Parameter1 = 1;
    private int Parameter2 = 1;
    private int Parameter3 = 1;
    private String alarm_high;
    private String alarm_low;
    private String TAG = "JVAlarmCryActivity";

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.CALL_TEXT_DATA: {// 文本回调
                switch (arg2) {
                    case JVNetConst.JVN_RSP_TEXTDATA:// 文本数据
                        dismissDialog();
                        String allStr = obj.toString();
                        try {
                            JSONObject dataObj = new JSONObject(allStr);
                            switch (dataObj.getInt("flag")) {
                                case JVNetConst.JVN_ALARMCRY:
                                    dismissDialog();
                                    break;
                                case JVNetConst.JVN_STREAM_INFO:// 3-- 码流配置请求

                                    String pnJSON = dataObj.getString("msg");
                                    HashMap<String, String> streamMap = ConfigUtil
                                            .genMsgMap(pnJSON);

                                    if (null != streamMap) {
                                        if (null != streamMap.get("baby_refdbfs")
                                                && !"".equalsIgnoreCase(streamMap
                                                        .get("baby_refdbfs"))) {
                                            int baby_refdbfs = Integer.parseInt(streamMap
                                                    .get("baby_refdbfs"));
                                            MyLog.v(TAG, "baby_refdbfs = " + baby_refdbfs);
                                            if (1 == baby_refdbfs) {
                                                togglebtn1.setText(alarm_low);
                                                togglebtn1.setChecked(false);
                                                Parameter1 = 1;
                                            } else {
                                                togglebtn1.setText(alarm_high);
                                                togglebtn1.setChecked(true);
                                                Parameter1 = 2;
                                            }
                                        }
                                    }

                                    if (null != streamMap) {
                                        if (null != streamMap.get("baby_ratio")
                                                && !"".equalsIgnoreCase(streamMap
                                                        .get("baby_ratio"))) {
                                            int baby_ratio = Integer.parseInt(streamMap
                                                    .get("baby_ratio"));
                                            MyLog.v(TAG, "baby_refdbfs = " + baby_ratio);
                                            if (1 == baby_ratio) {
                                                togglebtn2.setText(alarm_low);
                                                togglebtn2.setChecked(false);
                                                Parameter2 = 1;
                                            } else {
                                                togglebtn2.setText(alarm_high);
                                                togglebtn2.setChecked(true);
                                                Parameter2 = 2;
                                            }
                                        }
                                    }

                                    if (null != streamMap) {
                                        if (null != streamMap.get("baby_timeout")
                                                && !"".equalsIgnoreCase(streamMap
                                                        .get("baby_timeout"))) {
                                            int baby_timeout = Integer.parseInt(streamMap
                                                    .get("baby_timeout"));
                                            MyLog.v(TAG, "baby_refdbfs = " + baby_timeout);
                                            if (1 == baby_timeout) {
                                                togglebtn3.setText(alarm_low);
                                                togglebtn3.setChecked(false);
                                                Parameter3 = 1;
                                            } else {
                                                togglebtn3.setText(alarm_high);
                                                togglebtn3.setChecked(true);
                                                Parameter3 = 2;
                                            }
                                        }
                                    }

                                    break;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                }
            }
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {
        // TODO Auto-generated method stub
        setContentView(R.layout.alarmcry_layout);

        window = getIntent().getExtras().getInt("window");
        alarm_high = getResources().getString(R.string.str_high);
        alarm_low = getResources().getString(R.string.str_low);

    }

    @Override
    protected void onResume() {
        Jni.sendTextData(window, JVNetConst.JVN_RSP_TEXTDATA, 8,
                JVNetConst.JVN_STREAM_INFO);
        createDialog("", true);
        super.onResume();
    }

    @Override
    protected void initUi() {
        // TODO Auto-generated method stub
        togglebtn1 = (ToggleButton) findViewById(R.id.togglebtn1);
        togglebtn2 = (ToggleButton) findViewById(R.id.togglebtn2);
        togglebtn3 = (ToggleButton) findViewById(R.id.togglebtn3);
        Alarm_save = (TextView) findViewById(R.id.alarmcry_save);
        leftBtn = (Button) findViewById(R.id.btn_left);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        currentMenu.setText(R.string.str_alarmcry);
        rightBtn = (Button) findViewById(R.id.btn_right);
        rightBtn.setVisibility(View.GONE);

        Alarm_save.setOnClickListener(MyOnClickListener);
        leftBtn.setOnClickListener(MyOnClickListener);
        togglebtn1.setOnClickListener(MyOnClickListener);
        togglebtn2.setOnClickListener(MyOnClickListener);
        togglebtn3.setOnClickListener(MyOnClickListener);
    }

    OnClickListener MyOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.btn_left:
                    finish();
                    break;
                case R.id.togglebtn1:
                    if (!togglebtn1.isChecked()) {
                        togglebtn1.setText(alarm_low);
                        Parameter1 = 1;
                    } else {
                        togglebtn1.setText(alarm_high);
                        Parameter1 = 2;
                    }
                    break;
                case R.id.togglebtn2:
                    if (!togglebtn2.isChecked()) {
                        togglebtn2.setText(alarm_low);
                        Parameter2 = 1;
                    } else {
                        togglebtn2.setText(alarm_high);
                        Parameter2 = 2;
                    }
                    break;
                case R.id.togglebtn3:
                    if (!togglebtn3.isChecked()) {
                        togglebtn3.setText(alarm_low);
                        Parameter3 = 1;
                    } else {
                        togglebtn3.setText(alarm_high);
                        Parameter3 = 2;
                    }
                    break;
                case R.id.alarmcry_save:
                    Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, false, 0,
                            JVNetConst.RC_SETPARAM, String.format(
                                    Consts.BABY_ALARM_PARAM, Parameter1,
                                    Parameter2, Parameter3));
                    createDialog("", true);
                    break;
                default:
                    break;
            }
        }
    };

    protected void saveSettings() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void freeMe() {
        // TODO Auto-generated method stub

    }

}
