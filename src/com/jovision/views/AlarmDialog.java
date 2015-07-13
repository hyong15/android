
package com.jovision.views;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.activities.JVPlayActivity;
import com.jovision.bean.Device;
import com.jovision.bean.PushInfo;
import com.jovision.commons.MyActivityManager;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.PlayUtil;

import java.util.ArrayList;

public class AlarmDialog extends Dialog {
    private static AlarmDialog sSingleton = null;
    private Context context;

    private TextView dialogCancel;
    private TextView dialogView;
    private TextView dialogDeviceName;
    private TextView dialogDeviceModle;
    private TextView dialogAlarmTime;
    private ImageView dialogCancleImg;
    private String ystNum; // 云视通号
    private String deviceNickName;// 昵称
    private String alarmTypeName;// 报警类型
    private String alarmTime;
    // private static int alarmDialogObjs = 0;// new 出来的对象数量
    private ArrayList<Device> deviceList = new ArrayList<Device>();
    private Toast toast;
    private String strAlarmGUID;
    private Vibrator vibrator;
    private MediaPlayer player;

    public AlarmDialog(Context context) {
        super(context, R.style.mydialog);
        this.context = context;
        vibrator = (Vibrator) context
                .getSystemService(Service.VIBRATOR_SERVICE);
        // TODO Auto-generated constructor stub
        if (null == player) {
            player = new MediaPlayer();
        }
        player = MediaPlayer.create(this.context, R.raw.alarm);
        // try {
        // player.setDataSource(this.context, RingtoneManager
        // .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        // player.prepare();
        // } catch (IllegalArgumentException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (SecurityException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IllegalStateException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.alarm_dialog_summary);
        setCanceledOnTouchOutside(false);
        dialogCancel = (TextView) findViewById(R.id.dialog_cancel);
        dialogView = (TextView) findViewById(R.id.dialog_view);
        dialogCancleImg = (ImageView) findViewById(R.id.dialog_cancle_img);
        dialogDeviceName = (TextView) findViewById(R.id.dialog_devicename);
        dialogDeviceModle = (TextView) findViewById(R.id.dialog_devicemodle);
        dialogAlarmTime = (TextView) findViewById(R.id.dialog_alarm_time);

        dialogCancel.setOnClickListener(myOnClickListener);
        dialogView.setOnClickListener(myOnClickListener);
        dialogCancleImg.setOnClickListener(myOnClickListener);
        // MyLog.e("AlarmDialog", "onCreate" + getDialogObjs());

    }

    @Override
    protected void onStart() {
        dialogDeviceName.setText(deviceNickName);
        dialogDeviceModle.setText(alarmTypeName);
        dialogAlarmTime.setText(alarmTime);
        // MyLog.e("AlarmDialog", "onStart" + getDialogObjs());
        // if (isshowing) {
        // dismiss();
        // return;
        // }
        // isshowing = true;
    }

    @Override
    protected void onStop() {
        // synchronized (AlarmDialog.class) {
        Log.e("Alarm", "onStop, ishowing==false");
        vibrator.cancel();
        // }
    }

    // public synchronized static int getDialogObjs() {
    // return alarmDialogObjs;
    // }

    android.view.View.OnClickListener myOnClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dialog_cancel:
                    dismiss();
                    for (int i = 0; i < Consts.AlarmList.size(); i++) {
                        Consts.AlarmList.get(i).dismiss();
                    }
                    break;
                case R.id.dialog_cancle_img:
                    dismiss();
                    for (int i = 0; i < Consts.AlarmList.size(); i++) {
                        Consts.AlarmList.get(i).dismiss();
                    }
                    break;
                case R.id.dialog_view:
                    // String contextString = context.toString();
                    // String strTempNameString = contextString.substring(
                    // contextString.lastIndexOf(".") + 1,
                    // contextString.indexOf("@"));
                    // if (strTempNameString.equals("JVPlayActivity")) {
                    // if (!mApp.getMarkedAlarmList().contains(
                    // strAlarmGUID)) {
                    // mApp.getMarkedAlarmList().add(
                    // strAlarmGUID);
                    // }
                    MySharedPreference.putString(Consts.CHECK_ALARM_KEY,
                            strAlarmGUID);
                    Activity currentActivity = MyActivityManager
                            .getActivityManager().currentActivity();
                    if (currentActivity == null
                            || currentActivity
                                    .getClass()
                                    .getName()
                                    .equals("com.jovision.activities.JVPlayActivity")) {
                    } else {
                        try {
                            deviceList = CacheUtil.getDevList();// 再取一次
                            int dev_index = getDeivceIndex(ystNum);
                            if (dev_index == -1 || dev_index >= deviceList.size()) {
                                showTextToast(context, "error index:" + dev_index
                                        + ", size:" + deviceList.size());
                                return;
                            }

                            ArrayList<Device> playList = PlayUtil.prepareConnect(
                                    deviceList, dev_index);// 该函数里已经调用SaveList了
                            // MyLog.v("Alarm",
                            // "prepareConnect2--" + deviceList.toString());
                            // CacheUtil.saveDevList(deviceList);
                            // deviceList = CacheUtil.getDevList();//再取一次
                            Intent intentPlay = new Intent(context,
                                    JVPlayActivity.class);
                            intentPlay.putExtra("PlayFlag", Consts.PLAY_NORMAL);
                            intentPlay.putExtra(Consts.KEY_PLAY_NORMAL,
                                    playList.toString());
                            intentPlay.putExtra("DeviceIndex", dev_index);
                            if (deviceList.get(dev_index).getChannelList().size() == 0) {
                                showTextToast(context,
                                        "error channel list size 0, dev_index:"
                                                + dev_index);
                                return;
                            }
                            intentPlay.putExtra("ChannelofChannel",
                                    deviceList.get(dev_index).getChannelList()
                                            .toList().get(0).getChannel());
                            // Toast.makeText(context, "DeviceIndex:" +
                            // dev_index,
                            // Toast.LENGTH_SHORT).show();
                            context.startActivity(intentPlay);
                            dismiss();
                        } catch (IllegalArgumentException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    dismiss();
                    for (int i = 0; i < Consts.AlarmList.size(); i++) {
                        Consts.AlarmList.get(i).dismiss();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    private int getDeivceIndex(String strYstNum) {
        Device devs = null;
        boolean bfind = false;
        for (int j = 0; j < CacheUtil.getDevList().size(); j++) {
            devs = CacheUtil.getDevList().get(j);
            MyLog.v("AlarmConnect",
                    "dst:" + strYstNum + "---yst-num = " + devs.getFullNo());
            if (strYstNum.equalsIgnoreCase(devs.getFullNo())) {
                bfind = true;
                MyLog.v("New Alarm Dialog", "find dev num " + strYstNum
                        + ", index:" + j);
                return j;
            }
        }
        return -1;
    }

    public void Show(Object obj) {

        // if (isshowing) {
        // Log.e("Alarm", "isshowing is true, then return");
        // return;
        // }

        Log.e("Alarm", "Show() isshowing == true");
        if (MySharedPreference.getBoolean(Consts.ALARM_SETTING_VIBRATE, true)) {
            vibrator.vibrate(new long[] {
                    500, 2000, 500, 2000
            }, -1);
            // vibrator.vibrate(2000);
        }
        if (MySharedPreference.getBoolean(Consts.ALARM_SETTING_SOUND, true)) {
            player.start();
        }

        PushInfo pi = (PushInfo) obj;
        ystNum = pi.ystNum;
        deviceNickName = pi.deviceNickName;
        // deviceList = CacheUtil.getDevList();// 再取一次
        // int dev_index = getDeivceIndex(ystNum);
        // if (dev_index == -1) {
        // deviceNickName = pi.deviceNickName;
        // } else {
        // // deviceNickName = deviceList.get(dev_index).getNickName();
        // deviceNickName = pi.deviceNickName;
        // if (pi.alarmType == 11)// 第三方
        // {
        // deviceNickName = deviceNickName + "-"
        // + pi.deviceNickName;
        // }
        // }

        // deviceNickName = pi.deviceNickName;

        alarmTime = pi.alarmTime;
        String strAlarmTypeName = "";
        if (pi.alarmType == 7) {
            strAlarmTypeName = context.getResources().getString(
                    R.string.str_alarm_type_move);
        } else if (pi.alarmType == 11) {
            strAlarmTypeName = context.getResources().getString(
                    R.string.str_alarm_type_third);
        } else if (pi.alarmType == 4) {
            strAlarmTypeName = context.getResources().getString(
                    R.string.str_alarm_type_external);
        } else {
            strAlarmTypeName = context.getResources().getString(
                    R.string.str_alarm_type_unknown);
        }
        alarmTypeName = strAlarmTypeName;
        strAlarmGUID = pi.strGUID;
        show();
        Consts.AlarmList.add(this);
        // } else {
        // Log.e("Alarm", "收到信息，但不提示");
        // // Toast.makeText(context, "收到信息，但不提示",
        // // Toast.LENGTH_SHORT).show();
        // }
    }

    // @Override
    // public void dismiss() {
    // // TODO Auto-generated method stub
    // isshowing = false;
    // super.dismiss();
    // }

    /**
     * 弹系统消息
     * 
     * @param context
     * @param id
     */
    public void showTextToast(Context context, int id) {
        String msg = context.getResources().getString(id);
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    public void showTextToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }
}
