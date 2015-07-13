
package com.jovision.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.adapters.DeviceSettingAdapter;
import com.jovision.bean.MoreFragmentBean;
import com.jovision.commons.JVDeviceConst;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.CacheUtil;
import com.jovision.utils.DeviceUtil;
import com.jovision.utils.JsonFileReader;

import java.util.ArrayList;
import java.util.List;

public class JVDeviceSettingsReferenceFragment extends BaseFragment {

    private final String TAG = "JVDeviceSettingsReferenceFragment";
    private final String JSONFILE = "device_setting.json";
    // assets/profile.json中主界面部分对应的标志
    private final String JSONTAG = "device";
    // 当前对象
    private JVDeviceSettingsReferenceActivity mInstance;
    // 模块listView
    private ListView mListView;
    // Adapter 存储模块文字和图标
    private List<MoreFragmentBean> mDataList;
    // listView 适配器
    private DeviceSettingAdapter mAdapter;

    /** intent 传递过来的信息 **/
    private Bundle mArguments;

    private int Cloudenable = 2;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mInstance = (JVDeviceSettingsReferenceActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(
                R.layout.device_setting_functions_layout, container, false);
        initViews(rootView);
        initListeners();
        initDatas();
        return rootView;
    }

    /**
     * 实例化布局
     */
    private void initViews(final View view) {
        /*** other **/
        mListView = (ListView) view.findViewById(R.id.listView);
        Cloudenable = mInstance.device.getCloudEnabled();
        if (0 != Cloudenable && 1 != Cloudenable) {
            MySharedPreference.putBoolean("Cloudenable", true);
        } else {
            MySharedPreference.putBoolean("Cloudenable", false);
        }
        if (0 == Cloudenable) {
            MySharedPreference.putBoolean(Consts.DEVICE_SETTING_CLOUDSERVICE,
                    false);
        } else if (1 == Cloudenable) {
            MySharedPreference.putBoolean(Consts.DEVICE_SETTING_CLOUDSERVICE,
                    true);
        } else {

        }
    }

    /**
     * 初始化监听事件
     */
    private void initListeners() {
        mListView.setOnItemClickListener(myOnItemClickListener);
    }

    /**
     * 初始化数据及一些设置操作
     */
    private void initDatas() {
        mArguments = getArguments();
        // 加载json数据
        new DataThread().start();
    }

    /**
     * ListView click事件处理
     */
    OnItemClickListener myOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            // 存储管理
            if (mDataList.get(position).getItemFlag()
                    .equals(Consts.DEVICE_SETTING_STORAGE)) {
                mInstance.functionStorage();
                return;
            }
            // 云存储服务
            if (mDataList.get(position).getItemFlag()
                    .equals(Consts.DEVICE_SETTING_CLOUDSERVICE)) {
                if (2 != Cloudenable
                        && 0 != Integer.valueOf(mActivity.statusHashMap
                                .get(Consts.MORE_CLOUD_SWITCH))) {
                    mInstance.createDialog("", false);
                    CloudSwitchTask cloudtask = new CloudSwitchTask();
                    String[] params1 = new String[3];
                    cloudtask.execute(params1);
                }
                return;
            }

            // 存储管理和云存储服务以外的跳转到二级页面
            Intent intent = new Intent(mInstance, DeviceSettingsActivity.class);
            mArguments.putString("jsontag", mDataList.get(position)
                    .getItemFlag());
            intent.putExtras(mArguments);
            startActivity(intent);
            mInstance.executeAnimRightinLeftout();
        }
    };

    /**
     * 加载数据线程
     */
    class DataThread extends Thread {

        @Override
        public void run() {
            mDataList = new ArrayList<MoreFragmentBean>();
            String jsonStr = JsonFileReader.getJson(mInstance.getBaseContext(),
                    JSONFILE);
            mDataList = JsonFileReader.getDataListByJson(
                    mInstance.getBaseContext(), jsonStr, JSONTAG);
            mInstance.runOnUiThread(new UiThreadRunnable());
        }

    }

    /**
     * 处理最终的结果(主线程)
     */
    class UiThreadRunnable implements Runnable {

        @Override
        public void run() {
            mAdapter = new DeviceSettingAdapter(mInstance, mDataList);

            mListView.setAdapter(mAdapter);
        }
    }

    class CloudSwitchTask extends AsyncTask<String, Integer, Integer> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {
            int switchRes = -1;// 0成功， 1失败，1000离线
            try {
                if (null == mInstance.device) {
                    mInstance.device = mInstance.deviceList
                            .get(mInstance.deviceIndex);
                }
                // if (JVDeviceConst.DEVICE_SERVER_ONLINE == device
                // .getServerState()) {
                int sendTag = 0;
                if (1 == mInstance.device.getCloudEnabled()) {
                    sendTag = JVDeviceConst.DEVICE_SWITCH_CLOSE;
                } else {
                    sendTag = JVDeviceConst.DEVICE_SWITCH_OPEN;
                }
                switchRes = DeviceUtil.saveCloudSettings(0, sendTag,
                        mInstance.statusHashMap.get(Consts.KEY_USERNAME),
                        mInstance.device.getFullNo());
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
            mInstance.dismissDialog();
            if (0 == result) {
                // StatService.trackCustomEvent(mActivity, "Alarm", mActivity
                // .getResources().getString(R.string.census_alarm2));
                if (1 == mInstance.device.getCloudEnabled()) {
                    mInstance.device.setCloudEnabled(0);
                    mInstance.showTextToast(R.string.cloud_close_succ);// 关闭OK
                    MySharedPreference.putBoolean(
                            Consts.DEVICE_SETTING_CLOUDSERVICE, false);
                } else {
                    mInstance.device.setCloudEnabled(1);
                    mInstance.showTextToast(R.string.cloud_open_succ);
                    MySharedPreference.putBoolean(
                            Consts.DEVICE_SETTING_CLOUDSERVICE, true);
                }
                // [{"fullNo":"S52942216","port":0,"hasWifi":1,"isDevice":0,"no":52942216,"is05":false,"onlineState":-1182329167,"channelList":[{"channel":1,"channelName":"S52942216_1","index":0}],"isHomeProduct":false,"ip":"","pwd":"123","nickName":"S52942216","deviceType":2,"alarmSwitch":1,"gid":"S","user":"abc","serverState":1,"doMain":""}]
                CacheUtil.saveDevList(mInstance.deviceList);
                mInstance.statusHashMap.put(Consts.HAG_GOT_DEVICE, "true");
            } else if (1000 == result) {
                mInstance.showTextToast(R.string.device_offline);
            } else {
                if (1 == mInstance.device.getCloudEnabled()) {
                    mInstance.showTextToast(R.string.cloud_close_fail);
                } else {
                    mInstance.showTextToast(R.string.cloud_open_fail);
                }
            }
            mAdapter.notifyDataSetChanged();
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
