package com.twituji;

import com.wiyun.engine.nodes.MotionStreak;
import com.wiyun.engine.nodes.Node;
import com.wiyun.engine.nodes.Sprite;
import com.wiyun.engine.opengl.Texture2D;
import com.wiyun.engine.types.WYColor4B;
import com.wiyun.engine.types.WYPoint;

public class ShoeBodyData {
	private MotionStreak streak;
	private Sprite sprite;
	private Node node;
	private boolean removedMStreak;
	
	public WYPoint shotPoint = WYPoint.makeZero();
	public WYPoint shotVelocity = WYPoint.makeZero();
	
	public ShoeBodyData(int streakPic, int spritePic, Node node) {
		this.node = node;
		streak = MotionStreak.make(100f, Texture2D.makePNG(streakPic), new WYColor4B(0, 255, 0, 255));
		sprite = Sprite.make(spritePic);
		removedMStreak = true;//true
		
		node.addChild(streak);
		node.addChild(sprite);
	}
	
	public boolean isRemoveMStreak() {
		return removedMStreak;
	}

	public void setRemoveMStreak(boolean removedMStreak) {
		this.removedMStreak = removedMStreak;
	}

	public MotionStreak getStreak() {
		return streak;
	}
	public void setStreak(MotionStreak streak) {
		this.streak = streak;
	}
	public Sprite getSprite() {
		return sprite;
	}
	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}
	
	public void destroyMotionStreak(){
		if(removedMStreak)return;
		
		setRemoveMStreak(true);
		//streak.autoRelease();
		//node.removeChild(streak, true);
	}
	
	public void removeSprite(){
		sprite.autoRelease();
		node.removeChild(sprite, true);
		
	}
	
	public void destoryData(){
		removeSprite();
		streak.autoRelease();
		node.removeChild(streak, true);
	}
	
	
	
	
}
