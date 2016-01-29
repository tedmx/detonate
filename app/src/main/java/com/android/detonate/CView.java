package com.android.detonate;

import java.util.Iterator;
import java.util.Locale;

import com.android.detonate.Grid.TileType;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.graphics.Xfermode;
import android.opengl.Matrix;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

 /** :Custom View **/
 class CView extends SurfaceView implements SurfaceHolder.Callback  {
		
	 
    	MainThread surfaceThread;
    			
    	/** Отступ View сверху. Нужен для корректировки показаний тачскрина. **/
    	static float yOffset; 
    			
    	/** paint **/
    	static Paint p;
    	static int[] size;
    	
    	static float scalingFactor; 
        static LinearGradient darkGrayToGray;
        static PorterDuffXfermode screenXfermode; 
        static ColorFilter flashed;
        
        static GlowingRectangle goalColumn;
        
        static android.graphics.Matrix inv;
        
        private Bitmap lowRes;
        
        /** helper canvas**/
    	private Canvas hCanvas;
    	
		public CView(Context inpParentActivity) {
			
			super(inpParentActivity);
			
			getHolder().addCallback(this);
			
	        
		}
		
		void init(){
			
			p = new Paint();
			
			yOffset = CAct.instance.getWindowManager().getDefaultDisplay().getHeight()-getBottom();
			
			scalingFactor = size[0]/(float)(Cnt.canonicalGridSize[0]*Cnt.cellSize);
	
			lowRes = Bitmap.createBitmap(Math.round(size[0]), Math.round(size[1]), Bitmap.Config.RGB_565);
			hCanvas = new Canvas();
	        hCanvas.setBitmap(lowRes);
			
	        screenXfermode = new PorterDuffXfermode(PorterDuff.Mode.SCREEN);
	        flashed = new PorterDuffColorFilter(Color.argb(96,214,225,189), PorterDuff.Mode.SRC_ATOP);
	        darkGrayToGray = new LinearGradient(0, 0, 0, size[1], 
					 0xFF12010A,0xFF011721, 
					 TileMode.CLAMP);
			
		}
		
		
		public CView(Context context, AttributeSet attrs){
			super(context, attrs);
			getHolder().addCallback(this);
			MainThread.threadSurfaceHolder = getHolder();
			CAct.threadView = this;
		}

		
		@Override 
		public boolean onTouchEvent(MotionEvent event) {
			
			TouchMsgBuf.addMsg(event);
    		return true;

		}
		
		
		void helperDraw(){
			
			p.setShader(darkGrayToGray);
			hCanvas.drawRect(0, 0, size[0], size[1], p);
			
			hCanvas.save();
			hCanvas.scale(scalingFactor,scalingFactor);
			
				hCanvas.save();
				hCanvas.translate(TouchMsgBuf.getScaledViewportXShift(), 0);
					
						hCanvas.getMatrix().invert(inv);
						
						Pointer.draw(hCanvas);
						
						if(World.drawGoalColumn) goalColumn.cycle(hCanvas);
						
						Grid.draw(hCanvas); 
						
						for (Warrior w : World.warriors) {
							 w.draw(hCanvas);
						}
						
						TimerEffect.instance.draw(hCanvas);
						
						AVContainer.drawAll(hCanvas, Grounds.BACK);
						
						PathFinding.drawPath(World.testAL,hCanvas);
				
				// Проблема: при выходе из сна 
				// в этот момент restoreCount равен 1.
				hCanvas.restoreToCount(2);  
				
				StatusUI.draw(hCanvas);
			
			hCanvas.restoreToCount(1);  
			
			Msg.onDraw(hCanvas, p);
		
			AVContainer.drawAll(hCanvas, Grounds.FORE);
			
		}

		@Override
		public void onDraw(Canvas c){
			
			MainThread.startTimer("onDraw");
			
			if(p==null)  return; 
			
			p.reset();
			
			c.drawBitmap(lowRes, 0, 0, p);
			
    		MainThread.postTime();
    		
		}
		
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			
			size = new int[2];
			
			size[0] = width;
			size[1] = height;
			
			World.init();
			
		}

		public void surfaceCreated(SurfaceHolder holder) {
			
			
			 surfaceThread = new MainThread(getHolder(), this); 
			 surfaceThread.running=true;
			 surfaceThread.start();
			 
		}
		

		public void surfaceDestroyed(SurfaceHolder holder) {
			
			boolean retry = true;
			surfaceThread.running=false;
			while (retry) {
				try {
					surfaceThread.join();
					retry = false;
				} catch (InterruptedException e) {
					
				}
			}
						
		}
		

    }
		