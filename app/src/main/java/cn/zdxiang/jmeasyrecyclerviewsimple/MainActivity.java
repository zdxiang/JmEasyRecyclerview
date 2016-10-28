package cn.zdxiang.jmeasyrecyclerviewsimple;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import cn.zdxiang.jmeasyrecyclerview.JMEasyAdapter;
import cn.zdxiang.jmeasyrecyclerview.JMEasyRecyclerView;
import cn.zdxiang.jmeasyrecyclerview.OnLoadMoreListener;
import cn.zdxiang.jmeasyrecyclerview.OnRefreshListener;

public class MainActivity extends AppCompatActivity implements OnLoadMoreListener, OnRefreshListener, JMEasyAdapter.OnLoadMoreRetryListener {

    private JMEasyRecyclerView recyclerView;

    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        recyclerView = (JMEasyRecyclerView) findViewById(R.id.jm_erv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addOnRefreshListener(this);
        recyclerView.addOnLoadMoreListener(this, 1);
        mAdapter = new MyAdapter();
        mAdapter.setOnLoadMoreRetryListener(this);
        recyclerView.setAdapter(mAdapter);

        initData();
    }

    private void initData() {
        recyclerView.setRefreshing(true);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.initList(getData());
                    }
                });
            }
        });
        thread.start();
    }

    private List<String> getData() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            strings.add(i, "Text" + (i + 1));
        }
        return strings;
    }

    private List<String> getMoreData() {
        List<String> strings = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            strings.add(i, "NewText" + (i + 1));
        }
        return strings;
    }

    @Override
    public void onRetry() {

    }

    @Override
    public void onLoadMore(int overallItemsCount, int itemsBeforeMore, int maxLastVisiblePosition) {
        mAdapter.addList(getMoreData());
    }

    @Override
    public void onRefresh() {
        mAdapter.refreshList(getData());
    }
}
