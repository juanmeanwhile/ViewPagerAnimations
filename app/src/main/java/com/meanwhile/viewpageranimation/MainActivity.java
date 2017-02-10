package com.meanwhile.viewpageranimation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private AnimViewPager mPager;
    private Button mButton;
    private MyAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPager = (AnimViewPager) findViewById(R.id.pager);
        mAdapter = new MyAdapter(getSupportFragmentManager());
        mPager.setAdapter(mAdapter);
        int pageMargin = getResources().getDimensionPixelOffset(R.dimen.journey_card_margin);
        mPager.setClipToPadding(false);
        mPager.setPadding(pageMargin,0,pageMargin,0);
        mPager.setPageMargin(pageMargin/2);
        mPager.setOffscreenPageLimit(3);

        ArrayList<Item> list = new ArrayList<Item>();
        list.addAll(Arrays.asList(new Item(1, Color.BLUE), new Item(2, Color.RED), new Item(3, Color.YELLOW), new Item(4, Color.GREEN)));
        mAdapter.setData(list);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            int count = 0;
            int[] colors = new int[]{Color.DKGRAY, Color.LTGRAY, Color.CYAN};
            @Override
            public void onClick(View v) {
                //replaceFragment(Color.CYAN, mPager.getCurrentItem());

                mPager.replaceFragment(1, new Item(5, Color.DKGRAY), new Item(6, Color.LTGRAY));
                //removeFragment(mPager.getCurrentItem());
                count++;
            }
        });
    }

    public class MyAdapter extends AnimViewPagerAdapter<Item> {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getIdForObject(Item item) {
            return item.id;
        }

        @Override
        protected Fragment getFragmentForItem(Item item) {
            return BlankFragment.newInstance(item);
        }
    }

    public class Item {
        int id;
        int color;
        public Item(int id, int color) {
            this.id = id;
            this.color = color;
        }

        @Override
        public boolean equals(Object object) {
            if (object instanceof Item) {
                return ((Item)object).id == this.id;
            }
            return false;
        }
    }
}
