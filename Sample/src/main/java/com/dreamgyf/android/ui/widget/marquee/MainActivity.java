package com.dreamgyf.android.ui.widget.marquee;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		TextView tv = findViewById(R.id.tv);
		tv.setTextSize(30);
	}
}