package com.android.detonate;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class Splash extends AnimatedVisual {
	
	Splash(){
		setDuration(8*Cnt.DT);
	}
	
	public void draw(Canvas canvas){
		
			int filler =  (int)((float)Cnt.cellSize*getProgress())/4;
			CView.p.setColor(Color.rgb(255,252,232));
			CView.p.setAlpha((int)((float)255*(1-getProgress())));
			
			canvas.drawRect(x+Cnt.cellSize/4-filler, 
							y+Cnt.cellSize/4-filler, 
							x+3*Cnt.cellSize/4+filler, 
							y+3*Cnt.cellSize/4+filler, CView.p);
		
	}
	
}
