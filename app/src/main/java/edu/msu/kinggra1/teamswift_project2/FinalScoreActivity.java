package edu.msu.kinggra1.teamswift_project2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;


public class FinalScoreActivity extends ActionBarActivity {
    Game game;
    Cloud cloud = new Cloud();
    SharedPreferences sharedPref;

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

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        // Check here for winner
        String winningPlayerText;

        if (game.isWinner()) {
            winningPlayerText = getString(R.string.you_won);
        }
        else {
            winningPlayerText = getString(R.string.you_lost);
        }

        ((TextView)findViewById(R.id.winningPlayerText)).setText(winningPlayerText);
        ((TextView)findViewById(R.id.birdText)).setText(String.format(getString(R.string.birds_placed), game.getNumBirdsPlaced()));

    }

    public void onNewGame(View view) {
        logOutAll();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        game.saveInstanceState(bundle, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d("CLICKED", "CLICKED");
            logOut();
        }

        return super.onOptionsItemSelected(item);
    }

    public void logOut() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                cloud.LogOut(sharedPref.getString("USERNAME","null"), sharedPref.getString("PASSWORD","null"));
            }
        }).start();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("PASSWORD", "null");
        editor.putString("USERNAME", "null");
        editor.commit();

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    public void logOutAll(){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                cloud.LogOutAll();
            }
        });
        thread.start();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("PASSWORD", "null");
        editor.putString("USERNAME", "null");
        editor.commit();
    }
}
