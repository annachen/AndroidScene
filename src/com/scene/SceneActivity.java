package com.scene;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.RelativeLayout;
import android.support.v4.app.NavUtils;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class SceneActivity extends Activity {

	protected static final String TAG = "SceneActivity";
	
	protected CvCamera mView;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack)){
			Log.e(TAG, "Cannot connect to OpenCV Manager");
		}
		else
			Log.i(TAG, "OpenCV Manager Loaded");
		
		//requestWindowFeature(Window.FEATURE_NO_TITLE);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_scene, menu);
		return true;
	}
	
	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
				case LoaderCallbackInterface.SUCCESS:
				{
					Log.i(TAG, "OpenCV loaded successfully");

					// Create and set View
					setContentView(R.layout.activity_scene);
					mView = (CvCamera) findViewById(R.id.mainView);
					
					//mView = new CvCamera(mAppContext);
					//setContentView(mView);
					
					// Check native OpenCV camera
					if( !mView.openCamera() ) {
						AlertDialog ad = new AlertDialog.Builder(mAppContext).create();
						ad.setCancelable(false); // This blocks the 'BACK' button
						ad.setMessage("Fatal error: can't open camera!");
						ad.setButton("OK", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								dialog.dismiss();
								finish();
							}
						});
						ad.show();
					}
				} break;
				default:
				{
					super.onManagerConnected(status);
				} break;
			}
		}
	};
	

	@Override
	protected void onPause() {
		Log.i(TAG, "onPause");
		super.onPause();
		if (null != mView)
			mView.releaseCamera();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "onResume");
		super.onResume();
		if((null != mView) && !mView.openCamera() ) {
			AlertDialog ad = new AlertDialog.Builder(this).create();  
			ad.setCancelable(false); // This blocks the 'BACK' button  
			ad.setMessage("Fatal error: can't open camera!");  
			ad.setButton("OK", new DialogInterface.OnClickListener() {  
				public void onClick(DialogInterface dialog, int which) {  
					dialog.dismiss();
					finish();
				}  
			});  
			ad.show();
		}
	}


}
