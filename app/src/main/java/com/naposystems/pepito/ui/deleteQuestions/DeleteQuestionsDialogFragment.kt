package com.naposystems.pepito.ui.deleteQuestions

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.DeleteQuestionsDialogFragmentBinding

class DeleteQuestionsDialogFragment : DialogFragment() {

    private lateinit var listener: DeleteQuestionsListener
    private lateinit var binding: DeleteQuestionsDialogFragmentBinding

    companion object {
        fun newInstance() = DeleteQuestionsDialogFragment()
    }

    interface DeleteQuestionsListener {
        fun onDeleteQuestionsChange()
    }

    private lateinit var viewModel: DeleteQuestionsDialogViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(
            inflater, R.layout.delete_questions_dialog_fragment, container, false
        )

        binding.buttonCancel.setOnClickListener {
            dismiss()
        }

        binding.buttonAccept.setOnClickListener {
            listener.onDeleteQuestionsChange()
            dismiss()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(DeleteQuestionsDialogViewModel::class.java)

        dialog!!.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    fun setListener(listener: DeleteQuestionsListener) {
        this.listener = listener
    }
}
