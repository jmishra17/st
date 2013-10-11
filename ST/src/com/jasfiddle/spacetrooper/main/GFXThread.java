package com.jasfiddle.spacetrooper.main;


import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

public class GFXThread extends Thread {
	private SurfaceHolder holder;
	private STSurface surface;
	private boolean running = false;
	private boolean isInitialized;
	public int level;
	
	
	public GFXThread(SurfaceHolder holder, STSurface sTSurface, boolean isInitialized){
		this.surface = sTSurface;
		this.holder = holder;
		this.isInitialized = isInitialized;
		level = 1;
	}
	
	public void setRunning(boolean run){
		this.running = run;
	}
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Canvas c;
		while(running){
			c = null;
			try {				
				c = holder.lockCanvas();
				synchronized(holder){	
					if(!isInitialized){
						surface.loadAllGameEssentials(c, this.level);
						isInitialized = true;
					}
						surface.draw(c);
				}
				
			}  finally {
				if(c!=null)
					holder.unlockCanvasAndPost(c);
			}
		}
			
	}


	
}
