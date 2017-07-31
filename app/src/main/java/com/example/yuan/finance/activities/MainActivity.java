package com.example.yuan.finance.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import com.example.yuan.finance.MyTabLayout;
import com.example.yuan.finance.R;
import com.example.yuan.finance.adapters.MyFragmentPagerAdapter;

// TODO: 2017/04/17 取消滑動刪除
// TODO: 2017/04/17 備註改成卡片，備註改成icon info 
// TODO: 2017/04/17 刪除改在dialog 
// TODO: 2017/04/17 滑動翻月 
// TODO: 2017/04/17  圓餅圖統計月(選項卡)
// TODO: 2017/04/17 主題
public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private MyTabLayout mTabLayout;
    private ViewPager mViewPager;
    private MyFragmentPagerAdapter myFragmentPagerAdapter;
    private TabLayout.Tab tabDetail;
    private TabLayout.Tab tabChart;

    public void onCreate(@Nullable Bundle savedInstanceState) {

        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        mTabLayout = (MyTabLayout) findViewById(R.id.tabLayout);
        mViewPager = (ViewPager) findViewById(R.id.viewPager);
        myFragmentPagerAdapter = new MyFragmentPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(myFragmentPagerAdapter);
        mViewPager.setOnPageChangeListener(mViewPagerChangeListener);
        mTabLayout.setupWithViewPager(mViewPager);

        tabDetail = mTabLayout.getTabAt(0);
        tabChart = mTabLayout.getTabAt(1);

        tabDetail.setIcon(R.drawable.detail_tab_selector);
        tabChart.setIcon(R.drawable.chart_tab_selector);
    }

    ViewPager.OnPageChangeListener mViewPagerChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override public void onPageSelected(int position) {
            Log.d(TAG, "onPageSelected: ");
            myFragmentPagerAdapter.notifyDataSetChanged();
            tabDetail.setIcon(R.drawable.detail_tab_selector);
            tabChart.setIcon(R.drawable.chart_tab_selector);
        }

        @Override public void onPageScrollStateChanged(int state) {

        }
    };
}
