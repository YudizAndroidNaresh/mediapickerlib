package com.mediapickerlib.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView


open class BaseAdapter<T>() : RecyclerView.Adapter<BaseAdapter<T>.ViewHolder>() {
    private var layoutId: Int = 0
    private var list: List<T>? = null
    private var viewHolder: ((RecyclerView.ViewHolder, T) -> Unit)? = null
    private val clickableViews = ArrayList<Int>()
    private var clickListener: ((View, Int) -> Unit)? = null

    constructor(layoutId: Int, list: List<T>?, viewHolder: ((RecyclerView.ViewHolder, T) -> Unit)) : this(
        layoutId,
        list,
        viewHolder,
        listOf(),
        null
    )

    constructor(
        layoutId: Int,
        list: List<T>?,
        viewHolder: ((RecyclerView.ViewHolder, T) -> Unit),
        clickableViews: List<Int>,
        clickListener: ((View, Int) -> Unit)?
    ) : this() {
        this.layoutId = layoutId
        this.list = list
        this.viewHolder = viewHolder
        this.clickableViews.addAll(clickableViews)
        this.clickListener = clickListener
    }

    constructor(
        layoutId: Int,
        list: List<T>?,
        viewHolder: ((RecyclerView.ViewHolder, T) -> Unit),
        clickableView: Int,
        clickListener: ((View, Int) -> Unit)? = null
    ) : this(layoutId, list, viewHolder, listOf(clickableView), clickListener)

    override fun getItemCount() = list?.size ?: 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(layoutId, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        viewHolder?.apply { this(holder, list!![position]) }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        init {
            for (clickableView in clickableViews)
                view.findViewById<View>(clickableView).setOnClickListener { v ->
                    clickListener?.invoke(
                        v,
                        adapterPosition
                    )
                }
        }
    }
}