package com.example.none.pigmanbox.fragment;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.none.pigmanbox.R;
import com.example.none.pigmanbox.base.BaseFragment;

public class GameListFragment extends BaseFragment {
    @Override
    public void onLazyLoad() {

    }

    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_gamelist, container, false);
        return view;
    }

    @Override
    public void initEvent() {

    }
}
