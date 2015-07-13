
package com.jovision.views;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;

public class popSaveManager extends PopupWindow {

    public RelativeLayout Alarm_Recode, Normal_Recode, Stop_Recode;
    private View mMenuView;
    private RelativeLayout pop_outside;
    public TextView Alarm_Recodetext, Normal_Recodetext, Stop_Recodetext;

    public popSaveManager(Activity context, OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.popsavemanager, null);
        Alarm_Recode = (RelativeLayout) mMenuView
                .findViewById(R.id.Alarm_recode);
        Normal_Recode = (RelativeLayout) mMenuView
                .findViewById(R.id.Normal_recode);

        Alarm_Recodetext = (TextView) mMenuView
                .findViewById(R.id.Alarm_recodetext);
        Normal_Recodetext = (TextView) mMenuView
                .findViewById(R.id.Normal_recodetext);
        Stop_Recodetext = (TextView) mMenuView
                .findViewById(R.id.Stop_recodetext);

        Stop_Recode = (RelativeLayout) mMenuView.findViewById(R.id.Stop_recode);
        pop_outside = (RelativeLayout) mMenuView.findViewById(R.id.pop_outside);

        pop_outside.setOnClickListener(itemsOnClick);
        Alarm_Recode.setOnClickListener(itemsOnClick);
        Normal_Recode.setOnClickListener(itemsOnClick);
        Stop_Recode.setOnClickListener(itemsOnClick);
        // 设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        // 设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.FILL_PARENT);
        // 设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        // // 设置SelectPicPopupWindow弹出窗体可点击
        // this.setFocusable(true);
        // 设置SelectPicPopupWindow弹出窗体动画效果
        // this.setAnimationStyle(R.style.popupAnimation);
        // mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
    }

}
