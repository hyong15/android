
package com.jovision.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.bean.WifiAdmin;
import com.jovision.commons.MyActivityManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * 摇一摇的活动基类，所有活动都应该继承这个类，并实现其抽象方法和接口
 * 
 * @author juyang
 */
public abstract class ShakeActivity extends BaseActivity implements
        SensorEventListener {
    private final String TAG = "ShakeActivity";
    /** Sensor管理器 */
    protected SensorManager mSensorManager = null;
    /** 震动 */
    protected Vibrator mVibrator = null;
    protected AssetManager assetMgr = null;
    protected MediaPlayer mediaPlayer = new MediaPlayer();
    protected WifiAdmin wifiAdmin;
    protected String oldWifiSSID = "";
    protected static boolean oldWifiState = false;
    protected boolean threadRunning = false;

    private ShakeHandler shakeHandler;
    /** IPC网络 */
    protected ArrayList<ScanResult> scanIpcWifiList = new ArrayList<ScanResult>();
    /** 手机wifi列表（除IPC） */
    protected ArrayList<ScanResult> scanMobileWifiList = new ArrayList<ScanResult>();

    /** 是否开启摇一摇 */
    protected boolean shakeState = false;

    /** startActicityCode */
    protected int startActicityCode = -1;

    /**
     * 初始化摇一摇功能
     */
    protected void initShake() {
        assetMgr = this.getAssets();
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        wifiAdmin = new WifiAdmin(ShakeActivity.this);

        // wifi打开的前提下,获取oldwifiSSID
        if (wifiAdmin.getWifiState()) {
            if (null != wifiAdmin.getSSID()) {
                oldWifiSSID = wifiAdmin.getSSID().replace("\"", "");
            }
        }

        shakeHandler = new ShakeHandler(ShakeActivity.this);
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    protected void initSettings() {

    }

    @Override
    protected void initUi() {
        // if (shakeState) {
        initShake();
        // }
    }

    @Override
    protected void saveSettings() {

    }

    @Override
    protected void freeMe() {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (null != mSensorManager) {
            mSensorManager.unregisterListener(this);
        }

        saveSettings();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        freeMe();
        MyActivityManager.getActivityManager().popActivity(this);
        super.onDestroy();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        float[] values = event.values;
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            if (Math.abs(values[0]) > 13 || Math.abs(values[1]) > 13
                    || Math.abs(values[2]) > 13) {
                if (shakeState) {
                    startSearch(true, -1);
                }
            }
        }
    }

    public void startSearch(boolean playSound, int startCode) {
        startActicityCode = startCode;
        if (!threadRunning) {
            threadRunning = true;
            if (playSound) {
                mVibrator.vibrate(100);
                prepareAndPlay(1);
            }
            createDialog(R.string.quick_setting_searching, false);
            GetWifiThread gwt = new GetWifiThread(ShakeActivity.this);
            gwt.start();
        }
    }

    int i = 0;

    /**
     * 获取wifi列表线程
     * 
     * @author Administrator
     */
    class GetWifiThread extends Thread {
        private final WeakReference<ShakeActivity> mActivity;

        public GetWifiThread(ShakeActivity activity) {
            mActivity = new WeakReference<ShakeActivity>(activity);
        }

        @Override
        public void run() {
            super.run();
            i = 0;
            ShakeActivity activity = mActivity.get();
            if (null != activity) {

                // wifi打开的前提下
                if (activity.wifiAdmin.getWifiState()) {
                    if (null != activity.wifiAdmin.getSSID()) {
                        oldWifiSSID = activity.wifiAdmin.getSSID().replace(
                                "\"", "");
                    }

                }

                boolean openFlag = false;

                if (activity.wifiAdmin.getWifiState()) {// 如果wifi为打开状态无需等待5秒
                    oldWifiState = true;
                    openFlag = true;
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                } else {
                    oldWifiState = false;
                    openFlag = activity.wifiAdmin.openWifi();// 打开wifi需要6秒左右

                    boolean state = false;

                    while (!state) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e1) {
                            e1.printStackTrace();
                        }
                        state = activity.wifiAdmin.getWifiState();
                    }

                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }

                if (openFlag) {
                    scanIpcWifiList = activity.wifiAdmin.startScanIPC();
                    i++;
                }
                // 多次检索，防止有设备检索不到
                if (null == scanIpcWifiList || 0 == scanIpcWifiList.size()) {
                    scanIpcWifiList = activity.wifiAdmin.startScanIPC();
                    i++;
                }
                // 多次检索，防止有设备检索不到
                if (null == scanIpcWifiList || 0 == scanIpcWifiList.size()) {
                    scanIpcWifiList = activity.wifiAdmin.startScanIPC();
                    i++;
                }

                // 多次检索，防止有设备检索不到
                if (null == scanIpcWifiList || 0 == scanIpcWifiList.size()) {
                    scanIpcWifiList = activity.wifiAdmin.startScanIPC();
                    i++;
                }

                // 多次检索，防止有设备检索不到
                if (null == scanIpcWifiList || 0 == scanIpcWifiList.size()) {
                    scanIpcWifiList = activity.wifiAdmin.startScanIPC();
                    i++;
                }

                // 多次检索，防止有设备检索不到
                if (null == scanIpcWifiList || 0 == scanIpcWifiList.size()) {
                    scanIpcWifiList = activity.wifiAdmin.startScanIPC();
                    i++;
                }

                Message msg = shakeHandler.obtainMessage();
                if (null != scanIpcWifiList && 0 != scanIpcWifiList.size()) {
                    msg.what = Consts.WHAT_SHAKE_IPC_WIFI_SUCCESS;
                } else {
                    msg.what = Consts.WHAT_SHAKE_IPC_WIFI_FAILED;
                }
                shakeHandler.sendMessage(msg);
            }
        }
    }

    /**
     * 搜索附近IPCwifi
     */
    private void openSearchDialog1() {
        AlertDialog.Builder builder = new AlertDialog.Builder(
                ShakeActivity.this);

        builder.setTitle(R.string.tips);

        String str = getResources().getString(
                R.string.str_quick_setting_alert_wifiinfo).replace("1",
                scanIpcWifiList.size() + "");
        builder.setMessage(str);

        builder.setPositiveButton(R.string.sure,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        threadRunning = false;

                        Intent intent = new Intent(ShakeActivity.this,
                                JVQuickSettingActivity.class);
                        intent.putExtra("OLD_WIFI", oldWifiSSID);
                        intent.putExtra("IPC_LIST", scanIpcWifiList);
                        intent.putExtra("MOBILE_LIST", scanMobileWifiList);
                        ShakeActivity.this.startActivity(intent);

                    }
                });
        builder.setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (oldWifiState) {// 原wifi开着的，什么也不做

                        } else {// 原wifi关闭状态，关闭wifi
                            wifiAdmin.closeWifi();
                        }
                        threadRunning = false;
                    }
                });

        Dialog dia = builder.create();
        dia.setCancelable(false);
        dia.show();
    }

    private void prepareAndPlay(int state) {
        // try {
        // // 打开指定音乐文件
        // String file = "";
        // if (1 == state) {
        // file = "shake_1.mp3";
        // } else {
        // file = "shake_match_2.mp3";
        // }
        //
        // AssetFileDescriptor afd = assetMgr.openFd(file);
        // mediaPlayer.reset();
        //
        // // 使用MediaPlayer加载指定的声音文件。
        // mediaPlayer.setDataSource(afd.getFileDescriptor(),
        // afd.getStartOffset(), afd.getLength());
        // // 准备声音
        // mediaPlayer.prepare();
        // // 播放
        // mediaPlayer.start();
        //
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
    }

    private class ShakeHandler extends Handler {
        private ShakeActivity activity;

        public ShakeHandler(ShakeActivity activity) {
            this.activity = activity;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Consts.WHAT_SHAKE_IPC_WIFI_SUCCESS: {
                    // showTextToast("i=" + i + "-----" +
                    // scanIpcWifiList.toString());
                    scanMobileWifiList = activity.wifiAdmin.startScanWifi();
                    activity.prepareAndPlay(2);
                    // activity.openSearchDialog();
                    threadRunning = false;

                    Intent intent = new Intent(ShakeActivity.this,
                            JVQuickSettingActivity.class);
                    intent.putExtra("OLD_WIFI", oldWifiSSID);
                    intent.putExtra("IPC_LIST", scanIpcWifiList);
                    intent.putExtra("MOBILE_LIST", scanMobileWifiList);
                    if (-1 == startActicityCode) {
                        ShakeActivity.this.startActivity(intent);
                    } else {
                        ShakeActivity.this.startActivityForResult(intent,
                                startActicityCode);
                    }

                    break;
                }
                case Consts.WHAT_SHAKE_IPC_WIFI_FAILED: {
                    // showTextToast("i=" + i + "-----" +
                    // scanIpcWifiList.toString());
                    scanMobileWifiList = activity.wifiAdmin.startScanWifi();
                    activity.showTextToast(R.string.str_quick_setting_alert_nowifi);

                    if (oldWifiState) {// 原wifi开着的，什么也不做

                    } else {// 原wifi关闭状态，关闭wifi
                        activity.wifiAdmin.closeWifi();
                    }
                    break;
                }
            }
            if (null != activity.proDialog && activity.proDialog.isShowing()) {
                activity.proDialog.dismiss();
                activity.proDialog = null;
            }
            activity.threadRunning = false;
        }
    }

}
