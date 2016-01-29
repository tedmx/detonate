package com.android.detonate;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.android.detonate.Grid.TileType;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

enum Dir {

	LEFT, RIGHT;

	// Возвращает модификатор, который, будучи помножен на беззнаковый сдвиг по
	// оси X,
	// даст верное смещение по этой оси с учетом направления.
	int numericalValue() {
		return (this == LEFT ? -1 : 1);
	}

	Dir fromNumber(int d) {
		return (d == -1 ? LEFT : RIGHT);
	}

	Dir getReverse() {
		return (this == LEFT ? RIGHT : LEFT);
	}
};

class Warrior {

	// Movement
	private int pixY;
	Dir dir;
	private int pixYVel;

	private int[] blCrds;
	Weapon currentWeapon;
	String tag;

	// Status
	private int hp;
	private int maxHP = 100;

	// Effects
	private BloodBurst warriorBlood;
	private GunBurst warriorGunFlash;

	// Rival system
	Warrior rival;

	Agenda agenda;

	private boolean flying;
	private boolean gotShifts;
	boolean passive;

	boolean engagedInBattle;
	private boolean rivalInSight;
	private boolean canDamageRival;
	private boolean faceToFace;
	private int xDistToRival;

	// Visuals
	int lifebarColor;
	Bitmap image;

	/**
	 * Прямоугольник нужен только для drawBitmap (Bitmap, Rect, Rect dst,
	 * Paint). Он задает область, в которую будет нарисован вырезаннй кусок
	 * текстуры.
	 **/
	private static final Rect canonicalImageCell = new Rect(0, 0, Cnt.cellSize,
			Cnt.cellSize);

	private Animator cutter;
	private Animator actionDelay;

	private static Action jump = new Action() {

		public void run(Warrior w) {

			w.pixYVel = -280;
			w.flying = true;
			w.gotShifts = true;

		}
	};

	private static Action turnAround = new Action() {

		public void run(Warrior w) {
			w.dir = w.dir.getReverse();

		}
	};

	private static Action attack = new Action() {

		public void run(Warrior w) {

			Grid.launchAttack(w.blXInFront(), w.blCrds[1], w.dir,
					w.currentWeapon);

			if (w.currentWeapon == Weapon.MELEE) {
				w.cutter.restart(Cnt.meleeShowDuration);
			} else if (w.currentWeapon == Weapon.RIFLE) {
				w.warriorGunFlash.restart();
			}

		}

	};

	private static Action shiftForward = new Action() {

		public void run(Warrior w) {
			w.blCrds[0] += w.dir.numericalValue();
			if (w.flying && w.gotShifts)
				w.gotShifts = false;
		}

	};

	Warrior() {

		tag = "Warrior";

		World.warriors.add(this);

		agenda = new Agenda();

		currentWeapon = Weapon.RIFLE;

		image = BitmapFactory.decodeResource(CAct.instance.getResources(),
				R.drawable.playa);

		warriorBlood = new BloodBurst();
		warriorGunFlash = new GunBurst();

		blCrds = new int[2];

		cutter = new Animator();

		actionDelay = new Animator(Cnt.warriorActionPause);

	}

	Warrior(String tag, int lifebarColor, Bitmap image) {
		this();
		this.tag = tag;
		this.lifebarColor = lifebarColor;
		this.image = image;
	}

	boolean alive() {
		return hp > 0;
	}

	private void battleLogics(Weapon w) {

		if (w == Weapon.MELEE) {

			if (canDamageRival) {

				if (xDistToRival < 2)
					tryAction(attack);

				else { 
					
					if (faceToFace)
						goToEnemy();
				}
			} else {
				
				if (rivalInSight)
					goToEnemy();
			}
		}

		if (w == Weapon.RIFLE) {

			if (rival.alive() && xDistToRival < 2 && agenda.isEmpty()) {

				extendRange();

			} else if (canDamageRival) {

				tryAction(attack);
			}
		}

	}

	int blXInFront() {

		if (dir == Dir.LEFT)
			return blCrds[0] - 1;
		else if (dir == Dir.RIGHT)
			return blCrds[0] + 2;
		return -1;
	}

	void computeFall() {

		int pixY_surface = Grid.topOfTwoAdjasCols(blCrds[0]) * Cnt.cellSize;

		// Если боец находится над землей, то считаем его летящим.
		if (pixY + Cnt.warriorSize < pixY_surface) {
			flying = true;
		}

		if (flying) {

			float secDT = Cnt.DT / 1000.0f;

			int nextY = (int) (pixY + pixYVel * secDT + Cnt.freeFallG * secDT
					* secDT / 2.0f);

			// Если в течение прыжка боец остается над землей, то продолжаем.
			if (nextY + Cnt.warriorSize < pixY_surface) {
				pixY = nextY;
				pixYVel += Cnt.freeFallG * secDT;

				// Если в момент прыжка боец проваливается под землею, то
				// останавливаем прыжок.
			} else {
				pixY = pixY_surface - Cnt.warriorSize;
				pixYVel = 0;
				flying = false;
			}

		}

		blCrds[1] = Math.round(pixY / (float) Cnt.cellSize);// Grid.pixToBlock(pixY);

	}

	private void dealWithTask(Task taskToDealWith) {

		if (taskToDealWith.getMovetype() == MoveType.CUSTOM_ACTION) {
			tryAction(taskToDealWith.getAction());
			agenda.remove(taskToDealWith);
			return;
		}

		int goalBlockX = -1;

		if (taskToDealWith.getMovetype() == MoveType.GOTO
				|| taskToDealWith.getMovetype() == MoveType.POINTER) {
			goalBlockX = taskToDealWith.getValue();
		}

		// Если цель еще не достигнута, выбираем направление
		if (goalBlockX > blCrds[0] + 1) {
			dir = Dir.RIGHT;
		} else if (goalBlockX < blCrds[0]) {
			dir = Dir.LEFT;
		} else {
			// Цель достигнута.
			if (!flying) {

				if (taskToDealWith.getMovetype() == MoveType.POINTER) {
					Pointer.solid.enabled = false;
				}
				agenda.remove(taskToDealWith);
			}

			return;
		}

		// Вычисление координат и типов блоков прямо перед бойцом
		int[] upObstacleCrds = { blXInFront(), blCrds[1] }, botObstacleCrds = {
				upObstacleCrds[0], blCrds[1] + 1 };

		TileType upperType = Grid.getBlType(upObstacleCrds), bottomType = Grid
				.getBlType(botObstacleCrds);

		if (flying) {

			if (upperType == TileType.EMPTY && bottomType == TileType.EMPTY) {
				tryAction(shiftForward);
			} else if (upperType == TileType.FLUFF
					|| bottomType == TileType.FLUFF) {
				tryAction(attack);
			}

		} else {
			TileType extraUp = Grid.getBlType(new int[] { upObstacleCrds[0],
					upObstacleCrds[1] - 1 });

			if (upperType == TileType.EMPTY && bottomType == TileType.EMPTY) {
				tryAction(shiftForward);
			} else if (upperType == TileType.FLUFF
					|| bottomType == TileType.FLUFF) {
				tryAction(attack);
			} else if (extraUp == TileType.FLUFF
					|| Grid.topOfTheColumn(upObstacleCrds[0]) >= upObstacleCrds[1]) {
				tryAction(jump);
			} else {
				World.newGame();
			}
		}
	}

	private void tryAction(Action a) {
		if (!hasActions() || !actionAvailable(a))
			return;
		spendAction();
		a.run(this);
	}

	private boolean actionAvailable(Action a) {
		if (a == jump && flying) {
			return false;
		} else if (a == shiftForward && flying && !gotShifts) {
			return false;
		}
		return true;
	}

	void draw(Canvas canvas) {

		int pixX = blCrds[0] * Cnt.cellSize;

		canvas.save();
		canvas.translate(pixX, pixY);

		CView.p.reset();

		if (!alive()) {
			canvas.translate(Cnt.cellSize, Cnt.cellSize);
			warriorBlood.cycle(canvas);
			canvas.restore();
			return;
		}

		CView.p.setColor(lifebarColor);
		CView.p.setAlpha(200);
		CView.p.setXfermode(CView.screenXfermode);
		canvas.drawText(String.valueOf(currentWeapon.getLetter()), 0, 0,
				CView.p);
		CView.p.reset();

		if (dir == Dir.LEFT) {
			canvas.translate(Cnt.warriorSize, 0);
			canvas.scale(-1, 1);
		}

		// Вспышка от выстрела
		canvas.save();
		canvas.translate(Cnt.cellSize * 2, Cnt.gunBurstShift);
		warriorGunFlash.cycle(canvas);
		canvas.restore();

		CView.p.reset();

		// Фигура с освещением
		if (warriorGunFlash.isRunning() && warriorGunFlash.visible) {
			CView.p.setColorFilter(CView.flashed);
		} else {
			CView.p.reset();
		}

		canvas.save();
		canvas.scale(2, 2);

		/** Прямоугольник указывает, какую ячейку из картинки натягивать **/
		Rect spriteFrame = new Rect(0, 0, Cnt.cellSize, Cnt.cellSize);

		if (currentWeapon == Weapon.MELEE) {
			spriteFrame.offset((cutter.isRunning() ? 2 : 1) * Cnt.cellSize, 0);
		}

		canvas.drawBitmap(image, spriteFrame, canonicalImageCell, CView.p);

		canvas.restore();

		// Кровь
		CView.p.reset();
		canvas.translate(Cnt.cellSize, Cnt.cellSize);
		warriorBlood.cycle(canvas);

		canvas.restore();

	}

	/** Разрешить ситуацию, в которых два стрелка сталкиваются вместе. **/
	private void extendRange() {

		// Выбор направления для отхода
		boolean leftReachable = Grid.isValidBlX(rival.blCrds[0]
				- Cnt.DISTANCE_TO_GO), rightReachable = Grid
				.isValidBlX(rival.blCrds[0] + 1 + Cnt.DISTANCE_TO_GO);

		boolean chooseLeft;

		if (leftReachable && rightReachable) {

			if (rival.blCrds[0] > blCrds[0]) {
				chooseLeft = true;
			} else if (rival.blCrds[0] < blCrds[0]) {
				chooseLeft = false;
			} else {

				if (Math.random() > .5) {
					chooseLeft = true;
				} else {
					chooseLeft = false;
				}

			}
		} else if (leftReachable) {
			chooseLeft = true;
		} else if (rightReachable) {
			chooseLeft = false;
		} else
			return;

		int xDestination;
		if (chooseLeft) {
			xDestination = rival.blCrds[0] - Cnt.DISTANCE_TO_GO;
		} else {
			xDestination = rival.blCrds[0] + 1 + Cnt.DISTANCE_TO_GO;
		}

		agenda.add(Task.doActionTask("rotate back", turnAround));

		agenda.add(Task.goToTask(xDestination, "Step out from enemy"));

	}

	// Вычисление того, виден ли враг, можно ли в него попасть, а также других
	// булевых выражений.
	private void gatherInfo() {

		rivalInSight = false;
		faceToFace = false;
		xDistToRival = -1;
		canDamageRival = false;

		if (rival != null && rival.alive()) {

			if (Math.abs(this.blCrds[1] - rival.blCrds[1]) <= Cnt.sightMaxYDiff
					&& dir.numericalValue()
							* (rival.blCrds[0] - this.blCrds[0]) >= Cnt.sightMinXDiff
					&& dir.numericalValue()
							* (rival.blCrds[0] - this.blCrds[0]) <= Cnt.sightMaxXDiff)
				rivalInSight = true;

			Warrior leftMost, rightMost;
			if (toLeftOf(rival)) {
				leftMost = this;
				rightMost = rival;
			} else {
				leftMost = rival;
				rightMost = this;
			}
			faceToFace = leftMost.dir == Dir.RIGHT && rightMost.dir == Dir.LEFT;

			xDistToRival = Math.abs(rival.blCrds[0] - blCrds[0]);

			if (rivalInSight) {
				canDamageRival = Grid.testAttack(blXInFront(), blCrds[1], dir,
						currentWeapon, rival);
			}
		}

	}

	boolean toLeftOf(Warrior w) {
		return this.blCrds[0] < w.blCrds[0];
	}

	int[] getBlCrds() {
		return blCrds.clone();
	}

	int getHp() {
		return hp;
	}
	
	float getRelativeHP(){
		return hp/(float)maxHP;
	}

	private boolean hasActions() {
		return !actionDelay.isRunning();
	}

	boolean hasRivalInSight() {
		return rivalInSight;
	}

	void doLogics() {

		// Условия, при которых боец пропускает ход.
		if (!alive() || passive || actionDelay.isRunning())
			return;

		if (Math.random() > 0.5f) {
			if ((this == World.hero && World.playerInteractiveScore < 0.7f)
					|| this == World.rivalSoldier
					&& World.playerInteractiveScore > 1.5f) {
				spendAction();
				return;
			}
		}

		gatherInfo();

		battleLogics(currentWeapon);

		// Однажды увидев врага, будет его преследовать.
		if (!engagedInBattle && rivalInSight) {
			engagedInBattle = true;
		}

		if (engagedInBattle && rival.alive() && !rivalInSight) {
			goToEnemy();
		}

		if (!agenda.isEmpty() && hasActions()) {
			dealWithTask(agenda.getLast());
		}

	}

	private void goToEnemy() {
		if (agenda.isEmpty())
			agenda.add(Task.goToTask(rival.blCrds[0], "pursue enemy"));
	}

	void reborn(int[] blCrds) {

		engagedInBattle = false;

		actionDelay.stop();

		hp = maxHP;
		dir = Dir.RIGHT;

		agenda.clear();

		warriorGunFlash.pause();
		warriorBlood.pause();

		setBlCrds(blCrds[0], blCrds[1]);

	}

	private void setBlCrds(int x, int y) {
		blCrds[0] = x;
		blCrds[1] = y;
		pixY = blCrds[1] * Cnt.cellSize;
	}

	boolean standsOnX(int x) {
		return blCrds[0] == x || blCrds[0] + 1 == x;
	}

	private void spendAction() {
		actionDelay.restart();
	}

	void takeDamage(int amount, Dir shotDirection) {

		if (alive()) {

			warriorBlood.restart();
			if (shotDirection == dir && !engagedInBattle) {
				agenda.add(Task.doActionTask("rotate back", turnAround));
			}
		}

		float modifiedAmount = amount
				/ (this == World.hero ? World.playerInteractiveScore
						: 1 / World.playerInteractiveScore);
		hp -= modifiedAmount;

	}
}
