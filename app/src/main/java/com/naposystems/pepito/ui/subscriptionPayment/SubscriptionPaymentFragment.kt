package com.naposystems.pepito.ui.subscriptionPayment

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.navArgs

import com.naposystems.pepito.R
import com.naposystems.pepito.databinding.SubscriptionFragmentBinding
import com.naposystems.pepito.databinding.SubscriptionPaymentFragmentBinding
import dagger.android.support.AndroidSupportInjection

class SubscriptionPaymentFragment : Fragment() {

    companion object {
        fun newInstance() = SubscriptionPaymentFragment()
    }

    private lateinit var viewModel: SubscriptionPaymentViewModel
    private lateinit var binding: SubscriptionPaymentFragmentBinding
    private val args: SubscriptionPaymentFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.subscription_payment_fragment,
            container,
            false
        )

        binding.WebViewPayment.apply {
            loadUrl(args.url)
            settings.javaScriptEnabled = true
            webViewClient = object : WebViewClient() {

                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?
                ): Boolean {
                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    super.onPageStarted(view, url, favicon)
                    binding.progressBar.isVisible = true
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.progressBar.isVisible = false
                }
            }
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this)
            .get(SubscriptionPaymentViewModel::class.java)


    }

}
