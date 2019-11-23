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
        val chat1 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Uno",
            "@albertouno",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "08:00 a.m"
        )

        val chat2 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Dos",
            "@albertodos",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "09:00 a.m"
        )

        val chat3 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Tres",
            "@albertotres",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "10:00 a.m"
        )

        val chat4 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Cuatro",
            "@albertocuatro",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "11:00 a.m"
        )

        val chat5 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Cinco",
            "@albertocinco",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "12:00 a.m"
        )

        val chat6 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Seis",
            "@albertoseis",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "01:00 p.m"
        )

        val chat7 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Siete",
            "@albertosiete",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "02:00 p.m"
        )

        val chat8 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Ocho",
            "@albertoocho",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "03:00 p.m"
        )

        val chat9 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Nueve",
            "@albertonueve",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
            "04:00 p.m"
        )

        val chat10 = Chat(
            "https://images.vexels.com/media/users/3/145908/preview2/52eabf633ca6414e60a7677b0b917d92-creador-de-avatar-masculino.jpg",
            "Alberto Diez",
            "@albertodiez",
            "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
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
