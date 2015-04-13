package edu.msu.kinggra1.teamswift_project2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Xml;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;


public class NewUserActivity extends ActionBarActivity {

    Cloud cloud = new Cloud();
    EditText username;
    EditText passwordOne;
    EditText passwordTwo;

    private static final String UTF8 = "UTF-8";
    private static final String COMM_EXCEPTION = "An exception occurred while communicating with the server";
    private static final String PARSING_EXCEPTION = "An exception occurred while parsing the server's return";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new_user, menu);
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

    public void newUser(final View view) {
        username = (EditText) findViewById(R.id.usernameText);
        passwordOne = (EditText) findViewById(R.id.passwordOneText);
        passwordTwo = (EditText) findViewById(R.id.passwordTwoText);

        new Thread( new Runnable() {
            @Override
            public void run()
            {
                InputStream stream = cloud.Register(username.getText().toString(), passwordOne.getText().toString(), passwordTwo.getText().toString());

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
                            // Start the new activity
                            Intent intent = new Intent();
                            intent.setClass(getApplicationContext(), LoginActivity.class);
                            startActivity(intent);
                        }
                    }
                    catch (Exception ex) {
                        ToastMessage(PARSING_EXCEPTION);
                    }
                }
                else {
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

    public void backLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
