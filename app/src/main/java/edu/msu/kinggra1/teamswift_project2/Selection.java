package edu.msu.kinggra1.teamswift_project2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

/**
 * Created by Ethan on 2/12/15.
 */
public class Selection {
    /**
     * Percentage of the view width/height that is occupied by the game
     */
    private final static float SCALE_IN_VIEW = 0.9f;

    private final static float BORDER_WIDTH = 3.0f;

    /**
     * The size of the game field
     */
    private int gameSize;


    /**
     * The width screen margin for the game
     */
    private int marginX;

    /**
     * The height screen buffer for the game
     */
    private int marginY;

    /**
     * The 1:1 scaling width of the game
     */
    private float scalingWidth;

    /**
     * the scaling factor for drawing birds
     */
    private float scaleFactor;

    /**
     * Paint for outlining the area the puzzle is in
     */
    private Paint outlinePaint;

    private Paint selectionPaint;

    public Bird getTouchedBird() {
        return touchedBird;
    }

    /**
     * Currently touched bird
     */
    private Bird touchedBird = null;

    /**
     * Collection of the birds that have been placed
     */
    private ArrayList<Bird> birds = new ArrayList<>();


    /**
     * @param context the current context
     */
    public Selection(Context context) {

        // Create the paint for outlining the play area
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(3.0f);
        outlinePaint.setColor(Color.GREEN);

        // Create the paint to outline a bird when selected

        selectionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        selectionPaint.setStyle(Paint.Style.STROKE);
        selectionPaint.setStrokeWidth(5.0f);
        selectionPaint.setColor(Color.RED);

        // Birds will be scaled so that the game is "1.5 ostriches" wide
        Bitmap scaleBird = BitmapFactory.decodeResource(context.getResources(), R.drawable.ostrich);
        scalingWidth = scaleBird.getWidth()*1.5f;


        // load the bird images
        birds.add(new Bird(context, R.drawable.ostrich, 0.0650f, 0.150f));
        birds.add(new Bird(context, R.drawable.swallow, 0.866f, 0.158f));
        birds.add(new Bird(context, R.drawable.robin, 0.841f, 0.451f));
        birds.add(new Bird(context, R.drawable.hummingbird, 0.158f, 0.119f));
        birds.add(new Bird(context, R.drawable.seagull, 0.800f, 0.901f));

    }

    public void draw(Canvas canvas) {

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // The puzzle size is the view scale ratio of the minimum dimension, to make it square
        int minSide = (int)((width < height ? width : height) * SCALE_IN_VIEW);

        scaleFactor = minSide/scalingWidth;

        // Margins for centering the puzzle
        marginX = (int)((width - minSide) / (scaleFactor*2));
        marginY = (int)((height - minSide) / (scaleFactor*2));


        gameSize = (int)scalingWidth;


        canvas.save();

        canvas.scale(scaleFactor, scaleFactor);

        // Draw the outline of the gameplay area
        canvas.drawRect(marginX - BORDER_WIDTH, marginY - BORDER_WIDTH,
                marginX + gameSize + BORDER_WIDTH, marginY + gameSize + BORDER_WIDTH, outlinePaint);

        for (Bird bird : birds) {
            bird.draw(canvas, marginX, marginY, gameSize);
        }

        if(touchedBird != null) {
            Rect birdRect = touchedBird.getRect();
            canvas.drawRect(birdRect.left + marginX, birdRect.top + marginY, birdRect.right + marginX, birdRect.bottom + marginY, selectionPaint);
        }

        canvas.restore();
    }

    /**
     * Handle a touch event from the view.
     * @param view The view that is the source of the touch
     * @param event The motion event describing the touch
     * @return true if the touch is handled.
     */
    public boolean onTouchEvent(View view, MotionEvent event) {
        float relX = (event.getX()/scaleFactor - marginX);
        float relY = (event.getY()/scaleFactor - marginY);

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                onTouched(relX, relY);
                view.invalidate();
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;

            case MotionEvent.ACTION_MOVE:
                break;
        }

        return false;
    }

    private boolean onTouched(float x, float y) {
        Log.i("onTouched", "checking..." + x + " " + y);

        // Check each piece to see if it has been hit
        // We do this in reverse order so we find the pieces in front
        for(int b=birds.size()-1; b>=0;  b--) {
            if (birds.get(b).hit(x, y, gameSize, scaleFactor)) {
                // We hit a piece!
                Log.i("onTouched", "PIECE HIT!!" + birds.get(b));
                touchedBird = birds.get(b);

                return true;
            }
        }
        return false;
    }

    /**
     * save the selection in to a bundle
     * @param bundle the bundle we save to
     */
    public void saveInstanceState(Bundle bundle) {
        if (touchedBird != null) {
            bundle.putInt("touchedBirdIndex", birds.indexOf(touchedBird));
        }
        else {
            bundle.putInt("touchedBirdIndex", -1);
        }

    }

    /**
     * Read the selection from a bundle
     * @param bundle The bundle we save to
     */
    public void loadInstanceState(Bundle bundle) {
        int touchedBirdIndex = bundle.getInt("touchedBirdIndex");

        if(touchedBirdIndex != -1) {
            touchedBird = birds.get(touchedBirdIndex);
        }
    }
}
