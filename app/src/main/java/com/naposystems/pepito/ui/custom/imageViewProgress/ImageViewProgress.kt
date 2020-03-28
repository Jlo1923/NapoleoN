package com.naposystems.pepito.ui.custom.imageViewProgress

import android.content.Context
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.gif.GifDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.CustomViewImageViewProgressBinding
import java.io.File

class ImageViewProgress constructor(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs),
    IContractImageViewProgress {

    private var binding: CustomViewImageViewProgressBinding

    private var mColor: Int = R.color.colorPrimary

    init {

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.ImageViewProgress,
            0, 0
        ).apply {
            try {
                val infService = Context.LAYOUT_INFLATER_SERVICE
                val layoutInflater =
                    getContext().getSystemService(infService) as LayoutInflater

                binding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.custom_view_image_view_progress,
                    this@ImageViewProgress,
                    true
                )

                mColor = getResourceId(R.styleable.ImageViewProgress_progressbarColor, mColor)

                binding.progressBar.apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val colorFilter = BlendModeColorFilter(mColor, BlendMode.SRC_IN)
                        indeterminateDrawable.colorFilter = colorFilter
                    } else {
                        indeterminateDrawable.setColorFilter(
                            ContextCompat.getColor(
                                context,
                                mColor
                            ),
                            PorterDuff.Mode.SRC_IN
                        )
                    }
                }


            } finally {
                recycle()
            }
        }
    }

    override fun loadImageFile(file: File) {
        Glide.with(binding.imageView)
            .load(file)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.INVISIBLE
                    return false
                }
            })
            .into(binding.imageView)
    }

    override fun loadImageFileAsGif(file: File) {
        Glide.with(binding.imageView)
            .asGif()
            .load(file)
            .listener(object : RequestListener<GifDrawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }

                override fun onResourceReady(
                    resource: GifDrawable?,
                    model: Any?,
                    target: Target<GifDrawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    binding.progressBar.visibility = View.INVISIBLE
                    return false
                }
            })
            .into(binding.imageView)
    }
}