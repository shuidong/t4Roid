package com.twituji;

public class ConstParam {
	final static boolean CHIPMUNK_ISDEBUG = false;
	final static int GROUND_COLLISION_TYPE = 1;
	final static float GROUND_RESTITUTION = 0.6f;
	final static float GROUND_FRICTION = 1f;
	
	final static float SPACE_GRAVITY = -120f;
	
	final static float BRICK_HEIGHT = 33f;
	final static float BRICK_WIDTH = 33f;
	final static float BRICK_MASS = 1f;
	final static float BRICK_RESTITUTION = 0.3f;
	final static float BRICK_FRICTION = 1f;
	final static int BRICK_COLLISION_TYPE = 2;
	
	final static int SHOE_COLLISION_TYPE = 3;
	final static float SHOE_VELOCITY_X = 0f;//100f;
	final static float SHOE_VELOCITY_Y = 0f;//120f;
	final static float SHOE_POS_X = 200;
	final static float SHOE_POS_Y_OFFSET = 120;
	final static float SHOE_MASS = 0.01f;
	
	final static float BRICK_BOM_WIDTH = 40f;//160;
	final static float BRICK_BOM_HEIGHT = 40f;//160;
	
	final static float SPRING_RESTLENGTH = 100f;
	final static float SPRING_STIFFNESS = 250f;
	final static float SPRING_DAMPING = 0.5f;
	
	final static float SHOOTER_R = 100f;
	final static float SHOOTER_HEIGHT = 80f;
	
	final static int LAY_BKGROUND = -1;
	final static int LAY_CLOUDS = 0;
	final static int LAY_BULID = 1;
	final static int LAY_TREE = 2;
	final static int LAY_CHIPMUNK = 3;
	
	final static int TAG_PARALLAX = 1;
	final static int PARA_MIN_X = -100;
}
