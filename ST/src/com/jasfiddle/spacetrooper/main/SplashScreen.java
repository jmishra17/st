package com.jasfiddle.spacetrooper.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import com.jasfiddle.spacetrooper.R;

public class SplashScreen extends Activity {
	
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		//remove title while splash displays
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
	
		//remove notification whiles splash displays
		this.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN
				);
		setContentView(R.layout.splashscreen);
		
		Thread screenTimer = new Thread(){
			public void run(){
				try{
					int screenTimer = 0;
					while(screenTimer < 2000){
						sleep(100);
						screenTimer+= 100;						
					}
					startActivity(new Intent("com.jasfiddle.spacetrooper.CLEARSCREEN"));
					
				}
				catch(Exception e){
					e.printStackTrace();
				}
				finally{
					finish();
				}
			}
		};
		screenTimer.start();
	}
}
