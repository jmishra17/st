package com.jasfiddle.spacetrooper.main;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class StatsBar {
	public static final int EASY_MODE = 0, MED_MODE = 1, HARD_MODE = 2, FULL_HEALTH_LENGTH = 200;
	public int maxAllowableHits;
	public int  healthReductionFactor;
	private int health_holder_left, health_holder_top, health_holder_right, health_holder_bottom;
	public static int level = 0;
	public Rect box;
	public Matrix matrix;
	public Paint p;
	public Rect healthHolder, healthSize, reducedHealth;
	public int hitCount;
	public int score;
	
	public StatsBar(int maxAllowableHits){
		this.maxAllowableHits = maxAllowableHits;
		this.healthReductionFactor = getHealthReductionFactor();
		box = new Rect();
		healthHolder = new Rect();
		healthSize = new Rect();
		reducedHealth = new Rect();
		matrix= new Matrix();
		p = new Paint();
		hitCount = 0;
		score = 0;
		
	}
	
	public int getHealthReductionFactor(){
		return (int) FULL_HEALTH_LENGTH/maxAllowableHits;
	
	}
	
	public void setHealthHolderRect(int left, int top, int right, int bottom){
		health_holder_left = left;
		health_holder_top = top;
		health_holder_right = right;
		health_holder_bottom = bottom;
		
		
	}
	
	
	public void setDifficultyLevel(int mode){
		switch(mode){
		case EASY_MODE: {
			this.maxAllowableHits  = 8;
			break;
		}
		
		case MED_MODE: {
			this.maxAllowableHits = 5;
			break;
		}
		
		case HARD_MODE: {
			this.maxAllowableHits = 3;
			break;
			}
		default:{
			this.maxAllowableHits = 5;
			break;
		}
	  }
	this.maxAllowableHits = this.getHealthReductionFactor();
	}
	
	public void  drawScore(Canvas c){
		p.reset();
		p.setColor(Color.GREEN);
		p.setTextSize(60);
		c.drawText(this.score+"",c.getWidth() -100 , 100, p);
	
	}
	
	public void drawHealthBar(Canvas c){
		p.reset();
		p.setColor(Color.RED);
		healthHolder.set(health_holder_left, health_holder_top, health_holder_right, health_holder_bottom);
		c.drawRect(healthHolder, p);
		p.setColor(Color.GREEN);
		healthSize.set(health_holder_left + 5, health_holder_top +5, health_holder_right -5, health_holder_bottom - 5);
		c.drawRect(healthSize, p);
			drawReducdeHealth(c);
		
	}
	
	public void drawGameOver(Canvas c){
		p.reset();
		p.setColor(Color.rgb(190, 190, 190));
		p.setTextSize(60);
		c.drawText("GAME OVER", c.getWidth()/2 - 175, c.getHeight()/2, p);
	}
	
	private void drawReducdeHealth(Canvas c){
		p.setColor(Color.BLACK);
		if(hitCount<=this.maxAllowableHits)
			reducedHealth.set(healthSize.right - (this.hitCount * (healthReductionFactor)),
							  healthSize.top, 
							  healthSize.right, 
							  healthSize.bottom);
		else
			reducedHealth.set(healthSize.left, healthSize.top, healthSize.right, healthSize.bottom);
		c.drawRect(reducedHealth, p);
	}
	
	
	
	
}
