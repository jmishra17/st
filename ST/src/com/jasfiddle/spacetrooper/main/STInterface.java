package com.jasfiddle.spacetrooper.main;

import java.util.Random;

import android.R.color;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class STInterface {
	public  boolean isFlashingLevel;
	public  long levelFlashStartTime;
	private long LEVEL_FLASH_DURATION = 2000;
	public static long MIN_PLAY_TIME = 20000, MAX_PLAY_TIME = 40000;
	public long levelPlayTime;
	public long levelPlayStartTime;
	private int level = 1;
	private Random r;
	public boolean levelComplete;
	private Paint p;

	public STInterface(){	
		r = new Random();
		levelPlayTime = MIN_PLAY_TIME + (long)((r.nextFloat()) * (MAX_PLAY_TIME - MIN_PLAY_TIME + 1)); 
		levelComplete = false;
		p = new Paint();
	}
	
	public void setLevelParams(int level){
		levelFlashStartTime = System.currentTimeMillis();
		isFlashingLevel = true;
		levelPlayStartTime = System.currentTimeMillis();
		this.level = level;
		
		
	}
		
	public boolean levelPlayTimeIsUp(long gamePauseDuration){
		long diff = System.currentTimeMillis() - levelPlayStartTime - (gamePauseDuration);
		if(diff >= levelPlayTime)
			return true;
		return false;
		
	}

	public void drawLevelIndicator(Canvas c, long gamePauseDuration){
		long diff = System.currentTimeMillis() - levelFlashStartTime - (gamePauseDuration);
		if(diff<= LEVEL_FLASH_DURATION){
				p.reset();
				p.setColor(Color.rgb(0, 255, 0));
				p.setTextSize(60);
				c.drawText("LEVEL "+level, c.getWidth()/1.5f - 175, c.getHeight()/2, p);
				p.reset();
				p.setColor(Color.rgb(255, 255, 255));
				p.setTextSize(30);
				c.drawText("HOLD AND DRAG THE TROOPER", c.getWidth()/1.5f-300, c.getHeight()/2 + 100, p);
				c.drawText("WITH ONE FINGER AND", c.getWidth()/1.5f-250, c.getHeight()/2 + 150, p);
				c.drawText("SHOOT WITH ANOTHER", c.getWidth()/1.5f-250, c.getHeight()/2 + 200, p);
		}
		else {
			isFlashingLevel = false;
			levelFlashStartTime = 0;				
		}
	}
	
	public void drawGamePaused(Canvas c){
		p.reset();
		p.setColor(Color.rgb(190, 190, 190));
		p.setTextSize(60);
		c.drawText("GAME PAUSED", c.getWidth()/2 - 200, c.getHeight()/2, p);
		
	}
	
	public void drawGameComplete(Canvas c){
		p.reset();
		p.setColor(Color.rgb(255, 255, 255));
		p.setTextSize(30);
		c.drawText("THANK YOU FOR PLAYING ", c.getWidth()/1.5f-250, c.getHeight()/2 - 100, p);
		c.drawText("SPACETROOPER. FEEL FREE ", c.getWidth()/1.5f-250, c.getHeight()/2 - 50, p);
		c.drawText("TO POST YOUR FEEDBACKS ", c.getWidth()/1.5f-250, c.getHeight()/2 , p);
		c.drawText("ON GOOGLE PLAY", c.getWidth()/1.5f-250 , c.getHeight()/2 + 50 , p);
	}

}
