package com.mediapickerlib.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mediapickerlib.BR
import com.mediapickerlib.R
import com.mediapickerlib.databinding.RowSingleSelectionMediaBinding
import com.mediapickerlib.modals.Img

class SingleSelectionMediaAdapter(val context: Context, val mMediaList: ArrayList<Img>, val listener: OnSelectedMediaAdapterListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mSelectedPos = -1

    open inner class RecyclerViewHolderParent(private val mBinding: RowSingleSelectionMediaBinding) :
        RecyclerView.ViewHolder(mBinding.root), View.OnClickListener {

        fun initView(position: Int) {
            mBinding.setVariable(BR.click, this)
            mBinding.setVariable(BR.position, position)
            mBinding.setVariable(BR.media, mMediaList[position])
            mBinding.setVariable(BR.isSelected, mSelectedPos == position)
            mBinding.executePendingBindings()
        }

        fun initViewPayload(position: Int) {
            mBinding.setVariable(BR.isSelected, mSelectedPos == position)
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.cv_image_container -> {
                    mSelectedPos = adapterPosition
                    listener.onMediaSelected(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RecyclerViewHolderParent(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_single_selection_media,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mMediaList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as RecyclerViewHolderParent).apply {
            this.initView(position)
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isNotEmpty()) {
            if (payloads[0] is Int) {
                (holder as RecyclerViewHolderParent).initViewPayload(position)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    interface OnSelectedMediaAdapterListener{
        fun onMediaSelected(position: Int)
    }
    fun addMedias(media:ArrayList<Img>){
        mMediaList.addAll(media)
        ///mSelectedMedia.addAll(media)
        notifyItemInserted(mMediaList.size - 1)
        //listener.onMediaSelected(mMediaList.size - 1)
    }

    fun getImg( position: Int) : Img = mMediaList[position]


}