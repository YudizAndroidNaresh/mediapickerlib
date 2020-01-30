package com.mediapickerlib.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.mediapickerlib.BR
import com.mediapickerlib.R
import com.mediapickerlib.base.MediaType
import com.mediapickerlib.base.SelectMedia
import com.mediapickerlib.builder.Media.Companion.getBuilder
import com.mediapickerlib.databinding.RowHeaderBinding
import com.mediapickerlib.databinding.RowMediaBinding
import com.mediapickerlib.modals.Img
import java.text.DecimalFormat

class MediaAdapter(val context: Context, val listener: OnAdapterEventListener) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        /**
         * Row Type
         */
        const val HEADER = 1
        const val MEDIA = 2
        const val PAYLOAD_COUNTER = 12345
    }

    private var mMediaList: ArrayList<Img> = ArrayList()
    private var mSelectMedia: ArrayList<Img> = ArrayList()

    open inner class RecyclerViewHolderParent(view: View) : RecyclerView.ViewHolder(view) {
        private var mPosition = -1

        open fun initView(position: Int) {
            mPosition = position
        }
    }

    inner class ViewHolderMedia(private val mBinding: RowMediaBinding) :
        RecyclerViewHolderParent(mBinding.root), View.OnClickListener {
        private var mPosition = -1

        override fun initView(position: Int) {
            mPosition = position
            mBinding.setVariable(BR.click, this)
            mBinding.setVariable(BR.position, position)
            mBinding.setVariable(BR.media, mMediaList[position])
            mBinding.executePendingBindings()
        }

        fun initViewPayload(position: Int) {
            if (mSelectMedia.contains(mMediaList[position])) {
                mBinding.tvSelectionCount.visibility = View.VISIBLE
                mBinding.tvSelectionCount.text =
                    (mSelectMedia.indexOf(mMediaList[position]) + 1).toString()
            } else {
                mBinding.tvSelectionCount.visibility = View.GONE
            }
            mBinding.executePendingBindings()
        }

        override fun onClick(v: View?) {
            when (v?.id) {
                R.id.root_layout -> {
                    if (getBuilder().selectMedia == SelectMedia.SINGLE) {
                        /**
                         * if Builder media size -1 then not check condition
                         * check selected media size is not more {Builder media size}
                         */
                        if (getBuilder().selectMediaMaxSize != -1L && getBuilder().selectMediaMaxSize < mMediaList[adapterPosition].size) {
                            Toast.makeText(context, getMediaSizeExitsMessage(), Toast.LENGTH_SHORT)
                                .show()
                            return
                        }

                        mSelectMedia.add(mMediaList[adapterPosition])
                        listener.onSingleMediaSelected()
                    } else {
                        /**
                         * if Builder media count -1 then not check condition
                         * check selected media count is not more than {Builder media count}
                         */
                        if (getBuilder().selectMediaCount != -1 && getBuilder().selectMediaCount == mSelectMedia.size) {
                            Toast.makeText(
                                context,
                                getSelectCountExitsMessage(),
                                Toast.LENGTH_SHORT
                            ).show()
                            return
                        }

                        /**
                         * if Builder media size -1 then not check condition
                         * check selected media size is not more {Builder media size}
                         */
                        if (getBuilder().selectMediaMaxSize != -1L && getBuilder().selectMediaMaxSize < mMediaList[adapterPosition].size) {
                            Toast.makeText(context, getMediaSizeExitsMessage(), Toast.LENGTH_SHORT)
                                .show()
                            return
                        }

                        if (mSelectMedia.contains(mMediaList[adapterPosition])) {
                            mSelectMedia.remove(mMediaList[adapterPosition])
                            notifyItemRangeChanged(0, mMediaList.size, PAYLOAD_COUNTER)
                        } else {
                            mSelectMedia.add(mMediaList[adapterPosition])
                            notifyItemChanged(adapterPosition, PAYLOAD_COUNTER)
                        }
                        listener.onMultiSelectionModeEnable(getSelectedMedia().isNotEmpty())
                    }
                }
            }
        }
    }

    inner class ViewHolderHeader(private val mBinding: RowHeaderBinding) :
        RecyclerViewHolderParent(mBinding.root) {
        override fun initView(position: Int) {
            mBinding.setVariable(BR.header, mMediaList[position].headerDate)
            mBinding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            MEDIA -> {
                ViewHolderMedia(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.row_media,
                        parent,
                        false
                    )
                )
            }

            else -> {
                ViewHolderHeader(
                    DataBindingUtil.inflate(
                        LayoutInflater.from(parent.context),
                        R.layout.row_header,
                        parent,
                        false
                    )
                )
            }
        }
    }

    override fun getItemCount(): Int = mMediaList.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (getItemViewType(position)) {
            MEDIA -> {
                (holder as ViewHolderMedia).apply {
                    this.initView(position)
                }
            }

            else -> {
                (holder as ViewHolderHeader).apply {
                    this.initView(position)
                }
            }
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        when (getItemViewType(position)) {
            MEDIA -> {
                if (payloads.isNotEmpty()) {
                    if (payloads[0] is Int) {
                        (holder as ViewHolderMedia).initViewPayload(position)
                    }
                } else {
                    super.onBindViewHolder(holder, position, payloads)
                }
            }
            else -> {
                (holder as ViewHolderHeader).apply {
                    this.initView(position)
                }
            }
        }
    }

    /**
     * Return layout type view
     */
    override fun getItemViewType(position: Int): Int {
        var pos = position
        if (pos == -1) {
            pos = 0
        }
        val media = mMediaList[pos]
        return if (media.contentUrl == "")
            HEADER
        else
            MEDIA
    }

    fun addImageList(list: ArrayList<Img>) {
        val oldListSize = mMediaList.size
        mMediaList.addAll(list)
        notifyItemRangeInserted(oldListSize, list.size)
    }

    fun getMediaList(): ArrayList<Img> = mMediaList

    fun getSelectedMedia(): ArrayList<Img> = mSelectMedia

    fun deSelectMedia() {
        mSelectMedia.clear()
        notifyItemRangeChanged(0, mMediaList.size, PAYLOAD_COUNTER)
        listener.onMultiSelectionModeEnable(getSelectedMedia().isNotEmpty())
    }

    fun clearList() {
        mMediaList.clear()
        notifyDataSetChanged()
    }

    fun clearSelectedMedia() {
        mSelectMedia.clear()
        notifyItemRangeChanged(0, mMediaList.size, PAYLOAD_COUNTER)
    }

    private fun getDisplayMediaType(): String {
        return when (getBuilder().mediaType) {
            MediaType.IMAGE -> context.getString(R.string.image)
            MediaType.VIDEO -> context.getString(R.string.video)
            MediaType.BOTH -> context.getString(R.string.media)
        }
    }

    private fun getMediaSizeExitsMessage(): String {
        val df = DecimalFormat("##.##")
        val size = df.format(getBuilder().selectMediaMaxSize / 1024.0)
        return "${context.getString(R.string.you_cant_select_more_than)} $size KB ${context.getString(
            R.string.size_of
        )} ${getDisplayMediaType()}"
    }

    private fun getSelectCountExitsMessage(): String {
        return "${context.getString(R.string.you_cant_select_more_than)} ${getBuilder().selectMediaCount} ${getDisplayMediaType()}"
    }

    interface OnAdapterEventListener {
        fun onMultiSelectionModeEnable(isSelectionMode: Boolean)
        fun onSingleMediaSelected()
    }
}