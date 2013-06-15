package com.noisepages.nettoyeur.openslstreamsample;

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

	private static interface LowpassFactory {
		Lowpass createLowpass() throws IOException;
	}

	// This factory illustrates how to query OpenSL config parameters on Jelly Bean MR1 while maintaining
	// backward compatibility with older versions of Android. The trick is to place the new API calls in
	// a class that will only be loaded if we're running on JB MR1 or later.
	private final LowpassFactory factory = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) ?
			new JellyBeanMr1LowpassFactory() : new DefaultLowpassFactory();

	// Factory implementation for Jelly Bean MR1 or later. Note that this class cannot be static because
	// the lookup of OpenSL parameters requires the context provided by the enclosing activity.
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	private class JellyBeanMr1LowpassFactory implements LowpassFactory {
		@Override
		public Lowpass createLowpass() throws IOException {
			AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			// Provide default values in case config lookup fails.
			int sr = 44100;
			int bs = 64;
			try {
				// If possible, query the native sample rate and buffer size.
				sr = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE));
				bs = Integer.parseInt(am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
			} catch (NumberFormatException e) {
				Log.w(getClass().getName(), "Failed to read native OpenSL config: " + e);
			}
			return new Lowpass(sr, bs);
		}
	};
	
	// Default factory for Jelly Bean or older.
	private static class DefaultLowpassFactory implements LowpassFactory {
		@Override
		public Lowpass createLowpass() throws IOException {
			// If the native sample rate and buffer size are not known, CD sample rate and 64 frames per
			// buffer are a reasonable default.
			return new Lowpass(44100, 64);
		}
	};
	
	private Lowpass lowpass;
	private SeekBar filterBar;
	private Switch playSwitch;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		filterBar = (SeekBar) findViewById(R.id.filterBar);
		filterBar.setOnSeekBarChangeListener(this);
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
			lowpass = factory.createLowpass();
			lowpass.setAlpha(filterBar.getProgress());
			if (playSwitch.isChecked()) {
				lowpass.start();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		lowpass.close();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (isChecked) {
			try {
				lowpass.start();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			lowpass.stop();
		}
	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
		lowpass.setAlpha(progress);
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
