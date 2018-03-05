package com.onlyknow.app.api;

import android.content.Context;
import android.os.AsyncTask;

import com.onlyknow.app.database.bean.OKCardBean;
import com.onlyknow.app.net.OKBusinessNet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户主页数据源加载Api
 * <p>
 * Created by Administrator on 2017/12/23.
 */

public class OKLoadHomeApi extends OKBaseApi {
    private onCallBack mOnCallBack;
    private Context context;
    private LoadCardListTask mLoadCardListTask;
    private boolean isLoadMore = false;

    public OKLoadHomeApi(Context con, boolean isLm) {
        this.context = con;
        this.isLoadMore = isLm;
    }

    public interface onCallBack {
        void homeApiComplete(List<OKCardBean> list);
    }

    public void requestCardBeanList(Map<String, String> param, boolean isLm, onCallBack mCallBack) {
        this.isLoadMore = isLm;
        this.mOnCallBack = mCallBack;
        cancelTask();
        mLoadCardListTask = new LoadCardListTask();
        mLoadCardListTask.executeOnExecutor(exec, param);
    }

    public void cancelTask() {
        if (mLoadCardListTask != null && mLoadCardListTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadCardListTask.cancel(true);
        }
    }

    private class LoadCardListTask extends AsyncTask<Map<String, String>, Void, List<OKCardBean>> {

        @Override
        protected List<OKCardBean> doInBackground(Map<String, String>... params) {
            if (isCancelled()) {
                return null;
            }
            OKBusinessNet mOKBusinessNet = new OKBusinessNet();
            List<OKCardBean> mOKCardBeanList = new ArrayList<>();
            if (isLoadMore) {
                mOKCardBeanList = mOKBusinessNet.loadMoreUserCard(params[0]);
            } else {
                mOKCardBeanList = mOKBusinessNet.getUserCard(params[0]);
            }
            return mOKCardBeanList;
        }

        @Override
        protected void onPostExecute(List<OKCardBean> mOKCardBeanList) {
            if (isCancelled()) {
                return;
            }
            mOnCallBack.homeApiComplete(mOKCardBeanList);
            super.onPostExecute(mOKCardBeanList);
        }
    }
}
