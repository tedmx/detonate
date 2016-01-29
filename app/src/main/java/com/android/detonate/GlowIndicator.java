package com.android.detonate;

import android.graphics.Canvas;
import android.graphics.Color;

public class GlowIndicator extends AnimatedVisual{

	public GlowIndicator() {
		setDuration(15*Cnt.DT);
		infinite = true;
	}
	
	public void draw(Canvas canvas) {
		int size = (int)(2.0f*Cnt.cellSize*getProgress()/getDuration());
		CView.p.setXfermode(CView.screenXfermode);
		CView.p.setColor(Color.argb((int)(255.0f-255.0f*getProgress()/getDuration()),0,60,220));
		canvas.drawCircle(0, 0, size, CView.p);

	}

}
