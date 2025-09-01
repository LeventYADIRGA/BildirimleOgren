package com.lyadirga.bildirimleogren.ui

import android.content.Context
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import com.lyadirga.bildirimleogren.R
import com.lyadirga.bildirimleogren.model.Language
import java.util.Locale

class RecyclerAdapter(private val context: Context, private var dataList: List<Language>): RecyclerView.Adapter<RecyclerAdapter.MyViewHolder>(), TextToSpeech.OnInitListener{

    private var textToSpeech: TextToSpeech? = null

    init {
        // TextToSpeech başlatılıyor
        textToSpeech = TextToSpeech(context, this)
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val sentence: MaterialTextView = itemView.findViewById(R.id.sentence)
        val mean: MaterialTextView = itemView.findViewById(R.id.mean)
        val image: AppCompatImageView = itemView.findViewById(R.id.image)
    }

    // Adapter içindeki veri kümesinin uzunluğu
    override fun getItemCount(): Int {
        return dataList.size
    }

    // Yeni bir ViewHolder oluşturulduğunda çağrılır
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return MyViewHolder(view)
    }

    // ViewHolder ile belirli bir pozisyondaki veriyi bağlama
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.sentence.text = dataList[position].wordOrSentence
        holder.mean.text = dataList[position].meaning
        dataList[position].imageResId?.let {
            holder.image.setVisible()
            holder.image.setImageResource(it)
        } ?: run {
            holder.image.setGone()
        }

        holder.itemView.setOnClickListener {
            val sentenceToSpeak = dataList[position].wordOrSentence
            speakOut(sentenceToSpeak)
        }
    }

    fun swapData(newData: List<Language>) {
        // Yeni veri kümesini doğrudan atayarak güncelleme
        dataList = newData

        // Adaptöre veri setinin değiştiğini bildir
        notifyDataSetChanged()
    }

    // Metni seslendirmek için TextToSpeech kullanımı
    private fun speakOut(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // TextToSpeech başlatma işlemi tamamlandığında çağrılır
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Dil ayarını İngilizce (ABD) olarak ayarla
            val result = textToSpeech?.setLanguage(Locale.US)

            // Dil başarıyla ayarlandı mı kontrol et
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                // Dil desteklenmiyorsa veya eksikse, hata mesajı göster
                // Gerekirse ek işlemleri burada gerçekleştirebilirsiniz
                context.showToast(result.toString())
            }
        }
    }

    // Adaptör öğesi yok edildiğinde TextToSpeech'ı serbest bırak
    fun releaseTextToSpeech() {
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }
}