
package com.jovision.activities;

import android.content.Context;
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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.MainApplication;
import com.jovision.commons.GetDemoTask;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.AccountUtil;
import com.jovision.utils.CommonUtil;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.GetPhoneNumber;
import com.jovision.utils.JSONUtil;
import com.jovision.utils.MobileUtil;
import com.jovision.views.popw;
import com.tencent.stat.StatService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;

/**
 * 个人中心
 */
public class JVProfileActivity extends ShakeActivity {

    private final String TAG = "JVProfileActivity";
    private JVProfileActivity mInstance;
    private RelativeLayout mHeaderLayout;
    private ListView mListView;
    private MainListAdapter mAdapter;
    private ImageView mHeadImage;
    private int mListViewCount;
    private JSONArray mDataJsonArray;
    private LinearLayout loadFailedLayout;
    // 账号名称
    private TextView mUserName;
    // 账号名称(用来存储获取到的用户名)
    private String more_name;
    private GetPhoneNumber phoneNumber;
    // 存放头像的文件
    private File file;
    // 旧头像文件
    File tempFile;
    // 新头像文件
    File newFile;

    private boolean isgetemail;
    private String hasbandEmail = "";
    private String hasbandPhone = "";
    private String hasnicknameString = "";
    private String usernameInfo = "";

    private popw popupWindow; // 声明PopupWindow对象；
    public static boolean localFlag = false;// 本地登陆标志位
    private static final int PHOTO_REQUEST_TAKEPHOTO = 1;// 拍照
    private static final int PHOTO_REQUEST_GALLERY = 2;// 从相册中选择
    private static final int PHOTO_REQUEST_CUT = 3;// 结果

    private final int RESULT_OK = 1;
    private final String NOTE_OK = "1"; // 0 未开启, 1开启
    private String mNoteListUrl, mNoteDetailUrl;

    @Override
    protected void initSettings() {
        mInstance = this;
        localFlag = Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN));
    }

    @Override
    protected void initUi() {
        setContentView(R.layout.profile_layout);
        initViews();
        initJson();
        initDatas();
        initListeners();
    }

    private void initViews() {
        mHeaderLayout = (RelativeLayout) findViewById(R.id.header_rlyt);
        mHeadImage = (ImageView) findViewById(R.id.head_img);
        mUserName = (TextView) findViewById(R.id.more_uesrname);

        mListView = (ListView) findViewById(R.id.main_lv);

        loadFailedLayout = (LinearLayout) findViewById(R.id.loadfailedlayout);
        loadFailedLayout.setVisibility(View.GONE);

        rightBtn = (Button) findViewById(R.id.btn_right);
        rightBtn.setVisibility(View.GONE);
        leftBtn = (Button) findViewById(R.id.btn_left);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
    }

    private void initDatas() {
        // 标题
        currentMenu.setText(R.string.person_center);
        // 放大左侧箭头的点击区域
        CommonUtil.expandViewTouchDelegate(leftBtn, CommonUtil.dp2px(this, 50));
        checkUserDatas();
    }

    private void initListeners() {
        // 头部
        mHeaderLayout.setOnClickListener(myOnClickListener);
        // 左上角按钮
        leftBtn.setOnClickListener(myOnClickListener);
        // 头像
        mHeadImage.setOnClickListener(myOnClickListener);
        // 用户名
        mUserName.setOnClickListener(myOnClickListener);
        // listview
        mListView.setOnItemClickListener(myOnItemClickListener);
        // 加载失败界面
        loadFailedLayout.setOnClickListener(myOnClickListener);
    }

    /**
     * 加载json数据
     */
    private void initJson() {

        String noteSwitch = statusHashMap.get(Consts.PROFILE_NOTE_SWITCH);

        if (noteSwitch != null && noteSwitch.equals(NOTE_OK)) {
            mNoteListUrl = statusHashMap.get(Consts.PROFILE_NOTELISTURL);
            mNoteDetailUrl = statusHashMap.get(Consts.PROFILE_NOTEDETAILURL);
            MyLog.v(TAG, "--note list url:" + mNoteListUrl);
            MyLog.v(TAG, "--note detail url:" + mNoteDetailUrl);
        } else {
            MyLog.e(TAG, "--note switch is closed--");
            loadFailedLayout.setVisibility(View.VISIBLE);
            return;
        }

        // 进度提示框
        createDialog("", false);
        new Thread(new Runnable() {

            @Override
            public void run() {
                mDataJsonArray = new JSONArray();
                int errorCode = -1;
                try {
                    JSONArray dataArray = JSONUtil.getJSON(mNoteListUrl);
                    if (dataArray != null && dataArray.length() > 0) {
                        mListViewCount = dataArray.length();
                        mDataJsonArray = dataArray;
                        errorCode = 1;
                    }
                } catch (Exception e) {
                    MyLog.e(TAG, e);
                }

                JVProfileActivity.this.runOnUiThread(new UiThreadRunnable(
                        errorCode));
            }

        }).start();

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN))
                && "".equals(MySharedPreference.getString("USERINFO"))) {
            isgetemail = false;
            CheckUserInfoTask task = new CheckUserInfoTask();
            task.execute(more_name);
        } else if (!"".equals(MySharedPreference.getString("USERINFO"))) {
            Bitmap bitmap = BitmapFactory.decodeFile(Consts.HEAD_PATH
                    + MySharedPreference.getString("USERINFO")
                    + Consts.IMAGE_JPG_KIND);
            if (bitmap != null) {
                mHeadImage.setImageBitmap(bitmap);
            } else {
                Log.i("TAG", "--bitmap == null--");
            }
        } else if (Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN))) {
            file = new File(Consts.HEAD_PATH);
            MobileUtil.createDirectory(file);
            tempFile = new File(Consts.HEAD_PATH + more_name + ".jpg");
            newFile = new File(Consts.HEAD_PATH + more_name + "1.jpg");

            if (null != tempFile && tempFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(Consts.HEAD_PATH
                        + more_name + Consts.IMAGE_JPG_KIND);
                mHeadImage.setImageBitmap(bitmap);
            }
        }

        if (!"".equals(MySharedPreference.getString("ACCOUNT"))
                && null != MySharedPreference.getString("ACCOUNT")) {
            more_name = MySharedPreference.getString("ACCOUNT");
        }

        // 设置账号名称
        mUserName.setText(more_name);
    }

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

    @Override
    public void onBackPressed() {
        executeAnimLeftinRightout();
    }

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.GET_DEMO_TASK_FINISH:// 异步执行完成
                MyLog.v(TAG, "--[GetDemoTask] has finished--");
                loadFailedLayout.setVisibility(View.GONE);
                initJson();
                break;
            default:
        }
    }

    /**
     * click事件处理
     */
    OnClickListener myOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left:
                    executeAnimLeftinRightout();
                    break;
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
                case R.id.more_uesrname:
                case R.id.header_rlyt:
                case R.id.head_img:
                    isgetemail = true;
                    if (!localFlag) {
                        createDialog("", true);
                        CheckUserInfoTask task = new CheckUserInfoTask();
                        task.execute(more_name);
                    } else {
                        StatService.trackCustomEvent(mInstance,
                                "census_moreheadimg", mInstance.getResources()
                                        .getString(R.string.census_moreheadimg));
                        popupWindow = new popw(mInstance, myOnClickListener);
                        popupWindow.setBackgroundDrawable(null);
                        popupWindow.setOutsideTouchable(true);
                        popupWindow.showAtLocation(getWindow().getDecorView(),
                                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0); // 设置layout在PopupWindow中显示的位置
                    }
                    break;
                case R.id.loadfailedlayout:
                    if (!ConfigUtil.isConnected(mInstance)) {
                        mInstance.alertNetDialog();
                    } else {

                        if ("false".equals(statusHashMap
                                .get(Consts.KEY_INIT_ACCOUNT_SDK))) {
                            MyLog.e("Login", "初始化账号SDK失败");
                            ConfigUtil.initAccountSDK(((MainApplication) mInstance
                                    .getApplication()));// 初始化账号SDK
                        }
                        // 重新请求url地址,然后进行跳转
                        GetDemoTask UrlTask = new GetDemoTask(mInstance);
                        String[] demoParams = new String[3];
                        demoParams[1] = String.valueOf(Consts.GET_DEMO_TASK_FINISH);
                        UrlTask.execute(demoParams);
                    }
                    break;
                default:
            }
        }
    };

    /**
     * ListView click事件处理
     */
    OnItemClickListener myOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            try {
                JSONObject obj = mDataJsonArray.getJSONObject(position);
                // 贴子地址
                String noteId = obj.getString("url");
                StringBuffer noteUrl = new StringBuffer(mNoteDetailUrl);
                String url = noteUrl.append(noteId).toString();
                Intent intent = new Intent(JVProfileActivity.this,
                        JVWebViewActivity.class);
                intent.putExtra("URL", url);
                intent.putExtra("title", -2);
                startActivity(intent);
                MyLog.v(TAG, url);
            } catch (Exception e) {
                MyLog.e(TAG, e);
            }
        }

    };

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
                    mHeadImage.setImageBitmap(bitmap);
                }

                if ((null == strMail || strMail.equals(""))
                        && (null == strPhone || strPhone.equals(""))) {
                    MySharedPreference.putBoolean("ISSHOW", true);
                    onNotify(Consts.WHAT_BIND, 0, 0, null);
                }
                if (null != strMail && !strMail.equals("")) {
                    hasbandEmail = strMail;
                    MySharedPreference.putString("EMAIL", strMail);
                } else {
                    hasbandEmail = "noemail";
                }
                if (null != strPhone && !strPhone.equals("")) {
                    hasbandPhone = strPhone;
                } else {
                    hasbandPhone = "nophone";
                }
                if (isgetemail) {
                    Intent intentmore = new Intent(mInstance,
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
                mInstance.showTextToast(R.string.str_video_load_failed);
            }

            // 关闭对话框
            mInstance.dismissDialog();
        }

        @Override
        protected void onPreExecute() {
            // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
        }
    }

    /**
     * 处理最终的结果(主线程)
     */
    class UiThreadRunnable implements Runnable {

        int resultCode;

        UiThreadRunnable(int code) {
            resultCode = code;
        }

        @Override
        public void run() {
            // 取消进度框
            dismissDialog();
            if (resultCode == RESULT_OK) {
                mAdapter = new MainListAdapter(getApplicationContext());
                mListView.setAdapter(mAdapter);
            } else {
                loadFailedLayout.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * 个人中心贴子列表
     */
    class MainListAdapter extends BaseAdapter {
        public MainListAdapter(Context context) {
            mContext = context;
        }

        public int getCount() {
            return mListViewCount;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(
                        R.layout.profile_lv_item, null);
                holder = new ViewHolder();
                holder.text = (TextView) convertView
                        .findViewById(R.id.item_text);
                holder.icon_rlyt = (RelativeLayout) convertView
                        .findViewById(R.id.item_icon_rlty);
                holder.icon_text = (TextView) convertView
                        .findViewById(R.id.item_icon_text);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // 列表内容处理
            deployListLayoutByTag(holder, position);

            return convertView;
        }

        private Context mContext;
    }

    static class ViewHolder {
        private RelativeLayout icon_rlyt;
        private TextView text, icon_text;
    }

    /**
     * 根据不同的情况创建不同的列表
     * 
     * @param viewHolder
     * @param position
     */
    private void deployListLayoutByTag(ViewHolder viewHolder, int position) {

        try {
            JSONObject obj = mDataJsonArray.getJSONObject(position);
            // 类型
            int type = obj.getInt("type");
            // 标题
            String title = obj.getString("title");

            switch (type) {
                case 1:// 置顶
                    viewHolder.icon_text.setText(R.string.note_stick);
                    viewHolder.icon_text.setTextColor(getResources().getColor(
                            R.color.list_item_icon_discuss));
                    viewHolder.text.setText(title);
                    viewHolder.icon_rlyt
                            .setBackgroundResource(R.drawable.profile_icon_red_bg);
                    break;
                case 2:// 精华
                    viewHolder.icon_text.setText(R.string.note_best);
                    viewHolder.icon_text.setTextColor(getResources().getColor(
                            R.color.list_item_icon_bbs));
                    viewHolder.text.setText(title);
                    viewHolder.icon_rlyt
                            .setBackgroundResource(R.drawable.profile_icon_blue_bg);
                default:
            }
        } catch (Exception e) {
            MyLog.e(TAG, e);
        }

    }

    /**
     * Activity切换动画 - 左进右出
     */
    private void executeAnimLeftinRightout() {
        JVProfileActivity.this.finish();
        // 设置切换动画，从左边进入,右边退出
        overridePendingTransition(R.anim.enter_left, R.anim.exit_right);
    }

    /**
     * Activity切换动画 - 右进左退
     */
    private void executeAnimRightinLeftout() {
        // 设置切换动画，从右边进入,左边退出
        overridePendingTransition(R.anim.enter_right, R.anim.exit_left);
    }

    /**
     * 检查设置账号信息
     */
    private void checkUserDatas() {
        MySharedPreference.putBoolean("ISPHONE", false);
        MySharedPreference.putBoolean("ISEMAIL", false);
        if (Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN))) {
            more_name = getResources().getString(R.string.location_login);
        } else {
            more_name = statusHashMap.get(Consts.KEY_USERNAME);
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

    /**
     * 将进行剪裁后的图片显示到UI界面上
     * 
     * @param picdata
     */
    private void setPicToView(Intent picdata) {
        Bundle bundle = picdata.getExtras();
        if (bundle != null) {
            Bitmap photo = bundle.getParcelable("data");
            saveBitmap(photo);
            Drawable drawable = new BitmapDrawable(photo);
            mHeadImage.setImageDrawable(drawable);
        }
    }

    /**
     * 保存图片
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

}
