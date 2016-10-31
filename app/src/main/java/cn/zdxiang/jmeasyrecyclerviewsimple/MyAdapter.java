package cn.zdxiang.jmeasyrecyclerviewsimple;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import cn.zdxiang.jmeasyrecyclerview.JMEasyAdapter;

/**
 * Created on 16/10/28.
 *
 * @author JM
 * @version v1.0
 * @discrition MyAdapter
 */

public class MyAdapter extends JMEasyAdapter {
    private List<String> mList;

    public MyAdapter() {
    }

    public void initList(List<String> list) {
        this.mList = list;
        notifyDataSetChanged();
    }

    public void addList(List<String> list) {
        if (list != null) {
            mList.addAll(list);
            notifyDataSetChanged();
        }
    }

    public void refreshList(List<String> list) {
        if (list != null) {
            mList.clear();
            mList = list;
            notifyDataSetChanged();
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateMyViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_simple_list, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        if (holder instanceof ViewHolder) {
            ViewHolder vh = (ViewHolder) holder;
            vh.tv_content.setText(mList.get(position));
        }
    }

    @Override
    public int getItemSize() {
        return mList == null ? 0 : mList.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tv_content;

        private ViewHolder(View itemView) {
            super(itemView);

            tv_content = (TextView) itemView.findViewById(R.id.tv_content);
        }
    }
}
