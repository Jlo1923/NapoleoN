package com.naposystems.pepito.ui.home

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.HomeFragmentBinding
import com.naposystems.pepito.databinding.HomeFragmentItemBinding
import com.naposystems.pepito.model.home.Chat
import com.naposystems.pepito.ui.home.adapter.ChatAdapter
import com.naposystems.pepito.ui.mainActivity.MainActivity

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: HomeFragmentBinding
    lateinit var adapter: ChatAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(layoutInflater, R.layout.home_fragment, container, false)

        adapter = ChatAdapter(getChats())

        binding.recyclerViewChats.adapter = adapter

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        (activity as MainActivity).getUser()
    }

    private fun getChats(): List<Chat> {

        val imageUrl = "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg"
        val message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."

        val chat1 = Chat(
            imageUrl,
            "Alberto Uno",
            "@albertouno",
            message,
            "08:00 a.m"
        )

        val chat2 = Chat(
            imageUrl,
            "Alberto Dos",
            "@albertodos",
            message,
            "09:00 a.m"
        )

        val chat3 = Chat(
            imageUrl,
            "Alberto Tres",
            "@albertotres",
            message,
            "10:00 a.m"
        )

        val chat4 = Chat(
            imageUrl,
            "Alberto Cuatro",
            "@albertocuatro",
            message,
            "11:00 a.m"
        )

        val chat5 = Chat(
            imageUrl,
            "Alberto Cinco",
            "@albertocinco",
            message,
            "12:00 a.m"
        )

        val chat6 = Chat(
            imageUrl,
            "Alberto Seis",
            "@albertoseis",
            message,
            "01:00 p.m"
        )

        val chat7 = Chat(
            imageUrl,
            "Alberto Siete",
            "@albertosiete",
            message,
            "02:00 p.m"
        )

        val chat8 = Chat(
            imageUrl,
            "Alberto Ocho",
            "@albertoocho",
            message,
            "03:00 p.m"
        )

        val chat9 = Chat(
            imageUrl,
            "Alberto Nueve",
            "@albertonueve",
            message,
            "04:00 p.m"
        )

        val chat10 = Chat(
            imageUrl,
            "Alberto Diez",
            "@albertodiez",
            message,
            "05:00 p.m"
        )

        val chatList = ArrayList<Chat>()

        chatList.add(chat1)
        chatList.add(chat2)
        chatList.add(chat3)
        chatList.add(chat4)
        chatList.add(chat5)
        chatList.add(chat6)
        chatList.add(chat7)
        chatList.add(chat8)
        chatList.add(chat9)
        chatList.add(chat10)
        chatList.add(chat1)
        chatList.add(chat2)
        chatList.add(chat3)
        chatList.add(chat4)
        chatList.add(chat5)
        chatList.add(chat6)
        chatList.add(chat7)
        chatList.add(chat8)
        chatList.add(chat9)
        chatList.add(chat10)

        return chatList
    }

}
