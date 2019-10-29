package com.naposystems.pepito.ui.splash

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

import com.naposystems.pepito.R
import java.util.concurrent.TimeUnit

class SplashFragment : Fragment() {

    private lateinit var viewModel: SplashViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProviders.of(this).get(SplashViewModel::class.java)

        viewModel.navigateToLanding.observe(this, Observer {
            if (it == true) {
                findNavController().navigate(SplashFragmentDirections.actionSplashFragmentToLandingFragment())
                viewModel.doneNavigateToLanding()
            }
        })

        Handler().postDelayed({
            context?.let {
                viewModel.onLoadingTimeEnd()
            }
        }, TimeUnit.SECONDS.toMillis(2))

        return inflater.inflate(R.layout.splash_fragment, container, false)
    }

}
