package com.android.detonate;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.Button;

public class PlainButton extends Button {

	static public Animator opacityAn;
	boolean chosen;
	boolean lastWasPointerDown;
	
	public PlainButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		if(event.getAction()==MotionEvent.ACTION_DOWN && !lastWasPointerDown){
			
			lastWasPointerDown = true;
			
			if(chosen){
				World.playerBenefit();
				
				int[] b = new int[2];
				getLocationInWindow(b);
				b[1]-=CView.yOffset;
				b[0]+=this.getWidth()/2.0f;
				b[1]+=this.getHeight()/2.0f;
				
				CyanSplashEffect a = new CyanSplashEffect();
				a.restart(b[0], b[1]);
				AVContainer.add(a,Grounds.FORE);
				
			} else {
				World.enemyBenefit();
				
			}
				
		} else {
			lastWasPointerDown = false;
		}
		
		return super.onTouchEvent(event);
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
				
		// TODO Auto-generated method stub
		canvas.drawColor(Color.BLACK, android.graphics.PorterDuff.Mode.CLEAR);
		Paint a = new Paint();
		a.setColor(isPressed() ? 
						0xFFC2A653 : 
						(chosen ? Color.WHITE : Color.YELLOW)
				   );  
		
		a.setAlpha((int) ((1-opacityAn.getProgress())*255));
	
		canvas.drawRect(0, 0, getHeight(), getWidth(), a);
		a.setColor(Color.BLACK);
		
		a.setAlpha((int) ((1-opacityAn.getProgress())*255));
		
		a.setTextSize(80);
		canvas.drawText((String) getText(),30,getHeight()-30,a);
	}

}
