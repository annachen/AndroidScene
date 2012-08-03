package com.scene;

import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CvCamera extends SurfaceView implements SurfaceHolder.Callback, Runnable{
	private static final String TAG = "Scene::CvCamera";

	protected SurfaceHolder       mHolder;
	protected VideoCapture        mCamera;

	//protected Handler mMainHandler;
	protected Mat mBuffer;
	protected Bitmap mBitmap;
	protected Processor mProcessor;
	
	protected int mFrameCount;
	
	protected void init(){
		mHolder = getHolder();
		mHolder.addCallback(this);
		
		mBitmap = null;
		mFrameCount = 0;
		mProcessor = new Processor();
		
		synchronized(this){ 
			mBuffer = new Mat();
		}
		
		Log.i(TAG, "Instantiated new " + this.getClass());
	}
	
	public CvCamera(Context context) {
		super(context);
		init();
	}

	public CvCamera(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init();
	}

	public CvCamera(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init();
	}
	
	public boolean openCamera() {
		Log.i(TAG, "openCamera");
		synchronized (this) {
			releaseCamera();
			mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID);
			if (!mCamera.isOpened()) {
				mCamera.release();
				mCamera = null;
				Log.e(TAG, "Failed to open native camera");
				return false;
			}
		}
		return true;
	}

	public void releaseCamera() {
		Log.i(TAG, "releaseCamera");
		synchronized (this) {
			if (mCamera != null) {
				mCamera.release();
				mCamera = null;
			}
		}
	}

	public void setupCamera(int width, int height) {
		Log.i(TAG, "setupCamera("+width+", "+height+")");
		synchronized (this) {
			if (mCamera != null && mCamera.isOpened()) {
				List<Size> sizes = mCamera.getSupportedPreviewSizes();
				int mFrameWidth = width;
				int mFrameHeight = height;

				// selecting optimal camera preview size
				{
					double minDiff = Double.MAX_VALUE;
					for (Size size : sizes) {
						if (Math.abs(size.height - height) < minDiff) {
							mFrameWidth = (int) size.width;
							mFrameHeight = (int) size.height;
							minDiff = Math.abs(size.height - height);
						}
					}
				}

				mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
				mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
			}
		}

	}
	
	public void surfaceChanged(SurfaceHolder _holder, int format, int width, int height) {
		Log.i(TAG, "surfaceChanged");
		setupCamera(width, height);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG, "surfaceCreated");
		
		new Thread(mProcessor).start();
		
		//mMainHandler = new Handler(Looper.getMainLooper());
		//mMainHandler.post(this);
		
		(new Thread(this)).start();
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.i(TAG, "surfaceDestroyed");
		releaseCamera();
	}
	
	
	public void run() {
		
		while (true) {

			synchronized (this) {
				if (mCamera == null)
					return;

				if (!mCamera.grab()) {
					Log.e(TAG, "mCamera.grab() failed");
					return;
				}
				
				grabFrame(mCamera);
				
				Mat m;
				//if(mFrameCount %2 == 0){
					mProcessor.addFrame(mBuffer);
					m = mProcessor.getOutput();
				//}
				//else
				//	m = null;
				if(m != null){
					mBitmap = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
					Utils.matToBitmap(m, mBitmap);
				}
				else{
					mBitmap = Bitmap.createBitmap(mBuffer.cols(), mBuffer.rows(), Bitmap.Config.ARGB_8888);
					Utils.matToBitmap(mBuffer, mBitmap);
				}

				if (mBitmap != null) {
					Canvas canvas = mHolder.lockCanvas();
					if (canvas != null) {
						canvas.drawBitmap(mBitmap, (canvas.getWidth() - mBitmap.getWidth()) / 2, (canvas.getHeight() - mBitmap.getHeight()) / 2, null);
						mHolder.unlockCanvasAndPost(canvas);
					}
					mBitmap.recycle();
				}
				
				mFrameCount++;
			}
			/*
			try{
				Thread.sleep(100);
			} catch(Exception e) {
				Log.e(TAG, e.toString());
			}
			 */
			//mMainHandler.post(this);
		}
		
	}
	
	protected Bitmap grabFrameAsBitmap(VideoCapture capture){
		capture.retrieve(mBuffer, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
				
		Bitmap bmp = Bitmap.createBitmap(mBuffer.cols(), mBuffer.rows(), Bitmap.Config.ARGB_8888);
		try {
			Utils.matToBitmap(mBuffer, bmp);
			return bmp;
		} catch(Exception e) {
			Log.e("Scene", "Utils.matToBitmap() throws an exception: " + e.getMessage());
			bmp.recycle();
			return null;
		}	
	}
	
	protected void grabFrame(VideoCapture capture){
		capture.retrieve(mBuffer, Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
	}
	
	protected void outputToCanvas(){
		Log.d(TAG, "output!");
		if (mBitmap != null) {
			Canvas canvas = mHolder.lockCanvas();
			if (canvas != null) {
				canvas.drawBitmap(mBitmap, (canvas.getWidth() - mBitmap.getWidth()) / 2, (canvas.getHeight() - mBitmap.getHeight()) / 2, null);
				mHolder.unlockCanvasAndPost(canvas);
			}
			mBitmap.recycle();
		}
	}
	
	public Mat getBuffer(){
		return mBuffer;
	}
	
	public int getFrameCount(){
		return mFrameCount;
	}
}
