package com.android.detonate;

import android.graphics.Canvas;
import android.graphics.Color;

public class CyanSplashEffect extends AnimatedVisual {

	final float maxR = 400;
	
	public CyanSplashEffect() {
		// TODO Auto-generated constructor stub
		setDuration(150);
	}
	
	@Override
	public void draw(Canvas canvas) {
		// TODO Auto-generated method stub
		CView.p.setColor(0xFF066580);
		CView.p.setXfermode(CView.screenXfermode);
		CView.p.setAlpha((int) (getProgress()*230));
		canvas.drawCircle(x, y, maxR*getProgress(), CView.p);

	}

}
