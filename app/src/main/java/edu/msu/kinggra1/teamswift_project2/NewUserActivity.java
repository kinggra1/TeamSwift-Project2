package edu.msu.kinggra1.teamswift_project2;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;


public class NewUserActivity extends ActionBarActivity {

    Cloud cloud = new Cloud();
    EditText username;
    EditText passwordOne;
    EditText passwordTwo;

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

    public void newUser(View view) {
        username = (EditText) findViewById(R.id.usernameText);
        passwordOne = (EditText) findViewById(R.id.passwordOneText);
        passwordTwo = (EditText) findViewById(R.id.passwordTwoText);

        Thread thread = new Thread( new Runnable() {
            @Override
            public void run() {
                String result = cloud.Register(username.getText().toString(), passwordOne.getText().toString(), passwordTwo.getText().toString());
                Log.d("RESULT", result);
            }
        });

        thread.start();


    }
}
