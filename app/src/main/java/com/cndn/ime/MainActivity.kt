package com.cndn.ime

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            setContentView(R.layout.activity_main)

            findViewById<Button>(R.id.btnEnable).setOnClickListener {
                startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
            }

            findViewById<Button>(R.id.btnSwitch).setOnClickListener {
                val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showInputMethodPicker()
            }
        } catch (e: Throwable) {
            // Çökmek yerine hatayı ekrana yazdır
            val tv = TextView(this)
            tv.text = "HATA:\n${e.javaClass.name}\n${e.message}\n\n${e.stackTrace.take(10).joinToString("\n")}"
            tv.setTextColor(android.graphics.Color.WHITE)
            tv.textSize = 12f
            tv.setPadding(24, 24, 24, 24)
            setContentView(tv)
            Toast.makeText(this, "Hata yakalandı, ekrandaki yazıyı gönder", Toast.LENGTH_LONG).show()
        }
    }
}
