package com.aciddroid.randomboobs;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;

public class SavedPhotosViewer extends Activity {

	
	/**
	 * Called when screen orientation is changed or keyboard is poped open/close
	 * Do nothing.
	 * */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  super.onConfigurationChanged(newConfig);
	  
	}
	

	/**
	 * Called when the activity is first created.
	 * */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.savedphotosviewer);

	}
}
