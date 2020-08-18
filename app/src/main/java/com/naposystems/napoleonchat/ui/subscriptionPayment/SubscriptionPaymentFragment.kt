package com.naposystems.napoleonchat.ui.subscriptionPayment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.SubscriptionPaymentFragmentBinding

class SubscriptionPaymentFragment : Fragment() {

    companion object {
        fun newInstance() = SubscriptionPaymentFragment()
    }

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

                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    binding.containerProgressPayment.visibility = View.GONE
                    binding.progressBarPayment.visibility = View.GONE
                }
            }
        }

        return binding.root
    }

}
