package com.aciddroid.randomboobs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aciddroid.randomboobs.feeds.FeedUtil;

public class RandomBoobsActivity extends Activity {


	private static final String APP_DIR = "randomBoobs";
	
	private static final int MAX_CACHE_SIZE = 500;

	/**Bytes used on this session*/
	private long session_bytes = 0;


	private boolean downloading = false;
	private boolean updating = false;

	private static ProgressBar progressBar;
	private static ProgressBar waitBar;

	private TextView bandwidth;

	private FeedUtil fu;
	private Bitmap currentBitmap = null;
	
	
	private Bitmap[] bitmapCache;

	//The image currently shown
	private int currentImage;


	public static ProgressBar getProgressBar() {
		return progressBar;
	}


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
		setContentView(R.layout.main);

		
		//GUI elements
		bandwidth = (TextView) this.findViewById(R.id.bandwidth);
		waitBar = (ProgressBar) this.findViewById(R.id.wait);
		progressBar = (ProgressBar) this.findViewById(R.id.progress);
		progressBar.setVisibility(View.VISIBLE);

		//Get content
		getFeeds();

		//Previous image and its action
		View previous = findViewById(R.id.clickprev);
		previous.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (currentImage > 0) {
					currentImage--;
					setBitmapImage(bitmapCache[currentImage]);
				}
			}
		});
		
		
		//Save image and its action
		ImageView saveIcon = (ImageView) findViewById(R.id.save);
		saveIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				saveImage();				
			}
		});

		//Share image and its action
		ImageView shareIcon = (ImageView) findViewById(R.id.share);
		shareIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				shareImage();	
			}
		});

		//Refresh image and its action
		ImageView refreshIcon = (ImageView) findViewById(R.id.refresh);
		refreshIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				if (!updating) {
					updating = true;
					progressBar.setVisibility(View.VISIBLE);
					getFeeds();
				}
			}
		});
		
		
		//View Downloads
		ImageView downloadsIcon = (ImageView) findViewById(R.id.downloads);
		downloadsIcon.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(RandomBoobsActivity.this,SavedPhotosViewer.class);
				startActivity(i);
			}
		});

	}






	/**
	 * Download feeds and set the fu variable
	 * */
	private void getFeeds() {


		//Init cache
		bitmapCache = new Bitmap[MAX_CACHE_SIZE];
		
		Handler h = new Handler() {

			@Override
			public void handleMessage(Message msg) {

				if (msg.what == FeedUtil.MESSAGE_FINISHED){
					Log.v("TEST", "OK");
					progressBar.setVisibility(View.GONE);
					startBrowsing();
					updating = false;
				}

			}
		};

		fu = new FeedUtil(h,this);		
	}



	/**
	 * Begin the browsing experience
	 * */
	private void startBrowsing() {

		new SetImage().execute(fu.getImages().get(0));

		currentImage = 0;

		//When clicking on the image
		View next = findViewById(R.id.clicknext);

		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (!downloading && !updating) {

					downloading = true;

					int max = 0;

					//Aggressive clicking
					if (fu != null)
						max = fu.getImages().size()-1;

					//Reset
					if (currentImage >= max) {
						progressBar.setVisibility(View.VISIBLE);
						getFeeds();
					}
					else {
						waitBar.setVisibility(View.VISIBLE);
						Log.v("TEST", "Displaying: "+(currentImage+1)+"/"+max);

						currentImage++;
						
						//Cache
						if (bitmapCache[currentImage] == null)
							new SetImage().execute(fu.getImages().get(currentImage));
						else {
							setBitmapImage(bitmapCache[currentImage]);
							downloading = false;
						}
						
					}								
				}
			}

		});

		Toast.makeText(this, getString(R.string.click_image), 1000).show();
	}



	/**
	 * Set the image to the given Bitmap
	 * */
	private void setBitmapImage(Bitmap bm) {

		if (bm != null) {		
			ImageView i = (ImageView)findViewById(R.id.content_image);
			i.setImageBitmap(bm);

			currentBitmap = bm;

			Log.v("TEST", "Image set");
		}
		else
			Log.v("TEST", "Image is null");


		//Cache
		bitmapCache[currentImage] = bm;
		
		//BW check
		String form = new DecimalFormat("#.##").format(((double)session_bytes/1024.0/1024.0));
		bandwidth.setText(form+" "+getString(R.string.mb));
		
		//GUI
		waitBar.setVisibility(View.GONE);
	}



	/**
	 * Saves image to location
	 * */
	private void saveImage() {


		if (currentBitmap != null) {
			Format formatter;
			Date date = new Date();

			ByteArrayOutputStream bytes = new ByteArrayOutputStream();		
			currentBitmap.compress(Bitmap.CompressFormat.JPEG, 98, bytes);

			formatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
			String time = formatter.format(date);

			String path		= Environment.getExternalStorageDirectory()+"/"+APP_DIR+"/";
			String filename = time+".jpg";
			String dest		= path+filename;									

			File f		= new File(dest);
			File dir	= new File(path);

			dir.mkdirs();

			try {

				f.createNewFile();
				FileOutputStream fo = null;
				fo = new FileOutputStream(f);
				fo.write(bytes.toByteArray());

			} catch (Exception e) {
				e.printStackTrace();
			}

			Log.v("TEST", "Saved: "+dest);
			Toast t = Toast.makeText(this, getString(R.string.image_saved)+dest, 1000);
			t.show();
		}

	}

	
	/**
	 * Shares an image on the selected media
	 * */
	private void shareImage(){
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");

		//shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Some text");
		
		String text = getResources().getString(R.string.share_image_text1) + " " +  
						getResources().getString(R.string.app_name) + " " +
						getResources().getString(R.string.share_image_text2) + " " +
						"\n\n" + fu.getImages().get(currentImage);
						
		
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, text);
		startActivity(Intent.createChooser(shareIntent, getResources().getString(R.string.share_image_title)));
		
	}
	

	/**
	 * Gets the image filesize without downloading it
	 * */
	private int getFilesize(String file_url) {

		int file_size = 0;

		try {

			URL url = new URL(file_url);
			URLConnection urlConnection = url.openConnection();
			urlConnection.connect();
			file_size = urlConnection.getContentLength();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return file_size;

	}




	/**
	 * Class that downlaods images in an asyntask and sets them through setBitmapImage
	 * */
	private class SetImage extends AsyncTask<String, Void, Bitmap> {


		protected Bitmap doInBackground(String... url) {

			Bitmap bm = null;

			try {
 
				session_bytes += getFilesize(url[0]);
				Log.v("TEST", "Trying to download: "+url[0]);
				bm = BitmapFactory.decodeStream((InputStream)new URL(url[0]).getContent());

			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			return bm;

		}

		protected void onPostExecute(Bitmap result) {
			downloading = false;
			setBitmapImage(result);	    	
		}

	}


}