package edu.msu.kinggra1.teamswift_project2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;

public class RoundWaitActivity extends ActionBarActivity {

    private static final String UTF8 = "UTF-8";
    private static final String COMM_EXCEPTION = "An exception occurred while communicating with the server";
    private static final String PARSING_EXCEPTION = "An exception occurred while parsing the server's return";
    private static final String THREAD_EXCEPTION = "An exception occurred during thread sleep";

    /**
     * Amount of milliseconds for the pull thread to wait
     */
    private long sleepTime = 2000;

    /**
     * Whether the pull thread is currently runnable
     * Set to false in order to kill the thread
     */
    private boolean pullThreadRunnable = true;

    /**
     * Game instance for this player
     */
    private Game game;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_round_wait);

        if (bundle != null) {
            // Device was rotated
            game = (Game)bundle.getSerializable(getString(R.string.game_state));
        }
        else {
            // We are starting from a previous activity
            game = (Game)getIntent().getExtras().getSerializable(getString(R.string.game_state));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_round_wait, menu);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        bundle.putSerializable(getString(R.string.game_state), game);

        // Kill the pull thread
        pullThreadRunnable = false;
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Kill the pull thread
        pullThreadRunnable = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        startPullThread(findViewById(R.id.roundWaitText));
    }

    public void startPullThread(final View view) {

        pullThreadRunnable = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Get a new cloud instance
                Cloud cloud = new Cloud();

                InputStream stream;

                while (pullThreadRunnable) {
                    // Pull data from the cloud, get the input stream
                    stream = cloud.Pull(game.getCloudID());

                    if (stream != null) {
                        try {
                            //Create an XML parser for the stream
                            XmlPullParser xmlParser = Xml.newPullParser();
                            xmlParser.setInput(stream, UTF8);

                            xmlParser.nextTag();      // Advance to first tag
                            xmlParser.require(XmlPullParser.START_TAG, null, "flock");

                            String xmlStatus = xmlParser.getAttributeValue(null, "status");
                            String xmlMsg = xmlParser.getAttributeValue(null, "msg");

                            // Check if this thread should be running
                            if (!pullThreadRunnable)
                                return;

                            if (xmlStatus.equals("yes") && xmlMsg != null) {
                                String newCloudID = xmlParser.getAttributeValue(null, "id");

                                // Update cloud ID
                                game.setCloudID(Integer.parseInt(newCloudID));

                                // Load the XML into the bird array
                                game.LoadXML(xmlMsg, view);

                                // Start the Selection Activity
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), SelectionActivity.class);
                                intent.putExtra(getString(R.string.game_state), game);
                                startActivity(intent);

                            } else if (xmlStatus.equals("no") && xmlMsg != null) {
                                ToastMessage(xmlMsg);
                            }
                        } catch (Exception ex) {
                            ToastMessage(PARSING_EXCEPTION);
                        }
                    } else {
                        ToastMessage(COMM_EXCEPTION);
                    }

                    // Check if this thread should be running
                    if (!pullThreadRunnable)
                        return;

                    // Sleep each while loop
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
