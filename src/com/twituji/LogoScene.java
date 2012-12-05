package com.twituji;

import com.wiyun.engine.actions.DelayTime;
import com.wiyun.engine.actions.FadeIn;
import com.wiyun.engine.actions.FiniteTimeAction;
import com.wiyun.engine.actions.IntervalAction;
import com.wiyun.engine.actions.Sequence;
import com.wiyun.engine.actions.Action.Callback;
import com.wiyun.engine.nodes.Director;
import com.wiyun.engine.nodes.Scene;
import com.wiyun.engine.nodes.Sprite;
import com.wiyun.engine.types.WYSize;

public class LogoScene extends Scene {
	public LogoScene() {
		WYSize s = Director.getInstance().getWindowSize();
	    
	    Sprite sprite = Sprite.make(R.drawable.logo);
	    sprite.setAlpha(0);
	    addChild(sprite);
        sprite.setPosition(s.width / 2, s.height / 2);

        IntervalAction a = (IntervalAction)FadeIn.make(1f).autoRelease();//4
        FiniteTimeAction d = (FiniteTimeAction)DelayTime.make(0.5f).autoRelease();
        IntervalAction r = (IntervalAction)a.reverse().autoRelease();
        IntervalAction seq = (IntervalAction)Sequence.make(a,d,r).autoRelease();
        
        Callback callback = new Callback() {

			public void onStop(int actionPointer) {
				Director.getInstance().replaceScene(new GameScene());
			}

			@Override
			public void onStart(int arg0) {
			}

			@Override
			public void onUpdate(int arg0, float arg1) {
			}

		};
		
        seq.setCallback(callback);
        sprite.runAction(seq);
	    autoRelease(true);
    }
}
