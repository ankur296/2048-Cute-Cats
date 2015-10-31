package corp.seedling.game2048.cute.cats.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.example.games.basegameutils.BaseGameActivity;

import corp.seedling.game2048.cute.cats.R;
import corp.seedling.game2048.cute.cats.logic.GameLogic;

public class GameScreen extends BaseGameActivity implements GameListener
{

	GameView view; 
	private View exitview;  
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String SCORE = "score";
	public static final String HIGH_SCORE = "high score temp";
	public static final String UNDO_SCORE = "undo score";
	public static final String CAN_UNDO = "can undo";
	public static final String UNDO_GRID = "undo";
	public static final String GAME_STATE = "game state";
	public static final String UNDO_GAME_STATE = "undo game state";

	private WindowManager.LayoutParams mWindowParams = null;
	private WindowManager mWin;
	private AlertDialog alertDialog; 
	private float mDensity;
	private GoogleApiClient googleApiClient;
	private AchievementsUnlocked mAchievementsUnlocked;
	public static boolean mConnected = false;

	private ArrayList<ResolveInfo> mShareApplList = new ArrayList<ResolveInfo>();
	private ShareAppListAdapter mAdapter;
	PopupWindow mPopupWindow;

	private boolean mShareWindow;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("Ankur oncreate");
		super.onCreate(savedInstanceState);

		if(mConnected)
			googleApiClient = getApiClient();
		mAchievementsUnlocked = new AchievementsUnlocked(this, googleApiClient);
		view = new GameView(this,mAchievementsUnlocked, this);//getBaseContext());

		setContentView(view);
		mContext = this.getApplicationContext();
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		view.hasSaveState = settings.getBoolean("save_state", false);

		if (savedInstanceState != null) {
			if (savedInstanceState.getBoolean("hasState")) {
				load();
			}
		} 

//		LayoutInflater inflater = (LayoutInflater)  getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
//		exitview = inflater.inflate(R.layout.exitviewlayout, null, false);
//		RelativeLayout mAddRect = (RelativeLayout)exitview.findViewById(R.id.adlayout1);
		
		alertDialog = new AlertDialog.Builder(this).create();
		if(alertDialog != null)
			alertDialog.setTitle(getString(R.string.exit_app));
		alertDialog.setView(exitview);
		
//		mDensity = getDensityName(getApplicationContext());
		
//		if(mDensity <= 2.3)
//		{
//			mAddRect.setVisibility(View.GONE); 
//		}
		
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,"OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
//				if(mDensity < 2.3)
//				{
					if(interstitialAds.isLoaded())
						interstitialAds.show();   
//				}
				GameScreen.this.finish();
			}
		});
		
		alertDialog.setButton(DialogInterface.BUTTON_NEUTRAL,"RESTART", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				view.game.newGame();
			}
		});
		
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,"CANCEL", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
			}
		});
		
//		mAdView2 = (AdView)exitview.findViewById(R.id.google_addrext);
//
//
//		layout = new RelativeLayout(this);
//		mWindowParams = new WindowManager.LayoutParams();
//		mWindowParams.gravity = Gravity.BOTTOM;
//		mWindowParams.x = 0;
//		mWindowParams.y = 0;
//		mWindowParams.height = WindowManager.LayoutParams.MATCH_PARENT;
//		mWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT;
//		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
//				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
//				| WindowManager.LayoutParams.FLAG_FULLSCREEN
//				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
//				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
//		mWindowParams.format = PixelFormat.TRANSLUCENT;
//		mWindowParams.windowAnimations = 0;
//		mWin = getWindowManager();

//		new RateMe().app_launched(this);
	}

	RelativeLayout layout;
	AdView adView;
//	private AdView mAdView2;
	private InterstitialAd interstitialAds;
	private Context mContext;
	private View mToatView;  

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("Ankur ondestroy");
		view.recycleBitmaps();
		GameLogic.unloadSound();
		/*mWin.removeView(layout);
		mWin.removeView(view);*/
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			// Do nothing
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			view.game.move(2);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			view.game.move(0);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			view.game.move(3);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			view.game.move(1);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putBoolean("hasState", true);
		save();
	}

	protected void onPause() {
		System.out.println("Ankur onPause");
		super.onPause();
		save();
		mWin.removeView(adView); //admob
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		System.out.println("ankur onstop");
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		System.out.println("ankur onstart");

	}

	private void save() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = settings.edit();
		Tile[][] field = view.game.grid.field;
		Tile[][] undoField = view.game.grid.undoField;
		editor.putInt(WIDTH, field.length);
		editor.putInt(HEIGHT, field.length);
		for (int xx = 0; xx < field.length; xx++) {
			for (int yy = 0; yy < field[0].length; yy++) {
				if (field[xx][yy] != null) {
					editor.putInt(xx + " " + yy, field[xx][yy].getValue());
				} else {
					editor.putInt(xx + " " + yy, 0);
				}

				if (undoField[xx][yy] != null) {
					editor.putInt(UNDO_GRID + xx + " " + yy,
							undoField[xx][yy].getValue());
				} else {
					editor.putInt(UNDO_GRID + xx + " " + yy, 0);
				}
			}
		}
//		view.game.gameState = settings.getInt(GAME_STATE, view.game.gameState);
		editor.putLong(SCORE, view.game.score);
		editor.putLong(HIGH_SCORE, view.game.highScore);
		editor.putLong(UNDO_SCORE, view.game.lastScore);
		editor.putBoolean(CAN_UNDO, view.game.canUndo);
		editor.putInt(GAME_STATE, view.game.gameState);
		editor.putInt(UNDO_GAME_STATE, view.game.lastGameState);
		editor.commit();
	}

	public  void googlePlay(){

		//for google play


		if (mConnected)
		{
			startActivityForResult(Games.Achievements.getAchievementsIntent(getApiClient()), 10);
			return;
		}
		beginUserInitiatedSignIn();
	}

	public  void viewAchievements(){
		startActivityForResult(Games.Achievements.getAchievementsIntent(
				getApiClient()), 1);
	}

	public GoogleApiClient getGoogleApiClient(){
		System.out.println("ankur enter getgoogle..");
		if (mConnected) {
			System.out.println("ankur since connected so call getapi");
			return getApiClient();
		}

		return null;
	}


	public  void beginUserSignIn(){
		System.out.println("ankur received sign in request");
		beginUserInitiatedSignIn();
	}


	protected void onResume() {
		System.out.println("Ankur onRsume");
		super.onResume();

		load();
		initAdMobXML();
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
				"Mute", false) == true)
			GameLogic.isMute = true;
		else
			GameLogic.isMute = false;  

		mWindowParams = new WindowManager.LayoutParams();
		mWindowParams.gravity = Gravity.BOTTOM;
		mWindowParams.x = 0;
		mWindowParams.y = 0;
		mWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		mWindowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
				| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
		mWindowParams.format = PixelFormat.TRANSLUCENT;
		mWindowParams.windowAnimations = 0;
		mWin = getWindowManager();

		// Create the adView
		adView = new AdView(this);

		adView.setAdSize(AdSize.BANNER); 
		adView.setAdUnitId("ca-app-pub-6816050713890894/1721569161");
		mWin.addView(adView, mWindowParams);

		AdRequest adRequest = new AdRequest.Builder().build();

		// Start loading the ad in the background.
		adView.loadAd(adRequest);
		if(mAchievementsUnlocked.mGoogleApiClient == null && mConnected){
			mAchievementsUnlocked.mGoogleApiClient = getApiClient();
		}
	}

	private void load() {
		// Stopping all animations
		view.game.aGrid.cancelAnimations();

		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		for (int xx = 0; xx < view.game.grid.field.length; xx++) {
			for (int yy = 0; yy < view.game.grid.field[0].length; yy++) {
				int value = settings.getInt(xx + " " + yy, -1);
				if (value > 0) {
					view.game.grid.field[xx][yy] = new Tile(xx, yy, value);
				} else if (value == 0) {
					view.game.grid.field[xx][yy] = null;
				}

				int undoValue = settings.getInt(UNDO_GRID + xx + " " + yy, -1);
				if (undoValue > 0) {
					view.game.grid.undoField[xx][yy] = new Tile(xx, yy,
							undoValue);
				} else if (value == 0) {
					view.game.grid.undoField[xx][yy] = null;
				}
			}
		}

		view.game.score = settings.getLong(SCORE, view.game.score);
		view.game.highScore = settings.getLong(HIGH_SCORE, view.game.highScore);
		view.game.lastScore = settings.getLong(UNDO_SCORE, view.game.lastScore);
		view.game.canUndo = settings.getBoolean(CAN_UNDO, view.game.canUndo);
		view.game.gameState = settings.getInt(GAME_STATE, view.game.gameState);
		view.game.lastGameState = settings.getInt(UNDO_GAME_STATE,
				view.game.lastGameState);
	}

	public void initAdMobXML() {
//		AdRequest adRequest = new AdRequest.Builder()
//		.build();
//		mAdView2.loadAd(adRequest);
		interstitialAds = new InterstitialAd(this.getApplicationContext());
		interstitialAds.setAdUnitId("ca-app-pub-6816050713890894/3198302367");

		// Create an ad request.
		AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
		interstitialAds.loadAd(adRequestBuilder.build());

	}

	@Override
	public void onBackPressed() {
		if (mPopupWindow != null && mPopupWindow.isShowing()){
			mPopupWindow.dismiss();
			return;
		}
		exitMsg();
	}


	private void exitMsg() {
		// Set the Icon for the Dialog
		alertDialog.setIcon(android.R.drawable.ic_menu_info_details);
		alertDialog.show();
	}


	private float getDensityName(Context context) {
		float density = context.getResources().getDisplayMetrics().density;
		return density;
	}

	@Override
	public void onSignInSucceeded() {
		System.out.println("Ankur sign in success");
		mConnected = true;
		if(mAchievementsUnlocked.mGoogleApiClient == null && mConnected){
			mAchievementsUnlocked.mGoogleApiClient = getApiClient();
		}
	}

	@Override
	public void onSignInFailed() {
		System.out.println("Ankur sign in fail");
		mConnected = false;
	}
	@Override
	protected void onActivityResult(int request, int response, Intent data) {
		// TODO Auto-generated method stub
		System.out.println("ankur on act re");
		super.onActivityResult(request, response, data);
	}

	@Override
	public void showMenu() {

		Typeface mFontStyle = Typeface.createFromAsset(getResources().getAssets(), "ClearSans-Bold.ttf");;

		LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
		View dialogView = inflater.inflate(R.layout.settings, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(GameScreen.this);
		alertDialogBuilder.setView(dialogView);  

		final AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.setIcon(R.drawable.ic_launcher);

		alertDialog.setTitle("Game Settings");

		
		Button bShare = (Button) dialogView.findViewById(R.id.button_Share);
		bShare.setText(R.string.share_us);
		bShare.setTypeface(mFontStyle);
		
		bShare.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				View Share = createPopup();
				
				try {
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (mPopupWindow == null) {
					mPopupWindow = new PopupWindow(Share,
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
				}
				showSharePopupMenu();
				alertDialog.dismiss();
				
			
			}
		});  
		
		Button b1 = (Button) dialogView.findViewById(R.id.button_achievements);
		b1.setText(R.string.achievements);
		b1.setTypeface(mFontStyle);
		b1.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				alertDialog.dismiss();

				if (mConnected)
				{
					System.out.println("ankur achieve.. connected");
					startActivityForResult(Games.Achievements.getAchievementsIntent(getGoogleApiClient()), 10);
					return; 
				}
				System.out.println("ankur achieve not connected");
				beginUserSignIn();


			}
		});         


		Button b2 = (Button) dialogView.findViewById(R.id.button_leaderboard);
		b2.setText(R.string.leader);
		b2.setTypeface(mFontStyle);
		b2.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				if (GameScreen.mConnected)
				{
//					startActivityForResult(Games.Leaderboards.getAllLeaderboardsIntent(getGoogleApiClient()), 11);
					//On selecting leaderboard option, show the list directly as there is just one leaderboard
					startActivityForResult(Games.Leaderboards.getLeaderboardIntent(getGoogleApiClient(), getResources().getString(R.string.leaderboard_leading_cats_n_lions)),11);

					return; 
				}
				beginUserSignIn();

				alertDialog.dismiss();
			}
		});


		final Button b3 = (Button) dialogView.findViewById(R.id.button_sound);
		b3.setTypeface(mFontStyle);

		if (view.game.isSoundMute() == true)
			b3.setText(R.string.sound_On);
		else					
			b3.setText(R.string.sound_Off);

		b3.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {

				view.game.revertMuteState();
				alertDialog.dismiss();
			}
		});


		Button b4 = (Button) dialogView.findViewById(R.id.button_rateus);
		b4.setTypeface(mFontStyle);
		b4.setText(R.string.rateus);
		b4.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=corp.seedling.game2048.cute.cats")));
				alertDialog.dismiss();

			}
		});

		
		Button bthPlayQuiz = (Button) dialogView.findViewById(R.id.button_play_quiz);
		bthPlayQuiz.setTypeface(mFontStyle);
		bthPlayQuiz.setText("Play 'Hollywood Quiz'");
		
		bthPlayQuiz.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=corp.seedling.guess.movie.hollywood")));
				alertDialog.dismiss();

			} 
		});
		
		Button bthPlay7Letters= (Button) dialogView.findViewById(R.id.button_play_7_letters);
		bthPlay7Letters.setTypeface(mFontStyle);
		bthPlay7Letters.setText("Play '7 Letters'");
		
		bthPlay7Letters.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=corp.seedling.seven.letters")));
				alertDialog.dismiss();
				
			} 
		});
		
		

		alertDialog.show();
	}



	private void showSharePopupMenu() {
		if (!mPopupWindow.isShowing()) {
			mShareWindow = false;
			mPopupWindow.setHeight(view.mHeight / 2);
			mPopupWindow.setWidth(view.mWidth);
			mPopupWindow.setAnimationStyle(R.style.ToastAnimationCaller);
			mPopupWindow.setOutsideTouchable(false);
			mPopupWindow.showAtLocation(findViewById(android.R.id.content),
					Gravity.LEFT | Gravity.BOTTOM, 0, 0);
		}
	}

	@Override
	public void gameWin(long score) {
		Log.e("Ankur", "Score Posted : "+ score);
		if(mConnected){
			Games.Leaderboards.submitScore(getApiClient(), getResources().getString(R.string.leaderboard_leading_cats_n_lions), score);
		}

	}

	@Override
	public void gameLose(long score) {

		Log.e("Ankur", "Score Posted : "+ score);
		if(mConnected){
			Games.Leaderboards.submitScore(getApiClient(), getResources().getString(R.string.leaderboard_leading_cats_n_lions), score);
		}

	}


	@SuppressLint("NewApi") protected View createPopup() {
		Drawable scoreRectangle = getResources()
				.getDrawable(R.drawable.score_rectangle);
		scoreRectangle.setAlpha(255);
		String type  = "text/*";
		initShareIntent(type);
		if (mShareApplList.size() == 0) { 
			Toast.makeText(mContext.getApplicationContext(),
					getString(R.string.no_app), 
					Toast.LENGTH_SHORT).show();
			return null;
		}
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mToatView = inflater.inflate(R.layout.share_app_list, null, false);
		RelativeLayout appBg = (RelativeLayout) mToatView
				.findViewById(R.id.sharelayout);
		if(Build.VERSION.SDK_INT < 16)
			appBg.setBackgroundDrawable(scoreRectangle);
		else
			appBg.setBackground(scoreRectangle);
		GridView shareAppList = (GridView) mToatView
				.findViewById(R.id.share_popup);
		shareAppList.setFocusable(true);
		shareAppList.setClickable(true);
		shareAppList.setFocusableInTouchMode(true);
		mAdapter = new ShareAppListAdapter(mContext.getApplicationContext());
		shareAppList.setAdapter(mAdapter);
		mAdapter.notifyDataSetInvalidated();
		if (mPopupWindow != null)
			mPopupWindow.setFocusable(true);
		shareAppList.setOnItemClickListener(new OnItemClickListener() {


			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				ResolveInfo tempinfo = mShareApplList.get(arg2);


				mContext.startActivity(getShareIntent(tempinfo.activityInfo.packageName));
				if (mPopupWindow != null && mPopupWindow.isShowing()) {
					mPopupWindow.dismiss();
				}



			}
		});

		return mToatView;

	}

	private Intent getShareIntent(String pkgName) {
		final Intent mShareIntent = new Intent(Intent.ACTION_SEND);

		mShareIntent.setType("text/plain");
		mShareIntent
		.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.install_app));
		mShareIntent
		.putExtra(
				Intent.EXTRA_TEXT,
				getString(R.string.extra_sharetext)
				+ " https://play.google.com/store/apps/details?id=corp.seedling.game2048.cute.cats");
		mShareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mShareIntent.setPackage(pkgName);
		return mShareIntent;
	}


	private class ShareAppListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public ShareAppListAdapter(Context context) {
			// Cache the LayoutInflate to avoid asking for a new one each time.
			mInflater = LayoutInflater.from(context);

		}

		public int getCount() {

			return mShareApplList.size();

		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.shareapp_layout_item,
						null);
				holder = new ViewHolder();
				holder.mShareAppText = (TextView) convertView
						.findViewById(R.id.shareapptext);
				holder.mShareAppIcon = (ImageView) convertView
						.findViewById(R.id.shareapplogo);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			ResolveInfo tempinfo = mShareApplList.get(position);
			String mDrawerStringData = tempinfo.loadLabel(
					mContext.getPackageManager()).toString();
			holder.mShareAppText.setText(mDrawerStringData);
			holder.mShareAppIcon.setImageDrawable(tempinfo.loadIcon(mContext
					.getPackageManager()));
			holder.mShareAppIcon.setTag(tempinfo.activityInfo.packageName);
			if(Build.VERSION.SDK_INT < 19){
				holder.mShareAppIcon.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mContext.startActivity(getShareIntent(v.getTag().toString()));

						if (mPopupWindow != null && mPopupWindow.isShowing()) {
							mPopupWindow.dismiss();
						}

					}
				});
			}
			return convertView;
		}

		class ViewHolder {
			ImageView mShareAppIcon;
			TextView mShareAppText;
		}
	}

	/**
	 * Method Creates the
	 */
	private void initShareIntent(String intentType) {
		mShareApplList.clear();
		Intent share = new Intent(android.content.Intent.ACTION_SEND);
		share.setType(intentType);
		final PackageManager pm = mContext.getApplicationContext()
				.getPackageManager();
		List<ResolveInfo> resInfo = pm.queryIntentActivities(share, 0);
		if (!resInfo.isEmpty()) {
			for (ResolveInfo info : resInfo) {
				mShareApplList.add(info);
			}
		}

	}
}
