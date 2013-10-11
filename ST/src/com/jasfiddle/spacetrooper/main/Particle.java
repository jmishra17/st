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

import com.jasfiddle.spacetrooper.*;

public class Particle {
	 private static final int NUM_SIMPLE_BULLETS = 10, RAND_BOUND_FOR_SHOOT = 200,
			 				 RAND_NUM_FOR_SHOOT = 25;
	 private static final long EXPLOSION_DURATION = 700;
	 public long explosionStartTime;
	 public static final int MAX_SHOWER_VELOCITY = 10;
	 boolean reverse;
	 float radius;
	 float theta;
	 Bitmap bitmap, explosion;
	 float x, y;
	 float yIncrmtFactor;
	 Matrix matrix;
	 private Random r;
	 Rect box;
	 Paint paint;
	 boolean exploded, debug;
	 List<Weapon> ammoList;
	 Queue<Weapon> ammo;
	 SoundPool exploPool, shootPool; 
	 int exploPoolId, shootPoolId;
	 
	 
	public Particle(Context context,boolean reverse, float radius, float theta, Bitmap  b, float centreX, 
									float centreY, float incrmtFactor, Matrix matrix){
		this.reverse = reverse;
		this.radius = radius;
		this.theta = theta;
		bitmap = b;
		this.x = centreX;
		this.y = centreY;
		this.yIncrmtFactor = incrmtFactor;
		r = new Random();
		this.matrix = matrix;
		this.box = new Rect();
		this.paint = new Paint();
		this.explosion = BitmapFactory.decodeResource(context.getResources(), R.drawable.explosion);
		ammoList = new ArrayList<Weapon>(NUM_SIMPLE_BULLETS);
		ammo = new LinkedBlockingQueue<Weapon>(NUM_SIMPLE_BULLETS);
		for(int i=0;i<NUM_SIMPLE_BULLETS;i++){
			Weapon w = new Weapon(null, 0, 0, new Matrix());
			this.ammo.add(w);
		}
		shootPool = exploPool =  new SoundPool(1, AudioManager.STREAM_MUSIC, 0); 
		exploPoolId = exploPool.load(context, R.raw.laser_blast_wav, 1);
		shootPoolId = shootPool.load(context, R.raw.dron_shoot_wav, 0);
		explosionStartTime = 0;	
		this.debug = false;
	}
	
	public void iniParticle(Canvas c){
		this.x =(float) c.getWidth() * r.nextFloat();
		this.y = 0;
		this.yIncrmtFactor = 2 + r.nextInt(MAX_SHOWER_VELOCITY);
		this.scaleExplosionBitmap();
		this.reset(c);
		
	}
	
	public void explosionSound(){
		if(exploPoolId!=0)
			exploPool.play(exploPoolId, 0.5f, 0.5f, 0, 0, 1);
		
	}
	
	public void shootSound(){
		if(shootPoolId!=0)
			shootPool.play(shootPoolId, 0.0f, 0.5f, 0, 0, 1);
	}
	
	
	public void prepShoot(){
		if(!this.exploded){
			int rareNumber = r.nextInt(RAND_BOUND_FOR_SHOOT);
			// we need to make sure we do not remove from the empty queue of ammo in our Particle object
			if((!this.ammo.isEmpty()) && (rareNumber == RAND_NUM_FOR_SHOOT)){
				shootSound();
				Weapon w = this.ammo.remove();
				w.shooting = true;
				float[] vals = new float[9];
				this.matrix.getValues(vals);
				w.x = vals[2] - this.bitmap.getWidth()/2;
				w.y = vals[5];
				this.ammoList.add(w);
			}
		}
	}
	
	
	public void drawShootAndEnqueueWeapon(Canvas canvas){
		if(!this.exploded){  //we let this Particle shoot only when it not exploded (!exploded)
			for(int j=0;j<this.ammoList.size();j++){
				this.ammoList.get(j).drawShoot(canvas, this);
				if(!this.ammoList.get(j).shooting)
					this.ammo.add(this.ammoList.remove(j));
			}
		}	
	}
	
	
/*	
	public void drawSpiral(Canvas canvas){
		if(theta%180==0)
			theta = 0;
		if(radius<=0){
			reverse = false;
		}
		else if(radius>=(canvas.getWidth()-200))
			reverse = true;
		if(reverse)
			radius--;
		else
			radius++; 
		float x= x + (float) (radius * Math.cos(theta));
		float y= y + (float) (radius * Math.sin(theta));
		theta+=yIncrmtFactor;
	
		canvas.drawBitmap(bitmap, x, y, null);		
	} */
	
	public void scaleExplosionBitmap(){
		float scale = (float) (this.explosion.getHeight()/this.bitmap.getHeight());
		int newWidth = Math.round((float) this.explosion.getWidth()/ scale);
		int newHeight = Math.round((float) this.explosion.getHeight()/ scale);
		explosion = Bitmap.createScaledBitmap(explosion, newWidth, newHeight, true);
		
	}	
	
	public void reset(Canvas c){
		y =  2 + r.nextInt(MAX_SHOWER_VELOCITY);
		x = r.nextFloat() * c.getWidth();
		this.matrix.reset();
		this.matrix.postTranslate(x, y);
		this.matrix.preRotate(180);
		exploded = false;
		this.box.setEmpty();
	}
	

	//Blinks the explosion for BLINKING_EXPLOSION_DURATION  and then makes it transparent (disappear!)
	private void drawBlinkingExplosionAndDisappear(Canvas c){ 
		final long endTime = System.currentTimeMillis();
		long diff = endTime - this.explosionStartTime;
		if(diff >= this.EXPLOSION_DURATION){
			//make the bitmap transparent
			this.explosionStartTime = 0;
			paint.reset();
			paint.setColor(Color.TRANSPARENT);
			c.drawBitmap(this.explosion, this.matrix, paint);
		}
		else if (diff%2==0){
			c.drawBitmap(this.explosion, this.matrix, paint);
		}
		
	}
	
	public void drawParticle(Canvas c, Paint p){
		paint.reset();
		y+=yIncrmtFactor;
		if(y>=(c.getHeight() + this.bitmap.getHeight())){
			reset(c);		
		}
		
		this.matrix.postTranslate(0, yIncrmtFactor);
		if(this.x >=0 && this.y>=0)
			this.box.set((int)(this.x - this.bitmap.getWidth()), (int) (this.y- this.bitmap.getHeight()),(int) (this.x),(int) (this.y));
		
		if(debug){	
			paint.setColor(Color.BLUE);
			c.drawRect(this.box, paint);
		}
		if(exploded){
			drawBlinkingExplosionAndDisappear(c); 
		}
		else
			c.drawBitmap(this.bitmap, this.matrix, null);
		
		

	}

	public void resetAmmo() {
		// TODO Auto-generated method stub
		for(int j=0;j<this.ammoList.size();j++){
				ammoList.get(j).shooting = false;
				this.ammo.add(this.ammoList.remove(j));
		}		
	}

}
