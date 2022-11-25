package com.nvt.color.demo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//
//        btnShowColorPicker.setOnClickListener {
//            val dialog = ColorPickerDialog(
//                this,
//                Color.BLACK,
//                true,
//                object : ColorPickerDialog.OnColorPickerListener {
//                    override fun onCancel(dialog: ColorPickerDialog?) {
//                        // none
//                    }
//                    override fun onOk(dialog: ColorPickerDialog?, color: Int) {
//                        txtColor.setTextColor(color)
//                    }
//
//                })
//            dialog.show()
//        }
    }
}