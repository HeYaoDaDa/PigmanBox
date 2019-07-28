package com.example.none.pigmanbox.fragment;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.FileUtils;
import com.example.none.pigmanbox.R;
import com.example.none.pigmanbox.adapter.FinishModListAdapter;
import com.example.none.pigmanbox.base.BaseFragment;
import com.example.none.pigmanbox.modle.Mod;
import com.example.none.pigmanbox.util.ModUtils;
import com.example.none.pigmanbox.util.PathUtils;
import com.example.none.pigmanbox.util.SettingUtils;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.blankj.utilcode.util.Utils.runOnUiThread;

public class FinishModListFragment extends BaseFragment {
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FloatingActionButton mFloatingActionButton;
    private TextView mTextView;

    private ProgressDialog mProgressDialog;

    private List<Mod> mModList = ModUtils.getFinishMods();

    private FinishModListAdapter mFinishModListAdapter;
    private AlertDialog.Builder mBuilder;
    @Override
    public void onLazyLoad() {
        new Thread(() -> {
            try {
                ModUtils.initFileModListTest();
            } catch (Exception e) {
                Toast.makeText(getContext(), "数组满了", Toast.LENGTH_LONG).show();
            }
            runOnUiThread(() -> {
                mModList.clear();
                mModList.addAll(ModUtils.getFinishMods());
                initTextView();
                mFinishModListAdapter.notifyDataSetChanged();
                mSwipeRefreshLayout.setRefreshing(false);
            });
        }).start();
    }

    @Override
    public View initView(LayoutInflater inflater, @Nullable ViewGroup container) {
        View view = inflater.inflate(R.layout.fragment_finishmodlist, container, false);
        mRecyclerView = view.findViewById(R.id.ffml_RecylerView);
        mSwipeRefreshLayout = view.findViewById(R.id.ffml_SwipereFreshLayout);
        mFloatingActionButton = view.findViewById(R.id.ffml_FloatingActionButton);
        mTextView = view.findViewById(R.id.ffml_TextView);
        return view;
    }

    @Override
    public void initEvent() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mFinishModListAdapter = new FinishModListAdapter(mModList);
        mRecyclerView.setAdapter(mFinishModListAdapter);
        mSwipeRefreshLayout.setRefreshing(true);
        mSwipeRefreshLayout.setOnRefreshListener(this::onLazyLoad);
        mFloatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("file/*.zip");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            startActivityForResult(intent, 10);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null) {
            if (mProgressDialog!=null)
                mProgressDialog.hide();
            Toast.makeText(getContext(), "没有选择文件。", Toast.LENGTH_SHORT).show();
            return;
        }
        showProgressDialog();

        String path;
        Uri uri = data.getData();
        path = uri.getPath();
        if (path.endsWith(SettingUtils.MOD_MODINFO_NAME)){
            File file = new File(path);
            try {
                Mod mod = ModUtils.createMod(ModUtils.readModInfoList(file),file.getParentFile().getName(),true);
                if (ModUtils.mods[mod.getId()]!=null&&ModUtils.mods[mod.getId()].isExist()){
                    mProgressDialog.hide();
                    showModConflict(mod,ModUtils.mods[mod.getId()],()-> loadMod(file.getParentFile(),mod));
                    return;
                }
                initTextView();
                loadMod(file.getParentFile(),mod);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "文件打开失败。", Toast.LENGTH_SHORT).show();
            }
            return;
        }else {
            try {
                ZipFile zipFile = new ZipFile(path);
                if (zipFile.isEncrypted()) {
                    zipFile.setPassword(SettingUtils.ZIP_PASSWOID);
                }
                if (!ModUtils.isMod(zipFile)) {
                    Toast.makeText(getContext(), "选择的压缩包不为mod", Toast.LENGTH_SHORT).show();
                    return;
                }
                Mod mod = ModUtils.createMod(ModUtils.readModInfoList(zipFile), zipFile.getFile().getName(), true);
                if (ModUtils.mods[mod.getId()] != null && ModUtils.mods[mod.getId()].isExist()) {
                    mProgressDialog.hide();
                    showModConflict(mod, ModUtils.mods[mod.getId()], () -> loadMod(zipFile, mod));
                    return;
                }
                initTextView();
                loadMod(zipFile, mod);
            } catch (Exception e) {
                Toast.makeText(getContext(), "选择的文件不为压缩包。", Toast.LENGTH_SHORT).show();
                mProgressDialog.hide();
            }
        }
    }

    /**
     * init ViewText
     */
    private void initTextView(){
        if (mModList.size() > 0){
            mTextView.setVisibility(View.GONE);
        }else {
            mTextView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * load Mod to app/mod
     * @param zipFile mod file
     * @param mod mod
     */
    private void loadMod(ZipFile zipFile,Mod mod){
        ModUtils.mods[mod.getId()] = mod;
        ModUtils.unzipModZip(zipFile, mod, () -> runOnUiThread(() -> {
            mModList.clear();
            mModList.addAll(ModUtils.getFinishMods());
            mFinishModListAdapter.notifyDataSetChanged();
            mProgressDialog.hide();
        }));
    }

    /**
     * load mod to app/mod
     * @param file modinfo.lua
     * @param mod mod object
     */
    private void loadMod(File file,Mod mod){
        ModUtils.mods[mod.getId()] = mod;
        String path = PathUtils.modPath + ModUtils.getModDirName(mod);
        new Thread(new Runnable() {
            @Override
            public void run() {
                FileUtils.moveDir(file, new File(PathUtils.modPath + ModUtils.getModDirName(mod)), () -> {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mModList.clear();
                            mModList.addAll(ModUtils.getFinishMods());
                            mFinishModListAdapter.notifyDataSetChanged();
                            mProgressDialog.hide();
                        }
                    });
                    return true;
                });
            }
        }).start();
    }
    /**
     * progressDialog
     */
    private void showProgressDialog() {
        mProgressDialog = new ProgressDialog(getContext(), R.style.Theme_AppCompat_DayNight_Dialog);
        mProgressDialog.setIcon(R.mipmap.ic_launcher);
        mProgressDialog.setTitle("加载中，请稍后");
        mProgressDialog.setMessage("加载中...");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

    }
    /**
     * mod conflict Dialog
     */
    private void showModConflict(Mod oldMod,Mod newMod,Runnable runnable) {

         mBuilder = new AlertDialog.Builder(getActivity(),R.style.Theme_AppCompat_DayNight_Dialog)
                 .setIcon(R.mipmap.ic_launcher)
                 .setTitle("选择的mod已存在")
                .setMessage("老Mod->新Mod"+"\n"+
                        oldMod.getId()+"->"+newMod.getId()+"\n"+
                        oldMod.getName()+"->"+newMod.getName()+"\n"+
                        oldMod.getAuthor()+"->"+newMod.getAuthor()+"\n"+
                        oldMod.getDescription()+"->"+newMod.getDescription()+"\n"+
                        oldMod.getTags()+"->"+newMod.getTags()+"\n"+
                        oldMod.isExist()+"->"+newMod.isExist()+"\n"
                )
                 .setPositiveButton("确定", (dialogInterface, i) -> {
                     showProgressDialog();
                     runnable.run();
                })
                 .setNegativeButton("取消", (dialogInterface, i) -> dialogInterface.dismiss());
        mBuilder.create().show();
    }
}
