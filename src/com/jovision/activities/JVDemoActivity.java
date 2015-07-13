
package com.jovision.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.adapters.DemoListAdapter;
import com.jovision.bean.Device;
import com.jovision.utils.DeviceUtil;
import com.jovision.utils.PlayUtil;
import com.jovision.views.RefreshableView;
import com.jovision.views.RefreshableView.PullToRefreshListener;

import java.util.ArrayList;

public class JVDemoActivity extends BaseActivity {

    /** topBar */
    protected LinearLayout topBar;

    private RefreshableView refreshableView;
    /** 演示点设备列表 */
    private ListView demoListView;
    private ArrayList<Device> demoList = new ArrayList<Device>();
    DemoListAdapter demoAdapter;
    private boolean isbig;

    @Override
    protected void initSettings() {

    }

    @Override
    protected void initUi() {
        setContentView(R.layout.demo_layout);
        topBar = (LinearLayout) findViewById(R.id.top_bar);
        leftBtn = (Button) findViewById(R.id.btn_left);
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        rightBtn = (Button) findViewById(R.id.btn_right);
        currentMenu.setText(R.string.demo);
        rightBtn.setOnClickListener(myOnClickListener);
        rightBtn.setBackgroundResource(R.drawable.demo_switch_icon);
        leftBtn.setOnClickListener(myOnClickListener);

        refreshableView = (RefreshableView) findViewById(R.id.demo_refreshable_view);
        demoAdapter = new DemoListAdapter(JVDemoActivity.this);
        demoListView = (ListView) findViewById(R.id.demo_listview);

        demoListView.setOnItemClickListener(myOnItemClickListener);
        refreshableView.setOnRefreshListener(new PullToRefreshListener() {
            @Override
            public void onRefresh() {
                createDialog("", true);
                GetDemoTask task = new GetDemoTask();
                String[] params = new String[3];
                task.execute(params);
            }

            @Override
            public void onLayoutTrue() {
                refreshableView.finishRefreshing();
            }

        }, 0);

        createDialog("", true);
        GetDemoTask task = new GetDemoTask();
        String[] params = new String[3];
        task.execute(params);

    }

    OnClickListener myOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left:
                    JVDemoActivity.this.finish();
                    break;
                case R.id.btn_right:
                    if (!isbig) {
                        demoAdapter.setData(demoList, false);
                        demoAdapter.notifyDataSetChanged();
                        isbig = true;
                    } else {
                        demoAdapter.setData(demoList, true);
                        demoAdapter.notifyDataSetChanged();
                        isbig = false;
                    }
                    demoListView.setAdapter(demoAdapter);
                    break;
                default:
                    break;
            }
        }
    };

    OnItemClickListener myOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {

            Device dev = demoList.get(arg2);
            ArrayList<Device> playList = PlayUtil
                    .prepareConnect(demoList, arg2);

            // String devJsonString = Device.listToString(demoList);
            Intent intentPlay = new Intent(JVDemoActivity.this,
                    JVPlayActivity.class);
            intentPlay.putExtra(Consts.KEY_PLAY_DEMO, playList.toString());
            intentPlay.putExtra("DeviceIndex", arg2);
            intentPlay.putExtra("ChannelofChannel", dev.getChannelList()
                    .toList().get(0).getChannel());
            // intentPlay.putExtra("DevJsonString", devJsonString);
            intentPlay.putExtra("PlayFlag", Consts.PLAY_DEMO);
            // MySharedPreference.putString(Consts.KEY_PLAY_DEMO, jsonStr);

            JVDemoActivity.this.startActivity(intentPlay);

        }

    };

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void saveSettings() {

    }

    @Override
    protected void freeMe() {

    }

    // 设置三种类型参数分别为String,Integer,String
    class GetDemoTask extends AsyncTask<String, Integer, Integer> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {
            int getRes = -1;// 0成功 1失败
            try {
                demoList = DeviceUtil.getDemoDeviceList(Consts.APP_NAME);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (null != demoList && 0 != demoList.size()) {
                getRes = 0;
            } else {
                getRes = 1;
            }
            return getRes;
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
                if (isbig) {
                    demoAdapter.setData(demoList, false);
                    demoAdapter.notifyDataSetChanged();
                } else {
                    demoAdapter.setData(demoList, true);
                    demoAdapter.notifyDataSetChanged();
                }
                demoListView.setAdapter(demoAdapter);
            } else {
                showTextToast(R.string.demo_get_failed);
            }
            refreshableView.finishRefreshing();
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
            createDialog("", true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新进度,此方法在主线程执行，用于显示任务执行的进度。
        }
    }

}
