package com.lyadirga.bildirimleogren.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lyadirga.bildirimleogren.databinding.ItemBinding
import com.lyadirga.bildirimleogren.model.Language
import java.util.Locale

class LanguageListAdapter(
    private val context: Context
) : ListAdapter<Language, LanguageListAdapter.MyViewHolder>(DiffCallback), TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    inner class MyViewHolder(val binding: ItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            sentence.text = item.wordOrSentence
            mean.text = item.meaning

            item.imageResId?.let {
                image.visibility = android.view.View.VISIBLE
                image.setImageResource(it)
            } ?: run {
                image.visibility = android.view.View.GONE
            }

            root.setOnClickListener {
                speakOut(item.wordOrSentence)
            }
        }
    }

    // DiffUtil for efficient updates
    object DiffCallback : DiffUtil.ItemCallback<Language>() {
        override fun areItemsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem.wordOrSentence == newItem.wordOrSentence &&
                    oldItem.meaning == newItem.meaning
        }

        override fun areContentsTheSame(oldItem: Language, newItem: Language): Boolean {
            return oldItem == newItem
        }
    }

    private fun speakOut(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                val message = when (result) {
                    TextToSpeech.LANG_MISSING_DATA -> "Dil verisi eksik"
                    TextToSpeech.LANG_NOT_SUPPORTED -> "Dil desteklenmiyor"
                    else -> "Bilinmeyen hata durumu: $result"
                }
                context.showToast(message)
            }
        }
    }

    fun releaseTextToSpeech() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}
