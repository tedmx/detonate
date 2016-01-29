package com.android.detonate;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

public class GunBurst extends AnimatedVisual {
	
	public boolean visible;
	final static int height = 9;
	final static int width = 10;
	static Path gunBurstPath;
	
	public GunBurst(){
		setDuration(5*Cnt.DT);
		
		gunBurstPath = new Path();
		gunBurstPath.moveTo(0, height/3-getProgress()*5);
		gunBurstPath.lineTo(width, height/3*2-getProgress()*5);
		gunBurstPath.lineTo(0, height-getProgress()*5);
		gunBurstPath.close();
	}
	
	@Override
	public void draw(Canvas canvas) {
		
		CView.p.setColor(Color.rgb(220,240,255));
		
		if(Math.round(getElapsed()/Cnt.DT)%2==1){
			visible = true;
			CView.p.setAlpha(World.randomFloatInRange(64,300));
		} else {
			visible = false;
			CView.p.setAlpha(0);
		}
		
		canvas.save();
		canvas.translate(x, y);
		canvas.translate(0, World.randomFloatInRange(-2, 2));
		canvas.drawPath(gunBurstPath, CView.p);
		canvas.restore();
		
	}

}
