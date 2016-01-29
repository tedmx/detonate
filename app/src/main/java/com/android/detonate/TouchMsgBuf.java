package com.android.detonate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.Toast;



class TouchMsgBuf {
	
	static private List<MotionEvent> msgStack = new ArrayList<MotionEvent>();
	
	static private int lastEvent = KeyEvent.ACTION_UP;
	// Для корректной работы при записи необходимо использовать сеттер.
	static private float startDrag[] = new float[2];
	static private float persistentShift[] = new float[2];
	
	static private float[] rawShift = new float[2];
	/**Значение сдвига с учетом замедления на краях.**/
	static private float[] viewportShift = new float[2];
	
	/** Диапазон, в котором смещение экрана
	 *  не будет каким-либо образом преобразовано. **/ 
	static private float[] unhinderedShiftBorders = {0,0};
	
	static boolean havingJob(){
		return lastEvent!=MotionEvent.ACTION_UP;
	}
	
	static public void resetShifts(){
		viewportShift[0]=0;
		rawShift[0]=0;
	}
	
	static private void execMsg(MotionEvent e){

		switch (e.getAction()){
		
		case MotionEvent.ACTION_UP:
			
			actUPResponse(e);
			break;
		
		case MotionEvent.ACTION_DOWN:
			
			actDWNResponse(e);
			break;

		case MotionEvent.ACTION_MOVE:
			
			if(lastEvent==KeyEvent.ACTION_UP)
				actDWNResponse(e);
			else
				actMULResponse(e);
			break;
			
		 default:
			Msg.logcat(String.valueOf(e.getAction()));
		}
	}
	
	static public void updateHinderShiftBorders(){
		unhinderedShiftBorders[1] = (Grid.blValToPixVal(Grid.dimen)[0]-Cnt.canonicalGridSize[0]*Cnt.cellSize)*CView.scalingFactor;  
	}
	
	static public void setRawShift(float inp){
		 
		rawShift[0] = inp;
		
		if(rawShift[0]>=unhinderedShiftBorders[0] && rawShift[0]<=unhinderedShiftBorders[1]){ 
			viewportShift[0] = rawShift[0]; return;
		} 
		
		float hinderedX = 0;
		if(rawShift[0]<unhinderedShiftBorders[0]){
			
			hinderedX = rawShift[0]-unhinderedShiftBorders[0];
			viewportShift[0]=unhinderedShiftBorders[0];
			
		} else if(rawShift[0]>unhinderedShiftBorders[1]){
			
			hinderedX = rawShift[0]-unhinderedShiftBorders[1];
			viewportShift[0]=unhinderedShiftBorders[1];
			
		}
		
		float normalizedX = (hinderedX+Cnt.maxRawShift)/Cnt.maxRawShift/2; 
		float sigmoidY =  World.sigmoid(normalizedX, Cnt.viewportShiftSigmoidParameter);
		viewportShift[0] += (sigmoidY-0.5f)*2*Cnt.maxVisualShift;
		
		
		
	}
	
	static public void setRawShiftWithinBorders(float inp){
		
		float screenWidth = Cnt.canonicalGridSize[0]*Cnt.cellSize;
		float leftBorder = 0;
		float rightBorder = Grid.dimen[0]*Cnt.cellSize;
		
		float shiftedInp = 0;
		if(inp<screenWidth/2.0f){
			shiftedInp = leftBorder;
		} else if(inp>rightBorder-screenWidth/2.0f){
			shiftedInp = rightBorder-screenWidth;
		} else shiftedInp = inp-screenWidth/2.0f;
		
		setRawShift(shiftedInp*CView.scalingFactor);
	}
	
	static private float getRawShiftFromVisual(float inp){
		
		if(inp>unhinderedShiftBorders[0] && inp<unhinderedShiftBorders[1]){ 
			return inp;  
		}
		
		float hinderedY = 0;
		
		float finalX = 0;
		if(inp<=unhinderedShiftBorders[0]){
			hinderedY = inp-unhinderedShiftBorders[0];
			finalX = unhinderedShiftBorders[0];
		} else if(inp>=unhinderedShiftBorders[1]){
			hinderedY = inp-unhinderedShiftBorders[1];
			finalX = unhinderedShiftBorders[1];
		}
		
		float normalizedY = (hinderedY+Cnt.maxVisualShift)/(Cnt.maxVisualShift*2);
		
		float sigmoidX = World.sigmoidInverse(normalizedY, Cnt.viewportShiftSigmoidParameter);
		
		return finalX+(sigmoidX-0.5f)*2*Cnt.maxRawShift;
	}
	
	static public void applyReturningShift(){
		
		float backStep = Cnt.maxVisualShift/20.0f; 
		if(!havingJob()){ 
			if(rawShift[0]-unhinderedShiftBorders[1]>backStep) setRawShift(getRawShiftFromVisual(viewportShift[0]-backStep));
			else if(rawShift[0]>unhinderedShiftBorders[1]) setRawShift(unhinderedShiftBorders[1]);
			else if(rawShift[0]-unhinderedShiftBorders[0]<-backStep ) setRawShift(getRawShiftFromVisual(viewportShift[0]+backStep));
			else if(rawShift[0]<unhinderedShiftBorders[0]) setRawShift(unhinderedShiftBorders[0]);
		}
	}
	
	static private void actUPResponse(MotionEvent e){
		
		if(lastEvent == MotionEvent.ACTION_UP) return;
		
		lastEvent = MotionEvent.ACTION_UP;
		
		Pointer.touchUpReaction();
		
		return;
	}
	
	static private void actDWNResponse(MotionEvent e){
		
		if(lastEvent == MotionEvent.ACTION_DOWN) return;
		
		lastEvent = MotionEvent.ACTION_DOWN;
		
		persistentShift[0] = rawShift[0];
		startDrag[0] = e.getX();
		
		
		float touchCoords[] = {startDrag[0], getCorrectedY(e)};
		Pointer.touchDownReaction(touchCoords);

	}
	
	static private void actMULResponse(MotionEvent e){
		
		//if(lastEvent!=MotionEvent.ACTION_MOVE) Msg.logcat("MUL");
		
		lastEvent = MotionEvent.ACTION_MOVE;
		
		// Знаки у startDrag[0] и e.getX() обращены, 
		// чтобы положительные значения смещения соответствовали 
		// смещению вправо.
		float futureShift = startDrag[0]-e.getX()+persistentShift[0];
		
		if(Math.abs(futureShift)<Math.abs(rawShift[0])){
			// Если после тугого сдвига движение направленно обратно,
			// то изинг отменяется.
			float rawDifference = futureShift - rawShift[0];
			setRawShift(getRawShiftFromVisual(viewportShift[0]+rawDifference));
			persistentShift[0] = rawShift[0];
			startDrag[0] = e.getX();
		} else {
			if(futureShift>Cnt.maxRawShift+unhinderedShiftBorders[1]) setRawShift(Cnt.maxRawShift+unhinderedShiftBorders[1]);
			else if(futureShift<-Cnt.maxRawShift+unhinderedShiftBorders[0]) setRawShift(-Cnt.maxRawShift+unhinderedShiftBorders[0]);
			else setRawShift(futureShift);
		}
		
		
		Pointer.touchMulReaction(e.getX(),getCorrectedY(e));
		
		

	}
	
	/**Функция вычитает высоту верхних панелей ОС из ординаты точки на экране,
	 * полученной через стандартные вызовы, определяющие положение объектов или точек касания. 
	 * В результате функция приводит ординату в точное соответствие ординате окна приложения.
	 */
	static public float getCorrectedY(MotionEvent e){
		return e.getRawY()-CView.yOffset;
	}
	
	static public void addMsg (MotionEvent event){
		
		synchronized(msgStack){ 
			
			msgStack.add(event);
			
		}
		
	}
	
	static public void execStack(){
		
		synchronized(msgStack){
			
			if(msgStack.size()>0){
				Iterator<MotionEvent> msgStackI = msgStack.iterator();
				
				while(msgStackI.hasNext()){
					execMsg(msgStackI.next());
				}
				
				msgStack.clear();
			}
			
		}
		
	}

	static public float getScaledViewportXShift(){
		// Смена знака нужна для того, чтобы положительные 
		// значения сдвига соответствовали смещению вправо.
		return -viewportShift[0]/CView.scalingFactor;
	}
	
}



 class Pointer{

	 class Point{
		 int x,y;
		 boolean enabled;
		 int alpha;
		 
		 public Point(int alpha) {
			super();
			this.alpha = alpha;
		}

		public void draw(Canvas c) {
				
				CView.p.reset();
				
				CView.p.setColor(Color.WHITE);
				
				if(enabled){
					CView.p.setAlpha(alpha);
					
					c.drawRect(x*Cnt.cellSize, y*Cnt.cellSize, 
								(x+1)*Cnt.cellSize, (y+1)*Cnt.cellSize, CView.p);
				}
				
			}
	 }
	 
	static Point ghost = new Pointer().new Point(128);
	static Point solid = new Pointer().new Point(200);
	
	
	
	/**Абсцисса касания*/
	static float downTouchX,downTouchY;
	/**Максимальное расстояние, на которое можно отвести указатель, не погасив временную колонку. */
	static final float maxDiff = 75;
	
	public static void touchUpReaction() {
		
		if(Pointer.ghost.enabled == true){
			
			Pointer.ghost.enabled = false;
			
			Pointer.solid.x = Pointer.ghost.x;
			Pointer.solid.y = Pointer.ghost.y;
			Pointer.solid.enabled = true;
			
			// Передвигаем блок-указатель в достижимую позицию.
			solid.y = Grid.goodLandingBlY(solid.x, solid.y);
			World.testAL = PathFinding.getPathFromWarriorToPoint(World.hero.getBlCrds(),new int[] {solid.x,solid.y});
			//World.hero.agenda.add(Task.followPointerTask(Pointer.solid.x));
			
		}
		
	}

	public static void touchDownReaction(float[] touchCoords) {
		
		Pointer.downTouchX = touchCoords[0];
		Pointer.downTouchY = touchCoords[1];
		
		CView.inv.mapPoints(touchCoords);
		
		Pointer.ghost.x = Math.round(touchCoords[0])/Cnt.cellSize;
		Pointer.ghost.y = Math.round(touchCoords[1])/Cnt.cellSize;
		Pointer.ghost.enabled = true;
		
	}

	public static void touchMulReaction(float touchX, float touchY) {
		
		// Не слишком ли далеко отведен курсор от позиции нажатия?
		if(Math.abs(touchX-Pointer.downTouchX)>Pointer.maxDiff ||
				Math.abs(touchY-Pointer.downTouchY)>Pointer.maxDiff){
			Pointer.ghost.enabled = false;
		}
		
	}

	public static void draw(Canvas c) {
		
		ghost.draw(c);
		solid.draw(c);
		
	}
	
}
 
