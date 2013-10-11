package com.jasfiddle.spacetrooper.main;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

public class Weapon {
	private static final int SIMPLE_BULLET_VELOCITY = 20, //velocity of basic bullets
			 				 MISSILE_VELOCITY = 30,
							EXPLOSIVE_VELOCITY  = 40;
	
	Bitmap bitmap, bitmap2, bitmap3;
	float x, y;
	float centreX, centreY, theta, radius;
	Matrix matrix;
	Paint paint;
	boolean shooting, bitmapIsSet;
	Rect box; 
	int id;

	
	public Weapon(Bitmap weapon, float x, float y, Matrix matrix){
		this.bitmap = weapon;
		this.x = x;
		this.y = y;
		theta = 0;
		this.matrix = matrix;
		shooting = false;
		box = new Rect();
		id = 0;
		paint = new Paint();
		bitmapIsSet = false;
	
	}
	
	public void resetWeapon(){
		this.x = -10;
		this.y = -10;
		centreX = -10;
		centreY = -10;
		theta = 0;
		id =0;
		this.matrix.reset();
		this.box.setEmpty();
		shooting = false;
		bitmapIsSet = false;
		radius = 80;
	}
	
	
	public void drawShoot(Canvas c, Player p){
		if(shooting){			
			this.x+=0;
			this.y+= (-SIMPLE_BULLET_VELOCITY);
			this.box.set((int)(this.x), (int) (this.y),(int) (this.x + 10),(int) (this.y + 50) );
			matrix.setTranslate(this.x, this.y);			
			paint.reset();
			paint.setColor(Color.YELLOW);
			c.drawRect(this.box, paint);			
			if(bitmapIsSet){
					c.drawBitmap(bitmap, matrix, null);
			}
			if((this.y<(-10)) || (this.y> (c.getHeight() +10))){
				resetWeapon();
			}
		}
	}
	

	public void drawShoot(Canvas c, Particle p){
		if(shooting){
			this.x+=0;
			this.y+= (SIMPLE_BULLET_VELOCITY);
			matrix.setTranslate(this.x, this.y);	
			this.box.set((int)(this.x), (int) (this.y),(int) (this.x + 10),(int) (this.y + 50));
			paint.reset();
			paint.setColor(Color.MAGENTA);
			c.drawRect(this.box, paint);
			if(bitmapIsSet){
				c.drawBitmap(bitmap, matrix, null);
		}
			if((this.y<(-10)) || (this.y> (c.getHeight() +10))){
				resetWeapon();
			}
		}
	}

	public void drawShoot(Canvas c, Boss boss) {
		if(shooting){
			if(bitmapIsSet){
				this.x+=0;
				this.y+= MISSILE_VELOCITY;
				this.matrix.setTranslate(this.x, this.y);
				this.box.set((int)(this.x), (int) (this.y),(int) (this.x + this.bitmap.getWidth()),(int) (this.y + this.bitmap.getHeight()));
				c.drawBitmap(bitmap, matrix, null);
			}
			else {
				this.x+=0;
				this.y+= (SIMPLE_BULLET_VELOCITY);
				matrix.setTranslate(this.x, this.y);
				this.box.set((int)(this.x), (int) (this.y),(int) (this.x + 10),(int) (this.y + 50));				
				paint.reset();
				paint.setColor(Color.MAGENTA);
				c.drawRect(this.box, paint);
			}
			if((this.y<(-10)) || (this.y> (c.getHeight() +10))){
				resetWeapon();
			}
		}
			
	}
	
	public void drawSpecialWeaponTraj(Canvas c, int level, Boss boss, float rotationSpeed, float radIncrmtFactor, 
										float centreXIncrmtFactor, float centreYIncrmtFactor){
		if(shooting){
			if(theta == 180){
				theta = 0;
			}
			this.x= centreX + (float)(radius * Math.cos(theta));
			this.y= centreY + (float)(radius * Math.sin(theta));
			this.matrix.setTranslate(this.x, this.y);
			this.box.set((int)(this.x), (int) (this.y),(int) (this.x + this.bitmap.getWidth()),(int) (this.y + this.bitmap.getHeight()));
			c.drawBitmap(this.bitmap, this.matrix, null);
			if(level == 3){		
				drawLightning(c);
			}
			theta+=rotationSpeed; //0.5f
			centreX+=centreXIncrmtFactor; //0
			centreY+=centreYIncrmtFactor;  //5
 			radius+=radIncrmtFactor; //0
			if(this.y >=(this.bitmap.getHeight() + c.getHeight())){
				resetWeapon();
			}
		}
	}
	
	private void drawLightning(Canvas c){
		for(int i = 0;i<8;i++){
			float fx = (float) (this.box.left +  (Math.random() *((this.box.right - this.box.left))));
			float fy = (float) (this.box.top + (Math.random()  *((this.box.bottom - this.box.top))));
			fx-=Math.random()  * this.box.width();
			fy-=Math.random()  * this.box.height();
			if(fx%2 == 0)
				c.drawBitmap(bitmap2, fx, fy, null);
			else
				c.drawBitmap(bitmap3, fx, fy, null);
		}
		
	}
	
}


