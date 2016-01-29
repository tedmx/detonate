package com.android.detonate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import android.graphics.Canvas;
import android.graphics.Color;

class Tracer {
	LinkedList<int[]> history = new LinkedList<int[]>();
	boolean hasTurnTicket = true;
	int jumpPower = 0;
	boolean haveAirShifts = false;
	int curBlX;
	int curBlY;
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return '['+String.valueOf(curBlX)+", "+String.valueOf(curBlY)+']';
	}
	
	static Tracer getStartTracer(int[] blCrds, int[] goalCrds) {
		Tracer t = new Tracer();
		t.history.add(blCrds.clone());
		t.curBlX = blCrds[0];
		t.curBlY = blCrds[1];
		return t;
	}

	private void initiateJump() {
		jumpPower = 1;
		haveAirShifts = true;
		curBlY--;
	}

	protected Tracer clone() {
		Tracer t = new Tracer();
		t.history = (LinkedList<int[]>) history.clone();
		t.jumpPower = jumpPower;
		t.haveAirShifts = haveAirShifts;
		t.curBlX = curBlX;
		t.curBlY = curBlY;
		return t;
	}

	private boolean inAir() {
		if(curBlY+2==Grid.dimen[1] ) return false;
		else return !Grid.fill[curBlX][curBlY + 2].isPlatform() 
						&& !Grid.fill[curBlX + 1][curBlY + 2].isPlatform();
	}

	void continueJump() {
		if (inAir()) {
			if (jumpPower <= 0) {
				curBlY++;
			} else {
				curBlY--;
				jumpPower--;

			}
		}
	}

	boolean canShift(Dir dir) {
		int[] futCrds = {curBlX + dir.numericalValue(), curBlY};
		return Grid.areValidWarrBlCrds(futCrds[0], futCrds[1]);
	}

	boolean canGoUp() {
		return Grid.areValidWarrBlCrds(curBlX, curBlY-1);
	}

	int blXInFrontOfWarr(Dir dir) {
		if (dir == Dir.LEFT) {
			return curBlX - 1;
		} else if (dir == Dir.RIGHT) {
			return curBlX + 2;
		}
		return 0;
	}

	void shift(Dir dir) {
		
		Tracer t = this.clone();
		t.haveAirShifts = false;
		int shiftedBlX = curBlX+dir.numericalValue();
		t.curBlX = shiftedBlX;
		t.pushStateInHistory();
		t.hasTurnTicket = false;
		PathFinding.tracerList.add(t);
	}

	
	public void pushStateInHistory(){
		history.add(new int[] {curBlX,curBlY});
	}
	
	/*
	 * Трэйсер пользуется первой доступной возможностью передвинуться (влево,
	 * вправо или вверх). Программа обеспечивает каждое возможное 
	 * направление движения собственным трейсером, 
	 * сдвинутым в этом направлении относительно первоначально данного трейсера.
	 */
	public void progress() {

		if (inAir()) {
			if (haveAirShifts) {
				if (canShift(Dir.LEFT)) shift(Dir.LEFT);
				if (canShift(Dir.RIGHT)) shift(Dir.RIGHT);
			} 
			continueJump();
			pushStateInHistory();
			
		} else {
			if (jumpPower > 0) {
				jumpPower = 0;
			}
			if (canShift(Dir.LEFT)) shift(Dir.LEFT);
			if (canShift(Dir.RIGHT)) shift(Dir.RIGHT);
			if (canGoUp()) {
				/*
				 * Важная деталь: эта функция вызывается только тогда, когда из
				 * всех возможностей передвижения остается только прыжок вверх.
				 * Поэтому я не произвожу клонирование трейсера внутри функции.
				 **/
				initiateJump();
				pushStateInHistory();
			}
			
		}
		hasTurnTicket = false;
	}

	public boolean touches(int[] goalBlCrds) {
		return (goalBlCrds[0] == curBlX || goalBlCrds[0] == curBlX + 1)
				&& (goalBlCrds[1] == curBlY || goalBlCrds[1] == curBlY + 1);
	}
	
	public boolean isVisiting(int[] blCrds){
		return  blCrds[0] == curBlX && blCrds[1] == curBlY;
	}
	
	// Посетил только в прошлом
	public boolean visitedInThePast(int[] blCrds) {
		for (int i=0; i<history.size()-1; i++) {
			int[] visitedCrds = history.get(i);
			if (visitedCrds[0] == blCrds[0] && visitedCrds[1] == blCrds[1]) {
				return true;
			}
		}
		return false;
	}

	// Посетил в прошлом или сейчас находится в указанном месте
	public boolean hasVisited(int[] blCrds){
		return isVisiting(blCrds) || visitedInThePast(blCrds);
	}

	public boolean othersHasVisited(int[] blCrds) {
		for (Iterator<Tracer> i = PathFinding.tracerList.iterator(); i
				.hasNext();) {
			Tracer t = i.next();
			if (t!=this && t.hasVisited(blCrds)){
				return true;
			}
		}
		return false;
	}
	
	public boolean repeatsOthers() {
		return othersHasVisited(new int[] {curBlX, curBlY});
	}

	public void takeTurnTicket() {
		// TODO Auto-generated method stub
		hasTurnTicket = true;
	}
	

}

class PathFinding {

	static public List<Tracer> tracerList = new ArrayList<Tracer>();

	enum GraphDir {LEFT, RIGHT, UP, DOWN;
	
		static GraphDir getFromPoints(float[]start, float[]end){
			
			if(start==null || end==null) return null;
			
			if(start[0]>end[0] && start[1]==end[1]){
				return LEFT;
			} else if(start[0]<end[0] && start[1]==end[1]){
				return RIGHT;
			} else if(start[0]==end[0] && start[1]>end[1]){
				return UP;
			} else if(start[0]==end[0] && start[1]<end[1]){
				return DOWN;
			}   
			return null;
		}
	}
	
	public static void drawPath(List<int[]> al, Canvas c) {
		if (al == null)
			return;
		
		CView.p.reset();
		float dotRadius = Cnt.cellSize * 0.3f;
		CView.p.setStrokeWidth(2);
		CView.p.setColor(Cnt.PATH_COLOR);
		
		float[] currentPoint = Grid.getPxCenterOfBl(al.get(0));
		float[] nextPoint = null;
		
		c.drawCircle(currentPoint[0], currentPoint[1], dotRadius, CView.p);
		for (int i = 1; i < al.size(); i++) {
			
			nextPoint = Grid.getPxCenterOfBl(al.get(i));
			c.drawLine(currentPoint[0], currentPoint[1], nextPoint[0],nextPoint[1], CView.p);
			currentPoint = nextPoint;
			c.drawCircle(currentPoint[0], currentPoint[1], dotRadius, CView.p);
			
		}
	}
	

	public static List<int[]> getPathFromWarriorToPoint(int[] wBlCrds,
			int[] goalBlCrds) {

		tracerList.clear();
		tracerList.add(Tracer.getStartTracer(wBlCrds, goalBlCrds));

		/* Если система ищет путь больше maxIterations шагов,
		 * обрываем расчеты. */
		final int maxIterations = 200;
		
		for (int computationalStep = 0; computationalStep < maxIterations; computationalStep++) {
			Tracer currentTracer;
			/* В начале каждого шага наделяем 
			 * каждый существующий трейсер правом хода. */
			for (Tracer t : tracerList) {
				t.takeTurnTicket();
			}
			for (int indexInTracerList = 0; 
						indexInTracerList < tracerList.size(); 
						indexInTracerList++ ) {
				
				currentTracer = tracerList.get(indexInTracerList);
				if(currentTracer.touches(goalBlCrds)){
					return  currentTracer.history;
				}
				if (currentTracer.repeatsOthers()){
					tracerList.remove(currentTracer);
					indexInTracerList--;
					continue;
				}
				
				if(currentTracer.hasTurnTicket){
					/* Здесь трейсер может создать своих клонов.
					 * С помощью проверки строкой выше 
					 * запретим клонам действовать
					 * в ходе, в котором их породил
					 * другой трейсер.
					 */
					currentTracer.progress();
				}
			}
		}
		return null;

	}

	public static ArrayList<int[]> getDumbPathFromWarriorToPoint(int[] wBlCrds,
			int[] goalBlCrds) {
		ArrayList<int[]> al = new ArrayList<int[]>();
		int modPBCY = Grid.goodLandingBlY(goalBlCrds[0], goalBlCrds[1]);
		if (modPBCY == -1)
			return null;
		al.add(wBlCrds);
		al.add(new int[] { goalBlCrds[0], wBlCrds[1] });
		al.add(new int[] { goalBlCrds[0], modPBCY });
		return al;
	}

}
