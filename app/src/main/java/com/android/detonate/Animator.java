package com.android.detonate;

import java.util.ArrayList;

interface AnimatorListener{
	
	void end();
}

public class Animator {
	
	/**Таймер будет продвигаться даже во время пауз игры **/
	private boolean running;
	private long duration;
	private int elapsed;
	boolean infinite;
	
	AnimatorListener al;
	
	private static ArrayList<Animator> inss = new ArrayList<Animator>();
	
	public Animator() {
		
		inss.add(this);
		duration = 100;
	}
	
	public Animator(int duration) {
		inss.add(this);
		this.duration = duration;
	}

	static void tickAll(){
		for(Animator a : inss){
			a.tick();
		}
	}

	void tick(){
		
		if(!running) return;
		
		if(elapsed + Cnt.DT>duration){
			
			if(al!=null){ 
				
				al.end();
				// Особый случай: таймер был возобновлен в колбэке end()
				if(elapsed==0) return;
			}
				
			if(infinite) restart();
			else {
				running = false;
			}
		} else {
			elapsed+=Cnt.DT;
		}
	}
	
	public void restart(){
		elapsed = 0;
		running = true;
	}
	
	public void pause(){
		running = false;
	}
	
	public void stop(){
		running = false;
		elapsed = 0;
	}

	Animator restart(long duration){
		restart();
		this.duration = duration;
		return this;
	}

	boolean isRunning() {
		return running;
	}

	float getProgress() {
		return getElapsed()/(float)duration;
	}

	public int getElapsed() {
		return elapsed;
	}

	long getDuration() {
		return duration;
	}

	void setDuration(long duration) {
		this.duration = duration;
	}
	
}
