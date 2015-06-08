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
		requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀바없애기위해사용

		setContentView(R.layout.splash);
		h = new Handler();
		h.postDelayed(irun, 1000);//인트로화면실행시간 약1초?
	}

	Runnable irun = new Runnable() {
		public void run() {
			Intent i = new Intent(IntroActivity.this, MainActivity.class);
			startActivity(i);
			finish();

			// fade in으로 시작하여 fade out으로 인트로 화면이 꺼지게 애니메이션 추가
			overridePendingTransition(android.R.anim.fade_in,
					android.R.anim.fade_out);
		}
	};

	// 인트로 화면 중간에 뒤로가기 버튼을 눌러서 꺼졌을 시 1초 후 메인 페이지가 뜨는 것을 방지
	@Override
	public void onBackPressed() {
		super.onBackPressed();
		h.removeCallbacks(irun);
	}

}




