
package com.jovision.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import com.jovision.bean.MoreFragmentBean;
import com.jovision.commons.MyLog;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Json 文件操作工具类
 */
public class JsonFileReader {
    private static final String TAG = "JsonFileReader";
    private static final String EMPTY = "";
    private static final String DOT = ".";
    private static final int ERROR_RESID = 0;

    /**
     * 获取Json字符串
     * 
     * @param context
     * @param fileName
     * @return String类型的json串
     */
    public static String getJson(Context context, String fileName) {

        StringBuilder stringBuilder = new StringBuilder();
        try {
            AssetManager assetManager = context.getAssets();
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (Exception e) {
            MyLog.v(TAG, "--getJson error--");
        }

        return stringBuilder.toString();
    }

    /**
     * 设置个人中心的列表数据
     * 
     * @param context 上下文对象
     * @param jsonStr json串
     * @param tag 具体的json内容的标志(例:个人中心,消息,图像查看...)
     */
    public static List<MoreFragmentBean> getDataListByJson(Context context,
            String jsonStr, String tag) {
        try {
            MyLog.v(TAG, "--json tag:--" + tag);
            Resources res = context.getResources();
            List<MoreFragmentBean> dataList = new ArrayList<MoreFragmentBean>();
            JSONObject object = new JSONObject(jsonStr);
            JSONArray array = object.getJSONArray(tag);
            int len = array.length();
            MyLog.v(TAG, "--jsonarray length:--" + len);
            for (int i = 0; i < len; i++) {
                JSONObject content = array.getJSONObject(i);
                MoreFragmentBean item = new MoreFragmentBean();

                /** 标志 **/
                item.setItemFlag(content.getString("itemFlag"));
                /** 获取名称对应的资源ID **/
                int nameResId = getResId(context, 's',
                        content.getString("itemName"));
                if (nameResId != ERROR_RESID) {
                    item.setName(res.getString(nameResId));
                } else {
                    item.setName(EMPTY);
                }
                /** 获取图片对应的资源ID **/
                int iconResId = getResId(context, 'd',
                        content.getString("itemImg"));
                item.setItem_img(iconResId);
                /** 是否隐藏 true隐藏 false显示 **/
                item.setLocalDismiss(content.optBoolean("localDismiss"));
                item.setDismiss(content.optBoolean("dismiss"));
                /** 右侧是否显示新 **/
                item.setIsnew(content.optBoolean("isNew"));
                /** 是否显示空白栏 **/
                item.setShowWhiteBlock(content.optBoolean("showWhiteBlock"));
                /** 是否显示右侧圆形按钮 **/
                item.setShowRightCircleBtn(content
                        .optBoolean("showRightCircleBtn"));
                /** 显示右侧圆形按钮选中 **/
                item.setShowRightCircleBtnSelected(content
                        .optBoolean("showRightCircleBtnSelected"));
                /** 关于里面显示版本信息 **/
                item.setShowVersion(content.optBoolean("showVersion"));
                /** 是否显示右侧通知信息 **/
                item.setShowTVNews(content.optBoolean("showTVNews"));
                /** 是否显示右侧bbs通知信息 **/
                item.setShowBBSNews(content.optBoolean("showBBSNews"));
                /** 是否为当前模块的最后一个元素 **/
                item.setLast(content.optBoolean("isLast"));
                /** 设备设置 - 功能的使用说明 **/
                int tipsResId = getResId(context, 's',
                        content.optString("tips"));
                if (tipsResId != ERROR_RESID) {
                    item.setTips(res.getString(tipsResId));
                } else {
                    item.setTips(EMPTY);
                }
                /** 设备设置 - 是否显示顶部的分隔线 **/
                item.setShowTopLine(content.optBoolean("showTopLine"));

                dataList.add(item);
            }
            return dataList;
        } catch (Exception e) {
            MyLog.v(TAG, "--getProfileDataList error--");
            return null;
        }
    }

    /**
     * 获取资源ID
     * 
     * @param context
     * @param type
     * @param name
     */
    private static int getResId(Context context, char type, String resInfo) {
        String resName = null;
        int resId = 0;
        Resources res = context.getResources();

        // 获取资源名称
        resName = getResName(resInfo);
        if (resName == null) {
            return resId;
        }

        MyLog.v(TAG, "--resName:--" + resName);
        switch (type) {
            case 's':// string类型
                resId = res.getIdentifier(resName, "string",
                        context.getPackageName());
                break;
            case 'd':// drawable类型
                resId = res.getIdentifier(resName, "drawable",
                        context.getPackageName());
                break;
            case 'i'://
                break;
            default:
        }

        return resId;
    }

    /**
     * 获取资源的名称
     * 
     * @param resInfo
     * @return
     */
    private static String getResName(String resInfo) {
        String resName = null;

        if (resInfo != null && !resInfo.equals(EMPTY)) {
            try {
                int lastPosition = resInfo.lastIndexOf(DOT);
                if (lastPosition != -1) {
                    resName = resInfo.substring(lastPosition + 1,
                            resInfo.length());
                } else {
                    return null;
                }
            } catch (Exception e) {
                MyLog.v(TAG, "--getResName error--");
                return null;
            }
        }

        return resName;
    }
}
