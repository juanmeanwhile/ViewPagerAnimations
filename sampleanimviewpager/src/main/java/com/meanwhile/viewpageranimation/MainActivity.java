package com.meanwhile.viewpageranimation;

import android.app.Fragment;
import android.app.FragmentManager;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.meanwhile.animviewpager.AnimViewPager;
import com.meanwhile.animviewpager.AnimViewPagerAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

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
        mAdapter = new MyAdapter(getFragmentManager());
        mPager.setAdapter(mAdapter);
        int pageMargin = getResources().getDimensionPixelOffset(R.dimen.journey_card_margin);
        mPager.setClipToPadding(false);
        mPager.setPadding(pageMargin,0,pageMargin,0);
        mPager.setPageMargin(pageMargin/2);
        mPager.setOffscreenPageLimit(3);

        ArrayList<Item> list = new ArrayList<Item>();
        list.addAll(Arrays.asList(new Item(1, Color.BLUE), new Item(2, Color.RED), new Item(3, Color.YELLOW), new Item(4, Color.GREEN), new Item(5, Color
                .CYAN), new Item(6, Color.BLACK), new Item(7, Color.DKGRAY), new Item(8, Color.LTGRAY), new Item(9, 747474)));
        mAdapter.setData(list);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            int count = 100;
            int[] colors = new int[]{Color.DKGRAY, Color.LTGRAY, Color.CYAN};
            @Override
            public void onClick(View v) {
                //replaceFragment(Color.CYAN, mPager.getCurrentItem());

                mPager.replaceAndAddAfter(1, new Item(count++, Color.DKGRAY), new Item(count++, Color.LTGRAY));
                //removeFragment(mPager.getCurrentItem());
                //mPager.replaceAndDeleteBefore(mPager.getCurrentItem(), 2, 2, new Item(count, Color.DKGRAY));
                count++;
            }
        });
    }

    public class MyAdapter extends AnimViewPagerAdapter<Item, String> {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public String getIdForObject(Item item) {
            return item.id;
        }

        @Override
        protected Fragment getFragmentForItem(Item item) {
            return BlankFragment.newInstance(item);
        }
    }

    public class Item {
        String id;
        int color;
        public Item(int id, int color) {
            this.id = ""+id;
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
