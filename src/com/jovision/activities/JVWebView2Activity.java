
package com.jovision.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.RenderPriority;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovetech.product.IShare;
import com.jovetech.product.Share;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.bean.Channel;
import com.jovision.bean.Device;
import com.jovision.commons.MyGestureDispatcher;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.commons.PlayWindowManager;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.PlayUtil;
import com.jovision.utils.UploadUtil;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMVideo;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 播放包含视频直播和点播
 * 
 * @author Administrator
 */
public class JVWebView2Activity extends BaseActivity implements
        PlayWindowManager.OnUiListener, IShare, OnPreparedListener {

    private static final String TAG = "JVWebView2Activity";

    /** umeng share **/
    private boolean mIsShare;
    private UMSocialService mController;
    private UMVideo mWeixinVideo, mCircleVideo, mWeiboVideo;
    private Share mShare;

    /** topBar **/
    private RelativeLayout topBar;
    protected RelativeLayout.LayoutParams reParamsV;
    protected RelativeLayout.LayoutParams reParamsH;
    protected RelativeLayout.LayoutParams reParamstop1;
    protected RelativeLayout.LayoutParams reParamstop2;

    /** play layout **/
    private RelativeLayout videoLayout;// 视频播放布局
    private RelativeLayout playLayout;
    private SurfaceView playSurfaceView;
    private MediaPlayer vodMediaPlayer;// 点播媒体播放器
    private MediaPlayer mediaPlayer = new MediaPlayer();// 播放抓拍声音的MediaPlayer

    /** 播放按钮 **/
    private ProgressBar loadingVideoBar;// 连接进度条
    private TextView loadingState;// 连接状态
    private TextView linkSpeed;// 连接速度
    private TextView linkParams;// 连接属性
    private ImageView playImgView;// 播放按钮
    private RelativeLayout livePlayBar;// 实时播放工具条
    private RelativeLayout vodPlayBar;// 点播工具条
    private ImageView livePause;// 直播暂停
    private ImageView vodPause;// 点播暂停
    private ImageView captureScreen;// 抓拍
    private ImageView fullScreen;// 全屏
    private LinearLayout linkSetting;
    private EditText minCache;
    private EditText desCache;
    private Button saveSetting;
    private SeekBar vodSeekBar;// 点播进度条

    /** webview网页 **/
    private WebView webView;
    private ImageView loadingBar;
    private LinearLayout loadinglayout;
    private RelativeLayout loadFailedLayout;
    private ImageView reloadImgView;
    private boolean loadFailed = false;
    private RelativeLayout zhezhaoLayout;

    private boolean fullScreenFlag = false;
    private int audioByte = 0;
    private Channel playChannel;
    private boolean isDoubleClickCheck;// 双击事件

    private String webUrl = "";// 完整url
    private String playUrl = "";// 播放地址，rtmp或者流媒体文件地址
    private int titleID = 0;

    private boolean isDisConnected = false;// 断开成功标志
    private boolean manuPause = false;// 人为暂停
    private boolean onPause = false;// onPause
    private boolean vodPaused = false;// 点播暂停
    private boolean onResume = false;// onResume

    private int connectRes3 = 0;// 正常断开
    private int connectRes4 = 0;// 异常断开
    private String uploadUrl = "";// 图片上传地址
    private Boolean isVod;// true：点播 false：直播 2015.5.11

    // 互斥量，seekbar拖动和Timer
    // private volatile boolean isChanging = false;
    private Timer mTimer = new Timer();

    @Override
    protected void initSettings() {
        webUrl = getIntent().getStringExtra("URL");// web完整url
        titleID = getIntent().getIntExtra("title", 0);
        playUrl = getIntent().getStringExtra("rtmp");// 播放地址
        isVod = getIntent().getBooleanExtra("isvod", false);

        if (!isVod) {// 直播
            String cloudNum = getIntent().getStringExtra("cloudnum");
            String channel = getIntent().getStringExtra("channel");
            Device dev = new Device();
            playChannel = new Channel(dev, 1, Integer.parseInt(channel), false,
                    false, cloudNum + "_" + channel);
        }

        int height = disMetrics.heightPixels;
        int width = disMetrics.widthPixels;
        int useWidth = 0;
        if (height < width) {
            useWidth = height;
        } else {
            useWidth = width;
        }
        reParamsV = new RelativeLayout.LayoutParams(useWidth,
                (int) (0.75 * useWidth));
        reParamstop1 = new RelativeLayout.LayoutParams(useWidth,
                (int) (0.75 * useWidth));
        reParamstop2 = new RelativeLayout.LayoutParams(useWidth,
                (int) (0.5 * useWidth));
        reParamsH = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        // 判断是否展示分享功能
        mIsShare = checkShareEnabled(webUrl);
        if (mIsShare) {
            MyLog.e(TAG, "share is enable");
            mShare = Share.getInstance(this);
            mController = mShare.getShareController();
            // 配置需要分享的相关平台
            mShare.configPlatforms();
            // 设置分享的内容
            mShare.setShareContent();
        }
        // 获取当前屏幕方向
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            fullScreenFlag = true;
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            fullScreenFlag = false;
        }
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void initUi() {
        setContentView(R.layout.webview2_layout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (!isVod) {// 直播
            PlayUtil.setContext(JVWebView2Activity.this);
        }

        /** topBar **/
        topBar = (RelativeLayout) findViewById(R.id.topbarh);
        leftBtn = (Button) findViewById(R.id.btn_left);
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        currentMenu.setText(R.string.demo);
        zhezhaoLayout = (RelativeLayout) findViewById(R.id.zhezhao);
        zhezhaoLayout.setLayoutParams(reParamstop1);
        loadingBar = (ImageView) findViewById(R.id.loadingbar);
        loadinglayout = (LinearLayout) findViewById(R.id.loadinglayout);
        linkSetting = (LinearLayout) findViewById(R.id.linksetting);
        minCache = (EditText) findViewById(R.id.mincache);
        desCache = (EditText) findViewById(R.id.descache);
        saveSetting = (Button) findViewById(R.id.savesetting);
        saveSetting.setOnClickListener(myOnClickListener);
        linkParams = (TextView) findViewById(R.id.linkparams);

        if (MySharedPreference.getBoolean(Consts.MORE_LITTLE)) {// 调试版本
            linkSetting.setVisibility(View.VISIBLE);
            linkParams.setVisibility(View.VISIBLE);
        } else {
            linkSetting.setVisibility(View.GONE);
            linkParams.setVisibility(View.GONE);
        }

        loadFailedLayout = (RelativeLayout) findViewById(R.id.loadfailedlayout);
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

        if (mIsShare) {
            // 分享的场合，更改标题栏右上角的图标
            rightBtn.setBackgroundResource(R.drawable.share);
            rightBtn.setOnClickListener(myOnClickListener);
        }

        videoLayout = (RelativeLayout) findViewById(R.id.videolayout);
        playLayout = (RelativeLayout) findViewById(R.id.playlayout);
        playSurfaceView = (SurfaceView) findViewById(R.id.playsurface);
        loadingVideoBar = (ProgressBar) findViewById(R.id.videoloading);
        loadingState = (TextView) findViewById(R.id.playstate);
        playImgView = (ImageView) findViewById(R.id.playview);
        livePlayBar = (RelativeLayout) findViewById(R.id.liveplaybar);
        vodPlayBar = (RelativeLayout) findViewById(R.id.vodplaybar);
        linkSpeed = (TextView) findViewById(R.id.livelinkspeed);
        livePause = (ImageView) findViewById(R.id.pause);
        vodPause = (ImageView) findViewById(R.id.vodpause);
        captureScreen = (ImageView) findViewById(R.id.capturescreen);// 抓拍
        fullScreen = (ImageView) findViewById(R.id.fullscreen);
        webView = (WebView) findViewById(R.id.webview);
        linkSpeed.setOnClickListener(myOnClickListener);
        linkSpeed.setVisibility(View.GONE);
        captureScreen.setOnClickListener(myOnClickListener);
        fullScreen.setOnClickListener(myOnClickListener);
        fullScreen.setVisibility(View.GONE);
        livePlayBar.setVisibility(View.GONE);
        vodPlayBar.setVisibility(View.GONE);
        livePause.setOnClickListener(myOnClickListener);
        vodPause.setOnClickListener(myOnClickListener);
        playImgView.setOnClickListener(myOnClickListener);

        if (isVod) {// 点播
            linkSetting.setVisibility(View.GONE);
            loadingVideoBar.setVisibility(View.GONE);
            livePause.setVisibility(View.GONE);
            playImgView.setVisibility(View.GONE);
            vodMediaPlayer = new MediaPlayer();
            vodSeekBar = (SeekBar) findViewById(R.id.vodplayback_seekback);
            vodSeekBar
                    .setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
                        int progress;

                        @Override
                        public void onProgressChanged(SeekBar seekBar,
                                int progress, boolean fromUser) {
                            this.progress = progress
                                    * vodMediaPlayer.getDuration()
                                    / seekBar.getMax();
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar arg0) {
                            // isChanging = true;// 停止timer
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            vodMediaPlayer.seekTo(progress);
                            // isChanging = false;//

                        }
                    });

        }

        playSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                MyLog.v("LifeCyle", "surfaceDestroyed");
                if (isVod) {// 点播
                } else {
                    stopConnect();
                    playChannel.setSurface(null);
                }
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                MyLog.v("LifeCyle", "surfaceCreated");
                if (isVod) {// 点播
                    loadingState(Consts.RTMP_CONN_SCCUESS);
                    playVodVideo(holder);
                } else {
                    playChannel.setSurface(holder.getSurface());
                    if (!manuPause) {
                        loadingState(Consts.TAG_PLAY_CONNECTING);
                        tensileView(playChannel, playChannel.getSurfaceView());
                    }
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format,
                    int width, int height) {
                MyLog.v("LifeCyle", "surfaceChanged");
                if (isVod) {// 点播
                } else {
                    playChannel.setSurface(holder.getSurface());
                    if (!onPause && !manuPause) {
                        if (false == playChannel.isConnected()
                                && false == playChannel.isConnecting()) {
                            startConnect(playUrl, holder.getSurface());
                        } else {
                            tensileView(playChannel,
                                    playChannel.getSurfaceView());
                            resumeVideo();
                        }
                    }
                }
            }
        });
        setSurfaceSize(fullScreenFlag);
        // fullScreenFlag = false;
        if (!isVod) {
            playChannel.setSurfaceView(playSurfaceView);
        }

        final MyGestureDispatcher dispatcher = new MyGestureDispatcher(
                new MyGestureDispatcher.OnGestureListener() {

                    @Override
                    public void onGesture(int gesture, int distance,
                            Point vector, Point middle) {

                        if (isVod) {// 点播
                            switch (gesture) {
                            // 手势单击双击
                                case MyGestureDispatcher.CLICK_EVENT:
                                    if (0 == lastClickTime) {
                                        isDoubleClickCheck = false;
                                        lastClickTime = System.currentTimeMillis();
                                        handler.sendMessageDelayed(
                                                handler.obtainMessage(
                                                        Consts.WHAT_SURFACEVIEW_CLICK,
                                                        middle.x, middle.y, null),
                                                350);
                                        MyLog.e("Click1--", "单击：lastClickTime="
                                                + lastClickTime);
                                    }
                                    break;
                            }
                        } else {
                            if (null != playChannel
                                    && playChannel.isConnected()
                                    && !playChannel.isConnecting()) {
                                switch (gesture) {
                                // 手势放大缩小
                                    case MyGestureDispatcher.GESTURE_TO_BIGGER:
                                    case MyGestureDispatcher.GESTURE_TO_SMALLER:
                                        gestureOnView(playChannel.getSurfaceView(),
                                                playChannel, gesture, distance,
                                                vector, middle);
                                        lastClickTime = 0;
                                        break;
                                    // 手势云台
                                    case MyGestureDispatcher.GESTURE_TO_LEFT:
                                        gestureOnView(playChannel.getSurfaceView(),
                                                playChannel, gesture, distance,
                                                vector, middle);
                                        lastClickTime = 0;
                                        break;

                                    case MyGestureDispatcher.GESTURE_TO_UP:
                                        gestureOnView(playChannel.getSurfaceView(),
                                                playChannel, gesture, distance,
                                                vector, middle);
                                        lastClickTime = 0;
                                        break;

                                    case MyGestureDispatcher.GESTURE_TO_RIGHT:
                                        gestureOnView(playChannel.getSurfaceView(),
                                                playChannel, gesture, distance,
                                                vector, middle);
                                        lastClickTime = 0;
                                        break;

                                    case MyGestureDispatcher.GESTURE_TO_DOWN:
                                        gestureOnView(playChannel.getSurfaceView(),
                                                playChannel, gesture, distance,
                                                vector, middle);
                                        lastClickTime = 0;
                                        break;
                                    // 手势单击双击
                                    case MyGestureDispatcher.CLICK_EVENT:
                                        if (0 == lastClickTime) {
                                            isDoubleClickCheck = false;
                                            lastClickTime = System
                                                    .currentTimeMillis();
                                            handler.sendMessageDelayed(
                                                    handler.obtainMessage(
                                                            Consts.WHAT_SURFACEVIEW_CLICK,
                                                            middle.x, middle.y,
                                                            playChannel), 350);
                                            MyLog.e("Click1--", "单击：lastClickTime="
                                                    + lastClickTime);
                                        } else {
                                            int clickTimeBetween = (int) (System
                                                    .currentTimeMillis() - lastClickTime);
                                            MyLog.e("Click1--",
                                                    "双击：clickTimeBetween="
                                                            + clickTimeBetween);
                                            if (clickTimeBetween < 350) {// 认为双击
                                                isDoubleClickCheck = true;
                                            }
                                            lastClickTime = 0;
                                        }
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }

                    }
                });
        playSurfaceView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dispatcher.motion(event);
                return true;
            }
        });

        WebChromeClient wvcc = new WebChromeClient() {
            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
                if (-2 == titleID) {
                    currentMenu.setText(ConfigUtil.getShortTile(title));
                    if (mIsShare) {
                        MyLog.v(TAG, "website's title:" + title);
                        // 从webview获取到title以后，展示分享按钮
                        rightBtn.setVisibility(View.VISIBLE);
                        // 设置视频标题
                        mWeixinVideo.setTitle(title);
                        mCircleVideo.setTitle(title);
                        mWeiboVideo.setTitle(title);
                    }
                }
            }
        };
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "wst");

        // 设置setWebChromeClient对象
        webView.setWebChromeClient(wvcc);
        webView.requestFocus(View.FOCUS_DOWN);
        webView.getSettings().setDomStorageEnabled(true);
        // 加快加载速度
        webView.getSettings().setRenderPriority(RenderPriority.HIGH);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode,
                    String description, String failingUrl) {
                MyLog.v(TAG, "webView load failed");
                videoLayout.setVisibility(View.VISIBLE);
                loadFailed = true;
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String newUrl) {
                view.loadUrl(newUrl);
                return true;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                loadinglayout.setVisibility(View.VISIBLE);
                Animation anim = AnimationUtils.loadAnimation(
                        JVWebView2Activity.this, R.anim.rotate);
                loadingBar.setAnimation(anim);
                MyLog.v(TAG, "webView start load");
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (loadFailed) {
                    loadFailedLayout.setVisibility(View.VISIBLE);
                    loadinglayout.setVisibility(View.GONE);
                    webView.setVisibility(View.GONE);
                } else {
                    webView.loadUrl("javascript:(function() { var videos = document.getElementsByTagName('video'); for(var i=0;i<videos.length;i++){videos[i].play();}})()");
                    loadinglayout.setVisibility(View.GONE);
                    videoLayout.setVisibility(View.VISIBLE);
                    loadFailedLayout.setVisibility(View.GONE);
                }
                // webView.loadUrl("javascript:videopayer.play()");
                MyLog.v(TAG, "webView finish load");
                videoLayout.setVisibility(View.VISIBLE);
            }
        });

        loadFailed = false;
        webView.loadUrl(webUrl);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * 播放点播视频
     */
    private void playVodVideo(SurfaceHolder holder) {
        try {
            // 创建一个MediaPlayer对象
            vodMediaPlayer = new MediaPlayer();
            // 设置播放的视频数据源
            vodMediaPlayer.setDataSource(playUrl);
            // 将视频输出到SurfaceView
            vodMediaPlayer.setDisplay(holder);
            // 播放准备，使用异步方式，配合OnPreparedListener
            vodMediaPlayer.prepareAsync();
            // 设置相关的监听器
            vodMediaPlayer.setOnCompletionListener(new OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer arg0) {
                    MyLog.v("LifeCyle", "onCompletion");
                    try {
                        if (onResume) {
                            onResume = false;
                        } else {
                            if (null != vodMediaPlayer
                                    && vodMediaPlayer.isPlaying()) {// 没播放完
                            } else {
                                MyLog.v("LifeCyle", "onCompletion;vodProcess="
                                        + vodProcess
                                        + ";vodMediaPlayer.isPlaying()="
                                        + vodMediaPlayer.isPlaying());
                                JVWebView2Activity.this.finish();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            });
            vodMediaPlayer.setOnPreparedListener(this);
            // 设置音频流类型
            vodMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

            startTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.VOD_VIDEO_CHANG_PROCESS: {
                if (vodCurrentPos > 0) {
                    vodMediaPlayer.seekTo(vodCurrentPos);
                    MyLog.v("LifeCyle", "onResume--seekto=" + vodCurrentPos);
                }
                break;
            }
            case Consts.BBS_IMG_UPLOAD_SUCCESS: {
                dismissDialog();
                if (null != obj) {
                    webView.loadUrl("javascript:uppic(\"" + obj.toString() + "\")");
                } else {
                }
                break;
            }

            case Consts.WHAT_NET_ERROR_DISCONNECT: {
                if (!isVod) {
                    startConnect(playUrl, playChannel.getSurface());
                }
                break;
            }
            case Consts.WHAT_DEMO_BUFFING: {// 缓存中
                loadingState(Consts.RTMP_CONN_SCCUESS);
                break;
            }
            case Consts.WHAT_DEMO_RESUME: {// resume Channel
                if (isVod) {
                    vodMediaPlayer.start();
                } else {
                    resumeVideo();
                }

                break;
            }
            case Consts.CALL_CONNECT_CHANGE: {

                if (arg2 == Consts.BAD_NOT_CONNECT) {
                    connectRes3 = arg2;
                    MyLog.v("reConnect2", "connectRes3=" + connectRes3
                            + ";connectRes4=" + connectRes4);
                } else if (arg2 == Consts.RTMP_EDISCONNECT) {
                    connectRes4 = arg2;
                    MyLog.v("reConnect1", "connectRes3=" + connectRes3
                            + ";connectRes4=" + connectRes4);
                }
                if (connectRes4 == Consts.RTMP_EDISCONNECT
                        && connectRes3 == Consts.BAD_NOT_CONNECT) {
                    MyLog.v("reConnect3", "connectRes3=" + connectRes3
                            + ";connectRes4=" + connectRes4);
                    loadingState(Consts.RTMP_CONN_SCCUESS);
                    connectRes3 = 0;
                    connectRes4 = 0;
                    handler.sendMessageDelayed(
                            handler.obtainMessage(Consts.WHAT_NET_ERROR_DISCONNECT),
                            200);
                } else {
                    MyLog.v("reConnect0", "connectChange=" + arg2);
                    loadingState(arg2);
                }
                break;
            }
            case Consts.CALL_NEW_PICTURE: {
                livePause.setImageDrawable(getResources().getDrawable(
                        R.drawable.video_stop_icon));
                if (MySharedPreference.getBoolean(Consts.MORE_LITTLE)) {// 调试版本
                    linkSpeed.setVisibility(View.VISIBLE);
                } else {
                    linkSpeed.setVisibility(View.GONE);
                }
                loadingState(Consts.CALL_NEW_PICTURE);
                break;
            }

            case Consts.CALL_PLAY_BUFFER: {
                if (arg2 > 0) {
                    // MyLog.i(Consts.TAG_PLAY, "buffering: " + arg2);// 0-99
                    bufferingState(Consts.TAG_PLAY_BUFFERING, arg2);
                } else if (Consts.BUFFER_START == arg2) {
                    // MyLog.w(Consts.TAG_PLAY, "buffer started");// show
                    bufferingState(Consts.TAG_PLAY_BUFFERING, 0);
                } else if (Consts.BUFFER_FINISH == arg2) {
                    // MyLog.w(Consts.TAG_PLAY, "buffer finished");// dismiss
                    bufferingState(Consts.TAG_PLAY_BUFFERED, 0);
                }
                break;
            }
            case Consts.WHAT_SURFACEVIEW_CLICK: {// 单击事件
                if (isVod) {// 点播
                    if (isDoubleClickCheck) {// 双击

                    } else {// 单击
                        if (View.VISIBLE == vodPlayBar.getVisibility()) {
                            vodPlayBar.setVisibility(View.GONE);
                        } else {
                            vodPlayBar.setVisibility(View.VISIBLE);
                        }
                    }
                } else {
                    Channel channel = (Channel) obj;
                    int x = arg1;
                    int y = arg2;
                    if (null != channel && channel.isConnected()
                            && !channel.isConnecting()) {
                        boolean originSize = false;
                        if (channel.getLastPortWidth() == channel.getSurfaceView()
                                .getWidth()) {
                            originSize = true;
                        }

                        if (isDoubleClickCheck) {
                            MyLog.e("Click--", "双击：clickTimeBetween=");
                        } else {
                            MyLog.e("Click--", "单击：time=");
                        }

                        if (isDoubleClickCheck) {// 双击
                            Point vector = new Point();
                            Point middle = new Point();
                            middle.set(x, y);
                            if (originSize) {// 双击放大
                                vector.set(channel.getSurfaceView().getWidth(),
                                        channel.getSurfaceView().getHeight());
                                gestureOnView(channel.getSurfaceView(), channel,
                                        MyGestureDispatcher.GESTURE_TO_BIGGER, 1,
                                        vector, middle);
                            } else {// 双击还原
                                vector.set(-channel.getSurfaceView().getWidth(),
                                        -channel.getSurfaceView().getHeight());
                                gestureOnView(channel.getSurfaceView(), channel,
                                        MyGestureDispatcher.GESTURE_TO_SMALLER, -1,
                                        vector, middle);
                            }
                        } else {// 单击
                            if (View.VISIBLE == livePlayBar.getVisibility()) {
                                livePlayBar.setVisibility(View.GONE);
                                if (MySharedPreference
                                        .getBoolean(Consts.MORE_LITTLE)) {// 调试版本
                                    linkSetting.setVisibility(View.GONE);
                                    linkParams.setVisibility(View.GONE);
                                }
                            } else {
                                livePlayBar.setVisibility(View.VISIBLE);
                                if (MySharedPreference
                                        .getBoolean(Consts.MORE_LITTLE)) {// 调试版本
                                    linkSetting.setVisibility(View.VISIBLE);
                                    linkParams.setVisibility(View.GONE);
                                }
                            }

                        }
                    }
                }

                lastClickTime = 0;
                break;
            }

            case Consts.CALL_STAT_REPORT: {
                try {
                    JSONArray array = new JSONArray(obj.toString());
                    JSONObject object = null;

                    int size = array.length();
                    for (int i = 0; i < size; i++) {
                        object = array.getJSONObject(i);
                        linkSpeed.setText(String.format("%.1fk/%.1fk",
                                object.getDouble("kbps"),
                                object.getDouble("audio_kbps")));

                        linkParams
                                .setText(String
                                        .format("%.1fk/%.1fk/D:%.1fk/AJ:%.1fk/VJ:%.1fk/N:%.1fk/AL:%dk/VL:%dk",
                                                object.getDouble("kbps"),
                                                object.getDouble("audio_kbps"),
                                                object.getDouble("decoder_fps"),
                                                object.getDouble("audio_jump_fps"),
                                                object.getDouble("video_jump_fps"),
                                                object.getDouble("network_fps"),
                                                object.getInt("audio_left"),
                                                object.getInt("video_left"))

                                );
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            }

        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        // MyLog.i(TAG, "onNotify: " + what + ", " + arg1 + ", " + arg2
        // + ", " + obj);
        switch (what) {
            case Consts.CALL_STAT_REPORT: {
                handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
                break;
            }

            case Consts.CALL_CONNECT_CHANGE: {
                if (!isVod) {
                    playChannel.setConnecting(false);
                    switch (arg2) {
                        case Consts.BAD_NOT_CONNECT: {
                            isDisConnected = true;
                            MyLog.e("BAD_NOT_CONNECT", "-3");
                            handler.sendMessage(handler.obtainMessage(what, arg1, arg2,
                                    obj));
                            break;
                        }
                        case Consts.RTMP_CONN_SCCUESS: {
                            playChannel.setConnected(true);
                            if (playChannel.isPaused()) {
                                Jni.resume(playChannel.getIndex(),
                                        playChannel.getSurface());
                            }
                            handler.sendMessage(handler.obtainMessage(what, arg1, arg2,
                                    obj));
                            break;
                        }
                        case Consts.RTMP_CONN_FAILED: {
                            playChannel.setConnected(false);
                            handler.sendMessage(handler.obtainMessage(what, arg1, arg2,
                                    obj));
                            break;
                        }
                        case Consts.RTMP_DISCONNECTED: {
                            playChannel.setConnected(false);
                            handler.sendMessage(handler.obtainMessage(what, arg1, arg2,
                                    obj));
                            break;
                        }
                        case Consts.RTMP_EDISCONNECT: {
                            playChannel.setConnected(false);
                            handler.sendMessage(handler.obtainMessage(what, arg1, arg2,
                                    obj));
                            break;
                        }
                    }
                }
                break;
            }
            case Consts.CALL_NEW_PICTURE: {
                if (!isVod) {
                    startAudio(playChannel.getIndex(), audioByte);
                    handler.sendMessage(handler
                            .obtainMessage(what, arg1, arg2, obj));
                }
                break;
            }
            case Consts.CALL_NORMAL_DATA: {
                if (!isVod) {
                    try {
                        JSONObject jobj;
                        jobj = new JSONObject(obj.toString());
                        if (null != jobj) {
                            audioByte = jobj.optInt("audio_bit");
                            startAudio(playChannel.getIndex(), audioByte);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            }
            default: {
                handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
                break;
            }
        }
    }

    /**
     * 应用层开启音频监听功能
     * 
     * @param index
     * @return
     */
    private boolean startAudio(int index, int audioByte) {
        boolean open = false;
        PlayUtil.startAudioMonitor(index);// enable audio
        open = true;
        return open;
    }

    /**
     * 应用层关闭音频监听功能
     * 
     * @param index
     * @return
     */
    private boolean stopAudio(int index) {
        boolean close = false;
        PlayUtil.stopAudioMonitor(index);// stop audio
        close = true;
        return close;
    }

    /**
     * 设置播放显示的大小
     */
    private void setSurfaceSize(boolean full) {
        if (full) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
            playSurfaceView.setLayoutParams(reParamsH);
            playLayout.setLayoutParams(reParamsH);
            webView.setVisibility(View.GONE);
            topBar.setVisibility(View.GONE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);// 竖屏
            playSurfaceView.setLayoutParams(reParamsV);
            playLayout.setLayoutParams(reParamsV);
            webView.setVisibility(View.VISIBLE);
            topBar.setVisibility(View.VISIBLE);
            webView.getViewTreeObserver().addOnGlobalLayoutListener(
                    new OnGlobalLayoutListener() {

                        @Override
                        public void onGlobalLayout() {
                            if ((disMetrics.heightPixels
                                    - disMetrics.widthPixels * 0.75 - 100)
                                    - webView.getHeight() > 300) {
                                zhezhaoLayout.setLayoutParams(reParamstop2);
                            } else {
                                zhezhaoLayout.setLayoutParams(reParamstop1);
                            }
                        }
                    });
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

    /**
     * 开始连接
     */
    private void startConnect(String playUrl, Object surface) {
        playChannel.setConnecting(true);
        playChannel.setPaused(false);
        manuPause = false;
        Jni.connectRTMP(playChannel.getIndex(), playUrl, surface, false, null);
    };

    /**
     * 断开连接
     */
    private boolean stopConnect() {
        if (!isVod) {
            stopAudio(playChannel.getIndex());
            return Jni.shutdownRTMP(playChannel.getIndex());
        }
        return true;
    };

    /**
     * resume连接
     */
    private boolean resumeVideo() {
        boolean resumeRes = false;
        if (manuPause) {
            resumeRes = true;
            return true;
        } else {
            if (!isVod) {
                if (false == playChannel.isConnected()
                        && false == playChannel.isConnecting()
                        && null != playChannel.getSurface()) {
                    loadingState(Consts.TAG_PLAY_CONNECTING);
                    startConnect(playUrl, playChannel.getSurface());
                    resumeRes = true;
                } else {
                    if (playChannel.isPaused()) {
                        loadingState(Consts.RTMP_CONN_SCCUESS);
                    }

                    if (null != playChannel.getSurface()) {
                        resumeRes = Jni.resume(playChannel.getIndex(),
                                playChannel.getSurface());
                        Jni.setViewPort(playChannel.getIndex(),
                                playChannel.getLastPortLeft(),
                                playChannel.getLastPortBottom(),
                                playChannel.getLastPortWidth(),
                                playChannel.getLastPortHeight());
                    }
                    if (resumeRes) {
                        playChannel.setPaused(false);
                    }
                }
            }
        }

        return resumeRes;
    }

    OnClickListener myOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_left: {
                    backMethod();
                    break;
                }
                case R.id.livelinkspeed: {// 实时连接速度
                    if (MySharedPreference.getBoolean(Consts.MORE_LITTLE)) {// 调试版本
                        if (View.VISIBLE == linkParams.getVisibility()) {
                            linkParams.setVisibility(View.GONE);
                        } else {
                            linkParams.setVisibility(View.VISIBLE);
                        }
                    }
                    break;
                }
                case R.id.savesetting: {// 保存配置
                    if ("".equalsIgnoreCase(minCache.getText().toString())) {
                        showTextToast("哎呀妈呀，啥也不输让我设置啥呀！");
                    } else if ("".equalsIgnoreCase(desCache.getText().toString())) {
                        showTextToast("你在考验我的智商吗？");
                    } else {
                        int min = Integer.parseInt(minCache.getText().toString());
                        int des = Integer.parseInt(desCache.getText().toString());

                        if (min < 0 || min > 25) {
                            showTextToast("你没看到最小限定的范围吗？");
                        } else if (des < 25 || des > 1000) {
                            showTextToast("你没看到最大限定的范围吗？");
                        } else {
                            boolean res = Jni.setFrameCounts(
                                    playChannel.getIndex(), min, des);
                            if (res) {
                                showTextToast("设置成功");
                            } else {
                                showTextToast("设置失败");
                            }
                        }
                    }
                    break;
                }
                case R.id.refreshimg: {// webview刷新
                    loadFailedLayout.setVisibility(View.GONE);
                    loadinglayout.setVisibility(View.VISIBLE);
                    Animation anim = AnimationUtils.loadAnimation(
                            JVWebView2Activity.this, R.anim.rotate);
                    loadingBar.setAnimation(anim);
                    loadFailed = false;
                    webView.reload();
                    // webView.loadUrl(url);
                    break;
                }
                case R.id.pause: {// 直播暂停
                    if (playChannel.isConnected()) {// 已连接
                        manuPause = true;
                        linkSpeed.setVisibility(View.GONE);
                        livePause.setImageDrawable(getResources().getDrawable(
                                R.drawable.video_play_icon));
                        // 暂停视频
                        stopConnect();
                    } else {
                        manuPause = false;
                        // 继续播放视频
                        resumeVideo();
                    }
                    break;
                }
                case R.id.vodpause: {// 点播暂停
                    if (isVod) {

                        if (vodMediaPlayer != null) {
                            if (vodPaused) {// 点播暂停继续播
                                vodPaused = false;
                                vodMediaPlayer.start();// 继续播放
                                vodPause.setImageDrawable(getResources()
                                        .getDrawable(R.drawable.video_stop_icon));
                                MyLog.e(TAG, "徐波");
                            } else {
                                vodPaused = true;
                                vodMediaPlayer.pause();// 暂停而不是停止
                                vodPause.setImageDrawable(getResources()
                                        .getDrawable(R.drawable.video_play_icon));
                                MyLog.e(TAG, "暂停");
                            }
                            MyLog.e(TAG, "别的");
                        }
                    }
                    break;
                }
                case R.id.capturescreen: {// 抓拍
                    if (hasSDCard(5, true) && playChannel.isConnected()) {
                        boolean captureRes = PlayUtil.capture(playChannel
                                .getIndex());
                        if (captureRes) {
                            PlayUtil.prepareAndPlay(mediaPlayer, true);
                            showTextToast(Consts.CAPTURE_PATH);
                            MyLog.e("capture", "success");
                        } else {
                            showTextToast(R.string.str_capture_error);
                        }
                    }
                    break;
                }
                case R.id.fullscreen: {// 全屏
                    if (playChannel.isConnected()) {
                        if (fullScreenFlag) {
                            fullScreenFlag = false;
                            fullScreen.setImageDrawable(getResources().getDrawable(
                                    R.drawable.full_screen_icon));
                        } else {
                            fullScreenFlag = true;
                            fullScreen.setImageDrawable(getResources().getDrawable(
                                    R.drawable.notfull_screen_icon));
                        }
                        setSurfaceSize(fullScreenFlag);
                    }
                    break;
                }
                case R.id.playview: {// 视频播放按钮
                    loadingState(Consts.RTMP_CONN_SCCUESS);
                    if (isVod) {// 播放点播
                        startVideoPlayback();
                    } else {
                        startConnect(playUrl, playChannel.getSurface());
                    }
                    break;
                }
                case R.id.btn_right: {
                    // 分享的场合,视频连接成功后才能分享
                    if (mIsShare) {
                        if (isVod) {// 点播
                            MyLog.v(TAG, "open share pane");

                            if (vodMediaPlayer.isPlaying()) {
                                mShare.openSharePane();
                            } else {
                                showTextToast(R.string.str_wait_connect);
                            }

                        } else {// 直播
                            if (playChannel.isConnected()) {
                                MyLog.v(TAG, "open share pane");
                                mShare.openSharePane();
                            } else {
                                showTextToast(R.string.str_wait_connect);
                            }
                        }

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
            webView.goBack(); // goBack()表示返回WebView的上一页面
        } else {
            if (!isVod) {

                DisConnectTask disTask = new DisConnectTask();
                String[] params = new String[3];
                disTask.execute(params);
            } else {
                this.finish();
            }
        }
    }

    /**
     * 断开视频线程
     * 
     * @author Administrator
     */
    class DisConnectTask extends AsyncTask<String, Integer, Integer> {
        // 可变长的输入参数，与AsyncTask.exucute()对应
        @Override
        protected Integer doInBackground(String... params) {
            int disRes = 0;// 0成功 1失败
            stopConnect();
            int errorCount = 0;
            while (!isDisConnected) {
                errorCount++;
                if (errorCount > 60) {
                    break;
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return disRes;
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
                if (mIsShare) {
                    MyLog.v(TAG,
                            "remove sina's sso handler and clear listeners");
                    mController.getConfig().cleanListeners();
                    mController.getConfig().removeSsoHandler(SHARE_MEDIA.SINA);
                }
                finish();
            }
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

    @Override
    protected void freeMe() {
        MyLog.v("LifeCyle", "freeMe");
        try {
            if (null != mediaPlayer) {
                mediaPlayer.release();
            }
            if (null != vodMediaPlayer) {
                vodMediaPlayer.release();
            }
            stopTimer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 开始timer
    public void startTimer() {
        if (null == mTimer) {
            mTimer = new Timer();
        }
        mTimer.schedule(new MyTimerTask(), 0, 10);
    }

    // 停止timer
    public void stopTimer() {
        if (null != mTimer) {
            mTimer.cancel();
            mTimer = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyLog.v("LifeCyle", "onPause");
        onPause = true;
        if (isVod) {
            vodMediaPlayer.pause();
            stopTimer();
        } else {
            if (!manuPause) {
                stopConnect();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        MyLog.v("LifeCyle", "onResume");
        onPause = false;
        onResume = true;
        if (isVod) {
            handler.sendMessageDelayed(
                    handler.obtainMessage(Consts.WHAT_DEMO_RESUME), 500);
            vodPaused = false;
            vodPause.setImageDrawable(getResources().getDrawable(
                    R.drawable.video_stop_icon));
        } else {
            if (!manuPause) {
                handler.sendMessageDelayed(
                        handler.obtainMessage(Consts.WHAT_DEMO_RESUME), 500);
            }
        }
    }

    @Override
    public void onClick(Channel channel, boolean isFromImageView, int viewId) {

    }

    @Override
    public void onLongClick(Channel channel) {

    }

    // 第一次click时间
    private long lastClickTime = 0;

    @Override
    public void onGesture(int index, int gesture, int distance, Point vector,
            Point middle) {
        if (null != playChannel && playChannel.isConnected()
                && !playChannel.isConnecting()) {
            switch (gesture) {
            // 手势放大缩小
                case MyGestureDispatcher.GESTURE_TO_BIGGER:
                case MyGestureDispatcher.GESTURE_TO_SMALLER:
                    gestureOnView(playChannel.getSurfaceView(), playChannel,
                            gesture, distance, vector, middle);
                    lastClickTime = 0;
                    break;
            }
        }
    }

    private void gestureOnView(View v, Channel channel, int gesture,
            int distance, Point vector, Point middle) {
        int viewWidth = v.getWidth();
        int viewHeight = v.getHeight();

        int left = channel.getLastPortLeft();
        int bottom = channel.getLastPortBottom();
        int width = channel.getLastPortWidth();
        int height = channel.getLastPortHeight();

        boolean needRedraw = false;

        switch (gesture) {
            case MyGestureDispatcher.GESTURE_TO_LEFT:
            case MyGestureDispatcher.GESTURE_TO_UP:
            case MyGestureDispatcher.GESTURE_TO_RIGHT:
            case MyGestureDispatcher.GESTURE_TO_DOWN:
                left += vector.x;
                bottom += vector.y;
                needRedraw = true;
                break;

            case MyGestureDispatcher.GESTURE_TO_BIGGER:
            case MyGestureDispatcher.GESTURE_TO_SMALLER:
                if (width > viewWidth || distance > 0) {
                    float xFactor = (float) vector.x / viewWidth;
                    float yFactor = (float) vector.y / viewHeight;
                    float factor = yFactor;

                    if (distance > 0) {
                        if (xFactor > yFactor) {
                            factor = xFactor;
                        }
                    } else {
                        if (xFactor < yFactor) {
                            factor = xFactor;
                        }
                    }

                    int xMiddle = middle.x - left;
                    int yMiddle = viewHeight - middle.y - bottom;

                    factor += 1;
                    left = middle.x - (int) (xMiddle * factor);
                    bottom = (viewHeight - middle.y) - (int) (yMiddle * factor);
                    width = (int) (width * factor);
                    height = (int) (height * factor);

                    if (width <= viewWidth || height < viewHeight) {
                        left = 0;
                        bottom = 0;
                        width = viewWidth;
                        height = viewHeight;
                    } else if (width > 4000 || height > 4000) {
                        width = channel.getLastPortWidth();
                        height = channel.getLastPortHeight();

                        if (width > height) {
                            factor = 4000.0f / width;
                            width = 4000;
                            height = (int) (height * factor);
                        } else {
                            factor = 4000.0f / height;
                            width = (int) (width * factor);
                            height = 4000;
                        }

                        left = middle.x - (int) (xMiddle * factor);
                        bottom = (viewHeight - middle.y) - (int) (yMiddle * factor);
                    }

                    needRedraw = true;
                }
                break;

            default:
                break;
        }

        if (needRedraw) {
            if (left + width < viewWidth) {
                left = viewWidth - width;
            } else if (left > 0) {
                left = 0;
            }

            if (bottom + height < viewHeight) {
                bottom = viewHeight - height;
            } else if (bottom > 0) {
                bottom = 0;
            }

            channel.setLastPortLeft(left);
            channel.setLastPortBottom(bottom);
            channel.setLastPortWidth(width);
            channel.setLastPortHeight(height);
            Jni.setViewPort(channel.getIndex(), left, bottom, width, height);
        }
    }

    @Override
    public void onLifecycle(int index, int status, Surface surface, int width,
            int height) {
    }

    private void tensileView(Channel channel, View view) {
        channel.setLastPortLeft(0);
        channel.setLastPortBottom(0);
        channel.setLastPortWidth(view.getWidth());
        channel.setLastPortHeight(view.getHeight());
        Jni.setViewPort(channel.getIndex(), 0, 0, view.getWidth(),
                view.getHeight());
    }

    /**
     * 修改连接状态
     * 
     * @param tag
     */
    private void loadingState(int tag) {
        switch (tag) {
            case Consts.TAG_PLAY_CONNECTING: {// 连接中
                loadingVideoBar.setVisibility(View.VISIBLE);
                loadingState.setVisibility(View.VISIBLE);
                playImgView.setVisibility(View.GONE);
                loadingState.setText(R.string.connecting);
                break;
            }
            case Consts.RTMP_CONN_SCCUESS: {
                loadingVideoBar.setVisibility(View.VISIBLE);
                loadingState.setVisibility(View.VISIBLE);
                playImgView.setVisibility(View.GONE);
                loadingState.setText(R.string.connecting_buffer2);

                break;
            }
            case Consts.RTMP_CONN_FAILED: {
                loadingVideoBar.setVisibility(View.GONE);
                loadingState.setVisibility(View.GONE);
                playImgView.setVisibility(View.VISIBLE);
                loadingState.setText(R.string.connect_failed);
                linkSpeed.setVisibility(View.GONE);
                break;
            }
            case Consts.RTMP_DISCONNECTED: {
                loadingVideoBar.setVisibility(View.GONE);
                loadingState.setVisibility(View.GONE);
                playImgView.setVisibility(View.VISIBLE);
                loadingState.setText(R.string.closed);
                linkSpeed.setVisibility(View.GONE);
                break;
            }
            case Consts.RTMP_EDISCONNECT: {
                loadingVideoBar.setVisibility(View.GONE);
                loadingState.setVisibility(View.GONE);
                playImgView.setVisibility(View.VISIBLE);
                // loadingState.setText("断开失败");
                break;
            }
            case Consts.CALL_NEW_PICTURE: {
                loadingVideoBar.setVisibility(View.GONE);
                loadingState.setVisibility(View.GONE);
                playImgView.setVisibility(View.GONE);
                break;
            }
            // 缓冲完成
            case Consts.TAG_PLAY_BUFFERED: {
                loadingVideoBar.setVisibility(View.GONE);
                loadingState.setVisibility(View.GONE);
                // playImgView.setVisibility(View.GONE);
                break;
            }
        }

    }

    /**
     * 修改连接状态
     * 
     * @param tag
     */
    private void bufferingState(int tag, int process) {
        switch (tag) {
            case Consts.TAG_PLAY_BUFFERING: {// 已连接正在缓冲
                loadingVideoBar.setVisibility(View.VISIBLE);
                loadingState.setVisibility(View.VISIBLE);
                playImgView.setVisibility(View.GONE);
                loadingState.setText(getString(R.string.connecting_buffer2)
                        + process + "%");
                break;
            }
            case Consts.TAG_PLAY_BUFFERED: {// 缓冲完成
                loadingVideoBar.setVisibility(View.GONE);
                loadingState.setVisibility(View.GONE);
                playImgView.setVisibility(View.GONE);
                break;
            }

        }

    }

    /**
     * @功能描述 : 根据不同的平台设置不同的分享内容</br>
     * @return
     */
    @Override
    public void setShareContent() {
        // 视频图标
        UMImage urlImage = new UMImage(this, R.drawable.share_logo);
        // 视频链接地址
        String videoUrl = createVideoUrl();
        // 视频内容
        String videoContent = getString(R.string.umeng_socialize_share_video_content);
        // 视频分享
        mWeixinVideo = new UMVideo(createVideoUrlByPlatform(videoUrl, "weixin"));
        mWeixinVideo.setThumb(urlImage);
        // 设置微信分享的内容
        WeiXinShareContent weixinContent = new WeiXinShareContent();
        weixinContent.setShareContent(videoContent);
        weixinContent.setShareMedia(urlImage);
        weixinContent.setShareMedia(mWeixinVideo);
        mController.setShareMedia(weixinContent);
        // 视频分享
        mCircleVideo = new UMVideo(createVideoUrlByPlatform(videoUrl,
                "pengyouquan"));
        mCircleVideo.setThumb(urlImage);
        // 设置朋友圈分享的内容
        CircleShareContent circleContent = new CircleShareContent();
        circleContent.setShareContent(videoContent);
        circleContent.setShareMedia(urlImage);
        circleContent.setShareMedia(mCircleVideo);
        mController.setShareMedia(circleContent);
        // 视频分享
        mWeiboVideo = new UMVideo(createVideoUrlByPlatform(videoUrl, "weibo"));
        mWeiboVideo.setThumb(urlImage);
        // 设置新浪微博分享的内容
        SinaShareContent sinaContent = new SinaShareContent();
        sinaContent.setShareContent(videoContent);
        sinaContent.setShareImage(urlImage);
        sinaContent.setShareMedia(mWeiboVideo);
        mController.setShareMedia(sinaContent);
    }

    /**
     * @功能描述：判断是否展示分享功能<br/>
     * @param pUrl 网址
     * @return true/false
     */
    private boolean checkShareEnabled(String pUrl) {
        MyLog.e(TAG, "check the url:" + pUrl);
        if (pUrl.contains("allowshare")) {
            return true;
        }
        return false;
    }

    /**
     * @功能描述 : 生成视频链接地址</br>
     * @return
     */
    private String createVideoUrl() {
        HashMap<String, String> urlParamsMap;
        // 解析URL
        String urlParamsArray[] = webUrl.split("\\?");
        urlParamsMap = ConfigUtil.genMsgMapFromhpget(urlParamsArray[1]);

        String lang = urlParamsMap.get("lang");
        String vid = urlParamsMap.get("vid");
        String d = urlParamsMap.get("d");

        // 拼接视频地址
        StringBuffer urlStrBuf = new StringBuffer();
        urlStrBuf.append(urlParamsArray[0]);
        // 设置平台标记，不同平台使用时进行替换
        urlStrBuf.append("?plat=platform_flag");
        urlStrBuf.append("&lang=");
        urlStrBuf.append(lang);
        urlStrBuf.append("&vid=");
        urlStrBuf.append(vid);
        urlStrBuf.append("&d=");
        urlStrBuf.append(d);

        MyLog.v(TAG, "video url:" + urlStrBuf.toString());
        return urlStrBuf.toString();
    }

    /**
     * @功能描述 : 根据不同的平台生成视频链接地址</br>
     * @param pVideoUrl 视频链接地址
     * @param platform 分享平台
     * @return
     */
    private String createVideoUrlByPlatform(final String pVideoUrl,
            final String platform) {
        return pVideoUrl.replace("platform_flag", platform);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** 使用SSO授权必须添加如下代码 */
        mShare.setAuthorizeCallBack(requestCode, resultCode, data);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (fullScreenFlag) {
            fullScreenFlag = false;
            fullScreen.setImageDrawable(getResources().getDrawable(
                    R.drawable.full_screen_icon));
        } else {
            fullScreenFlag = true;
            fullScreen.setImageDrawable(getResources().getDrawable(
                    R.drawable.notfull_screen_icon));
        }
        setSurfaceSize(fullScreenFlag);
        super.onConfigurationChanged(newConfig);
    }

    /**
     * 获取图片上传的url:upload_url js的返回值
     * 
     * @param upUrl 即js的返回值
     */
    public void getUploadUrl(String upUrl) {
        uploadUrl = upUrl;
        MyLog.v("uploadUrl", uploadUrl);
    }

    /**
     * js window.wst.cutpic() 2015-03-31 修改上传照片dialog
     */
    public void cutpic() {
        if (hasSDCard(5, true) && playChannel.isConnected()) {
            String savePath = PlayUtil
                    .captureReturnPath(playChannel.getIndex());
            if (null != savePath) {
                final File captureFile = new File(savePath);
                if (null != captureFile) {
                    createDialog("", false);
                    Thread uploadThread = new Thread() {
                        @Override
                        public void run() {
                            String res = UploadUtil.uploadFile(captureFile,
                                    uploadUrl);
                            captureFile.delete();// 上传完图片删除本地图片
                            handler.sendMessage(handler.obtainMessage(
                                    Consts.BBS_IMG_UPLOAD_SUCCESS, 0, 0, res));
                            super.run();
                        }
                    };
                    uploadThread.start();
                }
            }
        }
    }

    /**
     * 显示大图
     */
    public void showPhoto(String imgUrl) {
        MyLog.v(TAG, "showPhoto-url=" + imgUrl);
        if (null != imgUrl && !"".equalsIgnoreCase(imgUrl)) {
            Intent bigImageIntent = new Intent();
            bigImageIntent.setClass(JVWebView2Activity.this,
                    JVNetImageViewActivity.class);
            bigImageIntent.putExtra("ImageUrl", imgUrl);
            JVWebView2Activity.this.startActivity(bigImageIntent);
        } else {
            MyLog.e(TAG, "showPhoto-url=null");
        }

    }

    private int vodProcess = 0;// 进度
    private int vodCurrentPos = 0;
    private int vodDuration = 0;

    /**
     * 点播的seekbar更新
     */
    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            if (vodMediaPlayer == null) {
                return;
            }
            try {
                if (vodMediaPlayer.isPlaying()) {//
                    int position = vodMediaPlayer.getCurrentPosition();
                    int duration = vodMediaPlayer.getDuration();
                    if (duration > 0) {
                        long pos = vodSeekBar.getMax() * position / duration;
                        vodProcess = (int) pos;
                        vodSeekBar.setProgress((int) pos);
                    }
                    vodCurrentPos = vodMediaPlayer.getCurrentPosition();
                    duration = vodMediaPlayer.getDuration();
                    // MyLog.v("MediaPlayer",
                    // "当前：" + vodMediaPlayer.getCurrentPosition()
                    // + "--总进度：" + vodMediaPlayer.getDuration());
                } else {
                    // 正在拖动seekbar,停止Timer
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    };

    // 播放视频的方法
    private void startVideoPlayback() {
        vodMediaPlayer.start();
        loadingState(Consts.CALL_NEW_PICTURE);
        onResume = false;
        // handler.sendMessageDelayed(
        // handler.obtainMessage(Consts.VOD_VIDE_CHANG_PROCESS),100);
    }

    @Override
    public void onPrepared(MediaPlayer arg0) {
        MyLog.v("LifeCyle", "onPrepared");
        startVideoPlayback();
        if (vodCurrentPos > 0) {
            vodMediaPlayer.seekTo(vodCurrentPos);
            MyLog.v("LifeCyle", "onResume--seekto=" + vodCurrentPos);
        }
    }

    @Override
    protected void saveSettings() {

    }
}
