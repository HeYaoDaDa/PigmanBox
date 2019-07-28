package com.example.none.pigmanbox.fragment;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.none.pigmanbox.R;
import com.example.none.pigmanbox.adapter.GameModListAdapter;
import com.example.none.pigmanbox.base.BaseFragment;
import com.example.none.pigmanbox.modle.Game;
import com.example.none.pigmanbox.modle.Mod;
import com.example.none.pigmanbox.util.GameUtils;
import com.example.none.pigmanbox.util.ModUtils;

import java.util.ArrayList;
import java.util.List;

import static com.blankj.utilcode.util.Utils.runOnUiThread;

public class GameModListFragment extends BaseFragment {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFloatingActionButton;
    private FloatingActionButton mFloatingActionButton2;
    private TextView mTextView;

    private ProgressDialog mProgressDialog;

    private GameModListAdapter mGameModListAdapter;
    private Game mGame;
    private List<Mod> planAddModlist;
    private List<Mod> planDeleterModlist;

    public GameModListFragment() {
    }

    @Override
    public void onLazyLoad() {
        mSwipeRefreshLayout.setRefreshing(true);
        new Thread(()->{
            initData();
            try {
                GameUtils.initModlist(mGame);
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                initSaveButton();
                initTextView();
                mSwipeRefreshLayout.setRefreshing(false);
            });
        }).start();

    }

    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_gamemodlist, container, false);

        mRecyclerView = view.findViewById(R.id.fgml_RecyclerView);
        mSwipeRefreshLayout = view.findViewById(R.id.fgml_SwipeRefreshLayout);
        mFloatingActionButton = view.findViewById(R.id.fgml_FloatingActionButton);
        mFloatingActionButton2 = view.findViewById(R.id.fgml_FloatingActionButton2);
        mTextView = view.findViewById(R.id.fgml_TextView);

        initData();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mGameModListAdapter = new GameModListAdapter(mGame.getModList(), planAddModlist, planDeleterModlist, this::initSaveButton);
        mRecyclerView.setAdapter(mGameModListAdapter);
        mSwipeRefreshLayout.setOnRefreshListener(this::onLazyLoad);
        return view;
    }

    @Override
    public void initEvent() {

    }

    /**
     * init button2
     */
    @SuppressLint("RestrictedApi")
    private void initSaveButton(){
        initData();
        if (planAddModlist.size() > 0 || planDeleterModlist.size() > 0) {
            mFloatingActionButton2.setVisibility(View.VISIBLE);
            mFloatingActionButton2.setOnClickListener(v -> {
                showMultiSelect();
//                    ModUtil.updataGameMod(game,handler);
            });
        }else {
            mFloatingActionButton2.setVisibility(View.GONE);
        }
    }

    /**
     * init ViewText
     */
    private void initTextView(){
        initData();
        if (mGame.getModList().size()>0||planAddModlist.size() > 0 || planDeleterModlist.size() > 0){
            mTextView.setVisibility(View.GONE);
        }else {
            mTextView.setVisibility(View.VISIBLE);
        }
    }
    /**
     * initData
     */
    private void initData() {
        Bundle bundle = getArguments();
        if (bundle == null)
            return;
        String pack = bundle.getString("pack");
        for (Game game : GameUtils.gameList) {
            if (game.getPackName().equals(pack)) {
                this.mGame = game;
                break;
            }
        }
        planAddModlist = mGame.getPlanAddModlist();
        planDeleterModlist = mGame.getPlanDeleterModlist();
    }

    /**
     * dialog
     */
    private void showMultiSelect() {
        AlertDialog.Builder builder;
        final List<Integer> choice = new ArrayList<>();
        final List<Mod> canAddModList = getCanAddModList();
        //初始化mod名称列表和选中
        final String[] items = new String[canAddModList.size()];
        boolean[] isSelect = new boolean[canAddModList.size()];
        for (int i = 0; i < canAddModList.size(); i++) {
            items[i] = canAddModList.get(i).getName();
            isSelect[i] = false;
        }

        builder = new AlertDialog.Builder(getContext(),R.style.Theme_AppCompat_DayNight_Dialog)
                .setTitle(mGame.getGameName())
                .setIcon(GameUtils.getGameIcon(mGame.getPackName()))
                .setMultiChoiceItems(items, isSelect, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                        if (b) {
                            choice.add(i);
                        } else {
                            choice.remove(i);
                        }

                    }
                }).setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for (int j = 0; j < choice.size(); j++) {
                            //将选择的mod列表添加到计划添加mod列表中
                            planAddModlist.add(canAddModList.get(choice.get(j)));
                            mGameModListAdapter.notifyDataSetChanged();
                        }
                        //对计划添加的mod列表进行了操作所以这里进行了第二个浮动按钮的更新
                        initSaveButton();
                    }
                });

        builder.create().show();

    }

    /**
     * get can install mod
     *
     * @return can install mod
     */
    private List<Mod> getCanAddModList() {
        List<Mod> planFindModList = mGame.getModList();
        planFindModList.addAll(mGame.getPlanAddModlist());
        List<Mod> canAddModList = new ArrayList<>(ModUtils.getFinishMods());
        for (Mod mod : planFindModList) {
            Mod buffMod = canAddModList.get(canAddModList.indexOf(mod));
            if (buffMod != null) {
                canAddModList.remove(buffMod);
            }
        }
        return canAddModList;
    }
}
