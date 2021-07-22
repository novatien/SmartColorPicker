# SmartColorPicker
<img src="https://i.imgur.com/PTdkWDT.png">

## Gradle
```gradle
allprojects{
	repositories{
		...
		maven { url 'https://jitpack.io' }
	}
}

dependencies{
    ...
    implementation 'com.github.novatien:SmartColorPicker:1.0.2'
}
 ```
 ## Use
 ```kotlin
    val colorPicker = ColorPickerDialog(
                        context,
                        Colors.BLACK, // color init
                        true, // true is show alpha
                        object : ColorPickerDialog.OnColorPickerListener {
                            override fun onCancel(dialog: ColorPickerDialog?) {
                                // handle click button Cancel
                            }

                            override fun onOk(dialog: ColorPickerDialog?, colorPicker: Int) {
                                // handle click button OK
                            }
                        })
     colorPicker.show()
 ```
