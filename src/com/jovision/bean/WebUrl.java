
package com.jovision.bean;

public class WebUrl {
    private String demoUrl;// (视频广场url)
    private int demoSwitch;// (视频广场开关 0屏蔽 1开启)
    private String custUrl;// (我要装监控url)
    private int custSwitch;// (我要装监控开关 0屏蔽 1开启)
    private String statUrl;// (云视通指数url)
    private int statSwitch;// (云视通指数开关 0屏蔽 1开启)
    private String bbsUrl;// (论坛url)
    private int bbsSwitch;// (论坛开关 0屏蔽 1开启)
    private String gcsUrl;// (我是工程商url)
    private int gcsSwitch;// (我是工程商开关 0屏蔽 1开启)
    private String cloudUrl = null;// 云服务
    private int cloudSwitch;// 云服务标志
    private String addDeviceurl;// 添加设备Url
    private int addDeviceSwitch;// 添加设备开关
    private String shopUrl;// 商城url
    private int shopSwitch;// 商城开关(0屏蔽 1开启)
    private String shareUrl;// 视频分享到广场url
    private int shareSwitch;// 视频分享到广场开关(0屏蔽 1开启);
    private int noteSwitch;// 个人中心帖子开关(0屏蔽 1开启);
    private String noteListUrl;// 个人中心帖子列表url
    private String noteDetailUrl;// 个人中心帖子详情url

    public String getDemoUrl() {
        return demoUrl;
    }

    public void setDemoUrl(String demoUrl) {
        this.demoUrl = demoUrl;
    }

    public String getCustUrl() {
        return custUrl;
    }

    public int getDemoSwitch() {
        return demoSwitch;
    }

    public void setDemoSwitch(int demoSwitch) {
        this.demoSwitch = demoSwitch;
    }

    public int getCustSwitch() {
        return custSwitch;
    }

    public void setCustSwitch(int custSwitch) {
        this.custSwitch = custSwitch;
    }

    public int getStatSwitch() {
        return statSwitch;
    }

    public void setStatSwitch(int statSwitch) {
        this.statSwitch = statSwitch;
    }

    public int getBbsSwitch() {
        return bbsSwitch;
    }

    public void setBbsSwitch(int bbsSwitch) {
        this.bbsSwitch = bbsSwitch;
    }

    public int getGcsSwitch() {
        return gcsSwitch;
    }

    public void setGcsSwitch(int gcsSwitch) {
        this.gcsSwitch = gcsSwitch;
    }

    public int getCloudSwitch() {
        return cloudSwitch;
    }

    public void setCloudSwitch(int cloudSwitch) {
        this.cloudSwitch = cloudSwitch;
    }

    public void setCustUrl(String custUrl) {
        this.custUrl = custUrl;
    }

    public String getStatUrl() {
        return statUrl;
    }

    public void setStatUrl(String statUrl) {
        this.statUrl = statUrl;
    }

    public String getBbsUrl() {
        return bbsUrl;
    }

    public void setBbsUrl(String bbsUrl) {
        this.bbsUrl = bbsUrl;
    }

    public String getGcsUrl() {
        return gcsUrl;
    }

    public void setGcsUrl(String gcsUrl) {
        this.gcsUrl = gcsUrl;
    }

    public String getCloudUrl() {
        return cloudUrl;
    }

    public void setCloudUrl(String cloudUrl) {
        this.cloudUrl = cloudUrl;
    }

    public String getAddDeviceurl() {
        return addDeviceurl;
    }

    public void setAddDeviceurl(String addDeviceurl) {
        this.addDeviceurl = addDeviceurl;
    }

    public int getAddDeviceSwitch() {
        return addDeviceSwitch;
    }

    public void setAddDeviceSwitch(int addDeviceSwitch) {
        this.addDeviceSwitch = addDeviceSwitch;
    }

    public String getShopUrl() {
        return shopUrl;
    }

    public void setShopUrl(String shopUrl) {
        this.shopUrl = shopUrl;
    }

    public int getShopSwitch() {
        return shopSwitch;
    }

    public void setShopSwitch(int shopSwitch) {
        this.shopSwitch = shopSwitch;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public int getShareSwitch() {
        return shareSwitch;
    }

    public void setShareSwitch(int shareSwitch) {
        this.shareSwitch = shareSwitch;
    }

    public int getNoteSwitch() {
        return noteSwitch;
    }

    public void setNoteSwitch(int noteSwitch) {
        this.noteSwitch = noteSwitch;
    }

    public String getNoteListUrl() {
        return noteListUrl;
    }

    public void setNoteListUrl(String noteListUrl) {
        this.noteListUrl = noteListUrl;
    }

    public String getNoteDetailUrl() {
        return noteDetailUrl;
    }

    public void setNoteDetailUrl(String noteDetailUrl) {
        this.noteDetailUrl = noteDetailUrl;
    }

}
