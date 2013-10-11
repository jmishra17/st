package com.jasfiddle.spacetrooper.main;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.jasfiddle.spacetrooper.R;

public class STSurface extends SurfaceView implements SurfaceHolder.Callback {
	private static final int NUM_ENEMIES = 7, //number of enemies per showers
							INVALID_POINTER_ID = -1; // pointer
	public static final boolean DEBUG = false;
	private int activePointerId = INVALID_POINTER_ID, secondPointerId = INVALID_POINTER_ID;
	private GFXThread thread;
	private List<Particle> particles;
	private Bitmap background;
	private Random r;
	private Player player;
	private Boss boss;
	float windowWidth, windowHeight, windowScaleX, windowScaleY;
	private STInterface menuStuff;
	private Context context;
	public boolean gamePaused;
	public long gamePauseStartTime, gamePauseEndTime;
	
	public STSurface(Context context, float windowWidth, float windowHeight) {
		super(context);
		getHolder().addCallback(this);
		 r = new Random();
		this.windowWidth = windowWidth;
		this.windowHeight = windowHeight;
		this.context = context; 
		r = new Random();
		thread = new GFXThread(getHolder(), this, false);	
	}
	
	public long getGamePauseDuraton(){
		long diff = (gamePauseEndTime - gamePauseStartTime);
		return (diff);
		
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		
		// TODO Auto-generated method stub	
	}
	
	public void loadAllGameEssentials(Canvas c, int level){
		windowScaleX = (float) c.getWidth()/windowWidth;
		windowScaleY = (float) c.getHeight()/windowHeight;
		player = new Player(context,0,0,BitmapFactory.decodeResource(getResources(), R.drawable.jetfighter_ss),new Matrix());	
		List<Bitmap> objs= loadGameObjs("d", level);
		int size = objs.size();		
		particles = new ArrayList<Particle>(NUM_ENEMIES);
		for(int i=0;i<NUM_ENEMIES;i++){
			Particle p = new Particle(context,false, r.nextFloat() * 180, 0, 
						objs.get(r.nextInt(size)), 0, 0, 0, new Matrix());	
			particles.add(p);
		}
		objs.clear();
		objs = loadGameObjs("b", level);
		boss = new Boss(context, objs.get(0),  new Matrix(), level);
		objs.clear();
		boss.loadSpecialWeapons(loadGameObjs("special", level).get(0), level);
		
		menuStuff = new STInterface();			
		background =  loadGameObjs("back", level).get(0);
		background = this.scaleBackground(background);
		for(int i=0;i<particles.size();i++){
			particles.get(i).iniParticle(c);
		}
		gamePaused = false;
		gamePauseStartTime = gamePauseEndTime = 0;
		player.iniPlayer(c);
		boss.setPosForEntrance(c);
		menuStuff.setLevelParams(level);
		
	}
	
	public List<Bitmap> loadGameObjs(String category, int level){
		String startsWithStr = "lv"+level+"_"+category;
		List<Bitmap> objs = new ArrayList<Bitmap>();
		Class resources = R.drawable.class;
   	 	Field[] fields = resources.getFields();	 
   	 	for(Field f:fields){
   	 		if(f.getName().startsWith(startsWithStr)){
   	 			int id = getResources().getIdentifier(f.getName(), "drawable", "com.jasfiddle.spacetrooper");
   	 			objs.add(BitmapFactory.decodeResource(getResources(), id));
   	 		}
   		 }
  
   	 	return objs;		
	}
	
	private Bitmap scaleBackground(Bitmap background){
		 float scale = (float)background.getHeight()/(float)getHeight();
		 int newWidth = Math.round(background.getWidth()/scale);
		 int newHeight = Math.round(background.getHeight()/scale);
		 background = Bitmap.createScaledBitmap(background, newWidth, newHeight, true);
		 return background;
	}

	public void draw(Canvas c) {
		if(!menuStuff.levelComplete){
			if(menuStuff.isFlashingLevel){
				c.drawColor(Color.BLACK);
				menuStuff.drawLevelIndicator(c, getGamePauseDuraton());		
			}
			else{
				c.drawBitmap(background, 0, 0, null);
				if(!menuStuff.levelPlayTimeIsUp(getGamePauseDuraton())){
					for(int i=0;i<particles.size();i++){
						particles.get(i).drawParticle(c, null);
						particles.get(i).prepShoot();
					}  
					for(int i=0;i<particles.size();i++)
						particles.get(i).drawShootAndEnqueueWeapon(c);
					
					if(!player.blinking)
						particles = player.drawParticlesBulletCollisionWithThis(c, particles);
					
					particles = player.drawBulletCollisionWithParticles(c, particles);
					
					if(boss.ammo.size() == boss.NUM_SIMPLE_BULLETS){
							boss.prepSpecialWeaponTraj(r.nextFloat() * (c.getWidth() + 1), -20);
						
					}
				}
				else if(!boss.exploded) {
					if(!boss.isBlinking)
						boss = player.drawBulletCollisionWithBoss(c, boss);
					if(!player.blinking)
						boss = player.drawBossBulletCollisionWithThis(c, boss);
					boss.drawBoss(c, null);
					boss.prepShoot(thread.level);
					boss.drawShootAndEnqueueWeapon(c, thread.level);
					boss.bossStats.drawHealthBar(c);							
				}
				else if(boss.exploded){
					boss.drawExplosionsAndRetreat(c);
					if(boss.y <= (10 - boss.bitmap.getHeight())){
						menuStuff.levelComplete = true;
					}
				}
				boss.drawShootAndEnqueueWeapon(c, thread.level);
				if(!player.blinking)
					boss = player.drawBossBulletCollisionWithThis(c, boss);
				player.playerStats.drawScore(c);
				player.drawPlayer(c);
				player.drawShootAndEnqueueWeapon(c);
				player.playerStats.drawHealthBar(c);
				if(player.exploded){
					if(player.getExplosionDisplayDuration() <=2500)
						player.playerStats.drawGameOver(c);
					else{
						this.setThreadGamePaused(true);
						this.setThreadGameResume(false);
					}
				}
				
			}
		}
		else if(menuStuff.levelComplete && thread.level<3){
			c.drawColor(Color.BLACK);
			thread.level++;
			loadAllGameEssentials(c, thread.level);
			menuStuff.levelComplete = false;
		}
		else if(menuStuff.levelComplete && thread.level==3){
			menuStuff.drawGameComplete(c);
		}
		
		else if(gamePaused){
			menuStuff.drawGamePaused(c);
		}
	}
	
	public void setThreadGamePaused(boolean gameEnd){
		gamePaused = true;
		boolean retry = false;
		 thread.setRunning(false);
		while(retry){
			try {
				thread.join();
				retry = false;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(!gameEnd)
			gamePauseStartTime = System.currentTimeMillis();
	}
	
	public void setThreadGameResume(boolean isInitialized){
		gamePaused = false;
		gamePauseEndTime = System.currentTimeMillis();
		thread = new GFXThread(getHolder(), this, isInitialized);
		thread.setRunning(true);
		thread.start();
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		final int action = event.getAction(), 
				actionCode = action & MotionEvent.ACTION_MASK, pointerCount = event.getPointerCount();
		switch(actionCode){
			case MotionEvent.ACTION_DOWN: {
					player.isDraggable = true;
					activePointerId = event.getPointerId(0);
				break;
			}
			
			case MotionEvent.ACTION_MOVE: {
				if(player.isDraggable && (!player.exploded)){
					final int pointerIndex = event.findPointerIndex(activePointerId);
					final float evX = event.getX(pointerIndex) * windowScaleX, evY = event.getY(pointerIndex) * windowScaleY;
					player.dragPlayer(evX, evY);
				}
				break;
			}
			
			case MotionEvent.ACTION_POINTER_DOWN: {
				secondPointerId = event.getPointerId(1);			
				if((!player.ammo.isEmpty()) && (!player.exploded)){
					player.prepShoot();
				}
			}
			
			case MotionEvent.ACTION_POINTER_UP: {
				// Extract the index of the pointer that left the touch sensor
		        final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) 
		                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		        final int pointerId = event.getPointerId(pointerIndex);
		        if (pointerId == activePointerId) {
		            // This was our active pointer going up. Choose a new
		            // active pointer and adjust accordingly.
		            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
		            activePointerId = event.getPointerId(newPointerIndex);
		        }
		        break;
			}
			
			case MotionEvent.ACTION_UP:{
				activePointerId = INVALID_POINTER_ID;
				break;
			}
			
			case MotionEvent.ACTION_CANCEL: {
			    activePointerId = INVALID_POINTER_ID;
			        break;
			 }
		}
		
		return true;
	}
	

	public void surfaceCreated(SurfaceHolder holder) {
		thread.setRunning(true);
		thread.start();
		// TODO Auto-generated method stub
		
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub
		boolean retry = true;
		thread.setRunning(false);

		while(retry){
			try {
				thread.join();
				retry = false;
			}
			catch(InterruptedException e){
				
			}
		}
	}

	public static int getInvalidPointerId() {
		// TODO Auto-generated method stub
		return INVALID_POINTER_ID;
	}

}
