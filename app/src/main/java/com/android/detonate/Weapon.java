package com.android.detonate;

class Weapon {
	
	private String name;
	public String getName() {
		return name;
	}

	public int getPower() {
		return power;
	}

	public int getPenetrability() {
		return penetrability;
	}

	public int getRange() {
		return range;
	}

	private int power;
	private int penetrability;
	private int range;
	
	static final Weapon RIFLE = new Weapon("Rifle", 15, 1, 4);
	static final Weapon MELEE = new Weapon("Melee", 5, 1, 1);
	
	char getLetter() {
		return name.charAt(0);
	}

	private Weapon(String name, int power, int penetrability, int range) {
		super();
		this.name = name;
		this.power = power;
		this.penetrability = penetrability;
		this.range = range;
	};
	
}