package com.onlyknow.app.ui.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.dmcbig.mediapicker.PickerActivity;
import com.dmcbig.mediapicker.PickerConfig;
import com.dmcbig.mediapicker.bean.MediaBean;
import com.onlyknow.app.OKConstant;
import com.onlyknow.app.R;
import com.onlyknow.app.api.OKUserEditApi;
import com.onlyknow.app.database.bean.OKUserInfoBean;
import com.onlyknow.app.ui.OKBaseActivity;
import com.onlyknow.app.ui.view.OKCircleImageView;
import com.onlyknow.app.utils.OKSDCardUtil;
import com.yalantis.ucrop.UCrop;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OKUserEditActivity extends OKBaseActivity implements OKUserEditApi.onCallBack {
    private AppCompatButton mButtonCommit;
    private EditText mEditName, mEditPhone, mEditEmail, mEditTag, mEditNian, mEditYue, mEditRi;
    private OKCircleImageView mImageViewTouXian;
    private RadioGroup mRgSex;
    private RadioButton mRbNan, mRbNv;
    private TextView mTextViewQrCode;

    private String USERNAME, NICKNAME, PHONE, EMAIL, QIANMIN, SEX, BIRTH_DATE;
    private String XG_NICKNAME = "", XG_PHONE = "", XG_EMAIL = "", XG_QIANMIN = "", XG_BIRTHDATE = "", XG_SEX = "";
    private String mFilePath;

    private OKUserEditApi mOKUserEditApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ok_activity_user_edit);
        initUserInfoSharedPreferences();
        initSystemBar(this);
        findView();
        loadData();
        init();
    }

    private void loadData() {
        USERNAME = USER_INFO_SP.getString(OKUserInfoBean.KEY_USERNAME, "");
        NICKNAME = USER_INFO_SP.getString(OKUserInfoBean.KEY_NICKNAME, "");
        PHONE = USER_INFO_SP.getString(OKUserInfoBean.KEY_PHONE, "");
        EMAIL = USER_INFO_SP.getString(OKUserInfoBean.KEY_EMAIL, "");
        QIANMIN = USER_INFO_SP.getString(OKUserInfoBean.KEY_QIANMIN, "");
        SEX = USER_INFO_SP.getString(OKUserInfoBean.KEY_SEX, "");
        BIRTH_DATE = USER_INFO_SP.getString(OKUserInfoBean.KEY_BIRTH_DATE, "");

        String url = USER_INFO_SP.getString(OKUserInfoBean.KEY_HEADPORTRAIT_URL, "");

        GlideRoundApi(mImageViewTouXian, url, R.drawable.touxian_placeholder_hd, R.drawable.touxian_placeholder_hd);

        mEditName.setText(NICKNAME);
        mEditPhone.setText(PHONE);
        mEditEmail.setText(EMAIL);

        if (!TextUtils.isEmpty(QIANMIN)) {
            mEditTag.setText(QIANMIN);
        }

        if (!TextUtils.isEmpty(BIRTH_DATE) && !BIRTH_DATE.equals("NULL")) {
            String[] items = BIRTH_DATE.split("/");
            mEditNian.setText(items[0]);
            mEditYue.setText(items[1]);
            mEditRi.setText(items[2]);
        }

        if (!TextUtils.isEmpty(SEX) && SEX.equals("NAN")) {
            mRbNan.setChecked(true);
            mRbNv.setChecked(false);
        } else if (!TextUtils.isEmpty(SEX) && SEX.equals("NV")) {
            mRbNan.setChecked(false);
            mRbNv.setChecked(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mOKUserEditApi != null) {
            mOKUserEditApi.cancelTask();
        }
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (mToolbar != null) {
            mToolbar.setTitle("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UCrop.REQUEST_CROP:
                if (resultCode == RESULT_OK) {
                    Uri resultUri = UCrop.getOutput(data);
                    if (resultUri == null) {
                        showSnackBar(mButtonCommit, "没有URI地址", "");
                        return;
                    }
                    mFilePath = OKSDCardUtil.getFilePathByImageUri(OKUserEditActivity.this, resultUri);
                    if (TextUtils.isEmpty(mFilePath)) {
                        showSnackBar(mButtonCommit, "文件路径错误", "");
                        return;
                    }
                    if (mOKUserEditApi != null) {
                        mOKUserEditApi.cancelTask();
                    }
                    mOKUserEditApi = new OKUserEditApi(this);
                    Map<String, String> params = new HashMap<>();
                    params.put("username", USERNAME);
                    params.put("baseimag", mFilePath);
                    params.put("type", "TOUXIAN");
                    mOKUserEditApi.requestUserEdit(params, OKUserEditApi.TYPE_UPDATE_HEAD_PORTRAIT, this);
                    showProgressDialog("正在上传头像...");
                }
                break;
            case UCrop.RESULT_ERROR:
                showSnackBar(mButtonCommit, "剪裁失败", "");
                break;
            case SELECT_MEDIA_REQUEST_CODE:
                if (resultCode == PickerConfig.RESULT_CODE) {
                    mSelectMediaBean = data.getParcelableArrayListExtra(PickerConfig.EXTRA_RESULT);
                    dealWith(mSelectMediaBean);
                }
            default:
                break;
        }
    }

    private void init() {
        mButtonCommit.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                XG_NICKNAME = NICKNAME;
                XG_PHONE = PHONE;
                XG_EMAIL = EMAIL;
                XG_QIANMIN = QIANMIN;
                XG_BIRTHDATE = BIRTH_DATE;
                if (!TextUtils.isEmpty(mEditName.getText().toString()) && !mEditName.getText().toString().equals(NICKNAME)) {
                    XG_NICKNAME = mEditName.getText().toString();
                }
                if (!TextUtils.isEmpty(mEditPhone.getText().toString()) && !mEditPhone.getText().toString().equals(PHONE)) {
                    XG_PHONE = mEditPhone.getText().toString();
                }
                if (!TextUtils.isEmpty(mEditEmail.getText().toString()) && !mEditEmail.getText().toString().equals(EMAIL)) {
                    XG_EMAIL = mEditEmail.getText().toString();
                }
                if (!TextUtils.isEmpty(mEditTag.getText().toString()) && !mEditTag.getText().toString().equals(QIANMIN)) {
                    XG_QIANMIN = mEditTag.getText().toString();
                }
                if (!TextUtils.isEmpty(mEditNian.getText().toString())
                        && !TextUtils.isEmpty(mEditYue.getText().toString())
                        && !TextUtils.isEmpty(mEditRi.getText().toString())) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
                    boolean isBirth = Integer.parseInt(dateFormat.format(new Date())) > Integer.parseInt(mEditNian.getText().toString());
                    if (isBirth) {
                        XG_BIRTHDATE = mEditNian.getText().toString() + "/" + mEditYue.getText().toString()
                                + "/" + mEditRi.getText().toString();
                    } else {
                        showSnackBar(mButtonCommit, "生日不能大于当前年份", "");
                        return;
                    }
                }
                if (TextUtils.isEmpty(XG_SEX)) {
                    XG_SEX = SEX;
                }
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", USERNAME);
                params.put("nickname", XG_NICKNAME);
                params.put("phone", XG_PHONE);
                params.put("email", XG_EMAIL);
                params.put("qianmin", XG_QIANMIN);
                params.put("birth", XG_BIRTHDATE);
                params.put("sex", XG_SEX);
                if (mOKUserEditApi != null) {
                    mOKUserEditApi.cancelTask();
                }
                mOKUserEditApi = new OKUserEditApi(OKUserEditActivity.this);
                mOKUserEditApi.requestUserEdit(params, OKUserEditApi.TYPE_UPDATE_USER_INFO, OKUserEditActivity.this);
                showProgressDialog("正在修改资料...");
            }
        });

        mImageViewTouXian.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OKUserEditActivity.this, PickerActivity.class);
                intent.putExtra(PickerConfig.SELECT_MODE, PickerConfig.PICKER_IMAGE);//default image and video (Optional)
                intent.putExtra(PickerConfig.MAX_SELECT_SIZE, 3145728L); //default 180MB (Optional)
                intent.putExtra(PickerConfig.MAX_SELECT_COUNT, 1);  //default 40 (Optional)
                intent.putExtra(PickerConfig.DEFAULT_SELECTED_LIST, mSelectMediaBean); // (Optional)
                startActivityForResult(intent, SELECT_MEDIA_REQUEST_CODE);
            }
        });

        mToolbarLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog("用户管理", "是否退出当前账号?退出之后将无法使用部分功能!", "退出", "取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        USER_INFO_SP.edit().putBoolean("STATE", false).commit();
                        USER_INFO_SP.edit().putBoolean("STATE_CHANGE", true).commit();
                        sendUserBroadcast(ACTION_MAIN_SERVICE_LOGOUT_IM, null);
                        finish();
                    }
                });
            }
        });

        mTextViewQrCode.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle();
                bundle.putString(OKUserInfoBean.KEY_USERNAME, USER_INFO_SP.getString(OKUserInfoBean.KEY_USERNAME, ""));
                bundle.putString(OKUserInfoBean.KEY_NICKNAME, USER_INFO_SP.getString(OKUserInfoBean.KEY_NICKNAME, ""));
                bundle.putString(OKUserInfoBean.KEY_HEADPORTRAIT_URL, USER_INFO_SP.getString(OKUserInfoBean.KEY_HEADPORTRAIT_URL, ""));
                bundle.putString(OKUserInfoBean.KEY_QIANMIN, USER_INFO_SP.getString(OKUserInfoBean.KEY_QIANMIN, "这个人很懒 , 什么都没有留下!"));
                startUserActivity(bundle, OKMeQrCodeActivity.class);
            }
        });

        mRgSex.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == mRbNan.getId()) {
                    XG_SEX = "NAN";
                } else if (checkedId == mRbNv.getId()) {
                    XG_SEX = "NV";
                }
            }
        });

        mToolbarBack.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void findView() {
        super.findCommonToolbarView(this);
        setSupportActionBar(mToolbar);
        mToolbarLogout.setVisibility(View.VISIBLE);
        mToolbarBack.setVisibility(View.VISIBLE);
        mToolbarTitle.setVisibility(View.VISIBLE);
        mToolbarTitle.setText("用户信息编辑");
        mButtonCommit = (AppCompatButton) findViewById(R.id.UserEdit_btn_TiJiao);
        mEditName = (EditText) findViewById(R.id.UserEdit_input_name);
        mEditPhone = (EditText) findViewById(R.id.UserEdit_input_phone);
        mEditEmail = (EditText) findViewById(R.id.UserEdit_input_email);
        mEditTag = (EditText) findViewById(R.id.UserEdit_input_tag);
        mEditNian = (EditText) findViewById(R.id.UserEdit_input_nian);
        mEditYue = (EditText) findViewById(R.id.UserEdit_input_yue);
        mEditRi = (EditText) findViewById(R.id.UserEdit_input_ri);
        mRgSex = (RadioGroup) findViewById(R.id.UserEdit_RG_sex);
        mRbNan = (RadioButton) findViewById(R.id.UserEdit_RB_male);
        mRbNv = (RadioButton) findViewById(R.id.UserEdit_RB_female);
        mImageViewTouXian = (OKCircleImageView) findViewById(R.id.UserEdit_TouXian_Imag);
        mTextViewQrCode = (TextView) findViewById(R.id.UserEdit_link_qrcode);
    }

    private ArrayList<MediaBean> mSelectMediaBean;
    private final int SELECT_MEDIA_REQUEST_CODE = 200;

    private void dealWith(List<MediaBean> imageItems) {
        if (imageItems == null || imageItems.size() == 0) {
            showSnackBar(mToolbarAddImage, "未获选择图片", "");
            return;
        }
        String fp = imageItems.get(0).path;
        String gs = fp.substring(fp.lastIndexOf(".") + 1, fp.length());
        if (gs.equalsIgnoreCase("gif")) {
            showSnackBar(mButtonCommit, "您不能选择动图作为头像", "");
            return;
        }
        startUCrop(imageItems.get(0).path, 1, 1);
    }

    @Override
    public void userEditApiComplete(boolean b, String type) {
        if (b) {
            if (type.equals(OKUserEditApi.TYPE_UPDATE_USER_INFO)) {
                SharedPreferences.Editor editor = USER_INFO_SP.edit();
                editor.putString(OKUserInfoBean.KEY_NICKNAME, XG_NICKNAME);
                editor.putString(OKUserInfoBean.KEY_PHONE, XG_PHONE);
                editor.putString(OKUserInfoBean.KEY_EMAIL, XG_EMAIL);
                editor.putString(OKUserInfoBean.KEY_QIANMIN, XG_QIANMIN);
                editor.putString(OKUserInfoBean.KEY_SEX, XG_SEX);
                editor.putString(OKUserInfoBean.KEY_BIRTH_DATE, XG_BIRTHDATE);
                int age = 0;
                if (!TextUtils.isEmpty(XG_BIRTHDATE)) {
                    String[] items = XG_BIRTHDATE.split("/");
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");
                    age = Integer.parseInt(dateFormat.format(new Date())) - Integer.parseInt(items[0]);
                }
                editor.putInt(OKUserInfoBean.KEY_AGE, age);
                editor.commit();

                loadData();
            } else if (type.equals(OKUserEditApi.TYPE_UPDATE_HEAD_PORTRAIT)) {
                GlideRoundApi(mImageViewTouXian, mFilePath, R.drawable.touxian_placeholder_hd, R.drawable.touxian_placeholder_hd);
            }
            showSnackBar(mButtonCommit, "修改成功", "");
        } else {
            showSnackBar(mButtonCommit, "修改失败", "ErrorCode :" + OKConstant.SERVICE_ERROR);
        }
        closeProgressDialog();
    }
}