
package com.jovision.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.test.JVACCOUNT;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.MainApplication;
import com.jovision.activities.JVTabActivity.OnMainListener;
import com.jovision.adapters.FragmentAdapter;
import com.jovision.bean.MoreFragmentBean;
import com.jovision.commons.GetDemoTask;
import com.jovision.commons.JVAlarmConst;
import com.jovision.commons.MyActivityManager;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.AccountUtil;
import com.jovision.utils.BitmapCache;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.GetPhoneNumber;
import com.jovision.utils.JsonFileReader;
import com.jovision.utils.ListViewUtil;
import com.jovision.utils.MobileUtil;
import com.jovision.utils.UserUtil;
import com.jovision.views.AlarmDialog;
import com.jovision.views.popw;
import com.tencent.stat.StatService;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 更多
 */
public class JVMoreFragment extends BaseFragment implements OnMainListener {

    private final String TAG = "JVMoreFragment";
    // assets/profile.json中个人中心部分对应的标志
    private final String JSONTAG = "profile";
    // 本地登陆标志位
    private boolean localFlag = false;
    // Adapter 存储模块文字和图标
    private List<MoreFragmentBean> dataList;
    // 模块listView
    private ListView more_listView;
    // listView 适配器
    private FragmentAdapter adapter;
    // Fragment依附的activity
    private Activity activity;
    // 头像
    private ImageView more_head;
    // 用户名称
    private TextView more_username;
    // 用户名
    private String more_name;
    // 个人中心部分
    private RelativeLayout more_usertop;

    private boolean isgetemail;

    private GetPhoneNumber phoneNumber;

    protected static popw popupWindow; // 声明PopupWindow对象；
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果
    // 存放头像的文件夹
    File file;
    // 旧头像文件
    File tempFile;
    // 新头像文件
    File newFile;

    private String hasbandEmail = "";
    private String hasbandPhone = "";
    private String hasnicknameString = "";
    private String usernameInfo = "";

    private MainApplication mApp;
    private final String OPEN = "1";// 开通

    // 消息数量更新的回调接口,通过此接口可以操作JVTabActivity
    public interface OnFuncActionListener {
        public void OnFuncEnabled(int func_index, int enabled);

        public void OnFuncSelected(int func_index, String params);
    }

    private OnFuncActionListener mListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_more, container, false);
        mApp = (MainApplication) getActivity().getApplication();
        intiUi(view);
        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFuncActionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement OnFuncEnabledListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mParent = getView();
        mActivity = (BaseActivity) getActivity();

        localFlag = Boolean.valueOf(mActivity.statusHashMap
                .get(Consts.LOCAL_LOGIN));
        currentMenu.setText(R.string.person_center);
        rightBtn.setVisibility(View.GONE);
        leftBtn.setVisibility(View.GONE);
    }

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.WHAT_PUSH_MESSAGE:
                // 弹出报警信息对话框
                if (null != mActivity) {
                    // 向JVTabActivity发送通知,重新计算消息数量
                    mActivity.onNotify(Consts.NEW_PUSH_MSG_TAG_PRIVATE, 0, 0, null);
                    // 通知显示报警信息条数
                    int new_alarm_nums = mApp.getNewPushCnt();
                    adapter.setNewNums(new_alarm_nums);
                    adapter.notifyDataSetChanged();
                    new AlarmDialog(mActivity).Show(obj);
                } else {
                    MyLog.e("Alarm",
                            "onHandler mActivity is null ,so dont show the alarm dialog");
                }
                break;
            case Consts.WHAT_BIND:
                // more_bindmail.setVisibility(View.VISIBLE);
                break;
            case Consts.NEW_BBS:
                if (null != adapter) {
                    adapter.setBBSNums(arg1);
                    adapter.notifyDataSetChanged();
                }
                // mActivity.showTextToast("获得结果");
                break;
        }

    }

    private void intiUi(View view) {
        activity = getActivity();
        MySharedPreference.putBoolean("ISPHONE", false);
        MySharedPreference.putBoolean("ISEMAIL", false);
        if (Boolean.valueOf(((BaseActivity) activity).statusHashMap
                .get(Consts.LOCAL_LOGIN))) {
            more_name = activity.getResources().getString(
                    R.string.location_login);
        } else {
            more_name = ((BaseActivity) activity).statusHashMap
                    .get(Consts.KEY_USERNAME);
            MySharedPreference.putString("ACCOUNT", more_name);
        }
        if (AccountUtil.verifyEmail(more_name)) {
            MySharedPreference.putBoolean("ISEMAIL", true);
        } else {
            MySharedPreference.putBoolean("ISEMAIL", false);
        }
        phoneNumber = new GetPhoneNumber(more_name);
        if (5 != phoneNumber.matchNum() && 4 != phoneNumber.matchNum()) {
            MySharedPreference.putBoolean("ISPHONE", true);
        } else {
            MySharedPreference.putBoolean("ISPHONE", false);
        }

        more_username = (TextView) view.findViewById(R.id.more_uesrname);
        more_head = (ImageView) view.findViewById(R.id.more_head_img);
        more_usertop = (RelativeLayout) view.findViewById(R.id.more_usertop);

        more_listView = (ListView) view.findViewById(R.id.more_listView);

        more_usertop.setOnClickListener(myOnClickListener);
        more_username.setOnClickListener(myOnClickListener);
        more_head.setOnClickListener(myOnClickListener);
        listViewClick();

        MySharedPreference.putString("REBINDPHONE", "");
        MySharedPreference.putString("REBINDEMAIL", "");

        // 初始化数据
        initDatalist();
    }

    @Override
    public void onResume() {
        MyLog.e(TAG, "the JVMoreFragment onResume invoke~~~");
        super.onResume();

        // 刷新Adapter
        if (adapter != null) {
            // 通知显示报警信息条数
            int new_alarm_nums = mApp.getNewPushCnt();
            adapter.setNewNums(new_alarm_nums);
            adapter.notifyDataSetChanged();
        }

        if (!localFlag && "".equals(MySharedPreference.getString("USERINFO"))) {
            isgetemail = false;
            CheckUserInfoTask task = new CheckUserInfoTask();
            task.execute(more_name);
        } else if (!"".equals(MySharedPreference.getString("USERINFO"))) {
            Bitmap bitmap = BitmapFactory.decodeFile(Consts.HEAD_PATH
                    + MySharedPreference.getString("USERINFO")
                    + Consts.IMAGE_JPG_KIND);
            if (bitmap != null) {
                more_head.setImageBitmap(bitmap);
            } else {
                MyLog.v(TAG, "--bitmarp is null--");
            }
        } else if (localFlag) {
            file = new File(Consts.HEAD_PATH);
            MobileUtil.createDirectory(file);
            tempFile = new File(Consts.HEAD_PATH + more_name + ".jpg");
            newFile = new File(Consts.HEAD_PATH + more_name + "1.jpg");

            if (null != tempFile && tempFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(Consts.HEAD_PATH
                        + more_name + Consts.IMAGE_JPG_KIND);
                more_head.setImageBitmap(bitmap);
            }
        }
        // if (MySharedPreference.getBoolean("ISSHOW", false)) {
        // more_bindmail.setVisibility(View.VISIBLE);
        // }
        if (!"".equals(MySharedPreference.getString("ACCOUNT"))
                && null != MySharedPreference.getString("ACCOUNT")) {
            more_name = MySharedPreference.getString("ACCOUNT");
        }
        more_username.setText(more_name);
    }

    /**
     * onClick 事件
     */
    OnClickListener myOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.pop_outside:
                    popupWindow.dismiss();
                    break;
                case R.id.btn_pick_photo: {
                    popupWindow.dismiss();
                    Intent intent = new Intent(Intent.ACTION_PICK, null);
                    intent.setDataAndType(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                    startActivityForResult(intent, PHOTO_REQUEST_GALLERY);
                    break;
                }
                case R.id.btn_take_photo:
                    // 调用系统的拍照功能
                    popupWindow.dismiss();
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    // 指定调用相机拍照后照片的储存路径
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
                    startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
                    break;
                case R.id.btn_cancel:
                    popupWindow.dismiss();
                    break;
                case R.id.more_usertop:// 全部个人中心区域
                    if (!localFlag) {// 非访客
                        mActivity.createDialog("", true);
                        CheckUserInfoTask task = new CheckUserInfoTask();
                        task.execute(more_name);
                    }
                    break;
                case R.id.more_uesrname:// 账号名称
                case R.id.more_head_img:// 账号头像
                    isgetemail = true;
                    if (!localFlag) {// 非访客
                        mActivity.createDialog("", true);
                        CheckUserInfoTask task = new CheckUserInfoTask();
                        task.execute(more_name);
                    } else {// 访客
                        StatService.trackCustomEvent(mActivity,
                                "census_moreheadimg", mActivity.getResources()
                                        .getString(R.string.census_moreheadimg));
                        popupWindow = new popw(mActivity, myOnClickListener);
                        popupWindow.setBackgroundDrawable(null);
                        popupWindow.setOutsideTouchable(true);
                        // 设置layout在PopupWindow中显示的位置
                        popupWindow.showAtLocation(mActivity.getWindow()
                                .getDecorView(), Gravity.BOTTOM
                                | Gravity.CENTER_HORIZONTAL, 0, 0);
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PHOTO_REQUEST_TAKEPHOTO:
                if (resultCode == -1) {
                    startPhotoZoom(Uri.fromFile(newFile), 300);
                }
                break;

            case PHOTO_REQUEST_GALLERY:
                if (data != null)
                    startPhotoZoom(data.getData(), 300);
                break;

            case PHOTO_REQUEST_CUT:
                if (data != null)
                    setPicToView(data);
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startPhotoZoom(Uri uri, int size) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", size);
        intent.putExtra("outputY", size);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }

    // 将进行剪裁后的图片显示到UI界面上
    private void setPicToView(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (bundle != null) {
            Bitmap photo = bundle.getParcelable("data");
            saveBitmap(photo);
            Drawable drawable = new BitmapDrawable(photo);
            // more_head.setBackgroundDrawable(drawable);
            more_head.setImageDrawable(drawable);
        }
    }

    /**
     * 图像保存
     * 
     * @param bm
     */
    public void saveBitmap(Bitmap bm) {
        if (null == bm) {
            return;
        }
        File f;
        if (localFlag) {
            f = new File(Consts.HEAD_PATH + more_name + ".jpg");
        } else {
            f = new File(Consts.HEAD_PATH + usernameInfo + ".jpg");
        }
        if (f.exists()) {
            f.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 初始化数据和加载数据的操作
     */
    private void initDatalist() {
        // 新起线程获取json数据
        new DataThread().start();
    }

    /**
     * 加载数据线程
     */
    class DataThread extends Thread {

        @Override
        public void run() {
            dataList = new ArrayList<MoreFragmentBean>();
            String jsonStr = JsonFileReader.getJson(activity.getBaseContext(),
                    "profile.json");
            dataList = JsonFileReader.getDataListByJson(
                    activity.getBaseContext(), jsonStr, JSONTAG);
            // 数据检查,条件判断等操作
            checkDatas();
            activity.runOnUiThread(new UiThreadRunnable());
        }

    }

    /**
     * 处理最终的结果(主线程)
     */
    class UiThreadRunnable implements Runnable {

        @Override
        public void run() {
            adapter = new FragmentAdapter(JVMoreFragment.this, dataList);

            int alarm_new_nums = mApp.getNewPushCnt();
            adapter.setNewNums(alarm_new_nums);

            more_listView.setAdapter(adapter);
            ListViewUtil.setListViewHeightBasedOnChildren(more_listView);
        }

    }

    /**
     * 数据检查,条件判断重置等操作
     */
    private void checkDatas() {
        for (MoreFragmentBean bean : dataList) {
            if (bean.getItemFlag().equals(Consts.PROFILE_ITEM_SERVICE)) {
                /** 我的服务 **/
                // 我的服务中的云服务开通状态未开启|访客|不在中国的情况,不显示我的服务
                if (!OPEN.equals(((BaseActivity) mActivity).statusHashMap
                        .get(Consts.MORE_CLOUD_SWITCH))
                        || localFlag
                        || ConfigUtil.getServerLanguage() != 1) {
                    bean.setDismiss(true);
                }
            } else if (bean.getItemFlag().equals(Consts.PROFILE_ITEM_SETTING)) {
                /** 功能设置 **/
                // 我的服务不显示的情况,功能设置底部的短线变为长线
                if (!OPEN.equals(((BaseActivity) mActivity).statusHashMap
                        .get(Consts.MORE_CLOUD_SWITCH))
                        || localFlag
                        || ConfigUtil.getServerLanguage() != 1) {
                    bean.setLast(true);
                }
            }
        }
    }

    /**
     * ListView的OnClick事件
     */
    private void listViewClick() {
        more_listView
                .setOnItemClickListener(new AdapterView.OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {

                        // 退出登陆
                        if (dataList.get(position).getItemFlag()
                                .equals(Consts.PROFILE_ITEM_LOGOUT)) {
                            LogOutTask task = new LogOutTask();
                            String[] strParams = new String[3];
                            task.execute(strParams);
                            return;
                        }
                        // 我的服务
                        if (dataList.get(position).getItemFlag()
                                .equals(Consts.PROFILE_ITEM_SERVICE)) {
                            Intent intent = new Intent(mActivity,
                                    UserCloudStorgeBriefBillActivity.class);
                            startActivity(intent);
                            executeAnimRightinLeftout();
                            return;
                        }

                        // 跳转到二级页面
                        Intent intent = new Intent(mActivity,
                                JVProfileFunctionActivity.class);
                        intent.putExtra("jsontag", dataList.get(position)
                                .getItemFlag());
                        mActivity.startActivity(intent);
                        executeAnimRightinLeftout();
                    }
                });
    }

    // ------------------------------------------------
    // ## (工程商入驻、社区)公共入口
    // ## (虽然更多改版,但是这部分中间扩展按钮还会调用,所以保持不变)
    // ------------------------------------------------
    /**
     * 扩展功能面板调用的时候, 重新传递activity
     * 
     * @param pActivity
     */
    public void reconfigActivity(BaseActivity pActivity) {
        mActivity = pActivity;
        try {
            mListener = (OnFuncActionListener) mActivity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + "must implement OnFuncEnabledListener");
        }
    }

    /**
     * 工程商入驻
     * 
     * @param pActivity 扩展功能面板调用时传值,当前fragment调用传null
     * @return
     */
    public void gcsurl(BaseActivity pActivity) {
        if (pActivity != null) {
            reconfigActivity(pActivity);
        }

        // // 2015.3.16 我要装监控改为工程商入驻
        // if (!MySharedPreference
        // .getBoolean(Consts.MORE_GCSURL)) {
        // MySharedPreference.putBoolean(
        // Consts.MORE_GCSURL, true);
        // mListener.OnFuncEnabled(0, 1);
        // }
        if (!ConfigUtil.isConnected(mActivity)) {
            mActivity.alertNetDialog();
        } else {
            if (null != ((BaseActivity) mActivity).statusHashMap
                    .get(Consts.MORE_GCSURL)) {
                Intent intentAD0 = new Intent(mActivity,
                        JVWebViewActivity.class);
                intentAD0.putExtra("URL",
                        ((BaseActivity) mActivity).statusHashMap
                                .get(Consts.MORE_GCSURL));
                intentAD0.putExtra("title", -2);
                mActivity.startActivity(intentAD0);
            } else {
                if ("false".equals(mActivity.statusHashMap
                        .get(Consts.KEY_INIT_ACCOUNT_SDK))) {
                    MyLog.e("Login", "初始化账号SDK失败");
                    ConfigUtil.initAccountSDK(((MainApplication) mActivity
                            .getApplication()));// 初始化账号SDK
                }
                GetDemoTask UrlTask = new GetDemoTask(mActivity);
                String[] demoParams = new String[3];
                demoParams[1] = "0";
                UrlTask.execute(demoParams);
            }
        }
    }

    /**
     * 进入社区
     * 
     * @param pActivity 扩展功能面板调用时传值,当前fragment调用传null
     * @return
     */
    public void bbsurl(BaseActivity pActivity) {
        if (pActivity != null) {
            reconfigActivity(pActivity);
        }

        if (!ConfigUtil.isConnected(mActivity)) {
            mActivity.alertNetDialog();
        } else {
            onNotify(Consts.NEW_BBS, 0, 0, null);
            if (null != ((BaseActivity) mActivity).statusHashMap
                    .get(Consts.MORE_BBSNUMURL)
                    && !"".equals(((BaseActivity) mActivity).statusHashMap
                            .get(Consts.MORE_BBSNUMURL))) {
                Intent intentAD0 = new Intent(mActivity,
                        JVWebViewActivity.class);
                intentAD0.putExtra("URL",
                        ((BaseActivity) mActivity).statusHashMap
                                .get(Consts.MORE_BBSNUMURL));
                intentAD0.putExtra("title", -2);
                mActivity.startActivity(intentAD0);
                ((BaseActivity) mActivity).statusHashMap.put(
                        Consts.MORE_BBSNUMURL, "");
            } else {
                if (null != ((BaseActivity) mActivity).statusHashMap
                        .get(Consts.MORE_BBS)) {
                    Intent intentAD0 = new Intent(mActivity,
                            JVWebViewActivity.class);
                    intentAD0.putExtra("URL",
                            ((BaseActivity) mActivity).statusHashMap
                                    .get(Consts.MORE_BBS));
                    intentAD0.putExtra("title", -2);
                    mActivity.startActivity(intentAD0);
                } else {

                    if ("false".equals(mActivity.statusHashMap
                            .get(Consts.KEY_INIT_ACCOUNT_SDK))) {
                        MyLog.e("Login", "初始化账号SDK失败");
                        ConfigUtil.initAccountSDK(((MainApplication) mActivity
                                .getApplication()));// 初始化账号SDK
                    }
                    // 为了扩展功能面板调用不报错, 添加if判断
                    if (null != adapter) {
                        adapter.setBBSNums(0);
                        adapter.notifyDataSetChanged();
                    }
                    GetDemoTask UrlTask2 = new GetDemoTask(mActivity);
                    String[] demoParams2 = new String[3];
                    demoParams2[1] = "3";
                    UrlTask2.execute(demoParams2);
                }
            }
        }
    }

    // ---------------------------END----------------------------------

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        fragHandler.sendMessage(fragHandler
                .obtainMessage(what, arg1, arg2, obj));
    }

    /**
     * 检测账号信息
     */
    class CheckUserInfoTask extends AsyncTask<String, Integer, Integer> {
        String account = "";
        String strResonse = "";
        String strPhone = "";
        String strMail = "";

        @Override
        protected Integer doInBackground(String... params) {
            account = params[0];
            int ret = -1;
            strResonse = JVACCOUNT.GetAccountInfo();
            JSONObject resObject = null;
            Log.i("TAG", strResonse);
            try {
                resObject = new JSONObject(strResonse);
                ret = resObject.optInt("result", -2);
                if (ret == 0) {
                    strPhone = resObject.optString("phone");
                    strMail = resObject.optString("mail");
                    hasnicknameString = resObject.optString("nickname");
                    usernameInfo = resObject.optString("username");
                    Log.i("TAG", usernameInfo);
                    MySharedPreference.putString("USERINFO", usernameInfo);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return ret;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            mActivity.dismissDialog();
            if (result == 0)// ok
            {
                file = new File(Consts.HEAD_PATH);
                MobileUtil.createDirectory(file);
                tempFile = new File(Consts.HEAD_PATH + usernameInfo + ".jpg");
                newFile = new File(Consts.HEAD_PATH + usernameInfo + "1.jpg");

                if (null != tempFile && tempFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(Consts.HEAD_PATH
                            + usernameInfo + Consts.IMAGE_JPG_KIND);
                    Log.i("TAG", Consts.HEAD_PATH + usernameInfo
                            + Consts.IMAGE_JPG_KIND);
                    more_head.setImageBitmap(bitmap);
                }

                if ((strMail.equals("") || null == strMail)
                        && (strPhone.equals("") || null == strPhone)) {
                    MySharedPreference.putBoolean("ISSHOW", true);
                    onNotify(Consts.WHAT_BIND, 0, 0, null);
                }
                if (!strMail.equals("") && null != strMail) {
                    hasbandEmail = strMail;
                    MySharedPreference.putString("EMAIL", strMail);
                } else {
                    hasbandEmail = "noemail";
                }
                if (!strPhone.equals("") && null != strPhone) {
                    hasbandPhone = strPhone;
                } else {
                    hasbandPhone = "nophone";
                }
                if (isgetemail) {
                    Intent intentmore = new Intent(mActivity,
                            JVRebandContactActivity.class);
                    intentmore.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    intentmore.putExtra("phone", hasbandPhone);
                    intentmore.putExtra("email", hasbandEmail);
                    intentmore.putExtra("nickname", hasnicknameString);
                    intentmore.putExtra("username", usernameInfo);
                    startActivity(intentmore);
                    executeAnimRightinLeftout();
                }
            } else {
                mActivity.showTextToast(R.string.str_video_load_failed);
            }
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
        }
    }

    // 设置三种类型参数分别为String,Integer,String
    private class AlarmTask extends AsyncTask<Integer, Integer, Integer> {// A,361,2000
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(Integer... params) {
            int switchRes = -1;
            if (JVAlarmConst.ALARM_ON == params[0]) {// 开报警
                switchRes = JVACCOUNT.SetCurrentAlarmFlag(
                        JVAlarmConst.ALARM_ON, ConfigUtil.getIMEI(mActivity));
                if (0 == switchRes) {
                    MyLog.e("JVAlarmConst.ALARM--ON-", switchRes + "");
                    MySharedPreference
                            .putBoolean(Consts.MORE_ALARMSWITCH, true);
                }
            } else {// 关报警
                switchRes = JVACCOUNT.SetCurrentAlarmFlag(
                        JVAlarmConst.ALARM_OFF, ConfigUtil.getIMEI(mActivity));
                if (0 == switchRes) {
                    MyLog.e("JVAlarmConst.ALARM--CLOSE-", switchRes + "");
                    MySharedPreference.putBoolean(Consts.MORE_ALARMSWITCH,
                            false);
                }
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
            mActivity.dismissDialog();
            adapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
            mActivity.createDialog("", true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新进度,此方法在主线程执行，用于显示任务执行的进度。
        }
    }

    /**
     * 账号注销操作
     */
    class LogOutTask extends AsyncTask<String, Integer, Integer> {// A,361,2000
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {
            int logRes = -1;
            try {
                if (!localFlag) {
                    MyLog.v(TAG, "start-logout");
                    // if (0 != AccountUtil.userLogout()) {
                    AccountUtil.userLogout();
                    // }
                    MyLog.v(TAG, "end-logout");
                    MySharedPreference.putString(Consts.KEY_LAST_LOGIN_USER,
                            more_name);
                    MySharedPreference.putString(Consts.DEVICE_LIST, "");
                }
                // 添加手动注销标志，离线报警使用，如果为手动注销账号，不接收离线报警
                MySharedPreference.putBoolean(Consts.MANUAL_LOGOUT_TAG, true);
                UserUtil.resetAllUser();
                BitmapCache.getInstance().clearAllCache();
                mActivity.statusHashMap.put(Consts.HAS_LOAD_DEMO, "false");
                mActivity.statusHashMap.put(Consts.HAG_GOT_DEVICE, "false");
                mActivity.statusHashMap.put(Consts.ACCOUNT_ERROR, null);
                MySharedPreference.putString("USERINFO", "");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return logRes;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            // 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
            ((BaseActivity) mActivity).dismissDialog();
            ((BaseActivity) mActivity).statusHashMap.put(Consts.MORE_BBS, null);
            // ((BaseActivity) mActivity).statusHashMap.put(Consts.MORE_STATURL,
            // null);
            ((BaseActivity) mActivity).statusHashMap.put(Consts.MORE_GCSURL,
                    null);
            ((BaseActivity) mActivity).statusHashMap.put(Consts.MORE_DEMOURL,
                    null);
            ((BaseActivity) mActivity).statusHashMap.put(
                    Consts.MORE_CLOUD_SHOP, null);

            MySharedPreference.putBoolean("ISSHOW", false);
            MySharedPreference.putString("ACCOUNT", "");
            MyActivityManager.getActivityManager().popAllActivityExceptOne(
                    JVLoginActivity.class);
            Intent intent = new Intent();
            String userName = mActivity.statusHashMap.get(Consts.KEY_USERNAME);
            mActivity.statusHashMap.put(Consts.HAS_LOAD_DEMO, "false");

            clearCacheFolder(mActivity.getCacheDir(),
                    System.currentTimeMillis());

            mActivity.deleteDatabase("webview.db");
            mActivity.deleteDatabase("webviewCache.db");

            intent.putExtra("UserName", userName);
            // MySharedPreference.putBoolean(Consts.MORE_REMEMBER, false);
            intent.setClass(mActivity, JVLoginActivity.class);
            mActivity.startActivity(intent);
            mActivity.finish();
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
            ((BaseActivity) mActivity).createDialog("", true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 更新进度,此方法在主线程执行，用于显示任务执行的进度。
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private int clearCacheFolder(File dir, long numDays) {
        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }
                    if (child.lastModified() < numDays) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return deletedFiles;
    }

    @Override
    public void onMainAction(int packet_type) {
        // TODO Auto-generated method stub

    }

    /**
     * Activity切换动画 - 右进左退
     */
    private void executeAnimRightinLeftout() {
        // 设置切换动画，从右边进入,左边退出
        mActivity.overridePendingTransition(R.anim.enter_right,
                R.anim.exit_left);
    }
}
