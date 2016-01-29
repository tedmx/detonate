package com.android.detonate;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

 public class Msg {
	
	
	static String txt = new String();
	
	static void logcat(String input){
		Log.i(Cnt.LOG,input);
	}
	
	static void onDraw(Canvas canvas, Paint paint){
		
		if(txt.length()==0) return;
		
		paint.reset();
		paint.setColor(Color.WHITE);
		paint.setTextSize(Cnt.MSG_TEXT_SIZE);

		float xLeftOffset = 40,
			  xRightOffset = xLeftOffset;
		float topOffset = 40;
		
		int remainingChars = txt.length(),
			index=0,
			stroke=1,
			charsToDraw, correctedCTD;
		
		while(remainingChars>0){
			
			charsToDraw = paint.breakText(txt.substring(index, index+remainingChars), 
						true, 
						canvas.getWidth()-(xLeftOffset+xRightOffset), 
						null);
			 
			 correctedCTD = charsToDraw;
			
			if(
					txt.length()>index+charsToDraw
					 && txt.charAt(index+charsToDraw)!=' ' 
						&& txt.substring(index, index+charsToDraw).contains(" ")){
				
				correctedCTD = txt.substring(index, index+charsToDraw).lastIndexOf(" ")+1;
				
			}
			
			canvas.drawText(txt.substring(index, index+correctedCTD), 
							xLeftOffset, 
							topOffset+stroke*paint.getTextSize(), 
							paint);
			
			index+=correctedCTD;
			remainingChars -= correctedCTD;
			stroke++;
			
		}
		
	}
	
}
