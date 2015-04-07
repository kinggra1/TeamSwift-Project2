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

    public void newUser(final View view) {
        username = (EditText) findViewById(R.id.usernameText);
        passwordOne = (EditText) findViewById(R.id.passwordOneText);
        passwordTwo = (EditText) findViewById(R.id.passwordTwoText);

        new Thread( new Runnable() {
            @Override
            public void run()
            {
                final String result = cloud.Register(username.getText().toString(), passwordOne.getText().toString(), passwordTwo.getText().toString());

                if(result != null)
                {
                    // We have failed to register

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
                    intent.setClass(getApplicationContext(), LoginActivity.class);
                    startActivity(intent);
                }


            }
        }).start();
    }

    public void backLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
