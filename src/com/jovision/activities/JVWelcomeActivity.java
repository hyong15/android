
package com.jovision.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.test.AutoLoad;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.bean.User;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.BitmapCache;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.ImportOldData;
import com.jovision.utils.UserUtil;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatConfig;
import com.tencent.stat.StatService;
import com.tencent.stat.common.StatConstants;
import com.umeng.analytics.MobclickAgent;

import java.io.File;

public class JVWelcomeActivity extends BaseActivity {

    private final String TAG = "JVWelcomeActivity";
    private Handler initHandler;
    private ImageView welcomeImage;
    private String welcomePath = "";
    private RelativeLayout cloudseeLayout;
    private RelativeLayout neturalLayout;

    // private static boolean HAS_LOADED = false;

    static {
        AutoLoad.foo();
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {
        Consts.TEST_SERVER = MySharedPreference.getBoolean(
                Consts.MORE_TESTSWITCH, false);// true 测试服务器 ; false 正式服务器
        Consts.FOREIGN_SERVER = MySharedPreference.getBoolean(
                Consts.MORE_FOREIGNSWITCH, false);// true 国外服务器 ; false 国内服务器

        ConfigUtil.getJNIVersion();
        ImportOldData importOld = new ImportOldData(JVWelcomeActivity.this);
        if (!MySharedPreference.getBoolean("HasImport")) {
            Consts.DEVICE_LIST = Consts.LOCAL_DEVICE_LIST;
            importOld.queryAllUserList();
            importOld.getDevList();
            deleteDatabase(Consts.JVCONFIG_DATABASE);
            MySharedPreference.putBoolean("HasImport", true);
        }
        importOld.close();

        getWindowManager().getDefaultDisplay().getMetrics(disMetrics);
        statusHashMap.put(Consts.KEY_INIT_ACCOUNT_SDK, "false");
        statusHashMap.put(Consts.KEY_INIT_CLOUD_SDK, "false");
        statusHashMap.put(Consts.KEY_GONE_MORE, "true");// 是否屏蔽视频广场和更多中的专属功能
                                                         // false 显示，true 隐藏
        statusHashMap.put(Consts.IMEI,
                ConfigUtil.getIMEI(JVWelcomeActivity.this));
        statusHashMap.put(Consts.NEUTRAL_VERSION, "true");// 默认非中性版本
        statusHashMap.put(Consts.LOCAL_LOGIN, "false");// 默认非本地登陆
        statusHashMap.put(Consts.SCREEN_WIDTH,
                String.valueOf(disMetrics.widthPixels));
        statusHashMap.put(Consts.SCREEN_HEIGHT,
                String.valueOf(disMetrics.heightPixels));
        MySharedPreference.putBoolean(Consts.AD_UPDATE, false);
        if (null == statusHashMap.get(Consts.KEY_LAST_LOGIN_TIME)
                || "".equalsIgnoreCase(statusHashMap
                        .get(Consts.KEY_LAST_LOGIN_TIME))) {
            statusHashMap.put(Consts.KEY_LAST_LOGIN_TIME,
                    ConfigUtil.getCurrentDate());
        }

        initThread.start();
        initHandler = new Handler();
        initHandler.postDelayed(jumpThread, 4000);

        // SIMCardUtil siminfo = new SIMCardUtil(this);
        // System.out.println(siminfo.getProvidersName());
        // System.out.println(siminfo.getNativePhoneNumber());
        //
        // MyLog.v("tel",
        // siminfo.getNativePhoneNumber() + "--"
        // + siminfo.getProvidersName());
        // Intent stopIntent1 = new
        // Intent(JVWelcomeActivity.this,Protectservice.class);
        // stopService(stopIntent1);
        // Intent stopIntent = new
        // Intent(JVWelcomeActivity.this,WakeLockService.class);
        // stopService(stopIntent);
    }

    @Override
    protected void initUi() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.welcome_layout);

        Consts.CURRENT_LAN = ConfigUtil.getLanguage2(this);

        welcomeImage = (ImageView) findViewById(R.id.cloudseewelcome);

        if ("false".equalsIgnoreCase(statusHashMap.get(Consts.NEUTRAL_VERSION))) {// 非中性软件
            String fileName = "";
            if (Consts.LANGUAGE_ZH == ConfigUtil
                    .getLanguage2(JVWelcomeActivity.this)) {
                fileName = "welcome_zh";
            } else if (Consts.LANGUAGE_ZHTW == ConfigUtil
                    .getLanguage2(JVWelcomeActivity.this)) {
                fileName = "welcome_zht";
            } else {
                fileName = "welcome_en";
            }

            welcomePath = Consts.WELCOME_IMG_PATH + fileName
                    + Consts.IMAGE_JPG_KIND;
            File imgFile = new File(welcomePath);
            welcomeImage.setScaleType(ScaleType.FIT_XY);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapCache.getInstance().getBitmap(
                        welcomePath, "welcome", fileName);
                if (null != bitmap) {
                    welcomeImage.setImageBitmap(bitmap);
                } else {
                    welcomeImage.setBackgroundDrawable(getResources()
                            .getDrawable(R.drawable.welcome_default2));
                }
            } else {
                welcomeImage.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.welcome_default2));
            }
        } else {// 中性软件
            welcomeImage.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.welcome_netural));
        }

        StatConfig.setDebugEnable(true);

        if (statusHashMap.get(Consts.NEUTRAL_VERSION).equals("false")) {
            String appkey = "A8IA5GMIL13M";
            try {
                StatService.startStatService(JVWelcomeActivity.this, appkey,
                        StatConstants.VERSION);
            } catch (MtaSDkException e) {
                e.printStackTrace();
            }
            StatService.trackCustomEvent(JVWelcomeActivity.this, "onCreat",
                    "welcome");
        }
        // cloudseeLayout = (RelativeLayout) findViewById(R.id.cloudseewelcome);
        // neturalLayout = (RelativeLayout) findViewById(R.id.neturalwelcome);
        //
        // if
        // (statusHashMap.get(Consts.NEUTRAL_VERSION).equalsIgnoreCase("false"))
        // {// CloudSEE
        // cloudseeLayout.setVisibility(View.VISIBLE);
        // neturalLayout.setVisibility(View.GONE);
        // } else {
        // cloudseeLayout.setVisibility(View.GONE);
        // neturalLayout.setVisibility(View.VISIBLE);
        // }
    }

    @Override
    protected void saveSettings() {
    }

    @Override
    protected void freeMe() {

        dismissDialog();
        // 关闭此定时器
        initHandler.removeCallbacks(initThread);
        BitmapCache.getInstance().clearCache();
        welcomeImage.setBackgroundResource(0);
        welcomeImage = null;
        Log.v(TAG, "welcome activity freeme");
    }

    /**
     * 时间到跳转线程
     */
    Thread jumpThread = new Thread() {
        @Override
        public void run() {
            Intent intent = new Intent();
            // 首次登陆,且非公共版本
            if (MySharedPreference.getBoolean(Consts.KEY_SHOW_GUID, true)
                    && "false".equalsIgnoreCase(statusHashMap
                            .get(Consts.NEUTRAL_VERSION))) {

                intent.setClass(JVWelcomeActivity.this, JVGuideActivity.class);

                if (Consts.LANGUAGE_ZH == ConfigUtil
                        .getLanguage2(JVWelcomeActivity.this)) {// 中文
                    intent.putExtra("ArrayFlag", Consts.LANGUAGE_ZH);
                } else {// 英文或其他
                    intent.putExtra("ArrayFlag", Consts.LANGUAGE_EN);
                }
            } else {
                User user = UserUtil.getLastUser();
                if (ConfigUtil.getNetWorkConnection(JVWelcomeActivity.this)
                        && null != user
                        && !"".equalsIgnoreCase(user.getUserName())
                        && !"".equalsIgnoreCase(user.getUserPwd())) {

                    statusHashMap.put(Consts.KEY_USERNAME, user.getUserName());
                    statusHashMap.put(Consts.KEY_PASSWORD, user.getUserPwd());
                    if (MySharedPreference.getBoolean(Consts.MORE_REMEMBER)) {
                        intent.setClass(JVWelcomeActivity.this,
                                JVTabActivity.class);
                        // intent.putExtra("FirstLogin", true);
                        MySharedPreference.putBoolean(Consts.FIRST_LOGIN, true);
                        intent.putExtra("AutoLogin", true);
                    } else {
                        intent.setClass(JVWelcomeActivity.this,
                                JVLoginActivity.class);
                        intent.putExtra("AutoLogin", false);
                    }
                    intent.putExtra("UserName", user.getUserName());
                    intent.putExtra("UserPass", user.getUserPwd());
                    // createDialog(R.string.logining, true);

                } else {
                    intent.setClass(JVWelcomeActivity.this,
                            JVLoginActivity.class);
                }

            }
            JVWelcomeActivity.this.startActivity(intent);
            JVWelcomeActivity.this.finish();
        }
    };

    /**
     * 欢迎界面的一些初始化，设置设备小助手
     */
    Thread initThread = new Thread() {
        @Override
        public void run() {

            // // [Neo] TODO set timer for timeout
            // while (false == HAS_LOADED) {
            //
            // try {
            // sleep(100);
            // } catch (InterruptedException e) {
            // }
            // }
            Looper.prepare();
            ConfigUtil.initCloudSDK(getApplication());// 初始化CloudSDK
            ConfigUtil.getCountry();

            if (ConfigUtil.getNetWorkConnection(JVWelcomeActivity.this)) {
                boolean initASdkState = ConfigUtil
                        .initAccountSDK(getApplication());// 初始化账号SDK

                ConfigUtil.getIMEI(JVWelcomeActivity.this);

                if (!initASdkState) {// 初始化Sdk失败
                    initASdkState = ConfigUtil.initAccountSDK(getApplication());// 初始化账号SDK
                }
                if (!initASdkState) {// 初始化Sdk失败
                    handler.sendMessage(handler.obtainMessage(
                            Consts.WHAT_INIT_ACCOUNT_SDK_FAILED, 0, 0));
                }
                // 提示账号库初始化情况
                if (MySharedPreference.getBoolean(Consts.MORE_LITTLE)) {
                    Toast.makeText(JVWelcomeActivity.this, ConfigUtil.errorMsg,
                            Toast.LENGTH_LONG).show();
                }
            }
            Looper.loop();
        }
    };

    /**
     * 消息处理handler
     * 
     * @author Administrator
     */
    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.WHAT_INIT_ACCOUNT_SDK_FAILED:// 初始化SDK失败
                showTextToast(R.string.str_initsdk_failed);
                break;

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        // welcomeImage = null;

    }

}
