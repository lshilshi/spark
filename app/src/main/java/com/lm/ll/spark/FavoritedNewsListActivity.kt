package com.lm.ll.spark

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.lm.ll.spark.adapter.NewsAdapter
import com.lm.ll.spark.db.News
import com.lm.ll.spark.decoration.NewsItemDecoration
import com.vicpin.krealmextensions.querySorted
import io.realm.Realm
import io.realm.Sort
import kotlinx.android.synthetic.main.elitenews_list.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

/**
 * 作者：Created by ll on 2018-06-07 17:15.
 * 邮箱：wenhelinlu@gmail.com
 */
class FavoritedNewsListActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        this.swipeRefreshEliteList.isRefreshing = false
    }

    //文章列表数据源
    private var newsList: ArrayList<News> = ArrayList()
    //文章列表adapter
    private var adapter: NewsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.elitenews_list)

        Realm.init(this)

        supportActionBar!!.title = this.getString(R.string.action_favouited)

//        swipeRefreshEliteList.setColorSchemeResources(R.color.blueGrey)
//        swipeRefreshEliteList.setDistanceToTriggerSync(400)

        val linearLayoutManager = LinearLayoutManager(this@FavoritedNewsListActivity)

        this.recyclerViewEliteList.addItemDecoration(NewsItemDecoration(2))
        this.recyclerViewEliteList.layoutManager = linearLayoutManager

        loadContent()
    }

    /**
     * @desc 加载文章列表
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadContent() {
        val deferredLoad = async(CommonPool) {
            //按插入时间降序排列
            newsList = News().querySorted("insertTime", Sort.DESCENDING) as ArrayList<News>
        }

        async(UI) {
            swipeRefreshEliteList.isRefreshing = true
            deferredLoad.await()
            adapter = NewsAdapter(this@FavoritedNewsListActivity, newsList)
            this@FavoritedNewsListActivity.recyclerViewEliteList.adapter = adapter
            this@FavoritedNewsListActivity.recyclerViewEliteList.adapter.notifyDataSetChanged()

            //停止刷新
            swipeRefreshEliteList.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            R.id.action_search -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
