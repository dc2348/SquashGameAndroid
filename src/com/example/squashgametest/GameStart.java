package com.example.squashgametest;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.server.*;

import com.example.squashgame.R;



//게임화면
public class GameStart extends BT_Preference implements TextToSpeech.OnInitListener{

	MyServer server;
	public static final int EXIT_BUTTON = 1;
	Handler handler;

	float[] accelerometerValues;
	float[] gyroscopeValues;

	int sensorA = Sensor.TYPE_ACCELEROMETER;
	int sensorG = Sensor.TYPE_GYROSCOPE;

	SensorManager aSensor = null;
	SensorManager gSensor = null;

	float X;
	float Y;
	float Z;
	float Roll;
	float Pitch;
	float Yaw;

	int accelerometerLock1 = 0, gyroscopeLock1 = 0;
	int accelerometerLock2 = 0, gyroscopeLock2 = 0;
	
	int startLock=0;
	
	private TextToSpeech tts;

	BluetoothAdapter mBluetoothAdapter;
	BluetoothSocket mmSocket;
	BluetoothDevice mmDevice;
	OutputStream mmOutputStream;
	InputStream mmInputStream;

	boolean is_connected = false;
	String devicename;

	Thread workerThread;
	byte[] readBuffer;
	int readBufferPosition;
	int counter;
	volatile boolean stopWorker;

	Button openButton;
	Button closeButton;
	Button sendButton;
	TextView info_textview;
	TextView info_textview2;
	EditText myTextbox;

	public SensorEventListener mySensorEventListener = new SensorEventListener() {
		public void onSensorChanged(SensorEvent sensorEvent) {
			if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				accelerometerValues = sensorEvent.values;

				X = accelerometerValues[0];
				Y = accelerometerValues[1];
				Z = accelerometerValues[2];
			}
			
			if (sensorEvent.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
				gyroscopeValues = sensorEvent.values;

				Roll = (float) Math.toDegrees(gyroscopeValues[0]);
				Pitch = (float) Math.toDegrees(gyroscopeValues[1]);
				Yaw = (float) Math.toDegrees(gyroscopeValues[2]);
			}

			
			//가속도센서가 먼저 범위안에 들었을경우
			if (X > 16 && Y < -16 && accelerometerLock1 == 0) { //숫자 16은 가속도
				accelerometerLock1 = 1;// 가속도센서 Lock
				if (Roll > 45 && Yaw > 45 && gyroscopeLock1 == 0) {//숫자 45는 자이로센서 각도 
					gyroscopeLock1 = 1;// 자이로센서 Lock
					Context context = getApplicationContext();
					String msg = "포핸드 성공!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, msg, duration);
					
					Log.d("log","forehand");
					server.sendToAll("f");
					toast.show();
				}
			}

			//Lock해제
			if (!(Roll > 60 && Yaw > 60)) {
				gyroscopeLock1 = 0;
			}
			if (!(X > 16 && Y < -16)) {
				accelerometerLock1 = 0;
			}

			//자이로센서가 먼저 범위안에 들었을경우
			if (Roll < -80 && Yaw < 0 && gyroscopeLock2 == 0) {
				gyroscopeLock2 = 1; // 자이로센서 Lock
				if (X > 16 && Y < -16 && accelerometerLock2 == 0) {
					accelerometerLock2 = 1; // 가속도센서 Lock
					Context context = getApplicationContext();
					String msg = "백핸드 성공!";
					int duration = Toast.LENGTH_SHORT;
					Toast toast = Toast.makeText(context, msg, duration);
					
					server.sendToAll("b");
					toast.show();
				}
			}

			//Lock해제
			if (!(X > 16 && Y < -16)) {
				accelerometerLock2 = 0;
			}
			if (!(Roll < -80 && Yaw < 0)) {
				gyroscopeLock2 = 0;
			}

		}

		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.game_start);

		Button backButton = (Button) findViewById(R.id.backButton);
		backButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				showDialog(EXIT_BUTTON);
			}
		});
		
		Button startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				
				//if(startLock==0){
				//	Toast.makeText(getApplicationContext(), "game start.", Toast.LENGTH_SHORT).show();
				//	startLock=1;
					server.sendToAll("s");
				//}
			}
		});

		// sensor
		aSensor = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		gSensor = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		try {
			server = new MyServer(12345);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		server.start();
		Log.d("log","Server Conneccted");
		
		String strText = "MPUCAFE";
		SharedPreferences mPairedSettings;
		mPairedSettings = getSharedPreferences(BT_PREFERENCES,
				Context.MODE_PRIVATE);

		Editor editor = mPairedSettings.edit();
		editor.putString(BP_PREFERENCES_PAIRED_DEVICE, strText);
		editor.commit();

		if (!mPairedSettings.contains(BP_PREFERENCES_PAIRED_DEVICE)) {
		}
		devicename = mPairedSettings
				.getString(BP_PREFERENCES_PAIRED_DEVICE, "");

		final IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		final BroadcastReceiver mReceiver = new ScreenReceiver();
		registerReceiver(mReceiver, filter);

		tts = new TextToSpeech(this, this);

		//info_textview = (TextView) findViewById(R.id.textview_info);		
	}
	
	@Override
	public void onBackPressed() {
		try {
			String msg = "stop";
			msg += "\n";
			mmOutputStream.write(msg.getBytes());
			try {

				Thread.sleep(500);

			} catch (InterruptedException e) {
			}
			closeBT();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}

		} catch (IOException ex) {
		}

		super.onBackPressed();
	}

	public void onInit(int status) {

		if (status == TextToSpeech.SUCCESS) {

			int result = tts.setLanguage(Locale.US);

			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			} else {
				showToast("start");

				try {
					findBT();
					openBT();

				} catch (IOException ex) {
					showToast("fail to open connection.");
				}
			}
		} else {
			Log.e("TTS", "Initilization Failed!");
		}

	}

	private void showToast(String txt) {

		Toast.makeText(this, txt, Toast.LENGTH_SHORT).show();

	}

	@Override
	protected void onResume() {
		super.onResume();

		aSensor.registerListener(mySensorEventListener,
				aSensor.getDefaultSensor(sensorA),
				SensorManager.SENSOR_DELAY_GAME);
		gSensor.registerListener(mySensorEventListener,
				gSensor.getDefaultSensor(sensorG),
				SensorManager.SENSOR_DELAY_GAME);
		
		handler = new Handler();
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		aSensor.unregisterListener(mySensorEventListener);
		
		if (ScreenReceiver.wasScreenOn) {

			try {
				String msg = "stop";
				msg += "\n";
				mmOutputStream.write(msg.getBytes());
				try {

					Thread.sleep(1000);

				} catch (InterruptedException e) {
				}
				closeBT();

				openButton.setEnabled(true);
				closeButton.setEnabled(false);

			} catch (IOException ex) {
			}

			// this is the case when onPause() is called by the system due to a
			// screen state change
			Log.e("MYAPP", "SCREEN TURNED OFF");
		} else {
			// this is when onPause() is called when the screen state has not
			// changed
		}
	}

	@Override
	@Deprecated
	protected Dialog onCreateDialog(int id) {
		AlertDialog dialog = null;

		switch (EXIT_BUTTON) {
		case 1:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure exit this game?");
			builder.setPositiveButton("no",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Toast.makeText(getApplicationContext(),
									"return to game.", Toast.LENGTH_LONG)
									.show();
						}
					});
			builder.setNegativeButton("yes",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							GameStart.this.finish();
						}
					});

			dialog = builder.create();
		}

		return dialog;
	}
	
	void findBT() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (mBluetoothAdapter == null) {
			showToast("No bluetooth adapter available");
		}

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBluetooth = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBluetooth, 0);
		}

		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				if (device.getName().equals(devicename)) {
					mmDevice = device;
					break;
				}
			}
		}
		// showToast("Bluetooth Device Found");
	}

	void openBT() throws IOException {
		UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); // Standard
																				// SerialPortService
																				// ID
		mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
		mmSocket.connect();
		mmOutputStream = mmSocket.getOutputStream();
		mmInputStream = mmSocket.getInputStream();

		beginListenForData();

		showToast("Connection Opened");
		is_connected = true;

		String msg = "start";
		msg += "\n";
		mmOutputStream.write(msg.getBytes());

		// msg = "blinkOn13";
		// msg += "\n";
		// mmOutputStream.write(msg.getBytes());
	}

	void closeBT() throws IOException {
		String msg = "stop";
		msg += "\n";
		mmOutputStream.write(msg.getBytes());
		try {

			Thread.sleep(100);

		} catch (InterruptedException e) {
		}

		stopWorker = true;
		mmOutputStream.close();
		mmInputStream.close();
		mmSocket.close();
		showToast("Connection Closed");

		is_connected = false;
	}

	void beginListenForData() {
		final Handler handler = new Handler();
		final byte delimiter = 10; // This is the ASCII code for a newline
									// character

		stopWorker = false;
		readBufferPosition = 0;
		readBuffer = new byte[1024];
		workerThread = new Thread(new Runnable() {
			public void run() {
				while (!Thread.currentThread().isInterrupted() && !stopWorker) {

					if (!is_connected) {
						continue;
					}

					try {
						int bytesAvailable = mmInputStream.available();
						if (bytesAvailable > 0) {
							byte[] packetBytes = new byte[bytesAvailable];
							mmInputStream.read(packetBytes);
							for (int i = 0; i < bytesAvailable; i++) {
								byte b = packetBytes[i];
								if (b == delimiter) {
									byte[] encodedBytes = new byte[readBufferPosition];
									System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
									
									final String data = new String(encodedBytes, "EUC-KR");
									readBufferPosition = 0;

									handler.post(new Runnable() {
										public void run() {

											//info_textview.setText(data);
											String tmp2 = (String) data;
											//String tmp; = (String) info_textview.getText();
											
											//info_textview.setText(tmp2);
											
											//showToast(data);
											//server.sendToAll("r");
											
											if(data.length()>2){//Left
												showToast(tmp2);
												server.sendToAll("l");
											}else{
												showToast(tmp2);//Right
												server.sendToAll("r");
											}
											
											/*
											if (tmp.indexOf("blocked") != -1) {
												showToast("blocked");

											} else if (tmp.indexOf("cleared") != -1) {
												showToast("cleared");
											}
											*/
										}
									});
								} else {
									readBuffer[readBufferPosition++] = b;
								}
							}
						}
					} catch (IOException ex) {
						stopWorker = true;
					}

					try {

						Thread.sleep(100);

					} catch (InterruptedException e) {
					}
				}
			}
		});
		workerThread.start();
	}
}