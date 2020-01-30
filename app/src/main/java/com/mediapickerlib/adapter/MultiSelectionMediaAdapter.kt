package com.mediapickerlib.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mediapickerlib.BR
import com.mediapickerlib.R
import com.mediapickerlib.databinding.RowMultiSelectionMediaBinding
import com.mediapickerlib.modals.Img


class MultiSelectionMediaAdapter(
    val context: Context,
    val listener: SingleSelectionMediaAdapter.OnSelectedMediaAdapterListener
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mMediaList: ArrayList<Img> = ArrayList()
    private var mSelectedMedia: ArrayList<Img> = ArrayList()
    private val PAYLOAD_COUNTER = 12345

    open inner class RecyclerViewHolderParent(private val mBinding: RowMultiSelectionMediaBinding) :
        RecyclerView.ViewHolder(mBinding.root), View.OnClickListener {

        fun initView(position: Int) {
            mBinding.setVariable(BR.click, this)
            mBinding.setVariable(BR.position, position)
            mBinding.setVariable(BR.media, mMediaList[position])
            mBinding.executePendingBindings()
        }

        fun initViewPayload(position: Int) {
            if (mSelectedMedia.contains(mMediaList[position])) {
                mBinding.tvSelectionCount.visibility = View.VISIBLE
                mBinding.tvSelectionCount.text =
                    (mSelectedMedia.indexOf(mMediaList[position]) + 1).toString()
            } else {
                mBinding.tvSelectionCount.visibility = View.GONE
            }
            mBinding.executePendingBindings()
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.cv_image_container -> {
                    if (mSelectedMedia.contains(mMediaList[adapterPosition])) {
                        mSelectedMedia.remove(mMediaList[adapterPosition])
                        notifyItemRangeChanged(0, mMediaList.size, MediaAdapter.PAYLOAD_COUNTER)
                    } else {
                        mSelectedMedia.add(mMediaList[adapterPosition])
                        notifyItemChanged(adapterPosition, PAYLOAD_COUNTER)
                    }
                    listener.onMediaSelected(adapterPosition)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RecyclerViewHolderParent(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                R.layout.row_multi_selection_media,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = mMediaList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as RecyclerViewHolderParent).apply {
            this.initView(position)
            this.initViewPayload(position)
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

    fun addMedia(img: Img) {
        mMediaList.add(img)
        mSelectedMedia.add(img)
        notifyItemInserted(mMediaList.size - 1)
        listener.onMediaSelected(mMediaList.size - 1)
    }

    fun clearMedia() {
        mMediaList.clear()
        mSelectedMedia.clear()
        notifyDataSetChanged()
    }

    fun getSelectedMedia() = mSelectedMedia
    fun getCaptureMedia() = mMediaList
}