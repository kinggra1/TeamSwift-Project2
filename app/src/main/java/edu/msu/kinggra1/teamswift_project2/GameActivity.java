package edu.msu.kinggra1.teamswift_project2;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import java.io.InputStream;

public class GameActivity extends ActionBarActivity {

    private static final String UTF8 = "UTF-8";
    private static final String COMM_EXCEPTION = "An exception occurred while communicating with the server";
    private static final String PARSING_EXCEPTION = "An exception occurred while parsing the server's return";

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
        tv.setText(getString(R.string.bird_placement_info));

        gameView.reloadBirds();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public void onPlaceBird(View view) {
        gameView.onPlaceBird();

        //TextView tv = (TextView)findViewById(R.id.placementText);
        //tv.setText(getString(R.string.bird_placement_info));

        // If the bird placement caused the game to end
        if (gameView.getGame().inGameOverState()) {
            // This player is the loser
            gameView.getGame().setWinner(false);

            startPushThread(view);

            // Push thread will finish the game after pushing
        }
        else {
            boolean evenRound = gameView.getGame().isEvenRound();
            boolean evenPlayer = gameView.getGame().isEvenPlayer();

            // If they are the same
            if (evenRound == evenPlayer) {
                // Swap players, same round
                startPushThread(view);
            }
            else {
                // Don't swap players, new round
                gameView.getGame().incrementRoundNum();

                // Start the selection activity for the next round
                Intent intent = new Intent();
                intent.setClass(getApplicationContext(), SelectionActivity.class);
                intent.putExtra(getString(R.string.game_state), gameView.getGame());
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        gameView.saveInstanceState(bundle, this);
    }

    public void startPushThread(final View view) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // Get a new cloud instance
                Cloud cloud = new Cloud();

                InputStream stream = cloud.Push(gameView.getGame());

                if (stream != null) {
                    try {
                        //Create an XML parser for the stream
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(stream, UTF8);

                        xmlParser.nextTag();      // Advance to first tag
                        xmlParser.require(XmlPullParser.START_TAG, null, "flock");

                        String xmlStatus = xmlParser.getAttributeValue(null, "status");

                        if (xmlStatus.equals("no")) {
                            String xmlMsg = xmlParser.getAttributeValue(null, "msg");

                            ToastMessage(xmlMsg);
                        }
                        else if (xmlStatus.equals("yes")) {
                            // Push successful

                            if (gameView.inGameOverState()) {
                                // Start the final score activity with this
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), FinalScoreActivity.class);
                                intent.putExtra(getString(R.string.game_state), gameView.getGame());
                                startActivity(intent);
                                finish();
                            }
                            else {
                                // Start waiting for the next round
                                Intent intent = new Intent();
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.setClass(getApplicationContext(), RoundWaitActivity.class);
                                intent.putExtra(getString(R.string.game_state), gameView.getGame());
                                startActivity(intent);
                                finish();
                            }
                        }
                    } catch (Exception ex) {
                        ToastMessage(PARSING_EXCEPTION);
                    }
                } else {
                    ToastMessage(COMM_EXCEPTION);
                }
            }

            private void ToastMessage(final String message) {
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
}
