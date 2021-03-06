package com.onlyknow.app.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.onlyknow.app.OKConstant;
import com.onlyknow.app.R;
import com.onlyknow.app.api.OKServiceResult;
import com.onlyknow.app.api.card.OKManagerCardApi;
import com.onlyknow.app.api.user.OKManagerUserApi;
import com.onlyknow.app.db.OKDatabaseHelper;
import com.onlyknow.app.db.bean.OKCardBean;
import com.onlyknow.app.db.bean.OKCardRelatedBean;
import com.onlyknow.app.db.bean.OKUserInfoBean;
import com.onlyknow.app.ui.OKBaseActivity;
import com.onlyknow.app.ui.view.OKCircleImageView;
import com.onlyknow.app.ui.view.OKSEImageView;
import com.onlyknow.app.utils.OKDateUtil;
import com.onlyknow.app.utils.OKFileUtil;
import com.onlyknow.app.utils.OKLoadBannerImage;
import com.onlyknow.app.utils.OKLogUtil;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 图文卡片浏览界面
 * 传入启动界面标识与列表Position
 * 匹配缓存中的副本列表,如果匹配失败该界面将退出
 * Created by ReSet on 2018/03/01.
 */

public class OKCardTWActivity extends OKBaseActivity implements OKManagerCardApi.onCallBack, OKManagerUserApi.onCallBack {
    public final static String KEY_INTENT_IMAGE_AND_TEXT_CARD = "IMAGE_AND_TEXT_CARD";

    private OKCircleImageView imageHeadPortrait;
    private TextView textNickName, textSignature, textContentTitle, textContent, textZan, textWatch, textComment, textTag, textLink, textDate;
    private Button buttonAttention;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private Banner mBanner;
    private OKSEImageView imageViewZAN, imageViewSC, imageViewPL;
    private LinearLayout linearLayoutZY;

    private OKCardBean mCardBean;
    private int mStartInterfaceType;

    private OKManagerCardApi okManagerCardApi;
    private OKCardRelatedBean mCardBindBean;

    private OKManagerUserApi okManagerUserApi;

    private List<String> mImageUrls = new ArrayList<>();

    private UMShareListener mShareListener = new UMShareListener() {

        @Override
        public void onStart(SHARE_MEDIA share_media) {
        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            showSnackBar(linearLayoutZY, "分享成功", "");
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            showSnackBar(linearLayoutZY, "分享失败", "ErrorCode: " + throwable.getMessage());
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            showSnackBar(linearLayoutZY, "分享取消", "");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ok_activity_card_graphic);
        initUserBody();
        initSettingBody();
        mStartInterfaceType = getIntent().getExtras().getInt(INTENT_KEY_INTERFACE_TYPE);
        mCardBean = (OKCardBean) getIntent().getExtras().getSerializable(KEY_INTENT_IMAGE_AND_TEXT_CARD);

        findView();
        init();
        addCardBrowsing();
    }

    @Override
    public void onResume() {
        OKManagerCardApi.Params params = new OKManagerCardApi.Params();
        params.setUsername(USER_BODY.getString(OKUserInfoBean.KEY_USERNAME, ""));
        params.setCardId(mCardBean.getCardId());
        params.setType(OKManagerCardApi.Params.TYPE_CARD_RELATED);

        if (okManagerCardApi != null) {
            okManagerCardApi.cancelTask();
        }
        okManagerCardApi = new OKManagerCardApi(this);
        okManagerCardApi.requestManagerCard(params, this);

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (okManagerCardApi != null) {
            okManagerCardApi.cancelTask(); // 如果线程已经在执行则取消执行
        }

        if (okManagerUserApi != null) {
            okManagerUserApi.cancelTask(); // 如果线程已经在执行则取消执行
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopBanner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.ok_menu_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_message_jubao) {
            Bundle bundle = new Bundle();
            bundle.putString(OKRePortActivity.KEY_TYPE, OKRePortActivity.TYPE_CARD);
            bundle.putString(OKRePortActivity.KEY_ID, Integer.toString(mCardBean.getCardId()));
            startUserActivity(bundle, OKRePortActivity.class);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }

    private void findView() {
        super.findCollapsingToolbarView();
        setSupportActionBar(mToolbar);

        mToolbarBack.setVisibility(View.VISIBLE);
        mToolbarSharing.setVisibility(View.VISIBLE);

        mBanner = (Banner) findViewById(R.id.ok_activity_card_graphic_banner);
        imageHeadPortrait = (OKCircleImageView) findViewById(R.id.MESSAGE_top_biaoti_imag);
        textNickName = (TextView) findViewById(R.id.MESSAGE_top_biaoti_text);
        textSignature = (TextView) findViewById(R.id.MESSAGE_top_qianmin_text);
        textContentTitle = (TextView) findViewById(R.id.message_neiron_biaoti_text);
        textContent = (TextView) findViewById(R.id.message_neiron_text);
        textZan = (TextView) findViewById(R.id.MESSAGE_top_zan_text);
        textWatch = (TextView) findViewById(R.id.MESSAGE_top_shouchang_text);
        textComment = (TextView) findViewById(R.id.MESSAGE_top_pinglun_text);
        textTag = (TextView) findViewById(R.id.MESSAGE_biaoqian_text);
        buttonAttention = (Button) findViewById(R.id.MESSAGE_top_guanzhu_but);
        textLink = (TextView) findViewById(R.id.message_link_text);
        textDate = (TextView) findViewById(R.id.message_date_text);
        linearLayoutZY = (LinearLayout) findViewById(R.id.MESSAGE_top_zhuye_layout);
        imageViewZAN = (OKSEImageView) findViewById(R.id.MESSAGE_top_zan_imag);
        imageViewSC = (OKSEImageView) findViewById(R.id.MESSAGE_top_shouchang_imag);
        imageViewPL = (OKSEImageView) findViewById(R.id.MESSAGE_top_pinglun_imag);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.TW_message_toolbar_layout);
    }

    private void init() {
        // 字体设置
        if (SETTING_BODY.getString("FONT", "NORM").equals("MAX")) {
            textContent.setTextSize(25);
        } else if (SETTING_BODY.getString("FONT", "NORM").equals("CENTRE")) {
            textContent.setTextSize(22);
        } else if (SETTING_BODY.getString("FONT", "NORM").equals("MIN")) {
            textContent.setTextSize(16);
        } else {
            textContent.setTextSize(20);
        }

        // 获取内容图片信息
        mImageUrls = mCardBean.cardImagesToUrls();

        mBanner.setImageLoader(new OKLoadBannerImage(false));
        mBanner.setBannerAnimation(Transformer.DepthPage);
        mBanner.isAutoPlay(true);
        mBanner.setDelayTime(5000);
        mBanner.setIndicatorGravity(BannerConfig.CENTER);
        mBanner.setImages(mImageUrls);
        startBanner();

        GlideRoundApi(imageHeadPortrait, mCardBean.getTitleImageUrl(), R.drawable.touxian_placeholder, R.drawable.touxian_placeholder);

        textNickName.setText(mCardBean.getTitleText());
        textSignature.setText("这个人很懒 , 什么都没有留下!");
        textZan.setText("" + mCardBean.getPraiseCount());
        textWatch.setText("" + mCardBean.getWatchCount());
        textComment.setText("" + mCardBean.getCommentCount());
        textTag.setText(mCardBean.getLabelling());
        textContentTitle.setText(mCardBean.getContentTitleText());
        textContent.setText(mCardBean.getContentText());
        textLink.setText(mCardBean.getMessageLink());
        textDate.setText(OKDateUtil.formatTime(mCardBean.getCreateDate()) + " 发表");
        if (mCardBean.getContentTitleText().length() >= 12) {
            mCollapsingToolbarLayout.setTitle(mCardBean.getContentTitleText().substring(0, 12) + "...");
        } else {
            mCollapsingToolbarLayout.setTitle(mCardBean.getContentTitleText());
        }
        mCollapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
        mCollapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);

        mToolbarSharing.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                shareImage(mCardBean.getImageList(), mShareListener);
            }
        });

        buttonAttention.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (USER_BODY.getBoolean("STATE", false)) {
                    if (mCardBindBean != null && mCardBindBean.isAttention()) {
                        showSnackBar(v, "您已经关注了", "");
                        return;
                    }

                    OKManagerUserApi.Params params = new OKManagerUserApi.Params();
                    params.setUsername(USER_BODY.getString(OKUserInfoBean.KEY_USERNAME, ""));
                    params.setAttentionUsername(mCardBean.getUserName());
                    params.setType(OKManagerUserApi.Params.TYPE_ADD_ATTENTION);

                    if (okManagerUserApi != null) {
                        okManagerUserApi.cancelTask();
                    }
                    okManagerUserApi = new OKManagerUserApi(OKCardTWActivity.this);
                    okManagerUserApi.requestManagerUser(params, OKCardTWActivity.this);
                } else {
                    startUserActivity(null, OKLoginActivity.class);
                }
            }
        });

        mToolbarBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                OKCardTWActivity.this.finish();
            }
        });

        linearLayoutZY.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (mStartInterfaceType != INTERFACE_HOME) {
                    Bundle bundle = new Bundle();
                    bundle.putString(OKUserInfoBean.KEY_USERNAME, mCardBean.getUserName());
                    bundle.putString(OKUserInfoBean.KEY_NICKNAME, mCardBean.getTitleText());
                    startUserActivity(bundle, OKHomePageActivity.class);
                } else {
                    finish();
                }
            }
        });

        imageViewZAN.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (USER_BODY.getBoolean("STATE", false)) {
                    if (mCardBindBean != null && mCardBindBean.isPraise()) {
                        showSnackBar(v, "您已经点赞了", "");
                        return;
                    }

                    OKManagerCardApi.Params params = new OKManagerCardApi.Params();
                    params.setUsername(USER_BODY.getString(OKUserInfoBean.KEY_USERNAME, ""));
                    params.setCardId(mCardBean.getCardId());
                    params.setType(OKManagerCardApi.Params.TYPE_PRAISE);

                    if (okManagerCardApi != null) {
                        okManagerCardApi.cancelTask();
                    }
                    okManagerCardApi = new OKManagerCardApi(OKCardTWActivity.this);
                    okManagerCardApi.requestManagerCard(params, OKCardTWActivity.this);
                } else {
                    startUserActivity(null, OKLoginActivity.class);
                }
            }
        });

        imageViewSC.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (USER_BODY.getBoolean("STATE", false)) {
                    if (mCardBindBean != null && mCardBindBean.isWatch()) {
                        showSnackBar(v, "您已经收藏了", "");
                        return;
                    }

                    OKManagerCardApi.Params params = new OKManagerCardApi.Params();
                    params.setUsername(USER_BODY.getString(OKUserInfoBean.KEY_USERNAME, ""));
                    params.setCardId(mCardBean.getCardId());
                    params.setType(OKManagerCardApi.Params.TYPE_WATCH);

                    if (okManagerCardApi != null) {
                        okManagerCardApi.cancelTask();
                    }
                    okManagerCardApi = new OKManagerCardApi(OKCardTWActivity.this);
                    okManagerCardApi.requestManagerCard(params, OKCardTWActivity.this);
                } else {
                    startUserActivity(null, OKLoginActivity.class);
                }
            }
        });

        imageViewPL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(OKUserInfoBean.KEY_USERNAME, mCardBean.getUserName());
                bundle.putInt(OKCardBean.KEY_CARD_ID, mCardBean.getCardId());
                startUserActivity(bundle, OKCommentActivity.class);
            }
        });

        textLink.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString("WEB_LINK", textLink.getText().toString());
                Intent intent = new Intent();
                intent.setClass(OKCardTWActivity.this, OKBrowserActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
            }
        });

        mBanner.setOnBannerListener(new OnBannerListener() {
            @Override
            public void OnBannerClick(int position) {
                String url = mImageUrls.get(position);
                if (OKFileUtil.isVideoUrl(url)) {
                    Bundle bundle = new Bundle();
                    bundle.putString("URL", url);
                    bundle.putString("TITLE", mCardBean.getTitleText() + "发表的视频");
                    startUserActivity(bundle, OKVideoActivity.class);
                } else {
                    int location[] = new int[2];
                    mBanner.getLocationOnScreen(location);

                    Bundle mBundle = new Bundle();
                    mBundle.putInt("left", location[0]);
                    mBundle.putInt("top", location[1]);
                    mBundle.putInt("height", mBanner.getHeight());
                    mBundle.putInt("width", mBanner.getWidth());

                    mBundle.putString("url", url);

                    startUserActivity(mBundle, OKDragPhotoActivity.class);
                    overridePendingTransition(0, 0);
                }
            }
        });
    }

    private void addCardBrowsing() {

        OKManagerCardApi.Params params = new OKManagerCardApi.Params();
        params.setUsername(USER_BODY.getString(OKUserInfoBean.KEY_USERNAME, ""));
        params.setCardId(mCardBean.getCardId());
        params.setType(OKManagerCardApi.Params.TYPE_BROWSING);

        if (okManagerCardApi != null) {
            okManagerCardApi.cancelTask();
        }
        okManagerCardApi = new OKManagerCardApi(OKCardTWActivity.this);
        okManagerCardApi.requestManagerCard(params, OKCardTWActivity.this);

        mCardBean.setRead(true);
        mCardBean.setReadTime(OKConstant.getNowDateByLong());
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    OKDatabaseHelper helper = OKDatabaseHelper.getHelper(OKCardTWActivity.this);
                    helper.getCardDao().createOrUpdate(mCardBean);
                } catch (SQLException e) {
                    e.printStackTrace();
                    OKLogUtil.print("卡片记录更新错误 ErrorMsg :" + e.getMessage());
                }
            }
        }.start();
    }

    private void startBanner() {
        if (mBanner != null) {
            mBanner.start();
        }
    }

    private void stopBanner() {
        if (mBanner != null) {
            mBanner.stopAutoPlay();
        }
    }

    @Override
    public void managerCardComplete(OKServiceResult<Object> result, String type, int pos) {
        if (OKManagerCardApi.Params.TYPE_CARD_RELATED.equals(type)) {
            if (result == null || !result.isSuccess()) return;

            mCardBindBean = (OKCardRelatedBean) result.getData();

            if (mCardBindBean == null) return;

            if (mCardBindBean.isCardRemove()) {
                imageViewZAN.setEnabled(false);
                imageViewSC.setEnabled(false);
                imageViewPL.setEnabled(false);
                mToolbarTitle.setText("该卡片已被用户删除");
                showSnackBar(linearLayoutZY, "该卡片已被用户删除", "");
            }

            if (mCardBindBean.isAttention()) {
                buttonAttention.setText("已关注");
                buttonAttention.setEnabled(false);
            }

            if (mCardBindBean.isWatch()) {
                textWatch.setTextColor(getResources().getColor(R.color.fenhon));
            }

            if (mCardBindBean.isPraise()) {
                textZan.setTextColor(getResources().getColor(R.color.fenhon));
            }

            if (TextUtils.isEmpty(mCardBindBean.getTag())) {
                textSignature.setText("这个人很懒 , 什么都没有留下!");
            } else {
                textSignature.setText(mCardBindBean.getTag());
            }

            textZan.setText("" + mCardBindBean.getPraiseCount());
            textWatch.setText("" + mCardBindBean.getWatchCount());
            textComment.setText("" + mCardBindBean.getCommentCount());

        } else if (OKManagerCardApi.Params.TYPE_WATCH.equals(type)) {

            if (result == null || !result.isSuccess()) {
                showSnackBar(mCollapsingToolbarLayout, "操作失败,请重试", "");
                return;
            }

            if (mCardBindBean == null) {
                mCardBindBean = new OKCardRelatedBean();
            }
            mCardBindBean.setWatch(true);

            int count = mCardBean.getWatchCount() + 1;
            textWatch.setText("" + count);
            textWatch.setTextColor(getResources().getColor(R.color.fenhon));

        } else if (OKManagerCardApi.Params.TYPE_PRAISE.equals(type)) {

            if (result == null || !result.isSuccess()) {
                showSnackBar(mCollapsingToolbarLayout, "操作失败,请重试", "");
                return;
            }

            if (mCardBindBean == null) {
                mCardBindBean = new OKCardRelatedBean();
            }
            mCardBindBean.setPraise(true);

            int count = mCardBean.getPraiseCount() + 1;
            textZan.setText("" + count);
            textZan.setTextColor(getResources().getColor(R.color.fenhon));

        } else if (OKManagerCardApi.Params.TYPE_BROWSING.equals(type)) {
            if (result == null || !result.isSuccess()) {
                showSnackBar(mCollapsingToolbarLayout, "PutBrowsingFailure", "");
            }
        }
    }

    @Override
    public void managerUserComplete(OKServiceResult<Object> result, String type, int pos) {
        if (OKManagerUserApi.Params.TYPE_ADD_ATTENTION.equals(type)) {

            if (result == null || !result.isSuccess()) {
                showSnackBar(mCollapsingToolbarLayout, "操作失败,请重试", "");
                return;
            }

            if (mCardBindBean == null) {
                mCardBindBean = new OKCardRelatedBean();
            }
            mCardBindBean.setAttention(true);

            buttonAttention.setText("已关注");
            buttonAttention.setEnabled(false);

        }
    }
}
