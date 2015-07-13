
package com.jovision.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.tmway.temsee.R;
import com.jovision.Consts;
import com.jovision.activities.BaseActivity;
import com.jovision.activities.BaseFragment;
import com.jovision.bean.MoreFragmentBean;
import com.jovision.commons.MySharedPreference;
import com.jovision.utils.ConfigUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 个人中心(原有更多)及里面的二级页面全部使用这个适配器
 */
public class FragmentAdapter extends BaseAdapter {
    private List<MoreFragmentBean> dataList;
    // 访客/非访客标志
    private boolean mLocalFlag;
    // Activity
    private Activity mActivity;
    // 报警消息
    private int new_nums_;
    private int new_bbsnums_;

    // private boolean showGCS = false;// 是否显示工程

    public FragmentAdapter(BaseFragment mfragment,
            List<MoreFragmentBean> dataList) {
        if (dataList != null) {
            this.dataList = dataList;
        } else {
            this.dataList = new ArrayList<MoreFragmentBean>();
        }
        mActivity = mfragment.getActivity();
        mLocalFlag = Boolean.valueOf(((BaseActivity) mActivity).statusHashMap
                .get(Consts.LOCAL_LOGIN));
    }

    public FragmentAdapter(Activity activity, List<MoreFragmentBean> dataList) {
        mActivity = activity;
        if (dataList != null) {
            this.dataList = dataList;
        } else {
            this.dataList = new ArrayList<MoreFragmentBean>();
        }
        mLocalFlag = Boolean.valueOf(((BaseActivity) mActivity).statusHashMap
                .get(Consts.LOCAL_LOGIN));
    }

    // public void setShowGCS(boolean show) {
    // this.showGCS = show;
    // }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setNewNums(int nums) {
        this.new_nums_ = nums;
    }

    public void setBBSNums(int nums) {
        this.new_bbsnums_ = nums;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mActivity).inflate(
                    R.layout.fragment_more_item, null);
            holder = new ViewHolder();

            holder.itemNewRlyt = (RelativeLayout) convertView
                    .findViewById(R.id.item_new_layout);
            holder.dividerTop = (View) convertView
                    .findViewById(R.id.divider_top);
            holder.blank = (View) convertView.findViewById(R.id.blank);
            holder.itemFlyt = (FrameLayout) convertView.findViewById(R.id.item);
            holder.dividerBottom = (View) convertView
                    .findViewById(R.id.divider_bottom);
            holder.dividerBottomMatch = (View) convertView
                    .findViewById(R.id.divider_bottom_match);
            holder.itemImg = (ImageView) convertView
                    .findViewById(R.id.item_img);
            holder.itemArrowImg = (ImageView) convertView
                    .findViewById(R.id.item_next);
            holder.itemTv = (TextView) convertView.findViewById(R.id.item_name);
            holder.itemVersion = (TextView) convertView
                    .findViewById(R.id.item_version);
            holder.itemNewTv = (TextView) convertView
                    .findViewById(R.id.tv_item_new_nums);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.itemImg.setBackgroundResource(dataList.get(position)
                .getItem_img());
        holder.itemTv.setText(dataList.get(position).getName());

        /** 显示白框 **/
        if (dataList.get(position).isShowWhiteBlock()) {
            holder.blank.setVisibility(View.VISIBLE);
            holder.dividerTop.setVisibility(View.VISIBLE);
        }

        /** 设置隐藏 **/
        // 隐藏开关
        if (dataList.get(position).isDismiss()) {
            holder.itemFlyt.setVisibility(View.GONE);
        } else {
            holder.itemFlyt.setVisibility(View.VISIBLE);
        }
        // 访客的时候是否隐藏(非访客不起作用)
        if (mLocalFlag && dataList.get(position).isLocalDismiss()) {
            holder.itemFlyt.setVisibility(View.GONE);
            holder.blank.setVisibility(View.GONE);
            holder.dividerTop.setVisibility(View.GONE);
        }

        /** 显示右侧圆形按钮 **/
        if (dataList.get(position).isShowRightCircleBtn()) {
            boolean defaultValue = false;
            if (Consts.MORE_DEVICE_SCENESWITCH.equalsIgnoreCase(dataList.get(
                    position).getItemFlag())) {
                defaultValue = true;
            }
            if (MySharedPreference.getBoolean(dataList.get(position)
                    .getItemFlag(), defaultValue)) {
                holder.itemArrowImg
                        .setBackgroundResource(R.drawable.morefragment_selector_icon);
            } else {
                holder.itemArrowImg
                        .setBackgroundResource(R.drawable.morefragment_normal_icon);
            }
        }

        /** 是否显示”新“ **/
        if (dataList.get(position).isIsnew()) {
            if (!MySharedPreference.getBoolean(dataList.get(position)
                    .getItemFlag())) {
                holder.itemNewTv.setText(R.string.new_tag);
                holder.itemNewRlyt.setVisibility(View.VISIBLE);
            } else {
                holder.itemNewTv.setText("");
                holder.itemNewRlyt.setVisibility(View.GONE);
            }
        }

        /** 显示信息数目 **/
        if (dataList.get(position).isShowTVNews()) {
            if (!mLocalFlag) {
                if (new_nums_ > 0) {
                    holder.itemNewTv.setText(String.valueOf(new_nums_));
                    holder.itemNewRlyt.setVisibility(View.VISIBLE);
                } else {
                    holder.itemNewTv.setText("0");
                    holder.itemNewRlyt.setVisibility(View.INVISIBLE);
                }
            } else {
                holder.itemNewRlyt.setVisibility(View.INVISIBLE);
            }
        }
        /** 显示论坛信息数目 **/
        if (dataList.get(position).isShowBBSNews()) {
            if (new_bbsnums_ > 0) {
                holder.itemNewTv.setText(String.valueOf(new_bbsnums_));
                holder.itemNewRlyt.setVisibility(View.VISIBLE);
            } else {
                holder.itemNewTv.setText("0");
                holder.itemNewRlyt.setVisibility(View.INVISIBLE);
            }
        }
        /** 显示版本信息 **/
        if (dataList.get(position).isShowVersion()) {
            holder.itemArrowImg.setVisibility(View.GONE);
            holder.itemVersion.setVisibility(View.VISIBLE);
            holder.itemVersion.setText(ConfigUtil.getVersion(mActivity));
        }

        /** 当前模块内最后一项元素的底部分隔线为长线 **/
        if (dataList.get(position).isLast()) {
            holder.dividerBottom.setVisibility(View.GONE);
            holder.dividerBottomMatch.setVisibility(View.VISIBLE);
        }

        // ------------------针对某一项进行设置------------------------
        // ## 这里面应该只判断MoreFragmentBean中的属性
        // ## 非特殊情况不要加其它的判断操作,把判断操作移到外面
        // -----------------------------------------------------------
        /** 注销登陆不显示右侧箭头 **/
        if (dataList.get(position).getItemFlag()
                .equals(Consts.PROFILE_ITEM_LOGOUT)) {
            holder.itemArrowImg.setVisibility(View.GONE);
        }

        /** 新消息右侧的NEW **/
        if (dataList.get(position).getItemFlag()
                .equals(Consts.PROFILE_ITEM_MSG)) {
            if (!mLocalFlag) {
                if (new_nums_ > 0) {
                    holder.itemNewTv.setText(R.string.new_tag);
                    holder.itemNewRlyt.setVisibility(View.VISIBLE);
                } else {
                    holder.itemNewTv.setText("");
                    holder.itemNewRlyt.setVisibility(View.GONE);
                }
            } else {
                holder.itemNewTv.setText("");
                holder.itemNewRlyt.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    static class ViewHolder {
        // 左侧图标
        private ImageView itemImg;
        // 功能名称
        private TextView itemTv;
        // 右侧箭头图标
        private ImageView itemArrowImg;
        // 版本信息
        private TextView itemVersion;
        // 整个item
        private FrameLayout itemFlyt;
        // 右侧提示布局(例如:NEW)
        private RelativeLayout itemNewRlyt;
        // 右侧提示布局上显示的信息
        private TextView itemNewTv;
        // 顶部预留空白,顶部和底部分隔线
        private View blank, dividerTop, dividerBottom, dividerBottomMatch;
    }

}
