
package com.jovision.bean;

/**
 * 个人中心,设备设置共用这个实体类
 */
public class MoreFragmentBean {
    private int item_img;
    private boolean isnew;
    private String name;
    private String itemFlag;
    private boolean dismiss;
    /** 是否隐藏 true隐藏 false显示 **/
    private boolean localDismiss;
    /** 访客的情况 是否隐藏 true隐藏 false显示 **/
    private boolean showTVNews;
    /** 是否显示右面TV通知信息 **/
    private boolean showBBSNews;
    /** 是否显示右面bbs通知信息 **/
    private boolean showRightCircleBtn;
    /** 是否显示右侧圆形按钮 **/
    private boolean showRightCircleBtnSelected;
    /** 显示右侧圆形按钮选中 **/
    private boolean showWhiteBlock;
    /** 是否显示空白栏 **/
    private boolean showVersion;
    /** 关于里面显示版本信息 **/
    private boolean isLast;
    /** 是否为当前模块的最后一个元素 **/
    private String tips;
    /** 设备设置 - 功能的使用说明 **/
    private boolean showTopLine;

    /** 设备设置 - 是否显示顶部的分隔线 true显示 false隐藏 **/

    public int getItem_img() {
        return item_img;
    }

    public void setItem_img(int item_img) {
        this.item_img = item_img;
    }

    public boolean isIsnew() {
        return isnew;
    }

    public void setIsnew(boolean isnew) {
        this.isnew = isnew;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDismiss() {
        return dismiss;
    }

    public void setDismiss(boolean dismiss) {
        this.dismiss = dismiss;
    }

    public String getItemFlag() {
        return itemFlag;
    }

    public void setItemFlag(String itemFlag) {
        this.itemFlag = itemFlag;
    }

    public boolean isShowTVNews() {
        return showTVNews;
    }

    public void setShowTVNews(boolean showTVNews) {
        this.showTVNews = showTVNews;
    }

    public boolean isShowBBSNews() {
        return showBBSNews;
    }

    public void setShowBBSNews(boolean showBBSNews) {
        this.showBBSNews = showBBSNews;
    }

    public boolean isShowWhiteBlock() {
        return showWhiteBlock;
    }

    public void setShowWhiteBlock(boolean showWhiteBlock) {
        this.showWhiteBlock = showWhiteBlock;
    }

    public boolean isShowRightCircleBtn() {
        return showRightCircleBtn;
    }

    public void setShowRightCircleBtn(boolean showRightCircleBtn) {
        this.showRightCircleBtn = showRightCircleBtn;
    }

    public boolean isShowVersion() {
        return showVersion;
    }

    public void setShowVersion(boolean showVersion) {
        this.showVersion = showVersion;
    }

    public boolean isShowRightCircleBtnSelected() {
        return showRightCircleBtnSelected;
    }

    public void setShowRightCircleBtnSelected(boolean showRightCircleBtnSelected) {
        this.showRightCircleBtnSelected = showRightCircleBtnSelected;
    }

    public boolean isLast() {
        return isLast;
    }

    public void setLast(boolean isLast) {
        this.isLast = isLast;
    }

    public boolean isLocalDismiss() {
        return localDismiss;
    }

    public void setLocalDismiss(boolean localDismiss) {
        this.localDismiss = localDismiss;
    }

    public String getTips() {
        return tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public boolean isShowTopLine() {
        return showTopLine;
    }

    public void setShowTopLine(boolean showTopLine) {
        this.showTopLine = showTopLine;
    }

}
