package edu.msu.kinggra1.teamswift_project2;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;


public class LoginActivity extends ActionBarActivity {

    Cloud cloud = new Cloud();
    EditText userText;
    EditText userPasssword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    public void newUser(View view) {
        Intent intent = new Intent(this, NewUserActivity.class);
        startActivity(intent);
    }

    public void userLogin(final View view) {
        userText = (EditText) findViewById(R.id.userText);
        userPasssword = (EditText) findViewById(R.id.userPassword);

        new Thread( new Runnable() {
            @Override
            public void run() {

                final String result = cloud.LogIn(userText.getText().toString(), userPasssword.getText().toString());

                if(result != null)
                {
                    // We have failed to log in

                    Log.d("RESULT", result);

                    view.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Toast.makeText(view.getContext(), result, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Intent intent = new Intent();
                    intent.setClass(getApplicationContext(), WaitingRoomActivity.class);
                    startActivity(intent);
                    // Otherwise, we have succeeded
                }


            }
        }).start();
    }
}
