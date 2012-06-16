package com.aciddroid.randomboobs.feeds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.aciddroid.randomboobs.RandomBoobsActivity;



public class FeedUtil {	
	
	
	private static String[] feed_list = {
		
		"http://definemotorsports.tumblr.com/rss",
		"http://eyegasmiccars.tumblr.com/rss",
		"http://fuckyeahclassiccars.tumblr.com/rss",
		"http://rollsroycemotorcars.tumblr.com/rss",
		"http://shelby4271965.tumblr.com/rss",
		"http://automotiveporn.tumblr.com/rss",
		"http://bennnnnnyo.tumblr.com/rss",
		"http://carpr0n.tumblr.com/rss",
		"http://turnerbudds-carblog.tumblr.com/rss",
		"http://carinteriors.tumblr.com/rss",
		"http://vintagecars.tumblr.com/rss",
		"http://citsandbugs.tumblr.com/rss",
		"http://mesmomeugenero.tumblr.com/rss",
		"http://wellisnthatnice.tumblr.com/rss",
		"http://crazyforcars.tumblr.com/rss",
		"http://hypercharged.tumblr.com/rss",
		"http://process-vision.tumblr.com/rss",
		"http://ferrari-world.tumblr.com/rss",
		"http://rwd-cars.tumblr.com/rss",
		"http://givemecars.tumblr.comrss",
		"http://imcarsforever.tumblr.com/rss",
		"http://thinkstance.tumblr.com/rss",
		"http://musclecardreaming.tumblr.com/rss",
		"http://fuckyeahsexytrucks.tumblr.com/rss",
		"http://carsonrecord.tumblr.com/rss",
		"http://goodoldvalves.tumblr.com/rss"
	}; 
	
	
	public static final int MESSAGE_FINISHED= 0;
	public static final int MESSAGE_ERROR= 1;
	
	
	private static final int MAX_FEEDS = 10;
	
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
	    	
	    	
	    	//We set a limit to avoid long init waiting times
	    	String[] feeds_list;
	    	if (elements >= MAX_FEEDS) {
	    		
	    		elements = MAX_FEEDS;
	    		
	    		feeds_list = new String[MAX_FEEDS];
	    		
	    		Random randGen = new Random();
	    		
	    		for (int i=0; i<MAX_FEEDS; i++) {
	    			
	    			int n = randGen.nextInt(MAX_FEEDS);
	    			
	    			feeds_list[i] = feeds[0][n];
	    		}
	    			    		
	    	}	    	
	    	//Not many :)
	    	else {
	    		
	    		feeds_list = new String[elements];
	    		for (int i=0; i<elements; i++) {
	    			
	    			feeds_list[i] = feeds[0][i];
	    		}	    		
	    	}
	    	
	    	
	    	
	    	for (int i=0; i<elements; i++) {
	    		
	    		//Download feed
	    		Log.v("TEST", "Downloading: "+feeds_list[i]);
	    		
	    		try {
					RSSFeed feed = reader.load(feeds_list[i]);
										
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

