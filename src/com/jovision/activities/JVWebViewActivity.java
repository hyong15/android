
package com.jovision.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import barcode.zxing.decoding.RGBLuminanceSource;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.commons.Url;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.GetQRImageFromBBS;
import com.jovision.utils.ImageUtil;
import com.jovision.utils.JSONUtil;
import com.jovision.utils.MobileUtil;
import com.jovision.utils.UploadUtil;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

@SuppressLint("NewApi")
public class JVWebViewActivity extends BaseActivity {

    private static final String TAG = "JVWebViewActivity";
    private LinearLayout loadinglayout;
    /** topBar **/
    private RelativeLayout topBar;

    private WebView webView;
    private String url = "";
    private int titleID = 0;
    private ImageView loadingBar;
    WebChromeClient wvcc = null;
    String sid = "";
    String lan = "";

    private LinearLayout loadFailedLayout;
    private ImageView reloadImgView;
    private boolean loadFailed = false;
    private boolean isfirst = false;

    Stack<String> titleStack = new Stack<String>();// 标题栈，后进先出

    // protected ValueCallback<Uri> mUploadMessage;
    // protected int FILECHOOSER_RESULTCODE = 1;
    // private Uri imageUri;

    protected static final int REQUEST_CODE_IMAGE_CAPTURE = 0;
    protected static final int REQUEST_CODE_IMAGE_SELECTE = 1;
    protected static final int REQUEST_CODE_IMAGE_CROP = 2;
    // /* 拍照的照片存储位置 */
    // private static final File PHOTO_DIR = new File(
    // Environment.getExternalStorageDirectory() + "/DCIM/Camera");
    // 照相机拍照得到的图片
    private File mCurrentPhotoFile;
    // 缓存图片URI
    Uri imageTempUri = null;
    private String uploadUrl = "";// "http://bbs.cloudsee.net/misc.php?mod=swfupload&operation=upload&type=image&inajax=yes&infloat=yes&simple=2&uid=1";

    private Dialog initDialog;
    private RelativeLayout capture_Load;
    private RelativeLayout select_Load;
    private RelativeLayout captureparent;
    private ImageView dialog_cancle_img;
    private TextView capturetext;
    private TextView selecttext;
    private View view;

    // ----------视频分享到广场参数-------
    private String mark;// 标志
    private byte[] postData;// 请求url时,post提交的内容

    private String cloudseeNo;// 云视通号
    private Bitmap captureBitmap;// 抓拍图片
    private String captureEncrypt;// 加密后的抓拍图片字符串
    private boolean mIsPost;// 是否是post请求

    // --------长按图片识别二维码--------------------------
    // 网页中图片的链接地址
    private String BBSPicUrl = "";
    // 网页中图片的内容
    private String BBSPicData = "";
    // 接收网页中二维码图片
    private Bitmap BBSQRBitmap;

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.BBS_IMG_UPLOAD_SUCCESS: {
                dismissDialog();
                if (null != obj) {
                    // showTextToast(obj.toString());
                    webView.loadUrl("javascript:uppic(\"" + obj.toString() + "\")");
                } else {
                    // showTextToast("null");
                }

                break;
            }
            case Consts.WHAT_DEMO_URL_SUCCESS: {
                dismissDialog();
                HashMap<String, String> paramMap = (HashMap<String, String>) obj;
                Intent intentAD = new Intent(JVWebViewActivity.this,
                        JVWebView2Activity.class);
                intentAD.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intentAD.putExtra("URL", paramMap.get("new_url"));
                intentAD.putExtra("title", -2);
                intentAD.putExtra("rtmp", paramMap.get("rtmpurl"));
                intentAD.putExtra("cloudnum", paramMap.get("cloud_num"));
                intentAD.putExtra("channel", paramMap.get("channel"));
                intentAD.putExtra("vmode", "live");
                JVWebViewActivity.this.startActivity(intentAD);
                break;
            }
            case Consts.WHAT_DEMO_URL_FAILED: {
                // TODO
                dismissDialog();
                showTextToast(R.string.str_video_load_failed);
                break;
            }
            // case Consts.READ_QR_CONTENT: {
            // // TODO
            // MyLog.e(TAG, "图片内容：" + qr_content);
            // break;
            // }
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {
        url = getIntent().getStringExtra("URL");
        // url = "http://test.cloudsee.net/mobile/?plat=android&appv=V5.5.0";
        // url =
        // "http://bbst.cloudsee.net/forum.php?mod=forumdisplay&fid=36&mobile=2";
        // url = "http://test.cloudsee.net/phone.action";
        // url = "http://app.ys7.com/";
        titleID = getIntent().getIntExtra("title", 0);
        mark = getIntent().getStringExtra("mark");
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initUi() {
        setContentView(R.layout.findpass_layout);

        // if (url.contains("rotate=x")) {
        // url = url.replace("rotate=x", "");
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//
        // 横屏
        // }

        if (Consts.LANGUAGE_ZH == ConfigUtil.getLanguage2(this)) {
            lan = "zh_cn";
        } else if (Consts.LANGUAGE_ZHTW == ConfigUtil.getLanguage2(this)) {
            lan = "zh_tw";
        } else {
            lan = "en_us";
        }

        if (!Boolean.valueOf(statusHashMap.get(Consts.LOCAL_LOGIN))) {
            String sessionResult = ConfigUtil.getSession();
            sid = sessionResult;
        } else {
            sid = "";
        }

        MyLog.v(TAG, "webview-URL=" + url);
        /** topBar **/
        topBar = (RelativeLayout) findViewById(R.id.topbarh);
        leftBtn = (Button) findViewById(R.id.btn_left);
        leftBtn.setBackgroundDrawable(getResources().getDrawable(
                R.drawable.close_web));
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        currentMenu.setText(R.string.loading);
        loadingBar = (ImageView) findViewById(R.id.loadingbars);
        loadinglayout = (LinearLayout) findViewById(R.id.loadinglayout);

        loadFailedLayout = (LinearLayout) findViewById(R.id.loadfailedlayout);
        loadFailedLayout.setVisibility(View.GONE);
        reloadImgView = (ImageView) findViewById(R.id.refreshimg);
        reloadImgView.setOnClickListener(myOnClickListener);

        if (-1 == titleID) {
            currentMenu.setText("");
        } else if (-2 == titleID) {

        } else {
            currentMenu.setText(titleID);
        }

        leftBtn.setOnClickListener(myOnClickListener);
        rightBtn = (Button) findViewById(R.id.btn_right);
        rightBtn.setVisibility(View.GONE);
        rightBtn.setOnClickListener(myOnClickListener);

        webView = (WebView) findViewById(R.id.findpasswebview);

        // url = "http://172.16.25.228:8080/";
        wvcc = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (-2 == titleID) {
                    currentMenu.setText(ConfigUtil.getShortTile(title));
                    titleStack.push(title);
                }
            }

            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
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
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // webView.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {
                MyLog.v(TAG, "webView load failed");
                loadFailed = true;
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String newUrl) {
                MyLog.v("new_url", newUrl);
                try {
                    HashMap<String, String> resMap = new HashMap<String, String>();
                    if (null != newUrl && newUrl.contains("?")) {
                        String param_array[] = newUrl.split("\\?");
                        resMap = ConfigUtil.genMsgMapFromhpget(param_array[1]);
                    }

                    if (resMap.containsKey("open")) {// 打开新的WebView模式
                        Intent intentAD2 = new Intent(JVWebViewActivity.this,
                                JVWebViewActivity.class);
                        intentAD2.putExtra("URL", newUrl);
                        intentAD2.putExtra("title", -2);
                        JVWebViewActivity.this.startActivity(intentAD2);
                    } else if (resMap.containsKey("close")) {// 关闭当前webview
                        JVWebViewActivity.this.finish();
                    } else if (resMap.containsKey("video")
                            || resMap.containsKey("viewmode")) {// 是否含有视频
                        MyLog.e("plazza--urlbefore", newUrl);
                        // http://www.cloudsee.net/mobile/playPhoneVideo.action?
                        // plat=android&platv=19&lang=zh_cn&
                        // sid=8d5c76c66c2b7f259bc11784ae5900cb&
                        // appv=V5.5.0&video=hidden&viewmode=&
                        // cloudnum=b82952331&channel=1&rtmport=1935&
                        // hlsport=8080&streamsvr=119.188.172.3&
                        // allowshare=&vid=1071&d=1430796831933

                        // String param_array[] = newUrl.split("\\?");
                        // HashMap<String, String> resMap;
                        // resMap =
                        // ConfigUtil.genMsgMapFromhpget(param_array[1]);

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
                            createDialog("", false);
                            new GetPlayUrlThread(paramMap, getPlayUtlRequest)
                                    .start();
                        } else if ("vod".equalsIgnoreCase(vMode)) {// vod（点播）
                            String httport = resMap.get("httport");//
                            String vfname = resMap.get("vfname");//

                            String playAddress = "http://" + rtmp_url + ":"
                                    + httport + "/" + vMode + "/" + vfname;
                            // http://119.188.172.4:8085/vod/trailer.mp4
                            MyLog.v("vod-url", playAddress);
                            Intent intentAD = new Intent(
                                    JVWebViewActivity.this,
                                    JVWebView2Activity.class);
                            intentAD.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            intentAD.putExtra("URL", newUrl);
                            intentAD.putExtra("title", -2);
                            intentAD.putExtra("vmode", vMode);
                            intentAD.putExtra("rtmp", playAddress);
                            intentAD.putExtra("cloudnum", "A5678");
                            intentAD.putExtra("channel", "1");
                            intentAD.putExtra("isvod", true);
                            JVWebViewActivity.this.startActivity(intentAD);

                        }
                        // http://test.cloudsee.net/mobile/playPhoneVideo.action?
                        // plat=android
                        // &platv=
                        // &lang=zh_cn
                        // &sid=
                        // &appv=1
                        // &video=hidden
                        // &viewmode=
                        // &streamsvr=119.188.172.4
                        // &allowshare=
                        // &vid=1075
                        // &vmode=vod
                        // &httport=8085
                        // &vfname=trailer.mp4
                        // &d=1430883590640

                    } else {
                        // String plazzaUrl = statusHashMap
                        // .get(Consts.MORE_DEMOURL);
                        // if (newUrl.contains(plazzaUrl)) {
                        // newUrl = newUrl + "?" + "plat=android&platv="
                        // + Build.VERSION.SDK_INT + "&lang=" + lan
                        // + "&d=" + System.currentTimeMillis()
                        // + "&sid=" + sid;
                        // }
                        loadinglayout.setVisibility(View.VISIBLE);
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
                if (!isfirst) {
                    loadinglayout.setVisibility(View.VISIBLE);
                    loadingBar.setAnimation(AnimationUtils.loadAnimation(
                            JVWebViewActivity.this, R.anim.rotate));
                    isfirst = true;
                }
                MyLog.v(TAG, "webView start load");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // webView.loadUrl("javascript:videopayer.play()");
                webView.loadUrl("javascript:upload_url()");// 调用js 获取图片上传地址
                if (loadFailed) {
                    loadFailedLayout.setVisibility(View.VISIBLE);
                    webView.setVisibility(View.GONE);
                    loadinglayout.setVisibility(View.GONE);
                } else {
                    webView.loadUrl("javascript:function uploadPicFromMobile(str){fileupload.addFileList(str);}");
                    // webView.loadUrl("javascript:(function() { var videos = document.getElementsByTagName('video'); for(var i=0;i<videos.length;i++){videos[i].play();}})()");
                    loadinglayout.setVisibility(View.GONE);
                    webView.setVisibility(View.VISIBLE);
                    loadFailedLayout.setVisibility(View.GONE);
                }
                MyLog.v(TAG, "webView finish load");
            }
        });
        // 添加长按监听获取页面元素 nihy 2015年5月28日09:01:38
        webView.setOnLongClickListener(new OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                // TODO 自动生成的方法存根

                HitTestResult result = ((WebView) v).getHitTestResult();
                if (null == result)
                    return false;

                int type = result.getType();
                if (type == WebView.HitTestResult.UNKNOWN_TYPE)
                    return false;

                if (type == WebView.HitTestResult.EDIT_TEXT_TYPE) {
                    // let TextView handles context menu
                    MyLog.i(TAG, "可编辑");
                    return true;
                }
                MyLog.i(TAG, "type==" + type);
                switch (type) {
                    case WebView.HitTestResult.PHONE_TYPE:
                        // 处理拨号
                        MyLog.i(TAG, "拨号");
                        break;
                    case WebView.HitTestResult.EMAIL_TYPE:
                        // 处理Email
                        MyLog.i(TAG, "邮件");
                        break;
                    case WebView.HitTestResult.GEO_TYPE:

                        MyLog.i(TAG, "地图");
                        break;
                    case WebView.HitTestResult.SRC_ANCHOR_TYPE:
                        // 超链接
                        MyLog.i(TAG, "超链接");
                        break;
                    case WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE:
                        MyLog.i(TAG, "图片超链接");
                    case WebView.HitTestResult.ANCHOR_TYPE:
                        MyLog.i(TAG, "超链接");
                        break;
                    case WebView.HitTestResult.IMAGE_ANCHOR_TYPE:
                        MyLog.i(TAG, "图片链接");
                        break;
                    case WebView.HitTestResult.IMAGE_TYPE:
                        // TODO
                        // 处理长按图片的菜单项
                        MyLog.i(TAG, "图片");
                        BBSPicUrl = result.getExtra();
                        Timer timer = new Timer();
                        timer.schedule(new MyTask(), 50);

                        break;
                    default:
                        break;
                }
                return true;

            }
        });
        loadFailed = false;

        // 请求参数配置
        initParamsConfig();
        if (mIsPost) {
            // 请求地址,并且用post传递参数(无参的情况postData为null)
            webView.postUrl(url, postData);
        } else {
            webView.loadUrl(url);
        }
    }

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
                    MyLog.v("plazza--url", rtmpUrl);
                    handler.sendMessage(handler.obtainMessage(
                            Consts.WHAT_DEMO_URL_SUCCESS, 0, 0, paramMap));
                } else {
                    handler.sendMessage(handler.obtainMessage(
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
                case R.id.btn_right: {// 关闭当前网页
                    backWebview();
                    break;
                }
                case R.id.refreshimg: {
                    loadFailedLayout.setVisibility(View.GONE);
                    loadinglayout.setVisibility(View.VISIBLE);
                    loadingBar.setAnimation(AnimationUtils.loadAnimation(
                            JVWebViewActivity.this, R.anim.rotate));
                    loadFailed = false;
                    webView.reload();
                    break;
                }
            }

        }
    };

    /**
     * 返回事件
     */
    private void backMethod() {
        MyLog.v("webView.canGoBack()", "" + webView.canGoBack());
        try {
            JVWebViewActivity.this.finish();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // webview返回
    public void backWebview() {
        MyLog.v("webView.canGoBack()", "" + webView.canGoBack());
        try {
            if (webView.canGoBack()) {
                if (null != titleStack && 0 != titleStack.size()) {
                    titleStack.pop();
                    String lastTitle = titleStack.peek();
                    currentMenu.setText(ConfigUtil.getShortTile(lastTitle));
                    webView.goBack(); // goBack()表示返回WebView的上一页面
                }
            } else {
                JVWebViewActivity.this.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        // backMethod();
        backWebview();
    }

    @Override
    protected void saveSettings() {

    }

    @Override
    protected void freeMe() {
    }

    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
    }

    /**
     * upload_url js的返回值
     * 
     * @param upUrl 即js的返回值
     */
    public void getUploadUrl(String upUrl) {
        uploadUrl = upUrl;
    }

    /**
     * js window.wst.cutpic()
     */
    // public void cutpic() {
    // new AlertDialog.Builder(JVWebViewActivity.this)
    // .setTitle(getResources().getString(R.string.str_delete_tip))
    // .setItems(
    // new String[] {
    // getResources().getString(
    // R.string.capture_to_upload),
    // getResources().getString(
    // R.string.select_to_upload),
    // getResources().getString(R.string.cancel) },
    // new OnMyOnClickListener()).show();
    //
    // }

    /**
     * 2015-03-31 修改上传照片dialog
     */
    public void cutpic() {
        initDialog = new Dialog(JVWebViewActivity.this, R.style.mydialog);
        view = LayoutInflater.from(JVWebViewActivity.this).inflate(
                R.layout.dialog_capture, null);
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

    // /** 图片来源菜单响应类 */
    // protected class OnMyOnClickListener implements
    // DialogInterface.OnClickListener {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // if (which == 0) {
    //
    // } else if (which == 1) {
    //
    // } else if (which == 2) {
    // /** 取消 */
    // dialog.dismiss();
    // }
    // }
    //
    // }

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
                            MobileUtil
                                    .createDirectory(new File(Consts.BBSIMG_PATH));
                            imageTempUri = Uri.fromFile(new File(
                                    Consts.BBSIMG_PATH, System.currentTimeMillis()
                                            + Consts.IMAGE_JPG_KIND));
                            // // 从相册取相片
                            // Intent it_photo = new Intent(
                            // Intent.ACTION_PICK,
                            // android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//new
                            // Intent(Intent.ACTION_GET_CONTENT);
                            // it_photo.addCategory(Intent.CATEGORY_OPENABLE);
                            // // 设置数据类型
                            // it_photo.setType("image/*");
                            // // 设置返回方式
                            // // intent.putExtra("return-data", true);
                            // it_photo.putExtra(MediaStore.EXTRA_OUTPUT,
                            // imageTempUri);
                            //
                            // // 设置截图
                            // // it_photo.putExtra("crop", "true");
                            // // it_photo.putExtra("scale", true);
                            // // 跳转至系统功能

                            // startActivityForResult(it_photo,
                            // REQUEST_CODE_IMAGE_SELECTE);

                            view.setVisibility(View.GONE);
                            initDialog.dismiss();
                            Intent intent;
                            // 此方法兼容5.0以上系统
                            intent = new Intent(
                                    Intent.ACTION_PICK,
                                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            startActivityForResult(intent,
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

    /** 获取调用摄像头以及相册返回数据 */
    @SuppressLint("NewApi")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            // ==========摄像头===========
            if (requestCode == REQUEST_CODE_IMAGE_CAPTURE
                    && resultCode == Activity.RESULT_OK) {
                // mCurrentPhotoFile =
                // ImageUtil.getImageFile(mCurrentPhotoFile.getAbsolutePath());
                if (mCurrentPhotoFile != null) {

                    createDialog("", false);
                    Thread uploadThread = new Thread() {

                        @Override
                        public void run() {
                            String res = UploadUtil.uploadFile(
                                    mCurrentPhotoFile, uploadUrl);
                            handler.sendMessage(handler.obtainMessage(
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
                // mCurrentPhotoFile =
                // ImageUtil.getImageFile(mCurrentPhotoFile.getAbsolutePath());
                if (mCurrentPhotoFile != null) {
                    createDialog("", false);
                    Thread uploadThread = new Thread() {
                        @Override
                        public void run() {
                            String res = UploadUtil.uploadFile(
                                    mCurrentPhotoFile, uploadUrl);
                            handler.sendMessage(handler.obtainMessage(
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
        Cursor actualimagecursor = managedQuery(uri, proj, null, null, null);
        int actual_image_column_index = actualimagecursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        actualimagecursor.moveToFirst();
        String img_path = actualimagecursor
                .getString(actual_image_column_index);
        MyLog.v("FileFromUri", "uri=" + uri + "----img_path=" + img_path);
        file = new File(img_path);
        return file;
    }

    /**
     * 初始化post请求的参数
     */
    private void initParamsConfig() {
        if (mark == null || mark.equals("")) {
            return;
        }

        mIsPost = true;
        // 视频分享到广场
        if (mark.equals("videoshare")) {

            // 获取intent中的值
            cloudseeNo = getIntent().getStringExtra("cloudno");
            // 用Base64把图片进行加密
            String capturepath = getIntent().getStringExtra("capturepath");
            captureBitmap = BitmapFactory.decodeFile(capturepath);
            captureEncrypt = ImageUtil.Bitmap2StrByBase64(captureBitmap);
            // 加密完图片后,删除原有图片
            final File captureFile = new File(capturepath);
            if (null != captureFile) {
                boolean isDelete = captureFile.delete();
                MyLog.v(TAG, "--capture file:" + capturepath + " is deleted:"
                        + isDelete);
            }

            // post方式传送参数
            String params = "";
            // 转换图片(防止进行post请求的时候转义图片内容)
            String textpic = "";
            try {
                textpic = URLEncoder.encode(captureEncrypt, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                MyLog.v(TAG, "--exception--");
            }
            // 账号名称
            String username = MySharedPreference.getString("UserName");

            // 语言
            String lang = "zh_cn";// 默认中文
            switch (ConfigUtil.getLanguage2(this)) {
                case 1:// 中文
                    lang = "zh_cn";
                    break;
                case 2:// 英文
                    lang = "en_us";
                    break;
                case 3:// 繁体
                    lang = "zh_tw";
                    break;
            }

            params = "sid=" + sid + "&username=" + username + "&lang=" + lang
                    + "&txtCloudNum=" + cloudseeNo + "&txtpic=" + textpic;
            postData = params.getBytes();
        }
        // 其它的post请求
    }

    // 获取二维码的图片内容
    private class MyTask extends TimerTask {
        @Override
        public void run() {
            getImage(BBSPicUrl);

        }
    }

    public void getImage(String imgUrl) {
        byte[] data;
        try {
            data = GetQRImageFromBBS.getImage(imgUrl);
            BBSQRBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (BBSQRBitmap != null) {
                scanningImage(BBSQRBitmap);
                if (!"".equals(BBSPicData) && null != BBSPicData) {
                    JVReadBBSQRPopViewActivity.qrContent = BBSPicData;// 把路径复制给ReadBBSQRPopView的参数
                    Intent intent = new Intent();
                    intent.setClass(JVWebViewActivity.this,
                            JVReadBBSQRPopViewActivity.class);
                    startActivity(intent);
                    BBSPicData = "";// 传递完值，BBSPicContent置为空，准备第二次使用
                }
            }
        } catch (IOException e) {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }

    // 解析获取到的二维码图片
    private String scanningImage(Bitmap bitmap) {
        Hashtable<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
        Collection<BarcodeFormat> decodeFormats = new Vector<BarcodeFormat>();
        decodeFormats.add(BarcodeFormat.QR_CODE);
        hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        RGBLuminanceSource source = new RGBLuminanceSource(bitmap);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader2 = new QRCodeReader();
        Result result;
        try {
            result = reader2.decode(bitmap1, hints);
            MyLog.i(TAG, "res-------" + result.getText());
            BBSPicData = result.getText();

        } catch (NotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ChecksumException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return BBSPicData;
    }
}
