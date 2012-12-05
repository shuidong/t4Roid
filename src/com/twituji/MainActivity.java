package com.twituji;

import com.wiyun.engine.nodes.Director;
import com.wiyun.engine.opengl.WYGLSurfaceView;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends Activity {
	static {
		System.loadLibrary("xml2");
		System.loadLibrary("wiengine");
//		System.loadLibrary("lua");
		System.loadLibrary("chipmunk");
//		System.loadLibrary("box2d");
		System.loadLibrary("wisound");
	}
	
	protected WYGLSurfaceView mGLView;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        mGLView = new WYGLSurfaceView(this);
        setContentView(mGLView);
        
        // demo controls music stream
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // 设置显示帧率，程序发布时应该去掉
        Director.getInstance().setDisplayFPS(true);

        // 开始运行第一个场景
        Director.getInstance().runWithScene(new LogoScene());
    }

	@Override
    public void onPause() {
        super.onPause();

        Director.getInstance().pause();
    }

    @Override
    public void onResume() {
        super.onResume();

        Director.getInstance().resume();
    }

    @Override
    public void onDestroy() {
    	Director.getInstance().end();
    	
        super.onDestroy();
    }
    
    /**
     * 如果用到了TextBox, 则需要这个方法. 这个方法保证WiEngine能收到back键事件
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
    	if(event.getKeyCode() == KeyEvent.KEYCODE_BACK)
    		return mGLView.dispatchKeyEvent(event);
    	else
    		return super.dispatchKeyEvent(event);
    }
}