package com.naposystems.napoleonchat.ui.subscription.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.android.billingclient.api.SkuDetails
import com.naposystems.napoleonchat.R
import com.naposystems.napoleonchat.databinding.SubscriptionItemBinding
import com.naposystems.napoleonchat.utility.Constants

class SkuDetailsAdapter constructor(
    context: Context,
    resource: Int,
    private val list: List<SkuDetails>
) : ArrayAdapter<SkuDetails>(context, resource, list) {

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getCustomView(position, convertView, parent)
    }

    private fun getCustomView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = LayoutInflater.from(parent.context)

        val binding = SubscriptionItemBinding.inflate(inflater, parent, false)
        binding.skuDetail = list[position]

        val context = binding.textViewSubscriptionItem.context

        val skuDetail = list[position]

        val subscriptionTitle= when(skuDetail.sku){
            Constants.SkuSubscriptions.MONTHLY.sku -> context.getString(R.string.text_subscription_monthly)
            Constants.SkuSubscriptions.SEMIANNUAL.sku -> context.getString(R.string.text_subscription_semiannual)
            else -> context.getString(R.string.text_subscription_yearly)
        }

        binding.textViewSubscriptionItem.text = "$subscriptionTitle ${skuDetail.price} (${skuDetail.priceCurrencyCode})"

        return binding.root
    }
}