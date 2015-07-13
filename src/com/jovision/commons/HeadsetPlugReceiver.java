
package com.jovision.commons;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.jovision.Consts;
import com.jovision.activities.BaseActivity;

public class HeadsetPlugReceiver extends BroadcastReceiver {
    private static final String TAG = "HeadsetPlugReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra("state")) {
            if (intent.getIntExtra("state", 0) == 0) {
                Consts.PLAY_HEADSET_FLAG = false;
                ((BaseActivity) context).onNotify(Consts.PLAY_HEADSET,
                        Consts.PLAY_HEADSET_OUT, 0, null);
            } else if (intent.getIntExtra("state", 0) == 1) {
                Consts.PLAY_HEADSET_FLAG = true;
                ((BaseActivity) context).onNotify(Consts.PLAY_HEADSET,
                        Consts.PLAY_HEADSET_IN, 0, null);
            }
        }
    }
}
