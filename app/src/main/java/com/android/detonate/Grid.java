package com.android.detonate;

import java.util.Iterator;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

 class Grid {
	
	static int dimen[];
	static TileType fill[][];
	static private int color[][];   
	static private final int padding = 1;
	
	private static Animator a;
	
	 enum TileType {
		EMPTY, FLUFF, STONE, JUMPABLE, WARRIOR;
		
		boolean isPlatform(){
			if(this==STONE || this==FLUFF || this==JUMPABLE){
				return true;
			}
			return false;
		}
		
		boolean isObstacle(){
			if(this==STONE || this==FLUFF){
				return true;
			}
			return false;
		}
	}
	
	 static void init(int width, int height){
		
		a = new Animator(10*Cnt.DT);
		a.infinite = true;
		a.al = new AnimatorListener() {
			
			 public void end() {
				for (int i = dimen[0]-1; i >-1 ; i--) 
					for (int j = dimen[1]-2; j > -1; j--) 
						
						if(fill[i][j+1]==TileType.EMPTY 
						   && (Cnt.blocksAreFalling || fill[i][j]==TileType.FLUFF) 
						){
						
							int tempColor = color[i][j];
							color[i][j] = color[i][j+1];
							color[i][j+1] = tempColor;
							
							TileType tempContent = fill[i][j];  
							fill[i][j]=fill[i][j+1];
							fill[i][j+1]=tempContent;
						}
				
			}
		};
		a.restart();
		dimen = new int[2];
		
		resize(width,height);
	}
	
	
	private static void resize(int w, int h){
		
		
		dimen[0] = w;
		dimen[1] = h;
		
		fill = new TileType [dimen[0]][dimen[1]];
		color = new int [dimen[0]][dimen[1]];
		
		clear();
		
		TouchMsgBuf.updateHinderShiftBorders();
	}
	
	 private static void clear(){
		for (int i = 0; i < dimen[0]; i++) {
			for (int j = 0; j < dimen[1]; j++) {
				fill[i][j] = TileType.EMPTY;
				color[i][j] = Color.CYAN;
			}
		}
	}
	
	 
	 
	static int[] getRandomValidWarriorBlCrds(){
		
		final int maxTryCount = 150;
		int testX, testY;
		
		for(int i=0; i<maxTryCount; i++){
			
			testX = World.generator.nextInt(dimen[0]-1);
			testY = World.generator.nextInt(dimen[1]-1);
			
			if(areValidWarrBlCrds(testX,testY)) return new int[]{testX,testY};
		}
		
		return new int[] {-1,-1};
	}
	
	static boolean areValidWarrBlCrds(int blX, int blY){
		return  blockExistsAt(blX, blY) 
				&& blockExistsAt(blX+1, blY+1) 
				&& !fill[blX][blY].isObstacle()
				&& !fill[blX+1][blY+1].isObstacle()
				&& !fill[blX+1][blY].isObstacle()
				&& !fill[blX][blY+1].isObstacle();
	}
	
	static int[] lowestValidWarriorBlCrds(int blX){
		for (int blY = dimen[1]-2; blY >-1; blY--) {
			
			if(areValidWarrBlCrds(blX,blY)){
				return new int[] {blX, blY};
			}
		}
		return new int[] {-1, -1};
	}
	
	private static void colorize(){
		
		float proportion;
		
		float startColor[] = new float[3];
		float endColor[] = new float[3];
		
		Color.colorToHSV(Cnt.tilesStartColor, startColor);
		Color.colorToHSV(Cnt.tilesEndColor, endColor);
		
		for (int i = 0; i < dimen[0]; i++) {
			for (int j = 0; j < dimen[1]; j++) {
				if(fill[i][j] == TileType.FLUFF){
					
					proportion = (float) Math.random();
					color[i][j] = Color.HSVToColor(new float[]{proportion*startColor[0]+(1-proportion)*endColor[0],
															   proportion*startColor[1]+(1-proportion)*endColor[1],
															   proportion*startColor[2]+(1-proportion)*endColor[2]});
				} else if (fill[i][j] == TileType.STONE) {
					color[i][j] = Cnt.stoneTileColor;
				} else if (fill[i][j] == TileType.JUMPABLE) {
					color[i][j] = 0xFFFF8FA3;
				}
			}
		}
	}
	
	 static void randomFill(int startCol, int endCol, int height){
		
		int lastColumnSolidBlocks = 0;
		
		for (int blockX = startCol; blockX <= endCol; blockX++) {
			
			int currentColumnSolidBlocks = 0;
			
			for (int blockY = dimen[1]-height; blockY < dimen[1]; blockY++) {
				
				int dice = World.generator.nextInt(10);
				
				if(dice>6){
					
					fill[blockX][blockY] = TileType.FLUFF;
				} else if (dice == 6 && currentColumnSolidBlocks-lastColumnSolidBlocks<2) {
					
					fill[blockX][blockY] = TileType.STONE;
					currentColumnSolidBlocks++;
				}
			}
			lastColumnSolidBlocks = currentColumnSolidBlocks;
		}
		colorize();	
	}
	
	 static void fill(Bitmap inp) {
		
		clear();
		if(inp.getWidth()!=dimen[0] || inp.getHeight()!=dimen[1])
			resize(inp.getWidth(),inp.getHeight());
		for(int i = 0; i < inp.getWidth(); i++)
			for(int j = 0; j < inp.getHeight(); j++)
				if(inp.getPixel(i, j)==Color.BLACK){
					fill[i][j] = TileType.STONE;
				} else if (inp.getPixel(i, j)==Color.GRAY){
					fill[i][j] = TileType.FLUFF;
				} else if (inp.getPixel(i, j)==Color.RED){
					fill[i][j] = TileType.JUMPABLE;
				}
		colorize();
		
		
	}
	
	
	private static void drawDotGrid(Canvas canvas){
		
		
		CView.p.setColor(Color.DKGRAY);
		
		canvas.save();
		
		canvas.translate(-TouchMsgBuf.getScaledViewportXShift()
				+TouchMsgBuf.getScaledViewportXShift()%Cnt.cellSize, 0);
		
		for (int i = 0; i <= dimen[0]; i++) {
			for (int j = 0; j <= dimen[1]; j++) {
				canvas.drawRect(Cnt.cellSize*i-padding,Cnt.cellSize*j-padding,
						Cnt.cellSize*(i)+padding,Cnt.cellSize*(j)+padding, CView.p);
			}
			
		}
		
		canvas.restore();
		
	}
	
	static  void draw(Canvas canvas){
		
		CView.p.reset();
		
		
		for (int i = 0; i < dimen[0]; i++) {
			for (int j = 0; j < dimen[1]; j++) {
				
				
				if(fill[i][j]!=TileType.EMPTY){
					CView.p.setColor(color[i][j]);
					
					canvas.drawRect((Cnt.cellSize)*i+padding, 
									(Cnt.cellSize)*j+padding,
									(Cnt.cellSize)*i+Cnt.cellSize-padding, 
									(Cnt.cellSize)*j+Cnt.cellSize-padding,
									CView.p);
				} 
			}
			
		}
		
		MainThread.startTimer("dotGrid");
		drawDotGrid(canvas);
		MainThread.postTime();
		
		
	}
	
	/**
	 * 
	 * @return Ординату для данного blX или (blX-1) такую, 
	 * что на ней сможет поместиться и стоять Warrior.
	 */
	static int warriorReachableBlY(int blX, int blY){
		blX = autoFixBlX(blX);
		return blY = autoFixBlY(blY);
 	}
	/**
	 * 
	 * @return Если подан пустой блок, то возвращает ординату 
	 * последнего пустого под ним (возможно, что его самого).
	 * Если подан заполненный блок, то возвращает ординату первого пустого над ним.
	 */
	static int goodLandingBlY(int blX, int blY){
		
		blX = autoFixBlX(blX);
		blY = autoFixBlY(blY);
		
		if(fill[blX][blY]==TileType.STONE || fill[blX][blY]==TileType.FLUFF)
			return firstFreeBlYAbove(blX, blY);
		else 
			return topOfTheSubcolumn(blX, blY)-1;
	}
	
	/** 
	 * @return Первый свободный блок в колонне blX над 
	 * рядом твердых блоков, в которых содержится блок с ординатой blY; возвращает
	 * -1, если ряд твердых блоков продолжается до потолка.
	 */
	static int firstFreeBlYAbove(int blX, int blY){
		
		blX = autoFixBlX(blX);
		blY = autoFixBlY(blY);
		
		for(int curBlY = blY; curBlY>-1; curBlY--){
			if(fill[blX][curBlY]==TileType.EMPTY || fill[blX][curBlY]==TileType.JUMPABLE) {
				return curBlY;
			}
		}
		
		return -1;
	}
	
	/** Возвращает самый высокий твердый блок в колонне blX, 
	 * расположенный не выше upperBorderBlY. **/
	static int topOfTheSubcolumn(int blX, int maxBlY){
		
		blX = autoFixBlX(blX);
		maxBlY = autoFixBlY(maxBlY);
		
		TileType startingBlockType = fill[blX][maxBlY];
		
		for(int i = maxBlY; i<dimen[1]; i++){
			if((startingBlockType==TileType.EMPTY && fill[blX][i]!=TileType.EMPTY) 
			   ||(startingBlockType==TileType.JUMPABLE && fill[blX][i]==TileType.STONE)){
				return i;
			}
		}
		
		return dimen[1];
	}
	
	
	/** Возвращает самый верхний твердый блок в колонне. **/
	static  int topOfTheColumn(int blockX){
		return topOfTheSubcolumn(blockX,0);
	}
	
	static  int topOfTwoAdjasCols(int leftColBlX){
		return Math.min(topOfTheColumn(leftColBlX), topOfTheColumn(leftColBlX+1));
	}
	
	static  TileType getBlType (int[] blCrds){
		return fill[blCrds[0]][blCrds[1]];
	}
	
	static  Warrior getWarrior (int blX, int blY){
		
		for (Warrior w : World.warriors) {
			
			int[] bc = w.getBlCrds();
			
			if(
				   (blX==bc[0] || blX==bc[0]+1) 
				&& (blY==bc[1] || blY==bc[1]+1)
   			){
					return w;
			}
		}
		return null;
	}
	
	 static boolean testAttack(int x, int y, Dir direction, Weapon weapon, Warrior rival){
		
		Boolean[] willHarmRivalDst = {false};
		launchAttackBackbone(x,y,direction,weapon,rival,willHarmRivalDst,true);
		return willHarmRivalDst[0];
		
	}
	
	/** Большая абстрактная функция, которая
	 * 1) возвращает информацию о том, ранен ли rival || бьёт rival,
	 * 2) не бьёт препятствия || бьёт препятствия
	 * при значениях test соответственно true и false.
	 */
	 private static void launchAttackBackbone(int x, int y, 
			 									Dir direction, 
			 									Weapon weapon, 
			 									Warrior rival,
												Boolean[] rivalWillBeHarmed,
												boolean test){
		
		int curBlockCrds[] = new int[2];
		int penetratedCount;
		for (int i = 0; i < 2 ; i++) {
			
			curBlockCrds[1] = y+i;
			penetratedCount = 0;
			
			for (int j = 0; j < weapon.getRange(); j++) {
				   
				curBlockCrds[0] = x+j*direction.numericalValue();    
				
				// Выход за пределы поля
				if(!isValidBlX(curBlockCrds[0])) break;
				
				Warrior victim = getWarrior(curBlockCrds[0],curBlockCrds[1]);
				
				if(victim!=null){ 
					
					if(test){
						if( victim==rival){
							rivalWillBeHarmed[0] = true;
							return;
						}
					} else {
						victim.takeDamage(weapon.getPower(),direction);
						break;
					}
					
				} else if (fill[curBlockCrds[0]][curBlockCrds[1]] == TileType.STONE){
					
					break;
					
				} else if (fill[curBlockCrds[0]][curBlockCrds[1]] == TileType.FLUFF){  
					
					if(penetratedCount++>weapon.getPenetrability()) break;
					
					if(!test){
						fill[curBlockCrds[0]][curBlockCrds[1]]=TileType.EMPTY;
						
						AVContainer.addAndStart(new Splash().restart(curBlockCrds[0]*Cnt.cellSize, curBlockCrds[1]*Cnt.cellSize),
												Grounds.BACK);
					} else {
						break;
					}
					
					
				}
				
			}
			
		}
	
		if(test) rivalWillBeHarmed[0] = false;
	}
	
	 static void launchAttack(int x, int y, Dir direction, Weapon weapon){
		
		launchAttackBackbone(x,y,direction,weapon,null,null,false);
		
	}
	
	 static boolean subrawIsFree(int y, int xStart, int xEnd ){
		for(int x=xStart; x<xEnd; x++){
			if (fill[x][y]!=TileType.EMPTY) return false;
		}
		return true;
	}
	
	 static boolean isValidBlX(int x){
		
		return x>=0 && x<dimen[0];
	}
	 
	 static int autoFixBlX(int x){
		 if(x<0) return 0;
		 if(x>=dimen[0]) return dimen[0]-1;
		 return x;
	 }
	 
	 static int autoFixBlY(int y){
		 if(y<0) return 0;
		 if(y>=dimen[1]) return dimen[1]-1;
		 return y;
	 }
	
	 static boolean blockExistsAt (int x, int y){ 
		return isValidBlX(x) && y>=0 && y< dimen[1];
	}


	static int[] blValToPixVal(int[] a){
		int[] b = new int[a.length];
		for (int i = 0; i < b.length; i++) {
			b[i] = a[i]*Cnt.cellSize;
		}
		return b;
	}


	public static float[] getPxCenterOfBl(int[] blCrds){
		return new float[] {(blCrds[0]+0.5f)*Cnt.cellSize, (blCrds[1]+0.5f)*Cnt.cellSize};
	}
	
}
