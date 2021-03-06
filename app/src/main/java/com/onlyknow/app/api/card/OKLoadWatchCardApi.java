package com.onlyknow.app.api.card;

import android.content.Context;
import android.os.AsyncTask;

import com.onlyknow.app.api.OKBaseApi;
import com.onlyknow.app.db.bean.OKCardBean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏界面数据源加载Api
 * <p>
 * Created by Administrator on 2017/12/22.
 */

public class OKLoadWatchCardApi extends OKBaseApi {
    private onCallBack mOnCallBack;
    private Context context;
    private LoadWatchTask mLoadWatchTask;

    public OKLoadWatchCardApi(Context con) {
        this.context = con;
    }

    public interface onCallBack {
        void loadWatchComplete(List<OKCardBean> mOKCardBeanList);
    }

    public void requestWatchCard(Params params, onCallBack mCallBack) {
        this.mOnCallBack = mCallBack;
        cancelTask();
        mLoadWatchTask = new LoadWatchTask();
        mLoadWatchTask.executeOnExecutor(exec, params);
    }

    public void cancelTask() {
        if (mLoadWatchTask != null && mLoadWatchTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadWatchTask.cancel(true);
        }
    }

    private class LoadWatchTask extends AsyncTask<Params, Void, List<OKCardBean>> {

        @Override
        protected List<OKCardBean> doInBackground(Params... params) {
            if (isCancelled()) {
                return null;
            }

            Params mParams = params[0];

            Map<String, String> map = new HashMap<>();
            map.put(Params.KEY_NAME, mParams.getUsername());
            map.put(Params.KEY_PAGE, String.valueOf(mParams.getPage()));
            map.put(Params.KEY_SIZE, String.valueOf(mParams.getSize()));

            return getWatchCard(map);
        }

        @Override
        protected void onPostExecute(List<OKCardBean> result) {
            if (isCancelled()) {
                return;
            }
            mOnCallBack.loadWatchComplete(result);
            super.onPostExecute(result);
        }
    }

    public static class Params {
        private String username;
        private int page;
        private int size;

        public final static String KEY_NAME = "username";
        public final static String KEY_PAGE = "page";
        public final static String KEY_SIZE = "size";

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }
    }
}
