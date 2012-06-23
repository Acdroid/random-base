package com.aciddroid.randomboobs;

import java.io.File;

import java.util.ArrayList;
import java.util.ListIterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.aciddroid.randomboobs.utils.mobclixListener.MobclixListener;
import com.flurry.android.FlurryAgent;
import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixMMABannerXLAdView;

public class SavedPhotosViewer extends Activity {


	private GridView gridView;
	private ArrayList<String> fileList = new ArrayList<String>();
	private RelativeLayout layoutImageFull;

	private Boolean isImageFullScreen = false;
	private int currentImage = 0;
	private Activity activity;
	
	//Publicidad
	public MobclixAdView adView;


	/**
	 * Called when screen orientation is changed or keyboard is poped open/close
	 * Do nothing.
	 * */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);

	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, RandomBoobsActivity.FLURRY_APP_ID);
		FlurryAgent.setReportLocation(true);
		FlurryAgent.logEvent("Downloaded", true);	
	}

	/**
	 * Method onStop.
	 */
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
		setContentView(R.layout.savedphotosviewer);

		activity = this;

		Toast.makeText(this, getResources().getString(R.string.longclicktoremove), Toast.LENGTH_LONG).show();


		layoutImageFull = (RelativeLayout)findViewById(R.id.layout_full_screen);

		gridView = (GridView)findViewById(R.id.gridview);
		
		//Publicidad
		adView = (MobclixMMABannerXLAdView)findViewById(R.id.mobclix_publicidad);
		//adView.setTestMode(true);
		adView.addMobclixAdViewListener(new MobclixListener());
		
		loadGrid();

	}

	/**
	 * Get the list of images from the donwload directory
	 */
	private void loadGrid(){

		//Get the list of images
		File root = new File(RandomBoobsActivity.DOWNLOAD_DIR);
		File[] files = root.listFiles();

		//if no files, put the no images message
		if (files == null || files.length == 0){
			((TextView)findViewById(R.id.grid_noimagestext)).setVisibility(View.VISIBLE);
			gridView.setVisibility(View.GONE);
			return;
		}

		fileList.clear();
		ArrayList<String> aux = new ArrayList<String>();
		for (File file : files){
			aux.add(file.getPath());  
		}

		//Damos la vuelta a la lista para pintar las imágenes de nuevas a viejas
		if (aux.size() > 0){
			ListIterator<String> iter = aux.listIterator(aux.size());
			while (iter.hasPrevious()){
				fileList.add(iter.previous());
			}
		}

		//set Adapter to the grid
		gridView.setAdapter(new GridAdapter(this));
		
		//Click on a item of the gridview
		gridView.setOnItemClickListener(new OnItemClickListener(){


			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				expandImageFullScreen(position);


			}

		});

		gridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
					final int position, long arg3) {

				AlertDialog.Builder builder = new AlertDialog.Builder(activity);
				builder.setMessage(activity.getResources().getString(R.string.suretodelete))
				.setCancelable(false)
				.setPositiveButton(activity.getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						deleteImage(position);
						dialog.cancel();
					}
				})
				.setNegativeButton(activity.getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
				AlertDialog alert = builder.create();
				alert.show();

				return true;
			}
		});
	}


	public void deleteImage(int position){
		if (fileList != null && fileList.size() > position){
			File file = new File(fileList.get(position));
			boolean deleted = file.delete();

			loadGrid();
		}
	}

	public void clickShareImage(View v){
		String path = (String)fileList.get(currentImage);
		if (path == null || path.equals(""))
			return;
		
		Intent share = new Intent(Intent.ACTION_SEND);
		share.setType("image/jpeg");
		
		share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + path));
		startActivity(Intent.createChooser(share, getResources().getString(R.string.share_image_jpg)));
	}
	
	/**
	 * Expand the image to full screen
	 * @param position of the image in the filesList
	 */
	private void expandImageFullScreen(int position){

		if (loadImage(position)){
			isImageFullScreen = true;
			currentImage = position;
			Animation anim = AnimationUtils.loadAnimation(SavedPhotosViewer.this.getApplicationContext(), R.anim.scale_tobig);
			layoutImageFull.setAnimation(anim);
			layoutImageFull.setVisibility(View.VISIBLE);
		}
		else{
			Toast.makeText(this, getResources().getString(R.string.failwhileloading), Toast.LENGTH_LONG).show();
		}


	}

	/**
	 * Load a image from the sdcard and put it on the Image_full_screen imageview
	 * @param position
	 * @return
	 */
	private boolean loadImage(int position){
		if (fileList == null || fileList.size() < position)
			return false;

		//Load the image
		String path = (String)fileList.get(position);
		if (path != null && !path.equals("")){
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			if (bitmap == null)
				return false;
			ImageView myImageView = (ImageView)findViewById(R.id.image_full_screen);
			myImageView.setImageBitmap(bitmap);
		}
		else
			return false;


		return true;
	}
	
	/**
	 * Load a image from the sdcard and put it on the Image_full_screen imageview
	 * @param position
	 * @return
	 */
	private Bitmap getImage(int position){
		if (fileList == null || fileList.size() < position)
			return null;

		//Load the image
		String path = (String)fileList.get(position);
		if (path != null && !path.equals("")){
			Bitmap bitmap = BitmapFactory.decodeFile(path);
			return bitmap;
		}
		
		return null;
	}

	public void clickNext(View v){
		if (currentImage == fileList.size()-1)
			currentImage = 0;
		else
			currentImage ++;
		loadImage(currentImage);

	}

	public void clickPrev(View v){
		if (currentImage == 0)
			currentImage = fileList.size()-1;
		else
			currentImage --;
		loadImage(currentImage);
	}



	/**
	 * Adapter for a GridView that load images from a directory
	 * @author marcos
	 *
	 */
	public class GridAdapter extends BaseAdapter{

		private LayoutInflater mInflater;

		public GridAdapter(Context context){
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		}
		@Override
		public int getCount() {
			return fileList.size();
		}

		@Override
		public Object getItem(int index) {
			return fileList.get(index);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.gridview_item, null);
			}

			String path = (String)getItem(position);
			
			if (path != null && !path.equals("")){
				Bitmap bitmap = BitmapFactory.decodeFile(path);
				if (bitmap == null ) Log.d("DEBUG","Bitmap return " + bitmap);

				ImageView myImageView = (ImageView)convertView.findViewById(R.id.grid_image);
				myImageView.setImageBitmap(bitmap);
			}
			return convertView;
		}



	}


	/****************************************    BACK KEY CONTROL    *********************************************/
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isImageFullScreen){
				Animation anim = AnimationUtils.loadAnimation(SavedPhotosViewer.this.getApplicationContext(), R.anim.scale_tosmall);
				layoutImageFull.setAnimation(anim);
				layoutImageFull.setVisibility(View.GONE);
				isImageFullScreen = false;
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}
}
