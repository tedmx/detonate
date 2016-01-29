package com.android.detonate;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Bitmap.Config;
import android.sax.StartElementListener;

public class RandEffects {

	private static void drawPixelizedTime(Canvas helperCanvas, Bitmap lowRes){
		
		//До рисования на вспомогательный канвас
		CView.p.reset();
		
		String timeString = "0:34:956";
		Rect stringBounds = new Rect();
		CView.p.getTextBounds(timeString, 0, timeString.length(), stringBounds);
		Bitmap a = Bitmap.createBitmap(stringBounds.width(),stringBounds.height(),Config.ARGB_8888);

		helperCanvas.setBitmap(a);
		CView.p.setColor(Color.WHITE);
		helperCanvas.drawText("0:34:956", -stringBounds.left, stringBounds.height(), CView.p);
		
		helperCanvas.setBitmap(lowRes);
		
		//В конце рисования на вспомогательный канвас
		CView.p.reset();
		helperCanvas.save();
		helperCanvas.scale(3, 3);
		helperCanvas.drawBitmap(a,20,20,CView.p);
		helperCanvas.restore();
	}
	
}


class TimerEffect extends AnimatedVisual{

	static TimerEffect instance = new TimerEffect(); 
	static final float textSize = Cnt.cellSize*3;
	static final float fadeTime = 1500;
	static final float sigmoidParameter = 0.3f;
	
	private TimerEffect() {
		
		super(5000);
		
		al = new AnimatorListener() {
			
			public void end() {
				World.shallBeginNewRound = true;
			}
		};
	}
	
	public void start(int inX){
		
		x = inX;
		restart();
	}
	
	@Override
	void draw(Canvas c) {
		// TODO Auto-generated method stub
		c.save();
		c.translate(-x, 0);
		c.translate(Cnt.cellSize*2, textSize+Cnt.cellSize*3);
		
			CView.p.reset();
			CView.p.setTextSize(textSize);
			CView.p.setColor(Cnt.timerColor);
			CView.p.setTypeface(Typeface.MONOSPACE);
			
			if(getElapsed()<fadeTime){
				
				float normalizedProgress = getElapsed()/fadeTime;
				CView.p.setAlpha(Math.round(255*World.accelerator(normalizedProgress, sigmoidParameter)));
				
			} else if(getDuration()-getElapsed()<fadeTime){
				
				float normalizedProgress = (getDuration()-getElapsed())/fadeTime;
				CView.p.setAlpha(Math.round(255*World.decelerator(normalizedProgress, sigmoidParameter)));
				
			}
			
			c.drawText(String.format("%d:", (getDuration()-getElapsed())/1000), 0, 0, CView.p);
			Rect b = new Rect();
			CView.p.getTextBounds("00", 0, 2, b);
			c.translate((b.right-b.left)*1.1f, -textSize/2);
			CView.p.setTextSize(textSize/2.0f);
			c.drawText(String.format("%02d", ((getDuration()-getElapsed())%1000)/10), 0, 0, CView.p);
		c.restore();
	}
	
}

class StatusUI {
	
	static int barsXPadding = 10;
	static int barsYPadding = 10;
	
	static float lifeBarWidth = 60;
	
	static float lifeBarHeight = lifeBarWidth/3f;
	
	static RectF lifeBarBack = new RectF(0,
			0,
			lifeBarWidth,
			lifeBarHeight);
	
	// relativeHelath равен отношению HP к максимально возможному значению HP
	public static void drawLifebar(Canvas c, float relativeHelath, int color){
		
		CView.p.reset();
		CView.p.setColor(0xFF223232);
		c.drawRect(lifeBarBack, CView.p);
		
		if(relativeHelath>0){
			RectF b = new RectF(lifeBarBack);
			b.left+=lifeBarHeight/5f;
			b.top+=lifeBarHeight/10f;
			b.right=b.left+Math.round(relativeHelath*(lifeBarWidth-lifeBarHeight/2.5f));
			b.bottom-=lifeBarHeight/10f;
			CView.p.setColor(color);
			c.drawRect(b, CView.p);
		}
		
		CView.p.setColor(Color.WHITE);
		
		CView.p.setXfermode(CView.screenXfermode);
		CView.p.setAlpha(140);
		
		c.drawRect(lifeBarHeight/10f,lifeBarHeight*0.2f, 
				lifeBarWidth-lifeBarHeight/10f, 
				lifeBarHeight*0.3f+lifeBarHeight/5f, 
				CView.p);
		
		CView.p.setAlpha(90);
		c.drawRect(lifeBarHeight/10f, 
				lifeBarHeight*0.75f, 
				lifeBarWidth-lifeBarHeight/10f, 
				lifeBarHeight*0.90f, 
				CView.p);
	}

	public static void draw(Canvas c){
		
		c.save();
			CView.p.reset();
			c.translate(barsXPadding, Grid.blValToPixVal(Grid.dimen)[1]+barsYPadding);
			drawLifebar(c,World.rivalSoldier.getRelativeHP(),World.rivalSoldier.lifebarColor);
			c.translate(0, barsYPadding*2.5f);
			drawLifebar(c,World.rivalSoldier.getRelativeHP(),World.hero.lifebarColor);
		c.restore();
		
	}
	
}