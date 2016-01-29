package com.android.detonate;

import java.util.ArrayList;
import java.util.List;

class Task {
	
	private String description;
	private int value;
	private Action action;
	private MoveType movetype;
	
	private Task(String descr){
		description = descr;
	}
	
	private Task() {
		// TODO Auto-generated constructor stub
	}

	public int getValue() {
		return value;
	}
	
	Action getAction() {
		return action;
	}
	
	public MoveType getMovetype() {
		return movetype;
	}
	
	static public Task goToTask(int dest, String descr){
		Task a = new Task(descr);
		a.movetype = MoveType.GOTO;
		a.value = dest;
		return a;
	}
	
	static public Task followPointerTask(int dest){
		Task a = new Task();
		a.movetype = MoveType.POINTER;
		a.value = dest;
		return a;
	}
	
	static public Task doActionTask(String descr,Action ac){
		Task a = new Task(descr);
		a.movetype = MoveType.CUSTOM_ACTION;
		a.action = ac;
		return a;
	}
}

class Agenda{
	
	private List<Task> c = new ArrayList<Task>();
	
	public boolean add(Task t){
		
		if(t.getMovetype() == MoveType.POINTER){  
			
			for (int i = 0; i < c.size(); i++) {
				
				if(c.get(i).getMovetype() == MoveType.POINTER){
					c.set(i, t);
					return true;
				}
			}
		} 
		c.add(t);
		return true;
		
	}

	public void clear() {
		c.clear();
		
	}

	public void remove(Task taskToDealWith) {
		c.remove(taskToDealWith);
	}

	public boolean isEmpty() {
		return c.isEmpty();
	}

	public Task getLast() { 
		return c.get(c.size()-1);
	}

	public boolean contains(Task goalTask) {
		return c.contains(goalTask);
	}
	
}

enum MoveType { GOTO, POINTER, CUSTOM_ACTION}