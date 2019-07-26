package com.example.none.pigmanbox.activity;

import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.none.pigmanbox.R;
import com.example.none.pigmanbox.util.PathUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private BottomNavigationView mBottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
        initEvent();
        Log.d(TAG, "onCreate: " + PathUtils.fileRootPath);
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
                break;
            case R.id.action_help:
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
            private Fragment[] mFragments = new Fragment[]{};

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
}
