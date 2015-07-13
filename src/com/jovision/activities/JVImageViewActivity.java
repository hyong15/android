
package com.jovision.activities;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.utils.BitmapCache;
import com.jovision.utils.ConfigUtil;
import com.jovision.views.TouchImageView;

import java.io.File;

public class JVImageViewActivity extends BaseActivity {

    private File[] fileArray;
    private int fileIndex;
    /** TopBar */

    private ViewPager imagePager;

    @Override
    protected void initSettings() {
        Intent intent = getIntent();
        String path = intent.getStringExtra("FolderPath");
        fileIndex = intent.getIntExtra("FileIndex", 0);

        if (null != path && !"".equalsIgnoreCase(path)) {
            File folder = new File(path);
            fileArray = folder.listFiles();
        }
    }

    @Override
    protected void initUi() {
        setContentView(R.layout.imageview_layout);
        leftBtn = (Button) findViewById(R.id.btn_left);
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        rightBtn = (Button) findViewById(R.id.btn_right);
        leftBtn.setVisibility(View.VISIBLE);
        rightBtn.setVisibility(View.GONE);
        // share.setBackgroundDrawable(getResources().getDrawable(
        // R.drawable.share));
        imagePager = (ViewPager) findViewById(R.id.imagepager);
        BrowseAdapter adapter = new BrowseAdapter(fileArray);
        imagePager.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        imagePager.setCurrentItem(fileIndex);
        leftBtn.setOnClickListener(mOnClickListener);
        rightBtn.setOnClickListener(mOnClickListener);

        currentMenu.setText((fileIndex + 1) + "/" + fileArray.length);

        imagePager.setOnPageChangeListener(new OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                // if(null != image.getChildAt(arg0)){
                // TouchImageView currentimage = (TouchImageView)
                // image.getChildAt(arg0).findViewById(R.id.currentimage);
                // currentimage.reset();
                // currentimage.setScaleType(ScaleType.CENTER_INSIDE);
                // }
                fileIndex = arg0;
                currentMenu.setText((fileIndex + 1) + "/" + fileArray.length);
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });
    }

    class BrowseAdapter extends PagerAdapter {
        private File[] filesArray;
        private LayoutInflater inflater;

        public BrowseAdapter(File[] fileArray) {
            filesArray = fileArray;
            inflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return filesArray.length;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            View imageLayout = inflater.inflate(R.layout.big_image, container,
                    false);
            final TouchImageView currentimage = (TouchImageView) imageLayout
                    .findViewById(R.id.currentimage);
            currentimage.setImageBitmap(BitmapFactory
                    .decodeFile(filesArray[position].getAbsolutePath()));
            currentimage.setMaxZoom(4f);
            ((ViewPager) container).addView(imageLayout, 0);
            return imageLayout;
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    OnClickListener mOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_left:
                    finish();
                    break;
                case R.id.btn_right:
                    ConfigUtil.shareTo(JVImageViewActivity.this,
                            fileArray[fileIndex].getAbsolutePath());
                    break;
                default:
                    break;
            }
        }

    };

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {

    }

    @Override
    protected void saveSettings() {

    }

    @Override
    protected void freeMe() {
        BitmapCache.getInstance().clearCache();
    }

}
