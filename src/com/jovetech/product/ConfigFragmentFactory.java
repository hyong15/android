
package com.jovetech.product;

import android.support.v4.app.Fragment;

import com.jovision.activities.JVDeviceManageFragment;
import com.jovision.activities.JVMediaManageFragment;

public class ConfigFragmentFactory implements IFragmentFactory {

    @Override
    public Fragment newInstance() {
        // TODO Auto-generated method stub
        // return new ConfigurationFragment();
        // return new JVDeviceManageFragment();配置换成影像
        return new JVMediaManageFragment();
    }

    @Override
    public ITabItem getTab() {
        // TODO Auto-generated method stub
        return new ConfigTab();
    }

}
