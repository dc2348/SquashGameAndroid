package com.example.squashgametest;

import com.example.squashgame.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;

public class IntroActivity extends Activity {

	Handler h;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE); // Ÿ��Ʋ�پ��ֱ����ػ��

		setContentView(R.layout.splash);
		h = new Handler();
		h.postDelayed(irun, 1000);//��Ʈ��ȭ�����ð� ��1��?
	}

	Runnable irun = new Runnable() {
		public void run() {
			Intent i = new Intent(IntroActivity.this, MainActivity.class);
			startActivity(i);
			finish();

			// fade in���� �����Ͽ� fade out���� ��Ʈ�� ȭ���� ������ �ִϸ��̼� �߰�
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
		}
	};

	// ��Ʈ�� ȭ�� �߰��� �ڷΰ��� ��ư�� ������ ������ �� 1�� �� ���� �������� �ߴ� ���� ����
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		h.removeCallbacks(irun);
	}

}




