package com.aciddroid.randomboobs.utils.mobclixListener;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.mobclix.android.sdk.MobclixFullScreenAdView;
import com.mobclix.android.sdk.MobclixFullScreenAdViewListener;


public class MobclixFullScreenListener implements  MobclixFullScreenAdViewListener{

	private Handler handler = null;
	
	public MobclixFullScreenListener ( Handler han){
		handler = han;
	}
	@Override
	public void onDismissAd(MobclixFullScreenAdView arg0) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void onFailedLoad(MobclixFullScreenAdView ad, int errorValue) {
		Log.i("Ads","Ad Full Screen no disponible pidiendo otro. Error " + errorValue);
//		if (errorValue == -503){
//			ad.requestAd();
//			Log.i("Ads","Ad Full Screen pedido...");
//		}
		
	}

	@Override
	public void onFinishLoad(MobclixFullScreenAdView ad) {
		Log.d("Ads","Anuncio Full Screen obtenido MobClix");
		if (handler != null){
			Message m = new Message();
			m.obj = true;
			handler.sendMessage(m);
		}
		
		ad.displayRequestedAd();
		
	}

	@Override
	public void onPresentAd(MobclixFullScreenAdView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String keywords() {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public String query() {
		// TODO Auto-generated method stub
		return null;
	}

}
