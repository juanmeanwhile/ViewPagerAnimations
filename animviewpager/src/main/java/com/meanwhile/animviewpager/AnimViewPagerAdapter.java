package com.meanwhile.animviewpager;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;
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

public abstract class AnimViewPagerAdapter<T,I> extends FragmentStatePagerAdapter {

    private HashMap<T, I> mIdMap;
    private SparseArray<WeakReference<Fragment>> registeredFragments;
    private List<T> mData;

    public AnimViewPagerAdapter(FragmentManager fm) {
        super(fm);

        mData = new ArrayList<T>();
        mIdMap = new HashMap<T, I>();
        registeredFragments = new SparseArray<WeakReference<Fragment>>();
    }

    public abstract I getIdForObject(T item);

    protected abstract Fragment getFragmentForItem(T t);

    public void setData(ArrayList<T> data){
        mData = data;

        for (T item : data) {
            mIdMap.put(item, getIdForObject(item));
        }

        notifyDataSetChanged();
    }

    public void delete(int fromInc, int toExc) {
        for (int i = 0;  i < fromInc - toExc ; i++) {
            T item = mData.remove(fromInc);
            mIdMap.remove(item);
        }
        notifyDataSetChanged();
    }

    public void replaceAndDeleteBefore(int deleteFromPos, int replacePos, T replacingItem) {
        for (int i = 0;  i < replacePos - deleteFromPos ; i++) {
           T item = mData.remove(deleteFromPos);
            mIdMap.remove(item);
        }

        mData.add(deleteFromPos, replacingItem);
        mIdMap.put(replacingItem, getIdForObject(replacingItem));
        notifyDataSetChanged();
    }

    public void replaceAndAddAfter(int position, T... items) {
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

    public I getItemId(int position){
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
