package io.ztc.pictureView

import android.animation.LayoutTransition
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import io.ztc.pictureView.content.PictureViewFragment
import io.ztc.pictureView.inter.OnLongClickListener
import io.ztc.pictureView.tools.DPUtils
import java.lang.RuntimeException
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.timerTask


@SuppressLint("StaticFieldLeak")
/**
 * 图片查看器
 */
object PictureView {

    const val INDICATOR_TYPE_DOT = "INDICATOR_TYPE_DOT"
    const val INDICATOR_TYPE_TEXT = "INDICATOR_TYPE_TEXT"


    private var context:AppCompatActivity? = null


    internal var mInterface: ShowImageViewInterface? = null
    internal var mLoadAndImgInterface:ShowLoadAndImgInterface? = null
    private var mCreatedInterface: OnPictureViewCreatedListener? = null
    private var mDestroyInterface: OnPictureViewDestroyListener? = null


    private lateinit var imgData: ArrayList<String> // 图片数据
    private lateinit var imgOriginalData: ArrayList<String> // 原图片数据
    private lateinit var container: WeakReference<ViewGroup>   // 存放图片的容器， ListView/GridView/RecyclerView
    private var currentPage = 0    // 当前页

    private var clickView: WeakReference<View>? = null //点击那一张图片时候的view
    private var longClickListener: OnLongClickListener? = null

    private var indicatorType = INDICATOR_TYPE_DOT   // 默认type为小圆点

    interface OnPictureViewCreatedListener {
        fun onCreated()
    }


    interface OnPictureViewDestroyListener {
        fun onDestroy()
    }


    fun context(context: AppCompatActivity):PictureView{
        this.context = context
        return this
    }

    /**
     * 查看页面创建时调用
     */
    fun setOnPictureViewCreatedListener(l: () -> Unit): PictureView {

        mCreatedInterface = object : OnPictureViewCreatedListener {
            override fun onCreated() {
                l()
            }
        }
        return this
    }

    /**
     * 页面关闭时调用
     */
    fun setOnPictureViewDestroyListener(l: () -> Unit): PictureView {
        mDestroyInterface = object : OnPictureViewDestroyListener {
            override fun onDestroy() {
                l()
            }
        }
        return this
    }

    /**
     * 小圆点的drawable
     * 下标0的为没有被选中的
     * 下标1的为已经被选中的
     */
//    private val mDot = intArrayOf(R.drawable.no_selected_dot, R.drawable.selected_dot)


    interface ShowLoadAndImgInterface {
        fun show(iv: ImageView,progressBar: ProgressBar,proText:TextView, opBtn:Button,url: String,OriginalUrl: String)
    }

    /**
     * 进度条控制器
     */
    interface ShowImageViewInterface {
        fun show(v: ImageView, url: String)
    }

    /**
     * 设置显示ImageView的接口
     */
    fun setShowImageViewInterface(i: ShowImageViewInterface): PictureView {
        mInterface = i
        return this
    }

    /**
     * 全功能显示插件
     */
    fun setLoadAndImgInterface(loadAndImgInterface: ShowLoadAndImgInterface): PictureView {
        mLoadAndImgInterface = loadAndImgInterface
        return this
    }

    /**
     * 设置点击一个图片
     */
    fun setSinglePicture(data: String, view: View): PictureView {
        imgData = arrayListOf(data)
        clickView = WeakReference(view)
        return this
    }

    /**
     * 设置点击一个图片
     */
    fun setSingleOriginalPicture(data: String,originalUrl: String, view: View): PictureView {
        imgData = arrayListOf(data)
        imgOriginalData = arrayListOf(originalUrl)
        clickView = WeakReference(view)
        return this
    }

    /**
     * 设置图片数据
     */
    fun setPictureData(data: ArrayList<String>): PictureView {
        imgData = data
        return this
    }

    /**
     * 设置原图图片数据
     */
    fun setOriginalPictureData(data: ArrayList<String>): PictureView {
        imgOriginalData = data
        return this
    }


    fun setImgContainer(container: AbsListView): PictureView {
        this.container = WeakReference(container)
        return this
    }

    /**
     * 用于对应多照片传入照片位置
     */
    fun setRecyclerView(container: androidx.recyclerview.widget.RecyclerView): PictureView {
        this.container = WeakReference(container)
        return this
    }

    /**
     * 获取itemView
     */
    private fun getItemView(): View {
        if (clickView == null) {
            val itemView = if (container.get() is AbsListView) {
                val absListView = container.get() as AbsListView
                absListView.getChildAt(currentPage - absListView.firstVisiblePosition)
            } else {
                (container.get() as androidx.recyclerview.widget.RecyclerView).layoutManager!!.findViewByPosition(currentPage)
            }

            return if (itemView is ViewGroup) {
                findImageView(itemView)!!
            } else {
                itemView as ImageView
            }
        } else {
            return clickView!!.get()!!
        }
    }

    private fun findImageView(group: ViewGroup): ImageView? {
        for (i in 0 until group.childCount) {
            return when {
                group.getChildAt(i) is ImageView -> group.getChildAt(i) as ImageView
                group.getChildAt(i) is ViewGroup -> findImageView(group.getChildAt(i) as ViewGroup)
                else -> throw RuntimeException("未找到ImageView")
            }
        }
        return null
    }

    /**
     * 获取现在查看到的图片的原始位置 (中间)
     */
    private fun getCurrentViewLocation(): IntArray {
        val result = IntArray(2)
        getItemView().getLocationInWindow(result)
        result[0] += getItemView().measuredWidth / 2
        result[1] += getItemView().measuredHeight / 2
        return result
    }


    /**
     * 设置当前页， 从0开始
     */
    fun setPosition(page: Int): PictureView {
        currentPage = page
        return this
    }

//    fun start(fragment: androidx.fragment.app.Fragment) {
//        val activity = fragment.activity!!
//        start(activity as AppCompatActivity)
//    }
//
//
//    fun start(fragment: android.app.Fragment) {
//        val activity = fragment.activity!!
//        start(activity as AppCompatActivity)
//    }
//
//
//    fun start(activity: AppCompatActivity) {
//        show(activity)
//    }

    fun show(){
        if (context!=null){
            show(context!!)
        }else{
            throw Exception("未配置context...")
        }
    }


    fun setOnLongClickListener(longClickListener: OnLongClickListener): PictureView {
        this.longClickListener = longClickListener
        return this
    }


    /**
     * 设置指示器的样式，但是如果图片大于9张，则默认设置为文字样式
     */
    fun setIndicatorType(type: String): PictureView {
        this.indicatorType = type
        return this
    }

    @SuppressLint("ObjectAnimatorBinding", "SetTextI18n")
    private fun show(activity: AppCompatActivity) {


        val decorView = activity.window.decorView as ViewGroup


        // 设置添加layout的动画
        val layoutTransition = LayoutTransition()
        val alphaOa = ObjectAnimator.ofFloat(null, "alpha", 0f, 1f)
        alphaOa.duration = 50
        layoutTransition.setAnimator(LayoutTransition.APPEARING, alphaOa)
        decorView.layoutTransition = layoutTransition

        val frameLayout = FrameLayout(activity)

        val photoViewLayout = LayoutInflater.from(activity).inflate(R.layout.activity_picture, null)
        val viewPager = photoViewLayout.findViewById<androidx.viewpager.widget.ViewPager>(R.id.mLookPicVP)

        val fragments = mutableListOf<PictureViewFragment>()
        /**
         * 存放小圆点的Group
         */
        var mDotGroup: LinearLayout? = null

        /**
         * 存放没有被选中的小圆点Group和已经被选中小圆点
         * 或者存放数字
         */
        var mFrameLayout: FrameLayout? = null
        /**
         * 选中的小圆点
         */
        var mSelectedDot: View? = null


        /**
         * 文字版本当前页
         */
        var tv: TextView? = null


        for (i in 0 until imgData.size) {
            val f = PictureViewFragment()
            f.exitListener = object : PictureViewFragment.OnExitListener {
                override fun exit() {
                    activity.runOnUiThread {
                        if (mDotGroup != null)
                            mDotGroup!!.removeAllViews()
                        frameLayout.removeAllViews()
                        decorView.removeView(frameLayout)
                        fragments.clear()


                        if (mDestroyInterface != null) {
                            mDestroyInterface!!.onDestroy()
                        }
                    }
                }

            }
            if (imgData.size>0 && imgOriginalData.size<=0){
                f.setData(intArrayOf(getItemView().measuredWidth, getItemView().measuredHeight), getCurrentViewLocation(), imgData[i], true)
            }else if(imgData.size>0 && imgOriginalData.size>0){
                f.setData(intArrayOf(getItemView().measuredWidth, getItemView().measuredHeight), getCurrentViewLocation(), imgData[i], imgOriginalData[i], true)
            }
            f.longClickListener = longClickListener
            fragments.add(f)
        }

        val adapter = PictureViewPagerAdapter(fragments, activity.supportFragmentManager)


        viewPager.adapter = adapter
        viewPager.currentItem = currentPage
        viewPager.offscreenPageLimit = 100
        viewPager.addOnPageChangeListener(object : androidx.viewpager.widget.ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {

            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

                if (mSelectedDot != null && imgData.size > 1) {
                    val dx = mDotGroup!!.getChildAt(1).x - mDotGroup!!.getChildAt(0).x
                    mSelectedDot!!.translationX = (position * dx) + positionOffset * dx
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onPageSelected(position: Int) {
                currentPage = position


                /**
                 * 解决RecyclerView获取不到itemView的问题
                 * 如果滑到的view不在当前页面显示，那么则滑动到那个position，再获取itemView
                 */
                if (container.get() !is AbsListView) {
                    val layoutManager = (container.get() as androidx.recyclerview.widget.RecyclerView).layoutManager
                    if (layoutManager is androidx.recyclerview.widget.LinearLayoutManager) {
                        if (currentPage < layoutManager.findFirstVisibleItemPosition() || currentPage > layoutManager.findLastVisibleItemPosition()) {
                            layoutManager.scrollToPosition(currentPage)
                        }
                    } else if (layoutManager is androidx.recyclerview.widget.GridLayoutManager) {
                        if (currentPage < layoutManager.findFirstVisibleItemPosition() || currentPage > layoutManager.findLastVisibleItemPosition()) {
                            layoutManager.scrollToPosition(currentPage)
                        }
                    }
                }

                /**
                 * 设置文字版本当前页的值
                 */
                if (tv != null) {
                    tv!!.text = "${currentPage + 1}/${imgData.size}"
                }

                // 这里延时0.2s是为了解决上面👆的问题。因为如果刚调用ScrollToPosition方法，就获取itemView是获取不到的，所以要延时一下
                Timer().schedule(timerTask {
                    fragments[currentPage].setData(intArrayOf(getItemView().measuredWidth, getItemView().measuredHeight), getCurrentViewLocation(), imgData[currentPage], false)
                }, 200)

            }

        })

        frameLayout.addView(photoViewLayout)


        frameLayout.post {
            mFrameLayout = FrameLayout(activity)
            if (imgData.size in 2..9 && indicatorType == INDICATOR_TYPE_DOT) {

                /**
                 * 实例化两个Group
                 */
//                if (mFrameLayout != null) {
//                    mFrameLayout!!.removeAllViews()
//                }
//                if (mDotGroup != null) {
//                    mDotGroup!!.removeAllViews()
//                    mDotGroup = null
//                }
//                mDotGroup = LinearLayout(activity)
//
//                if (mDotGroup!!.childCount != 0)
//                    mDotGroup!!.removeAllViews()
//                val dotParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
//                        ViewGroup.LayoutParams.WRAP_CONTENT)
                /**
                 * 未选中小圆点的间距
                 */
//                dotParams.rightMargin = DPUtils.dpToPx(activity, 12)

                /**
                 * 创建未选中的小圆点
                 */
//                for (i in 0 until imgData.size) {
//                    val iv = ImageView(activity)
//                    iv.setImageDrawable(activity.resources.getDrawable(mDot[0]))
//                    iv.layoutParams = dotParams
//                    mDotGroup!!.addView(iv)
//                }

                /**
                 * 设置小圆点Group的方向为水平
                 */
//                mDotGroup!!.orientation = LinearLayout.HORIZONTAL
                /**
                 * 设置小圆点在中间
                 */
//                mDotGroup!!.gravity = Gravity.CENTER or Gravity.BOTTOM
                /**
                 * 两个Group的大小都为match_parent
                 */
//                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                        ViewGroup.LayoutParams.MATCH_PARENT)
//
//
//                params.bottomMargin = DPUtils.dpToPx(activity, 70)
                /**
                 * 首先添加小圆点的Group
                 */
//                frameLayout.addView(mDotGroup, params)
//
//                mDotGroup!!.post {
//                    if (mSelectedDot != null) {
//                        mSelectedDot = null
//                    }
//                    if (mSelectedDot == null) {
//                        val iv = ImageView(activity)
//                        iv.setImageDrawable(activity.resources.getDrawable(mDot[1]))
//                        val params = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//                        /**
//                         * 设置选中小圆点的左边距
//                         */
//                        params.leftMargin = mDotGroup!!.getChildAt(0).x.toInt()
//                        iv.translationX = (dotParams.rightMargin * currentPage + mDotGroup!!.getChildAt(0).width * currentPage).toFloat()
//                        params.gravity = Gravity.BOTTOM
//                        mFrameLayout!!.addView(iv, params)
//                        mSelectedDot = iv
//                    }
//                    /**
//                     * 然后添加包含未选中圆点和选中圆点的Group
//                     */
//                    frameLayout.addView(mFrameLayout, params)
//                }
            } else {
                tv = TextView(activity)
                tv!!.text = "${currentPage + 1}/${imgData.size}"
                tv!!.setTextColor(Color.WHITE)
                tv!!.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                tv!!.textSize = 18f
                mFrameLayout!!.addView(tv)
                val params = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)
                params.bottomMargin = DPUtils.dpToPx(activity, 80)
                frameLayout.addView(mFrameLayout, params)

            }
        }
        decorView.addView(frameLayout, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        if (mCreatedInterface != null) {
            mCreatedInterface!!.onCreated()
        }
    }


}