package com.nguyenthanhchung.carop2p.activity;

import android.app.FragmentTransaction;
import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import com.nguyenthanhchung.carop2p.fragment.BoardEmotionFragment;
import com.nguyenthanhchung.carop2p.MainGameActivityCallBacks;
import com.nguyenthanhchung.carop2p.R;
import com.nguyenthanhchung.carop2p.fragment.BoardGameFragment;

public class MainGameActivity extends AppCompatActivity implements MainGameActivityCallBacks {

    FragmentTransaction fragmentTransaction;
    BoardGameFragment boardGameFragment;
    BoardEmotionFragment  emotionBoardFragmet;
    ImageButton btnOpenEmotionBoard;
    MediaPlayer background_song;
    boolean isOpenedEmotionBoard = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_game);

//        background_song = MediaPlayer.create(MainGameActivity.this,R.raw.playgame_sound);
//        background_song.setLooping(true);
//        turnOnBackGroundSong();
        addControls();
        addEvents();
    }
    @Override
    protected void onPause() {
        background_song.stop();
        background_song.release();
        super.onPause();
    }

    @Override
    protected  void onResume(){
        background_song = MediaPlayer.create(MainGameActivity.this, R.raw.playgame_sound);
        background_song.setLooping(true);
        background_song.start();
        super.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if(background_song.isPlaying()==false){
            background_song.start();
        }
    }
    private void addEvents() {
        btnOpenEmotionBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOpenedEmotionBoard == false){
                    showEmotionBoard();
                    isOpenedEmotionBoard = true;
                }
            }
        });
    }

    private void addControls() {

        btnOpenEmotionBoard = findViewById(R.id.btnOpenEmotionBoard);

        // add fragment boardgame
        fragmentTransaction = getFragmentManager().beginTransaction();
        boardGameFragment = BoardGameFragment.newInstance("boardGame");
        fragmentTransaction.replace(R.id.fragmentBoardGame, boardGameFragment);
        fragmentTransaction.commit();

        // add fragment emotion board
        fragmentTransaction = getFragmentManager().beginTransaction();
        emotionBoardFragmet = BoardEmotionFragment.newInstance("EmotionBoard");
        fragmentTransaction.replace(R.id.fragmentEmotionBoard, emotionBoardFragmet);
        fragmentTransaction.hide(emotionBoardFragmet);
        fragmentTransaction.commit();
    }




    @Override
    public void onMsgFromFragmentToMainGame(String sender, String strValue) {
        if(sender == null || strValue == null) return;
        if(sender.equals("EmotionBoard")){
            if(strValue.equals("close")){
                if(isOpenedEmotionBoard){
                    hideEmotionBoard();
                    isOpenedEmotionBoard = false;
                }
            }
        }else if(sender.equals("GameBoard")){

        }
    }

    private void showEmotionBoard(){
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.show(emotionBoardFragmet);
        fragmentTransaction.commit();
    }

    private void hideEmotionBoard(){
        fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.hide(emotionBoardFragmet);
        fragmentTransaction.commit();
    }
//    public void turnOnBackGroundSong(){
//        background_song.start();
//    }

    public void turnOffBackGroundSong(View v){
        if(background_song.isPlaying()){
            background_song.stop();
        }
    }
}
