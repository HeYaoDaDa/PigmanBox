package com.example.none.pigmanbox.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.none.pigmanbox.R;
import com.example.none.pigmanbox.adapter.MyViewPagerAdapter;
import com.example.none.pigmanbox.base.BaseFragment;
import com.example.none.pigmanbox.modle.Game;
import com.example.none.pigmanbox.util.GameUtils;

import net.lingala.zip4j.exception.ZipException;

import java.util.ArrayList;
import java.util.List;

import static com.blankj.utilcode.util.Utils.runOnUiThread;

public class GameListFragment extends BaseFragment {

    private TabLayout mTabLayout;
    private ProgressBar mProgressBar;
    private ViewPager mViewPager;

    private MyViewPagerAdapter mMyViewPagerAdapter;
    private List<String> title = new ArrayList<>();
    private List<Fragment> fragments = new ArrayList<>();
    @Override
    public void onLazyLoad() {
        new Thread(() -> {
            try {
                GameUtils.initGame();
                title.clear();
                fragments.clear();
                for (Game game : GameUtils.gameList) {
                    String pack = game.getPackName();
                    title.add(GameUtils.getGameName(pack));
                    Bundle bundle = new Bundle();
                    bundle.putString("pack", pack);
                    Fragment fragment = new GameModListFragment();
                    fragment.setArguments(bundle);
                    fragments.add(fragment);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                hideDialog();
            }
        }).start();
    }

    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_gamelist, container, false);
        mTabLayout = view.findViewById(R.id.fgm_TabLayout);
        mProgressBar = view.findViewById(R.id.fgm_ProgressBar);
        mViewPager = view.findViewById(R.id.fgm_ViewPager);
        return view;
    }

    @Override
    public void initEvent() {
        mMyViewPagerAdapter = new MyViewPagerAdapter(getChildFragmentManager(), title, fragments);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setAdapter(mMyViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));

        mTabLayout.setupWithViewPager(mViewPager);
    }

    /**
     * 隐藏对话框和刷新列表
     */
    public void hideDialog() {
        runOnUiThread(() -> {
            mMyViewPagerAdapter.notifyDataSetChanged();
            mProgressBar.setVisibility(View.GONE);
            initIcon();
        });
    }
    /**
     * 给标签加速icon
     */
    private void initIcon() {
        for (int i = 0; i < mTabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            tab.setIcon(GameUtils.getGameIcon(GameUtils.gameList.get(i).getPackName()));
        }
    }
}
