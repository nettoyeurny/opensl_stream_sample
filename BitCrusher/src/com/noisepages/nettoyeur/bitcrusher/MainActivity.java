package com.noisepages.nettoyeur.bitcrusher;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;

public class MainActivity extends Activity implements OnCheckedChangeListener, OnSeekBarChangeListener {

	private static interface BitCrusherFactory {
		OpenSlBitCrusher createBitCrusher() throws IOException;
	}

	// This factory illustrates how to query OpenSL config parameters on Jelly Bean MR1 while maintaining
	// backward compatibility with older versions of Android. The trick is to place the new API calls in
	// a class that will only be loaded if we're running on JB MR1 or later.
	private final BitCrusherFactory factory = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) ?
			new BitCrusherFactory() {
		@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
		@Override
		public OpenSlBitCrusher createBitCrusher() throws IOException {
			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			// Provide default values in case config lookup fails.
			int sr = 44100;
			int bs = 64;
			try {
				// If possible, query the native sample rate and buffer size.
				sr = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
				bs = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
			} catch (NumberFormatException e) {
				Log.w("BitCrusher", "Failed to read native OpenSL config: " + e);
			}
			return new OpenSlBitCrusher(sr, bs);
		}
	} : new BitCrusherFactory() {
		@Override
		public OpenSlBitCrusher createBitCrusher() throws IOException {
			// If the native sample rate and buffer size are not known, CD sample rate and 64 frames per
			// buffer are a reasonable default.
			return new OpenSlBitCrusher(44100, 64);
		}
	};

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
			bitCrusher = factory.createBitCrusher();
			setCrush(crushBar.getProgress());
			if (playSwitch.isChecked()) {
				bitCrusher.start();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
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

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Do nothing.
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Do nothing.
	}

	private void setCrush(int depth) {
		bitCrusher.crush(depth * 16 / 101);
	}
}
