package edu.msu.kinggra1.teamswift_project2;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;


public class GameActivity extends ActionBarActivity {


    private GameView gameView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        setContentView(R.layout.activity_game);

        gameView = (GameView)findViewById(R.id.gameView);

        if(bundle != null) {
            gameView.loadInstanceState(bundle, this);
        }
        else {
            Game game = (Game)getIntent().getExtras().getSerializable(getString(R.string.game_state));
            gameView.setGame(game);
        }

        TextView tv = (TextView)findViewById(R.id.placementText);
        tv.setText(String.format(getString(R.string.bird_placement_info),
                gameView.getGame().getCurrentPlayerName()));

        gameView.reloadBirds();
        gameView.startPullThread();
    }

    public void onPlaceBird(View view) {
        gameView.onPlaceBird();
        gameView.startPushThread();

        Bundle bundle = new Bundle();
        gameView.getGame().saveInstanceState(bundle, this);

        if (gameView.inGameOverState()) {

            Intent intent = new Intent(this, FinalScoreActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }

        else if (gameView.inSelectionState()) {

            Intent intent = new Intent(this, SelectionActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();
        }

        TextView tv = (TextView)findViewById(R.id.placementText);
        tv.setText(String.format(getString(R.string.bird_placement_info),
                gameView.getGame().getCurrentPlayerName()));


    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        gameView.saveInstanceState(bundle, this);
    }
}
