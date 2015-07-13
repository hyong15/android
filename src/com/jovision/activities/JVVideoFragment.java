
package com.jovision.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.MainApplication;
import com.jovision.activities.JVTabActivity.OnMainListener;
import com.jovision.commons.GetDemoTask;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.commons.Url;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.JSONUtil;
import com.jovision.utils.MobileUtil;
import com.jovision.utils.UploadUtil;
import com.jovision.views.AlarmDialog;

import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Stack;

@SuppressLint("NewApi")
public class JVVideoFragment extends BaseFragment implements OnMainListener {

    private static final String TAG = "JVWebViewActivity";
    private LinearLayout loadinglayout;
    /** topBar **/
    private RelativeLayout topBar;

    public static WebView webView;
    private String urls = "";
    private int titleID = 0;
    private ImageView loadingBar;
    private View rootView;
    private boolean isshow = false;
    String sid = "";
    String lan = "";

    private RelativeLayout loadFailedLayout;
    private ImageView reloadImgView;
    private boolean loadFailed = false;
    private boolean isConnected = false;

    Stack<String> titleStack = new Stack<String>();// 标题栈，后进先出

    protected static final int REQUEST_CODE_IMAGE_CAPTURE = 0;
    protected static final int REQUEST_CODE_IMAGE_SELECTE = 1;
    protected static final int REQUEST_CODE_IMAGE_CROP = 2;
    // /* 拍照的照片存储位置 */
    // private static final File PHOTO_DIR = new File(
    // Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    // 照相机拍照得到的图片
    private File mCurrentPhotoFile;
    // 缓存图片URI
    private Uri imageTempUri = null;
    private String uploadUrl = "";// "http://bbs.cloudsee.net/misc.php?mod=swfupload&operation=upload&type=image&inajax=yes&infloat=yes&simple=2&uid=1";

    private Dialog initDialog;
    private RelativeLayout capture_Load;
    private RelativeLayout select_Load;
    private RelativeLayout captureparent;
    private ImageView dialog_cancle_img;
    private TextView capturetext;
    private TextView selecttext;
    private View view;

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {

            case Consts.BBS_IMG_UPLOAD_SUCCESS: {
                mActivity.dismissDialog();
                if (null != obj) {
                    // mActivity.showTextToast(obj.toString());
                    webView.loadUrl("javascript:uppic(\"" + obj.toString() + "\")");
                } else {
                    // mActivity.showTextToast("null");
                }

                break;
            }
            case Consts.TAB_PLAZZA_RELOAD_URL: {

                if (null != obj && !"".equalsIgnoreCase(obj.toString())) {
                    isshow = false;
                    urls = obj.toString();
                    webView.loadUrl(urls);
                }
                break;
            }
            case Consts.TAB_WEBVIEW_BACK: {// tab点击返回
                try {
                    if (null != titleStack && 0 != titleStack.size()) {
                        titleStack.pop();
                        String lastTitle = titleStack.peek();
                        currentMenu.setText(ConfigUtil.getShortTile(lastTitle));
                    }
                    webView.goBack(); // goBack()表示返回WebView的上一页面
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }

            case Consts.WHAT_DEMO_URL_SUCCESS: {
                mActivity.dismissDialog();
                HashMap<String, String> paramMap = (HashMap<String, String>) obj;
                Intent intentAD = new Intent(mActivity, JVWebView2Activity.class);
                intentAD.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intentAD.putExtra("URL", paramMap.get("new_url"));
                intentAD.putExtra("title", -2);
                intentAD.putExtra("rtmp", paramMap.get("rtmpurl"));
                intentAD.putExtra("cloudnum", paramMap.get("cloud_num"));
                intentAD.putExtra("channel", paramMap.get("channel"));

                mActivity.startActivity(intentAD);
                break;
            }
            case Consts.WHAT_DEMO_URL_FAILED: {
                // TODO
                mActivity.dismissDialog();
                mActivity.showTextToast(R.string.str_video_load_failed);
                break;
            }
            case Consts.WHAT_PUSH_MESSAGE:
                // 弹出对话框
                if (null != mActivity) {
                    mActivity.onNotify(Consts.NEW_PUSH_MSG_TAG_PRIVATE, 0, 0, null);//
                    new AlarmDialog(mActivity).Show(obj);
                    // onResume();
                } else {
                    MyLog.e("Alarm",
                            "onHandler mActivity is null ,so dont show the alarm dialog");
                }
                break;
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        fragHandler.sendMessage(fragHandler
                .obtainMessage(what, arg1, arg2, obj));
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.demovideo_layout, container,
                    false);
        }
        ViewGroup parent = (ViewGroup) rootView.getParent();
        if (parent != null) {
            parent.removeView(rootView);
        }
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (null != ((BaseActivity) mActivity).statusHashMap
                .get(Consts.MORE_DEMOURL)) {
            urls = ((BaseActivity) mActivity).statusHashMap
                    .get(Consts.MORE_DEMOURL);
            urls = urls + ConfigUtil.getDemoParamsStr(mActivity);
        } else {
            isshow = true;
        }

        // url = "http://app.ys7.com/";
        if (urls.contains("rotate=x")) {
            urls = urls.replace("rotate=x", "");
            mActivity
                    .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        }
        MyLog.e("yanshidian", urls);
        /** topBar **/
        topBar = (RelativeLayout) rootView.findViewById(R.id.topbarh);
        leftBtn = (Button) rootView.findViewById(R.id.btn_left);
        leftBtn.setVisibility(View.GONE);
        alarmnet = (RelativeLayout) rootView.findViewById(R.id.alarmnet);
        accountError = (TextView) rootView.findViewById(R.id.accounterror);
        currentMenu = (TextView) rootView.findViewById(R.id.currentmenu);
        currentMenu.setText(R.string.demo);
        loadingBar = (ImageView) rootView.findViewById(R.id.loadingbars);
        loadinglayout = (LinearLayout) rootView
                .findViewById(R.id.loadinglayout);

        loadFailedLayout = (RelativeLayout) rootView
                .findViewById(R.id.loadfailedlayout);
        loadFailedLayout.setVisibility(View.GONE);
        reloadImgView = (ImageView) rootView.findViewById(R.id.refreshimg);
        reloadImgView.setOnClickListener(myOnClickListener);
        // currentMenu.setText(R.string.demo);
        //
        // if (-1 == titleID) {
        // currentMenu.setText("");
        // } else if (-2 == titleID) {
        //
        // } else {
        // currentMenu.setText(titleID);
        // }

        // urls = "http://172.16.25.228:8080/";

        leftBtn.setOnClickListener(myOnClickListener);
        rightBtn = (Button) rootView.findViewById(R.id.btn_right);
        rightBtn.setVisibility(View.GONE);
        webView = (WebView) rootView.findViewById(R.id.findpasswebview);

        WebChromeClient wvcc = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                // if (-2 == titleID) {
                currentMenu.setText(ConfigUtil.getShortTile(title));
                titleStack.push(title);
                // }
            }

        };
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.addJavascriptInterface(this, "wst");

        // 设置setWebChromeClient对象
        webView.setWebChromeClient(wvcc);
        webView.requestFocus(View.FOCUS_DOWN);

        // setting.setPluginState(PluginState.ON);
        // 加快加载速度
        webView.getSettings().setRenderPriority(RenderPriority.HIGH);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {
                super.onReceivedError(view, errorCode, description, failingUrl);
                MyLog.v(TAG, "webView load failed");
                loadFailed = true;
                loadFailedLayout.setVisibility(View.VISIBLE);
                webView.setVisibility(View.GONE);
                loadinglayout.setVisibility(View.GONE);
                mActivity.statusHashMap.put(Consts.HAS_LOAD_DEMO, "false");
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String newUrl) {
                MyLog.v("new_url", newUrl);
                // showTextToast(rtmp);//////////////等着去掉
                try {
                    HashMap<String, String> resMap = new HashMap<String, String>();
                    if (null != newUrl && newUrl.contains("?")) {
                        String param_array[] = newUrl.split("\\?");
                        resMap = ConfigUtil.genMsgMapFromhpget(param_array[1]);
                    }

                    if (resMap.containsKey("open")) {// 打开新的WebView模式
                        Intent intentAD2 = new Intent(mActivity,
                                JVWebViewActivity.class);

                        intentAD2.putExtra("URL", newUrl);
                        intentAD2.putExtra("title", -2);
                        mActivity.startActivity(intentAD2);
                    }
                    // else if (newUrl.contains("close")) {// 关闭当前webview
                    // mActivity.this.finish();
                    // }
                    else if (resMap.containsKey("video")
                            || resMap.containsKey("viewmode")) {// 是否含有视频

                        MyLog.e("plazza--urlbefore", newUrl);
                        String rtmp_url = resMap.get("streamsvr");
                        String rtmp_port = resMap.get("rtmport");
                        String hls_port = resMap.get("hlsport");
                        String cloud_num = resMap.get("cloudnum");
                        String channel = resMap.get("channel");
                        String vMode = resMap.get("vmode");// vod（点播），live（直播）
                        if ("live".equalsIgnoreCase(vMode)) {// live（直播）
                            HashMap<String, String> paramMap = new HashMap<String, String>();
                            paramMap.put("new_url", newUrl);
                            paramMap.put("rtmp_url", rtmp_url);
                            paramMap.put("rtmp_port", rtmp_port);
                            paramMap.put("hls_port", hls_port);
                            paramMap.put("cloud_num", cloud_num);
                            paramMap.put("channel", channel);
                            String getPlayUtlRequest = Url.JOVISION_PUBLIC_API
                                    + "server=" + rtmp_url + "&dguid="
                                    + cloud_num + "&channel=" + channel
                                    + "&hlsport=" + hls_port + "&rtmpport="
                                    + rtmp_port;
                            mActivity.createDialog("", false);
                            new GetPlayUrlThread(paramMap, getPlayUtlRequest)
                                    .start();
                        } else if ("vod".equalsIgnoreCase(vMode)) {// vod（点播）
                            String httport = resMap.get("httport");//
                            String vfname = resMap.get("vfname");//

                            String playAddress = "http://" + rtmp_url + ":"
                                    + httport + "/" + vMode + "/" + vfname;
                            // http://119.188.172.4:8085/vod/trailer.mp4
                            MyLog.v("vod-url", playAddress);
                            Intent intentAD = new Intent(mActivity,
                                    JVWebView2Activity.class);
                            intentAD.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intentAD.putExtra("URL", newUrl);
                            intentAD.putExtra("title", -2);
                            intentAD.putExtra("vmode", vMode);
                            intentAD.putExtra("rtmp", playAddress);
                            intentAD.putExtra("cloudnum", "A5678");
                            intentAD.putExtra("channel", "1");
                            intentAD.putExtra("isvod", true);
                            mActivity.startActivity(intentAD);

                        }
                    } else {
                        // String plazzaUrl = ((BaseActivity)
                        // mActivity).statusHashMap
                        // .get(Consts.MORE_DEMOURL);
                        // if (newUrl.contains(plazzaUrl)) {
                        // newUrl = newUrl + "?" + "plat=android&platv="
                        // + Build.VERSION.SDK_INT + "&lang=" + lan
                        // + "&d=" + System.currentTimeMillis()
                        // + "&sid=" + sid;
                        // }

                        view.loadUrl(newUrl);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                if (!MySharedPreference.getBoolean("webfirst")) {
                    MySharedPreference.putBoolean("webfirst", true);
                    loadinglayout.setVisibility(View.VISIBLE);
                    loadingBar.setAnimation(AnimationUtils.loadAnimation(
                            mActivity, R.anim.rotate));
                }
                MyLog.v(TAG, "webView start load");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // webView.loadUrl("javascript:videopayer.play()");
                webView.loadUrl("javascript:upload_url()");// 调用js 获取图片上传地址
                if (loadFailed) {
                    mActivity.statusHashMap.put(Consts.HAS_LOAD_DEMO, "false");
                    loadFailedLayout.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    loadinglayout.setVisibility(View.GONE);
                } else {
                    if (isConnected) {
                        mActivity.statusHashMap.put(Consts.HAS_LOAD_DEMO,
                                "true");
                        webView.loadUrl("javascript:(function() { var videos = document.getElementsByTagName('video'); for(var i=0;i<videos.length;i++){videos[i].play();}})()");
                        loadinglayout.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);
                        loadFailedLayout.setVisibility(View.GONE);
                    } else {
                        mActivity.statusHashMap.put(Consts.HAS_LOAD_DEMO,
                                "false");
                        loadFailedLayout.setVisibility(View.VISIBLE);
                        webView.setVisibility(View.GONE);
                        loadinglayout.setVisibility(View.GONE);
                    }
                }
                if (isshow) {
                    mActivity.statusHashMap.put(Consts.HAS_LOAD_DEMO, "false");
                    loadFailedLayout.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    loadinglayout.setVisibility(View.GONE);
                }
                MyLog.v(TAG, "webView finish load");
            }
        });

        boolean hasLoad = false;
        if (null != mActivity.statusHashMap.get(Consts.HAS_LOAD_DEMO)) {
            hasLoad = Boolean.parseBoolean(mActivity.statusHashMap
                    .get(Consts.HAS_LOAD_DEMO));
        }

        if (null != titleStack) {
            if (titleStack.size() <= 1) {
                titleStack.clear();
            }
        }

        // urls = "http://test.cloudsee.net/mobile/?plat=android&appv=V5.5.0";

        if (hasLoad && ConfigUtil.isConnected(mActivity)) {
            if (null == titleStack || 0 == titleStack.size()) {
                webView.getSettings().setCacheMode(
                        WebSettings.LOAD_CACHE_ELSE_NETWORK);// LOAD_CACHE_ELSE_NETWORK
            } else {

                loadinglayout.setVisibility(View.VISIBLE);
                webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
                loadFailed = false;
                webView.loadUrl(urls);

                if (null != titleStack) {
                    titleStack.clear();
                }
            }
        } else {
            loadinglayout.setVisibility(View.VISIBLE);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
            loadFailed = false;
            webView.loadUrl(urls);
            if (null != titleStack) {
                titleStack.clear();
            }
        }

    }

    /**
     * 获取播放地址
     * 
     * @author Administrator
     */
    class GetPlayUrlThread extends Thread {
        String requestUrl;
        HashMap<String, String> paramMap;

        public GetPlayUrlThread(HashMap<String, String> map, String url) {
            requestUrl = url;
            paramMap = map;
        }

        @Override
        public void run() {
            super.run();
            MyLog.v("post_request", requestUrl);
            int rt = 0;
            String rtmpUrl = "";
            // {"rt": 0, "rtmpurl":
            // "rtmp://119.188.172.3:1935/live/a579223323_1", "hlsurl":
            // "http://119.188.172.3:8080/live/a579223323_1.m3u8", "mid": 33512}
            String result = JSONUtil.getRequest(requestUrl);
            try {
                if (null != result && !"".equalsIgnoreCase(result)) {
                    JSONObject obj = new JSONObject(result);
                    if (null != obj) {
                        rt = obj.getInt("rt");
                        rtmpUrl = obj.getString("rtmpurl");
                    }
                }

                if (0 == rt) {
                    paramMap.put("rtmpurl", rtmpUrl);
                    fragHandler.sendMessage(fragHandler.obtainMessage(
                            Consts.WHAT_DEMO_URL_SUCCESS, 0, 0, paramMap));
                } else {
                    fragHandler.sendMessage(fragHandler.obtainMessage(
                            Consts.WHAT_DEMO_URL_FAILED, 0, 0, null));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            MyLog.v("post_result", result);
        }
    }

    OnClickListener myOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.dialog_cancle_img:
                    initDialog.dismiss();
                    break;
                case R.id.btn_left: {
                    backMethod();
                    break;
                }
                case R.id.refreshimg: {
                    if (ConfigUtil.isConnected(mActivity)) {
                        loadFailedLayout.setVisibility(View.GONE);
                        mActivity.statusHashMap.put(Consts.HAS_LOAD_DEMO, "false");
                        loadinglayout.setVisibility(View.VISIBLE);
                        loadingBar.setAnimation(AnimationUtils.loadAnimation(
                                mActivity, R.anim.rotate));
                        loadFailed = false;
                        if (null == urls || "".equalsIgnoreCase(urls)) {
                            if ("false".equals(mActivity.statusHashMap
                                    .get(Consts.KEY_INIT_ACCOUNT_SDK))) {
                                MyLog.e("Login", "初始化账号SDK失败");
                                ConfigUtil
                                        .initAccountSDK(((MainApplication) mActivity
                                                .getApplication()));// 初始化账号SDK
                            }

                            GetDemoTask task = new GetDemoTask(mActivity);
                            String[] params = new String[3];
                            params[1] = "5";//
                            task.execute(params);
                            MyLog.v(TAG, "urls is null");
                        }
                        webView.loadUrl(urls);
                    } else {
                        mActivity.alertNetDialog();
                    }
                    break;
                }
            }

        }
    };

    /**
     * 返回事件
     */
    private void backMethod() {
        if (webView.canGoBack()) {

            if (null != titleStack) {
                titleStack.pop();
                String lastTitle = titleStack.peek();
                currentMenu.setText(ConfigUtil.getShortTile(lastTitle));
            }

            webView.goBack(); // goBack()表示返回WebView的上一页面
        } else {
            mActivity.openExitDialog();
        }
    }

    // @Override
    // // 设置回退
    // // 覆盖Activity类的onKeyDown(int keyCoder,KeyEvent event)方法
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    // if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
    // webView.goBack(); // goBack()表示返回WebView的上一页面
    // return true;
    // }
    // return super.onKeyDown(keyCode, event);
    // }
    @Override
    public void onPause() {
        super.onPause();
        webView.onPause();

        // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
        // webView.onPause(); // 暂停网页中正在播放的视频
        // }
    }

    @Override
    public void onDestroy() {
        if (null != titleStack) {
            if (titleStack.size() > 1) {
                webView.loadDataWithBaseURL(null, "", "text/html", "utf-8",
                        null);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        webView.onResume();
        if (!ConfigUtil.isConnected(mActivity)) {
            isConnected = false;
            loadFailedLayout.setVisibility(View.VISIBLE);
            mActivity.statusHashMap.put(Consts.HAS_LOAD_DEMO, "false");
            webView.setVisibility(View.GONE);
            loadinglayout.setVisibility(View.GONE);
        } else {
            isConnected = true;
        }
    }

    @Override
    public void onMainAction(int packet_type) {

    }

    /**
     * upload_url js的返回值
     * 
     * @param upUrl 即js的返回值
     */
    public void getUploadUrl(String upUrl) {
        uploadUrl = upUrl;
    }

    // /**
    // * js window.wst.cutpic()
    // */
    // public void cutpic() {
    // new AlertDialog.Builder(mActivity)
    // .setTitle(getResources().getString(R.string.str_delete_tip))
    // .setItems(
    // new String[] {
    // getResources().getString(
    // R.string.capture_to_upload),
    // getResources().getString(
    // R.string.select_to_upload),
    // getResources().getString(R.string.cancel) },
    // new OnMyOnClickListener()).show();

    // }

    /**
     * 2015-03-31 修改上传照片dialog
     */
    public void cutpic() {
        initDialog = new Dialog(mActivity, R.style.mydialog);
        view = LayoutInflater.from(mActivity).inflate(R.layout.dialog_capture,
                null);
        initDialog.setContentView(view);

        captureparent = (RelativeLayout) view.findViewById(R.id.captureparent);
        capture_Load = (RelativeLayout) view.findViewById(R.id.capture_upload);
        select_Load = (RelativeLayout) view.findViewById(R.id.select_upload);
        dialog_cancle_img = (ImageView) view
                .findViewById(R.id.dialog_cancle_img);
        capturetext = (TextView) view.findViewById(R.id.capturetext);
        selecttext = (TextView) view.findViewById(R.id.selecttext);

        capture_Load.setOnTouchListener(myOnTouchListetner);
        select_Load.setOnTouchListener(myOnTouchListetner);
        dialog_cancle_img.setOnClickListener(myOnClickListener);
        initDialog.show();
    }

    OnTouchListener myOnTouchListetner = new OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // TODO Auto-generated method stub
            switch (v.getId()) {
                case R.id.capture_upload:
                    /** 从摄像头获取 */
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        capture_Load.setBackgroundColor(getResources().getColor(
                                R.color.welcome_blue));
                        capturetext.setTextColor(getResources().getColor(
                                R.color.white));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        try {
                            capture_Load.setBackground(getResources().getDrawable(
                                    R.drawable.dialog_wavebg_color));
                            capturetext.setTextColor(getResources().getColor(
                                    R.color.more_fragment_color2));
                            initDialog.dismiss();
                            MobileUtil
                                    .createDirectory(new File(Consts.BBSIMG_PATH));
                            imageTempUri = Uri.fromFile(new File(
                                    Consts.BBSIMG_PATH, System.currentTimeMillis()
                                            + Consts.IMAGE_JPG_KIND));

                            mCurrentPhotoFile = new File(Consts.BBSIMG_PATH,
                                    System.currentTimeMillis()
                                            + Consts.IMAGE_JPG_KIND);
                            Intent it_camera = new Intent(
                                    MediaStore.ACTION_IMAGE_CAPTURE);
                            it_camera.putExtra(MediaStore.EXTRA_OUTPUT,
                                    Uri.fromFile(mCurrentPhotoFile));
                            view.setVisibility(View.GONE);
                            initDialog.dismiss();
                            startActivityForResult(it_camera,
                                    REQUEST_CODE_IMAGE_CAPTURE);
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    break;
                case R.id.select_upload:
                    /** 从相册获取 */
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        select_Load.setBackgroundColor(getResources().getColor(
                                R.color.welcome_blue));
                        selecttext.setTextColor(getResources().getColor(
                                R.color.white));
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        try {
                            select_Load.setBackgroundColor(getResources().getColor(
                                    R.color.white));
                            selecttext.setTextColor(getResources().getColor(
                                    R.color.more_fragment_color2));
                            initDialog.dismiss();
                            MobileUtil
                                    .createDirectory(new File(Consts.BBSIMG_PATH));
                            imageTempUri = Uri.fromFile(new File(
                                    Consts.BBSIMG_PATH, System.currentTimeMillis()
                                            + Consts.IMAGE_JPG_KIND));
                            // 从相册取相片
                            Intent it_photo = new Intent(Intent.ACTION_GET_CONTENT);
                            it_photo.addCategory(Intent.CATEGORY_OPENABLE);
                            // 设置数据类型
                            it_photo.setType("image/*");
                            // 设置返回方式
                            // intent.putExtra("return-data", true);
                            it_photo.putExtra(MediaStore.EXTRA_OUTPUT, imageTempUri);
                            // 设置截图
                            // it_photo.putExtra("crop", "true");
                            // it_photo.putExtra("scale", true);
                            // 跳转至系统功能
                            view.setVisibility(View.GONE);
                            initDialog.dismiss();
                            startActivityForResult(it_photo,
                                    REQUEST_CODE_IMAGE_SELECTE);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;

                default:
                    break;
            }
            return true;
        }
    };

    // /** 图片来源菜单响应类 */
    // protected class OnMyOnClickListener implements
    // DialogInterface.OnClickListener {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // /** 从摄像头获取 */
    // if (which == 0) {
    // try {
    //
    // MobileUtil.createDirectory(new File(Consts.BBSIMG_PATH));
    // imageTempUri = Uri
    // .fromFile(new File(Consts.BBSIMG_PATH, System
    // .currentTimeMillis()
    // + Consts.IMAGE_JPG_KIND));
    //
    // mCurrentPhotoFile = new File(Consts.BBSIMG_PATH,
    // System.currentTimeMillis() + Consts.IMAGE_JPG_KIND);
    // Intent it_camera = new Intent(
    // MediaStore.ACTION_IMAGE_CAPTURE);
    // it_camera.putExtra(MediaStore.EXTRA_OUTPUT,
    // Uri.fromFile(mCurrentPhotoFile));
    // startActivityForResult(it_camera,
    // REQUEST_CODE_IMAGE_CAPTURE);
    // } catch (Exception e) {
    // System.out.println(e.getMessage());
    // }
    // } else if (which == 1) {
    // /** 从相册获取 */
    // try {
    //
    // // MobileUtil.createDirectory(new File(Consts.BBSIMG_PATH));
    // // imageTempUri = Uri
    // // .fromFile(new File(Consts.BBSIMG_PATH, System
    // // .currentTimeMillis()
    // // + Consts.IMAGE_JPG_KIND));
    // // 从相册取相片
    // Intent it_photo = new Intent(Intent.ACTION_GET_CONTENT);
    // it_photo.addCategory(Intent.CATEGORY_OPENABLE);
    // // 设置数据类型
    // it_photo.setType("image/*");
    // // 设置返回方式
    // // intent.putExtra("return-data", true);
    // it_photo.putExtra(MediaStore.EXTRA_OUTPUT, imageTempUri);
    // // 设置截图
    // // it_photo.putExtra("crop", "true");
    // // it_photo.putExtra("scale", true);
    // // 跳转至系统功能
    // startActivityForResult(it_photo, REQUEST_CODE_IMAGE_SELECTE);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // } else if (which == 2) {
    // /** 取消 */
    // dialog.dismiss();
    // }
    // }

    // }

    /**
     * 根据uri获取文件
     * 
     * @param uri
     * @return
     */
    public File getFileFromUri(Uri uri) {
        File file = null;

        String[] proj = {
            MediaStore.Images.Media.DATA
        };
        Cursor actualimagecursor = mActivity.managedQuery(uri, proj, null,
                null, null);
        int actual_image_column_index = actualimagecursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor
                .getString(actual_image_column_index);
        file = new File(img_path);
        return file;
    }

    /** 获取调用摄像头以及相册返回数据 */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            // ==========摄像头===========
            if (requestCode == REQUEST_CODE_IMAGE_CAPTURE
                    && resultCode == Activity.RESULT_OK) {
                if (mCurrentPhotoFile != null) {
                    mActivity.createDialog("", false);
                    Thread uploadThread = new Thread() {
                        @Override
                        public void run() {
                            String res = UploadUtil.uploadFile(
                                    mCurrentPhotoFile, uploadUrl);
                            fragHandler.sendMessage(fragHandler.obtainMessage(
                                    Consts.BBS_IMG_UPLOAD_SUCCESS, 0, 0, res));
                            super.run();
                        }
                    };
                    uploadThread.start();
                }
                // ==========相册============
            } else if (requestCode == REQUEST_CODE_IMAGE_SELECTE
                    && resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                mCurrentPhotoFile = getFileFromUri(uri);// 根据uri获取文件
                if (mCurrentPhotoFile != null) {
                    mActivity.createDialog("", false);
                    Thread uploadThread = new Thread() {
                        @Override
                        public void run() {
                            String res = UploadUtil.uploadFile(
                                    mCurrentPhotoFile, uploadUrl);
                            fragHandler.sendMessage(fragHandler.obtainMessage(
                                    Consts.BBS_IMG_UPLOAD_SUCCESS, 0, 0, res));
                            super.run();
                        }
                    };
                    uploadThread.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
