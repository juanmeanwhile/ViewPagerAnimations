package com.meanwhile.animviewpager;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Fragment;
import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mengujua on 10/2/17.
 */

public class AnimViewPager<T, K> extends ViewPager {

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


    public void replaceAndDeleteBefore(final int replacePosition, final int deleteItems, int animDeleted, final T item) {
        List<Animator> anims = new ArrayList<Animator>();
        for (int i = replacePosition - animDeleted +1; i <= replacePosition; i++) {
            View v = ((AnimViewPagerAdapter) getAdapter()).getRegisteredFragment(i).getView();
            anims.add(buildHideAnim(v));
        }
        AnimatorSet set = new AnimatorSet();
        set.playSequentially(anims);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setCurrentItem(replacePosition-deleteItems, true);
                final int firstPos = Math.max(0, getCurrentItem() - getOffscreenPageLimit());
                final HashMap<K, Integer> leftValueMap = getInitialPositionMap(firstPos);

                ((AnimViewPagerAdapter) getAdapter()).replaceAndDeleteBefore(replacePosition-deleteItems+1, replacePosition, item);
                setupSecondStepAnimations(firstPos, leftValueMap);

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        set.start();
    }

    public void addAfter(final int position, final T...items) {
        final int firstPos = Math.max(0, getCurrentItem() - getOffscreenPageLimit());
        final HashMap<K, Integer> leftValueMap = getInitialPositionMap(firstPos);

        ((AnimViewPagerAdapter) getAdapter()).addItems(position+1, false, items);
        setupSecondStepAnimations(firstPos +1, leftValueMap);
    }

    public void replaceAndAddAfter(final int position, final T...items) {
        Animator removeAnim;
        Fragment fr = ((AnimViewPagerAdapter) getAdapter()).getRegisteredFragment(position);
        if (fr != null) {
            View v = fr.getView();
            removeAnim = buildHideAnim(v);
            removeAnim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    final int firstPos = Math.max(0, getCurrentItem() - getOffscreenPageLimit());
                    final HashMap<K, Integer> leftValueMap = getInitialPositionMap(firstPos);

                    ((AnimViewPagerAdapter) getAdapter()).addItems(position, true, items);
                    setupSecondStepAnimations(firstPos, leftValueMap);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });

            removeAnim.start();
        } else {
            final int firstPos = Math.max(0, getCurrentItem() - getOffscreenPageLimit());
            final HashMap<K, Integer> leftValueMap = getInitialPositionMap(firstPos);

            ((AnimViewPagerAdapter) getAdapter()).addItems(position, true, items);
            setupSecondStepAnimations(firstPos, leftValueMap);
        }
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
        final HashMap<K, Integer> leftValueMap = getInitialPositionMap(firstPos);

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
                    K itemId = ((AnimViewPagerAdapter<T, K>) getAdapter()).getItemId(firstPos + i);

                    Integer startPos = leftValueMap.get(itemId);
                    int currentPos = child.getLeft();
                    if (startPos != currentPos) {
                        ObjectAnimator.ofFloat(child, View.TRANSLATION_X, startPos - currentPos, 0).start();
                    }
                }
                return false;
            }});
    }


    private void setupSecondStepAnimations(final int firstPos, final HashMap<K, Integer> leftValueMap) {
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
                    K itemId = ((AnimViewPagerAdapter<T, K>) getAdapter()).getItemId(firstPos + i);
                    Integer startPos = leftValueMap.get(itemId);
                    int currentPos = child.getLeft();

                    Log.d(TAG, "Anim id: " + itemId + " from: " + startPos + " to: " + currentPos);

                    if (startPos == null) {
                        count++;
                        Animator trick = ObjectAnimator.ofFloat(child, View.ALPHA, 0, 0);
                        trick.setDuration(300 + (100 *count));
                        AnimatorSet set = new AnimatorSet();
                        set.playSequentially(trick, buildAddAnim(child));
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

    private Animator buildHideAnim(View v) {
        AnimatorSet removeAnim = new AnimatorSet();
        removeAnim.playTogether(ObjectAnimator.ofFloat(v, View.ALPHA, 1, 0 ),
                                ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, 0, -ENTER_DISTANCE),
                                ObjectAnimator.ofFloat(v, View.SCALE_X, 1, 0.9f),
                                ObjectAnimator.ofFloat(v, View.SCALE_Y, 1, 0.9f));

        return removeAnim;
    }

    private Animator buildAddAnim(View v) {
        AnimatorSet add = new AnimatorSet();
        add.playTogether(ObjectAnimator.ofFloat(v, View.TRANSLATION_Y, ENTER_DISTANCE, 0),
                         ObjectAnimator.ofFloat(v, View.ALPHA, 0, 1),
                         ObjectAnimator.ofFloat(v, View.SCALE_X, 1.1f, 1f),
                         ObjectAnimator.ofFloat(v, View.SCALE_Y, 1.1f, 1f));
        return add;
    }


    private HashMap<K, Integer> getInitialPositionMap(int firstPos){
        final HashMap<K, Integer> map = new HashMap<K, Integer>();

        ArrayList<Integer> sortedPositions = sortViewByLeft();

        //There might be less views than adapter size. Calculate first position in the adapter corresponding to the first visible view
        for (int i = 0; i < getChildCount() ; i++){
            int visiblePosInView = sortedPositions.get(i);
            View child = getChildAt(visiblePosInView);
            AnimViewPagerAdapter<T, K> adapter = (AnimViewPagerAdapter<T, K>) getAdapter();
            K itemId = adapter.getItemId(firstPos + visiblePosInView);
            map.put(itemId, child.getLeft() - (adapter.getCount() > 1? getScrollX():0));
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
