package com.android.detonate;

import java.util.EnumMap;
import java.util.Map;

import android.graphics.Canvas;
import android.graphics.Paint;


enum Grounds {FORE, MIDDLE, BACK};

class AVContainer {
	
	static EnumMap<Grounds, AnimatedVisual[]> cont;

	 static {
		 
		 cont = new EnumMap <Grounds, AnimatedVisual[]>(Grounds.class);
		 
		 for (Grounds a : Grounds.values()) {
			 cont.put(a, new AnimatedVisual[10]);
		}
	 }
	 
	static public void drawAll(Canvas c, Grounds g){
		
		for (int i =0; i<cont.get(g).length; i++) {
			
			if(cont.get(g)[i]!=null)
				
				cont.get(g)[i].cycle(c);
			
		}
	}

	static public void add(AnimatedVisual in, Grounds g){
		
		for (int i =0; i<cont.get(g).length; i++) {
			
			if(cont.get(g)[i]==null || !cont.get(g)[i].isRunning()) {
				
				cont.get(g)[i]=in; return;
			}
		}
	}
	
	static public void addAndStart(AnimatedVisual in, Grounds g){
		
		for (int i =0; i<cont.get(g).length; i++) {
			
			if(cont.get(g)[i]==null || !cont.get(g)[i].isRunning()) {
				
				cont.get(g)[i]=in;
				in.restart();
				return;
			}
		}
	}
	
}


public abstract class AnimatedVisual extends Animator {
	
	int x, y;
	
	public AnimatedVisual() {
		super();
		// TODO Auto-generated constructor stub
	}

	public AnimatedVisual(int duration) {
		super(duration);
		// TODO Auto-generated constructor stub
	}

	public AnimatedVisual restart(int x, int y){     
		restart();  
		this.x = x; 
		this.y = y;
		return this;
	}
	
	public void cycle(Canvas canvas){
		
		if(isRunning()){
			CView.p.reset();
			draw(canvas);
		} 
	}
	
	abstract void draw(Canvas canvas);
	
}

