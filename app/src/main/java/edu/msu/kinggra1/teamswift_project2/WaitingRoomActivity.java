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


public class WaitingRoomActivity extends ActionBarActivity {

    private static final String UTF8 = "UTF-8";
    private static final String COMM_EXCEPTION = "An exception occurred while communicating with the server";
    private static final String PARSING_EXCEPTION = "An exception occurred while parsing the server's return";

    private long sleepTime = 2000;

    private boolean waitThreadRunnable = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_waiting_room);
        startWaitThread(findViewById(R.id.waitingText));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_waiting_room, menu);
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

        // Stop the current wait thread
        waitThreadRunnable = false;
    }



    private void startWaitThread(final View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Cloud cloud = new Cloud();

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

                                // Start the new activity
                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), WaitingRoomActivity.class);
                                startActivity(intent);
                            }
                            else if (xmlStatus.equals("no")) {
                                // Check to see if there is a message included with the no
                                String xmlMsg = xmlParser.getAttributeValue(null, "msg");

                                if (xmlMsg != null)
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
                        ToastMessage(COMM_EXCEPTION);
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
