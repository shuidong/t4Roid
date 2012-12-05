package com.twituji;

public class CollisionType {
	
	public static int SHOE_TYPE = 0;
	public static int BRICK_TYPE = 1;
	public static int GROUND_TYPE = 2;
	
	public static int TIMES_ONE = 1;//collision between shoe and ground or shoes
	public static int TIMES_TWO = 2;//collision between shoe and brick
	
	private int objType;
	private int data;

	public int getData() {
		return data;
	}

	public void setData(int data) {
		this.data = data;
	}

	public int getObjType() {
		return objType;
	}

	public CollisionType(int objType, int data) {
		this.objType = objType;
		this.data = data;
	}
	
}
