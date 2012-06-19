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
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.aciddroid.randomboobs.feeds.FeedUtil;
import com.flurry.android.FlurryAgent;

public class RandomBoobsActivity extends Activity {


	private static final String APP_DIR = "randomBoobs";
	public static final String DOWNLOAD_DIR = Environment.getExternalStorageDirectory()+"/"+APP_DIR+"/";
	public static final String FLURRY_APP_ID = "53GH9D9MB8KMXJDSDNZT";

	private static final int MAX_CACHE_SIZE = 500;

	/**Bytes used on this session*/
	private long session_bytes = 0;

	private boolean downloading = false;
	private boolean updating = false;
	private boolean startBrowsing = false;
	private boolean messageShowed = false;
	
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
		setContentView(R.layout.main);
		continueBrowsing();	  
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, FLURRY_APP_ID);
		FlurryAgent.setReportLocation(true);
		FlurryAgent.logEvent("PantPrincipal", true);		
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
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
	}


	/**
	 * Download feeds and set the fu variable
	 * */
	private void getFeeds() {
		updating = true;

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
					messageShowed = false; // flag to use when the user click a lot of times to show him a message only one time
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

		//Put visible the next/previous arrows
		((ImageView)findViewById(R.id.next)).setVisibility(View.VISIBLE);
		((ImageView)findViewById(R.id.previous)).setVisibility(View.VISIBLE);

		//Mark that we are in browsing mode
		startBrowsing = true;
	}

	
	/**
	 * Begin the browsing experience
	 * */
	private void continueBrowsing() {

		setBitmapImage(bitmapCache[currentImage]);

		//Put visible the next/previous arrows
		((ImageView)findViewById(R.id.next)).setVisibility(View.VISIBLE);
		((ImageView)findViewById(R.id.previous)).setVisibility(View.VISIBLE);

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

			String path		= DOWNLOAD_DIR;
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



	/*      ***                CLICKS METHODS         ***             */

	public void clickSaveImage(View v){
		saveImage();
	}

	public void clickShareImage(View v){
		shareImage();
	}

	public void clickDownload(View v){
		Intent i = new Intent(RandomBoobsActivity.this,SavedPhotosViewer.class);
		startActivity(i);
	}

	public void clickRefresh(View v){
		if (!updating) {
			updating = true;
			messageShowed = false;
			progressBar.setVisibility(View.VISIBLE);
			getFeeds();
			//Put visible the next/previous arrows
			((ImageView)findViewById(R.id.next)).setVisibility(View.GONE);
			((ImageView)findViewById(R.id.previous)).setVisibility(View.GONE);
		}
		else if (!messageShowed){
			Toast.makeText(this, getResources().getString(R.string.feeds_downloading), Toast.LENGTH_SHORT).show();
			messageShowed = true;
		}
	}

	/**
	 * Click method that is called when click on the previous image view
	 * 
	 */
	public void clickPreviousImage(View v){
		if (startBrowsing && currentImage > 0) {
			currentImage--;
			setBitmapImage(bitmapCache[currentImage]);
		}
	}

	/**
	 * Click method that is called when click on the next image view
	 * 
	 */
	public void clickNextImage(View v){
		if (startBrowsing && !downloading && !updating) {
			downloading = true;
			messageShowed = false; // flag to use when the user click a lot of times to show him a message only one time

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
		else if (downloading && !messageShowed){
			Toast.makeText(this, getResources().getString(R.string.image_downloading), Toast.LENGTH_SHORT).show();
			messageShowed = true;
		}
		else if (updating && !messageShowed){
			Toast.makeText(this, getResources().getString(R.string.feeds_downloading), Toast.LENGTH_SHORT).show();
			messageShowed = true;
		}
	}
}