package com.jasfiddle.spacetrooper.main;


import com.jasfiddle.spacetrooper.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;

@SuppressLint("NewApi")
public class GFXActivity extends Activity implements OnTouchListener{
	private STSurface mSurface; 
	private float x,y;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Display display = getWindowManager().getDefaultDisplay();
		final Point size = new Point();
		try {
	        display.getSize(size);
	    } catch (java.lang.NoSuchMethodError ignore) { // Older device
	        size.x = display.getWidth();
	        size.y = display.getHeight();
	    }
		float windowWidth = (float) size.x;
		float windowHeight = (float) size.y;
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mSurface = new STSurface(this, windowWidth, windowHeight);
		mSurface.setOnTouchListener(this);
		x=y=0;
		setContentView(mSurface);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}


	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	public boolean onTouch(View arg0, MotionEvent event) {
		// TODO Auto-generated method stub
		x = event.getX();
		y = event.getY();
		return false;
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {	
        getMenuInflater().inflate(R.menu.activity_main, menu);
        Log.i("onCreateOptionsMenu","I am about to go inside paused method");
        return super.onCreateOptionsMenu(menu);
    }
	                                                 


	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		mSurface.setThreadGamePaused(false);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch(item.getItemId()){
		case R.id.aboutUs: {
				Intent i = new Intent("com.jasfiddle.spacetrooper.ui.ABOUT");
				startActivity(i);
				break;
			}
		case R.id.resume: {
				mSurface.setThreadGameResume(true);
				break;
		}
		case R.id.objective: {
			Intent i = new Intent("com.jasfiddle.spacetrooper.ui.OBJECTIVE");
			startActivity(i);
			break;
		}
		case R.id.exit: {
			finish();
			break;
		}
	  }
		return true;
	}


}
