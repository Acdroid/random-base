package com.aciddroid.randomboobs.feeds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;

import com.aciddroid.randomboobs.RandomBoobsActivity;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;



public class FeedUtil {	
	
	
	private static String[] feed_list = {
		
		"http://definemotorsports.tumblr.com/rss",
		"http://eyegasmiccars.tumblr.com/",
		"http://fuckyeahclassiccars.tumblr.com/rss",
		"http://rollsroycemotorcars.tumblr.com/rss"		
	}; 
	
	
	public static final int MESSAGE_FINISHED= 0;
	public static final int MESSAGE_ERROR= 1;
	
	private Handler h;	
	
	
	private List<String> images;
	
	private void setImages(List<String> images) {
		this.images = images;
	}


	public List<String> getImages() {
		return images;
	}
	
	
	/*Inicia el proceso de descarga de feeds y avisa al handler una vez acabado*/
	public FeedUtil(Handler handler) {
		

		if (handler != null) {

			h = handler;
			
			//Sacamos todas las imagenes y las metemos en la var feeds
			new DownloadFeeds().execute(feed_list);
			
		}

		
	}
	
	

	private static String getImageFromDescription(String description) {
		
		
		Pattern p = Pattern.compile("^.*<img src=\"(.*[jJ][pP][gG])\".*");
		Matcher m = p.matcher(description);

		if (m.find()) {
		    return m.group(1);
		}

		return null;
		
	}


	private class DownloadFeeds extends AsyncTask<String[], Integer, List<String>> {
		
	    
	    protected List<String> doInBackground(String[]... feeds) {
	     
	    	//Aqui guardamos las imagenes
	    	List<String> l = new ArrayList<String>();	    	
	    	
	    	RSSReader reader = new RSSReader();
	    	
	    	int elements = feeds[0].length;
	    	
	    	for (int i=0; i<elements; i++) {
	    		
	    		//Download feed
	    		Log.v("TEST", "Downloading: "+feeds[0][i]);
	    		
	    		try {
					RSSFeed feed = reader.load(feeds[0][i]);
										
					List<RSSItem> rssItems = feed.getItems();
					
					Iterator<RSSItem> rssElem = rssItems.iterator();
					
					while(rssElem.hasNext()) {
						
						RSSItem item = rssElem.next();
						String description = item.getDescription();												
						String imageUrl = getImageFromDescription(description);
						
						if (imageUrl != null) {
							l.add(imageUrl);
							Log.v("TEST", imageUrl);
						}
						else
							Log.v("TEST", "Image extraction failed for: "+description);
					}
										
				} catch (Exception e) {
					Log.v("TEST", "Failed");
				}
				
				
				//Progreso
				int progress = (int) (((i+1) / (float) elements) * 100);
				publishProgress(progress);
				
	    	}	    		    	
	    	
	    	//Elementos aleatorizados
	    	Collections.shuffle(l);
	    	
	    	return l;
	    }
	    
	    
	    protected void onProgressUpdate(Integer... progress) {
	    	int v = progress[0].intValue();
	        Log.v("TEST", "Completed: "+v+"%");
	        RandomBoobsActivity.getProgressBar().setProgress(v);
	    }
	    

	    protected void onPostExecute(List<String> result) {
	        setImages(result);	        
	        h.sendEmptyMessage(MESSAGE_FINISHED);
	    }
	    
	    
	}
	
	
	
}

