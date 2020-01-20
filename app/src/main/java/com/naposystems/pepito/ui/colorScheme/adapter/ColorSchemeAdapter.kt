package com.naposystems.pepito.ui.colorScheme.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.naposystems.pepito.databinding.ColorSchemeItemBinding
import com.naposystems.pepito.entity.Theme

class ColorSchemeAdapter constructor(
    private val items: List<Theme>,
    private val clickListener: ColorSchemeListener
) :
    RecyclerView.Adapter<ColorSchemeAdapter.ColorSchemeViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ColorSchemeViewHolder {
        return ColorSchemeViewHolder.from(parent)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ColorSchemeViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, clickListener)
    }

    class ColorSchemeViewHolder constructor(private val binding: ColorSchemeItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Theme, clickListener: ColorSchemeListener) {
            binding.theme = item
            binding.clickListener = clickListener
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): ColorSchemeViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ColorSchemeItemBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )

                return ColorSchemeViewHolder(binding)
            }
        }

    }

    class ColorSchemeListener(val clickListener: (item: Theme) -> Unit) {
        fun onClick(item: Theme) = clickListener(item)
    }
}