package edu.msu.kinggra1.teamswift_project2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;


public class SelectionActivity extends ActionBarActivity {

    private Game game;

    private SelectionView selectionView;

    private TextView selectionText;

    private Toast noBirdToast;

    @Override
    protected void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);

        selectionView.saveInstanceState(bundle);
        game.saveInstanceState(bundle, this);
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_selection);

        if(bundle != null) {
            game = (Game)bundle.getSerializable(getString(R.string.game_state));
        }
        else {
            game = (Game)getIntent().getExtras().getSerializable(getString(R.string.game_state));
        }
        selectionView = (SelectionView)findViewById(R.id.selectionView);

        this.selectionText = (TextView) findViewById(R.id.playerNameLabel);
        setPlayerSelectionText();

        Context context = getApplicationContext();
        CharSequence noBirdText = "Please select a bird!";
        int duration = Toast.LENGTH_SHORT;

        noBirdToast = Toast.makeText(context, noBirdText, duration);
        TextView v = (TextView) noBirdToast.getView().findViewById(android.R.id.message);
        v.setTextColor(Color.RED);


        if (bundle != null){
            Log.i("onCreate()", "restoring state...");
            selectionView.loadInstanceState(bundle);
        }
    }

    /**
     * set the text at the top of the selection screen to the appropriate player
     */
    private void setPlayerSelectionText() {
        selectionText.setText(getString(R.string.player_select));
    }

    public void onConfirmSelection(View view) {
        Bundle bundle = new Bundle();
        game.saveInstanceState(bundle, this);

        if (selectionView.isSelected()) {
            selectionView.setPlayerSelection(game);

            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtras(bundle);
            startActivity(intent);
            finish();

        } else {
            noBirdToast.show();
            Log.i("onConfirmSelection", "bird not selected");
        }
    }

}
