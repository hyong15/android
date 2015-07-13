
package com.jovision.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.commons.MyLog;

import org.json.JSONException;
import org.json.JSONObject;

public class JVPlayMP4Activity extends BaseActivity implements
        SurfaceHolder.Callback, View.OnClickListener,
        SeekBar.OnSeekBarChangeListener {
    private static final String TAG = "OnPlayMP4Activity";
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000; // 需要自己定义标志

    private JVPlayMP4Activity mActivity;

    private SurfaceView mSurfaceView = null;
    private SurfaceHolder surfaceHolder;
    private RelativeLayout playControlLayout = null;
    private View loadingLayout; // 加载界面
    private ImageView pause;
    private TextView stepTv, totalTv;
    private SeekBar playBar;

    private int mSurfaceViewWidth, mSurfaceViewHeight;
    private int mStopSeconds = 0;
    private long exitTime = 0;
    private boolean bShowGoonPlayToast = false;
    private boolean barVisibility = true;
    private long lastShowSeconds = 0;
    private static int surfaceStatus = 0;// 默认没创建
    private String mp4Uri = "";
    private boolean isLocal = true;
    private boolean bPause = false;

    private GestureDetector mGestureDetector = null;
    Handler athandler = new Handler();

    // @Override
    // public boolean onKeyDown( int keyCode,KeyEvent event) {
    // if (keyCode == event.KEYCODE_HOME) {
    // if(LoadingLayout.getVisibility() == View.VISIBLE){
    // Log.e(TAG, "---------------此时屏蔽home键---------------");
    // return false;
    // }
    // }
    // return super.onKeyDown(keyCode, event);
    // }

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        if (obj == null) {
            obj = "";
        }

        MyLog.i("MP4 Activity", "what=" + what + ", arg1=" + arg1 + ", arg2="
                + arg2 + ", obj=" + obj.toString());

        switch (what) {
            case Consts.PERSON_IMGVIEW_VIDEO_MP4INF:// mp4一些基本信息
                                                    // 通过Jni.MP4Prepare回调
                String strMP4Info = obj.toString();
                JSONObject mp4Obj;
                int total_second = 0;
                try {
                    mp4Obj = new JSONObject(strMP4Info);
                    total_second = mp4Obj.optInt("length", 0);
                    int video_width = mp4Obj.optInt("width", 0);
                    int video_height = mp4Obj.optInt("height", 0);
                    onSurfaceSizeInit(video_width, video_height);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                totalTv.setText(playTimeFormat(total_second));
                playBar.setMax(total_second);
                break;
            case Consts.PERSON_IMGVIEW_VIDEO_PLAYPRESS:// 播放时间进度
                // dismissDialog();
                loadingLayout.setVisibility(View.GONE);
                mStopSeconds = arg1;
                if (bShowGoonPlayToast) {
                    String strRestartTime = playTimeFormat(mStopSeconds);
                    String formatString = getResources().getString(
                            R.string.person_imgview_video_playagin);
                    String playAginFinalTip = String.format(formatString,
                            strRestartTime);
                    Toast.makeText(this, playAginFinalTip, Toast.LENGTH_SHORT)
                            .show();
                    bShowGoonPlayToast = false;
                }
                stepTv.setText(playTimeFormat(arg1));
                totalTv.setText(playTimeFormat(arg2));
                playBar.setProgress(arg1);
                break;
            case Consts.PERSON_IMGVIEW_VIDEO_PLAYEND:// 播放结束
                mStopSeconds = 0;// 停止时间注意置0
                finish();
                break;
            case Consts.PERSON_IMGVIEW_VIDEO_PLAYFAIL:// 播放失败
                Toast.makeText(this, obj.toString(), Toast.LENGTH_SHORT).show();
                mStopSeconds = 0;
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        // 从xml里设置横屏，这里动态设置的话， 有时候surfaceview会有问题
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        getWindow().getDecorView().setKeepScreenOn(true);
        playControlLayout.setVisibility(View.VISIBLE);
        lastShowSeconds = System.currentTimeMillis();
        barVisibility = true;
        DismissBar();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        mSurfaceViewWidth = dm.widthPixels;
        mSurfaceViewHeight = dm.heightPixels;
        // 准备播放
        PreparePlayTask task = new PreparePlayTask();
        String[] params = new String[3];
        task.execute(params);

        super.onResume();
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {

    }

    @Override
    protected void initUi() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED,
                FLAG_HOMEKEY_DISPATCHED);// 关键代码
        setContentView(R.layout.play_mp4_layout);

        Intent intent = getIntent();
        if (null != intent) {
            mp4Uri = intent.getStringExtra("URL");
            isLocal = intent.getBooleanExtra("IS_LOCAL", false);
        }
        if (mp4Uri == null || "".equals(mp4Uri)) {
            finish();
            return;
        }
        // Jni.init(getApplication(), 9200, Consts.LOG_PATH);
        // [Neo]
        // Jni.enableLog(true);
        mActivity = this;
        // 初始化Mp4播放库
        Jni.Mp4Init();
        // SD_CARD_PATH+"CSAlarmVOD/848x480_fps25_h264_alaw.mp4"
        // 设置本地MP4路径
        Jni.SetMP4Uri(mp4Uri);

        pause = (ImageView) findViewById(R.id.btn_pause);
        playControlLayout = (RelativeLayout) findViewById(R.id.play_control_bar);
        playBar = (SeekBar) findViewById(R.id.seekbar_def);// 底部的进度控制
        playBar.setOnSeekBarChangeListener(this);
        playBar.setEnabled(false);// 一开始先不支持拖动

        stepTv = (TextView) findViewById(R.id.tv_step);
        totalTv = (TextView) findViewById(R.id.tv_total);
        // 时间进度格式00:00
        stepTv.setText(R.string.person_imgview_video_playdefaulttime);// 00:00
        // 时间总长度格式00:00
        totalTv.setText(R.string.person_imgview_video_playdefaulttime);// 00:00
        loadingLayout = (View) findViewById(R.id.loading);
        pause.setOnClickListener(this);
        mGestureDetector = new GestureDetector(this, new MyGestureListener());
    }

    @Override
    protected void onPause() {
        if (mStopSeconds > 0) {
            // 当home掉然后再进入播放界面时，提示用户从第几秒开始播放
            bShowGoonPlayToast = true;
        } else {
            bShowGoonPlayToast = false;
        }
        // 停止mp4播放

        Jni.Mp4Stop(mStopSeconds);
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - exitTime) > 2000) {
            Toast.makeText(getApplicationContext(),
                    R.string.person_imgview_video_backagin, Toast.LENGTH_SHORT)
                    .show();
            exitTime = System.currentTimeMillis();
        } else {
            // 完全退出播放，需要设置mStopSeconds为0，这样是为了与播放时Home掉区分
            mStopSeconds = 0;// 停止时间注意置0
            finish();
        }
    }

    // 将秒转成00:00的格式
    private String playTimeFormat(int in_seconds) {
        int play_minute = in_seconds / 60;
        int play_second = in_seconds - play_minute * 60;

        String str_minute = String.format("%02d", play_minute);
        String str_second = String.format("%02d", play_second);

        String str_format_res = str_minute + ":" + str_second;

        return str_format_res;
    }

    // 开始播放的流程
    class PreparePlayTask extends AsyncTask<String, Integer, Integer> {

        @Override
        protected Integer doInBackground(String... arg0) {
            int ret = -1;
            int try_cnt = 2;
            do {
                // 准备工作，底层会回调应用层将信息返回过来即onHandler的0xB3
                //
                ret = Jni.Mp4Prepare();
                if (ret == 0) {// 准备成功
                    MyLog.i("MP4", "Jni.Mp4Prepare success:" + ret);
                    int check_surface_cnt = 10;// 10*200
                                               // 即2秒超时，2秒后surfaceview还未创建就退出
                    // 当surfaceview创建成功时才可以播放
                    // 循环等待surfaceview创建成功
                    while (surfaceStatus == 0) {
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        check_surface_cnt--;
                        if (check_surface_cnt < 0) {
                            MyLog.i(TAG, "检测surfaceview状态超时...");
                            break;
                        }
                    }
                    // surface创建成功开始播放
                    if (surfaceStatus == 1) {
                        int ret2 = Jni.Mp4Start(surfaceHolder.getSurface());
                        if (ret2 != 0) {
                            MyLog.i(TAG, "Mp4Start failed, ret:" + ret2);
                            ret = -100;
                            break;
                        } else {
                            MyLog.i(TAG, "Mp4Start OK, ret:" + ret2);
                            ret = ret2;
                            break;
                        }
                    } else {
                        ret = -99;
                        break;
                    }
                } else if (ret == 1) {
                    // 正在播放，需要先停止
                    // ret==1说明播放器播放线程正在工作
                    MyLog.i(TAG, "视频正在播放...");
                    return ret;
                } else if (ret == 2) {
                    // 2：播放器工作线程正在停止，需要等待其完全退出后才可以重新准备播放
                    MyLog.i(TAG, "视频播放线程正在停止，稍后再运行...");
                    // 延时2秒，重新准备
                    try {
                        Thread.sleep(2000);
                        try_cnt--;
                        if (try_cnt < 0) {
                            break;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } while (ret == 2);// ret==2说明线程正在退出，需要循环调用prepare直到线程完全退出，再继续播放

            return ret;
        }

        @Override
        protected void onPreExecute() {
            loadingLayout.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Integer result) {
            if (result < 0) {
                Log.e("MP4", "play failed:" + result);
                Toast.makeText(mActivity, "play failed:" + result,
                        Toast.LENGTH_SHORT).show();
                mActivity.finish();
            } else {
                if (result == 0) {
                    Log.e("MP4", "pre and play success:" + result);
                } else if (result == 1) {
                    // 正在播放，需要先停止
                    MyLog.i(TAG, "视频正在播放...");
                    Toast.makeText(mActivity,
                            R.string.person_imgview_video_playing,
                            Toast.LENGTH_LONG).show();
                } else if (result == 2) {
                    MyLog.i(TAG, "视频播放线程正在停止，稍后再运行...");
                }
            }
        }
    }

    // 根据视频大小与手机屏幕比例设置surfaceview大小，防止画面失真情况
    private void onSurfaceSizeInit(int width, int height) {
        if (width == 0 || height == 0) {
            MyLog.i(TAG, "invalid video width(" + width + ") or height("
                    + height + ")");
            return;
        }

        MyLog.i(TAG, "video width(" + width + ") or height(" + height + ")"
                + "screen width:" + mSurfaceViewWidth + ", screen height:"
                + mSurfaceViewHeight);
        // if(mSurfaceView == null){

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_onplay);

        int w = mSurfaceViewHeight * width / height;
        int margin = (mSurfaceViewWidth - w) / 2;
        Log.e(TAG, "onSurfaceSizeInit margin:" + margin);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        if (margin > 50) {
            lp.setMargins(margin, 0, margin, 0);
        } else {
            margin = 2;// 这里margin不能为0，如果为0,surfaceview创建不成功，不知道为啥
            lp.setMargins(margin, 0, margin, 0);
        }

        mSurfaceView.setLayoutParams(lp);
        surfaceHolder = mSurfaceView.getHolder();// SurfaceHolder是SurfaceView的控制接口
        surfaceHolder.addCallback(this); //

        MyLog.i(TAG, "mSurfaceView is invoke~~~");

        // }
    }

    private class MyGestureListener extends SimpleOnGestureListener {
        /** 双击 */
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return true;
        }

        /** 单击 */
        @Override
        public boolean onSingleTapConfirmed(final MotionEvent e) {
            // 控制单击屏幕，下方的播放进度显示与不显示
            if (barVisibility) {
                playControlLayout.setVisibility(View.GONE);
                barVisibility = false;
            } else {
                barVisibility = true;
                playControlLayout.setVisibility(View.VISIBLE);
                lastShowSeconds = System.currentTimeMillis();
            }
            // return super.onSingleTapConfirmed(e);
            return true;
        }
    }

    // 当下方playbar显示3秒之后自动隐藏
    private void DismissBar() {
        runOnUIThread(new Runnable() {
            public void run() {
                long current_time = System.currentTimeMillis();
                long showed_seconds = current_time - lastShowSeconds;
                if (showed_seconds >= 3000) {
                    playControlLayout.setVisibility(View.GONE);
                    runOnUIThread(this, 3000);
                } else {
                    runOnUIThread(this, (int) (3000 - showed_seconds));
                }

            }
        }, 3000);
    }

    private void runOnUIThread(Runnable runnable, int i) {

        athandler.postDelayed(runnable, i);
    }

    @Override
    protected void saveSettings() {

    }

    @Override
    protected void freeMe() {
        MyLog.i(TAG, "freeMe ondestroy is invoke~~~");
        Jni.Mp4Release();// 释放播放器资源,只有在activity销毁时才调用
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_pause:
                if (!bPause) {
                    bPause = true;
                    Jni.Mp4Pause();// 暂停播放,暂停播放之后想继续播放只能调用MP4Resume函数，不能调用start函数
                    pause.setImageDrawable(getResources().getDrawable(
                            R.drawable.video_play_icon));
                } else {
                    bPause = false;
                    Jni.Mp4Resume();// 继续播放
                    pause.setImageDrawable(getResources().getDrawable(
                            R.drawable.video_stop_icon));
                }
                break;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        MyLog.i(TAG, "surfaceChanged is invoke~~~");
        surfaceStatus = 1;// surface创建成功
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        MyLog.i(TAG, "surfaceCreated is invoke~~~");
        surfaceStatus = 1;// surface创建成功
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        MyLog.i(TAG, "surfaceDestroyed is invoke~~~");
        surfaceStatus = 0;// surface销毁
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
            boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event))
            return true;
        return super.onTouchEvent(event);
    }
}
