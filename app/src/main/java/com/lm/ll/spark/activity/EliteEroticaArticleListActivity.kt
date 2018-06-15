package com.lm.ll.spark.activity

import android.content.Intent
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import com.lm.ll.spark.R
import com.lm.ll.spark.adapter.SimpleArticleAdapter
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.decoration.NewsItemDecoration
import com.lm.ll.spark.util.*
import kotlinx.android.synthetic.main.elite_erotica_article_list.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async

/**
 * 说明：禁忌书屋精华区
 * 作者：Created by ll on 2018-06-05 18:37.
 * 邮箱：wenhelinlu@gmail.com
 */
class EliteEroticaArticleListActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        this.swipeRefreshEliteList.isRefreshing = false
    }

    //文章列表数据源
    private var articleList: ArrayList<Article> = ArrayList()
    //文章列表adapter
    private var adapter: SimpleArticleAdapter? = null

    //当前加载的页数
    private var currentPage: Int = 1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.elite_erotica_article_list)

        supportActionBar!!.title = getString(R.string.action_eliteArea)

        swipeRefreshEliteList.setColorSchemeResources(R.color.blueGrey)
        swipeRefreshEliteList.setDistanceToTriggerSync(400)

        swipeRefreshEliteList.setOnRefreshListener({
            loadContent()
        })

        val linearLayoutManager = LinearLayoutManager(this@EliteEroticaArticleListActivity)

        this.recyclerViewEliteList.addItemDecoration(NewsItemDecoration(2))
        this.recyclerViewEliteList.layoutManager = linearLayoutManager

        //上拉加载更多
        recyclerViewEliteList.addOnScrollListener(object : MyRecyclerViewOnScrollListener(linearLayoutManager) {
            override fun loadMoreData() {
                currentPage++
                loadContent(true)
            }
        })

        loadContent()
    }

    /**
     * @desc 加载文章列表
     * @author ll
     * @time 2018-05-29 19:40
     * @param isLoadMore 是否是加载更多操作
     */
    private fun loadContent(isLoadMore: Boolean = false) {

        val currentPos: Int = articleList.size

        val deferredLoad = async(CommonPool) {
            val spider = Spider()
            //如果下拉刷新，则只抓取第一页内容，否则加载下一页内容
            val pageIndex = if (isLoadMore) currentPage else 1
            val list = spider.scratchEliteArticleList("$BASE_URL$CURRENT_ELITEAREA_BASE_URL$pageIndex")

            if (isLoadMore) {
                articleList.addAll(list) //如果是上拉加载更多，则直接将新获取的数据源添加到已有集合中
            } else {
                /**
                 *  如果不是第一次加载，即当前已存在数据，则在新获取的列表中找出和当前已存在的数据列表第一条数据相同
                 *  的数据位置（如果没有找到，则说明新获取的数据列表数据都为新数据，可直接添加当已有集合中），然后将新获取数据列表中
                 *  这个位置之前的数据添加到已有集合中
                 */
                if (articleList.count() > 0) {
                    val firstNews = list.findLast { x -> x.url == articleList[0].url }
                    if (firstNews != null) {
                        val firstIndex = list.indexOf(firstNews)
                        if (firstIndex > 0) {
                            val latest = list.take(firstIndex)
                            articleList.addAll(latest)
                        } else {
                        }
                    } else {
                    }
                } else {
                    articleList = list
                    //如果此时获取的集合数据不超过预定值，则继续加载数据
                    while (articleList.size < MIN_ROWS) {
                        currentPage++
                        val tmpList = spider.scratchEliteArticleList("$BASE_URL$CURRENT_ELITEAREA_BASE_URL$currentPage")
                        articleList.addAll(tmpList)
                    }
                }
            }
        }

        async(UI) {
            swipeRefreshEliteList.isRefreshing = true
            deferredLoad.await()
            val intent = Intent(this@EliteEroticaArticleListActivity, DisplayArticleActivity::class.java)
            adapter = SimpleArticleAdapter(this@EliteEroticaArticleListActivity, intent, articleList)
            this@EliteEroticaArticleListActivity.recyclerViewEliteList.adapter = adapter
            this@EliteEroticaArticleListActivity.recyclerViewEliteList.adapter.notifyDataSetChanged()

            if (isLoadMore) {
                this@EliteEroticaArticleListActivity.recyclerViewEliteList.layoutManager.scrollToPosition(currentPos - 1)
            }

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
            R.id.action_search -> true
            R.id.action_sort -> false
            else -> super.onOptionsItemSelected(item)
        }
    }
}