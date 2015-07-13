
package com.jovision.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.commons.JVNetConst;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.ConfigUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class JVOverTurnActivity extends BaseActivity {
    private RelativeLayout onechose;// 手动录像
    private RelativeLayout twochose;// 自动录像
    private HashMap<String, String> streamMap = new HashMap<String, String>();
    private ImageView one_img;// 第一个选择框
    private ImageView two_img;// 第二个选择框
    private int effect_flag;
    private int window;

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stubz
        switch (what) {
            case Consts.CALL_TEXT_DATA:
                switch (arg2) {
                    case JVNetConst.JVN_RSP_TEXTDATA:// 文本数据
                        String allStr = obj.toString();
                        MyLog.v("Alarm", "文本数据--" + allStr);
                        JSONObject dataObj;
                        try {
                            dataObj = new JSONObject(allStr);
                            int flag = dataObj.getInt("flag");

                            switch (flag) {
                                case JVNetConst.JVN_STREAM_INFO:// 码流
                                    String streamJSON = dataObj.getString("msg");
                                    streamMap = ConfigUtil.genMsgMap(streamJSON);
                                    if (null != streamMap) {
                                        if (null != streamMap.get("effect_flag")
                                                && !"".equalsIgnoreCase(streamMap
                                                        .get("effect_flag"))) {

                                            int effect_flags = Integer.parseInt(streamMap
                                                    .get("effect_flag"));
                                            effect_flag = effect_flags;
                                            if (0 == (0x04 & effect_flag)) {
                                                MySharedPreference.putInt("screenover", 0);
                                                one_img.setImageResource(R.drawable.morefragment_normal_icon);
                                                two_img.setImageResource(R.drawable.morefragment_selector_icon);
                                            } else {
                                                MySharedPreference.putInt("screenover", 4);
                                                one_img.setImageResource(R.drawable.morefragment_selector_icon);
                                                two_img.setImageResource(R.drawable.morefragment_normal_icon);
                                            }
                                        }
                                    }
                                    break;
                                default:
                                    break;
                            }
                        } catch (JSONException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                }
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
        setContentView(R.layout.overturn_layout);
        Bundle extras = getIntent().getExtras();
        window = extras.getInt("window");
    }

    @Override
    protected void initUi() {

    }

    @Override
    protected void saveSettings() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void freeMe() {
        // TODO Auto-generated method stub

    }
}
