package edu.msu.kinggra1.teamswift_project2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class FinalScoreActivity extends ActionBarActivity {
    Game game;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_final_score);

        if(bundle != null) {
            game = (Game)bundle.getSerializable(getString(R.string.game_state));
        }
        else {
            game = (Game)getIntent().getExtras().getSerializable(getString(R.string.game_state));
        }

        ((TextView)findViewById(R.id.winningPlayerText)).setText(String.format(getString(R.string.player_wins), game.getWinningPlayerName()));
        ((TextView)findViewById(R.id.birdText)).setText(String.format(getString(R.string.birds_placed), game.getNumBirdsPlaced()));
    }

    public void onNewGame(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        game.saveInstanceState(bundle, this);
    }
}
