package com.naposystems.napoleonchat.ui.previewmulti.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager.HORIZONTAL
import com.naposystems.napoleonchat.databinding.ViewAttachmentOptionsBinding
import com.naposystems.napoleonchat.ui.previewmulti.model.AttachmentOptionItem
import com.naposystems.napoleonchat.ui.previewmulti.model.AttachmentOptionItem.*
import com.naposystems.napoleonchat.ui.previewmulti.views.items.AttachmentOptionItemView
import com.naposystems.napoleonchat.ui.previewmulti.listeners.ViewAttachmentOptionsListener
import com.xwray.groupie.GroupieAdapter

class ViewAttachmentOptions @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    private var listener: ViewAttachmentOptionsListener? = null
    private val groupieAdapter = GroupieAdapter()

    private val viewBinding: ViewAttachmentOptionsBinding =
        ViewAttachmentOptionsBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

    init {
        configureRecycler()
        configureElements()
        defineListeners()
    }

    private fun defineListeners() {
        groupieAdapter.setOnItemClickListener { item, _ ->
            when (item) {
                is AttachmentOptionItemView -> handleAttachmentOptionClick(item.item)
            }
        }
    }

    private fun handleAttachmentOptionClick(item: AttachmentOptionItem) {
        when (item) {
            AUTO_DESTRUCTION -> listener?.onChangeSelfDestruction()
            CAN_RESEND -> TODO()
            CAN_DOWNLOAD -> TODO()
            DELETE -> TODO()
        }
    }

    private fun configureElements() {

        val listElements = mutableListOf<AttachmentOptionItemView>()

        val elementAutoDestruction = AttachmentOptionItemView(item = AUTO_DESTRUCTION)

        listElements.add(elementAutoDestruction)

        groupieAdapter.update(listElements)

    }

    private fun configureRecycler() {
        val layoutManager = LinearLayoutManager(viewBinding.root.context, HORIZONTAL, false)
        viewBinding.recyclerOptions.apply {
            adapter = groupieAdapter
            setLayoutManager(layoutManager)
        }
    }

    fun defineListener(listener: ViewAttachmentOptionsListener) {
        this.listener = listener
    }

    fun changeDrawableSelfDestructionOption(iconSelfDestruction: Int) {
        val itemView = groupieAdapter.getItem(0) as AttachmentOptionItemView
        itemView.changeDrawableIcon(iconSelfDestruction)
    }

}