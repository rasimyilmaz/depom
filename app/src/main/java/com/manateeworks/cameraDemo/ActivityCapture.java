package com.manateeworks.cameraDemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;

import com.manateeworks.BarcodeScanner;
import com.manateeworks.BarcodeScanner.MWResult;
import com.manateeworks.CameraManager;
import com.manateeworks.MWOverlay;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * The barcode reader activity itself. This is loosely based on the
 * CameraPreview example included in the Android SDK.
 */
public final class ActivityCapture extends Activity implements SurfaceHolder.Callback {
	public static final boolean PDF_OPTIMIZED = false;
	public static final int USE_RESULT_TYPE = BarcodeScanner.MWB_RESULT_TYPE_MW;
	public static final Rect RECT_LANDSCAPE_1D = new Rect(3, 20, 94, 60);
	public static final Rect RECT_LANDSCAPE_2D = new Rect(20, 5, 60, 90);
	public static final Rect RECT_PORTRAIT_1D = new Rect(20, 3, 60, 94);
	public static final Rect RECT_PORTRAIT_2D = new Rect(20, 5, 60, 90);
	public static final Rect RECT_FULL_1D = new Rect(3, 3, 94, 94);
	public static final Rect RECT_FULL_2D = new Rect(20, 5, 60, 90);
	public static final Rect RECT_DOTCODE = new Rect(30, 20, 40, 60);
	private static final String MSG_CAMERA_FRAMEWORK_BUG = "Sorry, the Android camera encountered a problem: ";
	public static final int ID_AUTO_FOCUS = 0x01;
	public static final int ID_DECODE = 0x02;
	public static final int ID_RESTART_PREVIEW = 0x04;
	public static final int ID_DECODE_SUCCEED = 0x08;
	public static final int ID_DECODE_FAILED = 0x10;

	private Handler decodeHandler;
	private boolean hasSurface;
	private String package_name;
	private int activeThreads = 0;
	public static int MAX_THREADS = Runtime.getRuntime().availableProcessors();
	private boolean surfaceChanged = false;
	private enum State {
		STOPPED, PREVIEW, DECODING
	}
	State state = State.STOPPED;
	public Handler getHandler() {
		return decodeHandler;
	}
	@Override
	public void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		surfaceChanged = false;
		package_name = getPackageName();
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(getResources().getIdentifier("capture", "layout", package_name));
		// register your copy of the mobiScan SDK with the given user name / key
		BarcodeScanner.MWBregisterCode(BarcodeScanner.MWB_CODE_MASK_EANUPC, "rasimyilmaz", "1905kal");
		// choose code type or types you want to search for
		if (PDF_OPTIMIZED) {
			BarcodeScanner.MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL);
			BarcodeScanner.MWBsetActiveCodes(BarcodeScanner.MWB_CODE_MASK_PDF);
			BarcodeScanner.MWBsetScanningRect(BarcodeScanner.MWB_CODE_MASK_PDF, RECT_LANDSCAPE_1D);
		} else {
			// Our sample app is configured by default to search both
			// directions...
			BarcodeScanner.MWBsetDirection(BarcodeScanner.MWB_SCANDIRECTION_HORIZONTAL | BarcodeScanner.MWB_SCANDIRECTION_VERTICAL);
			// Our sample app is configured by default to search all supported
			// barcodes...
			BarcodeScanner.MWBsetActiveCodes(BarcodeScanner.MWB_CODE_MASK_EANUPC);
			// set the scanning rectangle based on scan direction(format in pct:
			// x, y, width, height)
            BarcodeScanner.MWBsetScanningRect( BarcodeScanner.MWB_CODE_MASK_EANUPC, RECT_PORTRAIT_2D);
		}
		BarcodeScanner.MWBsetLevel(2);
		BarcodeScanner.MWBsetResultType(USE_RESULT_TYPE);
		//Set minimum result length for low-protected barcode types
		BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_25, 5);
		BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_MSI, 5);
		BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_39, 5);
		BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_CODABAR, 5);
		BarcodeScanner.MWBsetMinLength(BarcodeScanner.MWB_CODE_MASK_11, 5);
		CameraManager.init(getApplication());
		hasSurface = false;
		state = State.STOPPED;
		decodeHandler = new Handler(new Handler.Callback() {

			@Override
			public boolean handleMessage(Message msg) {
				// TODO Auto-generated method stub

				switch (msg.what) {
				case ID_DECODE:
					decode((byte[]) msg.obj, msg.arg1, msg.arg2);
					break;

				case ID_AUTO_FOCUS:
					// When one auto focus pass finishes, start another. This is
					// the
					// closest thing to
					// continuous AF. It does seem to hunt a bit, but I'm not
					// sure what
					// else to do.
					if (state == State.PREVIEW || state == State.DECODING) {
						CameraManager.get().requestAutoFocus(decodeHandler, ID_AUTO_FOCUS);
					}
					break;
				case ID_RESTART_PREVIEW:
					restartPreviewAndDecode();
					break;
				case ID_DECODE_SUCCEED:

					// Bundle bundle = message.getData();
					// Bitmap barcode = bundle == null ? null : (Bitmap)
					// bundle.getParcelable(DecodeThread.BARCODE_BITMAP);
					state = State.STOPPED;
					handleDecode((MWResult) msg.obj);
    				break;
				case ID_DECODE_FAILED:
					// We're decoding as fast as possible, so when one decode
					// fails,
					// start another.
					// state = State.PREVIEW;
					// CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
					// R.id.decode);
					break;
				}
				return false;
			}
		});
	}
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(getResources().getIdentifier("preview_view", "id", package_name));
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		MWOverlay.addOverlay(this, surfaceView);
		if (hasSurface) {
			Log.i("Init Camera", "On resume");
			initCamera(surfaceHolder);
		} else {
    		surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		MWOverlay.removeOverlay();
		CameraManager.get().stopPreview();
		CameraManager.get().closeDriver();
		state = State.STOPPED;
	}
	@Override
	public void onConfigurationChanged(Configuration config) {
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		int rotation = display.getRotation();
		CameraManager.get().updateCameraOrientation(rotation);
		super.onConfigurationChanged(config);
	}
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			// Log.i("Init Camera", "On Surface created");
			// initCamera(holder);
		}
	}
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;
	}
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		Log.i("Init Camera", "On Surface changed");
		initCamera(holder);
		surfaceChanged = true;
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_FOCUS || keyCode == KeyEvent.KEYCODE_CAMERA) {
			// Handle these events so they don't launch the Camera app
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	private void decode(final byte[] data, final int width, final int height) {
		if (activeThreads >= MAX_THREADS || state == State.STOPPED) {
			return;
		}
		new Thread(new Runnable() {
			public void run() {
				activeThreads++;
				long start = System.currentTimeMillis();
				byte[] rawResult = null;
				rawResult = BarcodeScanner.MWBscanGrayscaleImage(data, width, height);
				if (state == State.STOPPED) {
					activeThreads--;
					return;
				}
				MWResult mwResult = null;
				if (rawResult != null && BarcodeScanner.MWBgetResultType() == BarcodeScanner.MWB_RESULT_TYPE_MW) {
					BarcodeScanner.MWResults results = new BarcodeScanner.MWResults(rawResult);
					if (results.count > 0) {
						mwResult = results.getResult(0);
						rawResult = mwResult.bytes;
					}
				}
				if (mwResult != null)
				{
					state = State.STOPPED;
					Message message = Message.obtain(ActivityCapture.this.getHandler(), ID_DECODE_SUCCEED, mwResult);
					message.arg1 = mwResult.type;
					message.sendToTarget();
				} else {
					Message message = Message.obtain(ActivityCapture.this.getHandler(), ID_DECODE_FAILED);
					message.sendToTarget();
				}
				activeThreads--;
			}
		}).start();
	}
	private void restartPreviewAndDecode() {
		if (state == State.STOPPED) {
			state = State.PREVIEW;
			Log.i("preview", "requestPreviewFrame.");
			CameraManager.get().requestPreviewFrame(getHandler(), ID_DECODE);
			CameraManager.get().requestAutoFocus(getHandler(), ID_AUTO_FOCUS);
		}
	}
	public void handleDecode(MWResult result) {
		byte[] rawResult = null;
		if (result != null && result.bytes != null) {
			rawResult = result.bytes;
		}

		String s = "";

		try {
			s = new String(rawResult, "UTF-8");
		} catch (UnsupportedEncodingException e) {

			s = "";
			for (int i = 0; i < rawResult.length; i++)
				s = s + (char) rawResult[i];
			e.printStackTrace();
		}
		int bcType = result.type;
		String typeName = "";
		switch (bcType) {
		case BarcodeScanner.FOUND_25_INTERLEAVED:
			typeName = "Code 25 Interleaved";
			break;
		case BarcodeScanner.FOUND_25_STANDARD:
			typeName = "Code 25 Standard";
			break;
		case BarcodeScanner.FOUND_128:
			typeName = "Code 128";
			break;
		case BarcodeScanner.FOUND_39:
			typeName = "Code 39";
			break;
		case BarcodeScanner.FOUND_93:
			typeName = "Code 93";
			break;
		case BarcodeScanner.FOUND_AZTEC:
			typeName = "AZTEC";
			break;
		case BarcodeScanner.FOUND_DM:
			typeName = "Datamatrix";
			break;
		case BarcodeScanner.FOUND_EAN_13:
			typeName = "EAN 13";
			break;
		case BarcodeScanner.FOUND_EAN_8:
			typeName = "EAN 8";
			break;
		case BarcodeScanner.FOUND_NONE:
			typeName = "None";
			break;
		case BarcodeScanner.FOUND_RSS_14:
			typeName = "Databar 14";
			break;
		case BarcodeScanner.FOUND_RSS_14_STACK:
			typeName = "Databar 14 Stacked";
			break;
		case BarcodeScanner.FOUND_RSS_EXP:
			typeName = "Databar Expanded";
			break;
		case BarcodeScanner.FOUND_RSS_LIM:
			typeName = "Databar Limited";
			break;
		case BarcodeScanner.FOUND_UPC_A:
			typeName = "UPC A";
			break;
		case BarcodeScanner.FOUND_UPC_E:
			typeName = "UPC E";
			break;
		case BarcodeScanner.FOUND_PDF:
			typeName = "PDF417";
			break;
		case BarcodeScanner.FOUND_QR:
			typeName = "QR";
			break;
		case BarcodeScanner.FOUND_CODABAR:
			typeName = "Codabar";
			break;
		case BarcodeScanner.FOUND_128_GS1:
			typeName = "Code 128 GS1";
			break;
		case BarcodeScanner.FOUND_ITF14:
			typeName = "ITF 14";
			break;
		case BarcodeScanner.FOUND_11:
			typeName = "Code 11";
			break;
		case BarcodeScanner.FOUND_MSI:
			typeName = "MSI Plessey";
			break;
		case BarcodeScanner.FOUND_25_IATA:
			typeName = "IATA Code 25";
			break;
		}
		if (result.locationPoints != null && CameraManager.get().getCurrentResolution() != null) {

			MWOverlay.showLocation(result.locationPoints.points, result.imageWidth, result.imageHeight);
		}
		if (result.isGS1) {
			typeName += " (GS1)";
		}
		if (bcType >= 0)
        {
            Intent return_result = new Intent("com.example.RESULT_ACTION", Uri.parse(s));
            setResult(Activity.RESULT_OK, return_result);
            finish();
        }
	}
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			if (PDF_OPTIMIZED) {
				CameraManager.setDesiredPreviewSize(1280, 720);
			} else {
				CameraManager.setDesiredPreviewSize(800, 480);
			}
			CameraManager.get().openDriver(surfaceHolder, (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT));
		} catch (IOException ioe) {
			displayFrameworkBugMessageAndExit(ioe.getMessage());
			return;
		} catch (RuntimeException e) {
			// Barcode Scanner has seen crashes in the wild of this variety:
			// java.?lang.?RuntimeException: Fail to connect to camera service
			displayFrameworkBugMessageAndExit(e.getMessage());
			return;
		}
		Log.i("preview", "start preview.");
		CameraManager.get().startPreview();
		restartPreviewAndDecode();
	}
	private void displayFrameworkBugMessageAndExit(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getResources().getIdentifier("app_name", "string", package_name));
		builder.setMessage(MSG_CAMERA_FRAMEWORK_BUG + message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialogInterface, int i) {
				finish();
			}
		});
		builder.show();
	}
}
