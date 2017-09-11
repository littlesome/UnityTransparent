package com.unity.ProductName;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.unity3d.player.UnityPlayer;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class UnityPlayerActivity extends Activity
{
    protected UnityPlayer mUnityPlayer; // don't change the name of this variable; referenced from native code
    protected Surface mSurface;
    protected SurfaceHolder.Callback mProxyCallback;
    protected ProxySurfaceHolder mProxySurfaceHolder = new ProxySurfaceHolder();

    class ProxySurfaceHolder implements SurfaceHolder {
        @Override
        public void addCallback(Callback callback) {
        }

        @Override
        public void removeCallback(Callback callback) {
        }

        @Override
        public boolean isCreating() {
            return false;
        }

        @Override
        public void setType(int type) {
        }

        @Override
        public void setFixedSize(int width, int height) {
        }

        @Override
        public void setSizeFromLayout() {
        }

        @Override
        public void setFormat(int format) {
        }

        @Override
        public void setKeepScreenOn(boolean screenOn) {
        }

        @Override
        public Canvas lockCanvas() {
            return null;
        }

        @Override
        public Canvas lockCanvas(Rect dirty) {
            return null;
        }

        @Override
        public void unlockCanvasAndPost(Canvas canvas) {
        }

        @Override
        public Rect getSurfaceFrame() {
            return null;
        }

        @Override
        public Surface getSurface() {
            return mSurface;
        }
    }

    // Setup activity layout
    @Override protected void onCreate (Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().setFormat(PixelFormat.RGBX_8888); // <--- This makes xperia play happy

        mUnityPlayer = new UnityPlayer(this);
        mUnityPlayer.setBackgroundResource(R.drawable.app_banner);

        setContentView(mUnityPlayer);
        mUnityPlayer.requestFocus();

        for (int i = 0; i < mUnityPlayer.getChildCount(); i++)
        {
            View child = mUnityPlayer.getChildAt(i);
            if (child instanceof SurfaceView)
            {
                SurfaceView surfaceView = (SurfaceView)child;

                ViewGroup.LayoutParams layoutParams = surfaceView.getLayoutParams();
                layoutParams.width = 0;
                layoutParams.height = 0;
                surfaceView.setLayoutParams(layoutParams);

                try {
                    Field field = SurfaceView.class.getDeclaredField("mCallbacks");
                    field.setAccessible(true);
                    ArrayList<SurfaceHolder.Callback> callbacks = (ArrayList<SurfaceHolder.Callback>)field.get(surfaceView);
                    mProxyCallback = callbacks.get(0);
                    synchronized (callbacks) {
                        callbacks.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                final TextureView view = new TextureView(this);
                view.setOpaque(false);
                view.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
                    @Override
                    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                        mSurface = new Surface(surface);
                        mProxyCallback.surfaceCreated(mProxySurfaceHolder);
                    }

                    @Override
                    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                        mProxyCallback.surfaceChanged(mProxySurfaceHolder, 0, width, height);
                    }

                    @Override
                    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                        mProxyCallback.surfaceDestroyed(mProxySurfaceHolder);
                        return false;
                    }

                    @Override
                    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                    }
                });

                mUnityPlayer.addView(view, i);
                break;
            }
        }
    }

    @Override protected void onNewIntent(Intent intent)
    {
        // To support deep linking, we need to make sure that the client can get access to
        // the last sent intent. The clients access this through a JNI api that allows them
        // to get the intent set on launch. To update that after launch we have to manually
        // replace the intent with the one caught here.
        setIntent(intent);
    }

    // Quit Unity
    @Override protected void onDestroy ()
    {
        mUnityPlayer.quit();
        super.onDestroy();
    }

    // Pause Unity
    @Override protected void onPause()
    {
        super.onPause();
        mUnityPlayer.pause();
    }

    // Resume Unity
    @Override protected void onResume()
    {
        super.onResume();
        mUnityPlayer.resume();
    }

    // Low Memory Unity
    @Override public void onLowMemory()
    {
        super.onLowMemory();
        mUnityPlayer.lowMemory();
    }

    // Trim Memory Unity
    @Override public void onTrimMemory(int level)
    {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_RUNNING_CRITICAL)
        {
            mUnityPlayer.lowMemory();
        }
    }

    // This ensures the layout will be correct.
    @Override public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        mUnityPlayer.configurationChanged(newConfig);
    }

    // Notify Unity of the focus change.
    @Override public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        mUnityPlayer.windowFocusChanged(hasFocus);
    }

    // For some reason the multiple keyevent type is not supported by the ndk.
    // Force event injection by overriding dispatchKeyEvent().
    @Override public boolean dispatchKeyEvent(KeyEvent event)
    {
        if (event.getAction() == KeyEvent.ACTION_MULTIPLE)
            return mUnityPlayer.injectEvent(event);
        return super.dispatchKeyEvent(event);
    }

    // Pass any events not handled by (unfocused) views straight to UnityPlayer
    @Override public boolean onKeyUp(int keyCode, KeyEvent event)     { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onKeyDown(int keyCode, KeyEvent event)   { return mUnityPlayer.injectEvent(event); }
    @Override public boolean onTouchEvent(MotionEvent event)          { return mUnityPlayer.injectEvent(event); }
    /*API12*/ public boolean onGenericMotionEvent(MotionEvent event)  { return mUnityPlayer.injectEvent(event); }
}
