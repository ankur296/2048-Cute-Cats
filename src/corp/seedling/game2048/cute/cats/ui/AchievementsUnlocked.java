package corp.seedling.game2048.cute.cats.ui;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.preference.PreferenceManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;

import corp.seedling.game2048.cute.cats.R;
import corp.seedling.game2048.cute.cats.logic.GameLogic;

public class AchievementsUnlocked {

	private Activity mActivity;
	public GoogleApiClient mGoogleApiClient;
	private SharedPreferences settings;

	// Sound

	public AchievementsUnlocked(Activity app, GoogleApiClient googleApiClient) {
		mActivity = app;
		mGoogleApiClient = googleApiClient;
		settings = PreferenceManager.getDefaultSharedPreferences(mActivity);
	}

	public void case512() {

		boolean check256 = settings.getBoolean("Achievement512", true);
		if (check256 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement512", false);
			editor.commit();
			UnlockAchievements(mActivity.getString(R.string.achievement_ziggy), -1);
		}

	}

	public void case1024() {

		boolean check256 = settings.getBoolean("Achievement1024", true);
		if (check256 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement1024", false);
			editor.commit();
			UnlockAchievements(mActivity.getString(R.string.achievement_izzy), 200);
		}

	}
	public void case2048() {

		boolean check512 = settings.getBoolean("Achievement2048", true);
		if (check512 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement2048", false);
			editor.commit();
			UnlockAchievements(mActivity
					.getString(R.string.achievement_tiger) , 3);
		}

	}
	
	public void case4096() {

		boolean check512 = settings.getBoolean("Achievement4096", true);
		if (check512 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement4096", false);
			editor.commit();
			UnlockAchievements(mActivity
					.getString(R.string.achievement_ruby) , 4);
		}

	}
	
	public void case8192() {

		boolean check512 = settings.getBoolean("Achievement8192", true);
		if (check512 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement8192", false);
			editor.commit();
			UnlockAchievements(mActivity
					.getString(R.string.achievement_simba) , 5);
		}

	}
	
	public void case16384() {

		boolean check512 = settings.getBoolean("Achievement16384", true);
		if (check512 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement16384", false);
			editor.commit();
			UnlockAchievements(mActivity
					.getString(R.string.achievement_milo) , 6);
		}

	}
	
	public void case32768() {

		boolean check512 = settings.getBoolean("Achievement32768", true);
		if (check512 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement32768", false);
			editor.commit();
			UnlockAchievements(mActivity
					.getString(R.string.achievement_charlie) , 7);
		}

	}
	
	public void case65536() {

		boolean check512 = settings.getBoolean("Achievement65536", true);
		if (check512 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement65536", false);
			editor.commit();
			UnlockAchievements(mActivity
					.getString(R.string.achievement_smokey) , 8);
		}

	}
	
	public void case131072() {

		boolean check512 = settings.getBoolean("Achievement131072", true);
		if (check512 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement131072", false);
			editor.commit();
			UnlockAchievements(mActivity
					.getString(R.string.achievement_max) , 9);
		}

	}
	
	public void case262144() {

		boolean check512 = settings.getBoolean("Achievement262144", true);
		if (check512 && mGoogleApiClient != null) {
			SharedPreferences.Editor editor = settings.edit();
			editor.putBoolean("Achievement262144", false);
			editor.commit();
			UnlockAchievements(mActivity
					.getString(R.string.achievement_leo) , 10);
		}

	}

	
	public void UnlockAchievements(String id, int increment) {
		boolean mPlaySound;
		SoundPool mSoundPool;
		AudioManager audioManager;
		float volume;

		mPlaySound = settings.getBoolean("Mute", false);
		mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		audioManager = (AudioManager) mActivity
				.getSystemService(Context.AUDIO_SERVICE);

		float actualVolume = (float) audioManager
				.getStreamVolume(AudioManager.STREAM_MUSIC);
		float maxVolume = (float) audioManager
				.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		volume = maxVolume / actualVolume;

		try {
			if (!mPlaySound)
//				mSoundPool.play(mSoundPool.load(
//						mActivity.getApplicationContext(),
//						R.raw.whistling_and_cheering, 1), volume, volume, 1, 0,
//						1);
				GameLogic.SoundPlay(1);
			if(increment == -1){
				Games.Achievements.unlock(mGoogleApiClient, id);
			}else{
				Games.Achievements.increment(mGoogleApiClient, id, increment);
			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
