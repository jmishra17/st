package com.jasfiddle.spacetrooper.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;

import com.jasfiddle.spacetrooper.R;

public class Boss  {
	 private static final int RAND_BOUND_FOR_SHOOT = 50,
				 RAND_NUM_FOR_SHOOT = 25, BLINKING_DURATION = 1300,
						 DIVISOR_FOR_BLINKING = 2;
	static final int NUM_SIMPLE_BULLETS = 10;
	public static final long EXPLOSION_DURATION = 14000;
	public static final int MAX_SHOWER_VELOCITY = 10;
	private long explosionStartTime;	
	Bitmap bitmap, explosion;
	float x, y,yIncrmtFactor, theta;
	Matrix matrix;
	private Random r;
	Rect box;
	Paint paint;
	boolean exploded, debug;
	List<Weapon> ammoList;
	Queue<Weapon> ammo;
	SoundPool soundPool; 
	 int exploPoolId, shootPoolId, specialWeaponPoolId;
	boolean isFloatingHoriz, isFloatingVert, isEntering;
	float xIncrmtFactor;
	private float pivotX, pivotY;
	private int xDirVec, yDirVec, xCycle, yCycle;
	public long blinkingStartTime;
	public boolean isBlinking;	
	 StatsBar bossStats;
	 Context context;

	public Boss(Context context, Bitmap b, Matrix matrix, int level) {
		this.context = context;
		bitmap = b;
		r = new Random();
		this.matrix = matrix;
		this.box = new Rect();
		this.paint = new Paint();
		this.explosion = BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion);
		this.scaleExplosionBitmap();
		ammoList = new ArrayList<Weapon>(NUM_SIMPLE_BULLETS);
		ammo = new LinkedBlockingQueue<Weapon>(NUM_SIMPLE_BULLETS);
		soundPool =  new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		exploPoolId = soundPool.load(context, R.raw.boss_explosion_wav, 1);
		shootPoolId = soundPool.load(context, R.raw.dron_shoot_wav, 0);
		specialWeaponPoolId = loadSpecialWeaponSound(level);

		this.debug = false;
		isFloatingHoriz = true;
		isFloatingVert = false;
		isEntering = false;
		xDirVec = yDirVec = 1;
		xIncrmtFactor = 0.1f * this.MAX_SHOWER_VELOCITY;
		xCycle = yCycle = 0;
		blinkingStartTime = 0;
		isBlinking = false;
		exploded = false;
		this.explosionStartTime = 0;
		bossStats = new StatsBar(8);
	}
	
	public void loadSpecialWeapons(Bitmap bitmap, int level){
		for(int i =0;i<NUM_SIMPLE_BULLETS;i++){
			Weapon w = new Weapon(bitmap, 0, 0, new Matrix());
			if(level == 3){
				w.bitmap2  = BitmapFactory.decodeResource(context.getResources(), R.drawable.lightning3);
				w.bitmap3  = BitmapFactory.decodeResource(context.getResources(), R.drawable.lightning4);
			}
			else {
				w.bitmap2 = w.bitmap3 = null;
			}
			this.ammo.add(w);
		}
	}
	
	public int loadSpecialWeaponSound(int level){
		int id = 0;
		if(level == 1)
			return soundPool.load(context, R.raw.lv1_special_boss, 0);
		else if(level == 2)
			return soundPool.load(context, R.raw.lv2_special_boss, 0);
		else if(level == 3)
			return soundPool.load(context, R.raw.lv3_special_boss, 0);
	
		return soundPool.load(context, R.raw.lv3_special_boss, 0);
	}
	public void scaleExplosionBitmap(){
		float scale = (float) (this.explosion.getHeight()/this.bitmap.getHeight());
		scale*=2;
		int newWidth = Math.round((float) this.explosion.getWidth()/ scale);
		int newHeight = Math.round((float) this.explosion.getHeight()/ scale);
		explosion = Bitmap.createScaledBitmap(explosion, newWidth, newHeight, true);
		
	}

	
	public void explosionSound() {
		// TODO Auto-generated method stub
		if(exploPoolId!=0)
			soundPool.play(exploPoolId, 0.5f, 0.5f, 0, 0, 1);
		
	}

	public void shootSound(int id) {
		// TODO Auto-generated method stub
		if(id == shootPoolId)
			soundPool.play(shootPoolId, 0.5f, 0.5f, 0, 0, 1);
		else if(id == specialWeaponPoolId)
			soundPool.play(specialWeaponPoolId, 0.5f, 0.5f, 0, 0, 1);
		else if(id == exploPoolId)
			soundPool.play(exploPoolId, 0.5f, 0.5f, 0, 0, 1);
	}

	
	/**
	 * This method is used AFTER the boss has entered the space
	 */
	public void prepShoot(int level) {	
		if(!this.exploded){
			int rareNumber = r.nextInt(RAND_BOUND_FOR_SHOOT);
			float[] vals = new float[9];
			this.matrix.getValues(vals);;
			// we need to make sure we do not remove from the empty queue of ammo in our Particle object
			if((!this.ammo.isEmpty()) && (rareNumber == RAND_NUM_FOR_SHOOT)){
				rareNumber = r.nextInt(60);
				if(rareNumber >= 0 && rareNumber<=40){
					Weapon w = this.ammo.remove();
					w.shooting = true;
					if(rareNumber >= 0 &&  rareNumber <=20)
						w.x = vals[2] - this.bitmap.getWidth();		
					else if(rareNumber >= 20 &&  rareNumber <=40)
						w.x = vals[2];
					w.y = vals[5];
					this.ammoList.add(w);
					shootSound(shootPoolId);
				
				}
				else {
						float iniX = vals[2] - this.bitmap.getWidth()/2;		
						float iniY = vals[5] - this.bitmap.getHeight()/2;
						if(level == 1)
							prepSpecialWeaponTraj(iniX, iniY);
						else if(level == 2)
							prepSpecialWeaponTraj(iniX, iniY);
						else if(level == 3){
							prepSpecialWeaponTraj(iniX, iniY);
						shootSound(specialWeaponPoolId);
						}
				}
			}
		}
	}
	
	public void prepSpecialWeaponTraj(float centreX, float centreY){	
			if(!this.ammo.isEmpty()){
				Weapon w = this.ammo.remove();
				w.shooting = true;
				w.theta = 0;
				w.centreX = centreX;
				w.centreY = centreY;
				w.bitmapIsSet  = true;
				this.ammoList.add(w);
			}
			shootSound(specialWeaponPoolId);
	}

	

	public void drawShootAndEnqueueWeapon(Canvas c, int level) {
		for(int i =0;i<this.ammoList.size();i++){ 
			if(this.ammoList.get(i).bitmapIsSet){
				if(level == 1){
					this.ammoList.get(i).drawSpecialWeaponTraj(c, 1, this, 0.0f, 0 ,0, 17);
				}
				else if(level == 2){
					this.ammoList.get(i).drawSpecialWeaponTraj(c, 2, this, 0.5f, 2 ,0, 5);
				}
				else if(level == 3){
					this.ammoList.get(i).drawSpecialWeaponTraj(c, 3, this, 0.1f, 1 ,0, 3);
					//draw level 3 special weapon for the boss
					
				}
			}
			else {
				this.ammoList.get(i).drawShoot(c, this);
			}
			if(!this.ammoList.get(i).shooting){
				this.ammo.add(this.ammoList.remove(i));
			}
		}

	}
	
	public void reset(Canvas c) {
		// TODO Auto-generated method stub
		this.matrix.reset();
		this.matrix.postTranslate(x, y);
		this.matrix.preRotate(180);
		exploded = false;
		this.box.setEmpty();
	}
	
	
	public void setPosForEntrance(Canvas c){
		this.x = c.getWidth()/1.5f;
		this.y = -20 - this.bitmap.getHeight();
		this.pivotX = c.getWidth()/1.5f;
		this.pivotY = this.bitmap.getHeight() + 50;
		this.yIncrmtFactor = 0.3f * this.MAX_SHOWER_VELOCITY;
		this.matrix.setTranslate(x, y);
		this.isEntering = true;
		this.reset(c);
		this.bossStats.setHealthHolderRect(40,  10, 250, 35);
	}
	
	public void drawHorizMotion(Canvas c){
		if(this.x >= (c.getWidth() - 50)){
			xDirVec = -1;
			xCycle++;
		}
		else if(this.x <= (200)){
			xDirVec = 1;
			xCycle++;
		}			
		this.x+=xDirVec * xIncrmtFactor;
		this.matrix.postTranslate(xDirVec * xIncrmtFactor, 0);
		if ((this.x == this.pivotX)){
			xCycle++;
			if(xCycle == 4){
				xCycle = 0;
				isFloatingHoriz = false;
				isFloatingVert = true;
				
			}
		}
	}

	public void drawVerticalMotion(Canvas c){
		if(this.y >= (this.bitmap.getHeight() + 200)){
			yDirVec = -1;
			yCycle++;
		}
		else if(this.y	<= (this.bitmap.getHeight())){
			yDirVec = 1;
			yCycle++;
		}
		this.y+=yDirVec * yIncrmtFactor;
		this.matrix.postTranslate(0, yDirVec * yIncrmtFactor);
		if(this.y == this.pivotY){
			yCycle++;
			if(yCycle == 4){
				yCycle = 0;
				isFloatingHoriz = true;
				isFloatingVert = false;
			}
		}
		
	}
	
	private void drawEntrance(Canvas c){
		paint.reset();
		y+=yIncrmtFactor;
		if(this.y>= (this.bitmap.getHeight() + 50)){
			isEntering = false;
			this.yIncrmtFactor = this.xIncrmtFactor;
		}
		this.matrix.postTranslate(0, yIncrmtFactor);		
		c.drawBitmap(bitmap, matrix, null);
		
	}
	
 
	public void drawBoss(Canvas c, Paint p) {
		if(isEntering){
			this.drawEntrance(c);
		}
		else {
			if(isFloatingHoriz)
				drawHorizMotion(c);
			else if(isFloatingVert)
				drawVerticalMotion(c);		
		
		}
		if(this.x >=0 && this.y>=0)
			this.box.set((int)(this.x - this.bitmap.getWidth()), (int) (this.y- this.bitmap.getHeight()),(int) (this.x),(int) (this.y));
		if(debug){	
			paint.setColor(Color.BLUE);
			c.drawRect(this.box, paint);
		}
		if(exploded){
			//do explosion mechanics 
			explosionSound();
		}
		else{
			//if the player didn't get hit, then obviously it woudln't blink. Hence draw normally
			final long blinkingEndTime = System.currentTimeMillis();
			long diff = blinkingEndTime - this.blinkingStartTime;
			if(!isBlinking)
				c.drawBitmap(this.bitmap, this.matrix, null);
			else if(diff<=BLINKING_DURATION){//Player did get hit! Hence, blinking is obviously true
										//check to see if the blinking duration is over yet. 	
				if((diff%DIVISOR_FOR_BLINKING) == 0)
					c.drawBitmap(bitmap, matrix, null);
			}
			else { //diff >= BLINKING_DURATION. Set blinking to false and blinkingStartTime to 0 
				isBlinking = false;
				blinkingStartTime = 0;			
			}
			
		}
	}
	
	public void drawExplosionsAndRetreat(Canvas c){
		this.yDirVec = -1;
		this.y+= yDirVec * yIncrmtFactor;
		this.matrix.postTranslate(0, yDirVec * yIncrmtFactor);
		this.box.set((int)(this.x - this.bitmap.getWidth()), (int) (this.y- this.bitmap.getHeight()),(int) (this.x),(int) (this.y));
			if(debug){	
				paint.setColor(Color.BLUE);
				c.drawRect(this.box, paint);
			}
			c.drawBitmap(bitmap, matrix, null);
			for(int i=0;i<5;i++){
				float fx = this.box.left +  (r.nextFloat() *((this.box.right - this.box.left)));
				float fy = this.box.top + (r.nextFloat() *((this.box.bottom - this.box.top)));
				fx-=r.nextFloat() * this.box.width();
				fy-=r.nextFloat() * this.box.height();
				c.drawBitmap(this.explosion, fx, fy, null);
		}
	}

}
