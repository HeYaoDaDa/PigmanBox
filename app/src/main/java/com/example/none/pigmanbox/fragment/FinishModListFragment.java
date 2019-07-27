package com.example.none.pigmanbox.fragment;

import android.app.ProgressDialog;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.none.pigmanbox.adapter.FinishModListAdapter;
import com.example.none.pigmanbox.R;
import com.example.none.pigmanbox.base.BaseFragment;
import com.example.none.pigmanbox.util.ModUtils;

public class FinishModListFragment extends BaseFragment {
    RecyclerView mRecyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    FloatingActionButton mFloatingActionButton;

    ProgressDialog mProgressDialog;

    FinishModListAdapter mFinishModListAdapter;
    @Override
    public void onLazyLoad() {
        new Thread(()->{
            try {
                ModUtils.initFileModList();
            } catch (Exception e) {
                Toast.makeText(getContext(),"数组满了",Toast.LENGTH_LONG).show();
            }
            mFinishModListAdapter.notifyDataSetChanged();
            mSwipeRefreshLayout.setRefreshing(false);
        }).start();
    }

    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_finishmodlist, container, false);
        mRecyclerView = view.findViewById(R.id.ffml_RecylerView);
        mSwipeRefreshLayout = view.findViewById(R.id.ffml_SwipereFreshLayout);
        mFloatingActionButton = view.findViewById(R.id.ffml_FloatingActionButton);
        return view;
    }

    @Override
    public void initEvent() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mFinishModListAdapter = new FinishModListAdapter(ModUtils.mods);
        mRecyclerView.setAdapter(mFinishModListAdapter);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(this::onLazyLoad);
        mFloatingActionButton.setOnClickListener(v -> {
            ModUtils.mods[4444].getId();
        });
    }

    /**
     * 显示圆圈加载进度的 dialog
     */
    public void showProgressDialog() {
        mProgressDialog = new ProgressDialog(getContext(),R.style.Theme_AppCompat_DayNight_Dialog);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle("加载中，请稍后");
        mProgressDialog.setMessage("加载中...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }
}
