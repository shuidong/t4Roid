package com.twituji;

import static com.wiyun.engine.types.WYPoint.mul;
import static com.wiyun.engine.types.WYPoint.near;
import static com.wiyun.engine.types.WYPoint.sub;
import static javax.microedition.khronos.opengles.GL10.GL_LINE_SMOOTH;

import java.util.ArrayList;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import android.graphics.Typeface;
import android.view.MotionEvent;

import com.wiyun.engine.actions.Action;
import com.wiyun.engine.actions.Animate;
import com.wiyun.engine.actions.Action.Callback;
import com.wiyun.engine.chipmunk.Arbiter;
import com.wiyun.engine.chipmunk.Body;
import com.wiyun.engine.chipmunk.Chipmunk;
import com.wiyun.engine.chipmunk.Circle;
import com.wiyun.engine.chipmunk.Constraint;
import com.wiyun.engine.chipmunk.ICollisionHandler;
import com.wiyun.engine.chipmunk.PELoader;
import com.wiyun.engine.chipmunk.PinJoint;
import com.wiyun.engine.chipmunk.Poly;
import com.wiyun.engine.chipmunk.Segment;
import com.wiyun.engine.chipmunk.Shape;
import com.wiyun.engine.chipmunk.Space;
import com.wiyun.engine.chipmunk.Space.IPostStepCallback;
import com.wiyun.engine.nodes.Animation;
import com.wiyun.engine.nodes.Director;
import com.wiyun.engine.nodes.Label;
import com.wiyun.engine.nodes.MotionStreak;
import com.wiyun.engine.nodes.ParallaxNode;
import com.wiyun.engine.nodes.Scene;
import com.wiyun.engine.nodes.Sprite;
import com.wiyun.engine.opengl.Primitives;
import com.wiyun.engine.opengl.Texture2D;
import com.wiyun.engine.types.WYPoint;
import com.wiyun.engine.types.WYRect;
import com.wiyun.engine.types.WYSize;
import com.wiyun.engine.utils.ResolutionIndependent;
import com.wiyun.engine.utils.TargetSelector;
import com.wiyun.engine.utils.Utilities;

public class GameScene extends Scene implements ICollisionHandler, IPostStepCallback, Callback{
	
	Chipmunk chipmunk;
	Space mSpace;
	
	Random rand;
	PELoader m_bodyLoader;
	
	ArrayList<Body> m_shoeList;
	Body currentShoeBody;
	boolean mDragging = false;
	
	float ground_y;
	
	Constraint shoePin;
	Body staticBody;
	
	ParallaxNode parallax;
	WYPoint previousLocation;
	
	/**
	 * debug lable and others
	 */
	Label dgb_label;
	
	int para_min_x ;
	WYSize s;
	public GameScene() {
		
		s = Director.getInstance().getWindowSize();
		para_min_x = -(int)s.width / 8;
		
		/**
		 * setup the debug
		 */
		dgb_label = Label.make("DroidSans 12", 20, "DroidSans", Typeface.BOLD, 0);
		dgb_label.setPosition(s.width / 2, s.height/2);
		addChild(dgb_label, 99);
		dgb_label.setText("this is a demo version");
		
		/**
		 * create a void node, a parent node
		 */
		parallax = ParallaxNode.make();
		parallax.setMaxX(0);
		parallax.setMinX(para_min_x);//-100
		parallax.setMinY(0);
		parallax.setMaxY(0);
		
		/**
		 * add the game backGround
		 */
	    Sprite bkGround = Sprite.make(R.drawable.game);
	    bkGround.setPosition(s.width * 0.75f, s.height / 2);
        parallax.addChild(bkGround, ConstParam.LAY_BKGROUND, 1f, 1f);
        
        /**
         * add the building
         */
        Sprite build = Sprite.make(R.drawable.bulid);
        build.setPosition(s.width/4, s.height/2 );
        parallax.addChild(build, ConstParam.LAY_BKGROUND, 1.5f, 1f);
        
        /**
         * add the clouds
         */
        Sprite cloud1 = Sprite.make(R.drawable.cld);
        cloud1.setPosition(s.width, 0.8f * s.height );
        parallax.addChild(cloud1, 6, 2f, 1f);
        
        Sprite cloud2 = Sprite.make(R.drawable.cld);
        cloud2.setPosition(s.width * 0.75f, 0.8f * s.height );
        parallax.addChild(cloud2, 0, 3.5f, 1f);
        
        /**
         * add the mono
         */
        Sprite mono = Sprite.make(R.drawable.mono);
        mono.setPosition(s.width * 0.6f, s.height/2 );
        parallax.addChild(mono, 3, 3f, 1f);
        
        /**
         * init the chipmunk object
         */
        chipmunk = Chipmunk.make();
		chipmunk.setDebugDraw(ConstParam.CHIPMUNK_ISDEBUG);
		chipmunk.setPosition(0,0);
		parallax.addChild(chipmunk, 5, 4f, 1.0f, 1f, 1);
		Shape.resetShapeIdCounter();
		
		/**
		 * init the space object
		 */
		mSpace = chipmunk.getSpace();
		mSpace.resizeActiveHash(30f, 1000);
		mSpace.setIterations(10);
		mSpace.setGravity(0, ConstParam.SPACE_GRAVITY);
		
		/**
		 * add the ground
		 */
		staticBody = Body.make(Float.MAX_VALUE, Float.MAX_VALUE);
		ground_y = s.height /3;//define the height of the ground
		final Shape shape = Segment.make(staticBody, 0f, ground_y, s.width * 1.5f, ground_y, 1);
		shape.setCollisionType(ConstParam.GROUND_COLLISION_TYPE);
		shape.setRestitution(ConstParam.GROUND_RESTITUTION);
		shape.setFriction(ConstParam.GROUND_FRICTION);
		shape.setLayerMask(Chipmunk.NOT_GRABABLE_MASK);
		mSpace.addStaticShape(shape);
		
		rand = new Random(10);
		
		/**
		 * add the bricks
		 */
		final float brickWidth = ResolutionIndependent.resolveDp(ConstParam.BRICK_HEIGHT);
		final float brickHeight = ResolutionIndependent.resolveDp(ConstParam.BRICK_WIDTH);
		final WYPoint[] brickSize = {
				WYPoint.make(-brickWidth/2f, -brickHeight/2f), 
				WYPoint.make(-brickWidth/2f, brickHeight/2f), 
				WYPoint.make(brickWidth/2f, brickHeight/2f), 
				WYPoint.make(brickWidth/2f, -brickHeight/2f)
		};
		
		final float wall_x = s.width ;
		for(int i = 0; i < 6; i++) {
			for(int j = 0; j < 4; j++) {
				Body bodyBrick = Body.make(ConstParam.BRICK_MASS, 
						Chipmunk.calculateMomentForPoly(ConstParam.BRICK_MASS, brickSize, WYPoint.makeZero()));
				bodyBrick.setPosition(wall_x + i * brickWidth, ground_y + brickHeight /2f +  j * brickHeight);
				mSpace.addBody(bodyBrick);
				Shape shapeBrick = Poly.make(bodyBrick, brickSize, WYPoint.makeZero());
				shapeBrick.setFriction(ConstParam.BRICK_FRICTION);
				shapeBrick.setRestitution(ConstParam.BRICK_RESTITUTION);
				
				shapeBrick.setTexture(Texture2D.makePNG(R.drawable.bricks),
						WYRect.make(rand.nextInt(2) * brickWidth, 0, brickWidth, brickHeight));
				shapeBrick.setCollisionType(ConstParam.BRICK_COLLISION_TYPE);
				// TODO maybe here need to add some Constraint?
				mSpace.addShape(shapeBrick);
			}
		}
    	
		setTouchEnabled(true);
		m_bodyLoader = PELoader.make(R.raw.shoes_defs);
		m_shoeList = new ArrayList<Body>();
		
		mSpace.addCollisionHandler(ConstParam.BRICK_COLLISION_TYPE, ConstParam.SHOE_COLLISION_TYPE, this, null);
		mSpace.addCollisionHandler(ConstParam.GROUND_COLLISION_TYPE, ConstParam.SHOE_COLLISION_TYPE, this, null);

		initShoe(0f);
		
		/**
		 * add the ground
		 */
		Sprite ground = Sprite.make(R.drawable.ground);
		ground.setPosition(s.width * 0.75f, s.height * 0.2f);
        parallax.addChild(ground, 5, 3.5f, 1f);
		
		/**
		 * setup the updateSchedule
		 */
		schedule(new TargetSelector(this, "update(float)", new Object[] { 0f }));
		
		addChild(parallax, 0, ConstParam.TAG_PARALLAX);
		ParallaxNode node = ParallaxNode.from(getChildPointer(ConstParam.TAG_PARALLAX));
		node.offsetBy(para_min_x, 0);
		schedule(moveLeftSelector, 0.01f);
	    
		setGestureEnabled(true);
		autoRelease(true);
	}

	float moveOffset = 0f;
	TargetSelector moveLeftSelector = new TargetSelector(this, "moveScreen(float, int)", new Object[] { 0f, 0 });
	TargetSelector moveRightSelector = new TargetSelector(this, "moveScreen(float, int)", new Object[] { 0f, 1 });
	public void moveScreen(final float dl, final int direct){
		ParallaxNode node = ParallaxNode.from(getChildPointer(ConstParam.TAG_PARALLAX));
		if(0 == direct){//to left
			node.offsetBy(2f, 0);
			if(node.getOffsetX() >= node.getMaxX()){
				unschedule(moveLeftSelector);
			}
		}else{//to right
			node.offsetBy(-2f, 0);
			if(node.getOffsetX() <= node.getMinX()){
				unschedule(moveRightSelector);
			}
		}
	}
	
	/**
	 * add the shoe
	 * @param dlt
	 */
	public void initShoe(final float dlt){
		/**
		 * add the first shoe
		 */
		currentShoeBody = addNewBody(s.width / 4, s.height / 4 + ground_y);
		
		shoePin = PinJoint.make(currentShoeBody, staticBody, WYPoint.makeZero(), 
				((ShoeBodyData)currentShoeBody.getData()).shotPoint);
		mSpace.addConstraint(shoePin);
	}
	
	/**
	 * Chipmunk update
	 * @param delta
	 */
	public void update(float delta) {
		
		final float steps = 3f;
		final float dt = delta / steps;
		for(int i = 0; i < steps; i++){
			mSpace.step(dt);
		}
			
		for(Body body : m_shoeList) {
			//update sprite
			float posX = body.getPositionX();
			float posY = body.getPositionY();
			float angle = body.getAngle();
			
			Sprite sprite = ((ShoeBodyData) body.getData()).getSprite();
			sprite.setPosition(posX, posY);
			sprite.setRotation(-Utilities.r2d(angle));
			
			MotionStreak streak = ((ShoeBodyData) body.getData()).getStreak();
			if(!((ShoeBodyData) body.getData()).isRemoveMStreak())
            streak.addPoint(posX, posY, false);
		}
		
	}
	
	@Override
	public boolean wyTouchesBegan(MotionEvent event) {
		ParallaxNode node = ParallaxNode.from(getChildPointer(ConstParam.TAG_PARALLAX));
		WYPoint loc = Director.getInstance().convertToGL(event.getX(), event.getY());
		loc = sub(loc, WYPoint.make(node.getOffsetX()*4, 0));
		//dgb_label.setText("node.getOffsetX()=" + node.getOffsetX());
		//dgb_label.setText("loc.x=" + loc.x);
		WYRect rect = WYRect.makeZero();
		ArrayList <Shape> shoeShapes = mSpace.getShapesOfBody(currentShoeBody);
		for(Shape shapeUnit : shoeShapes){
			shapeUnit.getBoundingBox(rect);
			if(rect.containsPoint(loc)) {
				mDragging = true;
				break;
			}
		}
		previousLocation = WYPoint.make(event.getX(), event.getY());
		
		node.stopFling();
		return true;
	}
	
	@Override
	public boolean wyTouchesMoved(MotionEvent event) {
		if(mDragging) {//mDragging
			WYPoint shotPoint = ((ShoeBodyData)currentShoeBody.getData()).shotPoint;
			WYPoint loc = Director.getInstance().convertToGL(event.getX(), event.getY());
			loc = sub(loc, WYPoint.make(parallax.getOffsetX()*4, 0));
			WYPoint tmp = sub(loc, shotPoint);
			
			if(!near(loc, shotPoint, ConstParam.SHOOTER_R)){
				float now_distance = WYPoint.length(tmp);
				tmp.x *=  ConstParam.SHOOTER_R/now_distance;
				tmp.y *=  ConstParam.SHOOTER_R/now_distance;
				
				loc = WYPoint.add(shotPoint, tmp);
			}
			
			//currentShoeBody.applyForce(WYPoint.make(0, -currentShoeBody.getMass()*ConstParam.SPACE_GRAVITY), WYPoint.makeZero());
			currentShoeBody.setPosition(loc);
			
			GL10 gl = Director.getInstance().gl;
			//gl.glEnable(GL_LINE_SMOOTH);
			gl.glDisable(GL_LINE_SMOOTH);
			gl.glLineWidth(20.0f);
			gl.glColor4f(0f, 0.0f, 0.0f, 255f);
			Primitives.drawLine(sub(loc, WYPoint.make(10,10)), sub(shotPoint, WYPoint.make(parallax.getOffsetX()*4, -15)));
			Primitives.drawLine(sub(loc, WYPoint.make(10,10)), sub(shotPoint, WYPoint.make(parallax.getOffsetX()*4, 15)));
			
			mSpace.removeConstraint(shoePin);

			Sprite sprite = ((ShoeBodyData) currentShoeBody.getData()).getSprite();
			sprite.setPosition(loc);
			sprite.setRotation(-Utilities.r2d(currentShoeBody.getAngle()));
			
			WYPoint shotVelocity = WYPoint.mul(tmp, WYPoint.make(-3f, -3f));
			((ShoeBodyData)currentShoeBody.getData()).shotVelocity = shotVelocity;
			
		}else{
			WYPoint diff = WYPoint.makeZero();

			WYPoint touchLocation = WYPoint.make(event.getX(), event.getY());

			WYPoint location = Director.getInstance().convertToGL(touchLocation.x, touchLocation.y);
			WYPoint prevLocation = Director.getInstance().convertToGL(previousLocation.x, previousLocation.y);

			diff.x = location.x - prevLocation.x;
			diff.y = location.y - prevLocation.y;

			ParallaxNode node = ParallaxNode.from(getChildPointer(ConstParam.TAG_PARALLAX));
			node.offsetBy(diff.x, 0);

			previousLocation = touchLocation;
		}
		return true;
	}
	
    @Override
	public boolean wyTouchesEnded(MotionEvent event) {
    	
    	if(mDragging){
    		WYPoint shotV = ((ShoeBodyData)currentShoeBody.getData()).shotVelocity;
    	//	currentShoeBody.setVelocity(shotV);
    		currentShoeBody.applyImpulse(WYPoint.make(shotV.x/100, -ConstParam.SPACE_GRAVITY * ConstParam.SHOE_MASS + shotV.y/100), WYPoint.make(0,0));
    		((ShoeBodyData) currentShoeBody.getData()).setRemoveMStreak(false);
			scheduleOnce(new TargetSelector(this, "initShoe(float)", new Object[] { 0f }), 3f);
			//schedule(moveRightSelector, 0.01f);
			scheduleOnce(new TargetSelector(this, "endMv2Right(float)", new Object[] { 0f }), 1f);
			scheduleOnce(new TargetSelector(this, "endMv2Left(float)", new Object[] { 0f }), 5f);
    	}
    	mDragging = false;
		return true;
	}
    
    @Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		ParallaxNode node = ParallaxNode.from(getChildPointer(ConstParam.TAG_PARALLAX));
		node.fling(velocityX, velocityY);
		this.dgb_label.setText(String.valueOf(velocityX));
		if(0 < velocityX){
			schedule(moveLeftSelector, 0.01f);
		}else{
			schedule(moveRightSelector, 0.01f);
		}
		return true;
	}
    
    public void endMv2Left(final float dlt){
    	schedule(moveLeftSelector, 0.01f);
    }
    
    public void endMv2Right(final float dlt){
    	schedule(moveRightSelector, 0.01f);
    }

    private Body addNewBody(final float x, final float y) {
		/*float angle = ((float)(rand.nextLong()) % 360.0f);
		float radian = Utilities.d2r(angle);*/
		
    	float angle = 30f;
		float radian = Utilities.d2r(angle);
    	
		Body body = m_bodyLoader.createBodyByName(chipmunk, "shoe001");
		/**
		 * add a sprite for the shoe object
		 */
		ShoeBodyData bodyData = new ShoeBodyData(R.drawable.cloud, R.drawable.shoe001, chipmunk);
		WYPoint shoePos = WYPoint.make(x, y);
		bodyData.shotPoint = shoePos;
		
		bodyData.getSprite().setPosition(shoePos);
		bodyData.getSprite().setRotation(-angle);
		WYPoint anchor = m_bodyLoader.getAnchorPercent("shoe001");
		bodyData.getSprite().setAnchorPercent(anchor.x, anchor.y);
		
		/**
		 * init the body
		 */
		body.setAngle(radian);
		body.setPosition(shoePos);
		body.setVelocity(ConstParam.SHOE_VELOCITY_X, ConstParam.SHOE_VELOCITY_Y);
		body.setData(bodyData);
		m_shoeList.add(body);
		
		/**
		 * setup the CollisionType of shoe
		 */
		/*ArrayList<Shape> shapesOfShoe = mSpace.getShapesOfBody(body);
		for(Shape shapeUnit : shapesOfShoe){
			shapeUnit.setCollisionType(ConstParam.SHOE_COLLISION_TYPE);//it has defined in *.plist
		}*/
		return body;
	}
    
    ArrayList<String> collisionPairList = new ArrayList<String> ();
	//@Override
	public void postStep(Object arg0, Object arg1) {
		if(null != arg1){
			CollisionType collisionType = (CollisionType) arg1;
			if(collisionType.getObjType() == collisionType.BRICK_TYPE){
				Body brick = ((Shape)arg0).getBody();
				float brick_x = brick.getPositionX();
				float brick_y = brick.getPositionY();
				mSpace.removeAndDestroyShape((Shape)arg0);
				mSpace.removeAndDestroyBody(brick);
				
				scheduleOnce(new TargetSelector(this, "runBomAni(float, float, float)", new Object[] { 0f, brick_x, brick_y}), 0f);
			}
			
			if(collisionType.getObjType() == collisionType.SHOE_TYPE){
				Shape shape = (Shape)arg0;
				Body forDelShoeBody = shape.getBody();
				
				float shoe_x = forDelShoeBody.getPositionX();
				float shoe_y = forDelShoeBody.getPositionY();
				
				((ShoeBodyData) forDelShoeBody.getData()).destoryData();
				int delBodyInt = forDelShoeBody.getPointer();
				mSpace.removeAndDestroyShapesOfBody(forDelShoeBody);
				mSpace.removeAndDestroyBody(forDelShoeBody);
				for(Body bdy: m_shoeList){
					if(delBodyInt == bdy.getPointer()){
						m_shoeList.remove(bdy);
						//TODO maybe here need to set the currentShoe
						break;
					}
				}
				
				scheduleOnce(new TargetSelector(this, "runBomAni(float, float, float)", new Object[] { 0f, shoe_x, shoe_y}), 0f);
				return;
			}
		}
	}

	/**
	 * run the anima of the brick explo
	 * @param delta
	 * @param v
	 */
	public void runBomAni(final float delta, final float x, final float y) {
		Texture2D tex = Texture2D.makePNG(R.drawable.explode_shoe);//bom
		tex.autoRelease();
		float bom_w = ResolutionIndependent.resolveDp(ConstParam.BRICK_BOM_WIDTH);
		float bom_h = ResolutionIndependent.resolveDp(ConstParam.BRICK_BOM_HEIGHT);
		Sprite sprite = Sprite.make(tex, WYRect.make(0, 0, bom_w, bom_h));
		chipmunk.addChild(sprite);
        sprite.setPosition(x, y);
        
        Animation anim = new Animation(0);
        anim.addFrame(0.1f, 
        		WYRect.make(0 * bom_w, 0, bom_w, bom_h),
        		WYRect.make(1 * bom_w, 0, bom_w, bom_h),
        		WYRect.make(2 * bom_w, 0, bom_w, bom_h),
        		WYRect.make(3 * bom_w, 0, bom_w, bom_h),
        		WYRect.make(4 * bom_w, 0, bom_w, bom_h),
        		WYRect.make(5 * bom_w, 0, bom_w, bom_h),
        		WYRect.make(6 * bom_w, 0, bom_w, bom_h));
        
        Animate a = (Animate)Animate.make(anim).autoRelease();
        a.setCallback(this);
        sprite.runAction(a);
    }

	//@Override
	public int begin(int arg0, int arg1, Object arg2) {
		return 1;
	}

	//@Override
	public void postSolve(int arg0, int arg1, Object arg2) {
	}

	//@Override
	public int preSolve(int arg0, int arg1, Object arg2) {
		return 1;
	}
	
	//@Override
	public void separate(int arg0, int arg1, Object arg2) {
		Arbiter arbiter = Arbiter.from(arg0);
		Shape a = arbiter.getShapeA();
		Shape b = arbiter.getShapeB();
		if(null == a || null == b){
			return ;
		}
		String collPair = String.valueOf(b.getBody().getPointer()) + "_" + String.valueOf(a.getBody().getPointer());
		String shoeMark = String.valueOf(b.getBody().getPointer()) + "_" + "shoeBom";
		if( b.getCollisionType() == ConstParam.SHOE_COLLISION_TYPE 
				&& a.getCollisionType() == ConstParam.BRICK_COLLISION_TYPE) {
			((ShoeBodyData)b.getBody().getData()).destroyMotionStreak();
			if(!collisionPairList.contains(collPair) ){
				collisionPairList.add(collPair);
				mSpace.addPostStepCallback(this, a, new CollisionType(CollisionType.BRICK_TYPE, CollisionType.TIMES_TWO));
			}
		}
		
		if(b.getCollisionType() == ConstParam.SHOE_COLLISION_TYPE 
				&& a.getCollisionType() == ConstParam.GROUND_COLLISION_TYPE){
			if(!collisionPairList.contains(collPair)){
				collisionPairList.add(collPair);
				//scheduleOnce(new TargetSelector(this, "callShoeBom(float, Object)", new Object[] { 0f, (Object)b }), 1f);
			}
		}
		
		if(b.getCollisionType() == ConstParam.SHOE_COLLISION_TYPE ){
			if(!collisionPairList.contains(shoeMark)){
				collisionPairList.add(shoeMark);
				scheduleOnce(new TargetSelector(this, "callShoeBom(float, Object)", new Object[] { 0f, (Object)b }), 2.5f);
			}
		}
	}

	/**
	 * delay for calling addPostStepCallback
	 */
	public void callShoeBom(float value, Object b){
		mSpace.addPostStepCallback(this, (Shape)b, new CollisionType(CollisionType.SHOE_TYPE, CollisionType.TIMES_ONE));
	}
	
	@Override
	public void onStart(int arg0) {
	}

	@Override
	public void onStop(int arg0) {
		chipmunk.removeChild(Action.from(arg0).getTarget(), true);
	}

	@Override
	public void onUpdate(int arg0, float arg1) {
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}
	
}
