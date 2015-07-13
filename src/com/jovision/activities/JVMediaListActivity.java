
package com.jovision.activities;

import android.content.Intent;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.adapters.MediaFolderAdapter;
import com.jovision.bean.Filebean;
import com.jovision.utils.BitmapCache;
import com.jovision.utils.CommonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JVMediaListActivity extends BaseActivity {
    public static HashMap<String, ArrayList<Filebean>> fileMap = new HashMap<String, ArrayList<Filebean>>();

    private String media = "";
    private String mediaPath = "";
    private boolean noFile = true;
    private ArrayList<File> fileList = new ArrayList<File>();
    private MediaFolderAdapter mfAdapter;

    /** topBar **/
    private boolean isdelect = true;
    private boolean isselectall;
    private RelativeLayout fileBottom;
    private TextView fileCompleted;
    private TextView fileCancel;
    private TextView fileSelectNum;
    private TextView fileNumber;
    private ImageView fileSlectAll;
    public static int fileSum = 0;
    public static int fileSelectSum = 0;
    private String selectNum;
    private String totalNum;
    private LinearLayout selectalllinear;
    public static ArrayList<String> delectlist = new ArrayList<String>();

    private RelativeLayout fileLayout;
    private ListView fileListView;
    private LinearLayout noFileLayout;

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {
        switch (what) {
            case Consts.WHAT_LOAD_IMAGE_SUCCESS: {
                // mfAdapter.setLoadImageIndex(arg1,true);
                // mfAdapter.notifyDataSetChanged();
                break;
            }
            case Consts.WHAT_DIALOG_CLOSE: {
                dismissDialog();
                break;
            }
            case Consts.WHAT_LOAD_IMAGE_FINISHED: {
                mfAdapter.setLoadImage(true);
                mfAdapter.notifyDataSetChanged();
                break;
            }
            case Consts.WHAT_FILE_LOAD_SUCCESS: {
                if (noFile) {
                    dismissDialog();
                    noFileLayout.setVisibility(View.VISIBLE);
                    fileLayout.setVisibility(View.GONE);
                } else {
                    noFileLayout.setVisibility(View.GONE);
                    fileLayout.setVisibility(View.VISIBLE);
                    mfAdapter.setLoadImage(false);
                    mfAdapter.setData(media, fileList, isdelect);
                    fileListView.setAdapter(mfAdapter);
                }
                break;
            }
            case Consts.WHAT_FILE_NUM:
                fileSelectNum.setText(selectNum.replace("?", String.valueOf(0)));
                fileNumber.setText(totalNum.replace("?", String.valueOf(arg1)));
                break;
            case Consts.WHAT_FILE_SUM:
                if (arg2 == 1) {
                    fileSlectAll
                            .setBackgroundResource(R.drawable.morefragment_selector_icon);
                    isselectall = true;
                } else if (arg2 == 0) {
                    fileSlectAll
                            .setBackgroundResource(R.drawable.morefragment_normal_icon);
                    isselectall = false;
                }
                fileSelectNum.setText(selectNum.replace("?",
                        String.valueOf(fileSelectSum)));
                break;
        }
    }

    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {
        handler.sendMessage(handler.obtainMessage(what, arg1, arg2, obj));
    }

    @Override
    protected void initSettings() {
        Intent intent = getIntent();
        media = intent.getStringExtra("Media");
        if ("image".equalsIgnoreCase(media)) {
            mediaPath = Consts.CAPTURE_PATH;
        } else if ("video".equalsIgnoreCase(media)) {
            mediaPath = Consts.VIDEO_PATH;
        } else if ("downVideo".equalsIgnoreCase(media)) {
            mediaPath = Consts.DOWNLOAD_VIDEO_PATH;
        }
    }

    @Override
    protected void initUi() {
        setContentView(R.layout.medialist_layout);

        /** topBar **/
        leftBtn = (Button) findViewById(R.id.btn_left);
        alarmnet = (RelativeLayout) findViewById(R.id.alarmnet);
        accountError = (TextView) findViewById(R.id.accounterror);
        selectNum = JVMediaListActivity.this.getResources().getString(
                R.string.selectnum);
        totalNum = JVMediaListActivity.this.getResources().getString(
                R.string.number);
        currentMenu = (TextView) findViewById(R.id.currentmenu);
        fileBottom = (RelativeLayout) findViewById(R.id.file_bottom);
        fileCompleted = (TextView) findViewById(R.id.file_completed);
        fileCancel = (TextView) findViewById(R.id.file_cancel);
        fileSelectNum = (TextView) findViewById(R.id.file_selectnum);
        fileNumber = (TextView) findViewById(R.id.file_number);
        fileSlectAll = (ImageView) findViewById(R.id.file_selectall);
        selectalllinear = (LinearLayout) findViewById(R.id.selectalllinear);

        if ("image".equalsIgnoreCase(media)) {
            currentMenu.setText(R.string.media_image);
        } else if ("video".equalsIgnoreCase(media)) {
            currentMenu.setText(R.string.media_video);
        } else if ("downVideo".equalsIgnoreCase(media)) {
            currentMenu.setText(R.string.media_downvideo);
        }
        rightBtn = (Button) findViewById(R.id.btn_right);
        rightBtn.setBackgroundResource(R.drawable.mydevice_cancale_icon);
        leftBtn.setOnClickListener(myOnClickListener);
        rightBtn.setOnClickListener(myOnClickListener);
        fileCompleted.setOnClickListener(myOnClickListener);
        fileCancel.setOnClickListener(myOnClickListener);
        selectalllinear.setOnClickListener(myOnClickListener);

        fileLayout = (RelativeLayout) findViewById(R.id.filelayout);
        noFileLayout = (LinearLayout) findViewById(R.id.nofilelayout);
        fileListView = (ListView) findViewById(R.id.filelistview);
        mfAdapter = new MediaFolderAdapter(JVMediaListActivity.this);
        createDialog("", true);
        LoadImageThread loadThread = new LoadImageThread();
        loadThread.start();

        // 放大左侧箭头的点击区域
        CommonUtil.expandViewTouchDelegate(leftBtn, CommonUtil.dp2px(this, 50));
    }

    OnClickListener myOnClickListener = new OnClickListener() {

        @Override
        public void onClick(View view) {

            switch (view.getId()) {
                case R.id.btn_right: {
                    if (isdelect) {
                        fileBottom.setVisibility(View.VISIBLE);
                        isdelect = !isdelect;
                        mfAdapter.setData(media, fileList, isdelect);
                        mfAdapter.notifyDataSetChanged();
                    } else {
                        fileBottom.setVisibility(View.GONE);
                        isselectall = false;
                        fileSlectAll
                                .setBackgroundResource(R.drawable.morefragment_normal_icon);
                        fileSelectNum.setText(selectNum.replace("?",
                                String.valueOf(0)));
                        isdelect = !isdelect;
                        HashMethod();
                        mfAdapter.setData(media, fileList, isdelect);
                        mfAdapter.notifyDataSetChanged();
                    }
                }
                    break;
                case R.id.btn_left: {
                    JVMediaListActivity.this.finish();
                    fileSum = 0;
                    fileSelectSum = 0;
                    break;
                }
                case R.id.selectalllinear:
                    if (!isselectall) {
                        fileSlectAll
                                .setBackgroundResource(R.drawable.morefragment_selector_icon);
                        Iterator iter = fileMap.entrySet().iterator();
                        while (iter.hasNext()) {
                            Map.Entry entry = (Map.Entry) iter.next();
                            Object key = entry.getKey();
                            Object val = entry.getValue();
                            for (int i = 0; i < fileMap.get(key).size(); i++) {
                                fileMap.get(key).get(i).setSelect(true);
                            }
                        }
                        fileSelectSum = fileSum;
                        fileSelectNum.setText(selectNum.replace("?",
                                String.valueOf(fileSum)));
                        isselectall = true;
                    } else {
                        fileSlectAll
                                .setBackgroundResource(R.drawable.morefragment_normal_icon);
                        HashMethod();
                        isselectall = false;
                        fileSelectNum.setText(selectNum.replace("?",
                                String.valueOf(0)));
                    }
                    mfAdapter.setData(media, fileList, isdelect);
                    mfAdapter.notifyDataSetChanged();
                    break;
                case R.id.file_cancel:
                    fileBottom.setVisibility(View.GONE);
                    isdelect = !isdelect;
                    HashMethod();
                    fileSelectNum
                            .setText(selectNum.replace("?", String.valueOf(0)));
                    fileSlectAll
                            .setBackgroundResource(R.drawable.morefragment_normal_icon);
                    isselectall = false;
                    mfAdapter.setData(media, fileList, isdelect);
                    mfAdapter.notifyDataSetChanged();
                    break;
                case R.id.file_completed:
                    Iterator iter = fileMap.entrySet().iterator();
                    while (iter.hasNext()) {
                        Map.Entry entry = (Map.Entry) iter.next();
                        Object key = entry.getKey();
                        Object val = entry.getValue();
                        for (int i = 0; i < fileMap.get(key).size(); i++) {
                            if (fileMap.get(key).get(i).isSelect()) {
                                JVMediaListActivity.delectlist.add(fileMap.get(key)
                                        .get(i).getFilename());
                            }
                        }
                    }
                    if (JVMediaListActivity.delectlist.size() != 0) {
                        for (int i = 0; i < JVMediaListActivity.delectlist.size(); i++) {
                            File file = new File(
                                    JVMediaListActivity.delectlist.get(i));
                            file.delete();
                        }
                        File file = new File(mediaPath);
                        if (file.exists()) {
                            File[] fileArray = file.listFiles();
                            for (int j = 0; j < fileArray.length; j++) {
                                Log.i("TAG", fileArray[j].getAbsolutePath());
                                if (null != fileArray[j]
                                        && null != fileArray[j].list()
                                        && fileArray[j].list().length == 0) {
                                    delete(fileArray[j]);
                                }
                            }
                        }
                        isdelect = true;
                        fileSum = 0;
                        fileSelectSum = 0;
                        fileList.clear();
                        delectlist.clear();
                        fileBottom.setVisibility(View.GONE);
                        fileSlectAll
                                .setBackgroundResource(R.drawable.morefragment_normal_icon);
                        LoadImageThread loadThread = new LoadImageThread();
                        loadThread.start();
                        if (file.list().length == 0) {
                            noFile = true;
                            noFileLayout.setVisibility(View.VISIBLE);
                            fileLayout.setVisibility(View.GONE);
                        }
                        mfAdapter.setData(media, fileList, true);
                        mfAdapter.notifyDataSetChanged();
                    } else {
                        JVMediaListActivity.this
                                .showTextToast(JVMediaListActivity.this
                                        .getResources().getString(
                                                R.string.media_noticedelect));
                    }
                    break;
            }
        }
    };

    public void HashMethod() {
        fileSelectSum = 0;
        Iterator iter = fileMap.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            for (int i = 0; i < fileMap.get(key).size(); i++) {
                fileMap.get(key).get(i).setSelect(false);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {
            fileSum = 0;
            HashMethod();
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public static void delete(File file) {
        if (file.isFile()) {
            file.delete();
            return;
        }
        if (file.isDirectory()) {
            File[] childFiles = file.listFiles();
            if (childFiles == null || childFiles.length == 0) {
                file.delete();
                return;
            }
            for (int i = 0; i < childFiles.length; i++) {
                delete(childFiles[i]);
            }
            file.delete();
        }
    }

    @Override
    protected void saveSettings() {

    }

    @Override
    protected void freeMe() {
        BitmapCache.getInstance().clearCache();
    }

    private class LoadImageThread extends Thread {

        @Override
        public void run() {
            try {
                File file = new File(mediaPath);
                if (file.exists()) {
                    File[] fileArray = file.listFiles();
                    if (null != fileArray && 0 != fileArray.length) {
                        int length = fileArray.length;
                        for (int i = 0; i < length; i++) {
                            if (fileArray[i].isDirectory()) {
                                ArrayList<Filebean> list = new ArrayList<Filebean>();
                                File[] summary = fileArray[i].listFiles();
                                if (fileArray[i].isDirectory()) {
                                    fileList.add(fileArray[i]);
                                    fileSum = fileSum
                                            + fileArray[i].list().length;
                                }
                                for (int j = 0; j < summary.length; j++) {
                                    Filebean bean = new Filebean();
                                    bean.setFilename(summary[j]
                                            .getAbsolutePath());
                                    bean.setSelect(false);
                                    list.add(bean);
                                }
                                fileMap.put(fileArray[i].getAbsolutePath(),
                                        list);
                            }
                        }
                        if (0 != fileList.size()) {// 有文件
                            noFile = true;
                            for (File eFile : fileList) {
                                if (eFile.isFile()) {
                                    noFile = false;
                                    break;
                                } else if (eFile.isDirectory()) {
                                    File[] eFileArray = eFile.listFiles();
                                    if (null != eFileArray
                                            && 0 != eFileArray.length) {
                                        noFile = false;
                                        break;
                                    }
                                }
                            }
                        } else {// 没文件
                            noFile = true;
                        }
                    }
                }

                if (noFile) {
                    handler.sendMessage(handler
                            .obtainMessage(Consts.WHAT_FILE_LOAD_SUCCESS));
                } else {
                    Collections.sort(fileList, comparator);
                    Message msg = new Message();
                    msg.arg1 = fileSum;
                    msg.what = Consts.WHAT_FILE_NUM;
                    handler.sendMessage(msg);
                    handler.sendMessage(handler
                            .obtainMessage(Consts.WHAT_FILE_LOAD_SUCCESS));
                    if (null != fileList && 0 != fileList.size()) {
                        int size = fileList.size();
                        for (int i = 0; i < size; i++) {
                            handler.sendMessage(handler.obtainMessage(
                                    Consts.WHAT_LOAD_IMAGE_SUCCESS, i, 0, null));
                            File[] fileArray = fileList.get(i).listFiles();
                            if (null != fileArray && 0 != fileArray.length) {
                                int length = fileArray.length;
                                for (int j = 0; j < length; j++) {
                                    BitmapCache.getInstance().getBitmap(
                                            fileArray[j].getAbsolutePath(),
                                            media, "");
                                }
                            }
                        }
                        handler.sendMessage(handler
                                .obtainMessage(Consts.WHAT_LOAD_IMAGE_FINISHED));
                    }
                }

                handler.sendMessageDelayed(
                        handler.obtainMessage(Consts.WHAT_DIALOG_CLOSE), 2000);

                super.run();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fileMap.clear();
    }

    // 文件按时间倒序排序
    static Comparator<File> comparator = new Comparator<File>() {
        public int compare(File f1, File f2) {
            if (f1 == null || f2 == null) {// 先比较null
                if (f1 == null) {
                    {
                        return -1;
                    }
                } else {
                    return 1;
                }
            } else {
                if (f1.isDirectory() == true && f2.isDirectory() == true) { // 再比较文件夹
                    return -f1.getName().compareToIgnoreCase(f2.getName());
                } else {
                    if ((f1.isDirectory() && !f2.isDirectory()) == true) {
                        return -1;
                    } else if ((f2.isDirectory() && !f1.isDirectory()) == true) {
                        return 1;
                    } else {
                        return -f1.getName().compareToIgnoreCase(f2.getName());// 最后比较文件
                    }
                }
            }
        }
    };

}
