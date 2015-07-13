
package com.jovision.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;

import org.json.JSONArray;
import org.json.JSONObject;

public class JVDecodeTypeActivity extends BaseActivity {
    private RelativeLayout onechose;// 软解
    private RelativeLayout twochose;// 硬解
    private ImageView one_img;// 第一个选择框
    private ImageView two_img;// 第二个选择框
    private int window;
    private boolean isoxms;// 软硬解标示

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stubz
        switch (what) {
            case Consts.CALL_STAT_REPORT: {
                dismissDialog();
                boolean isoxm;
                try {
                    MyLog.e(Consts.TAG_PLAY, obj.toString());
                    JSONArray array = new JSONArray(obj.toString());
                    JSONObject object = null;

                    int size = array.length();
                    for (int i = 0; i < size; i++) {
                        object = array.getJSONObject(i);
                        isoxm = object.getBoolean("is_omx");
                        if (isoxm) {
                            two_img.setImageResource(R.drawable.morefragment_selector_icon);
                            one_img.setImageResource(R.drawable.morefragment_normal_icon);
                            MySharedPreference.putBoolean("decodetype", true);
                        } else {
                            one_img.setImageResource(R.drawable.morefragment_selector_icon);
                            two_img.setImageResource(R.drawable.morefragment_normal_icon);
                            MySharedPreference.putBoolean("decodetype", false);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
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
    protected void initSettings() {
        // TODO Auto-generated method stub
        setContentView(R.layout.decodetype_layout);
        Bundle extras = getIntent().getExtras();
        window = extras.getInt("window");
        isoxms = MySharedPreference.getBoolean("decodetype", false);
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        if (isoxms) {
            one_img.setImageResource(R.drawable.morefragment_normal_icon);
            two_img.setImageResource(R.drawable.morefragment_selector_icon);
        } else {
            one_img.setImageResource(R.drawable.morefragment_selector_icon);
            two_img.setImageResource(R.drawable.morefragment_normal_icon);
        }
    }

    @Override
    protected void initUi() {
        // TODO Auto-generated method stub
        onechose = (RelativeLayout) findViewById(R.id.onechose);
        twochose = (RelativeLayout) findViewById(R.id.twochose);
        one_img = (ImageView) findViewById(R.id.oneimg);
        two_img = (ImageView) findViewById(R.id.twoimg);
        leftBtn = (Button) findViewById(R.id.btn_left);
        rightBtn = (Button) findViewById(R.id.btn_right);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        rightBtn.setVisibility(View.GONE);

        leftBtn.setOnClickListener(MyOnClickListener);
        currentMenu.setText(R.string.str_decodetype);
        onechose.setOnClickListener(MyOnClickListener);
        twochose.setOnClickListener(MyOnClickListener);
    }

    OnClickListener MyOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left:
                    finish();
                    break;
                case R.id.onechose:
                    createDialog("", true);
                    Jni.setOmx(window, false);
                    break;
                case R.id.twochose:
                    createDialog("", true);
                    Jni.setOmx(window, true);
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
