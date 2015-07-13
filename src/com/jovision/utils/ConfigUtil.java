
package com.jovision.utils;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.test.JVACCOUNT;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Jni;
import com.jovision.MainApplication;
import com.jovision.activities.BaseActivity;
import com.jovision.bean.Device;
import com.jovision.bean.Wifi;
import com.jovision.commons.MyLog;
import com.jovision.commons.MySharedPreference;
import com.jovision.commons.Url;
import com.jovision.utils.mails.MailSenderInfo;
import com.jovision.utils.mails.MyAuthenticator;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class ConfigUtil {
    private final static String TAG = "ConfigUtil";
    public final static String ACCOUNT_VERSION = "V3.3.4.5";
    public final static String PLAY_VERSION = "0.9.1b[82250b5][2015-06-18]";
    public final static String NETWORK_VERSION = "v2.0.76.3.40[private:v2.0.75.13 20150618.1]";

    public static String GETACCTOUT_VERSION = "";
    public static String GETPLAY_VERSION = "";
    public static String GETNETWORK_VERSION = "";

    public static String SINA_COUNTRY = "";
    private final static String CHINA_JSON = "{\"country\":\"\u4e2d\u56fd\"}";

    private final static String TaiWan = "台湾";
    private final static String HongKong = "香港";
    private final static String Macau = "澳门";
    private final static String Tunisia = "突尼斯";

    // /**
    // * 获取本地数据库管理对象的引用
    // *
    // * @param con 上下文
    // * @return 数据库管理对象的引用
    // */
    // public static JVConfigManager getDbManager(Context con) {
    // JVConfigManager dbManager = new JVConfigManager(con,
    // JVConst.JVCONFIG_DATABASE, null, JVConst.JVCONFIG_DB_VER);
    // return dbManager;
    // }

    public static String sameVersion = "";
    public static String version = "";
    public static String remoteJNIVersion = "";// 库里面的版本号

    public static void getJNIVersion() {
        // remoteVerStr={"jni":"0.8[9246b6f][2014-11-03]","net":"v2.0.76.3.7[private:v2.0.75.13 201401030.2.d]"}

        try {
            if ("".equalsIgnoreCase(remoteJNIVersion)) {
                remoteJNIVersion = Jni.getVersion();
            }

            JSONObject obj = new JSONObject(remoteJNIVersion);
            String playVersion = obj.optString("jni");
            GETPLAY_VERSION = playVersion;
            String netVersion = obj.optString("net");
            GETNETWORK_VERSION = netVersion;
            if (PLAY_VERSION.equalsIgnoreCase(playVersion)
                    && NETWORK_VERSION.equalsIgnoreCase(netVersion)) {
                sameVersion = "Same";
                MyLog.v(TAG, "Same:localVer=" + PLAY_VERSION + "--"
                        + NETWORK_VERSION + ";\nremoteVer=" + remoteJNIVersion);
            } else {
                sameVersion = "Not-Same";
                MyLog.e(TAG, "Not-Same:localVer=" + PLAY_VERSION + "--"
                        + NETWORK_VERSION + ";\nremoteVerStr="
                        + remoteJNIVersion);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * playversion
     * 
     * @return
     */
    public static String getVersion(Context context) {
        try {
            HashMap<String, String> statusHashMap = ((MainApplication) context
                    .getApplicationContext()).getStatusHashMap();
            if ("".equalsIgnoreCase(version)) {
                String verName = "";
                String pkName = context.getPackageName();
                verName = context.getPackageManager().getPackageInfo(pkName, 0).versionName;
                if ("true".equalsIgnoreCase(statusHashMap
                        .get(Consts.NEUTRAL_VERSION))) {
                    version = verName;
                } else {
                    version = verName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return version;
    }

    /**
     * 获取设备的云视通组
     * 
     * @param deviceNum
     */
    public static String getGroup(String deviceNum) {

        StringBuffer groupSB = new StringBuffer();
        if (!"".equalsIgnoreCase(deviceNum)) {
            for (int i = 0; i < deviceNum.length(); i++) {
                if (Character.isLetter(deviceNum.charAt(i))) { // 用char包装类中的判断字母的方法判断每一个字符
                    groupSB = groupSB.append(deviceNum.charAt(i));
                }
            }
        }
        return groupSB.toString();
    }

    /**
     * 获取设备的云视通组和号码
     * 
     * @param deviceNum
     */
    public static int getYST(String deviceNum) {
        int yst = 0;

        StringBuffer ystSB = new StringBuffer();
        if (!"".equalsIgnoreCase(deviceNum)) {
            for (int i = 0; i < deviceNum.length(); i++) {
                if (Character.isDigit(deviceNum.charAt(i))) {
                    ystSB = ystSB.append(deviceNum.charAt(i));
                }
            }
        }

        if ("".equalsIgnoreCase(ystSB.toString())) {
            yst = 0;
        } else {
            yst = Integer.parseInt(ystSB.toString());
        }
        return yst;
    }

    private static String country = "";

    /**
     * 获取国家
     * 
     * @return
     */
    // //
    // http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=js
    // URL whatismyip = new URL(Url.COUNTRY_URL);
    // in = new BufferedReader(new InputStreamReader(
    // whatismyip.openStream(), "GBK"));
    // // var remote_ip_info =
    // //
    // {"ret":1,"start":-1,"end":-1,"country":"\u4e2d\u56fd","province":"\u5c71\u4e1c","city":"\u6d4e\u5357","district":"","isp":"","type":"","desc":""};
    // requestRes = in.readLine();
    public static String getCountry() {
        MyLog.v(TAG, "getCountry---E");
        if ("".equalsIgnoreCase(country) || country.startsWith("EChina")) {// 国家未取到再调用新浪接口
            String requestRes = "China0";
            BufferedReader in = null;
            try {
                requestRes = JSONUtil.getRequest3(Url.COUNTRY_URL);
                SINA_COUNTRY = requestRes;
                MyLog.v("getCountry--requestRes", requestRes);
                if (null != requestRes && !"".equalsIgnoreCase(requestRes)) {// 字符串不为null
                                                                             // 且不为空
                    String jsonStr = requestRes.substring(
                            requestRes.indexOf("{"),
                            requestRes.indexOf("}") + 1);
                    MyLog.v("getCountry--jsonStr", jsonStr);
                    JSONObject obj = new JSONObject(jsonStr);

                    int res = obj.getInt("ret");
                    if (1 == res) {
                        country = obj.getString("country") + "-"
                                + obj.getString("province") + "-"
                                + obj.getString("city");
                    } else {
                        country = "EChina3";// 有返回，返回的结果明确说错误
                    }
                } else {
                    country = "EChina1";// 有返回，返回的结果不对
                }
            } catch (Exception e) {
                country = "EChina2";// 抛异常，解析出错
                e.printStackTrace();
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            String cacheCountry = MySharedPreference.getString("SINA_COUNTRY");
            MyLog.v(TAG, "1-country(新浪返回国家):" + country);
            MyLog.v(TAG, "2-cacheCountry(缓存国家):" + cacheCountry);
            if ("".equalsIgnoreCase(country) || country.startsWith("EChina")) {// 国家获取出错
                if (null != cacheCountry && !"".equalsIgnoreCase(cacheCountry)) {
                    country = cacheCountry;
                    MyLog.v(TAG, "3-返回结果:" + country);
                }
            } else {// 国家获取成功
                if (!country.equalsIgnoreCase(cacheCountry)) {
                    MySharedPreference.putString("SINA_COUNTRY", country);
                    MyLog.v(TAG, "3-返回结果（sina和缓存不同）:" + country);
                } else {
                    MyLog.v(TAG, "3-返回结果（sina和缓存一样）:" + country);
                }
            }
        }

        MyLog.v(TAG, "getCountry---X:county=" + country);
        return country;
    }

    //
    // public static String getRealCountry() {
    // MyLog.v(TAG, "getCountry---E");
    // if ("".equalsIgnoreCase(country) || country.startsWith("EChina")) {//
    // 国家未取到再调用新浪接口
    // String requestRes = "China0";
    // BufferedReader in = null;
    // try {
    // requestRes = JSONUtil.getRequest3(Url.COUNTRY_URL);
    // // requestRes = "";
    // SINA_COUNTRY = requestRes;
    // MyLog.v("getCountry--requestRes", requestRes);
    // if (null != requestRes && !"".equalsIgnoreCase(requestRes)) {// 字符串不为null
    // // 且不为空
    // String jsonStr = requestRes.substring(
    // requestRes.indexOf("{"),
    // requestRes.indexOf("}") + 1);
    // MyLog.v("getCountry--jsonStr", jsonStr);
    // JSONObject obj = new JSONObject(jsonStr);
    // country = obj.getString("country");
    // } else {
    // country = "EChina1";
    // }
    // } catch (Exception e) {
    // country = "EChina2";
    // e.printStackTrace();
    // } finally {
    // if (in != null) {
    // try {
    // in.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    // }
    // }
    // MyLog.v("getCountry", country);
    // MyLog.v(TAG, "getCountry---X");
    // return country;
    // }

    public static String getBase64(String str) {
        byte[] b = null;
        String s = null;
        try {
            b = str.getBytes("utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (b != null) {
            s = Base64.encodeToString(b, Base64.NO_WRAP);
        }
        return s;
    }

    public static String encodeStr(String str) {
        // 将Unicode字符串转换为汉字输出
        String s[] = str.split("\\\\u");
        String t = "";
        for (int j = 1; j < s.length; j++) {
            int ab = Integer.valueOf(s[j], 16);// 先将16进制转换为整数
            char ac = (char) ab;// 再将整数转换为字符
            System.out.println(ac);
            t = t + ac;
        }
        return t;
    }

    public static int lan = -1;

    /**
     * 中文 1 英文2
     * 
     * @return
     */
    public static int getServerLanguage() {

        if (Consts.FOREIGN_SERVER) {
            lan = Consts.LANGUAGE_EN;
            MyLog.v("local-country", "假的国外位置");
            return lan;
        }
        String china = "";
        try {
            JSONObject chinaObj = new JSONObject(CHINA_JSON);
            china = chinaObj.getString("country");
            MyLog.v("local-country", china);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (-1 == lan) {
            String country = getCountry();// 可防止重复调用
            MyLog.v("country",
                    "flag1-" + country.contains(china) + "-flag2-"
                            + country.contains("China") + "-flag3-"
                            + country.contains("china"));
            if (country.contains("EChina")) {// 访问新浪接口出错
                if (Consts.CURRENT_LAN == Consts.LANGUAGE_ZH) {
                    lan = Consts.LANGUAGE_ZH;
                } else {
                    lan = Consts.LANGUAGE_EN;
                }
            } else {
                if (country.contains(china) || country.contains("China")
                        || country.contains("china")) {
                    lan = Consts.LANGUAGE_ZH;
                } else {
                    lan = Consts.LANGUAGE_EN;
                }
            }
        }
        MyLog.v("country", "lan=" + lan + ";中文 1 英文2");
        return lan;
    }

    /**
     * 注册时台湾香港澳门也认为是国外，我不是台独奥 中文 1 英文2
     * 
     * @return
     */
    public static int getRegistServerLanguage() {
        int lan = -1;
        if (Consts.FOREIGN_SERVER) {
            lan = Consts.LANGUAGE_EN;
            MyLog.v("local-country", "假的国外位置");
            return lan;
        }
        String china = "";
        try {
            JSONObject chinaObj = new JSONObject(CHINA_JSON);
            china = chinaObj.getString("country");
            MyLog.v("local-country", china);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (-1 == lan) {
            String country = getCountry();// 可防止重复调用
            MyLog.v("country",
                    "flag1-" + country.contains(china) + "-flag2-"
                            + country.contains("China") + "-flag3-"
                            + country.contains("china"));
            if (country.contains("EChina")) {// 访问新浪接口出错
                if (Consts.CURRENT_LAN == Consts.LANGUAGE_ZH) {
                    lan = Consts.LANGUAGE_ZH;
                } else {
                    lan = Consts.LANGUAGE_EN;
                }
            } else {
                if (country.contains(china) || country.contains("China")
                        || country.contains("china")) {
                    if (country.contains(TaiWan) || country.contains(HongKong)
                            || country.contains(Macau)) {
                        lan = Consts.LANGUAGE_EN;
                    } else {
                        lan = Consts.LANGUAGE_ZH;
                    }
                } else {
                    lan = Consts.LANGUAGE_EN;
                }
            }
        }
        MyLog.v("country", "lan=" + lan + ";中文 1 英文2");
        return lan;
    }

    /**
     * 获取系统语言
     * 
     * @return 1:中文 2:英文
     */
    public static int getLanguage2(Context context) {

        String language = context.getResources().getConfiguration().locale
                .getCountry();
        MyLog.v("language", "language=" + language);

        int lan = Consts.LANGUAGE_ZH;
        if (language.equalsIgnoreCase("cn")) {// 简体
            lan = Consts.LANGUAGE_ZH;
        } else if (language.equalsIgnoreCase("tw")) {// 繁体
            lan = Consts.LANGUAGE_ZHTW;
        } else {// 英文
            lan = Consts.LANGUAGE_EN;
        }
        return lan;
    }

    /**
     * 唯一的设备ID： 　　* GSM手机的 IMEI 和 CDMA手机的 MEID. 　　* Return null if device ID is
     * not available. IMEI --International Mobile Equipment Identity
     * EMID---Mobile Equipment IDentifier 都是用来唯一标示移动终端，MEID主要分配给CDMA制式的手机
     * 于是你可以参考EMID_百度百科 自我感觉两者有点类似ipv4与ipv6的关系，都是资源枯竭导致的 　　
     */
    public static String getIMEI(Context context) {
        String IMEI = "";
        if (!"".equalsIgnoreCase(IMEI)) {
            return IMEI;
        } else {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);
            IMEI = telephonyManager.getDeviceId();
        }
        if (null == IMEI || "".equalsIgnoreCase(IMEI)) {
            StringBuffer deviceId = new StringBuffer();
            // wifi mac地址
            WifiManager wifi = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifi.getConnectionInfo();
            String wifiMac = info.getMacAddress();
            if (!"".equalsIgnoreCase(wifiMac)) {
                deviceId.append(wifiMac);
            }
            IMEI = deviceId.toString();
        }

        MyLog.v("IMEI", IMEI);
        return IMEI;
    }

    /**
     * 自定义加载progressDialog
     * 
     * @author suifupeng
     * @param context
     * @param msg 为null时不显示文字
     * @param cancelable 是否可以取消
     */
    public static Dialog createLoadingDialog(Context context, String msg,
            boolean cancelable) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        if (null != msg) {
            tipTextView.setVisibility(View.VISIBLE);
            tipTextView.setText(msg);// 设置加载信息
        } else {
            tipTextView.setVisibility(View.GONE);
        }

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(cancelable);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        return loadingDialog;
    }

    public static String errorMsg;

    /**
     * 初始化sdk
     * 
     * @param context 上下文
     * @return 初始化sdk是否成功
     */
    public static boolean initAccountSDK(Context context) {
        MyLog.e(TAG, "initAccountSDK--E");
        boolean result = false;
        HashMap<String, String> statusHashMap = ((MainApplication) context
                .getApplicationContext()).getStatusHashMap();
        if ("false".equals(statusHashMap.get(Consts.KEY_INIT_ACCOUNT_SDK))) {
            int init_try_count = 5;
            do {
                result = JVACCOUNT.InitSDK(context, Consts.ACCOUNT_PATH);
                MyLog.v(TAG, "InitSDK=" + result);

                if (!result) {
                    init_try_count--;
                    if (init_try_count == 0) {
                        break;
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } while (!result);

            errorMsg = "InitSDK=" + result + ";init_try_count=" + init_try_count;
            statusHashMap.put(Consts.KEY_INIT_ACCOUNT_SDK,
                    String.valueOf(result));
            if (!result) {
                MyLog.e(TAG, "InitSDK failed need to restart APP");
                return result;
            }
            MyLog.v(TAG, "AccountSDK Version:=" + JVACCOUNT.GetVersion(0));
            int ret = JVACCOUNT.ConfigClusterServer(Url.ACCOUNT_URL);
            if (ret != 0) {
                MyLog.e(TAG, "ConfigClusterServer failed:" + ret);
            }

            errorMsg = "InitSDK=success;ConfigClusterServer=" + ret;
            MyLog.v(TAG, "Sever URL = " + Url.ACCOUNT_URL);
        } else {
            errorMsg = "initAccountSDK Success";
        }
        MyLog.e(TAG, "initAccountSDK--X--result=" + result);
        return result;
    }

    /**
     * 开启广播
     */
    public static int openBroadCast() {
        int res = -1;
        if (MySharedPreference.getBoolean(Consts.MORE_BROADCAST, true)) {
            MyLog.v(TAG, "enable  broad = " + true);
            res = Jni.searchLanServer(9400, 6666);
        } else {
            MyLog.v(TAG, "unEnable  broad = " + false);
        }
        return res;
    }

    /**
     * 停止广播
     */
    public static void stopBroadCast() {
        if (MySharedPreference.getBoolean(Consts.MORE_BROADCAST, true)) {
            Jni.stopSearchLanServer();
        }
    }

    public static int port = 9200;
    public static StringBuffer allCountryInfo = new StringBuffer();// 完整的国家

    /**
     * 初始化sdk
     * 
     * @param context 上下文
     * @return 初始化sdk是否成功
     */
    public static boolean initCloudSDK(Context context) {
        MyLog.e(TAG, "initCloudSDK--E");
        boolean result = false;
        HashMap<String, String> statusHashMap = ((MainApplication) context
                .getApplicationContext()).getStatusHashMap();
        if ("false".equals(statusHashMap.get(Consts.KEY_INIT_CLOUD_SDK))) {

            String fullCountry = getCountry();// 可防止重复调用
            // TODO 1
            allCountryInfo.append("fullCountry=" + fullCountry + "---");

            if (fullCountry.contains("-")) {
                String[] array = fullCountry.split("-");
                if (null != array && 0 != array.length) {
                    fullCountry = array[0];
                }
            }

            // TODO 2
            allCountryInfo.append("Country=" + fullCountry + "---");

            if (fullCountry.contains(Tunisia)) {
                port = 0;
            }

            // TODO 3
            allCountryInfo.append("port=" + port);

            result = Jni.init(context, port, Consts.LOG_PATH);
            MyLog.v(TAG, "initResult=" + result);

            // 获取mtu选中的值
            int selectedValue = MySharedPreference.getInt(
                    Consts.PROFILE_SETTING_MTU_RADIO_BTN, 1);
            switch (selectedValue) {
                case 0:// mtu -> 700
                    Jni.SetMTU(Consts.MTU_700);
                    MyLog.v(TAG, "SetMTU=" + Consts.MTU_700);
                    break;
                case 1:// mtu -> 1400
                    Jni.SetMTU(Consts.MTU_1400);
                    MyLog.v(TAG, "SetMTU=" + Consts.MTU_1400);
                    break;
                default:
            }

            boolean enableLog = false;
            if (MySharedPreference.getBoolean(Consts.MORE_LITTLEHELP, true)) {
                enableLog = true;
            }
            Jni.enableLog(enableLog);

            MyLog.v("haha2", "enableLog=" + enableLog);
            ConfigUtil.getJNIVersion();
            Jni.setThumb(320, 90);
            Jni.setStat(true);
            if (MySharedPreference.getBoolean(Consts.MORE_LITTLEHELP, true)) {
                boolean enableHelper = Jni.enableLinkHelper(true, 3, 10);// 开小助手
                MyLog.v(TAG, "enableHelper=" + enableHelper);
            } else {
                MyLog.v(TAG, "no need enableHelper");
            }

            int openBroadRes = openBroadCast();// 开广播
            MyLog.v(TAG, "openBroadRes=" + openBroadRes);
            statusHashMap
                    .put(Consts.KEY_INIT_CLOUD_SDK, String.valueOf(result));

        } else {
            MyLog.e(TAG, "no need to init sdk");
        }

        MyLog.e(TAG, "initCloudSDK--X--result=" + result);
        return result;
    }

    private static boolean flag = true;

    /**
     * 判断网络是否真正可用
     * 
     * @return 是否可用
     */
    public static boolean getNetWorkConnection(Context context) {
        flag = false;

        ConnectivityManager cManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            int type = info.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
            } else {
            }
            flag = true;
        } else {
            flag = false;
        }

        // new Thread() {
        // public void run() {
        // String serverURL = "";
        // if (isLanZH()) {
        // serverURL = "http://www.baidu.com/";
        // } else {
        // serverURL = "http://www.google.com/";
        // }
        // HttpGet httpRequest = new HttpGet(serverURL);// 建立http get联机
        // // HttpResponse httpResponse;
        // try {
        // new DefaultHttpClient().execute(httpRequest);
        // flag = true;
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // };
        // }.start();
        // for (int i = 0; i < 3; i++) {
        // if (!flag) {
        // try {
        // Thread.sleep(1000);
        // } catch (InterruptedException e) {
        // e.printStackTrace();
        // }
        // }
        // }
        return flag;
    }

    /**
     * 判断当前 手机是否联网
     * 
     * @param context Context上下文对象
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager cManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            int type = info.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
            } else {
            }
            return true;
        } else
            return false;
    }

    /**
     * 是否是3G网络环境
     * 
     * @param context 上下文
     * @param alert 是否弹出提示
     * @return 是否是3G网络
     */
    public static boolean is3G(Context context, boolean alert) {
        ConnectivityManager cManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            int type = info.getType();
            if (type == ConnectivityManager.TYPE_WIFI) {
                return false;
            } else {
                if (alert) {
                    Toast.makeText(context, R.string.tips_3g, Toast.LENGTH_LONG)
                            .show();
                }
                return true;
            }
        } else
            return false;
    }

    /**
     * 注销方法
     */
    public static void logOut() {
        // Jni.stopSearchLanServer();
        // Jni.enableLinkHelper(false, 3, 0);
        // Jni.deinitAudioEncoder();
        // Jni.deinit();
    }

    /**
     * 验证云通号
     * 
     * @param ystEdit
     * @return
     */
    public static boolean checkYSTNum(String ystEdit) {
        boolean flag = true;

        try {
            int kkk;
            for (kkk = 0; kkk < ystEdit.length(); kkk++) {
                char c = ystEdit.charAt(kkk);
                if (java.lang.Character.isDigit(c)) {
                    break;
                }
                // if (c <= '9' && c >= '0') {
                // break;
                // }
            }
            String group = ystEdit.substring(0, kkk);
            String yst = ystEdit.substring(kkk);
            if (group.length() <= 0 || group.length() > 4) {// 组号长度不对
                flag = false;
                return flag;
            }
            for (int mm = 0; mm < group.length(); mm++) {// 组号含有非字母
                char c = ystEdit.charAt(mm);
                boolean isLetter = java.lang.Character.isLetter(c);
                if (!isLetter) {
                    flag = false;
                    return flag;
                }

                // if (mm == 0
                // && ((c == 'A' || c == 'a' || c == 'B' || c == 'b'
                // || c == 'S' || c == 's'))) {
                //
                // } else {
                // flag = false;
                // }
            }

            for (int i = 0; i < yst.length(); i++) {
                char c = yst.charAt(i);
                if (java.lang.Character.isDigit(c)) {

                } else {
                    flag = false;
                }
            }
            int ystValue = "".equals(yst) ? 0 : Integer.parseInt(yst);
            if (ystValue <= 0) {// 云视通号不正确
                flag = false;
            }
        } catch (Exception e) {
            flag = false;
            e.printStackTrace();
        }

        return flag;

    }

    /**
     * 检查ip地址格式是否正确
     * 
     * @author suifupeng
     * @param ipAdress
     * @return
     */
    public static boolean checkIPAdress(String ipAddress) {
        // if (ipAddress
        // .matches("^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$"))
        // {
        // return true;
        // } else {
        // return false;
        // }
        Pattern pattern = Pattern
                .compile("^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}$");
        Matcher matcher = pattern.matcher(ipAddress);
        if (matcher.find()) {
            return true;
        } else {
            if (ipAddress
                    .matches("^(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])$")) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 检查端口号格式是否正确
     * 
     * @author suifupeng
     * @param portNum
     * @return
     */
    public static boolean checkPortNum(String portNum) {
        if (portNum.length() < 1 || portNum.length() > 5) {
            return false;
        }
        for (int i = 0; i < portNum.length(); i++) {
            char c = portNum.charAt(i);
            if (c > '9' || c < '0') {
                return false;
            }
        }
        if (Integer.valueOf(portNum).intValue() <= 0
                && Integer.valueOf(portNum).intValue() > 65535) {
            return false;
        }
        return true;
    }

    // 验证昵称
    public static boolean checkNickName(String str) {
        boolean flag = false;
        try {
            byte[] b = str.getBytes("UTF-8");
            str = new String(b, "UTF-8");
            Pattern pattern = Pattern
                    .compile("^[A-Za-z0-9_.()\\+\\-\\u4e00-\\u9fa5]{1,20}$");

            Matcher matcher = pattern.matcher(str);
            if (matcher.matches() && 20 >= str.getBytes().length) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (UnsupportedEncodingException e) {
            flag = false;
        }
        return flag;
    }

    // 验证联系方式
    public static boolean checkUserConnect(String str) {
        boolean flag = false;
        try {
            byte[] b = str.getBytes("UTF-8");
            str = new String(b, "UTF-8");
            Pattern pattern = Pattern
                    .compile("^[A-Za-z0-9_.@ \\+\\-\\u4e00-\\u9fa5]{1,200}$");

            Matcher matcher = pattern.matcher(str);
            if (matcher.matches() && 200 >= str.getBytes().length) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (UnsupportedEncodingException e) {
            flag = false;
        }
        return flag;
    }

    // 验证反馈内容
    public static boolean checkUserContent(String str) {
        boolean flag = false;
        try {
            byte[] b = str.getBytes("UTF-8");
            str = new String(b, "UTF-8");
            Pattern pattern = Pattern
                    .compile("^[A-Za-z0-9_.,?!:。，！？：%}{@ \\+\\-\\u4e00-\\u9fa5]{1,1000}$");

            Matcher matcher = pattern.matcher(str);
            if (matcher.matches() && 200 >= str.getBytes().length) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (UnsupportedEncodingException e) {
            flag = false;
        }
        return flag;
    }

    // 验证设备用户名
    public static boolean checkDeviceUsername(String str) {
        boolean flag = false;
        try {
            byte[] b = str.getBytes("UTF-8");
            str = new String(b, "UTF-8");
            Pattern pattern = Pattern.compile("^[A-Za-z0-9_\\-]{1,16}$");
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (UnsupportedEncodingException e) {
            flag = false;
        }
        // if(0 < str.length() && 16 > str.length()){
        // flag = true;
        // }

        return flag;

    }

    /**
     * 验证针对账号库的设备密码
     * 
     * @param str
     * @return
     */
    public static boolean checkDevicePwd(String str) {
        boolean flag = false;
        // try {
        // byte[] b = str.getBytes("UTF-8");
        // str = new String(b, "UTF-8");
        // Pattern pattern = Pattern.compile("^[A-Za-z0-9_\\-]{0,15}$");
        // Matcher matcher = pattern.matcher(str);
        // if (matcher.matches()) {
        // flag = true;
        // } else {
        // flag = false;
        // }
        // } catch (UnsupportedEncodingException e) {
        // flag = false;
        // }
        if (0 <= str.length() && 16 > str.length()) {
            flag = true;
        }
        return flag;
    }

    /**
     * 2015-3-11 验证针对设备密码
     * 
     * @param str
     * @return 中文返回false 非中文返回true
     */
    public static boolean checkDevPwd(String str) {
        boolean flag = false;
        try {
            byte[] b = str.getBytes("UTF-8");
            str = new String(b, "UTF-8");
            Pattern pattern = Pattern.compile("[^\u4e00-\u9fa5]{0,12}$");// 非中文正则
            Matcher matcher = pattern.matcher(str);
            if (matcher.matches()) {
                flag = true;
            } else {
                flag = false;
            }
        } catch (UnsupportedEncodingException e) {
            flag = false;
        }
        return flag;

    }

    /**
     * 发送邮件给多个接收者
     * 
     * @param mailInfo 带发送邮件的信息
     * @return
     */
    public static boolean sendMailtoMultiReceiver(MailSenderInfo mailInfo) {
        MyAuthenticator authenticator = null;
        if (mailInfo.isValidate()) {
            authenticator = new MyAuthenticator(mailInfo.getUserName(),
                    mailInfo.getPassword());
        }
        Session sendMailSession = Session.getInstance(mailInfo.getProperties(),
                authenticator);
        try {
            Message mailMessage = new MimeMessage(sendMailSession);
            // 创建邮件发送者地址
            Address from = new InternetAddress(mailInfo.getFromAddress());
            mailMessage.setFrom(from);
            // 创建邮件的接收者地址，并设置到邮件消息中
            Address[] tos = null;
            String[] receivers = mailInfo.getReceivers();
            if (receivers != null) {
                // 为每个邮件接收者创建一个地址
                tos = new InternetAddress[receivers.length + 1];
                tos[0] = new InternetAddress(mailInfo.getToAddress());
                for (int i = 0; i < receivers.length; i++) {
                    tos[i + 1] = new InternetAddress(receivers[i]);
                }
            } else {
                tos = new InternetAddress[1];
                tos[0] = new InternetAddress(mailInfo.getToAddress());
            }
            // 将所有接收者地址都添加到邮件接收者属性中
            mailMessage.setRecipients(Message.RecipientType.TO, tos);

            mailMessage.setSubject(mailInfo.getSubject());
            mailMessage.setSentDate(new Date());
            // 设置邮件内容
            Multipart mainPart = new MimeMultipart();
            BodyPart html = new MimeBodyPart();
            html.setContent(mailInfo.getContent(), "text/html; charset=GBK");
            mainPart.addBodyPart(html);
            mailMessage.setContent(mainPart);
            // 发送邮件
            Transport.send(mailMessage);
            return true;
        } catch (MessagingException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    // 获取当前系统时间
    public static String getCurrentTime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String time = formatter.format(curDate);
        return time;
    }

    // 获取当前系统时间
    public static String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String time = formatter.format(curDate);
        return time;
    }

    /**
     * 特定 json 转 HashMap
     * 
     * @param json
     * @param keyOfMsg 消息的键名
     * @return
     */
    public static HashMap<String, String> genMsgMap(String msg) {
        HashMap<String, String> map = new HashMap<String, String>();

        if (null == msg || "".equalsIgnoreCase(msg)) {
            return null;
        }
        Matcher matcher = Pattern.compile("([^=;]+)=([^=;]+)").matcher(msg);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    /**
     * 特定 json 转 HashMap 不会覆盖
     * 
     * @param json
     * @param keyOfMsg 消息的键名
     * @return
     */
    public static HashMap<String, String> genMsgMap1(String msg) {
        HashMap<String, String> map = new HashMap<String, String>();

        if (null == msg || "".equalsIgnoreCase(msg)) {
            return null;
        }
        Matcher matcher = Pattern.compile("([^=;]+)=([^=;]+)").matcher(msg);
        while (matcher.find()) {
            if (null != map.get(matcher.group(1))
                    && !"".equalsIgnoreCase(matcher.group(1))) {

            } else {
                map.put(matcher.group(1), matcher.group(2));
            }

        }
        return map;
    }

    // public static HashMap<String, String> getCH1(String key,String msg){
    // HashMap<String, String> map = new HashMap<String, String>();
    // String ch1Str = "";
    // String[] array = msg.split("CH1");
    // int length = array.length;
    // MyLog.v("splitStr", msg);
    // String splitStr = "";
    // for(int i = 0 ; i < length ; i++){
    // splitStr = array[i];
    // if(splitStr.contains(";[")){
    // splitStr = array[i].subSequence(0,
    // array[i].lastIndexOf(";[")).toString();
    // }
    // MyLog.v("splitStr", splitStr);
    // if(splitStr.contains("width")
    // && array[i].contains("height")
    // && array[i].contains("framerate")
    // && array[i].contains("nMBPH")){
    // break;
    // }
    // }
    //
    // map = genMsgMap(splitStr);
    //
    // return map;
    // }
    //
    /**
     * JSON 数组转成对象列表
     * 
     * @param msg
     * @return
     */
    public static ArrayList<Wifi> genWifiList(String msg) {
        ArrayList<Wifi> wifiList = new ArrayList<Wifi>();
        JSONArray array;
        try {
            array = new JSONArray(msg);
            if (null != array && 0 != array.length()) {
                int length = array.length();

                for (int i = 0; i < length; i++) {
                    Wifi wifi = new Wifi();
                    JSONObject obj = (JSONObject) array.get(i);
                    wifi.wifiAuth = obj.getString("auth");
                    wifi.wifiEnc = obj.getString("enc");
                    wifi.wifiKeyStat = obj.getInt("keystat");
                    wifi.wifiUserName = obj.getString("name");
                    wifi.wifiPassWord = obj.getString("pwd");
                    wifi.wifiQuality = obj.getInt("quality");
                    wifiList.add(wifi);
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return wifiList;
    }

    public static String getIpAddress(String host) {
        String IPAddress = "";
        InetAddress ReturnStr1 = null;
        try {
            ReturnStr1 = java.net.InetAddress.getByName(host);
            IPAddress = ReturnStr1.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        MyLog.v(TAG, "解析域名=" + host + ";IP=" + IPAddress);
        return IPAddress;
    }

    // 调用第三方分享功能
    public static void shareTo(Context context, String imagePath) {
        if (null == imagePath || "".equalsIgnoreCase(imagePath)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND); // 启动分享发送到属性
        // 纯文本
        // intent.setType("text/plain");
        // 图片分享
        intent.setType("image/jpg");
        File f = new File(imagePath);
        // 文件不存在return
        if (!f.exists()) {
            return;
        }
        Uri uri = Uri.fromFile(f);
        // Intent.createChooser(intentItem, "分享")
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.putExtra(Intent.EXTRA_SUBJECT, "分享的主题"); // 分享的主题
        intent.putExtra(Intent.EXTRA_TEXT, "分享的内容 "); // 分享的内容
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // 允许intent启动新的activity
        context.startActivity(Intent.createChooser(intent, "分享")); // //目标应用选择对话框的标题
    }

    public static String sendPostRequest(String url, List<NameValuePair> params) {
        String result = "";
        /* 建立HTTP Post连线 */
        HttpPost httpRequest = new HttpPost(url);
        // Post运作传送变数必须用NameValuePair[]阵列储存
        // 传参数 服务端获取的方法为request.getParameter("name")
        try {
            // 发出HTTP request
            httpRequest.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
            // 取得HTTP response
            HttpResponse httpResponse = new DefaultHttpClient()
                    .execute(httpRequest);

            // 若状态码为200 ok
            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                // 取出回应字串
                result = EntityUtils.toString(httpResponse.getEntity());
            } else {
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static int getInt(JSONObject object, String key) {
        int value = 0;
        if (!object.isNull(key)) {
            try {
                value = object.getInt(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static int optInt(JSONObject object, String key, int fallback) {
        int value = 0;
        if (!object.isNull(key)) {
            try {
                value = object.getInt(key);
                return value;
            } catch (JSONException e) {
                e.printStackTrace();
                return fallback;
            }
        } else {
            return fallback;
        }

    }

    public static String getString(JSONObject object, String key) {
        String value = "";
        if (!object.isNull(key)) {
            try {
                value = object.getString(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    public static boolean getBoolean(JSONObject object, String key) {
        boolean value = false;
        if (!object.isNull(key)) {
            try {
                value = object.getBoolean(key);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return value;
    }

    /**
     * 删除设备场景图
     * 
     * @param devName
     * @return
     */
    public static boolean deleteSceneFolder(String devName) {
        boolean deleteRes = false;
        String savePath = Consts.SCENE_PATH + devName + File.separator;
        File sceneFolder = new File(savePath);
        File[] files = sceneFolder.listFiles();
        // 移除内存中的图片
        if (null != files && 0 != files.length) {
            for (int i = 0; i < files.length; i++) {
                BitmapCache.getInstance().bitmapRefs.remove(files[i]
                        .getAbsolutePath());
            }
        }
        // 删掉文件夹
        deleteFile(sceneFolder);
        return deleteRes;
    }

    /**
     * 删除通道场景图
     * 
     * @param devName
     * @return
     */
    public static boolean deleteSceneFile(String devName, int channel) {
        boolean deleteRes = false;
        String savePath = Consts.SCENE_PATH + devName + File.separator;
        File sceneFolder = new File(savePath);
        if (sceneFolder.exists()) {// 有场景图文件夹
            File imgFile = new File(savePath + channel + Consts.IMAGE_JPG_KIND);
            if (imgFile.exists()) {
                imgFile.delete();
                deleteRes = true;
            }
        } else {
            deleteRes = true;
        }
        return deleteRes;
    }

    /**
     * @param dev
     * @return
     */
    public static String getImgPath(Device dev, boolean demo) {
        String imgFileKey = "";
        String filePath = "";
        if (demo) {
            filePath = Consts.SCENE_PATH + "demo_" + dev.getFullNo();
        } else {
            if (2 == dev.getIsDevice()) {
                filePath = Consts.SCENE_PATH + dev.getDoMain();
            } else {
                filePath = Consts.SCENE_PATH + dev.getFullNo();
            }
        }

        File[] files = new File(filePath).listFiles();
        if (null != files && 0 != files.length) {
            int random = (int) (Math.random() * files.length);
            imgFileKey = files[random].getAbsolutePath();
        }
        return imgFileKey;
    }

    /**
     * 删除文件（文件或文件夹均可删除）
     * 
     * @param file
     */
    public static void deleteFile(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }

        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }

            for (int i = 0; i < childFiles.length; i++) {
                deleteFile(childFiles[i]);
            }
            file.delete();
        }
    }

    // /**
    // * 获取当前ip地址
    // *
    // * @param context
    // * @return
    // */
    // public static String getLocalIpAddress(Context context) {
    // String ip = "";
    // try {
    // for (Enumeration<NetworkInterface> en = NetworkInterface
    // .getNetworkInterfaces(); en.hasMoreElements();) {
    // NetworkInterface intf = en.nextElement();
    // for (Enumeration<InetAddress> enumIpAddr = intf
    // .getInetAddresses(); enumIpAddr.hasMoreElements();) {
    // InetAddress inetAddress = enumIpAddr.nextElement();
    // if (!inetAddress.isLoopbackAddress()) {
    // ip = inetAddress.getHostAddress().toString();
    // }
    // }
    // }
    // } catch (Exception ex) {
    // ip = "";
    // ex.printStackTrace();
    // }
    // MyLog.v("LocalIpAddress", ip+"");
    // return ip;
    // }

    public static String convertToString(ArrayList<String> list) {

        StringBuilder sb = new StringBuilder();
        String delim = "";
        for (String s : list) {
            sb.append(delim);
            sb.append(s);
            ;
            delim = ",";
        }
        return sb.toString();
    }

    public static ArrayList<String> convertToArray(String string) {
        ArrayList<String> list = null;
        if (string.equals("") || null == string) {
            list = new ArrayList<String>();
        } else {
            list = new ArrayList<String>(Arrays.asList(string.split(",")));
        }

        return list;
    }

    public static String getSystemProperty(String propName) {
        String line;
        BufferedReader input = null;
        try {
            Process p = Runtime.getRuntime().exec("getprop " + propName);
            input = new BufferedReader(
                    new InputStreamReader(p.getInputStream()), 1024);
            line = input.readLine();
            input.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            MyLog.e(TAG, "Unable to read sysprop " + propName);
            return null;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Log.e(TAG, "Exception while closing InputStream", e);
                }
            }
        }
        return line;
    }

    public static HashMap<String, String> genMsgMapFromhpget(String msg) {
        HashMap<String, String> map = new HashMap<String, String>();

        if (null == msg || "".equalsIgnoreCase(msg)) {
            return null;
        }
        Matcher matcher = Pattern.compile("([^=&]+)=([^=&]+)").matcher(msg);
        while (matcher.find()) {
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    /**
     * 获取session
     * 
     * @param context
     * @return
     */
    public static String getSession() {
        String sessionResult = JVACCOUNT.GetSession();
        MyLog.v("session", sessionResult);
        return sessionResult;
    }

    /**
     * 获取视频广场需要拼接的url后缀,6个参数
     * 
     * @param mContext
     * @return
     */
    public static String getDemoParamsStr(Context mContext) {
        String str = "";

        String appVersion = "";
        try {
            appVersion = mContext.getPackageManager().getPackageInfo(
                    mContext.getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        String lan = "";
        if (Consts.LANGUAGE_ZH == ConfigUtil.getLanguage2(mContext)) {
            lan = "zh_cn";
        } else if (Consts.LANGUAGE_ZHTW == ConfigUtil.getLanguage2(mContext)) {
            lan = "zh_tw";
        } else {
            lan = "en_us";
        }

        String sid = "";
        if (!Boolean.valueOf(((BaseActivity) mContext).statusHashMap
                .get(Consts.LOCAL_LOGIN))) {// 在线
            sid = JVACCOUNT.GetSession();
        } else {
            sid = "";
        }
        str = "?" + "plat=android&platv=" + Build.VERSION.SDK_INT + "&lang="
                + lan + "&appv=" + appVersion + "&d="
                + System.currentTimeMillis() + "&sid=" + sid;

        return str;
    }

    /**
     * 2015-04-20 闫帅 获取社区论坛需要拼接的url后缀,1个参数 sid
     * 
     * @param mContext
     * @return
     */
    public static String getBbsParamsStr(Context mContext) {
        String str = "";
        String sid = "";
        if (!Boolean.valueOf(((BaseActivity) mContext).statusHashMap
                .get(Consts.LOCAL_LOGIN))) {// 在线
            sid = JVACCOUNT.GetSession();
        } else {
            sid = "";
        }
        str = "&sid=" + sid;

        return str;
    }

    /**
     * 2015-04-20 闫帅 获取论坛条数需要拼接的url后缀,3个参数 sid
     * 
     * @param mContext
     * @return
     */
    public static String getBbsNumParamsStr(String bbsNum, Context mContext) {
        String str = "";
        String[] array = bbsNum.split("mod");
        if (!Boolean.valueOf(((BaseActivity) mContext).statusHashMap
                .get(Consts.LOCAL_LOGIN))) {
            str = array[0] + "mod=api&act=user_pm&sid="
                    + JVACCOUNT.GetSession();
        } else {
            str = array[0] + "mod=api&act=user_pm";
        }

        return str;
    }

    /**
     * 2015-04-20 闫帅 获取云服务开通和小维商城需要拼接的url后缀,1个参数 sid
     * 
     * @param mContext
     * @return
     */
    public static String getCloudShopParamsStr(String cloudurl, Context mContext) {
        String sid = "";
        if (!Boolean.valueOf(((BaseActivity) mContext).statusHashMap
                .get(Consts.LOCAL_LOGIN))) {// 在线
            sid = JVACCOUNT.GetSession();
        } else {
            sid = "";
        }
        if (cloudurl.contains("?")) {
            cloudurl = cloudurl + "&sid=" + sid;
        } else {
            cloudurl = cloudurl + "?sid=" + sid;
        }

        return cloudurl;
    }

    /**
     * 2015.6.9 获取简短title
     * 
     * @param title
     * @return
     */
    public static String getShortTile(String title) {
        if (null == title || "".equalsIgnoreCase(title)) {
            return "";
        }
        String shortTitle = "";
        int length = title.length();
        if (length > 8) {
            shortTitle = title.substring(0, 4) + "...";
        } else {
            shortTitle = title;
        }
        return shortTitle;
    }
}
