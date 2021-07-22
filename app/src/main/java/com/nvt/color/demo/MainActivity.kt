package com.nvt.color.demo

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.nvt.color.ColorPickerDialog

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val dialog = ColorPickerDialog(
            this,
            Color.BLACK,
            true,
            object : ColorPickerDialog.OnColorPickerListener {
                override fun onCancel(dialog: ColorPickerDialog?) {

                }

                override fun onOk(dialog: ColorPickerDialog?, color: Int) {

                }

            })
        dialog.show()
    }
}