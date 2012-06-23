package com.aciddroid.randomboobs.utils.mobclixListener;

import android.util.Log;
import android.view.View;

import com.mobclix.android.sdk.MobclixAdView;
import com.mobclix.android.sdk.MobclixAdViewListener;
import com.mobclix.android.sdk.MobclixFullScreenAdView;
import com.mobclix.android.sdk.MobclixFullScreenAdViewListener;


public class MobclixListener implements MobclixAdViewListener, MobclixFullScreenAdViewListener{

	@Override
	public void onDismissAd(MobclixFullScreenAdView arg0) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
	public void onFailedLoad(MobclixFullScreenAdView arg0, int arg1) {
		
	}

	@Override
	public void onFinishLoad(MobclixFullScreenAdView arg0) {
		
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
	public void onAdClick(MobclixAdView arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCustomAdTouchThrough(MobclixAdView arg0, String arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFailedLoad(MobclixAdView AdView, int errorValue) {
		if (errorValue == -501){
			Log.d("Ads","Anuncio no disponible volviendo a pedir");
			AdView.setVisibility(View.GONE);
			AdView.getAd();
			return;
		}
		AdView.setVisibility(View.GONE);
	}

	@Override
	public boolean onOpenAllocationLoad(MobclixAdView arg0, int arg1) {
		return false;
	}

	@Override
	public void onSuccessfulLoad(MobclixAdView AdView) {
		Log.d("Ads","Anuncio obtenido MobClix");
		AdView.setVisibility(View.VISIBLE);
		
	}

	@Override
	public String query() {
		// TODO Auto-generated method stub
		return null;
	}

}
