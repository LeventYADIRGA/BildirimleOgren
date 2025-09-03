package com.lyadirga.bildirimleogren.ui.setdetail

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lyadirga.bildirimleogren.data.LanguageEntity
import com.lyadirga.bildirimleogren.databinding.ItemBinding
import com.lyadirga.bildirimleogren.ui.showToast
import java.util.Locale

class SetItemsAdapter(
    private val context: Context
) : ListAdapter<LanguageEntity, SetItemsAdapter.MyViewHolder>(DiffCallback),
    TextToSpeech.OnInitListener {

    private var textToSpeech: TextToSpeech? = null

    init {
        textToSpeech = TextToSpeech(context, this)
    }

    inner class MyViewHolder(val binding: ItemBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = getItem(position)

        holder.binding.apply {
            sentence.text = item.wordOrSentence
            mean.text = item.meaning

            // tıklayınca seslendirme
            root.setOnClickListener {
                speakOut(item.wordOrSentence)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<LanguageEntity>() {
        override fun areItemsTheSame(oldItem: LanguageEntity, newItem: LanguageEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LanguageEntity, newItem: LanguageEntity): Boolean {
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
