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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.InputStream;


public class LoginActivity extends ActionBarActivity {

    Cloud cloud = new Cloud();
    EditText userText;
    EditText userPasssword;
    SharedPreferences sharedPref;
    CheckBox check;

    private static final String UTF8 = "UTF-8";
    private static final String COMM_EXCEPTION = "An exception occurred while communicating with the server";
    private static final String PARSING_EXCEPTION = "An exception occurred while parsing the server's return";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        View view = findViewById(R.id.loginButton);
        check = (CheckBox)findViewById(R.id.stayLogged);
        userText = (EditText) findViewById(R.id.userText);
        userPasssword = (EditText) findViewById(R.id.userPassword);

        sharedPref = getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        if(!sharedPref.getString("USERNAME", "null").equals("null")) {
            Log.d("USERNAME",sharedPref.getString("USERNAME","null"));
            Log.d("PASSWORD",sharedPref.getString("PASSWORD","null"));

            userText.setText(sharedPref.getString("USERNAME","null"));
            userPasssword.setText(sharedPref.getString("PASSWORD","null"));
        }
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
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                cloud.LogOut(sharedPref.getString("USERNAME","null"), sharedPref.getString("PASSWORD","null"));
            }
        });
        thread.start();

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("PASSWORD", "null");
        editor.putString("USERNAME", "null");
        editor.commit();

        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    public void newUser(View view) {
        Intent intent = new Intent(this, NewUserActivity.class);
        startActivity(intent);
    }

    public void userLogin(final View view) {
        userText = (EditText) findViewById(R.id.userText);
        userPasssword = (EditText) findViewById(R.id.userPassword);

        if(sharedPref.getString("USERNAME","null").equals("null") && sharedPref.getString("PASSWORD","null").equals("null") ) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    cloud.LogOut(sharedPref.getString("USERNAME","null"), sharedPref.getString("PASSWORD","null"));
                }
            });
            thread.start();
        }

        new Thread( new Runnable() {
            @Override
            public void run() {

                InputStream stream = cloud.LogIn(userText.getText().toString(), userPasssword.getText().toString());


                if (stream != null) {
                    try {
                        //Create an XML parser for the stream
                        XmlPullParser xmlParser = Xml.newPullParser();
                        xmlParser.setInput(stream, UTF8);

                        xmlParser.nextTag();      // Advance to first tag
                        xmlParser.require(XmlPullParser.START_TAG, null, "flock");

                        String xmlStatus = xmlParser.getAttributeValue(null, "status");
                        String xmlMsg = xmlParser.getAttributeValue(null, "msg");

                        if (xmlStatus.equals("no")) {
                            ToastMessage(xmlMsg);
                        }
                        else if (xmlStatus.equals("yes")) {
                            // Login successful

                            if(check.isChecked()) {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("PASSWORD", userPasssword.getText().toString());
                                editor.putString("USERNAME", userText.getText().toString());
                                editor.commit();
                            }
                            else {
                                SharedPreferences.Editor editor = sharedPref.edit();
                                editor.putString("PASSWORD", "null");
                                editor.putString("USERNAME", "null");
                                editor.commit();
                            }

                            Game game = new Game(getApplicationContext());

                            // Increment the round number to 1 (starts at 0)
                            game.incrementRoundNum();

                            // XML message is empty
                            if (xmlMsg.equals("")) {
                                // You are the first player in the game, wait to find a game
                                game.getPlayer().setPlayerNumber(1);

                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), WaitingRoomActivity.class);
                                intent.putExtra(getString(R.string.game_state), game);
                                startActivity(intent);
                            }
                            else {
                                // Waiting for other player to make a move
                                game.getPlayer().setPlayerNumber(2);

                                Intent intent = new Intent();
                                intent.setClass(getApplicationContext(), RoundWaitActivity.class);
                                intent.putExtra(getString(R.string.game_state), game);
                                startActivity(intent);
                            }
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
}
