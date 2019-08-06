package com.example.none.pigmanbox.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Consumer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import com.example.none.pigmanbox.util.PathUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import static com.blankj.utilcode.util.Utils.runOnUiThread;

public class GameModListFragment extends BaseFragment {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFloatingActionButton;
    private FloatingActionButton mFloatingActionButton2;
    private TextView mTextView;

    private GameModListAdapter mGameModListAdapter;
    private Game mGame;
    private List<Mod> planAddModlist;
    private List<Mod> planDeleterModlist;
    @SuppressLint("HandlerLeak")
    private Handler mHandler = null;

    public GameModListFragment() {
    }

    @SuppressLint("HandlerLeak")
    @Override
    public void onLazyLoad() {
        mSwipeRefreshLayout.setRefreshing(true);
        new Thread(() -> {
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
        mFloatingActionButton.setOnClickListener(v -> showMultiSelect());
        mFloatingActionButton2.setOnClickListener(v -> {
            new ModigyZipTask(getActivity(),mGame).execute();
        });
    }

    static class ModigyZipTask extends AsyncTask<Void,String,Boolean>{
        private final WeakReference<Activity> mActity;
        private final Game mGame;
        private ProgressDialog mProgressDialog;
        private static int mSize;
        private static int mProgress;

        ModigyZipTask(Activity activity,Game game) {
            this.mActity = new WeakReference<>(activity);
            this.mGame = game;
        }

        @Override
        protected void onPreExecute() {
            mSize =0;
            mProgress = 0;
            mProgressDialog = new ProgressDialog(mActity.get());
            mProgressDialog.setIcon(GameUtils.getGameIcon(mGame.getPackName()));
            mProgressDialog.setTitle("莫着急，正在干活。");
            mProgressDialog.setMessage("启动中。。。。。");
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setCancelable(false);
            mProgressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... games) {
            File zipFile = mGame.getObbFile();
            String path = "mods/";
            List<String> planDeleteStringList = new ArrayList<>();
            List<File> planAddFileList = new ArrayList<>();
            for (Mod mod : mGame.getPlanDeleterModlist()) {
                planDeleteStringList.add(path + mod.getName()+"/");
            }
            for (Mod mod : mGame.getPlanAddModlist()) {
                planAddFileList.add(new File(PathUtils.modPath + mod.getName()));
            }
            File backFile = new File(zipFile.getPath() + ".bak");
            zipFile.renameTo(backFile);
            ZipFile backFileZip = null;
            try {
                backFileZip = new ZipFile(backFile);
                ZipOutputStream append = new ZipOutputStream(new FileOutputStream(zipFile));
                List<String> deleteZipEntry = new ArrayList<>();
                getDeleteZipEntry(deleteZipEntry, backFileZip, planDeleteStringList);
                // first, copy contents from existing war
                Enumeration<? extends ZipEntry> entries = backFileZip.entries();
                mSize-=planDeleteStringList.size();
                mSize+=planAddFileList.size();
                while (entries.hasMoreElements()){
                    entries.nextElement();
                    mSize++;
                }
                entries = backFileZip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry e = entries.nextElement();
                    //if e is plan delete entry
                    if (deleteZipEntry.contains(e.getName())) {
                        continue;
                    }
                    append.putNextEntry(new ZipEntry(e.getName()));
                    if (!e.isDirectory()) {
                        copy(backFileZip.getInputStream(e), append);
                    }
                    publishProgress(e.getName());
                    Log.d("hydd", "addEntry:"+e.getName());
                    append.closeEntry();
                }
                // now append some extra content
                addZipFile(append, planAddFileList, path,this::publishProgress);
                // close
                append.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("hydd", "erorr::"+e.getLocalizedMessage());
            }
            return false;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setTitle("("+(++mProgress)+"/"+mSize+")");
            mProgressDialog.setMessage("写入:"+values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mProgressDialog.hide();
        }

        static final byte[] BUFFER = new byte[4096 * 1024];
        /**
         * copy input to output stream - available in several StreamUtils or Streams classes
         */
        private static void copy(InputStream input, OutputStream output) throws IOException {
            int bytesRead;
            while ((bytesRead = input.read(BUFFER)) != -1) {
                output.write(BUFFER, 0, bytesRead);
            }
        }

        /**
         * get delete Zip Entry
         *
         * @param zipEntryList delete Zip Entry
         * @param zipFile      old zip
         * @param stringList   delete zip file
         */
        private static void getDeleteZipEntry(List<String> zipEntryList, ZipFile zipFile, List<String> stringList) {
            for (String s : stringList) {
                getDeleteZipEntry(zipEntryList, zipFile, new ZipEntry(s));
            }
        }

        /**
         * get delete Zip Entry
         *
         * @param zipEntryList delete Zip Entry
         * @param zipFile      old zip
         * @param zipEntry     delete zip entry
         */
        private static void getDeleteZipEntry(List<String> zipEntryList, ZipFile zipFile, ZipEntry zipEntry) {
            if (!zipEntry.isDirectory()) {
                zipEntryList.add(zipEntry.getName());
                return;
            }
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry nowZipEntry = entries.nextElement();
                if (nowZipEntry.getName().startsWith(zipEntry.getName())) {
                    zipEntryList.add(nowZipEntry.getName());
                }
            }
        }

        /**
         * zip added fileEntry
         *
         * @param zipOutputStream zip out stream
         * @param fileList        plan add file list
         * @param path            zip path
         * @throws IOException io
         */
        private static void addZipFile(ZipOutputStream zipOutputStream, List<File> fileList, String path, Consumer<String> consumer) throws IOException {
            for (File file : fileList) {
                addZipFile(zipOutputStream, file, path,consumer);
            }
        }

        /**
         * zip added fileEntry
         *
         * @param zipOutputStream zip out stream
         * @param file            plan add file
         * @param path            zip path
         * @throws IOException io
         */
        private static void addZipFile(ZipOutputStream zipOutputStream, File file, String path, Consumer<String> consumer) throws IOException {
            if (file.isDirectory()) {
                for (File file1 : file.listFiles()) {
                    addZipFile(zipOutputStream, file1, path + file.getName() + File.separator,consumer);
                }
            } else {
                BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(file));
                ZipEntry zipEntry = new ZipEntry(path + file.getName());
                zipOutputStream.putNextEntry(zipEntry);

                int len;
                byte[] buffer = new byte[1024];
                while ((len = inputStream.read(buffer)) > 0) {
                    zipOutputStream.write(buffer, 0, len);
                }
                consumer.accept(zipEntry.getName());
                zipOutputStream.closeEntry();
            }
        }
    }

    /**
     * init button2
     */
    @SuppressLint("RestrictedApi")
    private void initSaveButton() {
        initData();
        if (planAddModlist.size() > 0 || planDeleterModlist.size() > 0) {
            mFloatingActionButton2.setVisibility(View.VISIBLE);
        } else {
            mFloatingActionButton2.setVisibility(View.GONE);
        }
    }

    /**
     * init ViewText
     */
    private void initTextView() {
        initData();
        if (mGame.getModList().size() > 0 || planAddModlist.size() > 0 || planDeleterModlist.size() > 0) {
            mTextView.setVisibility(View.GONE);
        } else {
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

        builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()), R.style.Theme_AppCompat_DayNight_Dialog)
                .setMessage(items.length > 0 ? null : "没有找到未安装的mod")
                .setTitle(mGame.getGameName())
                .setIcon(GameUtils.getGameIcon(mGame.getPackName()))
                .setMultiChoiceItems(items, isSelect, (dialogInterface, i, b) -> {
                    if (b) {
                        choice.add(i);
                    } else {
                        choice.remove(i);
                    }
                }).setPositiveButton("确定", (dialogInterface, i) -> {
                    for (int j = 0; j < choice.size(); j++) {
                        //将选择的mod列表添加到计划添加mod列表中
                        planAddModlist.add(canAddModList.get(choice.get(j)));
                        mGameModListAdapter.notifyDataSetChanged();
                    }
                    //对计划添加的mod列表进行了操作所以这里进行了第二个浮动按钮的更新
                    initSaveButton();
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
            if (canAddModList.indexOf(mod) > -1) {
                Mod buffMod = canAddModList.get(canAddModList.indexOf(mod));
                if (buffMod != null) {
                    canAddModList.remove(buffMod);
                }
            }
        }
        return canAddModList;
    }

}
