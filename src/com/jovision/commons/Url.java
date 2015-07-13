
package com.jovision.commons;

import com.jovision.Consts;

public class Url {
    // 意见反馈地址
    public static String FEED_BACK_URL = "http://182.92.242.230/api.php";

    // 获取地理位置url
    public static String COUNTRY_URL = "http://int.dpool.sina.com.cn/iplookup/iplookup.php?format=js";
    // http://int.dpool.sina.com.cn/iplookup/iplookup.php

    // 找回密码
    public static String RESET_PWD_URL_CHINA_ZH = "http://webapp.afdvr.com:9006/findpwd?lang=0";// 重置密码地址
    public static String RESET_PWD_URL_FOREIGN_ZH = "http://webappen.afdvr.com:9006/findpwd?lang=0";// 重置密码地址
    public static String RESET_PWD_URL_CHINA_EN = "http://webapp.afdvr.com:9006/findpwd?lang=1";// 重置密码地址国内服务器不带en
    public static String RESET_PWD_URL_FOREIGN_EN = "http://webappen.afdvr.com:9006/findpwd?lang=1";// 重置密码地址国外服务器带en
    public static String RESET_PWD_URL_FOREIGN_ZHT = "http://webappen.afdvr.com:9006/findpwd?lang=2";// 重置密码地址
    public static String RESET_PWD_URL_CHINA_ZHT = "http://webapp.afdvr.com:9006/findpwd?lang=2";// 重置密码地址
    // public static String RESET_PWD_URL =
    // "http://webapp.afdvr.com:9003/findpwd/index.html";// 重置密码地址
    // public static String RESET_PWD_URL_EN =
    // "http://webappen.afdvr.com:9003/findpwd/index.html";// 重置密码地址

    // 国内url：http://webapp.afdvr.com:9006/findpwd?lang=0
    // 国外url：http://webappen.afdvr.com:9006/findpwd?lang=1
    // 国外url：http://webappen.afdvr.com:9006/findpwd?lang=2 繁体

    public static String JOVISION_PUBLIC_API = "http://webapp.yoosee.cc/api/genstreamurl?";

    // 检查软件更新地址
    public static String CHECK_UPDATE_URL = "http://wmap.yoosee.cc/MobileWeb.aspx";// 检查软件更新地址
    public static String APK_DOWNLOAD_URL = "http://wmap.yoosee.cc/";// 下载更新软件地址

    // public static String SHORTSERVERIP = "98.126.77.202";
    // public static String LONGSERVERIP = "98.126.77.202";

    // public static String SHORTSERVERIP = "58.56.19.187";
    // public static String LONGSERVERIP = "58.56.19.187";

    // public static String SHORTSERVERIP = Consts.FOREIGN_SERVER ?
    // "98.126.77.202"
    // : Consts.TEST_SERVER ? "58.56.19.187"
    // : ConfigUtil.getServerLanguage() == Consts.LANGUAGE_ZH ?
    // "appchannel.afdvr.com"
    // : "appchannelen.afdvr.com"; // "appchannel.jovecloud.com";
    // public static String LONGSERVERIP = Consts.FOREIGN_SERVER ?
    // "98.126.77.202"
    // : Consts.TEST_SERVER ? "58.56.19.187"
    // : ConfigUtil.getServerLanguage() == Consts.LANGUAGE_ZH ?
    // "apponline.afdvr.com"
    // : "apponlineen.afdvr.com"; // "apponline.jovecloud.com";

    // if (MySharedPreference
    // .getBoolean(Consts.MORE_FOREIGNSWITCH)) {
    // 测试开关地址
    // public static String TESTSWITCH_TRUE_URL =
    // "http://60.216.75.26:9999/jenkins/job/IPC/ws/cluster_test.txt";//
    // public static String TESTSWITCH_FALSE_URL =
    // "http://60.216.75.26:9999/jenkins/job/IPC/ws/cluster.txt";//
    // public static String FOREIGN_URL =
    // "http://60.216.75.26:9999/jenkins/job/IPC/ws/cluster_guowai.txt";
    // public static String BASIC_URL =
    // "http://www.jovetech.com/down/YST/cluster";
    // public static String ACCOUNT_URL = BASIC_URL
    // + (Consts.FOREIGN_SERVER ? "_guowai.txt"
    // : Consts.TEST_SERVER ? "_test.txt" : ".txt");

    public static String ACCOUNT_NORMAL_URL = "http://www.jovetech.com/down/YST/cluster.txt";
    public static String ACCOUNT_TEST_URL = "http://60.216.75.26:9999/jenkins/job/IPC/ws/cluster_test.txt";
    public static String ACCOUNT_FOREIGN_URL = "http://60.216.75.26:9999/jenkins/job/IPC/ws/cluster_guowai.txt";

    public static String ACCOUNT_URL = Consts.FOREIGN_SERVER ? ACCOUNT_FOREIGN_URL
            : Consts.TEST_SERVER ? ACCOUNT_TEST_URL : ACCOUNT_NORMAL_URL;

}
