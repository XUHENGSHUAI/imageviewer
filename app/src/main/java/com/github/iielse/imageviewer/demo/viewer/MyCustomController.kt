package com.github.iielse.imageviewer.demo.viewer

import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.github.iielse.imageviewer.ImageViewerActionViewModel
import com.github.iielse.imageviewer.ImageViewerBuilder
import com.github.iielse.imageviewer.adapter.ItemType
import com.github.iielse.imageviewer.core.OverlayCustomizer
import com.github.iielse.imageviewer.core.Photo
import com.github.iielse.imageviewer.core.VHCustomizer
import com.github.iielse.imageviewer.core.ViewerCallback
import com.github.iielse.imageviewer.demo.R
import com.github.iielse.imageviewer.demo.data.MyData
import com.github.iielse.imageviewer.utils.inflate
import com.github.iielse.imageviewer.widgets.PhotoView2
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer


class MyCustomController(private val activity: FragmentActivity) {
    private val viewerActions by lazy { ViewModelProvider(activity).get(ImageViewerActionViewModel::class.java) }
    private var indicatorDecor: View? = null
    private var overlayIndicator: TextView? = null
    private var preIndicator: TextView? = null
    private var nextIndicator: TextView? = null
    private var currentPosition = -1

    private var playingVH: RecyclerView.ViewHolder? = null

    fun init(builder: ImageViewerBuilder) {
        builder.setVHCustomizer(object : VHCustomizer {
            override fun initialize(type: Int, viewHolder: RecyclerView.ViewHolder) {
                (viewHolder.itemView as? ViewGroup?)?.let {
                    it.addView(it.inflate(R.layout.item_photo_custom_layout))

                    when (type) {
                        ItemType.SUBSAMPLING -> {
                            val imageView = it.findViewById<View>(R.id.subsamplingView)
                            imageView.setOnClickListener {
                                viewerActions.dismiss()
                            }
                        }
                        ItemType.PHOTO -> {
                            val imageView = it.findViewById<View>(R.id.photoView)
                            imageView.setOnClickListener {
                                viewerActions.dismiss()
                            }
                        }
                    }
                }
            }

            override fun bind(type: Int, data: Photo, viewHolder: RecyclerView.ViewHolder) {
                (viewHolder.itemView as? ViewGroup?)?.let {
                    val x = data as MyData
                    it.findViewById<TextView>(R.id.exText).text = x.desc

                    when (type) {
                        ItemType.PHOTO -> {
                            val photoView = it.findViewById<PhotoView2>(R.id.photoView)
                            val videoView = it.findViewById<StandardGSYVideoPlayer>(R.id.videoView)
                            val play = it.findViewById<View>(R.id.play)
                            if (data.url.endsWith(".mp4")) {
                                photoView.visibility = View.VISIBLE
                                photoView.isEnabled = false
                                videoView.visibility = View.GONE
                                play.visibility = View.VISIBLE

                                videoView.setUp(data.url, true, "")
                                videoView.titleTextView.visibility = View.GONE
                                videoView.backButton.visibility = View.GONE
                                videoView.setIsTouchWiget(true)
                                videoView.setVideoAllCallBack(object : GSYSampleCallBack() {
                                    override fun onAutoComplete(url: String?, vararg objects: Any?) {
                                        play.visibility = View.VISIBLE
                                        photoView.visibility = View.VISIBLE
                                        videoView.visibility = View.GONE
                                    }
                                })
                                play.setOnClickListener {
                                    videoView.startPlayLogic()
                                    playingVH = viewHolder
                                    videoView.visibility = View.VISIBLE
                                    play.visibility = View.GONE
                                }
                            } else {
                                photoView.visibility = View.VISIBLE
                                photoView.isEnabled = true
                                videoView.visibility = View.GONE
                                play.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        })
        builder.setOverlayCustomizer(object : OverlayCustomizer {
            override fun provideView(parent: ViewGroup): View? {
                return parent.inflate(R.layout.layout_indicator).also {
                    indicatorDecor = it.findViewById(R.id.indicatorDecor)
                    overlayIndicator = it.findViewById(R.id.indicator)
                    preIndicator = it.findViewById(R.id.pre)
                    nextIndicator = it.findViewById(R.id.next)

                    preIndicator?.setOnClickListener {
                        viewerActions.setCurrentItem(currentPosition - 1)
                    }
                    nextIndicator?.setOnClickListener {
                        viewerActions.setCurrentItem(currentPosition + 1)
                    }

                    it.findViewById<View>(R.id.dismiss).setOnClickListener {
                        viewerActions.dismiss()
                    }
                }
            }
        })
        builder.setViewerCallback(object : ViewerCallback {
            override fun onRelease(viewHolder: RecyclerView.ViewHolder, view: View) {
                viewHolder.itemView.findViewById<View>(R.id.customizeDecor)
                        .animate().setDuration(200).alpha(0f).start()
                indicatorDecor?.animate()?.setDuration(200)?.alpha(0f)?.start()
                releaseVideo()
            }

            override fun onPageSelected(position: Int) {
                currentPosition = position
                overlayIndicator?.text = position.toString()
                releaseVideo()
            }
        })
    }

    private fun releaseVideo() {
        val it = playingVH?.itemView ?: return
        val photoView = it.findViewById<View>(R.id.photoView)
        val videoView = it.findViewById<StandardGSYVideoPlayer>(R.id.videoView)
        val play = it.findViewById<View>(R.id.play)
        play.visibility = View.VISIBLE
        photoView.visibility = View.VISIBLE
        videoView.release()
        videoView.visibility = View.GONE
    }
}