
package com.jovision.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.MainApplication;
import com.jovision.commons.GetDemoTask;
import com.jovision.commons.JVDeviceConst;
import com.jovision.commons.MyLog;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.DeviceUtil;
import com.jovision.views.CircleProgressBar;
import com.jovision.views.OnProgressListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;

public class UserCloudStorgeBriefBillActivity extends ShakeActivity implements
        OnClickListener {

    private final String TAG = "UserCloudStorgeBriefBillActivity";
    private TextView tv_charge_left_value;
    private TextView tv_free_left_top_pct;
    private TextView tv_free_left_circle_pct;
    private CircleProgressBar mAbProgressBar;
    private RelativeLayout mCloudService;
    private UserCloudStorgeBriefBillActivity mInstance;
    private ScrollView mScrollView;

    private int max = 100;
    private int progress = 0;
    private final String OPEN = "1";// 开通

    @Override
    protected void initSettings() {
    }

    @Override
    protected void initUi() {
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.cloud_storge_traffic_layout);
        mInstance = this;

        initViews();
        initListeners();
    }

    private void initViews() {
        leftBtn = (Button) findViewById(R.id.btn_left);
        rightBtn = (Button) findViewById(R.id.btn_right);
        rightBtn.setVisibility(View.GONE);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        leftBtn.setOnClickListener(this);
        currentMenu.setText(R.string.person_item_service);

        tv_charge_left_value = (TextView) findViewById(R.id.tv_charge_left);
        tv_free_left_top_pct = (TextView) findViewById(R.id.tv_free_left);
        tv_free_left_circle_pct = (TextView) findViewById(R.id.tv_free_left_cir_pct);

        mAbProgressBar = (CircleProgressBar) findViewById(R.id.circleProgressBar);

        mScrollView = (ScrollView) findViewById(R.id.scrollview_content);
        mCloudService = (RelativeLayout) findViewById(R.id.cloud_service);

    }

    private void initListeners() {
        /** 云服务开通 **/
        if (OPEN.equals(((BaseActivity) mInstance).statusHashMap
                .get(Consts.MORE_CLOUD_SWITCH))) {
            mScrollView.setVisibility(View.VISIBLE);
            mCloudService.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 我的服务-云服务开通
                    cloudurl();
                }
            });
        } else {
            MyLog.v(TAG, "--cloud service not open--");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpViews();
    }

    private void setUpViews() {
        mAbProgressBar.setMax(100);
        mAbProgressBar.setProgress(0);
        mAbProgressBar.setOnProgressListener(new OnProgressListener() {

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onComplete(int progress1) {
                // progress = 0;
                // mAbProgressBar.reset();
            }
        });
        CheckUserFlowTask task = new CheckUserFlowTask();
        String[] params = new String[3];
        task.execute(params);
    }

    /**
     * 云服务开通
     */
    public void cloudurl() {
        if (!ConfigUtil.isConnected(mInstance)) {
            mInstance.alertNetDialog();
        } else {
            if (null != ((BaseActivity) mInstance).statusHashMap
                    .get(Consts.MORE_CLOUD_SHOP)) {
                Intent intent = new Intent(mInstance, JVWebViewActivity.class);
                intent.putExtra("URL", ((BaseActivity) mInstance).statusHashMap
                        .get(Consts.MORE_CLOUD_SHOP));
                intent.putExtra("title", -2);
                mInstance.startActivity(intent);
            } else {
                if ("false".equals(mInstance.statusHashMap
                        .get(Consts.KEY_INIT_ACCOUNT_SDK))) {
                    MyLog.e("Login", "初始化账号SDK失败");
                    ConfigUtil.initAccountSDK(((MainApplication) mInstance
                            .getApplication()));// 初始化账号SDK
                }
                GetDemoTask UrlTask = new GetDemoTask(mInstance);
                String[] demoParams = new String[3];
                demoParams[1] = "6";
                UrlTask.execute(demoParams);
            }
        }
    }

    class CheckUserFlowTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            String resJson = DeviceUtil.getUserSurFlow();
            Log.e("cloud", "check flow res:" + resJson);
            return resJson;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(String result) {

            JSONObject resObj;
            try {
                resObj = new JSONObject(result);

                int ret = resObj.optInt(JVDeviceConst.JK_RESULT);
                if (ret != 0) {
                    showTextToast(getResources().getString(
                            R.string.str_cloud_query_error_1)
                            + ":" + ret);
                    dismissDialog();
                    finish();
                    return;
                } else {
                    // 构造方法的字符格式这里如果小数不足2位,会以0补足.
                    DecimalFormat decimalFormat = new DecimalFormat("0.0");
                    int fee_type = resObj.optInt(
                            JVDeviceConst.JK_CLOUD_FEE_TYPE, 0);
                    if (fee_type == 0) {
                        // 单位流量
                        double flow_mb = (double) resObj.optInt(
                                JVDeviceConst.JK_CLOUD_STORAGE_FLOW, 0)
                                / (double) 1024;
                        double flow_gb = 0.0;
                        if (flow_mb > 1000) {
                            // 大于1000M显示G
                            flow_gb = flow_mb / (double) 1024;
                            String strFlowGB = decimalFormat.format(flow_gb);
                            tv_charge_left_value.setText(strFlowGB + "G");
                        } else {
                            String strFlowMB = decimalFormat.format(flow_mb);
                            tv_charge_left_value.setText(strFlowMB + "M");
                        }

                    } else if (fee_type == 1) {
                        // 单位元
                        double flow_money = resObj.optInt(
                                JVDeviceConst.JK_CLOUD_STORAGE_FLOW, 0);
                        tv_charge_left_value.setText(String.valueOf(flow_money)
                                + "元");
                    }
                    max = resObj
                            .optInt(JVDeviceConst.JK_CLOUD_STORAGE_FFREE, 0);
                    progress = resObj.optInt(
                            JVDeviceConst.JK_CLOUD_STORAGE_FFREE_USE, 0);
                    mAbProgressBar.setMax(max);
                    int left = max - progress;
                    double left_pct = 0;
                    if (left > 0 && left <= max) {
                        double db_pct = (double) left / (double) max;
                        left_pct = db_pct * 100;
                        mAbProgressBar.setProgress(left);
                    }
                    tv_free_left_top_pct.setText(decimalFormat.format(left_pct)
                            + "%");
                    tv_free_left_circle_pct.setText(decimalFormat
                            .format(left_pct) + "%");

                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            dismissDialog();
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
            createDialog("", true);
        }
    }

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void saveSettings() {
        // TODO Auto-generated method stub

    }

    @Override
    protected void freeMe() {
        // TODO Auto-generated method stub

    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_left:
                finish();
                overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
                break;

            default:
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
    }

}
