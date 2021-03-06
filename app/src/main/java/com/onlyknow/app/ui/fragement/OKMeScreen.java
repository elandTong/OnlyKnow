package com.onlyknow.app.ui.fragement;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onlyknow.app.R;
import com.onlyknow.app.api.OKServiceResult;
import com.onlyknow.app.api.user.OKManagerUserApi;
import com.onlyknow.app.db.bean.OKUserInfoBean;
import com.onlyknow.app.ui.OKBaseFragment;
import com.onlyknow.app.ui.activity.OKDragPhotoActivity;
import com.onlyknow.app.ui.activity.OKGoodsActivity;
import com.onlyknow.app.ui.activity.OKLoginActivity;
import com.onlyknow.app.ui.activity.OKSettingActivity;
import com.onlyknow.app.ui.activity.OKUserEditActivity;
import com.onlyknow.app.ui.adapter.OKFragmentPagerAdapter;
import com.onlyknow.app.ui.fragement.me.OKApproveFragment;
import com.onlyknow.app.ui.fragement.me.OKAttentionFragment;
import com.onlyknow.app.ui.fragement.me.OKCommentFragment;
import com.onlyknow.app.ui.fragement.me.OKDynamicFragment;
import com.onlyknow.app.ui.fragement.me.OKWatchFragment;
import com.onlyknow.app.ui.view.OKCircleImageView;
import com.onlyknow.app.ui.view.OKSEImageView;
import com.onlyknow.app.utils.OKDateUtil;

import java.util.ArrayList;
import java.util.List;

public class OKMeScreen extends OKBaseFragment implements AppBarLayout.OnOffsetChangedListener, NavigationView.OnNavigationItemSelectedListener, OKManagerUserApi.onCallBack {
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ImageView mImageViewHead;
    private OKCircleImageView imageViewTX;
    private ImageView imageViewSex;
    private TextView textViewName, textViewQianMin, textViewGuanZhu, textViewShouChan, textViewJiFeng;
    private LinearLayout linearLayoutJiFeng;
    private OKSEImageView menu_OKSEImageView, edit_OKSEImageView;
    private DrawerLayout mDrawerLayout;
    private DrawerToggle mDrawerToggle;
    private NavigationView mNavigationView;

    private OKFragmentPagerAdapter mFragmentPagerAdapter;
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mTabNameList = new ArrayList<>();

    // 动态,关注,收藏 fragment
    private final OKDynamicFragment mDynamicFragment = new OKDynamicFragment();
    private final OKAttentionFragment mAttentionFragment = new OKAttentionFragment();
    private final OKWatchFragment mWatchFragment = new OKWatchFragment();
    private final OKCommentFragment mCommentFragment = new OKCommentFragment();
    private final OKApproveFragment mApproveFragment = new OKApproveFragment();

    private OKManagerUserApi okManagerUserApi;

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.ok_fragment_me, container, false);
            initUserBody();
            findView(rootView);

            init();
            bindUserInfo();
            return rootView;
        } else {
            init();
            return rootView;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        appBarLayout.addOnOffsetChangedListener(this);

        if (USER_BODY.getBoolean("STATE_CHANGE", false)) {
            bindUserInfo();
            USER_BODY.edit().putBoolean("STATE_CHANGE", false).commit();
        }

        if (USER_BODY.getBoolean("STATE", false) && !TextUtils.isEmpty(USER_BODY.getString(OKUserInfoBean.KEY_USERNAME, ""))) {
            OKManagerUserApi.Params params = new OKManagerUserApi.Params();
            params.setUsername(USER_BODY.getString(OKUserInfoBean.KEY_USERNAME, ""));
            params.setPassword(USER_BODY.getString(OKUserInfoBean.KEY_PASSWORD, ""));
            params.setType(OKManagerUserApi.Params.TYPE_GET_INFO);

            okManagerUserApi = new OKManagerUserApi(getActivity());
            okManagerUserApi.requestManagerUser(params, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //如果异步任务不为空并且状态是运行时,就把他取消这个加载任务
        if (okManagerUserApi != null) {
            okManagerUserApi.cancelTask();
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        appBarLayout.removeOnOffsetChangedListener(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (rootView != null) {
            ((ViewGroup) rootView.getParent()).removeView(rootView);
        }
    }

    private void bindUserInfo() {
        if (USER_BODY.getBoolean("STATE", false)) {
            // 用户信息设置
            String url = USER_BODY.getString(OKUserInfoBean.KEY_HEAD_PORTRAIT_URL, "");
            GlideRoundApi(imageViewTX, url, R.drawable.touxian_placeholder_hd, R.drawable.touxian_placeholder_hd);
            GlideBlurApi(mImageViewHead, url, R.drawable.topgd3, R.drawable.topgd3);
            if (USER_BODY.getString(OKUserInfoBean.KEY_SEX, "").equals("NAN")) {
                imageViewSex.setVisibility(View.VISIBLE);
                GlideApi(imageViewSex, R.drawable.nan, R.drawable.nan, R.drawable.nan);
            } else {
                imageViewSex.setVisibility(View.VISIBLE);
                GlideApi(imageViewSex, R.drawable.nv, R.drawable.nv, R.drawable.nv);
            }
            textViewName.setText(USER_BODY.getString(OKUserInfoBean.KEY_NICKNAME, "您还未登录"));
            String qm = USER_BODY.getString(OKUserInfoBean.KEY_TAG, "");
            if (!TextUtils.isEmpty(qm) && !qm.equals("NULL")) {
                textViewQianMin.setText(qm);
            } else {
                textViewQianMin.setText("这个人很懒，什么都没有留下 !");
            }
            textViewGuanZhu.setText("" + USER_BODY.getInt(OKUserInfoBean.KEY_ME_ATTENTION, 0));
            textViewShouChan.setText("" + USER_BODY.getInt(OKUserInfoBean.KEY_ME_WATCH, 0));
            textViewJiFeng.setText("" + USER_BODY.getInt(OKUserInfoBean.KEY_INTEGRAL, 0));
        } else {
            imageViewSex.setVisibility(View.INVISIBLE);
            textViewName.setText("您还未登录");
            textViewQianMin.setText("这个人很懒，什么都没有留下 !");
            textViewGuanZhu.setText("0");
            textViewShouChan.setText("0");
            textViewJiFeng.setText("0");
            GlideRoundApi(imageViewTX, R.drawable.touxian_placeholder_hd, R.drawable.touxian_placeholder_hd, R.drawable.touxian_placeholder_hd);
            GlideBlurApi(mImageViewHead, R.drawable.topgd3, R.drawable.topgd3, R.drawable.topgd3);
        }
    }

    @SuppressLint("ResourceType")
    private void init() {
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);// 设置TabLayout的模式
        mTabLayout.addTab(mTabLayout.newTab().setText("动态"));
        mTabLayout.addTab(mTabLayout.newTab().setText("关注"));
        mTabLayout.addTab(mTabLayout.newTab().setText("收藏"));
        mTabLayout.addTab(mTabLayout.newTab().setText("评论"));
        mTabLayout.addTab(mTabLayout.newTab().setText("审批"));
        mFragmentList.clear();
        mTabNameList.clear();
        // 简单创建一个FragmentPagerAdapter
        mFragmentList.add(mDynamicFragment);
        mFragmentList.add(mAttentionFragment);
        mFragmentList.add(mWatchFragment);
        mFragmentList.add(mCommentFragment);
        mFragmentList.add(mApproveFragment);
        mTabNameList.add("动态");
        mTabNameList.add("关注");
        mTabNameList.add("收藏");
        mTabNameList.add("评论");
        mTabNameList.add("审批");
        mFragmentPagerAdapter = new OKFragmentPagerAdapter(getChildFragmentManager(), mFragmentList, mTabNameList);
        mViewPager.setAdapter(mFragmentPagerAdapter);
        mViewPager.setOffscreenPageLimit(5);
        mTabLayout.setupWithViewPager(mViewPager);
        mNavigationView.setNavigationItemSelectedListener(this);
        mDrawerToggle = new DrawerToggle(mDrawerLayout, toolbar);
        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        bindNavigationHeadView(mNavigationView.getHeaderView(0));

        // 监听器
        imageViewTX.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (USER_BODY.getBoolean("STATE", false)) {
                    int location[] = new int[2];
                    imageViewTX.getLocationOnScreen(location);

                    Bundle mBundle = new Bundle();
                    mBundle.putInt("left", location[0]);
                    mBundle.putInt("top", location[1]);
                    mBundle.putInt("height", imageViewTX.getHeight());
                    mBundle.putInt("width", imageViewTX.getWidth());
                    String url = USER_BODY.getString(OKUserInfoBean.KEY_HEAD_PORTRAIT_URL, "");
                    mBundle.putString("url", url);

                    startUserActivity(mBundle, OKDragPhotoActivity.class);
                    getActivity().overridePendingTransition(0, 0);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), OKLoginActivity.class);
                    getActivity().startActivity(intent);
                }
            }
        });

        linearLayoutJiFeng.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (USER_BODY.getBoolean("STATE", false)) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), OKGoodsActivity.class);
                    getActivity().startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), OKLoginActivity.class);
                    getActivity().startActivity(intent);
                }
            }
        });

        edit_OKSEImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (USER_BODY.getBoolean("STATE", false)) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), OKUserEditActivity.class);
                    getActivity().startActivity(intent);
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), OKLoginActivity.class);
                    getActivity().startActivity(intent);
                }
            }
        });

        menu_OKSEImageView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                showMainMenu();
            }
        });
    }

    private void findView(View rootView) {
        toolbar = (Toolbar) rootView.findViewById(R.id.ME_toolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        appBarLayout = (AppBarLayout) rootView.findViewById(R.id.ME_app_bar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.ME_toolbar_layout);
        mImageViewHead = (ImageView) rootView.findViewById(R.id.ME_toplayout_image);
        imageViewTX = (OKCircleImageView) rootView.findViewById(R.id.ME_touxiang_imag);
        imageViewSex = (ImageView) rootView.findViewById(R.id.ME_sex_imag);
        textViewName = (TextView) rootView.findViewById(R.id.ME_name_text);
        textViewQianMin = (TextView) rootView.findViewById(R.id.ME_qianmin_text);
        textViewGuanZhu = (TextView) rootView.findViewById(R.id.ME_guanzhu_num_text);
        textViewShouChan = (TextView) rootView.findViewById(R.id.ME_shouchan_num_text);
        textViewJiFeng = (TextView) rootView.findViewById(R.id.ME_jifeng_text);
        linearLayoutJiFeng = (LinearLayout) rootView.findViewById(R.id.ME_jifeng_layout);
        edit_OKSEImageView = (OKSEImageView) rootView.findViewById(R.id.ME_topbtnRight);
        menu_OKSEImageView = (OKSEImageView) rootView.findViewById(R.id.ME_topbtnSet);
        mViewPager = (ViewPager) rootView.findViewById(R.id.ME_viewpager);
        mTabLayout = (TabLayout) rootView.findViewById(R.id.ME_tabs);
        mDrawerLayout = (DrawerLayout) rootView.findViewById(R.id.ok_fragment_me_drawerLayout);
        mNavigationView = (NavigationView) rootView.findViewById(R.id.ok_fragment_me_NavigationView);

        mCollapsingToolbarLayout.setTitle(" ");
    }

    @Override
    public void onOffsetChanged(AppBarLayout arg0, int pos) {
        // 解决与刷新控件的冲突
        if (pos == 0) {
            mDynamicFragment.setSwipeRefreshEnabled(true);
            mAttentionFragment.setSwipeRefreshEnabled(true);
            mWatchFragment.setSwipeRefreshEnabled(true);
            mCommentFragment.setSwipeRefreshEnabled(true);
            mApproveFragment.setSwipeRefreshEnabled(true);
        } else {
            mDynamicFragment.setSwipeRefreshEnabled(false);
            mAttentionFragment.setSwipeRefreshEnabled(false);
            mWatchFragment.setSwipeRefreshEnabled(false);
            mCommentFragment.setSwipeRefreshEnabled(false);
            mApproveFragment.setSwipeRefreshEnabled(false);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.ok_menu_drawer_FuLi:
                startGanKioActivity(GAN_KIO_TYPE_FL);
                break;
            case R.id.ok_menu_drawer_Video:
                startGanKioActivity(GAN_KIO_TYPE_VIDEO);
                break;
            case R.id.ok_menu_drawer_ExtensionRes:
                startGanKioActivity(GAN_KIO_TYPE_RES);
                break;
            case R.id.ok_menu_drawer_Android:
                startGanKioActivity(GAN_KIO_TYPE_ANDROID);
                break;
            case R.id.ok_menu_drawer_IOS:
                startGanKioActivity(GAN_KIO_TYPE_IOS);
                break;
            case R.id.ok_menu_drawer_QianDuan:
                startGanKioActivity(GAN_KIO_TYPE_H5);
                break;
            case R.id.ok_menu_drawer_Setting:
                startUserActivity(null, OKSettingActivity.class);
                break;
            default:
                break;
        }
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void managerUserComplete(OKServiceResult<Object> result, String type, int pos) {
        if (OKManagerUserApi.Params.TYPE_GET_INFO.equals(type)) {
            if (result == null || !result.isSuccess()) {
                showSnackBar(rootView, "没有获取到用户信息", "");
                return;
            }

            OKUserInfoBean userInfoBean = (OKUserInfoBean) result.getData();

            if (userInfoBean == null) return;

            if (!userInfoBean.getHeadPortraitUrl().equals(USER_BODY.getString(OKUserInfoBean.KEY_HEAD_PORTRAIT_URL, ""))) {
                GlideRoundApi(imageViewTX, userInfoBean.getHeadPortraitUrl(), R.drawable.touxian_placeholder_hd, R.drawable.touxian_placeholder_hd);
                GlideBlurApi(mImageViewHead, userInfoBean.getHeadPortraitUrl(), R.drawable.topgd3, R.drawable.topgd3);
            }

            textViewName.setText(userInfoBean.getUserNickname());

            if (!TextUtils.isEmpty(userInfoBean.getTag())) {
                textViewQianMin.setText(userInfoBean.getTag());
            } else {
                textViewQianMin.setText("这个人很懒，什么都没有留下 !");
            }

            imageViewSex.setVisibility(View.VISIBLE);
            if (userInfoBean.getSex().equalsIgnoreCase("NAN")) {
                GlideApi(imageViewSex, R.drawable.nan, R.drawable.nan, R.drawable.nan);
            } else {
                GlideApi(imageViewSex, R.drawable.nv, R.drawable.nv, R.drawable.nv);
            }

            textViewShouChan.setText("" + userInfoBean.getMeWatch());
            textViewGuanZhu.setText("" + userInfoBean.getMeAttention());
            textViewJiFeng.setText("" + userInfoBean.getMeIntegral());

            // 保存用户信息
            SharedPreferences.Editor editor = USER_BODY.edit();
            editor.putInt(OKUserInfoBean.KEY_USER_ID, userInfoBean.getUserId());
            editor.putString(OKUserInfoBean.KEY_USERNAME, userInfoBean.getUserName());
            editor.putString(OKUserInfoBean.KEY_NICKNAME, userInfoBean.getUserNickname());
            editor.putString(OKUserInfoBean.KEY_PHONE, userInfoBean.getUserPhone());
            editor.putString(OKUserInfoBean.KEY_EMAIL, userInfoBean.getUserEmail());
            editor.putString(OKUserInfoBean.KEY_TAG, userInfoBean.getTag());
            editor.putString(OKUserInfoBean.KEY_SEX, userInfoBean.getSex());
            editor.putString(OKUserInfoBean.KEY_BIRTH_DATE, OKDateUtil.stringByDate(userInfoBean.getBirthDate()));
            editor.putInt(OKUserInfoBean.KEY_AGE, userInfoBean.getAge());
            editor.putString(OKUserInfoBean.KEY_RE_DATE, OKDateUtil.stringByDate(userInfoBean.getReDate()));
            editor.putInt(OKUserInfoBean.KEY_ME_WATCH, userInfoBean.getMeWatch());
            editor.putInt(OKUserInfoBean.KEY_ME_ATTENTION, userInfoBean.getMeAttention());
            editor.putInt(OKUserInfoBean.KEY_INTEGRAL, userInfoBean.getMeIntegral());
            editor.putInt(OKUserInfoBean.KEY_ARTICLE, userInfoBean.getMeArticle());
            editor.putString(OKUserInfoBean.KEY_HEAD_PORTRAIT_URL, userInfoBean.getHeadPortraitUrl());
            editor.putString(OKUserInfoBean.KEY_HOME_PAGE_URL, userInfoBean.getHomepageUrl());
            editor.putString(OKUserInfoBean.KEY_EDIT_DATE, OKDateUtil.stringByDate(userInfoBean.getEditDate()));
            editor.commit();
        }
    }
}
