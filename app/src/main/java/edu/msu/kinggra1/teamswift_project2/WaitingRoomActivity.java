package edu.msu.kinggra1.teamswift_project2;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

/**
 * Activity for waiting for another player to log in
 */

public class WaitingRoomActivity extends ActionBarActivity {

    Cloud cloud = new Cloud();
    SharedPreferences sharedPref;

    private static final String UTF8 = "UTF-8";
    private static final String COMM_EXCEPTION = "An exception occurred while communicating with the server";
    private static final String PARSING_EXCEPTION = "An exception occurred while parsing the server's return";
    private static final String THREAD_EXCEPTION = "An exception occurred during thread sleep";

    /**
     * Amount of milliseconds for the wait thread to wait
     */
    private long sleepTime = 2000;

    /**
     * Whether the wait thread is currently runnable
     * Set to false in order to kill the thread
     */
    private boolean waitThreadRunnable = true;

    /**
     * Game instance for this player
     */
    private Game game;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_waiting_room);

        if (bundle != null) {
            // Device was rotated
            game = (Game)bundle.getSerializable(getString(R.string.game_state));
        }
        else {
            // We are starting from a previous activity
            game = (Game)getIntent().getExtras().getSerializable(getString(R.string.game_state));
        }

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
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
        startActivity(intent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putSerializable(getString(R.string.game_state), game);

        // Stop the current wait thread
        waitThreadRunnable = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Kill the thread
        waitThreadRunnable = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        startWaitThread(findViewById(R.id.waitingRoomText));
    }

    private void startWaitThread(final View view) {

        waitThreadRunnable = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                cloud = new Cloud();

                InputStream stream;

                while (waitThreadRunnable) {
                    stream = cloud.Wait();

                    if (stream != null) {
                        try {
                            //Create an XML parser for the stream
                            XmlPullParser xmlParser = Xml.newPullParser();
                            xmlParser.setInput(stream, UTF8);

                            xmlParser.nextTag();      // Advance to first tag
                            xmlParser.require(XmlPullParser.START_TAG, null, "flock");

                            String xmlStatus = xmlParser.getAttributeValue(null, "status");

                            // Check if this thread should be running
                            if (!waitThreadRunnable)
                                return;

                            if (xmlStatus.equals("yes")) {
                                waitThreadRunnable = false;

                                // Start the selection activity
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), SelectionActivity.class);
                                intent.putExtra(getString(R.string.game_state), game);
                                startActivity(intent);
                                finish();
                            }
                            else if (xmlStatus.equals("no")) {
                                // Check to see if there is a message included with the no
                                String xmlMsg = xmlParser.getAttributeValue(null, "msg");

                                if (!xmlMsg.equals(""))
                                    ToastMessage(xmlMsg);
                            }
                        } catch (Exception ex) {
                            ToastMessage(PARSING_EXCEPTION);
                        }
                    } else {
                        ToastMessage(COMM_EXCEPTION);
                    }

                    if (!waitThreadRunnable)
                        return;

                    try {
                        Thread.sleep(sleepTime);
                    } catch (Exception ex) {
                        ToastMessage(THREAD_EXCEPTION);
                    }
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
