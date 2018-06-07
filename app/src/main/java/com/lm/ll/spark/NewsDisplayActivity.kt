package com.lm.ll.spark

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.widget.Toast
import com.lm.ll.spark.adapter.CommentRecyclerViewAdapter
import com.lm.ll.spark.db.News
import com.lm.ll.spark.decoration.DashlineItemDecoration
import com.lm.ll.spark.util.DETAIL_INTENT_KEY
import com.lm.ll.spark.util.Spider
import com.vicpin.krealmextensions.save
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_display_news.*
import kotlinx.android.synthetic.main.bottom_toolbar_text.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async


/**
 * 作者：Created by ll on 2018-05-28 14:56.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsDisplayActivity: AppCompatActivity() {

    //接收从文章列表传过来的被点击的文章Model
    private lateinit var news: News
    //此文章下的首层评论
    private var comments: ArrayList<News> = ArrayList()
    //评论adapter
    private lateinit var commentsAdapter: CommentRecyclerViewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_news)

        Realm.init(this)

        initData()

        initView()

        loadText()
    }

    /**
     * @desc 初始化数据
     * @author ll
     * @time 2018-06-07 16:33
     */
    private fun initData() {
        //从列表中传来的点击的标题
        news = this.intent.getParcelableExtra(DETAIL_INTENT_KEY)
    }

    /**
     * @desc 初始化视图
     * @author ll
     * @time 2018-06-07 16:34
     */
    private fun initView() {

        //跟?结合使用， let函数可以在对象不为 null 的时候执行函数内的代码，从而避免了空指针异常的出现。
        this.supportActionBar?.let {
            it.title = news.title
        }

        //收藏图标点击事件
        iv_favorite.setOnClickListener {

            //收藏或取消收藏
            if (news.isFavorited == 1) {
                news.isFavorited = 0
            } else {
                news.isFavorited = 1
            }
            news.save()
            Toast.makeText(this, "收藏成功", Toast.LENGTH_SHORT).show()
        }

        val linearLayoutManager = LinearLayoutManager(this@NewsDisplayActivity)
        //评论列表添加点线分隔线
        this.recyclerViewComment.addItemDecoration(DashlineItemDecoration())
        this.recyclerViewComment.layoutManager = linearLayoutManager
        this.recyclerViewComment.isNestedScrollingEnabled = false

        //显示或隐藏底栏
        scrollviewText.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            var isShow = true
            if (scrollY - oldScrollY > 0) {
                isShow = false
            } else if (oldScrollY - scrollY > 0) {
                isShow = true
            }
            showBottomToolbar(isShow)
        }

    }

    /**
     * @desc 加载文章正文和评论
     * @author ll
     * @time 2018-05-29 19:40
     */
    private fun loadText(){
        val deferredLoad = async(CommonPool) {
            val spider = Spider()
            news = spider.scratchText(news, comments) //正文中可能也包含链接（比如精华区）
            comments.reverse() //因为在精华区中，章节链接是倒序显示，所以将其翻转
            comments.addAll(spider.scratchComments(news))
        }

        async(UI) {

            //如果正文有内容，则说明是从本地读取的，不需要再从网上抓取
            if (news.text != null && news.text.toString().isNotEmpty()) {
                deferredLoad.await()
            }

            tvText.text = news.text
            viewDivider.visibility = View.VISIBLE
            //在正文加载完成后再显示评论区提示
            tvCommentRemark.text = this@NewsDisplayActivity.getString(R.string.comment_remark)

            commentsAdapter = CommentRecyclerViewAdapter(this@NewsDisplayActivity, comments)
            recyclerViewComment.adapter = commentsAdapter
            recyclerViewComment.adapter.notifyDataSetChanged()
        }
    }

    /**
     * @desc 滑动正文内容时显示或隐藏底栏
     * @author ll
     * @time 2018-06-03 08:14
     */
    private fun showBottomToolbar(isShow: Boolean){
//        if (isShow) {
//            toolbar_bottom_text.visibility = View.VISIBLE
//            val animation = AnimationUtils.loadAnimation(this@NewsDisplayActivity, R.anim.fab_jump_from_down)
//            toolbarBottomText!!.startAnimation(animation)
//        } else {
//            toolbar_bottom_text.visibility = View.GONE
//            val animation = AnimationUtils.loadAnimation(this@NewsDisplayActivity, R.anim.fab_jump_to_down)
//            toolbarBottomText!!.startAnimation(animation)
//        }

        toolbar_bottom_text.visibility = if (isShow) View.VISIBLE else View.GONE
    }
}

