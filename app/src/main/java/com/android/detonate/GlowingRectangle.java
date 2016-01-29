package com.android.detonate;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader.TileMode;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

public class GlowingRectangle extends AnimatedVisual {

	LinearGradient glowingColumnGradient;
	
	public GlowingRectangle(){
		
		setDuration(90*Cnt.DT);
		infinite = true;
		
		glowingColumnGradient = new LinearGradient(0, 0, 0, Grid.blValToPixVal(Grid.dimen)[1], 
				Cnt.glowingColumnColor,Color.argb(0,0,0,0), 
				 TileMode.CLAMP);
		
	}
	
	@Override
	public void draw(Canvas canvas) {
		
		CView.p.setShader(glowingColumnGradient);
		
		float progress = (float) ((Math.sin(MainThread.counter/14.0f)+1)*.5f);
				
		CView.p.setAlpha((int)(100+155.0f*progress));
		
		canvas.drawRect(World.goalDestination*Cnt.cellSize, 0, 
						(World.goalDestination+1)*Cnt.cellSize, Grid.blValToPixVal(Grid.dimen)[1], CView.p);
	}

}
