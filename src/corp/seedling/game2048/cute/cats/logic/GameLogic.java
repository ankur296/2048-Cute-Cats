package corp.seedling.game2048.cute.cats.logic;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.games.Games;

import corp.seedling.game2048.cute.cats.R;
import corp.seedling.game2048.cute.cats.ui.AchievementsUnlocked;
import corp.seedling.game2048.cute.cats.ui.AnimGrid;
import corp.seedling.game2048.cute.cats.ui.Cell;
import corp.seedling.game2048.cute.cats.ui.GameListener;
import corp.seedling.game2048.cute.cats.ui.GameScreen;
import corp.seedling.game2048.cute.cats.ui.GameView;
import corp.seedling.game2048.cute.cats.ui.Grid;
import corp.seedling.game2048.cute.cats.ui.Tile;


public class GameLogic {
 
	public static final int SPAWN_ANIMATION = -1;
	public static final int MOVE_ANIMATION = 0;
	public static final int MERGE_ANIMATION = 1;  

	public static final int FADE_GLOBAL_ANIMATION = 0;

	public static final long MOVE_ANIMATION_TIME = GameView.BASE_ANIMATION_TIME;
	public static final long SPAWN_ANIMATION_TIME = GameView.BASE_ANIMATION_TIME;
	public static final long NOTIFICATION_ANIMATION_TIME = GameView.BASE_ANIMATION_TIME * 10;
	public static final long NOTIFICATION_DELAY_TIME = MOVE_ANIMATION_TIME + SPAWN_ANIMATION_TIME;
	private static final String HIGH_SCORE = "high score";

	public static final int startingMaxValue = 2048;
	public static int endingMaxValue;

	//Odd state = game is not active
	//Even state = game is active
	//Win state = active state + 1
	public static final int GAME_WIN = 1;
	public static final int GAME_LOST = -1;
	public static final int GAME_NORMAL = 0;
	public static final int GAME_NORMAL_WON = 1;
	public static final int GAME_ENDLESS = 2; 
	public static final int GAME_ENDLESS_WON = 3;

	public Grid grid = null;
	public AnimGrid aGrid;
	public final int numSquaresX = 4;
	public final int numSquaresY = 4;
	public final int startTiles = 2;

	public int gameState = 0;
	public boolean canUndo;

	public long score = 0;
	public long highScore = 0;

	public long lastScore = 0;
	public int lastGameState = 0;

	private long bufferScore = 0;
	private int bufferGameState = 0;

	private Context mContext;

	private GameView mView;
	private GameScreen gameScreen;
	private AchievementsUnlocked mAcheivementUnLock;
	private GameListener mGameListener;
	public GameLogic(Context context, AchievementsUnlocked achvment, GameView view, GameListener gameLsnr) {
		mContext = context;
		mView = view;
		mGameListener = gameLsnr;
		endingMaxValue = (int) Math.pow(2, view.numCellTypes - 1);
		new Thread(new Runnable() {

			@Override
			public void run() {
				initSound();				
			}
		}).start();
		mAcheivementUnLock = achvment;
		gameScreen = new GameScreen();
//		initAdMobXML();
	}

	public void newGame() {
		if (grid == null) {
			grid = new Grid(numSquaresX, numSquaresY);
		} else {
			prepareUndoState();
			saveUndoState();
			grid.clearGrid();
		}
		aGrid = new AnimGrid(numSquaresX, numSquaresY); 
		highScore = getHighScore();
		if (score >= highScore) {
			highScore = score;
			recordHighScore();
		}
		score = 0;
		gameState = GAME_NORMAL;
		addStartTiles();
		mView.refreshLastTime = true;
		mView.resyncTime();
		mView.invalidate();
	}

	private void addStartTiles() {
		for (int xx = 0; xx < startTiles; xx++) {
			this.addRandomTile();
		}
	}

	private void addRandomTile() {
		if (grid.isCellsAvailable()) {
			int value = Math.random() < 0.9 ? 2 : 4;
			Tile tile = new Tile(grid.randomAvailableCell(), value);
			spawnTile(tile);
		}
	}

	private void spawnTile(Tile tile) {
		grid.insertTile(tile);
		aGrid.startAnimation(tile.getX(), tile.getY(), SPAWN_ANIMATION,
				SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null); //Direction: -1 = EXPANDING
	}

	private void recordHighScore() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(HIGH_SCORE, highScore);
		editor.commit();
	}

	private long getHighScore() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
		return settings.getLong(HIGH_SCORE, -1);
	}

	private void prepareTiles() {
		for (Tile[] array : grid.field) {
			for (Tile tile : array) {
				if (grid.isCellOccupied(tile)) {
					tile.setMergedFrom(null);
				}
			}
		} 
	} 

	private void moveTile(Tile tile, Cell cell) {
		grid.field[tile.getX()][tile.getY()] = null;
		grid.field[cell.getX()][cell.getY()] = tile;
		tile.updatePosition(cell);
	}

	private void saveUndoState() {
		grid.saveTiles();
		canUndo = true;
		lastScore =  bufferScore;
		lastGameState = bufferGameState;
	}

	private void prepareUndoState() {
		grid.prepareSaveTiles();
		bufferScore = score;
		bufferGameState = gameState;
	}

	public void revertUndoState() {
		if (canUndo) {
			canUndo = false;
			aGrid.cancelAnimations();
			grid.revertTiles();
			score = lastScore;
			gameState = lastGameState;
			mView.refreshLastTime = true;
			mView.invalidate();
		}
	}

	public static boolean isMute = false;

	public boolean  isSoundMute() {
		return PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("Mute", false);
	}


	public void revertMuteState() {
		if (PreferenceManager.getDefaultSharedPreferences(mContext).getBoolean("Mute", false) == true ){

			PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("Mute", false).commit();
			isMute = false;
		}else{

			PreferenceManager.getDefaultSharedPreferences(mContext).edit().putBoolean("Mute", true).commit();
			isMute = true;
		}


	}

	public boolean gameWon() {
		return (gameState > 0 && gameState % 2 != 0);
	}

	public boolean gameLost() {
		return (gameState == GAME_LOST);
	}

	public boolean isActive() {
		return !(gameWon() || gameLost());
	}


	///////////////////////////////

	private static final int rId2 = R.raw.cat2;
	private static final int rId4 = R.raw.cat4;
	private static final int rId8 = R.raw.cat8;
	private static final int rId16 = R.raw.cat16;
	private static final int rId32 = R.raw.cat32;
	private static final int rId64 = R.raw.cat64;
	private static final int rId128 = R.raw.cat128;
	private static final int rId256 = R.raw.cat256;
	private static final int rId512 = R.raw.cat512;
	private static final int rId1024 = R.raw.cat1024;
	private static final int rIdClap = R.raw.whistling_and_cheering;
	private static final int rId_lion = R.raw.lion_roar;
	static int sidClap = -1;
	static int sid2=-1;
	static int sid4=-1;
	static int sid8=-1;
	static int sid16=-1;
	static int sid32=-1;
	static int sid64=-1;
	static int sid128=-1;
	static int sid256=-1;
	static int sid512=-1;
	static int sid1024=-1;
	static int sid_lion=-1;
	private static boolean loaded = false;
	static SoundPool soundPool;

	private SoundPool.OnLoadCompleteListener listener =  
			new SoundPool.OnLoadCompleteListener(){
		@Override
		public void onLoadComplete(SoundPool soundPool, int sid, int status){ // could check status value here also
			if (GameLogic.sid2 == sid 
					|| GameLogic.sid4 == sid
					|| GameLogic.sid8 == sid
					|| GameLogic.sid16 == sid
					|| GameLogic.sid32 == sid
					|| GameLogic.sid64 == sid
					|| GameLogic.sid128 == sid
					|| GameLogic.sid256 == sid
					|| GameLogic.sid512 == sid
					|| GameLogic.sid1024 == sid
					|| GameLogic.sidClap == sid
					|| GameLogic.sid_lion == sid) {
				GameLogic.loaded = true;
			}
		}
	};


	public void initSound() {
		soundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 100);
		soundPool.setOnLoadCompleteListener(listener);
		GameLogic.sid2 = soundPool.load(mContext, rId2, 1); 
		GameLogic.sid4 = soundPool.load(mContext, rId4, 1);
		GameLogic.sid8 = soundPool.load(mContext, rId8, 1);
		GameLogic.sid16 = soundPool.load(mContext, rId16, 1);
		GameLogic.sid32 = soundPool.load(mContext, rId32, 1);
		GameLogic.sid64 = soundPool.load(mContext, rId64, 1);
		GameLogic.sid128 = soundPool.load(mContext, rId128, 1);
		GameLogic.sid256 = soundPool.load(mContext, rId256, 1);
		GameLogic.sid512 = soundPool.load(mContext, rId512, 1);
		GameLogic.sid1024 = soundPool.load(mContext, rId1024, 1);
		GameLogic.sidClap = soundPool.load(mContext, rIdClap, 1); 
		GameLogic.sid_lion = soundPool.load(mContext, rId_lion, 1); 
	}

 
	public static void unloadSound() {
		soundPool.unload(sid2); 
		soundPool.unload(sid4); 
		soundPool.unload(sid8); 
		soundPool.unload(sid16); 
		soundPool.unload(sid32); 
		soundPool.unload(sid64); 
		soundPool.unload(sid128); 
		soundPool.unload(sid256); 
		soundPool.unload(sid512); 
		soundPool.unload(sid1024); 
		soundPool.unload(sidClap); 
		soundPool.unload(sid_lion); 
	}
	
	
	public static void SoundPlay(final int soundType) {

		if (isMute == false) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					if (loaded) {

						switch (soundType) {

						case 1:
							soundPool.play(GameLogic.sidClap, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 2:
							soundPool.play(GameLogic.sid2, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 4:
							soundPool.play(GameLogic.sid4, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 8:
							soundPool.play(GameLogic.sid8, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 16:
							soundPool.play(GameLogic.sid16, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 32:
							soundPool.play(GameLogic.sid32, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 64:
							soundPool.play(GameLogic.sid64, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 128:
							soundPool.play(GameLogic.sid128, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 256:
							soundPool.play(GameLogic.sid256, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 512:
							soundPool.play(GameLogic.sid512, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 1024:
							soundPool.play(GameLogic.sid1024, 1.0f, 1.0f, 1, 0, 1f);
							break;

						case 2048:
							soundPool.play(GameLogic.sid_lion, 1.0f, 1.0f, 1, 0, 1f);
							break;

						default:
							soundPool.play(GameLogic.sid2, 1.0f, 1.0f, 1, 0, 1f);
							break;
						}
						//						if (soundType == 2)
						//							soundPool.play(GameLogic.sid2, 1.0f, 1.0f, 1, 0, 1f);
						//						else if (soundType == 2)
						//							soundPool.play(GameLogic.sid_lion, 1.0f, 1.0f, 1, 0, 1f);
						//						else if (soundType == 2)
						//							soundPool.play(GameLogic.sid_lion, 1.0f, 1.0f, 1, 0, 1f);

					}
				} }).start();

		}
	}
	//////////////////////////////


	public void move(int direction) {
		aGrid.cancelAnimations();
		// 0: up, 1: right, 2: down, 3: left
		if (!isActive()) {
			return;
		}
		prepareUndoState();
		Cell vector = getVector(direction);
		List<Integer> traversalsX = buildTraversalsX(vector);
		List<Integer> traversalsY = buildTraversalsY(vector);
		ArrayList<Integer> mergedList = new ArrayList<Integer>();
		boolean moved = false;

		prepareTiles();

		for (int xx: traversalsX) {
			for (int yy: traversalsY) {
				Cell cell = new Cell(xx, yy);
				Tile tile = grid.getCellContent(cell);

				if (tile != null) {
					Cell[] positions = findFarthestPosition(cell, vector);
					Tile next = grid.getCellContent(positions[1]);

					if (next != null && next.getValue() == tile.getValue() && next.getMergedFrom() == null) {
						Tile merged = new Tile(positions[1], tile.getValue() * 2);
						Tile[] temp = {tile, next};
						merged.setMergedFrom(temp);

						grid.insertTile(merged);
						grid.removeTile(tile);

						mergedList.add(tile.getValue());

						//						if (merged.getValue() > 2047  ){
						//							SoundPlay(2);
						//						}
						//						else{
						//							SoundPlay(1);
						//						}
						// Converge the two tiles' positions
						tile.updatePosition(positions[1]);

						int[] extras = {xx, yy};
						aGrid.startAnimation(merged.getX(), merged.getY(), MOVE_ANIMATION,
								MOVE_ANIMATION_TIME, 0, extras); //Direction: 0 = MOVING MERGED
						aGrid.startAnimation(merged.getX(), merged.getY(), MERGE_ANIMATION,
								SPAWN_ANIMATION_TIME, MOVE_ANIMATION_TIME, null);


						//unlock achievements

						if(mAcheivementUnLock != null){

							switch (merged.getValue()) {

							case 512:
								mAcheivementUnLock.case512();
								break;

							case 1024:
								mAcheivementUnLock.case1024();
								break;

							case 2048:
								mAcheivementUnLock.case2048();
								break;

							case 4096:
								mAcheivementUnLock.case4096();
								break;

							case 8192:
								mAcheivementUnLock.case8192();
								break;

							case 16384:
								mAcheivementUnLock.case16384();
								break;

							case 32768:
								mAcheivementUnLock.case32768();
								break;

							case 65536:
								mAcheivementUnLock.case65536();
								break;

							case 131072:
								mAcheivementUnLock.case131072();
								break;

							case 262144:
								mAcheivementUnLock.case262144();
								break;
							default:
								break;
							}

						}

						// Update the score
						score = score + merged.getValue();
						highScore = Math.max(score, highScore);

						// The mighty 2048 tile
						if (merged.getValue() >= winValue() && !gameWon()) {
							System.out.println("ankur // The mighty 2048 tile");
							gameState = gameState + GAME_WIN; // Set win state 
							mGameListener.gameWin(score);
							endGame();
						}
					} else {
						moveTile(tile, positions[0]);
						int[] extras = {xx, yy, 0};
						aGrid.startAnimation(positions[0].getX(), positions[0].getY(), MOVE_ANIMATION, MOVE_ANIMATION_TIME, 0, extras); //Direction: 1 = MOVING NO MERGE
					}

					if (!positionsEqual(cell, tile)) {
						moved = true;
					}
				}
			}
		}

		if (moved) {

			if (!mergedList.isEmpty()){

				int maxValue = Collections.max(mergedList);

				System.out.println("ankur max = " +maxValue);

				switch (maxValue) {

				case 2:
					SoundPlay(2);
					break;

				case 4:
					SoundPlay(4);
					break;

				case 8:
					SoundPlay(8);
					break;

				case 16:
					SoundPlay(16);
					break;

				case 32:
					SoundPlay(32);
					break;

				case 64:
					SoundPlay(64);
					break;

				case 128: 
					SoundPlay(128);
					break;

				case 256:
					SoundPlay(256);
					break;

				case 512:
					SoundPlay(512);
					break;

				case 1024:
					SoundPlay(1024);
					break;

				case 2048:
					SoundPlay(2);
					break;

				default:
					soundPool.play(GameLogic.sid2, 1.0f, 1.0f, 1, 0, 1f);
					break;

				}
			}

			saveUndoState();
			addRandomTile();
			checkLose();
		}
		mView.resyncTime();
		mView.invalidate();
	}

	private void checkLose() {
		System.out.println("ankur enter checkLose");
		if (!movesAvailable() && !gameWon()) {
			System.out.println("ankur enter checkLose if (!movesAvailable() && !gameWon()) ");
			gameState = GAME_LOST;
			mGameListener.gameLose(score);
			endGame();
		}
		System.out.println("ankur exit checkLose");
	}

	private InterstitialAd interstitialAds;
	
	private void initAdMobXML() {
		interstitialAds = new InterstitialAd(mContext);
		interstitialAds.setAdUnitId("ca-app-pub-6816050713890894/3198302367");

		// Create an ad request.
		AdRequest.Builder adRequestBuilder = new AdRequest.Builder();
		interstitialAds.loadAd(adRequestBuilder.build());

	}
	
	private Handler myHandler = new Handler(){
		public void handleMessage(Message msg) {
			
			if(interstitialAds.isLoaded())
				interstitialAds.show();   
			
		};
	};
	
	private void endGame() {
		initAdMobXML();
		myHandler.sendEmptyMessageDelayed(1,3000);
		
		System.out.println("ankur enter endgame");
		aGrid.startAnimation(-1, -1, FADE_GLOBAL_ANIMATION, NOTIFICATION_ANIMATION_TIME, NOTIFICATION_DELAY_TIME, null);
		if (score >= highScore) {
			highScore = score;
			recordHighScore(); 
		}

	}

	private Cell getVector(int direction) {
		Cell[] map = {
				new Cell(0, -1), // up
				new Cell(1, 0),  // right
				new Cell(0, 1),  // down
				new Cell(-1, 0)  // left
		};
		return map[direction];
	}

	private List<Integer> buildTraversalsX(Cell vector) {
		List<Integer> traversals = new ArrayList<Integer>();

		for (int xx = 0; xx < numSquaresX; xx++) {
			traversals.add(xx);
		}
		if (vector.getX() == 1) {
			Collections.reverse(traversals);
		}

		return traversals;
	}

	private List<Integer> buildTraversalsY(Cell vector) {
		List<Integer> traversals = new ArrayList<Integer>();

		for (int xx = 0; xx <numSquaresY; xx++) {
			traversals.add(xx);
		}
		if (vector.getY() == 1) {
			Collections.reverse(traversals);
		}

		return traversals;
	}

	private Cell[] findFarthestPosition(Cell cell, Cell vector) {
		Cell previous;
		Cell nextCell = new Cell(cell.getX(), cell.getY());
		do {
			previous = nextCell;
			nextCell = new Cell(previous.getX() + vector.getX(),
					previous.getY() + vector.getY());
		} while (grid.isCellWithinBounds(nextCell) && grid.isCellAvailable(nextCell));

		Cell[] answer = {previous, nextCell};
		return answer;
	}

	private boolean movesAvailable() {
		return grid.isCellsAvailable() || tileMatchesAvailable();
	}

	private boolean tileMatchesAvailable() {
		Tile tile;

		for (int xx = 0; xx < numSquaresX; xx++) {
			for (int yy = 0; yy < numSquaresY; yy++) {
				tile = grid.getCellContent(new Cell(xx, yy));

				if (tile != null) {
					for (int direction = 0; direction < 4; direction++) {
						Cell vector = getVector(direction);
						Cell cell = new Cell(xx + vector.getX(), yy + vector.getY());

						Tile other = grid.getCellContent(cell);

						if (other != null && other.getValue() == tile.getValue()) {
							return true;
						}
					}
				}
			}
		}

		return false;
	}

	private boolean positionsEqual(Cell first, Cell second) {
		return first.getX() == second.getX() && first.getY() == second.getY();
	}

	private int winValue() {
		if (!canContinue()) {
			return endingMaxValue;
		} else {
			return startingMaxValue;
		}
	}

	public void setEndlessMode() {
		gameState = GAME_ENDLESS;
		mView.invalidate();
		mView.refreshLastTime = true;
	}

	public boolean canContinue() {
		return !(gameState == GAME_ENDLESS || gameState == GAME_ENDLESS_WON);
	}
}
