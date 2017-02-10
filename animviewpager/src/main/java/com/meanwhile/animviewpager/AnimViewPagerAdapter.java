package com.meanwhile.animviewpager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mengujua on 10/2/17.
 */

public abstract class AnimViewPagerAdapter<T> extends FragmentStatePagerAdapter {

    private HashMap<T, Integer> mIdMap;
    private SparseArray<WeakReference<Fragment>> registeredFragments;
    private List<T> mData;

    public AnimViewPagerAdapter(FragmentManager fm) {
        super(fm);

        mData = new ArrayList<T>();
        mIdMap = new HashMap<T, Integer>();
        registeredFragments = new SparseArray<WeakReference<Fragment>>();
    }

    public abstract int getIdForObject(T item);

    protected abstract Fragment getFragmentForItem(T t);

    public void setData(ArrayList<T> data){
        mData = data;

        for (T item : data) {
            mIdMap.put(item, getIdForObject(item));
        }

        notifyDataSetChanged();
    }

    public void replaceItems(int position, T... items) {
        T removed = mData.remove(position);
        mData.addAll(position, Arrays.asList(items));

        //Now its id is its val
        mIdMap.remove(removed);
        for (T item : items) {
            mIdMap.put(item, getIdForObject(item));
        }

        notifyDataSetChanged();
    }

    public void removeItem(int pos) {
        mData.remove(pos);
        notifyDataSetChanged();
    }

    public Integer getItemId(int position){
        return mIdMap.get(mData.get(position));
    }


    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position).get();
    }

    @Override
    public Fragment getItem(int position) {
        return getFragmentForItem(mData.get(position));
    }

    @Override
    public int getCount() {
        if (mData != null) {
            return mData.size();
        }
        return 0;
    }

    @Override
    public int getItemPosition(Object object) {
        return PagerAdapter.POSITION_NONE;
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
}
