package com.noisepages.nettoyeur.openslstreamsample;

import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;

/**
 * Main activity for OpenSL sample app. This class is essentially boilerplate; most of the
 * interesting bits are in {@link OpenSlParams} and {@link Lowpass}.
 */
public class MainActivity extends Activity
    implements
      OnCheckedChangeListener,
      OnSeekBarChangeListener {

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
      OpenSlParams params = OpenSlParams.createInstance(this);
      lowpass = new Lowpass(params.getSampleRate(), params.getBufferSize());
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
