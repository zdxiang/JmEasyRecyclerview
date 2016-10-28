package cn.zdxiang.jmeasyrecyclerview;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Created on 16/9/5.
 *
 * @author JM
 * @version v1.0
 * @discrition JMEasyAdapter
 */

public abstract class JMEasyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_ALL_OTHERS = 0x3b9ac9ff;

    private final static int TYPE_FOOTER = 0x3b9ac9fe;

    /**
     * The footer type for retry view
     */
    public final static int FOOTER_RETRY = 1;

    /**
     * The default footer type
     */
    public final static int FOOTER_PROGRESS = 0;

    private int mFooterType = 0;

    private boolean isFooterEnable = true;

    public int getFooterType() {
        return mFooterType;
    }

    private OnLoadMoreRetryListener onLoadMoreRetryListener;

    public interface OnLoadMoreRetryListener {

        void onRetry();
    }

    public void setOnLoadMoreRetryListener(OnLoadMoreRetryListener onLoadMoreRetryListener) {
        this.onLoadMoreRetryListener = onLoadMoreRetryListener;
    }

    /**
     * Enable the footer of loading
     */
    void enableFooter() {
        isFooterEnable = true;
    }


    public boolean isFooterEnable() {
        return isFooterEnable;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            return new FooterHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.more_progress_view, parent, false));
        } else {
            return onCreateMyViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof FooterHolder) {
            FooterHolder footerHolder = (FooterHolder) holder;
            footerHolder.switchFooterModel(mFooterType);
        }
    }

    public abstract RecyclerView.ViewHolder onCreateMyViewHolder(ViewGroup parent, int viewType);

    @Override
    public int getItemViewType(int position) {
        int footerPosition = getItemCount() - 1;
        if (footerPosition == position && isFooterEnable) {
            return TYPE_FOOTER;
        } else {
            return TYPE_ALL_OTHERS;
        }
    }

    @Override
    public int getItemCount() {
        int count = getItemSize();
        if (isFooterEnable && count != 0) count++;
        return count;
    }

    public abstract int getItemSize();

    public void notifyLoadFinish() {
        if (isFooterEnable) {
            notifyItemRemoved(getItemCount() - 1);
            isFooterEnable = false;
        }
    }


    public void notifyLoadRetry() {
        if (isFooterEnable) {

        }
    }

    /**
     * notify show error view when loading more error
     */
    public void notifyLoadMoreModleChange(int type) {
        if (isFooterEnable) {
            mFooterType = type;
            notifyItemChanged(getItemCount() - 1);
        }
    }

    protected class FooterHolder extends RecyclerView.ViewHolder {
        public ProgressBar pb_loading;

        public TextView tv_retry;

        public FooterHolder(View itemView) {
            super(itemView);
            pb_loading = (ProgressBar) itemView.findViewById(R.id.pb_loading);
            tv_retry = (TextView) itemView.findViewById(R.id.tv_click_4_retry);
            tv_retry.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onLoadMoreRetryListener != null && isFooterEnable) {
                        onLoadMoreRetryListener.onRetry();
                        mFooterType = FOOTER_PROGRESS;
                        notifyItemChanged(getItemCount() - 1);
                    }
                }
            });
        }

        public void switchFooterModel(int type) {
            tv_retry.setVisibility(type == FOOTER_RETRY ? View.VISIBLE : View.GONE);
            pb_loading.setVisibility(type == FOOTER_RETRY ? View.GONE : View.VISIBLE);
        }
    }
}
