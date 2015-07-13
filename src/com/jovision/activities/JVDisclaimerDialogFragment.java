
package com.jovision.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.commons.MyLog;

/**
 * 自定义的DialogFragment对话框
 */
public class JVDisclaimerDialogFragment extends DialogFragment {

    private final String TAG = "JVDisclaimerDialogFragment";

    public static JVDisclaimerDialogFragment newInstance(String title) {
        JVDisclaimerDialogFragment fragment = new JVDisclaimerDialogFragment();
        // 给fragment传参
        Bundle args = new Bundle();
        if (title != null && !title.equals("")) {
            args.putString("title", title);
        } else {
            args.putString("title", "");
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final String title = getArguments().getString("title");

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .create();

        // 设置返回按键监听
        alertDialog.setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    ((JVPlayActivity) getActivity()).doNegativeClick();
                    try {
                        JVDisclaimerDialogFragment.super.dismiss();
                    } catch (IllegalStateException e) {
                        MyLog.v(TAG,
                                "--keycode_back, dimiss IllegalStateException--");
                    }
                }
                return false;
            }

        });

        // 自定义布局
        alertDialog.setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                float height = getActivity().getResources().getDimension(
                        R.dimen.dialog_disclaimer_height);
                Window window = ((Dialog) dialog).getWindow();
                window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                        (int) height);
                window.setContentView(R.layout.dialog_disclaimer);

                // 标题
                TextView tv_title = (TextView) window
                        .findViewById(R.id.tv_dialog_title);
                tv_title.setText(title);

                WebView webview = (WebView) window
                        .findViewById(R.id.agreement_wv);
                webview.loadUrl("file:///android_asset/disclaimer.html");

                Button positive = (Button) window.findViewById(R.id.positive);
                Button negative = (Button) window.findViewById(R.id.negative);

                // 监听
                positive.setOnClickListener(clickListenerImpl);
                negative.setOnClickListener(clickListenerImpl);
            }

        });

        return alertDialog;
    }

    private OnClickListener clickListenerImpl = new OnClickListener() {

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.positive:
                    ((JVPlayActivity) getActivity()).doPositiveClick();
                    break;
                case R.id.negative:
                    ((JVPlayActivity) getActivity()).doNegativeClick();
                    break;
            }
            // 关闭对话框
            try {
                JVDisclaimerDialogFragment.super.dismiss();
            } catch (IllegalStateException e) {
                MyLog.v(TAG, "--click, dismiss IllegalStateException--");
            }
        }
    };

}
