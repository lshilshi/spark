package com.lm.ll.spark.adapter

import android.support.constraint.ConstraintLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.lm.ll.spark.R
import com.lm.ll.spark.db.News

/**
 * 作者：Created by ll on 2018-05-28 13:36.
 * 邮箱：wenhelinlu@gmail.com
 */
class NewsAdapter(private val newsList: List<News>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return NewsListViewHolder(inflater.inflate(R.layout.news_item,parent,false))
    }

    override fun getItemCount(): Int {
        return newsList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: NewsListViewHolder = holder as NewsListViewHolder
        viewHolder.newsTitle.text = newsList[position].title
        viewHolder.newsDesc.text = "来自${newsList[position].author}"
        viewHolder.newsTime.text = newsList[position].date
    }

    internal inner class NewsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var newsItem: ConstraintLayout
        var newsTitle: TextView
        var newsDesc: TextView
        var newsTime: TextView

        init {
            newsItem = itemView.findViewById(R.id.news_item)
            newsTitle = itemView.findViewById(R.id.news_title)
            newsDesc = itemView.findViewById(R.id.news_desc)
            newsTime = itemView.findViewById(R.id.news_time)
        }
    }

}