package edu.msu.kinggra1.teamswift_project2;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Ethan on 2/12/15.
 */
public class SelectionView extends View {


    /**
     * The actual selection view
     */
    private Selection selection;

    public SelectionView(Context context) {
        super(context);
        init(null, 0);
    }

    public SelectionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public SelectionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        selection = new Selection(getContext());
    }

    public void setPlayerSelection(Game game){
        game.setPlayerSelection(selection.getTouchedBird());
    }


    /*
    ** check to see if player selected a bird yet
     */
    public boolean isSelected(){
        return selection.getTouchedBird() != null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        selection.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return selection.onTouchEvent(this, event);
    }

    /**
     * Save the selection to a bundle
     * @param bundle The bundle we save to
     */
    public void saveInstanceState(Bundle bundle) {
        selection.saveInstanceState(bundle);
    }

    /**
     * Load the selection from a bundle
     * @param bundle The bundle we save to
     */
    public void loadInstanceState(Bundle bundle) {
        selection.loadInstanceState(bundle);
    }
}

