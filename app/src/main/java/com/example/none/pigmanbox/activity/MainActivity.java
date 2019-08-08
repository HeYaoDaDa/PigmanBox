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
import android.util.Log;
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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initEvent();
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
