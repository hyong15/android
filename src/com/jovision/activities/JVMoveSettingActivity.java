
package com.jovision.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.commons.JVNetConst;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.ConfigUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JVMoveSettingActivity extends BaseActivity {

    private SeekBar moveSeekBar;
    private TextView move_save;
    private TextView move_cancle;
    private TextView nMDSensitivity;
    private int currentMove;
    private int window;

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stubz
        switch (what) {
            case 101:
                nMDSensitivity.setText(arg1 + "");
                break;
            case Consts.CALL_TEXT_DATA:
                switch (arg2) {
                    case JVNetConst.JVN_RSP_TEXTDATA:// 文本数据
                        String allStr = obj.toString();
                        MyLog.v("jvmore", "文本数据--" + allStr);
                        JSONObject dataObj;
                        try {
                            dataObj = new JSONObject(allStr);
                            switch (dataObj.getInt("flag")) {
                                case JVNetConst.JVN_MOTION_DETECT_GET_CALLBACK: {// 17.获取移动侦测灵敏度回调
                                    try {
                                        int extend_type = dataObj.getInt("extend_type");
                                        if (JVNetConst.EX_MDRGN_UPDATE == extend_type) {// 更新
                                            String motionJSON = dataObj.getString("msg");
                                            HashMap<String, String> motionMap = ConfigUtil
                                                    .genMsgMap(motionJSON);
                                            if (null != motionMap
                                                    && null != motionMap
                                                            .get("nMDSensitivity")
                                                    && !"".equalsIgnoreCase(motionMap
                                                            .get("nMDSensitivity"))) {
                                                String sensi = motionMap
                                                        .get("nMDSensitivity");
                                                int CurnMDSensitivity = Integer
                                                        .parseInt(sensi);
                                                nMDSensitivity.setText(CurnMDSensitivity
                                                        + "");
                                                moveSeekBar.setProgress(CurnMDSensitivity);
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    break;
                                }
                                case JVNetConst.JVN_MOTION_DETECT_SET_CALLBACK: {// 18.设置移动侦测灵敏度回调
                                    // 文本数据--{"extend_arg1":0,"extend_arg2":0,"extend_arg3":0,"extend_type":2,"flag":18,"packet_count":6,"packet_id":0,"packet_length":0,"packet_type":6}

                                    try {
                                        int extend_type = dataObj.getInt("extend_type");
                                        if (JVNetConst.EX_MDRGN_SUBMIT == extend_type) {// 提交
                                            dismissDialog();
                                            int curnmdsuc = Integer.valueOf(nMDSensitivity
                                                    .getText().toString());
                                            MySharedPreference.putInt("curnmd", curnmdsuc);
                                            finish();
                                            showTextToast(getResources().getString(
                                                    R.string.str_movesetsuccess));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                            }
                        } catch (JSONException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        break;
                }
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
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true,
                JVNetConst.RC_EX_MDRGN, JVNetConst.EX_MDRGN_UPDATE, null);

    }

    @Override
    protected void initSettings() {
        // TODO Auto-generated method stub
        setContentView(R.layout.movesetting_layout);
        Bundle extras = getIntent().getExtras();
        window = extras.getInt("window");
    }

    @Override
    protected void initUi() {

        moveSeekBar = (SeekBar) findViewById(R.id.moveseekbar);
        move_save = (TextView) findViewById(R.id.move_save);
        move_cancle = (TextView) findViewById(R.id.move_cancle);
        nMDSensitivity = (TextView) findViewById(R.id.nMDSensitivity);
        leftBtn = (Button) findViewById(R.id.btn_left);
        rightBtn = (Button) findViewById(R.id.btn_right);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        currentMenu.setText(R.string.str_movesetting);

        rightBtn.setVisibility(View.GONE);
        leftBtn.setOnClickListener(MyOnClickListener);
        move_save.setOnClickListener(MyOnClickListener);
        move_cancle.setOnClickListener(MyOnClickListener);
        moveSeekBar.setOnSeekBarChangeListener(mOnSeekBarChangeListener);
        moveSeekBar.setMax(100);
    }

    /**
     * 远程回放进度条拖动事件
     */
    OnSeekBarChangeListener mOnSeekBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar arg0, int currentProgress,
                boolean arg2) {
            onNotify(101, currentProgress, 0, null);
            currentMove = currentProgress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar arg0) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar arg0) {
            onNotify(101, currentMove, 0, null);
        }
    };

    OnClickListener MyOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left:
                    finish();
                    break;
                case R.id.move_save:
                    createDialog("", true);
                    String param = "nMDSensitivity="
                            + nMDSensitivity.getText().toString();
                    Jni.sendString(window, JVNetConst.JVN_RSP_TEXTDATA, true,
                            JVNetConst.RC_EX_MDRGN, JVNetConst.EX_MDRGN_SUBMIT,
                            param);
                    break;
                case R.id.move_cancle:
                    finish();
                    break;
                default:
                    break;
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

}
