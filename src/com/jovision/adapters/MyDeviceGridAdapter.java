
package com.jovision.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.bean.Device;

import java.util.ArrayList;

public class MyDeviceGridAdapter extends BaseAdapter {
    private ArrayList<Device> deviceList;
    private Context mContext;
    private LayoutInflater inflater;

    public MyDeviceGridAdapter(Context con) {
        mContext = con;
        inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void setData(ArrayList<Device> dataList) {
        deviceList = dataList;
    }

    @Override
    public int getCount() {
        return deviceList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return deviceList.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DeviceHolder deviceHolder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.mydevice_grid_item, null);
            deviceHolder = new DeviceHolder();
            deviceHolder.devName = (TextView) convertView
                    .findViewById(R.id.device_name);
            deviceHolder.onLineState = (TextView) convertView
                    .findViewById(R.id.device_online);
            deviceHolder.wifiState = (TextView) convertView
                    .findViewById(R.id.device_wifi);
            deviceHolder.devImg = (ImageView) convertView
                    .findViewById(R.id.device_image);
            convertView.setTag(deviceHolder);
        } else {
            deviceHolder = (DeviceHolder) convertView.getTag();
        }
        if (2 == deviceList.get(position).getIsDevice()) {
            deviceHolder.devName.setText(deviceList.get(position).getDoMain());
        } else {
            deviceHolder.devName
                    .setText(deviceList.get(position).getNickName());
        }

        deviceHolder.onLineState.setText("在线");
        deviceHolder.wifiState.setText("wifi");
        // deviceHolder.devName.setText(position+"");
        // deviceHolder.onLineState.setText(position+"");
        // deviceHolder.wifiState.setText(position+"");
        return convertView;
    }

    class DeviceHolder {
        TextView devName;
        TextView onLineState;
        TextView wifiState;
        ImageView devImg;
    }

}
