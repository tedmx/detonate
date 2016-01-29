package com.android.detonate;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.android.detonate.R.drawable;
import com.android.detonate.R.id;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Handler;
import android.view.View;

interface Action{
	abstract void run(Warrior warrior);
}

class World {
	
	static int goalDestination;
	
	private static boolean interactionInProcess;
	private static boolean roundIsOver;
	
	static boolean drawGoalColumn = true;
	static Bitmap[] levelMaps; 
	
	static Random generator;
	static Handler h;
	
	static boolean hasBeenInitialised;
	static Warrior hero;
	static float playerInteractiveScore;
	
	static Runnable redrawIB = new Runnable() {
		public void run() {
			for(PlainButton a : CAct.interactButtons) a.invalidate();
		}
	};
	
	static Warrior rivalSoldier;

	static boolean shallBeginNewRound;
	
	static List<Warrior> warriors;

	public static List<int[]> testAL = new ArrayList<int[]>();

	static float accelerator(float inp, float par){
		return sigmoid(inp/2.0f,par)*2;
	}
	
	static float decelerator(float inp, float par){
		return (sigmoid(inp/2.0f+0.5f,par)-0.5f)*2f;
	}
	
	static void endInteraction(){
		
		Msg.txt = String.valueOf(World.playerInteractiveScore);
		
		if(shouldResumeInteraction()){
			nextRoundOfInteraction(); return;
		} 
		
		PlainButton.opacityAn.pause();
		toggleActionPanel(View.GONE);
		interactionInProcess = false;
		
	}
	
	static void init(){
		
		h = new Handler();
		generator = new Random();
		CView.inv = new android.graphics.Matrix();
		
		CAct.threadView.setFocusableInTouchMode(true);
		
		Grid.init(Cnt.canonicalGridSize[0],Cnt.canonicalGridSize[1]);  
		
		goalDestination = Grid.dimen[0]-1;
		
		warriors = new ArrayList<Warrior>();
		
		testAL.add(0, new int[]{2,2});
		testAL.add(0, new int[]{6,2});
		testAL.add(0, new int[]{6,4});
		testAL.add(0, new int[]{9,4});
		 
		hero = new Warrior("Hero", 
							Cnt.HERO_COLOR, 
							BitmapFactory.decodeResource(CAct.instance.getResources(), R.drawable.playa));
		
	    hero.currentWeapon = Weapon.MELEE;
	    
	    rivalSoldier = new Warrior("Red", 
						    		Cnt.ENEMY_COLOR, 
						    		BitmapFactory.decodeResource(CAct.instance.getResources(), R.drawable.enem));
	
	    rivalSoldier.currentWeapon = Weapon.RIFLE;
		
	    if(Cnt.rivals){
	    	makeRivals(hero, rivalSoldier);
	    }
		
	    // Загрузим все карты уровней как отдельные 
	    // .png-файлы из папки assets
	    levelMaps = new Bitmap[Level.values().length];
	    for (int i = 0; i < levelMaps.length; i++) {
		    AssetManager am = CAct.instance.getAssets();
		    InputStream is = null;
		    try {
		    	is = am.open(String.valueOf(i)+".png");
		    } catch (IOException e) {
				e.printStackTrace();
			}
		    Bitmap b = BitmapFactory.decodeStream(is);
		    levelMaps[i] = b;
		}
	    
	    CView.goalColumn = new GlowingRectangle();
	    CView.goalColumn.restart();
	    
	    CAct.interactButtons = new PlainButton[Cnt.NUM_OF_INTERACT_BUTTONS];
		CAct.interactButtons[0] = (PlainButton) CAct.instance.findViewById(R.id.ta);
		CAct.interactButtons[1] = (PlainButton) CAct.instance.findViewById(R.id.tb);
		CAct.interactButtons[2] = (PlainButton) CAct.instance.findViewById(R.id.tc);
		
		CAct.threadView.init();
		
		PlainButton.opacityAn = new Animator(Cnt.INTERACT_WINDOW);
		PlainButton.opacityAn.al = new AnimatorListener() {
			public void end() {
				World.enemyBenefit();
			}
		};
		
		World.newGame();
		World.hasBeenInitialised = true;
	}
	
	static void nextRoundOfInteraction(){
		
		for(PlainButton a : CAct.interactButtons){
			a.chosen = false;
		}
		CAct.interactButtons[(int) Math.floor(Math.random()*CAct.interactButtons.length)].chosen = true;
		
		PlainButton.opacityAn.restart();
		TouchMsgBuf.setRawShiftWithinBorders(hero.getBlCrds()[0]*Cnt.cellSize);
	}
	
	static void processBattleSituation(){
		
		if(shallBeginNewRound){
			newGame(); 
			return;
		}
		
		if(TimerEffect.instance.isRunning()) return;
		
		if(!hero.alive()){
			
			Msg.txt = "This is defeat of the ancients.";
			
			endRound();  
			
		} else if (PlainButton.opacityAn.isRunning() && Math.abs(playerInteractiveScore-1.0f)<0.1f){
			
			Msg.txt = "Break through!";
			
		} else if((hero.standsOnX(goalDestination) && !Cnt.TILL_THE_DEATH) ||
				  (Cnt.TILL_THE_DEATH && !rivalSoldier.alive())){
			
			Msg.txt = "Did it!";
			
			endRound();
		} else if(shouldResumeInteraction() && Cnt.INTERRACTIONS){
			
			startInteraction();
			
		}
		
		
	}
	
	static void resetInteraction(){
		
		PlainButton.opacityAn.stop();
		toggleActionPanel(View.GONE);
		playerInteractiveScore = 1.0f;
		interactionInProcess = false;
	}
	
	static boolean shouldResumeInteraction(){
		return !roundIsOver && (hero.hasRivalInSight() || rivalSoldier.hasRivalInSight()); 
	}
	
	/** Входные и выходные значения лежат в диапазоне [0..1].
	 * Ускоряюще-замедляющая функция, большее значение par которой делает её более подобной линейному интерполятору.
	 * http://www.flong.com/texts/code/shapers_exp/
	 */
	static float sigmoid(float inp, float par){
		if(inp>=.5f) 
			return 1-(float)Math.pow(2*(1-inp), 1/par)/2;
		else 
			return (float)Math.pow(2*inp, 1/par)/2;
	}
	
	static float sigmoidInverse(float inp, float par){
		if(inp>=.5f) 
			return 1-(float)Math.pow(2*(1-inp), par)/2;
		else 
			return (float)Math.pow(2*inp, par)/2;
	}
	
	static void startInteraction(){
		
		if(interactionInProcess) return;
		
		toggleActionPanel(View.VISIBLE);
		interactionInProcess = true;
		
		nextRoundOfInteraction();
		
	}
	
	static void toggleActionPanel(final int vis){
		World.h.post(new Runnable() {
			public void run() {
				CAct.instance.findViewById(R.id.actionPanel).setVisibility(vis);
			}
		});
	}
	
	
	static public void endRound(){
		
		roundIsOver = true;
		TimerEffect.instance.start((int)TouchMsgBuf.getScaledViewportXShift());
		
	}
	
	public static void makeRivals(Warrior first, Warrior two){
		first.rival = two;
		two.rival = first;
	}
	
	static public void newGame(){ 
		
		shallBeginNewRound = false;
		
		roundIsOver = false;
		
		TimerEffect.instance.stop();
		
		Pointer.solid.enabled = false;
		
		resetInteraction();
		
		TouchMsgBuf.resetShifts();
		
		Msg.txt = "";
		
		Grid.fill(levelMaps[Cnt.levelType.ordinal()]);
		
		hero.reborn(Grid.lowestValidWarriorBlCrds(0));
		rivalSoldier.reborn(Grid.lowestValidWarriorBlCrds(13));
		  
		rivalSoldier.dir = Dir.LEFT;
		
	}

	static public int randomFloatInRange(int start, int end){
		return (int) (start+Math.round(((end-start)*Math.random())));
	}

	static void playerBenefit(){
		
		float playerBenefitStep = .1f; 
		playerInteractiveScore += playerBenefitStep;
		endInteraction();
		
	}

	static void enemyBenefit(){
		
		float playerPunishmentStep = .05f; 
		if(playerInteractiveScore>playerPunishmentStep){
			playerInteractiveScore-=playerPunishmentStep;
		}
		endInteraction();
	}
	
};




