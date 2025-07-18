package com.example.caller_id.ui.main.fragment.block.adapterblock

import android.content.Context
import android.graphics.drawable.PictureDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import com.example.caller_id.databinding.ItemSpinnerCountryBinding
import com.example.caller_id.model.CountryCodeItem

class CountrySpinnerAdapter(
    private val context: Context,
    private val items: List<CountryCodeItem>
) : BaseAdapter() {

    override fun getCount(): Int = items.size
    override fun getItem(position: Int): Any = items[position]
    override fun getItemId(position: Int): Long = position.toLong()

    private fun createView(position: Int, parent: ViewGroup?): View {
        val binding = ItemSpinnerCountryBinding.inflate(LayoutInflater.from(context), parent, false)
        val item = items[position]

        loadSvgFromAssets(context, item.isoCode, binding.ivFlag)
        binding.tvCountryInfo.text = "${item.countryName} (${item.isoCode}) ${item.dialCode}"

        return binding.root
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createView(position, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        return createView(position, parent)
    }

    private fun loadSvgFromAssets(context: Context, isoCode: String, imageView: ImageView) {
        val assetPath = "flags/${isoCode.lowercase()}.svg"
        try {
            val inputStream = context.assets.open(assetPath)
            val svg = SVG.getFromInputStream(inputStream)
            val drawable = PictureDrawable(svg.renderToPicture())

            imageView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
            imageView.setImageDrawable(drawable)

            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

