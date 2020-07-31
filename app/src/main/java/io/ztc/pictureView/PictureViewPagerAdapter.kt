package io.ztc.pictureView

import io.ztc.pictureView.content.PictureViewFragment

class PictureViewPagerAdapter(private var mData: MutableList<PictureViewFragment>, fragmentManager: androidx.fragment.app.FragmentManager)
    : androidx.fragment.app.FragmentStatePagerAdapter(fragmentManager) {
    override fun getItem(position: Int): androidx.fragment.app.Fragment {
        return mData[position]
    }

    override fun getCount(): Int {
        return mData.size
    }

}
