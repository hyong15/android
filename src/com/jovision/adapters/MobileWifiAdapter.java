
package com.jovision.adapters;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tmway.temsee.R;

import java.util.ArrayList;

public class MobileWifiAdapter extends BaseAdapter {
    private ArrayList<ScanResult> scanWifiList = new ArrayList<ScanResult>();
    private Context mContext = null;
    private LayoutInflater inflater;
    private int wifiIndex = -1;
    private String oldWifi = "";

    public MobileWifiAdapter(Context con) {
        mContext = con;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(ArrayList<ScanResult> list, String wifi) {
        scanWifiList = list;
        oldWifi = wifi;
    }

    public int getOldWifiIndex() {
        return wifiIndex;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        int size = 0;
        if (null != scanWifiList && 0 != scanWifiList.size()) {
            size = scanWifiList.size();
        }
        return size;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        Object obj = null;
        if (null != scanWifiList && 0 != scanWifiList.size()) {
            obj = scanWifiList.get(position);
        }
        return obj;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @SuppressWarnings("deprecation")
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        WifiHolder wifiHolder = null;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.wifi_item, null);
            wifiHolder = new WifiHolder();
            wifiHolder.wifiName = (TextView) convertView
                    .findViewById(R.id.videodate);
            wifiHolder.wifiImg = (ImageView) convertView
                    .findViewById(R.id.wifistate);
            convertView.setTag(wifiHolder);
        } else {
            wifiHolder = (WifiHolder) convertView.getTag();
        }

        // 与当前网络一致
        if (oldWifi.equalsIgnoreCase(scanWifiList.get(position).SSID)) {
            wifiIndex = position;
            wifiHolder.wifiName.setTextColor(mContext.getResources().getColor(
                    R.color.string_content));
        } else {
            wifiHolder.wifiName.setTextColor(mContext.getResources().getColor(
                    R.color.black));
        }

        wifiHolder.wifiName.setText(scanWifiList.get(position).SSID);
        // if (wifiIndex == position) {
        // wifiHolder.wifiImg.setBackgroundDrawable(mContext.getResources()
        // .getDrawable(R.drawable.wifi_flag_open_bg));
        // } else {
        // wifiHolder.wifiImg.setBackgroundDrawable(mContext.getResources()
        // .getDrawable(R.drawable.wifi_flag_close_bg));
        // }

        return convertView;
    }

    class WifiHolder {
        TextView wifiName;
        ImageView wifiImg;
    }

}
