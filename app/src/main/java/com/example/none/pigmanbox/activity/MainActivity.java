package com.example.none.pigmanbox.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.none.pigmanbox.R;
import com.example.none.pigmanbox.fragment.FinishModListFragment;
import com.example.none.pigmanbox.fragment.GameListFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;
    private AlertDialog.Builder mBuilder;

    //申请两个权限,文件读写
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<>();
    private final int mRequestCode = 100;//权限请求码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //只在6.0才用动态申请
        if (Build.VERSION.SDK_INT >= 23)
            initPermission();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_money:
                showTwo();
                break;
            case R.id.action_help:
                showHelp();
                break;
        }
        return true;
    }
    //权限判断和申请
    private void initPermission() {

        mPermissionList.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        } else {
            initView();
            initData();
            initEvent();
        }
    }

    /**
     * 不再提示权限时的展示对话框
     */
    AlertDialog mPermissionDialog;
    String mPackName = "com.huawei.liwenzhi.weixinasr";

    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", (dialog, which) -> {
                        cancelPermissionDialog();

                        Uri packageURI = Uri.parse("package:" + mPackName);
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                        startActivity(intent);
                    })
                    .setNegativeButton("取消", (dialog, which) -> {
                        //关闭页面或者做其他操作
                        cancelPermissionDialog();

                    })
                    .create();
        }
        mPermissionDialog.show();
    }

    //关闭对话框
    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showPermissionDialog();//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
            } else {
                //全部权限通过，可以进行下一步操作。。。
                initView();
                initData();
                initView();
            }
        }
    }
    private void initView() {
        mToolbar = findViewById(R.id.am_Toolbar);
        mViewPager = findViewById(R.id.am_ViewPager);
        mBottomNavigationView = findViewById(R.id.am_BottonNavigationView);

        setSupportActionBar(mToolbar);

    }

    private void initData() {
    }

    private void initEvent() {
        mViewPager.addOnPageChangeListener(
                new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int i, float v, int i1) {

                    }

                    @Override
                    public void onPageSelected(int i) {
                        if (i == 0)
                            mBottomNavigationView.setSelectedItemId(R.id.navigation_down);
                        else
                            mBottomNavigationView.setSelectedItemId(R.id.navigation_file);
                    }

                    @Override
                    public void onPageScrollStateChanged(int i) {

                    }
                }
        );
        mViewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
            private Fragment[] mFragments = new Fragment[]{new FinishModListFragment(), new GameListFragment()};

            @Override
            public Fragment getItem(int i) {
                return mFragments[i];
            }

            @Override
            public int getCount() {
                return mFragments.length;
            }
        });

        mBottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            switch (item.getItemId()) {
                case R.id.navigation_down:
                    mViewPager.setCurrentItem(0);
                    return true;
                case R.id.navigation_file:
                    mViewPager.setCurrentItem(1);
                    return true;
            }
            return false;
        });
        mBottomNavigationView.setSelectedItemId(mViewPager.getId());

    }
    private void showTwo() {
        mBuilder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog).setIcon(R.mipmap.ic_launcher).setTitle("捐赠")
                .setMessage("二维码").setPositiveButton("支付宝", (dialogInterface, i) -> {
                    Uri uri = Uri.parse("https://hydd.gitee.io/picture_storage/ewm/zfb.jpg");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }).setNegativeButton("微信", (dialogInterface, i) -> {
                    Uri uri = Uri.parse("https://hydd.gitee.io/picture_storage/ewm/wx.png");
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                });
        mBuilder.create().show();
    }

    private void showHelp() {
        mBuilder = new AlertDialog.Builder(this, R.style.Theme_AppCompat_DayNight_Dialog).setIcon(R.mipmap.ic_launcher).setTitle("帮助")
                .setMessage("***使用前请确认游戏已破解数据包验证不然会导致游戏无法打开。\n" +
                        "***游戏需要安装dsmm补丁否则无效\n" +
                        "***如出现问题可手动恢复数据包备份\n" +
                        "1,下载mod或者导入本地mod（导入页面）\n" +
                        "2,在游戏列表中管理mod\n" +
                        "3,单击保存按钮并等待。").setPositiveButton("返回", (dialogInterface, i) -> {
                        });
        mBuilder.create().show();
    }
}
