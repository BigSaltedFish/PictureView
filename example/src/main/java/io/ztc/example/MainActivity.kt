package io.ztc.example

import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.bumptech.glide.Glide
import io.ztc.appkit.appBase.adapter.AppTypeMoreListAdapter
import io.ztc.appkit.appBase.refresh.OnListToMoreListener
import io.ztc.appkit.appBase.refresh.RefreshListActivity
import io.ztc.appkit.base.adapter.BaseViewHolder
import io.ztc.appkit.view.Option
import io.ztc.pictureView.PictureView
import kotlinx.android.synthetic.main.activity_list.*


class MainActivity : RefreshListActivity<String>() {

    var adapter: MainAdapter? = null
    var moreDatas: ArrayList<Tab> = ArrayList()
    var pageSize: Int = 10

    override fun initContext(): Any {
        return this
    }

    override fun initData(savedInstanceState: Bundle?) {
        super.initData(savedInstanceState)
        for (i in 0..9) {
            moreDatas.add(Tab("item$i", null))
        }
    }

    override fun layout(): Int {
        return R.layout.activity_list
    }

    override fun initView() {
        initToolbar(R.id.toolbar).title("列表")
        toolbar.addEditMenu("菜单", R.menu.toolbar_menu) { item ->
            when (item!!.itemId) {
                R.id.u1 -> {
                    showError("连接失败 点击重试")
                }
            }
            false
        }
        initAppRecyclerView(main_list)
    }

    override fun initListAdapter() {
        //初始化适配器
        adapter = MainAdapter()
        //初始化加载更多操作
        addListToMoreListener(adapter, object : OnListToMoreListener {
            override fun getNextDate() {
                Handler().postDelayed({
                    adapter!!.addMoreData(moreDatas)
                }, 2000)
            }

            override fun canToMore(): Boolean {
                return adapter!!.canToMore() && load.isContent
            }
        })
        //显示列表
        showList(adapter)
    }

    override fun getData() {
        adapter!!.initData(moreDatas)
        main_list.showContent()
        recoveryState()
    }

    override fun initListener() {

    }

    override fun defaultAction() {
        initListAdapter()
        initRNMRefreshAndMotion(scroll, 2000)
        getData()
    }

    inner class Tab(var name: String, var type: Class<*>?)


    inner class MainAdapter :
        AppTypeMoreListAdapter<Tab>(context, pageSize, mapOf(1 to R.layout.item_main)) {
        override fun setControls(holder: BaseViewHolder?, bean: Tab?, type: Int, position: Int) {
            holder!!.getView<Option>(R.id.name).setType(bean?.name)
            val more = holder.getView<ImageView>(R.id.more)
            holder.itemView.setOnClickListener {
                val url = "https://qiniucdn.fairyever.com/15149579640159.jpg"
                PictureView
                    .context(this@MainActivity)
                    .setSinglePicture(url,more)
                    .setShowImageViewInterface(object : PictureView.ShowImageViewInterface {
                        override fun show(iv: ImageView, url: String) {
                            Glide.with(iv.context).load(url).into(iv)
                        }
                    })
                    .show()
            }
            if (bean!!.type != null) {
                holder.itemView.setOnClickListener {
                    To(bean.type!!).go()
                }
            }
        }

        override fun setType(bean: Tab?, pos: Int): Int {
            return 1
        }
    }
}
