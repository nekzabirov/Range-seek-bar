# Range seek bar android view

Seek bar with two btn to range select value for example price filter
![enter image description here](https://github.com/nikitaknyzevskiy/Range-seek-bar/blob/master/screenshot/Screenshot_1581541329.png?raw=true)

# Install

    	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

Module

    	dependencies {
	        implementation 'com.github.nikitaknyzevskiy:Range-seek-bar:1.0'
	}


## Uses

    <com.nikita.rangeseekbarlib.RangeSeekBar  
      android:layout_width="match_parent"  
      android:layout_height="wrap_content"  
      app:btn_color="@color/colorPrimary"  
      app:layout_constraintBottom_toBottomOf="parent"  
      app:layout_constraintLeft_toLeftOf="parent"  
      app:layout_constraintRight_toRightOf="parent"  
      app:layout_constraintTop_toTopOf="parent"  
      app:line_color="@android:color/darker_gray"  
      app:max="8"  
      app:min="1"  
      app:selected_color="@color/colorPrimary"  
      app:step="0.5"  
      app:text_color="@color/colorPrimary" />
