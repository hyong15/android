
package com.jovision.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.RelativeLayout;
import android.widget.Toast;

import cn.tmway.temsee.R;

/**
 * 读取网页二维码弹出菜单，选项：读取，取消
 */
public class JVReadBBSQRPopViewActivity extends Activity implements
        OnClickListener {
    private final static String TAG = "JVReadBBSQRPopViewActivity";
    private RelativeLayout cancle, readQR;
    public static String qrContent = "";// 去WebViewActivity接受数据

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.read_bbsqr_popview_layout);
        cancle = (RelativeLayout) findViewById(R.id.read_cancel);
        readQR = (RelativeLayout) findViewById(R.id.read_qr_verify);

        readQR.setOnClickListener(this);
        cancle.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        // TODO 自动生成的方法存根
        switch (v.getId()) {

            case R.id.read_qr_verify:
                // 方法一 用默认浏览器打开扫描得到的地址
                if (qrContent.equals("")) {
                    Toast.makeText(JVReadBBSQRPopViewActivity.this, "图片解析错误！",
                            Toast.LENGTH_LONG).show();
                    return;
                } else {
                    Uri uri = Uri.parse(qrContent);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
                this.finish();
                // 方法二webview显示
                // if (path.equals("")) {
                // Toast.makeText(PopViewActivity.this, "图片解析错误！",
                // Toast.LENGTH_LONG).show();
                // return;
                // } else {
                // Intent intent = new Intent();
                // intent.setClass(PopViewActivity.this,
                // ShowQRByWebviewActivity.class);
                // intent.putExtra("path", path);
                // startActivity(intent);
                // }
                break;
            case R.id.read_cancel:
                qrContent = "";
                JVReadBBSQRPopViewActivity.this.finish();
                this.finish();
                break;
        }
    }
}
