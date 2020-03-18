package com.naposystems.pepito.ui.conversation.test

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.naposystems.pepito.R

class TestFragment : Fragment() {

    companion object {
        fun newInstance(title: String) = TestFragment().apply {
            arguments = Bundle().apply {
                putString("TITLE", title)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_test, container, false)

        val textViewName = view.findViewById<TextView>(R.id.textView_name)

        arguments?.let {
            val title = it.getString("TITLE")
            textViewName.text = title
        }
        return view
    }

}
