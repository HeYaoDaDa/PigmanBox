package com.example.none.pigmanbox.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.Consumer;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.example.none.pigmanbox.R;
import com.example.none.pigmanbox.adapter.GameModListAdapter;
import com.example.none.pigmanbox.base.BaseFragment;
import com.example.none.pigmanbox.modle.Game;
import com.example.none.pigmanbox.modle.Mod;
import com.example.none.pigmanbox.util.GameUtils;
import com.example.none.pigmanbox.util.ModUtils;
import com.example.none.pigmanbox.util.MyFileUtils;
import com.example.none.pigmanbox.util.PathUtils;
import com.example.none.pigmanbox.util.SettingUtils;

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

    public GameModListFragment() {
    }


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
            new ModigyZipTask(getActivity(), mGame, () -> {
                onLazyLoad();
                for (Mod mod : planDeleterModlist) {//删除mod的记录
                    mGame.getModList().remove(mod);
                }
                for (Mod mod : planAddModlist) {//添加mod的记录
                    mGame.getModList().add(mod);
                }
                planAddModlist.clear();
                planDeleterModlist.clear();
                mGameModListAdapter.notifyDataSetChanged();
            }).execute();
        });
    }

    static class ModigyZipTask extends AsyncTask<Void, String, Boolean> {
        private final WeakReference<Activity> mActity;
        private final Game mGame;
        private final Runnable mRunnable;
        private ProgressDialog mProgressDialog;
        private static int mSize;
        private static int mProgress;

        ModigyZipTask(Activity activity, Game game, Runnable mRunnable) {
            this.mActity = new WeakReference<>(activity);
            this.mGame = game;
            this.mRunnable = mRunnable;
        }

        @Override
        protected void onPreExecute() {
            mSize = 0;
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
            List<String> planDeleteStringList = getPlanDeleteString(mGame);
            List<File> planAddFileList = getPlanAddFile(mGame);

            mGame.getPlanAddModlist().clear();
            mGame.getPlanDeleterModlist().clear();

            File backFile = new File(zipFile.getPath() + ".bak");
            zipFile.renameTo(backFile);
            ZipFile backFileZip = null;
            try {
                backFileZip = new ZipFile(backFile);
                ZipOutputStream append = new ZipOutputStream(new FileOutputStream(zipFile));
                List<String> deleteZipEntry = new ArrayList<>();
                getDeleteZipEntry(deleteZipEntry, backFileZip, planDeleteStringList);

                initSize(backFileZip,planDeleteStringList,planAddFileList);
                copyBackZip(backFileZip,planDeleteStringList,append);
                // now append some extra content
                addZipFile(append, planAddFileList, "mods/", this::publishProgress);
                // close
                append.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            mProgressDialog.setTitle("(" + (++mProgress) + "/" + mSize + ")");
            mProgressDialog.setMessage("写入:" + values[0]);
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (!aBoolean){
                Toast.makeText(mActity.get(),"失败了。",Toast.LENGTH_SHORT).show();
            }
            mProgressDialog.hide();
            mRunnable.run();
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
         * copy backZipFile to zipFile
         * @param backFileZip backZipFile
         * @param deleteZipEntry deleteZipEntry
         * @param append apped
         * @throws IOException io
         */
        private void copyBackZip(ZipFile backFileZip, List<String> deleteZipEntry, ZipOutputStream append) throws IOException {
            Enumeration<? extends ZipEntry> entries = backFileZip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                //if e is plan delete entry
                if (deleteZipEntry.contains(e.getName())) {
                    continue;
                }
                append.putNextEntry(new ZipEntry(e.getName()));
                if (!e.isDirectory()) {
                    publishProgress(e.getName());
                    copy(backFileZip.getInputStream(e), append);
                }
                append.closeEntry();
            }
        }
        /**
         * init Size
         * @param backFileZip zipFile
         * @param deleteZipEntry plan delete
         * @param planAddFileList plan add
         */
        private void initSize(ZipFile backFileZip,List<String> deleteZipEntry,List<File> planAddFileList){
            Enumeration<? extends ZipEntry> entries = backFileZip.entries();
            for (File file : planAddFileList) {
                if (file.isDirectory()){
                    List<File> fileList = FileUtils.listFilesInDirWithFilter(file, pathname -> !pathname.isDirectory(), true);
                    mSize += fileList.size();
                }else {
                    mSize++;
                }
            }
            while (entries.hasMoreElements()) {
                ZipEntry e = entries.nextElement();
                if (deleteZipEntry.contains(e.getName())) {
                    continue;
                }
                if (!e.isDirectory())
                    mSize++;
            }
        }
        /**
         * get Plan Delete Mod File and delete modsetting
         * @param game game
         * @return mod dieName list
         */
        private static List<String> getPlanDeleteString(Game game){
            List<String> planDeleteStringList = new ArrayList<>();
            for (Mod mod : game.getPlanDeleterModlist()) {
                planDeleteStringList.add("mods/" + ModUtils.getModDirName(mod) + "/");
            }
            for (String data:SettingUtils.MOD_MODSETTINGS){
                planDeleteStringList.add("mods/"+data);
            }
            return planDeleteStringList;
        }

        /**
         * get Plan Add Mod File and ModSetting file.
         * @param game game
         * @return mod dirName
         */
        private static List<File> getPlanAddFile(Game game){
            List<File> planAddFileList = new ArrayList<>();
            for (Mod mod : game.getPlanAddModlist()) {
                planAddFileList.add(new File(PathUtils.modPath + ModUtils.getModDirName(mod)));
            }
            try {
                List<Mod> finishMod = game.getModList();
                finishMod.removeAll(game.getPlanDeleterModlist());
                finishMod.addAll(game.getPlanAddModlist());
                planAddFileList.addAll(MyFileUtils.createModsettings(finishMod));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return planAddFileList;
        }
        /**
         * get delete Zip All Entry
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
         * get delete Zip All Entry
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
                addZipFile(zipOutputStream, file, path, consumer);
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
                    addZipFile(zipOutputStream, file1, path + file.getName() + File.separator, consumer);
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
//            items[i] = ModUtils.getModDirName(canAddModList.get(i));
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
