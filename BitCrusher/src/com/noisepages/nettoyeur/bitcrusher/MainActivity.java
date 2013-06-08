package com.noisepages.nettoyeur.bitcrusher;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;

public class MainActivity extends Activity implements OnCheckedChangeListener, OnSeekBarChangeListener {

	private OpenSlBitCrusher bitCrusher;
	private SeekBar crushBar;
	private Switch playSwitch;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		crushBar = (SeekBar) findViewById(R.id.crushBar);
		crushBar.setOnSeekBarChangeListener(this);
		playSwitch = (Switch) findViewById(R.id.playSwitch);
		playSwitch.setOnCheckedChangeListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onStart() {
		super.onStart();
		try {
			if (Build.VERSION.SDK_INT >= 17) {
				createBitCrusher();
			} else {
				createBitCrusherDefault();
			}
			setCrush(crushBar.getProgress());
			if (playSwitch.isChecked()) {
				bitCrusher.start();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	// This method will choose the recommended configuration for OpenSL on JB MR1 and later.
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private OpenSlBitCrusher createBitCrusher() throws IOException {
		// Detect native sample rate and buffer size. If at all possible, OpenSL should use
		// these values.
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		int sr = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
		int bs = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
		return new OpenSlBitCrusher(sr, bs);
	}
	
	// This method will choose a reasonable default on older devices, i.e., CD sample rate and
	// 64 frames per buffer.
	private OpenSlBitCrusher createBitCrusherDefault() throws IOException {
		return new OpenSlBitCrusher(44100, 64);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		bitCrusher.close();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			try {
				bitCrusher.start();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			bitCrusher.stop();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		setCrush(progress);
	}

	private void setCrush(int depth) {
		bitCrusher.crush(depth * 16 / 101);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Do nothing.
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Do nothing.
	}
}
