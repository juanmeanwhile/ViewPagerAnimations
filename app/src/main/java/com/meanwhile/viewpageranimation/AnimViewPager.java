package com.meanwhile.viewpageranimation;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mengujua on 10/2/17.
 */

public class AnimViewPager extends ViewPager {

    private static final String TAG = "AnimViewPager";
    private static final float ENTER_DISTANCE = -1600;

    public AnimViewPager(Context context) {
        super(context);
    }

    public AnimViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setAdapter(PagerAdapter adapter) {

        if (adapter instanceof AnimViewPagerAdapter) {
            super.setAdapter(adapter);
        } else {
            throw new RuntimeException("Adapter must extend AnimViewPagerAdapter");
        }
    }

    public void replaceFragment(final int position, final MainActivity.Item...items) {
        AnimatorSet removeAnim = new AnimatorSet();
        View v = ((AnimViewPagerAdapter) getAdapter()).getRegisteredFragment(position).getView();

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

    public void removeFragment(final int pos) {
        Log.d(TAG, "removeFragment");

        AnimatorSet hideAnim = new AnimatorSet();
        hideAnim.play(ObjectAnimator.ofFloat(((AnimViewPagerAdapter) getAdapter()).getRegisteredFragment(pos).getView(), View.ALPHA, 1, 0));
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

    private void doRemove(int position) {
        final int firstPos = Math.max(0, getCurrentItem() - getOffscreenPageLimit());
        final HashMap<Integer, Integer> leftValueMap = getInitialPositionMap(firstPos);

        ((AnimViewPagerAdapter) getAdapter()).removeItem(position);

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d(TAG, "onPreDraw");
                getViewTreeObserver().removeOnPreDrawListener(this);

                ArrayList<Integer> sortedPositions = sortViewByLeft();
                for (int i = 0; i < getChildCount() ; i++){
                    int visiblePosInView = sortedPositions.get(i);
                    View child = getChildAt(visiblePosInView);
                    int itemId = ((AnimViewPagerAdapter) getAdapter()).getItemId(firstPos + i);

                    Integer startPos = leftValueMap.get(itemId);
                    int currentPos = child.getLeft();
                    if (startPos != currentPos) {
                        ObjectAnimator.ofFloat(child, View.TRANSLATION_X, startPos - currentPos, 0).start();
                    }
                }
                return false;
            }});
    }

    private void doAddItems(int position, MainActivity.Item...items) {
        final int firstPos = Math.max(0, getCurrentItem() - getOffscreenPageLimit());
        final HashMap<Integer, Integer> leftValueMap = getInitialPositionMap(firstPos);

        ((AnimViewPagerAdapter) getAdapter()).replaceItems(position, items);

        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                Log.d(TAG, "onPreDraw");
                getViewTreeObserver().removeOnPreDrawListener(this);

                List<Animator> anims = new ArrayList<Animator>();
                ArrayList<Integer> sortedPositions = sortViewByLeft();
                int count = 0;
                for (int i = 0; i < getChildCount() ; i++){
                    int visiblePosInView = sortedPositions.get(i);
                    View child = getChildAt(visiblePosInView);
                    int itemId = ((AnimViewPagerAdapter) getAdapter()).getItemId(firstPos + i);
                    Integer startPos = leftValueMap.get(itemId);
                    int currentPos = child.getLeft();

                    Log.d(TAG, "Anim id: " + itemId + " from: " + startPos + " to: " + currentPos);

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

    private HashMap<Integer, Integer> getInitialPositionMap(int firstPos){
        final HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

        ArrayList<Integer> sortedPositions = sortViewByLeft();

        //There might be less views than adapter size. Calculate first position in the adapter corresponding to the first visible view
        for (int i = 0; i < getChildCount() ; i++){
            int visiblePosInView = sortedPositions.get(i);
            View child = getChildAt(visiblePosInView);
            int itemId = ((AnimViewPagerAdapter) getAdapter()).getItemId(firstPos + visiblePosInView);
            map.put(itemId, child.getLeft() - (((AnimViewPagerAdapter) getAdapter()).getCount() > 1? getScrollX():0));
        }

        return map;
    }

    private ArrayList<Integer> sortViewByLeft() {
        ArrayList<Integer> sortedPos = new ArrayList<Integer>();
        for (int i = 0; i < getChildCount(); i++){
            boolean added = false;
            for (int j = 0; j < sortedPos.size(); j++) {
                if (getChildAt(i).getLeft() < getChildAt(sortedPos.get(j)).getLeft()) {
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

}
