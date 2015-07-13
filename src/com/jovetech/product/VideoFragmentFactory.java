
package com.jovetech.product;

import android.support.v4.app.Fragment;

import com.jovision.activities.JVInfoFragment;
import com.jovision.activities.JVVideoFragment;

public class VideoFragmentFactory implements IFragmentFactory {

    @Override
    public Fragment newInstance() {
        // TODO Auto-generated method stub
        // return new VideoSquareFragment();
        //return new JVVideoFragment(); //HYONG 返回报警消息 界面 见下一行
    	JVInfoFragment jvInfoFragment = new JVInfoFragment();
    	jvInfoFragment.flag = true;
    	return jvInfoFragment;
    }

    @Override
    public ITabItem getTab() {
        // TODO Auto-generated method stub
        return new VideoTab();
    }

}
