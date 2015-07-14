
package com.jovision.activities;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import cn.tmway.temsee.R;

import com.jovision.activities.JVTabActivity.OnMainListener;
import com.jovision.adapters.MediaSelectorAdapter;
import com.jovision.utils.CommonUtil;

/**
 * 影像
 * @author HYONG
 *
 */
public class JVMediaManageFragment extends BaseFragment implements
        OnMainListener {
    /** topBar **/
    private ListView mediaListView;
    private MediaSelectorAdapter msAdapter;
    private ArrayList<String> mediaList = new ArrayList<String>();

    @Override
    public void onHandler(int what, int arg1, int arg2, Object obj) {

    }
    
    @Override
    public void onNotify(int what, int arg1, int arg2, Object obj) {

    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mediaselector_layout, container,
                false);
        /** topBar **/
        leftBtn = (Button) view.findViewById(R.id.btn_left);
        alarmnet = (RelativeLayout) view.findViewById(R.id.alarmnet);
        accountError = (TextView) view.findViewById(R.id.accounterror);
        currentMenu = (TextView) view.findViewById(R.id.currentmenu);
        currentMenu.setText(R.string.media);
        rightBtn = (Button) view.findViewById(R.id.btn_right);
        rightBtn.setVisibility(View.GONE);
        leftBtn.setVisibility(View.GONE);
        mediaListView = (ListView) view.findViewById(R.id.medialistview);
        msAdapter = new MediaSelectorAdapter(getActivity());
        msAdapter.setData(mediaList);
        mediaListView.setAdapter(msAdapter);
        mediaListView.setOnItemClickListener(mOnItemClickListener);
        JVMediaListActivity.fileMap.clear();
        // 放大左侧箭头的点击区域
        CommonUtil.expandViewTouchDelegate(leftBtn, CommonUtil.dp2px(getActivity(), 50));
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initSettings();
        leftBtn.setOnClickListener(null);
    }

	@Override
	public void onMainAction(int count) {
		// TODO Auto-generated method stub
		
	}
	
    public void initSettings() {
        if (null == mediaList) {
            mediaList = new ArrayList<String>();
        }
        mediaList.clear();
        mediaList.add(getResources().getString(R.string.media_image));
        mediaList.add(getResources().getString(R.string.media_video));
        mediaList.add(getResources().getString(R.string.media_downvideo));
    }

    OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                long arg3) {
            // createDialog("", false);
            Intent mediaListIntent = new Intent();
            mediaListIntent.setClass(getActivity(),
                    JVMediaListActivity.class);
            switch (arg2) {
                case 0: {
                    mediaListIntent.putExtra("Media", "image");
                    break;
                }
                case 1: {
                    mediaListIntent.putExtra("Media", "video");
                    break;
                }
                case 2: {
                    mediaListIntent.putExtra("Media", "downVideo");
                    break;
                }
            }
            JVMediaManageFragment.this.startActivity(mediaListIntent);
        }

    };
}
