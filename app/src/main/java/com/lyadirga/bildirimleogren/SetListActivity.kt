package com.lyadirga.bildirimleogren

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity


class SetListActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        setContentView(R.layout.activity_setlist)

        val intent = intent
        val action = intent.action
        val type = intent.type

        if (Intent.ACTION_VIEW == action && type != null) {
            if ("text/csv" == type) {
                // Burada CSV dosyasını işleyin
                // Örneğin, dosyanın URI'sini alabilir ve işleyebilirsiniz.
                val uri = intent.data
                // Dosyayı açmak veya içeriğini okumak için işlemleri burada gerçekleştirin.
                println("LLL uri::: $uri")
            }
        }

    }
}