package com.android.fake.flo.fakeflo.custom

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.fake.flo.fakeflo.R
import com.android.fake.flo.fakeflo.viewmodel.LyricData
import com.android.fake.flo.fakeflo.viewmodel.LyricViewModel


class LyricView(context: Context, attrs: AttributeSet? = null) : LinearLayout(context, attrs) {
    companion object {
        private const val TAG = "LyricView"

        private const val VIEW_TYPE_ITEM = 1
        private const val WHAT_SEND = 1000
    }

    interface CloseButtonClickListener {
        fun onCloseButtonClick()
    }

    var listener: CloseButtonClickListener? = null

    private val tvTitle: TextView
    private val tvClose: TextView
    private val recyclerView: RecyclerView

    private var lyricAdapter: LyricAdapter = LyricAdapter()
    private lateinit var lyricViewModel: LyricViewModel

    private var lastPosition = -1

    fun setLyricViewModel(lyricViewModel: LyricViewModel) {
        this.lyricViewModel = lyricViewModel
        this.lyricViewModel.position.observe(context as AppCompatActivity) {
            if (lastPosition != it) {
                lyricAdapter.notifyDataSetChanged()
            }
            lastPosition = it
        }
    }

    fun setTitle(title: String?) {
        if (!title.isNullOrEmpty()) {
            tvTitle.text = title
        }
    }

    var lyricDataList = mutableListOf<LyricData>()
        set(value) {
            field.addAll(value)
            this.lyricViewModel.lyricDataList = value
            this.lyricViewModel.run()
            lyricAdapter.notifyDataSetChanged()
        }

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.lyric_view, this, true)
        tvTitle = view.findViewById(R.id.tv_title)
        tvClose = view.findViewById(R.id.tv_close)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = lyricAdapter
            setHasFixedSize(true)
        }

        tvClose.setOnClickListener {
            listener?.onCloseButtonClick()
        }
    }

    private inner class LyricAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        override fun getItemCount(): Int {
            return lyricDataList.size
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return ItemViewHolder(LayoutInflater.from(context).inflate(R.layout.list_lyric_item, parent, false))
        }

        override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, position: Int) {
            val vh = viewHolder as ItemViewHolder
            val data = lyricDataList[position]

            vh.tvLyric.text = data.lyric
            vh.tvLyric.textColors

            var textColor = ContextCompat.getColor(context, R.color.black)
            if (lyricViewModel.position.value == position) {
                textColor = ContextCompat.getColor(context, R.color.teal_700)
            }
            vh.tvLyric.setTextColor(textColor)
        }

        private inner class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val tvLyric: TextView = view.findViewById(R.id.tv_lyric)

            init {
                tvLyric.isSelected = true
            }
        }
    }
}