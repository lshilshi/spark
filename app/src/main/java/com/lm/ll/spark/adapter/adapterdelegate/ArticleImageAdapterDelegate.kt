package com.lm.ll.spark.adapter.adapterdelegate

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade
import com.bumptech.glide.request.RequestOptions
import com.hannesdorfmann.adapterdelegates3.AdapterDelegate
import com.lm.ll.spark.R
import com.lm.ll.spark.db.Article
import com.lm.ll.spark.util.GlobalConst.Companion.LOG_TAG_COMMON
import com.lm.ll.spark.util.getImageSizeAhead
import kotlinx.android.synthetic.main.article_item_image.view.*
import kotlinx.coroutines.experimental.CommonPool
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.withContext

/**
 * 作者：Created by ll on 2018-09-21 14:46.
 * 邮箱：wenhelinlu@gmail.com
 */
class ArticleImageAdapterDelegate(activity: AppCompatActivity) : AdapterDelegate<ArrayList<Article>>() {
    private var inflater: LayoutInflater = activity.layoutInflater


    override fun onCreateViewHolder(parent: ViewGroup?): RecyclerView.ViewHolder {
        return ArticleImageViewHolder(inflater.inflate(R.layout.article_item_image, parent, false))
    }

    override fun isForViewType(items: ArrayList<Article>, position: Int): Boolean {
        return items[position].articleFlag == 3
    }

    override fun onBindViewHolder(items: ArrayList<Article>, position: Int, holder: RecyclerView.ViewHolder, payloads: MutableList<Any>) {
        val vh = holder as ArticleImageViewHolder
        with(vh) {
            items[position].let {

                //使用async，先获取图片的尺寸，然后根据尺寸加载图片
                async(UI) {
                    var size = intArrayOf()
                    withContext(CommonPool) {
                        //获取图片尺寸，因为是访问网络，所以需要异步操作
                        size = getImageSizeAhead(it.text)
                    }
                        Log.d(LOG_TAG_COMMON, "width = ${size[0]}, height = ${size[1]}")
                    requestOptions.override(size[0], size[1])
                    Glide.with(articleImage.context)
                            .load(it.text)
                            .apply(requestOptions)
                            .transition(withCrossFade()) //渐显效果
                            .into(articleImage)
                }
            }
        }
    }

    companion object {
        class ArticleImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val articleImage: ImageView = itemView.article_image
        }

        //Glide参数，使用单例模式
        lateinit var requestOptions: RequestOptions
            private set
    }

    init {
        requestOptions = RequestOptions()

        requestOptions.fitCenter()
        requestOptions.dontAnimate()  //这一行非常重要，如果没有此设置，则图片首先不会按照原始比例显示，会按正方形拉伸显示，滑动后才显示正常
//        requestOptions.placeholder(R.drawable.ic_placeholder_900dp)
        requestOptions.placeholder(ColorDrawable(Color.GRAY))
        requestOptions.error(R.drawable.ic_image_error)
        requestOptions.diskCacheStrategy(DiskCacheStrategy.ALL)
    }
}