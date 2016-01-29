package com.android.detonate;

import java.util.Iterator;

import android.graphics.Canvas;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

class MainThread extends Thread {
	
    	static SurfaceHolder threadSurfaceHolder;
    	
    	/**Для возможности дебага всего это поле решено сделать личным.
    	 * Если игровые интервалы основывать на реальном времени, 
    	 * то на дебаге невозможно будет отловить промежуточные состояния.**/
    	private long currentTime = 0; 
    	long startOfFrameProcessingTime=0, endOfFrameProcessingTime=0;
    	boolean running=false;
    	private static boolean globalPerformance = false;
    	private static boolean graphicsPerformance = false;
    	private static int perfPeriod = 4;
    	
    	static long counter = 0;
    	private static long tmpStartTime;
    	private static String timerDescript;
    	private static Canvas tempCanvas;
    	
    	static  public void startTimer(String descript){
    		if(counter%(Cnt.FPS*perfPeriod)!=0 || !graphicsPerformance) return;
    		tmpStartTime = SystemClock.uptimeMillis();
    		timerDescript = descript;     
    	}
    	
    	static public void postTime(){
    		if(counter%(Cnt.FPS*perfPeriod)!=0 || !graphicsPerformance) return;
    		Log.i("perfomance", timerDescript + ':' + (int) (SystemClock.uptimeMillis() - tmpStartTime));
    		
    	}
    	
    	
    	/*public RefresherThread(GameView inputThreadPanel, SurfaceHolder inputHolder){
    		threadSurfaceHolder = inputHolder;
    		threadView = inputThreadPanel;
    	}*/
    	
    	
    	/*public RefresherThread() {
			// TODO Auto-generated constructor stub
		}*/
    	

		public MainThread(SurfaceHolder holder, CView gameView) {
			threadSurfaceHolder = holder;
			CAct.threadView = gameView;
			// TODO Auto-generated constructor stub
		}

		@Override
		public void run (){
    		
			while(!World.hasBeenInitialised){
				   
			}
			
    		while(running){
    			
    			TouchMsgBuf.execStack();
    			
    			if((currentTime=SystemClock.uptimeMillis())-startOfFrameProcessingTime>Cnt.DT){
 
    				startOfFrameProcessingTime=currentTime;
    				
    				// Рисование
    				CAct.threadView.helperDraw();
	    				
    				tempCanvas = threadSurfaceHolder.lockCanvas();
		    		synchronized(threadSurfaceHolder) { 
		    			CAct.threadView.onDraw(tempCanvas);
		    		}
		    		threadSurfaceHolder.unlockCanvasAndPost(tempCanvas);
		    		
		    		World.h.post(World.redrawIB);
		    		
		    		long drawingDuration = SystemClock.uptimeMillis() - startOfFrameProcessingTime;
		    		
		    		// Обработка
		    		for (Warrior w : World.warriors) {
		    			w.doLogics();
		    			w.computeFall();
		    		}
		    		
	    			World.processBattleSituation();
	    			counter++;
		    		
		    		TouchMsgBuf.applyReturningShift();
		    		Animator.tickAll();
		    		
		    		long processingDuration = SystemClock.uptimeMillis()-startOfFrameProcessingTime-drawingDuration;
		    		long sumDuration = drawingDuration + processingDuration;
		    		if(counter%(Cnt.FPS*perfPeriod)==0 && globalPerformance){
			    		if(sumDuration>Cnt.DT){
			    			Log.e("perfomance","Sorry, lacking of " + (sumDuration-Cnt.DT) + " ms to process.");
			    		} else {
			    			Log.i("perfomance","Good so far. Having " + (Cnt.DT-sumDuration) + " ms not used.");
			    		}
			    		Log.i("perfomance","Drawing duration: " + drawingDuration + ", processing duration: " + processingDuration + ".");
		    		}
        		}	
    		}
    	}
    }