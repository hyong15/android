
package com.jovision.bean;

import android.view.Surface;
import android.view.SurfaceView;

import com.jovision.Consts;
import com.jovision.commons.MyList;
import com.jovision.utils.ConfigUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 简单的通道集合类
 * 
 * @author neo
 */
public class Channel {

    private final static String TAG = "Channel";

    private long primaryID = 0;

    /** 窗口索引 */
    private int index;// 0-35
    /** 设备通道，从1 开始，与张帅服务器统一从1开始 */
    private int channel;// 1-4

    /** 通道昵称 */
    public String channelName = "";
    private boolean isConnecting;
    private boolean isConnected;
    private boolean isRemotePlay;
    private boolean isConfigChannel;
    private SurfaceView surfaceView;
    private Device parent;
    private String dguid;// 设备服务器返回值
    private boolean hasFind;// 通道分组用

    private boolean isAuto = false;// 是否开启自动巡航
    // private boolean isPause = false;// 是否暂停视频
    private boolean isVoiceCall = false;// 是否正在对讲
    private boolean surfaceCreated = false;// surface是否已经创建
    private boolean isSendCMD = false;// 是否只发关键帧
    private boolean ispull;// 是否展开channal

    private int audioType = 0;// 音频类型
    private int audioByte = 0;// 音频比特率8 16

    // [Neo] 语音对讲时编码类型
    private int audioEncType = 0;
    // [Neo] 语音对讲时编码帧大小
    private int audioBlock = 0;

    // private boolean hasGotParams = false;
    private boolean newIpcFlag = true;// 为码流新增参数，是否新的IPC，新的融合代码后的IPC3个码流可以切换，融合前的只能两个码流相互切换

    private boolean agreeTextData = false;// 是否同意文本聊天
    private boolean isOMX = false;// 是否硬解
    private boolean singleVoiceTag = false;// 单向对讲标识位，默认是双向的，此值志获取到就不会变 2015.5.4
    private boolean singleVoice = false;// 单向对讲标识位，默认是双向的，此值志获取到有可能会变 2015.5.4
    private int storageMode = -1;// 录像模式// 1: 手动录像 2. 报警录像 storageMode
    private int streamTag = -1;// 码流参数值 第一码流(6)MainStreamQos 1,2,3
                               // 手机码流(5)MobileStreamQos 1,2,3 融合代码后手机码流改为
                               // MobileQuality 1,2,3
    private int screenTag = -1;// 屏幕方向值 effect_flag 老设备 0(正),4(反) 新设备不一定
    private int effect_flag = -1;// 屏幕方向值 effect_flag 新设备

    private int width = 0;// 音频类型
    private int height = 0;// 音频比特率
    private boolean supportVoice = true;

    // 慧通移动侦测和闪光灯状态 2014_11_27 闫帅
    private int htflight; // 惠通闪光灯状态 闪光灯状态 0.自动 1.开启 2.关闭
    private boolean htmotion;// 惠通移动侦测状态 //移动侦测状态 true.开 false.关

    private boolean isPaused;// Neo
    private Surface surface;

    private int lastPortLeft;
    private int lastPortBottom;
    private int lastPortWidth;
    private int lastPortHeight;

    /** 流媒体播放使用 2015-1-4 */
    private int vipLevel = 0;// 0:普通 1：新设备(支持流媒体)vip 2: 老设备vip全转发
    // "rtmp://192.168.10.22/live/B33380394_1"
    private String rtmpUrl = "";// =
                                // "rtmp://192.168.10.22/live/B33380394_1";//"rtmp://192.168.10.22/live/test01";

    public int getLastPortLeft() {
        return lastPortLeft;
    }

    public void setLastPortLeft(int lastPortLeft) {
        this.lastPortLeft = lastPortLeft;
    }

    public int getLastPortBottom() {
        return lastPortBottom;
    }

    public void setLastPortBottom(int lastPortBottom) {
        this.lastPortBottom = lastPortBottom;
    }

    public int getLastPortWidth() {
        return lastPortWidth;
    }

    public void setLastPortWidth(int lastPortWidth) {
        this.lastPortWidth = lastPortWidth;
    }

    public int getLastPortHeight() {
        return lastPortHeight;
    }

    public void setLastPortHeight(int lastPortHeight) {
        this.lastPortHeight = lastPortHeight;
    }

    public boolean isAgreeTextData() {
        return agreeTextData;
    }

    public void setAgreeTextData(boolean agreeTextData) {
        this.agreeTextData = agreeTextData;
    }

    public boolean isOMX() {
        return isOMX;
    }

    public void setOMX(boolean isOMX) {
        this.isOMX = isOMX;
    }

    public boolean isSingleVoice() {
        return singleVoice;
    }

    public void setSingleVoice(boolean singleVoice) {
        this.singleVoice = singleVoice;
    }

    public int getStorageMode() {
        return storageMode;
    }

    public void setStorageMode(int storageMode) {
        this.storageMode = storageMode;
    }

    public int getStreamTag() {
        return streamTag;
    }

    public void setStreamTag(int streamTag) {
        this.streamTag = streamTag;
    }

    public int getScreenTag() {
        return screenTag;
    }

    public void setScreenTag(int screenTag) {
        this.screenTag = screenTag;
    }

    public Channel(Device device, int index, int channel, boolean isConnected,
            boolean isRemotePlay, String nick) {
        this.parent = device;
        this.index = index;
        this.channel = channel;
        this.isConnected = isConnected;
        this.isRemotePlay = isRemotePlay;
        this.channelName = nick;
        isConfigChannel = false;
    }

    public Channel() {
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getChannel() {
        return channel;
    }

    public SurfaceView getSurfaceView() {
        return surfaceView;
    }

    public void setSurfaceView(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean isRemotePlay() {
        return isRemotePlay;
    }

    public void setRemotePlay(boolean isRemotePlay) {
        this.isRemotePlay = isRemotePlay;
    }

    public boolean isConfigChannel() {
        return isConfigChannel;
    }

    public JSONObject toJson() {
        JSONObject object = new JSONObject();

        try {
            object.put("index", index);
            object.put("channel", channel);
            object.put("channelName", channelName);
            object.put("vipLevel", vipLevel);
            object.put("rtmpUrl", rtmpUrl);

            // object.put("isPaused", isPaused);
            // object.put("isConnected", isConnected);
            // object.put("isConnecting", isConnecting);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return object;
    }

    @Override
    public String toString() {
        return toJson().toString();
    }

    public JSONArray toJsonArray(ArrayList<Channel> channelList) {
        JSONArray channelArray = new JSONArray();

        try {
            if (null != channelList && 0 != channelList.size()) {
                int size = channelList.size();
                for (int i = 0; i < size; i++) {
                    channelArray.put(i, channelList.get(i).toJson());
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return channelArray;
    }

    public String listToString(ArrayList<Channel> devList) {
        return toJsonArray(devList).toString();
    }

    public static Channel fromJson(String string) {
        Channel channel = new Channel();
        try {
            JSONObject object = new JSONObject(string);
            channel.setIndex(ConfigUtil.getInt(object, "index"));
            channel.setChannel(ConfigUtil.getInt(object, "channel"));
            channel.setChannelName(ConfigUtil.getString(object, "channelName"));
            channel.setVipLevel(ConfigUtil.getInt(object, "vipLevel"));
            channel.setRtmpUrl(ConfigUtil.getString(object, "rtmpUrl"));

            // channel.setConnecting(ConfigUtil.getBoolean(object,"isConnecting"));
            // channel.setConnecting(ConfigUtil.getBoolean(object,"isConnected"));
            // channel.setRemotePlay(ConfigUtil.getBoolean(object,"isRemotePlay"));
            // channel.setConfigChannel(ConfigUtil.getBoolean(object,"isConfigChannel"));
            // channel.setAuto(ConfigUtil.getBoolean(object,"isAuto"));
            // channel.setVoiceCall(ConfigUtil.getBoolean(object,"isVoiceCall"));
            // channel.setSurfaceCreated(ConfigUtil.getBoolean(object,"surfaceCreated"));
            // channel.setSendCMD(ConfigUtil.getBoolean(object,"isSendCMD"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return channel;
    }

    public static MyList<Channel> fromJsonArray(String string, Device device) {
        MyList<Channel> channelList = new MyList<Channel>(1);
        if (null == string || "".equalsIgnoreCase(string)) {
            return channelList;
        }
        JSONArray channelArray;
        try {
            channelArray = new JSONArray(string);
            if (null != channelArray && 0 != channelArray.length()) {
                int length = channelArray.length();
                for (int i = 0; i < length; i++) {
                    Channel channel = fromJson(channelArray.get(i).toString());
                    channel.setParent(device);
                    if (null != channel) {
                        channelList.add(channel.getChannel(), channel);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return channelList;
    }

    public Device getParent() {
        return parent;
    }

    public boolean isAuto() {
        return isAuto;
    }

    public void setAuto(boolean isAuto) {
        this.isAuto = isAuto;
    }

    // public boolean isPause() {
    // return isPause;
    // }
    //
    // public void setPause(boolean isPause) {
    // this.isPause = isPause;
    // }

    public boolean isConnecting() {
        return isConnecting;
    }

    public void setConnecting(boolean isConnecting) {
        this.isConnecting = isConnecting;
    }

    public boolean isVoiceCall() {
        return isVoiceCall;
    }

    public void setVoiceCall(boolean isVoiceCall) {
        this.isVoiceCall = isVoiceCall;
    }

    public boolean isSurfaceCreated() {
        return surfaceCreated;
    }

    public void setSurfaceCreated(boolean surfaceCreated) {
        this.surfaceCreated = surfaceCreated;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public void setConfigChannel(boolean isConfigChannel) {
        this.isConfigChannel = isConfigChannel;
    }

    public void setParent(Device parent) {
        this.parent = parent;
    }

    public boolean isSendCMD() {
        return isSendCMD;
    }

    public void setSendCMD(boolean isSendCMD) {
        this.isSendCMD = isSendCMD;
    }

    public long getPrimaryID() {
        return primaryID;
    }

    public void setPrimaryID(long primaryID) {
        this.primaryID = primaryID;
    }

    public int getAudioType() {
        return audioType;
    }

    public void setAudioType(int audioType) {
        this.audioType = audioType;
    }

    public int getAudioByte() {
        return audioByte;
    }

    public void setAudioByte(int audioByte) {
        this.audioByte = audioByte;
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void setPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }

    public Surface getSurface() {
        return surface;
    }

    public void setSurface(Surface surface) {
        this.surface = surface;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public boolean isIspull() {
        return ispull;
    }

    public void setIspull(boolean ispull) {
        this.ispull = ispull;
    }

    public int getEffect_flag() {
        return effect_flag;
    }

    public void setEffect_flag(int effect_flag) {
        this.effect_flag = effect_flag;
    }

    public String getDguid() {
        return dguid;
    }

    public void setDguid(String dguid) {
        this.dguid = dguid;
    }

    public boolean isHasFind() {
        return hasFind;
    }

    public void setHasFind(boolean hasFind) {
        this.hasFind = hasFind;
    }

    // public boolean isHasGotParams() {
    // return hasGotParams;
    // }
    //
    // public void setHasGotParams(boolean hasGotParams) {
    // this.hasGotParams = hasGotParams;
    // }

    public int getAudioEncType() {
        return audioEncType;
    }

    public int getAudioBlock() {
        return audioBlock;
    }

    public void setAudioEncType(int audioEncType) {
        this.audioEncType = audioEncType;

        switch (audioEncType) {
            case Consts.JAE_ENCODER_ALAW:
            case Consts.JAE_ENCODER_ULAW:
                audioBlock = Consts.ENC_G711_SIZE;
                break;

            case Consts.JAE_ENCODER_SAMR:
                audioBlock = Consts.ENC_AMR_SIZE;
                break;

            case Consts.JAE_ENCODER_G729:
                audioBlock = Consts.ENC_G729_SIZE;
                break;

            default:
                audioBlock = Consts.ENC_PCM_SIZE;
                break;
        }
    }

    public boolean isSupportVoice() {
        return supportVoice;
    }

    public void setSupportVoice(boolean supportVoice) {
        this.supportVoice = supportVoice;
    }

    public boolean isNewIpcFlag() {
        return newIpcFlag;
    }

    public void setNewIpcFlag(boolean newIpcFlag) {
        this.newIpcFlag = newIpcFlag;
    }

    public int getHtflight() {
        return htflight;
    }

    public void setHtflight(int htflight) {
        this.htflight = htflight;
    }

    public boolean isHtmotion() {
        return htmotion;
    }

    public void setHtmotion(boolean htmotion) {
        this.htmotion = htmotion;
    }

    public int getVipLevel() {
        return vipLevel;
    }

    public void setVipLevel(int vipLevel) {
        this.vipLevel = vipLevel;
    }

    public String getRtmpUrl() {
        return rtmpUrl;
    }

    public void setRtmpUrl(String rtmpUrl) {
        this.rtmpUrl = rtmpUrl;
    }

    public boolean isSingleVoiceTag() {
        return singleVoiceTag;
    }

    public void setSingleVoiceTag(boolean singleVoiceTag) {
        this.singleVoiceTag = singleVoiceTag;
    }

}
