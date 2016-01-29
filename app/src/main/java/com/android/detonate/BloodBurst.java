package com.android.detonate;

import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Xfermode;

public class BloodBurst extends AnimatedVisual {

	int randomX, randomY;
	
	public BloodBurst() {
		setDuration(5*Cnt.DT);
	}
	
	@Override
	public void restart(){
		super.restart();
		randomX=(int)(Cnt.cellSize/2*(World.generator.nextInt(100)-50)/50.0f);
		randomY=(int)(Cnt.cellSize/2*(World.generator.nextInt(100)-50)/50.0f);
	}
	
	@Override
	public void draw(Canvas canvas) {
		
		int size = (int)(Cnt.cellSize*getProgress());
		CView.p.setXfermode(CView.screenXfermode);
		CView.p.setColor(Color.argb(192,255,0,0));
		canvas.drawCircle(randomX, randomY, size, CView.p);

	}

}
