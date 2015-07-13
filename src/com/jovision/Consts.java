
package com.jovision;

import android.os.Environment;

import com.jovision.views.AlarmDialog;

import java.io.File;
import java.util.ArrayList;

public class Consts {

    /**************************** 中性软件修改变量 ******************************/
    // 软件名
    public static final String APP_NAME = "TEMSEE";
    // (终端类型 0-未知 1-Android 2-iPhone 3-iPad)
    public static final int TERMINAL_TYPE = 1;// 固定值，无需NEW_PUSH_MSG_TAG修改
    // 产品类型 0-CloudSEE 1-NVSIP 2-HITVIS 3-TONGFANG
    public static final int PRODUCT_TYPE = 50;// 推送定的值
    // app 检查更新 key
    public static final int APP_UPDATE_VERSION = 50;// 中维版本值为1，oem版为2，nvsip版本为3
    // 添加设备默认的用户名
    public static final String DEFAULT_USERNAME = "admin";// 中性软件改为admin
    // 添加设备默认的密码
    public static final String DEFAULT_PASSWORD = "";// 中性软件改为空
    // 添加设备默认的端口
    public static final String DEFAULT_PORT = "9101";// 固定值，无需修改
    // 测试服务器
    public static boolean TEST_SERVER = false;// true 测试服务器， false 正式服务器

    // 国外服务器
    public static boolean FOREIGN_SERVER = false;// true 国外服务器， false 国内服务器

    /**************************** 华丽的分割线 ******************************/
    public static int CURRENT_LAN = -1;// 当前语言
    public static int COUNT = -1;
    public static final String CONNECTIVITY_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";

    public static final boolean SMART_CONN_ENABLED = false;// 智连路由功能是否可用标志

    /** 老数据库数据 **/
    public static final String JVCONFIG_DATABASE = "JVConfigTemp.db";
    public static final int JVCONFIG_DB_VER = 2;

    public static final String SD_CARD_PATH = Environment
            .getExternalStorageDirectory().getPath() + File.separator;

    public static final String LOG_PATH = SD_CARD_PATH + APP_NAME;
    public static final String ACCOUNT_PATH = SD_CARD_PATH + "ACCOUNT";
    public static final String CAPTURE_PATH = Consts.SD_CARD_PATH + APP_NAME
            + File.separator + "capture" + File.separator;
    public static final String VIDEO_PATH = Consts.SD_CARD_PATH + APP_NAME
            + File.separator + "video" + File.separator;
    public static final String DOWNLOAD_VIDEO_PATH = Consts.SD_CARD_PATH
            + APP_NAME + File.separator + "downvideo" + File.separator;
    public static final String SOFTWARE_PATH = Consts.SD_CARD_PATH + APP_NAME
            + File.separator + "software" + File.separator;
    public static final String BUG_PATH = Consts.SD_CARD_PATH + APP_NAME
            + File.separator + "bugs" + File.separator;
    public static final String HEAD_PATH = Consts.SD_CARD_PATH + APP_NAME
            + File.separator + "head" + File.separator;
    public static final String AD_PATH = Consts.SD_CARD_PATH + APP_NAME
            + File.separator + "ad" + File.separator;
    public static final String WELCOME_IMG_PATH = Consts.SD_CARD_PATH
            + APP_NAME + File.separator + "welcome" + File.separator;
    public static final String SCENE_PATH = Consts.SD_CARD_PATH + APP_NAME
            + File.separator + "scene" + File.separator;
    public static final String BBSIMG_PATH = Consts.SD_CARD_PATH + APP_NAME
            + File.separator + "bbsimage" + File.separator;
    public static final String DB_PATH = Consts.SD_CARD_PATH + APP_NAME
            + File.separator + "db" + File.separator;

    public static final String DB_NAME = "CloudSEE.db";

    public static final String TAG_APP = "CS_APP";
    public static final String TAG_UI = "CS_UI";
    public static final String TAG_PLAY = "CS_PLAY";
    public static final String TAG_LOGICAL = "CS_LOGICAL";
    public static final String TAG_JY = "CS_JY";
    public static final String TAG_XXX = "NEO_XXX";

    public static final int CALL_CONNECT_CHANGE = 0xA1;
    public static final int CALL_NORMAL_DATA = 0xA2;
    public static final int CALL_CHECK_RESULT = 0xA3;
    public static final int CALL_CHAT_DATA = 0xA4;
    public static final int CALL_TEXT_DATA = 0xA5;
    public static final int CALL_DOWNLOAD = 0xA6;
    public static final int CALL_PLAY_DATA = 0xA7;
    public static final int CALL_LAN_SEARCH = 0xA8;
    public static final int CALL_NEW_PICTURE = 0xA9;
    public static final int CALL_STAT_REPORT = 0xAA;
    // public static final int CALL_GOT_SCREENSHOT = 0xAB;
    public static final int CALL_PLAY_DOOMED = 0xAC;
    public static final int CALL_PLAY_AUDIO = 0xAD;
    public static final int CALL_QUERY_DEVICE = 0xAE;// 纯局域网广播回调
    public static final int CALL_HDEC_TYPE = 0xAF;
    public static final int CALL_LIB_UNLOAD = 0xB0;
    public static final int CALL_GEN_VOICE = 0xB1;
    public static final int CALL_PLAY_BUFFER = 0xB2;

    public static final int BUFFER_START = -1;
    public static final int BUFFER_FINISH = -2;

    public static final int RTMP_CONN_SCCUESS = (0x01 | 0xA0);
    public static final int RTMP_CONN_FAILED = (0x02 | 0xA0);
    public static final int RTMP_DISCONNECTED = (0x03 | 0xA0);
    public static final int RTMP_EDISCONNECT = (0x04 | 0xA0);

    public static final int MAX_DEVICE_CHANNEL_COUNT = 64;
    public static final int DEFAULT_ADD_CHANNEL_COUNT = 4;

    public static final int TYPE_GET_PARAM = 0x02;
    public static final int TYPE_SET_PARAM = 0x03;

    public static final int TYPE_EX_UPDATE = 0x01;
    public static final int TYPE_EX_SENSOR = 0x02;
    public static final int TYPE_EX_STORAGE_SWITCH = 0x07;
    public static final int TYPE_EX_SET_DHCP = 0x09;

    public static final int COUNT_EX_UPDATE = 0x01;
    public static final int COUNT_EX_NETWORK = 0x02;
    public static final int COUNT_EX_STORAGE = 0x03;
    public static final int COUNT_EX_SENSOR = 0x08;

    /** 2015-06-03 解决突尼斯客户连不上视频的问题 ***/
    public static final int MTU_700 = 700;
    public static final int MTU_1400 = 1400;// 路由器可以设置的最大值是1472,

    public static final String FORMATTER_FLASH_SWITCH = "FlashMode=%d;";
    public static final String FORMATTER_MOTION_DETECTION_SWITCH = "bMDEnable=%d;";
    public static final String FORMATTER_PN_SWITCH = "PNMode=%d";
    public static final String FORMATTER_AP_SWITCH = "apModeOn=%d";

    public static final String FORMATTER_STORAGE_MODE = "storageMode=%d";
    public static final String FORMATTER_TALK_SWITCH = "talkSwitch=%d";
    public static final String FORMATTER_SET_WIFI = "ACTIVED=%d;WIFI_ID=%s;WIFI_PW=%s;";
    public static final String FORMATTER_SAVE_WIFI = "ACTIVED=%d;WIFI_ID=%s;WIFI_PW=%s;WIFI_AUTH=%s;WIFI_ENC=%s;";
    public static final String FORMATTER_SET_DHCP = "ACTIVED=%d;bDHCP=%d;nlIP=%d;nlNM=%d;nlGW=%d;nlDNS=%d;";
    public static final String FORMATTER_SET_BPS_FPS = "[CH%d];width=%d;height=%d;nMBPH=%d;framerate=%d;rcMode=%d;";
    public static final String FORMATTER_SET_MDENABLE = "bMDEnable=%d;";
    public static final String FORMATTER_SET_ALARM_TIME = "alarmTime0=%s;";
    public static final String FORMATTER_SET_ALARM_ONLY = "bAlarmEnable=%d;";
    public static final String FORMATTER_SET_ALARM_SOUND = "bAlarmSound=%d;";
    public static final String FORMATTER_CLOUD_DEV = "CLOUD_%s_%d";

    public static final String FORMATTER_ALARM_EMAIL_SET = "alarmTime1=%s-%s;"// 报警时间段
            + "nAlarmDelay=%d;"// 报警间隔
            + "bAlarmSound=%d;"// 报警声音开关
            + "acMailSender=%s;"// 邮件发送者
            + "acSMTPServer=%s;"// 邮件服务器地址
            + "acSMTPUser=%s;"// 用户名
            + "acSMTPPasswd=%s;"// 密码
            + "vmsServerIp=%s;"// vms服务器IP地址
            + "vmsServerPort=%d;"// vms服务器端口
            + "acSMTPort=%d;"// 发送邮件端口
            + "acSMTPCrypto=%s;"// 邮件加密方式（none/ssl/tls）
            + "acReceiver0=%s;"// 收件人1
            + "acReceiver1=%s;"// 收件人2
            + "acReceicer2=%s;"// 收件人3
            + "acReciever3=%s;";// 收件人4

    public static final String FORMATTER_ALARM_SEND_TEST_EMAIL = "acMailSender=%s;"// 邮件发送者
            + "acSMTPServer=%s;"// 邮件服务器地址
            + "acSMTPUser=%s;"// 用户名
            + "acSMTPPasswd=%s;"// 密码
            + "acSMTPort=%d;"// 发送邮件端口
            + "acSMTPCrypto=%s;"// 邮件加密方式（none/ssl/tls）
            + "acReceiver0=%s;"// 收件人1
            + "acReceiver1=%s;"// 收件人2
            + "acReceicer2=%s;"// 收件人3
            + "acReciever3=%s;";// 收件人4

    public static final String BABY_ALARM_PARAM = "baby_refdbfs=%d;"// 参考dbfs 1
                                                                    // ： -24 ， 2
                                                                    // ： -48
            + "baby_ratio=%d;"// 最大占比 1 ： 48， 2 ： 24
            + "baby_timeout=%d;";// 持续时长 1:10 2:25

    // “baby_refdbfs=%d;” 参考dbfs 1 ： -24 ， 2 ： -48
    //
    // “baby_ratio = %d;” 最大占比 1 ： 48， 2 ： 24
    //
    // “baby_timeout=%d;” 持续时长 1:10 2:25

    public static int pushHisCount = 0;
    public static final int PUSH_PAGESIZE = 5;

    public static final int DEVICE_TYPE_UNKOWN = -1;
    public static final int DEVICE_TYPE_DVR = 0x01;
    public static final int DEVICE_TYPE_950 = 0x02;
    public static final int DEVICE_TYPE_951 = 0x03;
    public static final int DEVICE_TYPE_IPC = 0x04;
    public static final int DEVICE_TYPE_NVR = 0x05;

    public static final int JAE_ENCODER_SAMR = 0x00;
    public static final int JAE_ENCODER_ALAW = 0x01;
    public static final int JAE_ENCODER_ULAW = 0x02;
    public static final int JAE_ENCODER_G729 = 0x03;

    public static final int ENC_PCM_SIZE = 320;
    public static final int ENC_AMR_SIZE = 640;
    public static final int ENC_G711_SIZE = 640;
    public static final int ENC_G729_SIZE = 960;

    public static final int TEXT_REMOTE_CONFIG = 0x01;
    public static final int TEXT_AP = 0x02;
    public static final int TEXT_GET_STREAM = 0x03;

    public static final int FLAG_WIFI_CONFIG = 0x01;
    public static final int FLAG_WIFI_AP = 0x02;
    public static final int FLAG_BPS_CONFIG = 0x03;
    public static final int FLAG_CONFIG_SCCUESS = 0x04;
    public static final int FLAG_CONFIG_FAILED = 0x05;
    public static final int FLAG_CONFIG_ING = 0x06;
    public static final int FLAG_SET_PARAM = 0x07;
    public static final int FLAG_GPIN_ADD = 0x10;
    public static final int FLAG_GPIN_SET = 0x11;
    public static final int FLAG_GPIN_SELECT = 0x12;
    public static final int FLAG_GPIN_DEL = 0x13;

    public static final int FLAG_CAPTURE_FLASH = 0x10;
    public static final int FLAG_GET_MD_STATE = 0x11;
    public static final int FLAG_SET_MD_STATE = 0x12;

    public static final int FLAG_GET_PWD = 0x14;

    public static final int FLAG_GET_PARAM = 0x50;
    public static final int FLAG_SET_PARAM_OK = 0x55;

    public static final int EX_WIFI_CONFIG = 0x0A;

    public static final int RESULT_SUCCESS = 0x01;
    public static final int RESULT_NO_FILENAME = 0x10;
    public static final int RESULT_OPEN_FAILED = 0x11;
    public static final int RESULT_NO_DATA = 0x12;

    public static final int ARG1_PLAY_BAD = 0x01;

    public static final int DOWNLOAD_REQUEST = 0x20;
    public static final int DOWNLOAD_START = 0x21;
    public static final int DOWNLOAD_FINISHED = 0x22;
    public static final int DOWNLOAD_ERROR = 0x23;
    public static final int DOWNLOAD_STOP = 0x24;
    public static final int DOWNLOAD_TIMEOUT = 0x76;

    public static final int BAD_STATUS_NOOP = 0x00;
    public static final int BAD_STATUS_DECODE = 0x01;
    public static final int BAD_STATUS_OPENGL = 0x02;
    public static final int BAD_STATUS_AUDIO = 0x03;
    public static final int BAD_STATUS_OMX = 0x04;
    public static final int BAD_STATUS_FFMPEG = 0x05;
    public static final int BAD_STATUS_LEGACY = 0x06;
    public static final int PLAYBACK_DONE = 0x10;
    public static final int VIDEO_SIZE_CHANGED = 0x11;

    public static final int BAD_SCREENSHOT_NOOP = 0x00;//
    public static final int BAD_SCREENSHOT_INIT = 0x01;//
    public static final int BAD_SCREENSHOT_CONV = 0x02;//
    public static final int BAD_SCREENSHOT_OPEN = 0x03;//

    public static final int BAD_HAS_CONNECTED = -1;
    public static final int BAD_CONN_OVERFLOW = -2;
    public static final int BAD_NOT_CONNECT = -3;
    public static final int BAD_ARRAY_OVERFLOW = -4;
    public static final int BAD_CONN_UNKOWN = -5;

    public static final int ARG2_REMOTE_PLAY_OVER = 0x32;
    public static final int ARG2_REMOTE_PLAY_ERROR = 0x39;
    public static final int ARG2_REMOTE_PLAY_TIMEOUT = 0x77;

    public static final int WHAT_DUMMY = 0x04;

    /******************************** 添加设备类型 1为wifi模式，2为声波，3为云视通号 ***********************************/
    public static final int NET_DEVICE_TYPE_YST_NUMBER = 3;// 云视通号添加设备
    public static final int NET_DEVICE_TYPE_SOUND_WAVE = 2;// 声波配置
    public static final int NET_DEVICE_TYPE_AP_SET = 1;// AP配置

    /********************************* 　以下修改设备用户名密码需要的宏定义　 ***************************************/
    public static final int SECRET_KEY = 0x1053564A;
    public static final int MAX_ACCOUNT = 13;
    public static final int SIZE_ID = 20;
    public static final int SIZE_PW = 20;
    public static final int SIZE_DESCRIPT = 32;

    // 用户组定义
    public static final int POWER_GUEST = 0x0001;
    public static final int POWER_USER = 0x0002;
    public static final int POWER_ADMIN = 0x0004;
    public static final int POWER_FIXED = 0x0010;

    // 帐户管理指令,lck20120308
    public static final int EX_ACCOUNT_OK = 0x01;
    public static final int EX_ACCOUNT_ERR = 0x02;
    public static final int EX_ACCOUNT_REFRESH = 0x03;
    public static final int EX_ACCOUNT_ADD = 0x04;
    public static final int EX_ACCOUNT_DEL = 0x05;
    public static final int EX_ACCOUNT_MODIFY = 0x06;

    // 操作状态
    public static final int ERR_OK = 0x0; // 修改成功 
    public static final int ERR_EXISTED = 0x1; // 用户已存在 
    public static final int ERR_LIMITED = 0x2; // 用户太多，超出了限制 
    public static final int ERR_NOTEXIST = 0x3; // 指定的用户不存在
    public static final int ERR_PASSWD = 0x4; // 密码错误
    public static final int ERR_PERMISION_DENIED = 0x5;// 无权限

    /********************************* 　以上修改设备用户名密码需要的宏定义　 ***************************************/

    /********************************* 　以下修改设备用户名密码需要的宏定义　 ***************************************/

    // 扩展类型，用于指定哪个模块去处理,lck20120206
    public static final int RC_EX_FIRMUP = 0x01;
    public static final int RC_EX_NETWORK = 0x02;
    public static final int RC_EX_STORAGE = 0x03;
    public static final int RC_EX_ACCOUNT = 0x04;
    public static final int RC_EX_COVERRGN = 0x05;
    public static final int RC_EX_MDRGN = 0X06;
    public static final int RC_EX_ALARM = 0x07;
    public static final int RC_EX_SENSOR = 0x08;
    public static final int RC_EX_PTZ = 0x09;
    public static final int RC_EX_AUDIO = 0x0a;
    public static final int RC_EX_ALARMIN = 0x0b;
    public static final int RC_EX_REGISTER = 0x0c;
    public static final int RC_EX_ROI = 0x0d;
    public static final int RC_EX_QRCODE = 0x0e;
    public static final int RC_EX_IVP = 0x0f;

    // 升级过程中用到的宏定义：
    // 系统升级指令,lck20120207
    public static final int EX_UPLOAD_START = 0x01;
    public static final int EX_UPLOAD_CANCEL = 0x02;
    public static final int EX_UPLOAD_OK = 0x03;
    public static final int EX_UPLOAD_DATA = 0x04;
    public static final int EX_FIRMUP_START = 0x05;
    public static final int EX_FIRMUP_STEP = 0x06;
    public static final int EX_FIRMUP_OK = 0x07;
    public static final int EX_FIRMUP_RET = 0x08;
    public static final int EX_FIRMUP_REBOOT = 0xA0;
    public static final int EX_FIRMUP_RESTORE = 0xA1;
    public static final int EX_FIRMUP_UPDINFO_REQ = 0x11; // 获取升级所需信息
    public static final int EX_FIRMUP_UPDINFO_RESP = 0x21;

    // //EX_FIRMUP_UPDINFO_REQ
    // typedef struct{
    // char product[32]; //产品类别
    // char url[4][128]; //升级服务器地址
    // int urlCnt;
    // char binName[32]; //升级文件
    // char verName[32]; //升级版本文件
    // }FirmupInfo_t;

    // 升级结果定义
    public static final int FIRMUP_SUCCESS = 0x01;
    public static final int FIRMUP_FAILED = 0x02;
    public static final int FIRMUP_LATEST = 0x03;
    public static final int FIRMUP_INVALID = 0x04;
    public static final int FIRMUP_ERROR = 0x05;
    public static final int FIRMUP_NOTFIT = 0x06;

    // 升级方法
    public static final int FIRMUP_HTTP = 0x00;
    public static final int FIRMUP_FILE = 0x01;
    public static final int FIRMUP_FTP = 0x02;// 已废弃
    /******************************** 更多功能定义 **********************************************/
    public static final String MORE_HELP = "HELP"; // 帮助图片显示
    public static final String MORE_PAGEONE = "page1"; // 我的设备界面帮助图
    public static final String MORE_PAGETWO = "page2"; // 配置界面帮助图
    public static final String MORE_REMEMBER = "REMEMBER"; // 自动登录功能
    public static final String MORE_ALARMSWITCH = "AlarmSwitch"; // 报警通知开关
    public static final String MORE_PLAYMODE = "PlayDeviceMode";// 观看模式（单设备，多设备）
    public static final String MORE_DEVICE_SCENESWITCH = "DeviceScene"; // 场景图开关
    public static final String MORE_LITTLEHELP = "LITTLEHELP"; // 小助手
    public static final String MORE_BROADCAST = "BROADCASTSHOW"; // 广播
    public static final String MORE_LITTLE = "LITTLE"; // 关于
    public static final String MORE_SYSTEMMESSAGE = "SystemMessage"; // 系统消息
    public static final String MORE_DEMOURL = "DEMOURL"; // 视频广场
    public static final String ALARM_SETTING_SOUND = "AlarmSound"; // 报警声音开关
    public static final String ALARM_SETTING_VIBRATE = "AlarmVibrate";// 报警振动开关
    // public static final String MORE_CUSTURL = "CUSTURL"; // 我要装监控
    // public static final String MORE_STATURL = "STATURL"; // 云视通指数
    public static final String MORE_BBS = "BBSURL"; // 论坛
    public static final String MORE_GCSURL = "GCSURL"; // 工程商入住
    public static final String MORE_BBSNUM = "BBSNUM"; // 论坛未读消息数量
    public static final int MORE_BBSNUMNOTY = 0x999991; // 论坛未读消息数量
    public static final String MORE_BBSNUMURL = "BBSNUMURL"; // 论坛未读消息数量
    public static final String MORE_SHOPURL = "SHOPURL"; // 小维商城
    public static final String MORE_KNOWLEDGEURL = "KNOWLEDGEURL"; // 小维知道
    public static final String MORE_ADDDEVICEURL = "DBURL";
    public static final String MORE_ADDDEVICESWITCH = "DB_SWITCH";
    public static final String MORE_SHAREURL = "SHAREURL";// 视频分享到广场

    public static final String MORE_DEMO_SWITCH = "DEMO_SWITCH"; // 视频广场开关
    public static final String MORE_CUST_SWITCH = "CUST_SWITCH"; // 我要装监控开关
    public static final String MORE_STAT_SWITCH = "STAT_SWITCH"; // 云视通指数开关
    public static final String MORE_BBS_SWITCH = "BBS_SWITCH"; // 论坛开关
    public static final String MORE_GCS_SWITCH = "GCS_SWITCH"; // 工程商入住开关
    public static final String MORE_SHOP_SWITCH = "SHOP_SWITCH"; // 小维商城开关
    public static final String MORE_CLOUD_SWITCH = "CLOUD_SWITCH"; // 小维商城开关
    public static final String MORE_SHARE_SWITCH = "SHARE_SWITCH"; // 视频分享到广场开关

    public static final String MORE_TESTSWITCH = "TESTSWITCH"; // 测试服务器开关
    public static final String MORE_FOREIGNSWITCH = "FOREIGNSWITCH"; // 国外服务器开关
    public static final String MORE_ALARMMSG = "AlarmMsg"; // 报警信息
    public static final String MORE_VERSION = "Version"; // 版本
    public static final String MORE_DEVICESHARE = "DEVICE_SHARE"; // 设备分享
    public static final String MORE_SHOWMEDIA = "SHOW_MEDIA"; // 察看图像
    public static final String MORE_FEEDBACK = "FEED_BACK"; // 反馈
    public static final String MORE_UPDATE = "UPDATE"; // 更新

    public static final String PROFILE_NOTE_SWITCH = "NOTE_SWITCH"; // 个人中心帖子开关
    public static final String PROFILE_NOTELISTURL = "NOTELISTURL"; // 个人中心帖子列表url
    public static final String PROFILE_NOTEDETAILURL = "NOTEDETAILURL"; // 个人中心帖子详情url

    // 云服务开通(商城)
    public static final String MORE_CLOUD_SHOP = "CLOUD_SHOP"; // 云视通服务开通

    /** 更多 list菜单每个item对应的属性标志 **/
    public static final String[] moreListItemFlag = new String[] {
            MORE_HELP, /*
                        * 0 帮助
                        */
            MORE_REMEMBER, /* 1 自动登陆 */
            MORE_ALARMSWITCH, /* 2 警告信息推送 */
            MORE_ALARMMSG, /* 3报警信息 */
            MORE_PLAYMODE, /* 4 观看模式 */
            MORE_DEVICESHARE, /* 5设备分享 */
            MORE_DEVICE_SCENESWITCH,/* 6 设备场景图 */
            MORE_CLOUD_SHOP,/* 7云服务开通 */
            // MORE_STATURL, /* 8 云视通指数 */
            MORE_GCSURL, /* 9 工程商入住 */
            MORE_BBS, /* 10进入社区 */
            MORE_SYSTEMMESSAGE, /* 11 系统消息 */
            MORE_SHOWMEDIA, /* 12 图像察看 */
            MORE_FEEDBACK, /* 13 反馈 */
            MORE_UPDATE, /* 14 检查更新 */
            MORE_LITTLE, /* 15关于 */
            MORE_LITTLEHELP, /* 16 小助手 */
            MORE_BROADCAST, /* 17广播 */
            MORE_TESTSWITCH, /* 18 测试开关 */
            MORE_FOREIGNSWITCH, /* 19 国外服务器开关 */
            MORE_VERSION
            /* 20 版本 */
    };

    /********************************* 　以上修改设备用户名密码需要的宏定义　 ***************************************/

    public static final String IPC_FLAG = "IPC-";
    public static final String IPC_TAG = "IPC-H-";
    public static final String IPC_DEFAULT_USER = "jwifiApuser";
    public static final String IPC_DEFAULT_PWD = "^!^@#&1a**U";
    public static final String IPC_DEFAULT_IP = "10.10.0.1";
    public static final int IPC_DEFAULT_PORT = 9101;

    // 张帅服务端0：中文，1：英文，2：繁体
    public static final int LANGUAGE_ZH = 1;// 中文
    public static final int LANGUAGE_EN = 2;// 英文
    public static final int LANGUAGE_ZHTW = 3;// 繁体

    /** 播放tag */
    public static final int PLAY_NORMAL = 0x01;
    public static final int PLAY_DEMO = 0x02;
    public static final int PLAY_AP = 0x03;

    /** 播放key */
    public static final String KEY_PLAY_NORMAL = "PLAY_NORMAL";
    public static final String KEY_PLAY_DEMO = "PLAY_DEMO";
    public static final String KEY_PLAY_AP = "PLAY_AP";

    /** 是否为慧通设备 */
    public static int ISHITVIS = 0; // 0.非慧通，1.慧通
    public static int FLIGHT_FLAG = 2;// 闪光灯状态 0.自动 1.开启 2.关闭
    public static boolean MOTION_DETECTION_FLAG = false;// 移动侦测状态 1.开 0.关
    /** 登录方法 */
    public static final int LOGIN_FUNCTION = 0xB1;
    /** 注册方法 */
    public static final int REGIST_FUNCTION = 0xB2;
    /** 获取设备列表方法 */
    public static final int GET_DEVICE_LIST_FUNCTION = 0xB3;
    /** 获取设备列表方法 */
    public static final int GUID_FUNCTION = 0xB4;
    // JVOfflineActivity
    public static final int OFFLINE_COUNTS = 15;// 15秒倒计时
    public static final int TAG_BROAD_DEVICE_LIST = 0x05;// 广播设备列表
    public static final int TAG_BROAD_ADD_DEVICE = 0x06;// 添加设备的广播
    public static final int TAG_BROAD_THREE_MINITE = 0x07;// 三分钟广播
    // 视频播放状态
    public static final int TAG_PLAY_CONNECTING = 1;// 连接中
    public static final int TAG_PLAY_CONNECTTED = 2;// 已连接
    public static final int TAG_PLAY_DIS_CONNECTTED = 3;// 断开
    public static final int TAG_PLAY_CONNECTING_BUFFER = 4;// 连接成功，正在缓冲数据。。。
    public static final int TAG_PLAY_STATUS_UNKNOWN = 5;// 未知状态
    public static final int TAG_PLAY_BUFFERING = 6;// 已连接，正在缓冲进度
    public static final int TAG_PLAY_BUFFERED = 7;// 已连接，缓冲成功
    public static final int ARG2_STATUS_CONNECTING = 0x01;
    public static final int ARG2_STATUS_CONNECTED = 0x02;
    public static final int ARG2_STATUS_BUFFERING = 0x03;
    public static final int ARG2_STATUS_DISCONNECTED = 0x04;
    public static final int ARG2_STATUS_HAS_CONNECTED = 0x05;
    public static final int ARG2_STATUS_CONN_OVERFLOW = 0x06;
    public static final int ARG2_STATUS_UNKNOWN = 0x07;
    public static final int STOP_AUDIO_GATHER = 0x08;
    public static final int START_AUDIO_GATHER = 0x09;

    /** 账号错误 tag */
    public static final int TAG_ACCOUNT_KEEP_ONLINE_FAILED = 1;// 连续三次保持在线失败
    public static final int TAG_ACCOUNT_OFFLINE = 2;// 提掉线
    public static final int TAG_ACCOUNT_TCP_ERROR = 3;// 提掉线

    /** AP配置声音流程提示音 */
    public static final int TAG_SOUNDONE = 1;// 请选择要配置设备 声音
    public static final int TAG_SOUNDTOW = 2;// 正在连接设备
    public static final int TAG_SOUNDTHREE = 3;// 请点击下一步，配置无线网络
    public static final int TAG_SOUNDFOUR = 4;// 请选择无线路由，输入密码，并点击右上角声波配置按钮
    public static final int TAG_SOUNDFIVE = 5;// 请将手机靠近设备
    public static final int TAG_SOUNDSIX = 6;// 叮
    public static final int TAG_SOUNDSEVINE = 7;// 配置完成，请点击图标查看设备
    public static final int TAG_SOUNDEIGHT = 8;// // 搜索声音

    // TODO 程序用到消息值，确保没有重复使用
    // JVLoginActivity
    public static final int GETDEMOURL = 1000;
    public static final int WHAT_SHOW_PRO = 0x01;// 显示dialog
    public static final int WHAT_DELETE_USER = 0x02;// 删除用户
    public static final int WHAT_SELECT_USER = 0x03;// 选择用户
    public static final int WHAT_CLICK_USER = 0x04;// 点击选择用户
    public static final int WHAT_INIT_ACCOUNT_SDK_FAILED = 0x04;// 初始化账号sdk失败
    // JVAddDeviceActivity
    public static final int WHAT_BARCODE_RESULT = 0x05; // 条码扫瞄返回值
    // JVQuickSetActivity
    public static final int WHAT_QUICK_SETTING_IPC_WIFI_SUCCESS = 0x07;// 快速设置获取IPCwifi成功
    public static final int WHAT_QUICK_SETTING_IPC_WIFI_FAILED = 0x08;// 快速设置获取IPCwifi失败
    public static final int WHAT_SHAKE_IPC_WIFI_SUCCESS = 0x09;// 摇一摇搜索IPC网络成功
    public static final int WHAT_SHAKE_IPC_WIFI_FAILED = 0x0A;// 摇一摇搜索IPC网络失败
    public static final int WHAT_AP_SET_SUCCESS = 0x0B;// AP配置成功
    public static final int WHAT_AP_SET_FAILED = 0x0C;// 刷新AP配置失败
    public static final int WHAT_DEVICE_MOST_COUNT = 0x0D;// 设备超过最大数
    public static final int WHAT_QUICK_SETTING_WIFI_CHANGED_FAIL = 0x0E;// 快速设置退出时wifi切换失败
    public static final int WHAT_QUICK_SETTING_MOBILE_WIFI_SUCC = 0x0F;// 快速配置手机wifi列表获取成功
    public static final int WHAT_QUICK_SETTING_WIFI_CONTINUE_SET = 0x10;// 快速设置继续配置wifi
    public static final int WHAT_QUICK_SETTING_WIFI_FINISH_SET = 0x11;// 快速设置完成wifi配置

    // JVMyDeviceFragment
    public static final int WHAT_DEVICE_ITEM_CLICK = 0x12;// 设备单击事件
    public static final int WHAT_DEVICE_ITEM_LONG_CLICK = 0x13;// 设备长按事件
    public static final int WHAT_DEVICE_ITEM_DEL_CLICK = 0x14;// 设备删除按钮事件
    public static final int WHAT_DEVICE_EDIT_CLICK = 0x15;// 设备编辑按钮事件
    // JVTabActivity
    public static final int WHAT_TAB_BACK = 0x16;

    // ChannelAdapter
    public static final int WHAT_CHANNEL_ITEM_CLICK = 0x17;// 通道单击事件
    public static final int WHAT_CHANNEL_ITEM_LONG_CLICK = 0x18;// 通道长按事件
    public static final int WHAT_CHANNEL_ITEM_DEL_CLICK = 0x19;// 通道删除按钮事件
    public static final int WHAT_CHANNEL_ADD_CLICK = 0x1A;// 通道添加事件
    public static final int WHAT_CHANNEL_EDIT_CLICK = 0x1B;// 通道删除按钮事件
    // PushAdapter
    public static final int WHAT_DELETE_ALARM_MESS = 0x1C;// 删除报警

    // JVMediaListActivity 媒体
    public static final int WHAT_LOAD_IMAGE_SUCCESS = 0x1D;
    public static final int WHAT_LOAD_IMAGE_FINISHED = 0x1E;
    public static final int WHAT_FILE_LOAD_SUCCESS = 0x1F;
    public static final int WHAT_FILE_NUM = 0x20;
    public static final int WHAT_FILE_SUM = 0x21;

    // JVOfflineActivity
    public static final int WHAT_COUNT_END = 0x22;// 倒计时结束
    public static final int WHAT_COUNTING = 0x23;// 倒计时

    public static final int WHAT_SEND_MAIL_SUCC = 0x24;// 邮件发送成功
    public static final int WHAT_SEND_MAIL_FAIL = 0x25;// 邮件发送失败
    public static final int WHAT_SEND_MAIL_SHOWMSG = 0x26;// 弹提示

    // StreamAdapter
    public static final int WHAT_STREAM_ITEM_CLICK = 0x27;// 码流单击事件
    public static final int WHAT_SELECT_SCREEN = 0x28;// 下拉选择多屏

    // JVRemotePlayBackActivity
    public static final int WHAT_REMOTE_DATA_SUCCESS = 0x29;// 数据加载成功
    public static final int WHAT_REMOTE_DATA_FAILED = 0x2A;// 数据加载失败
    public static final int WHAT_REMOTE_NO_DATA_FAILED = 0x2B;// 暂无远程视频
    public static final int WHAT_REMOTE_START_PLAY = 0x2C;// 开始播放远程视频
    public static final int WHAT_REMOTE_PLAY_FAILED = 0x2D;// 播放失败
    public static final int WHAT_REMOTE_PLAY_NOT_SUPPORT = 0x2E;// 手机暂不支持播放1080p的视频
    public static final int WHAT_REMOTE_PLAY_EXCEPTION = 0x2F;// 与主控断开连接
    public static final int WHAT_REMOTE_PLAY_DISMISS_PROGRESS = 0x30;// 隐藏远程回放进度条

    // JVDeviceUpdateActivity 一键升级
    public static final int WHAT_DOWNLOAD_KEY_UPDATE_SUCCESS = 0x31;
    public static final int WHAT_DOWNLOAD_KEY_UPDATE_CANCEL = 0x32;
    public static final int WHAT_DOWNLOADING_KEY_UPDATE = 0x33;
    public static final int WHAT_DOWNLOAD_KEY_UPDATE_ERROR = 0x34;
    public static final int WHAT_RESTART_DEVICE_SUCCESS = 0x35;
    public static final int WHAT_RESTART_DEVICE_FAILED = 0x36;
    public static final int WHAT_WRITE_KEY_UPDATE_SUCCESS = 0x37;

    // JVFeedBackActivity
    public static final int WHAT_FEEDBACK_SUCCESS = 0x38;// 提交意见反馈成功
    public static final int WHAT_FEEDBACK_FAILED = 0x39;// 提交意见反馈失败

    // JVWaveSetActivity
    public static final int WHAT_PLAY_AUDIO_WHAT = 0x3A;
    public static final int WHAT_SEND_WAVE_FINISHED = 0x3B;// 声波发送完毕
    public static final int WHAT_BROAD_DEVICE = 0x3C;// 广播到一个设备
    public static final int WHAT_BROAD_FINISHED = 0x3D;// 广播回调完毕
    public static final int WHAT_ADD_DEVICE = 0x3E;// 添加设备
    public static final int WHAT_SEND_WAVE = 0x3F;// 发送声波命令
    public static final int WHAT_WHEEL_DISMISS = 0x61;// 隐藏40秒倒计时

    // JVPlayActivity
    public static final int WHAT_CHECK_SURFACE = 0x40;
    public static final int WHAT_RESTORE_UI = 0x41;
    public static final int WHAT_PLAY_STATUS = 0x42;
    public static final int WHAT_SHOW_PROGRESS = 0x43;
    public static final int WHAT_DISMISS_PROGRESS = 0x44;
    public static final int WHAT_FINISH = 0x45;

    public static final int WHAT_AP_CONNECT_FINISHED = 0x46;// AP视频检测完成
    public static final int WHAT_START_CONNECT = 0x47;// 进播放开始连接
    public static final int WHAT_AP_CONNECT_REQUEST = 0x48;// AP请求ActivityForResult

    // JVMyDeviceFragment
    public static final int WHAT_DEVICE_GETDATA_SUCCESS = 0x49;// 设备加载成功
    public static final int WHAT_DEVICE_GETDATA_FAILED = 0x4A;// 设备加载失败
    public static final int WHAT_DEVICE_NO_DEVICE = 0x4B;// 暂无设备

    public static final int WHAT_AUTO_UPDATE = 0x4C;// 2分钟自动刷新时间到

    public static final int WHAT_AD_UPDATE = 0x4D;// 广告刷新
    public static final int WHAT_DEV_GETFINISHED = 0x4E;// 获取完设备列表

    /** ChannelFragment onResume */
    public static final int WHAT_CHANNEL_FRAGMENT_ONRESUME = 0x4F;// ChannelFragment
    // onResume
    /** ManageFragment onResume */
    public static final int WHAT_MANAGE_FRAGMENT_ONRESUME = 0x50;// ManageFragment
    public static final int WHAT_MANAGE_ITEM_CLICK = 0x51;// 通道单击事件
    /** 推送消息 tag */
    public static final int WHAT_PUSH_MESSAGE = 0x52;
    public static final int WHAT_BIND = 0x75;
    public static final int WHAT_DELETE_CHANNAL = 0x59;
    /** 引导界面滑屏 */
    public static final int WHAT_GUID_PAGE_SCROLL = 0x53;//
    public static final int WHAT_QUICK_SETTING_ERROR = 0x54;// 快速设置出错
    public static final int WHAT_QUICK_SETTING_DEV_ONLINE = 0x55;// 快速配置，设备上线
    public static final int WHAT_QUICK_SETTING_CONNECT_FAILED = 0x56;// 快速设置IPC连接失败
    /** 程序崩溃 */
    public static final int WHAT_APP_CRASH = 0x57;
    /** 解析完IP连接视频 */
    public static final int WHAT_RESOLVE_IP_CONNECT = 0x58;
    /** surfaceView 单击事件 */
    public static final int WHAT_SURFACEVIEW_CLICK = 0x59;

    /** 系统消息加载-刷新成功 */
    public static final int WHAT_SYSTEMINFO_REFRESH_SUCC = 0x60;

    /** 设备列表通道加载失败 */
    public static final int WHAT_MYDEVICE_POINT_FAILED = 0x61;

    /** dialog 隐藏 */
    public static final int WHAT_DIALOG_CLOSE = 0x62;

    /** 音频播放 */
    public static final int PLAY_AUDIO_WHAT = 0x26;

    /** 演示点缓冲 */
    public static final int WHAT_DEMO_BUFFING = 0x63;
    /** 演示点resume */
    public static final int WHAT_DEMO_RESUME = 0x64;

    /** 演示点url获取成功 */
    public static final int WHAT_DEMO_URL_SUCCESS = 0x65;

    /** 演示点url获取失败 */
    public static final int WHAT_DEMO_URL_FAILED = 0x66;

    /** 获取设备列表在线服务器检索失败 */
    public static final int WHAT_DEVICE_GETDATA_SEARCH_FAILED = 0x67;// 在线服务器检索失败

    /** 设备管理连接超时 */
    public static final int WHAT_MANAGE_TIMEOUT = 0x68;// 设备管理连接超时

    // MainApplication 发消息给BaseActivity、BaseFragment
    public static final int WHAT_HEART_ERROR = 0x69;// 心跳异常
    public static final int WHAT_HEART_NORMAL = 0x6A;// 心跳正常
    public static final int WHAT_HAS_NOT_LOGIN = 0x6B;// 未登录
    public static final int WHAT_HAS_LOGIN_SUCCESS = 0x6C;// 登陆成功
    public static final int WHAT_ACCOUNT_NORMAL = 0x6D;// 账号正常

    public static final int WHAT_SESSION_FAILURE = 0x6E;// 网络异常
    public static final int WHAT_SESSION_AUTOLOGIN = 0x6F;// 自动登陆，跳过登陆界面

    public static final int WHAT_HEART_TCP_ERROR = 0x70;// 账号库tcp连接断开
    public static final int WHAT_HEART_TCP_CLOSED = 0x71;// 账号库tcp连接关闭

    /** 网络异常视频断开 */
    public static final int WHAT_NET_ERROR_DISCONNECT = 0x72;// 网络异常视频断开
    /** 远程回放视频断开 */
    public static final int WHAT_VIDEO_DISCONNECT = 0x73;// 远程回放视频断开
    /** 远程回放视频下载 */
    public static final int PLAY_BACK_DOWNLOAD = 0x74;// 远程回放视频下载

    /** 视频播放进设备设置 */
    public static final int PLAY_DEVSET_REQUSET = 0x75;// 视频播放进设备设置
    /** 设备设置回视频播放 */
    public static final int PLAY_DEVSET_RESPONSE = 0x76;// 设备设置回视频播放
    /** 设备设置回视频播放 */
    public static final int TAB_WEBVIEW_BACK = 0x77;// tab页卡webview返回事件

    /** 视频广场重新加载url */
    public static final int TAB_PLAZZA_RELOAD_URL = 0x78;// 视频广场重新加载url

    /** 网络切换清小助手缓存 */
    public static final int NET_CHANGE_CLEAR_CACHE = 0x79;// 网络切换清小助手缓存

    /** tab onActivityResult */
    public static final int TAB_ON_ACTIVITY_RESULT = 0x80;// tab
                                                          // onActivityResult

    /** 社区图片上传成功 */
    public static final int BBS_IMG_UPLOAD_SUCCESS = 0x81;// 小维社区发帖图片上传成功

    /** 设备添加请求 */
    public static final int DEVICE_ADD_REQUEST = 0x82;// 设备添加请求

    /** 设备添加结果 */
    public static final int DEVICE_ADD_SUCCESS_RESULT = 0x83;// 设备添加结果

    /** 局域网添加设备请求 */
    public static final int SCAN_IN_LINE_REQUEST = 0x84;// 局域网添加设备请求
    /** 局域网添加设备回调结果 */
    public static final int SCAN_IN_LINE_RESULT = 0x85;// 局域网添加设备回调结果

    /** 播放界面耳机事件 2015.5.4 */
    public static final int PLAY_HEADSET = 0x86;// 耳机 2015.5.4
    public static final int PLAY_HEADSET_IN = 0x87;// 耳机插入 2015.5.4
    public static final int PLAY_HEADSET_OUT = 0x88;// 耳机拔出 2015.5.4
    public static boolean PLAY_HEADSET_FLAG = false;// 耳机 true:插入耳机 false：拔出耳机
                                                    // 2015.5.4

    public static final int MY_DEVICE_REFRESH = 0x89;// 我的设备列表通道获取正确刷新列表

    public static final int VOD_VIDEO_CHANG_PROCESS = 0x90;// 点播视频调节进度
    public static final int MY_DEVICE_REFRESH_IMAGE_THREAD = 0x91;// 我的设备列表刷新图片线程
    public static final int VOLUME_CHANGE_FINISHED = 0x92;// 声波音量调节完毕
    /** 新浪接口获取到国家 */
    public static final int GET_SINA_RESULT = 0x93;// 新浪接口获取到国家
    /** 广告有更新 */
    public static final int MY_DEVICE_AD_UPDATE = 0x94;// 广告有更新

    public static final int WHAT_DISMISS_DIALOG = 0x95;// 隐藏dialog
    public static final int JUMP_VIDEO_SHARE_PAGE = 0x100;// 跳转到视频分享web页

    /** 设备接口对应值 **/
    public static final int STORAGEMODE_NULL = 3;// 停止录像
    public static final int STORAGEMODE_NORMAL = 1;// 手动录像
    public static final int STORAGEMODE_ALARM = 2;// 报警录像

    public static final int SCREEN_NORMAL = 0;// 0(正),4(反)
    public static final int SCREEN_OVERTURN = 4;// 0(正),4(反)

    // Jni底层规定
    public static final int DECODE_OMX = 1;// 1硬
    public static final int DECODE_SOFT = 0;// 0软

    public static final String IMAGE_PNG_KIND = ".png";// 图片类型
    public static final String IMAGE_JPG_KIND = ".jpg";// 图片类型
    public static final String VIDEO_MP4_KIND = ".mp4";// 视频类型

    // 账号库广告接口标识 type（广告类型）意义
    public static final int AD_TYPE_1 = 1001;// 视频广场
    public static final int AD_TYPE_2 = 1002;// 小维知道
    public static final int AD_TYPE_3 = 1003;// 小维社区
    public static final int AD_TYPE_4 = 1004;// 产品宣传

    // action（广告行为）意义
    public static final int AD_ACTION_0 = 100;// 无连接URL
    public static final int AD_ACTION_1 = 101;// 直接打开URL

    /** 用户注册类型对应值 **/
    public static final int USERTYPE_MAIL = 1;// 邮箱注册
    public static final int USERTYPE_PHONE = 2;// 手机注册

    /*********************** 以下是状态变量key的声明 ********************************/

    /** 账号sdk是否初始化的key */

    /** 云台速度 key */
    public static final String YT_SPEED_KEY = "_yt_speed";// 云台速度 key

    /** 云视通sdk是否初始化的key */
    public static final String KEY_SHOW_GUID = "ShowGuide";
    public static final String KEY_INIT_ACCOUNT_SDK = "initAccountSdk";
    /** 云视通sdk是否初始化的key */
    public static final String KEY_INIT_CLOUD_SDK = "initCloudSdk";
    /** 更多视频广场和登录视频广场 */
    public static final String KEY_GONE_MORE = "GONEMORE";
    /** 用户名key */
    public static final String KEY_USERNAME = "USER_NAME";
    /** 密码key */
    public static final String KEY_PASSWORD = "PASSWORD";
    /** 用户手机key */
    public static final String KEY_USERPHONE = "PHONE";
    /** 用户邮箱key */
    public static final String KEY_USERMAIL = "MAIL";
    /** 上次登陆用户 */
    public static final String KEY_LAST_LOGIN_USER = "LAST_USER";
    /** 上次登陆时间key */
    public static final String KEY_LAST_LOGIN_TIME = "LAST_TIME";
    /** IMEIkey */
    public static final String IMEI = "IMEI";
    /** 公共版本key */
    public static final String NEUTRAL_VERSION = "NEUTRAL_VERSION";
    /** 本地登陆key */
    public static final String LOCAL_LOGIN = "LOCAL_LOGIN";
    /** 屏幕宽度key */
    public static final String SCREEN_WIDTH = "SCREEN_WIDTH";
    /** 屏幕高度key */
    public static final String SCREEN_HEIGHT = "SCREEN_HEIGHT";

    /** 数据加载key */
    public static final String DATA_LOADED_STATE = "DATA_LOADED_STATE";// -1加载失败，0没有数据，1，加载成功

    /** 推送消息JsonArray key */
    public static final String PUSH_JSONARRAY = "PUSH_JSONARRAY";

    /** 本地存储user列表key */
    public static final String LOCAL_USER_LIST = "LOCAL_USER_LIST";

    /** 存储设备列表key */
    public static String DEVICE_LIST = "";

    /** 本地存储设备列表key */
    public static final String LOCAL_DEVICE_LIST = "LOCAL_DEVICE_LIST";

    /** 缓存设备列表key */
    public static final String CACHE_DEVICE_LIST = "CACHE_DEVICE_LIST";

    /** 离线缓存设备列表key */
    public static final String OFFLINE_DEVICE_LIST = "OFFLINE_DEVICE_LIST";

    /** 账号异常 key */
    public static final String ACCOUNT_ERROR = "ACCOUNT_ERROR";// 账号异常

    /** 本地存储AD列表key */
    public static final String AD_LIST = "AD_LIST";

    /** 本地存储AD版本 key */
    public static final String AD_VERSION = "AD_VERSION";

    /** 本地存储APPImage key */
    public static final String APP_IMAGE = "APP_IMAGE";

    /** 存储取没取过设备列表key */
    public static String HAG_GOT_DEVICE = "HAG_GOT_DEVICE";

    /** 帮助图案首次显示标志 */
    public static String MORE_FREGMENT_FEEDBACK = "IS_FIRST";

    /** 提示不支持04版解码器 */
    public static String DIALOG_NOT_SUPPORT04 = "NOT_SUPPORT04";

    /** 提示不支持2.3系统 */
    public static String DIALOG_NOT_SUPPORT23 = "NOT_SUPPORT23";

    /** 网络切换需要广播 key */
    public static final String NEED_BROAD = "NEED_BROAD";

    /** 已经加载过视频广场 key */
    public static final String HAS_LOAD_DEMO = "HAS_LOAD_DEMO";

    /** 正在AP配置 key */
    public static final String AP_SETTING = "AP_SETTING";

    /** 广告检查过更新 */
    public static final String AD_UPDATE = "AD_UPDATE";

    /** 免登陆第一次登陆 */
    public static final String FIRST_LOGIN = "FIRST_LOGIN";

    // /** 功能设置页面场景图片标志位 */
    // public static String SETTING_SCENE = "SCENE";
    //
    // /** 功能设置页面帮助标志位 */
    // public static String SETTING_HELP = "HELP";
    //
    // /** 功能设置页面报警标志位 */
    // public static String SETTING_ALERT = "ALERT";
    //
    // /** 功能设置页面观看模式标志位 */
    // public static String SETTING_SEE_MODEL = "SEE_MODEL";

    /** 进程序WiFi名称 */
    public static final String KEY_OLD_WIFI = "OLD_WIFI";

    /** 进程序WiFi状态 */
    public static final String KEY_OLD_WIFI_STATE = "OLD_WIFI_STATE";

    public static final String KEY_LAST_PUT_STAMP = "last_put_stamp";

    public static final String BO_ID = "";
    public static final String BO_SECRET = "";

    public static final int RC_GPIN_ADD = 0x10; // 外设报警添加
    public static final int RC_GPIN_SET = 0x11; // 外设报警设置
    public static final int RC_GPIN_SECLECT = 0x12; // 外设报警查询
    public static final int RC_GPIN_DEL = 0x13; // 外设报警查询
    public static final int RC_GPIN_SET_SWITCH = 0x14; // 外设报警设置开关(只内部使用)
    public static final int RC_GPIN_SET_SWITCH_TIMEOUT = 0x15; // 外设报警设置开关
    public static final int ONLY_CONNECT_INDEX = 99; // 仅供报警相关连接云视通的window
                                                     // index
    public static String KEY_DEV_TOKEN = "DEV_TOKEN";
    public static final String EXT_THUMBNAIL_STORE_DIR = ".JVSThumbs";

    /**
     * 缩略图保存位置
     */
    public static final String EXT_THUMBNAIL_STORE_PATH = Environment
            .getExternalStorageDirectory().getAbsolutePath()
            + File.separator
            + APP_NAME
            + File.separator
            + EXT_THUMBNAIL_STORE_DIR
            + File.separator;

    /**
     * 图片缩略图大小（4:3长方形）
     */
    public static final int THUMBNAIL_WIDTH = 160;
    public static final int THUMBNAIL_HEIGHT = 120;

    // 报警设置功能索引
    public static final int DEV_SETTINGS_CLOUD = 0x00; // 云存储
    public static final int DEV_SETTINGS_ALARM = 0x01; // 安全防护开关
    public static final int DEV_SETTINGS_MD = 0x02; // 移动侦测开关
    public static final int DEV_SETTINGS_ALARMTIME = 0x03; // 防护时间段
    public static final int DEV_RESET_DEVICE = 0x04; // 设备重置
    public static final int DEV_RESTART_DEVICE = 0x07; // 设备重置
    public static final int DEV_MOD_USERINFO = 0x05; // 修改用户名密码
    public static final int DEV_ALARAM_SOUND = 0x06; // 设备报警声音
    /** 手动注销标志key */
    public static String MANUAL_LOGOUT_TAG = "MANUAL_LOGOUT_TAG";
    /** 实时弹出报警查看后标志guid **/
    public static String CHECK_ALARM_KEY = "CHECK_ALARM_KEY";
    /** 实时报警条数 **/
    public static final String NEW_PUSH_CNT_KEY = "NEW_PUSH_CNT_KEY";
    public static final int NEW_PUSH_MSG_TAG = 0x9990;
    public static final int NEW_PUSH_MSG_TAG_PRIVATE = 0x9991;
    public static final int WHAT_PERI_ITEM_CLICK = 0x90;// 外设菜单单击事件
    public static final int NEW_BBS = 0x99992;// 外设菜单单击事件

    /* 流量统计 */
    public static final String KEY_CLOUD_VOD_SIZE = "CLOUD_VOD_SIZE";
    /* GetDemoTask 调用完成 */
    public static final int GET_DEMO_TASK_FINISH = 999999;
    /* 个人中心(原来更多) */
    public static final String PROFILE_ITEM_MSG = "Message";
    public static final String PROFILE_ITEM_SETTING = "Setting";
    public static final String PROFILE_ITEM_SERVICE = "Service";
    public static final String PROFILE_ITEM_ABOUT = "About";
    public static final String PROFILE_ITEM_LOGOUT = "Logout";
    /* 设备设置 */
    public static final String DEVICE_SETTING_ALARM = "Alarm";
    public static final String DEVICE_SETTING_TIMEZONE = "TimeZone";
    public static final String DEVICE_SETTING_SYSTEMMANAGE = "SystemManage";
    public static final String DEVICE_SETTING_STORAGE = "Storage";
    public static final String DEVICE_SETTING_OTHER = "Other";
    public static final String DEVICE_SETTING_CLOUDSERVICE = "CloudService";
    /* 设备设置-报警设置 */
    public static final String DEVICE_SETTING_ALARM_SAFE_PROTECTED = "SafeProtected";
    public static final String DEVICE_SETTING_ALARM_MOTION_DETECTION = "MotionDetection";
    public static final String DEVICE_SETTING_ALARM_PROTECTED_TIME = "ProtectedTime";
    public static final String DEVICE_SETTING_ALARM_SENSITIVITY = "Sensitivity";
    public static final String DEVICE_SETTING_ALARM_SOUND = "AlarmSound";
    /* 设备设置-时间时区 */
    public static final String DEVICE_SETTING_TIMEZONE_ZONE = "SetTimeZone";
    public static final String DEVICE_SETTING_TIMEZONE_TIME = "SetTime";
    /* 设备设置-系统管理 */
    public static final String DEVICE_SETTING_SYSTEM_RESTART_DEVICE = "RestartDevice";
    public static final String DEVICE_SETTING_SYSTEM_RESET_DEVICE = "ResetDevice";
    public static final String DEVICE_SETTING_SYSTEM_MODIFY_PWD = "ModifyPwd";
    /* 设备设置-其它 */
    public static final String DEVICE_SETTING_OTHER_DECODE_TYPE = "DecodeType";
    public static final String DEVICE_SETTING_OTHER_OVERTURN = "Overturn";

    /* 报警AlarmActivity存储 */
    public static ArrayList<AlarmDialog> AlarmList = new ArrayList<AlarmDialog>();
    /* 个人中心-功能设置-MTU设置 */
    public static final String PROFILE_SETTING_MTU = "MtuSetting";
    public static final String PROFILE_SETTING_MTU_RADIO_BTN = "MtuRadioBtn";
    /* 个人中心-图像查看-视频 */
    public static final int PERSON_IMGVIEW_VIDEO_MP4INF = 0xB3; // mp4一些基本信息
    public static final int PERSON_IMGVIEW_VIDEO_PLAYPRESS = 0xB4; // 播放时间进度
    public static final int PERSON_IMGVIEW_VIDEO_PLAYEND = 0xB5; // 播放结束
    public static final int PERSON_IMGVIEW_VIDEO_PLAYFAIL = 0xB6; // 播放失败
}
