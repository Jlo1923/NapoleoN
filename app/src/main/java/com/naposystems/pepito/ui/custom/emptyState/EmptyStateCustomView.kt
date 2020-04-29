package com.naposystems.pepito.ui.custom.emptyState

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil
import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.EmptyStateBinding
import timber.log.Timber
import java.lang.Exception

class EmptyStateCustomView(context: Context, attrs: AttributeSet) :
    ConstraintLayout(context, attrs), IContractEmptyState {

    private lateinit var binding: EmptyStateBinding
    private var imageId: Int = 0
    private var title: String = ""
    private var description: String = ""

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.EmptyStateCustomView,
            0, 0
        ).apply {
            try {
                val layoutInflater = getContext().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE
                ) as LayoutInflater

                binding = DataBindingUtil.inflate(
                    layoutInflater,
                    R.layout.empty_state,
                    this@EmptyStateCustomView,
                    true
                )

                imageId = getResourceId(
                    R.styleable.EmptyStateCustomView_image,
                    R.drawable.logo_napoleon_app
                )
                title = getString(R.styleable.EmptyStateCustomView_title) ?: ""
                description = getString(R.styleable.EmptyStateCustomView_description) ?: ""

                setImageEmptyState(imageId)
                binding.textViewTitle.text = title
                binding.textViewDescription.text = description


            } catch (ex: Exception) {
                Timber.e(ex)
            }
        }

    }

    override fun setImageEmptyState(drawable: Int) {
        binding.imageViewEmptyState.setImageDrawable(
            context?.let { context ->
                context.getDrawable(drawable)
            }
        )
    }

    override fun setTitleEmptyState(string: Int) {
        binding.textViewTitle.text = context.getString(string)
    }

    override fun setDescriptionEmptyState(string: Int) {
        binding.textViewDescription.text = context.getString(string)
    }

}