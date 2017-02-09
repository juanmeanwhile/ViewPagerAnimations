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
    private static final float ENTER_DISTANCE = -1600;
    private ViewPager mPager;
    private Button mButton;
    private MyAdapter mAdapter;
    private HashMap<Integer, Integer> mIdPosMap = new HashMap<Integer, Integer>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPager = (ViewPager) findViewById(R.id.pager);
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

                replaceFragment(1, new Item(5, Color.DKGRAY), new Item(6, Color.LTGRAY));
                //removeFragment(mPager.getCurrentItem());
                count++;
            }
        });
    }

    private void printData() {
        Log.d(TAG, "scroll: " + mPager.getScrollX());
        Log.d(TAG, "x:" + mPager.getX());
        Log.d(TAG, "paddingLeft:" + mPager.getPaddingLeft());
    }


    private void removeFragment(final int pos) {
        Log.d(TAG, "removeFragment");

        AnimatorSet hideAnim = new AnimatorSet();
        hideAnim.play(ObjectAnimator.ofFloat(mAdapter.getRegisteredFragment(pos).getView(), View.ALPHA, 1, 0));
        hideAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                doRemove(pos);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        hideAnim.start();
    }

    private HashMap<Integer, Integer> getInitialPositionMap(int firstPos){
        final HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

        ArrayList<Integer> sortedPositions = sortViewByLeft();

        //There might be less views than adapter size. Calculate first position in the adapter corresponding to the first visible view
        for (int i = 0; i < mPager.getChildCount() ; i++){
            int visiblePosInView = sortedPositions.get(i);
            View child = mPager.getChildAt(visiblePosInView);
            int itemId = mAdapter.getItemId(firstPos + visiblePosInView);
            map.put(itemId, child.getLeft() - (mAdapter.getCount() > 1?mPager.getScrollX():0));
        }

        return map;
    }

    private void doRemove(int position) {
        final int firstPos = Math.max(0, mPager.getCurrentItem() - mPager.getOffscreenPageLimit());
        final HashMap<Integer, Integer> leftValueMap = getInitialPositionMap(firstPos);

        mAdapter.removeItem(position);

        mPager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d(TAG, "onPreDraw");
                mPager.getViewTreeObserver().removeOnPreDrawListener(this);

                ArrayList<Integer> sortedPositions = sortViewByLeft();
                for (int i = 0; i < mPager.getChildCount() ; i++){
                    int visiblePosInView = sortedPositions.get(i);
                    View child = mPager.getChildAt(visiblePosInView);
                    int itemId = mAdapter.getItemId(firstPos + i);

                    Integer startPos = leftValueMap.get(itemId);
                    int currentPos = child.getLeft();
                    if (startPos != currentPos) {
                        ObjectAnimator.ofFloat(child, View.TRANSLATION_X, startPos - currentPos, 0).start();
                    }
                }
                return false;
            }});
    }

    private ArrayList<Integer> sortViewByLeft() {
        ArrayList<Integer> sortedPos = new ArrayList<Integer>();
        for (int i = 0; i < mPager.getChildCount(); i++){
            boolean added = false;
            for (int j = 0; j < sortedPos.size(); j++) {
                if (mPager
                        .getChildAt(i)
                        .getLeft() < mPager.getChildAt(sortedPos.get(j)).getLeft()) {
                    sortedPos.add(j, i);
                    added = true;
                    break;
                }
            }
            if (!added) {
                sortedPos.add(sortedPos.size(), i);
            }
        }

        return sortedPos;
    }

    private void replaceFragment(final int position, final Item...items) {
        AnimatorSet removeAnim = new AnimatorSet();
        View v = mAdapter.getRegisteredFragment(position).getView();
        removeAnim.playTogether(ObjectAnimator.ofFloat(v, View.ALPHA, 1, 0 ),
                                ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, 0, -ENTER_DISTANCE),
                                ObjectAnimator.ofFloat(v, View.SCALE_X, 1, 0.9f),
                                ObjectAnimator.ofFloat(v, View.SCALE_Y, 1, 0.9f));
        removeAnim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                doAddItems(position, items);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        removeAnim.start();
    }

    private void doAddItems(int position, Item...items) {
        final int firstPos = Math.max(0, mPager.getCurrentItem() - mPager.getOffscreenPageLimit());
        final HashMap<Integer, Integer> leftValueMap = getInitialPositionMap(firstPos);

        mAdapter.replaceItems(position, items);

        mPager.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d(TAG, "onPreDraw");
                mPager.getViewTreeObserver().removeOnPreDrawListener(this);

                List<Animator> anims = new ArrayList<Animator>();
                ArrayList<Integer> sortedPositions = sortViewByLeft();
                int count = 0;
                for (int i = 0; i < mPager.getChildCount() ; i++){
                    int visiblePosInView = sortedPositions.get(i);
                    View child = mPager.getChildAt(visiblePosInView);
                    int itemId = mAdapter.getItemId(firstPos + i);

                    Integer startPos = leftValueMap.get(itemId);
                    int currentPos = child.getLeft();
                    if (startPos == null) {
                        count++;
                        AnimatorSet add = new AnimatorSet();
                        add.playTogether(ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, ENTER_DISTANCE, 0),
                                         ObjectAnimator.ofFloat(child, View.ALPHA, 0, 1),
                                         ObjectAnimator.ofFloat(child, View.SCALE_X, 1.1f, 1f),
                                         ObjectAnimator.ofFloat(child, View.SCALE_Y, 1.1f, 1f));

                        Animator trick = ObjectAnimator.ofFloat(child, View.ALPHA, 0, 0);
                        trick.setDuration(300 + (100 *count));
                        AnimatorSet set = new AnimatorSet();
                        set.playSequentially(trick, add);
                        anims.add(set);

                    }else if (startPos != currentPos) {
                        anims.add(ObjectAnimator.ofFloat(child, View.TRANSLATION_X, startPos - currentPos, 0));
                    }
                }
                AnimatorSet set = new AnimatorSet();
                set.playTogether(anims);
                set.start();

                return false;
            }});
    }

    private void printChildren(ViewGroup v) {
        for (int i = 0; i < v.getChildCount(); i++) {
            Log.d(TAG, ((TextView) ((FrameLayout)v.getChildAt(i)).getChildAt(0)).getText() + ", left: " + v.getChildAt(i).getLeft());
        }
        Log.d(TAG, "---");
    }

    public class MyAdapter extends FragmentStatePagerAdapter {

        private HashMap<Item, Integer> mIdMap;
        private SparseArray<WeakReference<Fragment>> registeredFragments = new SparseArray<WeakReference<Fragment>>();
        private ArrayList<Item> mData;

        public MyAdapter(FragmentManager fm) {
            super(fm);
            mIdMap = new HashMap<Item, Integer>();
        }

        public void replaceItems(int position, Item... items) {
            Item removed = mData.remove(position);
            mData.addAll(position, Arrays.asList(items));

            //Now its id is its val
            mIdMap.remove(removed);
            for (Item item : items) {
                mIdMap.put(item, item.id);
            }

            notifyDataSetChanged();
        }

        public void setData(ArrayList<Item> data){
            mData = data;

            for (Item item : data) {
                mIdMap.put(item, item.id);
            }

            notifyDataSetChanged();
        }

        public Integer getItemId(int pos){
            return mIdMap.get(mData.get(pos));
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fr = BlankFragment.newInstance( mData.get(position));
            return fr;
        }

        @Override
        public int getCount() {
            if (mData != null) {
                return mData.size();
            }
            return 0;
        }

        public Fragment getRegisteredFragment(int position) {
            return registeredFragments.get(position).get();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            registeredFragments.put(position, new WeakReference<Fragment>(fragment));
            return fragment;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            registeredFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public void removeItem(int pos) {
            mData.remove(pos);
            notifyDataSetChanged();
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
