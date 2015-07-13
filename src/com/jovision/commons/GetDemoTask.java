
package com.jovision.commons;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.Global;
import com.jovision.activities.BaseActivity;
import com.jovision.activities.JVWebViewActivity;
import com.jovision.bean.WebUrl;
import com.jovision.utils.ConfigUtil;
import com.jovision.utils.DeviceUtil;

//获取演示点 设置三种类型参数分别为String,Integer,String
public class GetDemoTask extends AsyncTask<String, Integer, Integer> {
    private Context mContext;
    private String demoUrl;
    private WebUrl webUrl;
    private String count;
    private String fragmentString;
    private String[] mExtendParams = new String[3];// 扩展参数

    public GetDemoTask(Context con) {
        mContext = con;
    }

    // 可变长的输入参数，与AsyncTask.exucute()对应
    @Override
    protected Integer doInBackground(String... params) {
        int getRes = -1;// 0成功 1失败
        // if (!Boolean.valueOf(((BaseActivity) mContext).statusHashMap
        // .get(Consts.LOCAL_LOGIN))) {// 在线
        // sid = JVACCOUNT.GetSession();
        // } else {
        // sid = "";
        // }

        count = params[1];
        fragmentString = params[2];
        // 扩展参数处理
        if (params.length > 3) {
            for (int i = 3, j = 0; i < params.length; i++, j++) {
                mExtendParams[j] = params[i];
            }
        }
        // demoUrl = DeviceUtil.getDemoDeviceList2(Consts.APP_NAME);
        // demoUrl = "http://www.cloudsee.net/phone.action";

        webUrl = DeviceUtil.getWebUrl(ConfigUtil.getLanguage2(mContext) - 1);
        if (null != webUrl) {
            getRes = 0;
        } else {
            webUrl = DeviceUtil
                    .getWebUrl(ConfigUtil.getLanguage2(mContext) - 1);
        }
        return getRes;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    @Override
    protected void onPostExecute(Integer result) {
        // 返回HTML页面的内容此方法在主线程执行，任务执行的结果作为此方法的参数返回。
        ((BaseActivity) mContext).dismissDialog();
        if (0 == result) {
            ((BaseActivity) mContext).statusHashMap.put(Consts.MORE_DEMOURL,
                    webUrl.getDemoUrl());
            int counts = Integer.valueOf(count);

            // String lan = "";
            // if (Consts.LANGUAGE_ZH == ConfigUtil.getLanguage2(mContext)) {
            // lan = "zh_cn";
            // } else if (Consts.LANGUAGE_ZHTW == ConfigUtil
            // .getLanguage2(mContext)) {
            // lan = "zh_tw";
            // } else {
            // lan = "en_us";
            // }

            if (null != webUrl.getGcsUrl()) {// 获取工程商开关
                ((BaseActivity) mContext).statusHashMap.put(
                        Consts.MORE_GCS_SWITCH,
                        String.valueOf(webUrl.getGcsSwitch()));
                ((BaseActivity) mContext).onNotify(Consts.MORE_BBSNUMNOTY, 0,
                        0, null);
            }
            // 获取添加设备界面 2015-04-17
            if (webUrl != null) {
                String dburl = webUrl.getAddDeviceurl();
                ((BaseActivity) mContext).statusHashMap.put(
                        Consts.MORE_ADDDEVICEURL, dburl);
            }

            // 小维商城开关
            ((BaseActivity) mContext).statusHashMap.put(
                    Consts.MORE_SHOP_SWITCH,
                    String.valueOf(webUrl.getShopSwitch()));

            // 个人中心帖子列表
            if (webUrl != null) {
                Log.v("GetDemoTask", "--note switch:" + webUrl.getNoteSwitch());
                ((BaseActivity) mContext).statusHashMap.put(
                        Consts.PROFILE_NOTE_SWITCH,
                        String.valueOf(webUrl.getNoteSwitch()));
                if (webUrl.getNoteSwitch() == 1) {
                    // 保存url
                    ((BaseActivity) mContext).statusHashMap
                            .put(Consts.PROFILE_NOTELISTURL,
                                    webUrl.getNoteListUrl());
                    ((BaseActivity) mContext).statusHashMap.put(
                            Consts.PROFILE_NOTEDETAILURL,
                            webUrl.getNoteDetailUrl());
                }
            }

            switch (counts) {
                case 0:// 2015-3-13从我要装监控变成工程商入驻
                    String custurl;
                    if (null != webUrl.getGcsUrl()) {
                        Intent intentAD0 = new Intent(mContext,
                                JVWebViewActivity.class);
                        custurl = webUrl.getGcsUrl()
                                + ConfigUtil.getBbsParamsStr(mContext);
                        // + "?" + "&lang=" + lan + "&d="
                        // + System.currentTimeMillis();
                        ((BaseActivity) mContext).statusHashMap.put(
                                Consts.MORE_GCSURL, custurl);
                        intentAD0.putExtra("URL", custurl);
                        intentAD0.putExtra("title", -2);
                        mContext.startActivity(intentAD0);
                    } else {
                        ((BaseActivity) mContext)
                                .showTextToast(R.string.str_video_load_failed);
                    }
                    break;

                case 1:// 视频广场

                    demoUrl = webUrl.getDemoUrl()
                            + ConfigUtil.getDemoParamsStr(mContext);
                    MyLog.v("demoUrl", demoUrl);
                    if (!"fragmentString".equals(fragmentString)
                            && null != webUrl.getDemoUrl()) {
                        Intent intentAD = new Intent(mContext,
                                JVWebViewActivity.class);
                        intentAD.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intentAD.putExtra("URL", demoUrl);
                        intentAD.putExtra("title", -2);
                        mContext.startActivity(intentAD);
                    } else if (!"fragmentString".equals(fragmentString)
                            && null == webUrl.getDemoUrl()) {
                        ((BaseActivity) mContext)
                                .showTextToast(R.string.demo_get_failed);
                    }

                    break;

                case 2:// 云视通指数
                       // String staturl = "";
                       // if (null != webUrl.getStatUrl()) {
                       // Intent intentAD2 = new Intent(mContext,
                       // JVWebViewActivity.class);
                       // staturl = webUrl.getStatUrl() + "?" + "&lang=" + lan
                       // + "&d=" + System.currentTimeMillis();
                       // ((BaseActivity) mContext).statusHashMap.put(
                       // Consts.MORE_STATURL, staturl);
                       //
                       // Log.i("TAG", staturl);
                       // intentAD2.putExtra("URL", staturl);
                       // intentAD2.putExtra("title", -2);
                       // mContext.startActivity(intentAD2);
                       // } else {
                       // ((BaseActivity) mContext)
                       // .showTextToast(R.string.str_video_load_failed);
                       // }

                    break;
                case 3:// 社区论坛
                    String bbsurl = "";
                    if (null != webUrl.getBbsUrl()) {
                        Intent intentAD2 = new Intent(mContext,
                                JVWebViewActivity.class);

                        bbsurl = webUrl.getBbsUrl()
                                + ConfigUtil.getBbsParamsStr(mContext);
                        ((BaseActivity) mContext).statusHashMap.put(
                                Consts.MORE_BBS, bbsurl);
                        intentAD2.putExtra("URL", bbsurl);
                        intentAD2.putExtra("title", -2);
                        mContext.startActivity(intentAD2);
                    } else {
                        ((BaseActivity) mContext)
                                .showTextToast(R.string.str_video_load_failed);
                    }
                    break;
                case 4:// 论坛条数
                    String bbsnum = " ";
                    if (null != webUrl.getBbsUrl()) {
                        bbsnum = ConfigUtil.getBbsNumParamsStr(webUrl.getBbsUrl(),
                                mContext);
                        ((BaseActivity) mContext).statusHashMap.put(
                                Consts.MORE_BBSNUM, bbsnum);
                    }
                    break;
                case 5:// 视频广场刷新
                    demoUrl = webUrl.getDemoUrl()
                            + ConfigUtil.getDemoParamsStr(mContext);
                    if (null != webUrl.getDemoUrl()) {
                        ((BaseActivity) mContext).onNotify(
                                Consts.TAB_PLAZZA_RELOAD_URL, 0, 0, demoUrl);
                    }
                    break;
                case 6:// 云服务开通
                    String cloudurl = webUrl.getCloudUrl();
                    if (null != cloudurl && !cloudurl.equals("")) {
                        Intent intentAD2 = new Intent(mContext,
                                JVWebViewActivity.class);
                        cloudurl = ConfigUtil.getCloudShopParamsStr(cloudurl,
                                mContext);
                        ((BaseActivity) mContext).statusHashMap.put(
                                Consts.MORE_CLOUD_SHOP, cloudurl);
                        Global.CLOUD_BUY_URL = cloudurl;
                        intentAD2.putExtra("URL", cloudurl);
                        intentAD2.putExtra("title", -2);
                        mContext.startActivity(intentAD2);
                    } else {
                        ((BaseActivity) mContext)
                                .showTextToast(R.string.str_video_load_failed);
                    }
                    break;
                case 7:// 小维商城
                    String shopurl = webUrl.getShopUrl();
                    if (null != shopurl && !shopurl.equals("")) {
                        Intent intentAD2 = new Intent(mContext,
                                JVWebViewActivity.class);
                        // 商城url拼接工作
                        shopurl = ConfigUtil.getCloudShopParamsStr(shopurl,
                                mContext);
                        // 保存商城的url
                        ((BaseActivity) mContext).statusHashMap.put(
                                Consts.MORE_SHOPURL, shopurl);

                        intentAD2.putExtra("URL", shopurl);
                        intentAD2.putExtra("title", -2);
                        mContext.startActivity(intentAD2);
                    } else {
                        ((BaseActivity) mContext)
                                .showTextToast(R.string.str_video_load_failed);
                    }
                    break;
                case 8:// 视频分享到广场
                    String shareurl = webUrl.getShareUrl();
                    if (null != shareurl && !shareurl.equals("")) {
                        Intent intentAD2 = new Intent(mContext,
                                JVWebViewActivity.class);
                        // 保存url
                        ((BaseActivity) mContext).statusHashMap.put(
                                Consts.MORE_SHAREURL, shareurl);

                        intentAD2.putExtra("mark", "videoshare");
                        intentAD2.putExtra("URL", shareurl);
                        intentAD2.putExtra("cloudno", mExtendParams[0]);
                        intentAD2.putExtra("capturepath", mExtendParams[1]);
                        intentAD2.putExtra("title", -2);
                        mContext.startActivity(intentAD2);
                    } else {
                        ((BaseActivity) mContext)
                                .showTextToast(R.string.str_video_load_failed);
                    }
                    break;
                case 9:
                    if (null != webUrl) {
                        ((BaseActivity) mContext).statusHashMap.put(
                                Consts.MORE_CLOUD_SWITCH, webUrl.getCloudSwitch()
                                        + "");
                    }
                    break;
                case 999999:// 异步调用完成(仅作为异步调用完成给个回馈)
                    ((BaseActivity) mContext).onNotify(Consts.GET_DEMO_TASK_FINISH,
                            0, 0, null);
                    break;
                default:
                    break;
            }
            // http://192.168.10.17:8080/WebPlatform/mobile/index.html?plat=A&platv=B&lang=C&sid=D
            // A：取值范围:iphone/ipad/android/
            // B：平台版本号,android系统版本
            // C：语言，参考如下连接中的各语言简写，如简体中文为：zh_cn，http://www.douban.com/group/topic/37393602/
            // D：登录会话号：未登录则为空
            //
            // 以上参数顺序没有要求，大小写没有要求
            //
            // 注意：aaa.html后为"?",参数之间使用"&"进行连接
        } else {
        	//HYONG 屏蔽掉，因为改了推送号：PRODUCT_TYPE，不然切换到我的设备老是弹出“数据加载失败”
        	/*
            ((BaseActivity) mContext)
                    .showTextToast(R.string.str_video_load_failed);
                    */
        }
    }

    @Override
    protected void onPreExecute() {
        // 任务启动，可以在这里显示一个对话框，这里简单处理,当任务执行之前开始调用此方法，可以在这里显示进度对话框。
        ((BaseActivity) mContext).createDialog("", true);
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        // 更新进度,此方法在主线程执行，用于显示任务执行的进度。
    }
}
