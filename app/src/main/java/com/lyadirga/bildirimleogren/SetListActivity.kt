package com.lyadirga.bildirimleogren

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity


class SetListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setlist)

        val intent = intent
        val action = intent.action
        val type = intent.type

        Toast.makeText(this, "dddd" + type, Toast.LENGTH_SHORT).show()
        if (Intent.ACTION_VIEW == action && type != null) {
            // Burada CSV dosyasını işleyin
            // Örneğin, dosyanın URI'sini alabilir ve işleyebilirsiniz.
            val uri = intent.data
            // Dosyayı açmak veya içeriğini okumak için işlemleri burada gerçekleştirin.
            println("LLL uri::: $uri")
        }
    }
}