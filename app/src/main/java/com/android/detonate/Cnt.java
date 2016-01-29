package com.android.detonate;

public class Cnt {

	/** Расстояние отхода стрелка при навязывании ему ближнего боя.**/
	static final int DISTANCE_TO_GO = 3;

	static final int FPS=25;
	
	static final String LOG = "detonate!";

	static final int DT = Math.round(1000.0f/FPS);  
	
	static final int[][] rectVertOffset = {{-1,-1},{1,-1},{1,1},{-1,1}};
	
	static final int cellSize = 21;
	static final int[] canonicalGridSize = {11,13};
	static boolean blocksAreFalling = false;
	
	static final int freeFallG = 800;
	static final int jumpSpeed = -280;
	
	static final int gridUpdateInterval = 10*DT;
	static final int warriorActionPause = 10*DT;
	
	static final int gunBurstShift = 13;
	static final int warriorSize = 2*cellSize;
	static final boolean rivals = true;
	
	static int meleeShowDuration = 4*Cnt.DT;
	
	final static int sightMaxYDiff = 2;
	final static int sightMinXDiff = 0;
	final static int sightMaxXDiff = 5;
	
	// Параметры скольжения вьюпорта
	static final float maxVisualShift = 250;
	static final float maxRawShift = 700;
	static final float viewportShiftSigmoidParameter = 0.4f;
	
	static final int stoneTileColor = 0xFF4B77A3;
	static final int glowingColumnColor = 0xFFC2C2C2;
	static final int tilesStartColor = 0xFFFF75BF;//0xFFDC0066;
	static final int tilesEndColor = 0xFFEDF4FF;//0xFF66CCFF;
	static final int timerColor = 0xFFAB8C98;
	static final int HERO_COLOR = 0xFFCE6BFF;
	public static final int PATH_COLOR = 0x80FF0066;
	public static final int ENEMY_COLOR = 0xFFFF3083;
	
	static final int MSG_TEXT_SIZE = 60;

	public static final int INTERACT_WINDOW = 1500;
	public static final int NUM_OF_INTERACT_BUTTONS = 3;
	public static final boolean INTERRACTIONS = true;
	
	public static final boolean TILL_THE_DEATH = true;
	
	static Level levelType =  Level.PATH_FINDER_TEST;

	
};

enum Level {OPEN, CLOSED, PATH_FINDER_TEST};
