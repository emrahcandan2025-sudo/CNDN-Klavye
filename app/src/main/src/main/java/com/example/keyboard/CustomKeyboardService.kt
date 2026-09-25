package com.example.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.widget.Button

class CustomKeyboardService : InputMethodService() {

    override fun onCreateInputView(): View {
        val keyboardView = layoutInflater.inflate(R.layout.keyboard_view, null)

        // Tuş Id'leri ve Karakter Eşleşmeleri
        val keyMap = mapOf(
            // Rakamlar
            R.id.btn_1 to "1", R.id.btn_2 to "2", R.id.btn_3 to "3", R.id.btn_4 to "4", R.id.btn_5 to "5",
            R.id.btn_6 to "6", R.id.btn_7 to "7", R.id.btn_8 to "8", R.id.btn_9 to "9", R.id.btn_0 to "0",

            // 1. Satır
            R.id.btn_q to "q", R.id.btn_w to "w", R.id.btn_e to "e", R.id.btn_r to "r", R.id.btn_t to "t",
            R.id.btn_y to "y", R.id.btn_u to "u", R.id.btn_i_up to "i", R.id.btn_o to "o", R.id.btn_p to "p",
            R.id.btn_g_soft to "ğ", R.id.btn_u_dot to "ü",

            // 2. Satır
            R.id.btn_a to "a", R.id.btn_s to "s", R.id.btn_d to "d", R.id.btn_f to "f", R.id.btn_g to "g",
            R.id.btn_h to "h", R.id.btn_j to "j", R.id.btn_k to "k", R.id.btn_l to "l", R.id.btn_s_dot to "ş",
            R.id.btn_i to "ı",

            // 3. Satır
            R.id.btn_z to "z", R.id.btn_x to "x", R.id.btn_c to "c", R.id.btn_v to "v", R.id.btn_b to "b",
            R.id.btn_n to "n", R.id.btn_m to "m", R.id.btn_o_dot to "ö", R.id.btn_c_dot to "ç",

            // Alt Satır
            R.id.btn_dot to "."
        )

        // Karakter tuşlarını bağlama
        for ((id, character) in keyMap) {
            keyboardView.findViewById<Button>(id)?.setOnClickListener {
                currentInputConnection?.commitText(character, 1)
            }
        }

        // Boşluk (Space) Tuşu
        keyboardView.findViewById<Button>(R.id.btn_space)?.setOnClickListener {
            currentInputConnection?.commitText(" ", 1)
        }

        // Silme (Backspace) Tuşu
        keyboardView.findViewById<Button>(R.id.btn_backspace)?.setOnClickListener {
            currentInputConnection?.deleteSurroundingText(1, 0)
        }

        // Enter Tuşu
        keyboardView.findViewById<Button>(R.id.btn_enter)?.setOnClickListener {
            currentInputConnection?.sendKeyEvent(
                android.view.KeyEvent(android.view.KeyEvent.ACTION_DOWN, android.view.KeyEvent.KEYCODE_ENTER)
            )
        }

        return keyboardView
    }
}
