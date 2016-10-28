package cn.zdxiang.jmeasyrecyclerview;

/**
 * Created on 16/9/5.
 *
 * @author JM
 * @version v1.0
 * @discrition OnLoadMoreListener
 */

public interface OnLoadMoreListener {

    /**
     * @param overallItemsCount      overallItemsCount
     * @param itemsBeforeMore        itemsBeforeMore
     * @param maxLastVisiblePosition for staggered grid this is max of all spans
     */
    void onLoadMore(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition);
}