package com.jasfiddle.spacetrooper.main;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import com.jasfiddle.spacetrooper.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.AudioManager;
import android.media.SoundPool;

public class Player {
	private static final int NUM_SIMPLE_BULLETS = 10, BLINKING_DURATION = 1300,
							 DIVISOR_FOR_BLINKING = 2;
	float x, y, prevX, prevY;
	Bitmap bitmap, explosion;
	Matrix matrix;
	public boolean isDraggable, debug, exploded;
	float shootX, shootY;
	Context context;
	Rect box;
	List<Weapon> ammoList;
	Queue<Weapon> ammo;
	public boolean blinking;
	public long blinkingStartTime;
	StatsBar playerStats;
	SoundPool exploPool, shootPool; 
	int exploPoolId, shootPoolId;
	long explosionDisplayStartTime;
	
		
	public Player(Context context,float x, float y, Bitmap bmp, Matrix matrix){
		this.x = x;
		this.y = y;
		this.prevX = 0;
		this.prevY = 0;
		this.bitmap = bmp;
		this.matrix = matrix;
		this.isDraggable = false;
		this.shootX = -1;
		this.shootY = -1;
		this.context = context;
		this.box = new Rect();
		ammoList = new ArrayList<Weapon>(NUM_SIMPLE_BULLETS);
		ammo = new LinkedBlockingQueue<Weapon>(NUM_SIMPLE_BULLETS);
		for(int i=0;i<NUM_SIMPLE_BULLETS;i++){
			Weapon w = new Weapon(BitmapFactory.decodeResource(context.getResources(), R.drawable.green_laser), 0, 0, new Matrix());
			 this.ammo.add(w);
		}
		explosion = BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion);
		this.scaleExplosionBitmap();
		debug = false;
		exploded = false;
		blinking = false;
		blinkingStartTime = 0;
		long explosionDisplayStartTime = 0;
		exploPool =  new SoundPool(1, AudioManager.STREAM_MUSIC, 0); 
		exploPoolId = exploPool.load(context, R.raw.missile_impact_wav, 1);
		playerStats = new StatsBar(5);
	}
	
	public long getExplosionDisplayDuration(){
		final long currTime = System.currentTimeMillis();
		final long diff = currTime - explosionDisplayStartTime;
		return diff;
		
	}
	
	public void explosionSound(int id){
		if(id == exploPoolId && id !=0){
			exploPool.play(exploPoolId, 0.5f, 0.5f, 0, 0, 1);
			exploPoolId = 0;
			explosionDisplayStartTime = System.currentTimeMillis();
		}	
	}
	
	
	public void iniPlayer(Canvas c) {
		this.prevX = c.getWidth()/1.5f;
		this.prevY = c.getHeight()-20;
				
		this.matrix.postTranslate(this.prevX, this.prevY);
		this.matrix.preRotate(180);
		this.playerStats.setHealthHolderRect(40, c.getHeight() - 35, 250, c.getHeight() - 10);
		
	}
	
	public void prepShoot(){
		Weapon w = this.ammo.remove();
		w.shooting = true;
		float[] vals = new float[9];
		this.matrix.getValues(vals);
		w.x = vals[2] - (this.bitmap.getWidth()/2);
		w.y = vals[5] - (this.bitmap.getWidth()/2);
		this.ammoList.add(w);
		
	}
	
	public void dragPlayer(float eventX, float eventY){
		this.x = eventX;
		this.y = eventY;
		float dx = this.x - this.prevX, dy = this.y - this.prevY;
		this.matrix.postTranslate(dx, dy);
		this.prevX = this.x;
		this.prevY = this.y;
		float[] vals = new float[9];
		this.matrix.getValues(vals);
		this.box.set((int) (this.x - this.bitmap.getWidth()),(int) (this.y - this.bitmap.getHeight()) ,(int) this.x, (int) this.y);	
	}
	
	public void drawPlayer(Canvas c){
			final long blinkingEndTime = System.currentTimeMillis();
			final long diff = blinkingEndTime - blinkingStartTime; 
			if(debug){
				Paint p = new Paint();
				p.setColor(Color.RED);
				c.drawRect(this.box, p);
			}
			 if(exploded){
					explosionSound(exploPoolId);
					c.drawBitmap(explosion, this.matrix, null);
				}
		
			else{
				//if the player didn't get hit, then obviously it woudln't blink. Hence draw normally
				if(!blinking)
					c.drawBitmap(this.bitmap, this.matrix, null);
				else if(diff<=BLINKING_DURATION){//Player did get hit! Hence, blinking is obviously true
											//check to see if the blinking duration is over yet. 	
					if((diff%DIVISOR_FOR_BLINKING) == 0)
						c.drawBitmap(bitmap, matrix, null);
				}
				else { //diff >= BLINKING_DURATION. Set blinking to false and blinkingStartTime to 0 
					blinking = false;
					blinkingStartTime = 0;			
				}	
			}
		
		
			
	}

	public void scaleExplosionBitmap(){
		float scale = (float) (this.explosion.getHeight()/this.bitmap.getHeight());
		int newWidth = Math.round((float) this.explosion.getWidth()/ scale);
		int newHeight = Math.round((float) this.explosion.getHeight()/ scale);
		explosion = Bitmap.createScaledBitmap(explosion, newWidth, newHeight, true);
		
	}
	
	
	public void drawShootAndEnqueueWeapon(Canvas canvas){
		for(int i =0;i<this.ammoList.size();i++){ 
			this.ammoList.get(i).drawShoot(canvas, this);
			if(!this.ammoList.get(i).shooting){
				this.ammo.add(this.ammoList.remove(i));
			}
		}
	}
	

	
	public Boss drawBulletCollisionWithBoss(Canvas c, Boss boss){
		if(!boss.isEntering){ //You Weapon(s) will have no effect on the boss while he is making an entrance
			for(int i=0;i<this.ammoList.size();i++){//We need to find only 1 Weapon from the this that hits this Particle. Hence, if this happens we break prematurely 
				Weapon w = this.ammoList.get(i);
				if(Rect.intersects(w.box,boss.box) && (!this.exploded)){
					boss.bossStats.hitCount+=1;
					if(boss.bossStats.hitCount >= boss.bossStats.maxAllowableHits)
						boss.exploded = true;
					else{
						boss.isBlinking = true;
						boss.blinkingStartTime = System.currentTimeMillis();
					}
					this.playerStats.score++;
					this.ammoList.get(i).resetWeapon();
					this.ammo.add(this.ammoList.remove(i));
					break;  //We found the Weapon that hit the Boss. We now exit this loop.
				}
			}
		}
			return boss;
		
	}
	
	
	/*
	 *  COLLISION ALGORITHMS 
	 * 
	 * 
	 *
	 */
	
	
	public List<Particle> drawParticlesBulletCollisionWithThis(Canvas c, List<Particle> particles){
		boolean playerHit = false;
		for(int i=0;i<particles.size();i++){ //we exit this loop as soon as we find a Particle whose Weapon hits the Player
			for(int j=0;j<particles.get(i).ammoList.size();j++){ //we exit this loop as soon as we find that one Weapon that hits the Player
 				if(Rect.intersects(particles.get(i).ammoList.get(j).box, this.box)){
 					this.playerStats.hitCount+=1;
 					if(this.playerStats.hitCount >= this.playerStats.maxAllowableHits){
						this.exploded = true;
					}
					else {
						playerHit = true; //this is hit
						this.blinking = true; // start blinking
						this.blinkingStartTime = System.currentTimeMillis(); 
						//now check the drawPlayer(Canvas) method in the this class for this effect
					}
				}
 				if(playerHit)
 					break;
			}
			if(playerHit) 
				break;
		}
		return particles;
	}
	
	
	public List<Particle> drawBulletCollisionWithParticles(Canvas c, List<Particle> particles){
		for(int i=0;i<this.ammoList.size();i++){ 
			for(int j=0;j<particles.size();j++){ //We need to find only 1 Weapon from the this that hits this Particle. Hence, if this happens we break prematurely
				Weapon w = this.ammoList.get(i);
				Particle p = particles.get(j);
				if(Rect.intersects(w.box,p.box) && (!p.exploded)){
					particles.get(j).exploded = true;
					particles.get(j).explosionStartTime = System.currentTimeMillis();
					particles.get(j).explosionSound();
					this.playerStats.score++;
					this.ammoList.get(i).resetWeapon();
					this.ammo.add(this.ammoList.remove(i));
					particles.get(j).resetAmmo();
					break;  //We found the Weapon that hit this Particle. We now exit this loop.
				}
			}
		}
		return particles;
	}
	
	public Boss drawBossBulletCollisionWithThis(Canvas c, Boss boss){
		for(int i = 0;i<boss.ammoList.size();i++){
			if(Rect.intersects(boss.ammoList.get(i).box, this.box)){			
				this.playerStats.hitCount+=1;
				if(this.playerStats.hitCount >= this.playerStats.maxAllowableHits){
					this.exploded = true;
				}
				else {
					this.blinking = true; // start blinking
					this.blinkingStartTime = System.currentTimeMillis(); 
					//now check the drawPlayer(Canvas) method in the this class for this effect
				}
			}	
		}
		return boss;
	}
	

	
	

}
