package cn.zdxiang.jmeasyrecyclerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorRes;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;

/**
 * Created on 16/8/26.
 *
 * @author JM
 * @version v1.0
 * @discrition JM easy recyclerView
 */

public class JMEasyRecyclerView extends FrameLayout {

    protected int ITEM_LEFT_TO_LOAD_MORE = 10;

    protected RecyclerView mRecycler;

    protected ViewStub vs_empty;

    protected ViewStub vs_errorRetry;

    protected View mEmptyView;

    protected boolean mClipToPadding;

    protected int mScrollbarStyle;

    protected int mEmptyId;

    protected int mErrorRetryId;

    protected LAYOUT_MANAGER_TYPE layoutManagerType;

    protected RecyclerView.OnScrollListener mInternalOnScrollListener;

    protected RecyclerView.OnScrollListener mExternalOnScrollListener;

    protected OnLoadMoreListener mOnLoadMoreListener;

    private OnRefreshListener mOnRefreshListener;

    protected boolean isLoadingMore;

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    protected int mainLayoutId;

    private int[] lastScrollPositions;

    public SwipeRefreshLayout getSwipeToRefresh() {
        return mSwipeRefreshLayout;
    }

    public RecyclerView getRecyclerView() {
        return mRecycler;
    }

    public JMEasyRecyclerView(Context context) {
        super(context);
        initView();
    }

    public JMEasyRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        initView();
    }

    public JMEasyRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        initView();
    }


    protected void initAttrs(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.jm_easy_recyclerview);
        try {
            mainLayoutId = a.getResourceId(R.styleable.jm_easy_recyclerview_mainLayoutId, R.layout.jmerv_main_view);
            mClipToPadding = a.getBoolean(R.styleable.jm_easy_recyclerview_recyclerClipToPadding, false);
            mScrollbarStyle = a.getInt(R.styleable.jm_easy_recyclerview_scrollbarStyle, -1);
            mEmptyId = a.getResourceId(R.styleable.jm_easy_recyclerview_layout_empty, R.layout.empty_view);
            mErrorRetryId = a.getResourceId(R.styleable.jm_easy_recyclerview_layout_error, R.layout.error_view);
        } finally {
            a.recycle();
        }
    }

    private void initView() {
        if (isInEditMode()) {
            return;
        }
        View v = LayoutInflater.from(getContext()).inflate(mainLayoutId, this);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.srl);
        mSwipeRefreshLayout.setEnabled(false);

        initEmptyView(v);

        initErrorView(v);

        initRecyclerView(v);
    }

    /**
     * init the empty view
     *
     * @param v main view
     */
    private void initEmptyView(View v) {
        vs_empty = (ViewStub) v.findViewById(R.id.empty);
        vs_empty.setLayoutResource(mEmptyId);
        mEmptyView = vs_empty.inflate();
        vs_empty.setVisibility(View.GONE);
    }

    /**
     * init the error view
     *
     * @param v main layout view
     */
    private void initErrorView(View v) {
        vs_errorRetry = (ViewStub) v.findViewById(R.id.vs_error_retry);
        vs_errorRetry.setLayoutResource(mErrorRetryId);
        View view = vs_errorRetry.inflate();
        vs_errorRetry.setVisibility(GONE);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                vs_errorRetry.setVisibility(GONE);
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh();
                }
            }
        });
    }

    /**
     * Implement this method to customize the AbsListView
     */
    protected void initRecyclerView(View view) {
        View recyclerView = view.findViewById(android.R.id.list);

        if (recyclerView instanceof RecyclerView) {
            mRecycler = (RecyclerView) recyclerView;
        } else {
            throw new IllegalArgumentException("it must match the RecyclerView!");
        }


        mRecycler.setClipToPadding(mClipToPadding);
        mInternalOnScrollListener = new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                processOnMore();

                if (mExternalOnScrollListener != null) {
                    mExternalOnScrollListener.onScrolled(recyclerView, dx, dy);
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mExternalOnScrollListener != null)
                    mExternalOnScrollListener.onScrollStateChanged(recyclerView, newState);
            }
        };
        mRecycler.addOnScrollListener(mInternalOnScrollListener);

        if (mScrollbarStyle != -1) {
            mRecycler.setScrollBarStyle(mScrollbarStyle);
        }
    }

    private void processOnMore() {
        RecyclerView.LayoutManager layoutManager = mRecycler.getLayoutManager();
        int lastVisibleItemPosition = getLastVisibleItemPosition(layoutManager);
        int visibleItemCount = layoutManager.getChildCount();
        int totalItemCount = layoutManager.getItemCount();

        if (((totalItemCount - lastVisibleItemPosition) <= ITEM_LEFT_TO_LOAD_MORE || (totalItemCount - lastVisibleItemPosition) == 0 && totalItemCount > visibleItemCount) && !isLoadingMore) {
            isLoadingMore = true;
            if (mOnLoadMoreListener != null) {

                if (mRecycler.getAdapter() instanceof JMEasyAdapter) {
                    JMEasyAdapter adapter = (JMEasyAdapter) mRecycler.getAdapter();
                    if (adapter.getFooterType() == JMEasyAdapter.FOOTER_PROGRESS) {
                        mOnLoadMoreListener.onLoadMore(mRecycler.getAdapter().getItemCount(), ITEM_LEFT_TO_LOAD_MORE, lastVisibleItemPosition);
                    }
                }
            }
        }
    }

    private int getLastVisibleItemPosition(RecyclerView.LayoutManager layoutManager) {
        int lastVisibleItemPosition = -1;
        if (layoutManagerType == null) {
            if (layoutManager instanceof GridLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.GRID;
            } else if (layoutManager instanceof LinearLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.LINEAR;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                layoutManagerType = LAYOUT_MANAGER_TYPE.STAGGERED_GRID;
            } else {
                throw new RuntimeException("Unsupported LayoutManager used. Valid ones are LinearLayoutManager, GridLayoutManager and StaggeredGridLayoutManager");
            }
        }

        switch (layoutManagerType) {
            case LINEAR:
                lastVisibleItemPosition = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case GRID:
                lastVisibleItemPosition = ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
                break;
            case STAGGERED_GRID:
                lastVisibleItemPosition = caseStaggeredGrid(layoutManager);
                break;
        }
        return lastVisibleItemPosition;
    }

    private int caseStaggeredGrid(RecyclerView.LayoutManager layoutManager) {
        StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) layoutManager;
        if (lastScrollPositions == null) {
            lastScrollPositions = new int[staggeredGridLayoutManager.getSpanCount()];
        }
        staggeredGridLayoutManager.findLastVisibleItemPositions(lastScrollPositions);
        return findMax(lastScrollPositions);
    }


    private int findMax(int[] lastPositions) {
        int max = Integer.MIN_VALUE;
        for (int value : lastPositions) {
            if (value > max)
                max = value;
        }
        return max;
    }

    /**
     * @param adapter                       The new adapter to set, or null to set no adapter
     * @param compatibleWithPrevious        Should be set to true if new adapter uses the same {@android.support.v7.widget.RecyclerView.ViewHolder}
     *                                      as previous one
     * @param removeAndRecycleExistingViews If set to true, RecyclerView will recycle all existing Views. If adapters
     *                                      have stable ids and/or you want to animate the disappearing views, you may
     *                                      prefer to set this to false
     */
    private void setAdapterInternal(final RecyclerView.Adapter adapter, boolean compatibleWithPrevious, boolean removeAndRecycleExistingViews) {
        if (compatibleWithPrevious) {
            mRecycler.swapAdapter(adapter, removeAndRecycleExistingViews);
        } else {
            mRecycler.setAdapter(adapter);
        }
        mRecycler.setVisibility(View.VISIBLE);
        mSwipeRefreshLayout.setRefreshing(false);
        if (null != adapter) {
            adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onItemRangeChanged(int positionStart, int itemCount) {
                    super.onItemRangeChanged(positionStart, itemCount);
                    update();
                }

                @Override
                public void onItemRangeInserted(int positionStart, int itemCount) {
                    super.onItemRangeInserted(positionStart, itemCount);
                    update();
                }

                @Override
                public void onItemRangeRemoved(int positionStart, int itemCount) {
                    super.onItemRangeRemoved(positionStart, itemCount);
                    update();
                }

                @Override
                public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                    super.onItemRangeMoved(fromPosition, toPosition, itemCount);
                    update();
                }

                @Override
                public void onChanged() {
                    super.onChanged();
                    update();
                }

                private void update() {
                    processEmptyView();
                    isLoadingMore = false;
                    mSwipeRefreshLayout.setRefreshing(false);
                    vs_errorRetry.setVisibility(GONE);
                }

                private void processEmptyView() {
                    if (adapter.getItemCount() == 0) {
                        vs_empty.setVisibility(View.VISIBLE);
                    } else {
                        vs_empty.setVisibility(View.GONE);
                    }
                }
            });
        }

//        if (mEmptyId != 0) {
//            vs_empty.setVisibility(null != adapter && adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
//        }
    }

    /**
     * Set the layout manager to the recycler
     */
    public void setLayoutManager(RecyclerView.LayoutManager manager) {
        mRecycler.setLayoutManager(manager);
    }

    /**
     * Set the adapter to the recycler
     * Automatically hide the progressbar
     * Set the refresh to false
     * If adapter is empty, then the emptyview is shown
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        setAdapterInternal(adapter, false, true);
    }

    /**
     * Set the customer adapter to the recycler
     */
    public void setAdapter(JMEasyAdapter adapter) {
        setAdapterInternal(adapter, false, true);
    }

    /**
     * @param adapter                       The new adapter to , or null to set no adapter.
     * @param removeAndRecycleExistingViews If set to true, RecyclerView will recycle all existing Views. If adapters
     *                                      have stable ids and/or you want to animate the disappearing views, you may
     *                                      prefer to set this to false.
     */
    public void swapAdapter(RecyclerView.Adapter adapter, boolean removeAndRecycleExistingViews) {
        setAdapterInternal(adapter, true, removeAndRecycleExistingViews);
    }


    /**
     * Remove the adapter from the recycler
     */
    public void clear() {
        mRecycler.setAdapter(null);
    }


    /**
     * Show the errorRetryView
     */
    public void showErrorRetryView() {
        if (getAdapter() != null && getAdapter() instanceof JMEasyAdapter) {
            JMEasyAdapter adapter = (JMEasyAdapter) getAdapter();
            if (adapter.isFooterEnable() && adapter.getItemCount() > 0) {
                adapter.notifyLoadMoreModleChange(JMEasyAdapter.FOOTER_RETRY);
            } else if (adapter.getItemCount() == 0) {
                vs_empty.setVisibility(GONE);
                vs_errorRetry.setVisibility(VISIBLE);
            } else {
                Log.d("gongxifa", "nothing");
            }
        }
    }


    public void setRefreshing(boolean refreshing) {
        if (getAdapter().getItemCount() == 0) {
            Log.d("gongxifa", "refreshing");
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }


    /**
     * Set the Internal refresh listener
     */
    public void addOnRefreshListener(final OnRefreshListener listener) {
        this.mOnRefreshListener = listener;
        mSwipeRefreshLayout.setEnabled(true);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mRecycler.getAdapter() instanceof JMEasyAdapter) {
                    JMEasyAdapter jmEasyAdapter = (JMEasyAdapter) mRecycler.getAdapter();
                    jmEasyAdapter.enableFooter();
                }
                if (listener != null) {
                    listener.onRefresh();
                }
            }
        });
    }

    /**
     * Set the colors for the SwipeRefreshLayout states
     */
    public void setRefreshingColorResources(@ColorRes int colRes1, @ColorRes int colRes2, @ColorRes int colRes3, @ColorRes int colRes4) {
        mSwipeRefreshLayout.setColorSchemeResources(colRes1, colRes2, colRes3, colRes4);
    }

    /**
     * Set the colors for the SwipeRefreshLayout states
     */
    public void setRefreshingColor(int col1, int col2, int col3, int col4) {
        mSwipeRefreshLayout.setColorSchemeColors(col1, col2, col3, col4);
    }

    /**
     * Set the scroll listener for the recycler
     */
    public void setOnScrollListener(RecyclerView.OnScrollListener listener) {
        mExternalOnScrollListener = listener;
    }

    /**
     * Add the onItemTouchListener for the recycler
     */
    public void addOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecycler.addOnItemTouchListener(listener);
    }

    /**
     * Remove the onItemTouchListener for the recycler
     */
    public void removeOnItemTouchListener(RecyclerView.OnItemTouchListener listener) {
        mRecycler.removeOnItemTouchListener(listener);
    }

    /**
     * @return the recycler adapter
     */
    public RecyclerView.Adapter getAdapter() {
        return mRecycler.getAdapter();
    }

    /**
     * Sets the More listener
     *
     * @param max Number of items before loading more
     */
    public void addOnLoadMoreListener(OnLoadMoreListener onMoreListener, int max) {
        mOnLoadMoreListener = onMoreListener;
        ITEM_LEFT_TO_LOAD_MORE = max;
    }

    public void addOnLoadMoreListener(OnLoadMoreListener onMoreListener) {
        mOnLoadMoreListener = onMoreListener;
    }

    public void setNumberBeforeMoreIsCalled(int max) {
        ITEM_LEFT_TO_LOAD_MORE = max;
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    /**
     * Enable/Disable the More event
     */
    public void setLoadingMore(boolean isLoadingMore) {
        this.isLoadingMore = isLoadingMore;
    }

    /**
     * Remove the moreListener
     */
    public void removeOnLoadMoreListener() {
        mOnLoadMoreListener = null;
    }


    public void setOnTouchListener(OnTouchListener listener) {
        mRecycler.setOnTouchListener(listener);
    }

    public void addItemDecoration(RecyclerView.ItemDecoration itemDecoration) {
        mRecycler.addItemDecoration(itemDecoration);
    }


    /**
     * @return inflated empty view or null
     */
    public View getEmptyView() {
        return mEmptyView;
    }

    /**
     * Animate a scroll by the given amount of pixels along either axis.
     *
     * @param dx Pixels to scroll horizontally
     * @param dy Pixels to scroll vertically
     */
    public void smoothScrollBy(int dx, int dy) {
        mRecycler.smoothScrollBy(dx, dy);
    }

    public enum LAYOUT_MANAGER_TYPE {
        LINEAR,
        GRID,
        STAGGERED_GRID
    }
}
