package edu.msu.kinggra1.teamswift_project2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.View;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String UTF8 = "UTF-8";

    /**
     * Percentage of the view width/height that is occupied by the game
     */
    private final static float SCALE_IN_VIEW = 0.9f;

    /**
     * Width of the border around the game
     */
    private final static float BORDER_WIDTH = 3.0f;

    /**
     * Paint for outlining the area the game is in
     */
    private static Paint outlinePaint;

    /**
     * The size of the game field
     */
    private transient int gameSize;

    /**
     * The 1:1 scaling width of the game
     */
    private transient float scalingWidth;

    /**
     * the scaling factor for drawing birds
     */
    private transient float scaleFactor;

    /**
     * Collection of the birds that have been placed
     */
    private ArrayList<Bird> birds = new ArrayList<>();

    /**
     * The current player in the game
     */
    private Player player = new Player();

    /**
     * The current round number
     */
    private int roundNum = 0;

    /**
     * True if the game is finished
     */
    private boolean gameOver = false;

    /**
     * Unique cloud identifier number to test for updates
     */
    private int cloudID = 0;

    /**
     * Is there a bird currently being dragged
     */
    private Bird dragging = null;

    /**
     * Most recent relative X touch when dragging
     */
    private float lastRelX;

    /**
     * Most recent relative Y touch when dragging
     */
    private float lastRelY;

    /**
     * @param context the current context
     */
    public Game(Context context) {
        // Create the paint for outlining the play area
        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(BORDER_WIDTH);
        outlinePaint.setColor(Color.RED);

        // Birds will be scaled so that the game is "1.5 ostriches" wide
        Bitmap scaleBird = BitmapFactory.decodeResource(context.getResources(), R.drawable.ostrich);
        scalingWidth = scaleBird.getWidth()*1.5f;
    }

    /**
     * Getter for cloud ID number
     * @return current cloud ID number
     */
    public int getCloudID() { return cloudID; }

    /**
     * Setter for cloud ID number
     * @param cloudID new cloud ID number
     */
    public void setCloudID(int cloudID) { this.cloudID = cloudID; }

    /**
     * Determines if the game is finished
     * @return true if the game is over; false otherwise
     */
    public boolean inGameOverState() { return gameOver; }

    /**
     * Determines if the round number is even
     * @return true if the number is even
     */
    public boolean isEvenRound() { return roundNum % 2 == 0; }

    /**
     * Determines if the player's number is even
     * @return true if the number is even
     */
    public boolean isEvenPlayer() { return player.isEvenPlayer(); }

    /**
     * Whether the local player is the winner or not
     * @return true if this player is the winner
     */
    public boolean isWinner() { return player.isWinner(); }

    /**
     * Setter for the winner variable of the player
     * @param winner true if the player is the winner
     */
    public void setWinner(boolean winner) { player.setWinner(winner); }

    /**
     * Get the current player
     * @return current player
     */
    public Player getPlayer() { return player; }

    /**
     * Increment the round number by one, returns the new round number
     * @return
     */
    public int incrementRoundNum() { roundNum++; return roundNum; }

    /**
     * Set the current player's bird selection
     * @param selection the bird selected to place this round
     */
    public void setPlayerSelection(Bird selection) {
        Bird copyOfSelected = new Bird(selection);
        player.setSelectedBird(copyOfSelected);
        dragging = copyOfSelected;
    }

    /**
     * Get the current number of birds placed
     * @return the current number of birds placed
     */
    public int getNumBirdsPlaced() { return birds.size(); }

    /**
     * Confirms the player has chosen where their bird goes
     */
    public void confirmBirdPlacement() {
        // Check to see if the player's bird collides with any other bird
        for(Bird bird : birds) {
            if(player.getSelectedBird().collisionTest(bird)) {
                gameOver = true;
                return;
            }
        }

        birds.add(player.getSelectedBird());
    }

    /**
     * Removes the recently added bird
     * Used when the push fails
     */
    public void removeAddedBird() { birds.remove(player.getSelectedBird()); }

    /**
     * Draw the game
     * @param canvas the canvas to draw on
     */
    public void draw(Canvas canvas) {

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        // The puzzle size is the view scale ratio of the minimum dimension, to make it square
        int minSide = (int)((width < height ? width : height) * SCALE_IN_VIEW);

        scaleFactor = minSide/scalingWidth;

        // Margins for centering the puzzle
        int marginX = (int) ((width - minSide) / (scaleFactor * 2));
        int marginY = (int) ((height - minSide) / (scaleFactor * 2));


        gameSize = (int)scalingWidth;


        canvas.save();

        canvas.scale(scaleFactor, scaleFactor);

        // Draw the outline of the gameplay area
        canvas.drawRect(marginX - BORDER_WIDTH, marginY - BORDER_WIDTH,
                marginX + gameSize + BORDER_WIDTH, marginY + gameSize + BORDER_WIDTH, outlinePaint);


        for (Bird bird : birds) {
            bird.draw(canvas, marginX, marginY, gameSize);
        }

        if(dragging != null) {
            dragging.draw(canvas, marginX, marginY, gameSize);
        }

        canvas.restore();
    }

    public void reloadBirds(Context context) {

        for (Bird bird : birds) {
            bird.reloadBitmap(context);
        }

        player.getSelectedBird().reloadBitmap(context);

        // Birds will be scaled so that the game is "1.5 ostriches" wide
        Bitmap scaleBird = BitmapFactory.decodeResource(context.getResources(), R.drawable.ostrich);
        scalingWidth = scaleBird.getWidth()*1.5f;
    }

    /**
     * Handle a touch event from the view.
     * @param view The view that is the source of the touch
     * @param event The motion event describing the touch
     * @return true if the touch is handled.
     */
    public boolean onTouchEvent(View view, MotionEvent event) {

        // Convert an x,y location to a relative location in the puzzle
        float relX = event.getX();
        float relY = event.getY();



        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                lastRelX = relX;
                lastRelY = relY;
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                break;

            case MotionEvent.ACTION_MOVE:
                if (dragging != null) {
                    dragging.move((relX - lastRelX) * 1/scaleFactor, (relY - lastRelY) * 1/scaleFactor, gameSize);
                    lastRelX = relX;
                    lastRelY = relY;
                    view.invalidate();
                    return true;
                }
                break;
        }
        return false;
    }

    public void LoadXML(XmlPullParser xmlParser, View view) {
        ArrayList<Bird> tempList = new ArrayList<>();

        float relX;
        float relY;
        int id;

        try {
            // Get the context of the view for the Bird constructor
            Context context = view.getContext();

            while (xmlParser.getEventType() != XmlPullParser.END_DOCUMENT) {

                if (xmlParser.getEventType() == XmlPullParser.END_TAG)
                    xmlParser.nextTag();

                if (xmlParser.getName().equals("bird")) {
                    relX = Float.parseFloat(xmlParser.getAttributeValue(null, "relX"));
                    relY = Float.parseFloat(xmlParser.getAttributeValue(null, "relY"));
                    id = Integer.parseInt(xmlParser.getAttributeValue(null, "id"));

                    // Add to temp list
                    tempList.add(new Bird(context, id, relX, relY));
                }
                else if (xmlParser.getName().equals("game")) {
                    gameOver = xmlParser.getAttributeValue(null, "gameOver").equals("true");
                    roundNum = Integer.parseInt(xmlParser.getAttributeValue(null, "round"));
                }

                // Advance to next tag
                xmlParser.nextTag();

                // If we've found the flock end tag, stop the loop
                if (xmlParser.getName().equals("flock") && (xmlParser.getEventType() == XmlPullParser.END_TAG)) {
                    break;
                }
            }
        } catch (Exception ex) {
            Log.e("EXCEPTION", "Exception Game.LoadXML");
        }

        // Set bird list to temp list
        birds = tempList;
    }

    public void CreateXML(XmlSerializer xmlSerializer) {

        // Create an XML packet
        try
        {
            for (Bird bird : birds) {
                xmlSerializer.startTag(null, "bird");

                xmlSerializer.attribute(null, "id", String.valueOf(bird.getId()));
                xmlSerializer.attribute(null, "relX", String.valueOf(bird.getRelX()));
                xmlSerializer.attribute(null, "relY", String.valueOf(bird.getRelY()));

                xmlSerializer.endTag(null, "bird");
            }

            xmlSerializer.startTag(null, "game");

            xmlSerializer.attribute(null, "round", String.valueOf(roundNum));
            xmlSerializer.attribute(null, "gameOver", String.valueOf(gameOver));

            xmlSerializer.endTag(null, "game");
        }
        catch (IOException e)
        {
            // This won't occur when writing to a string
            Log.e("CREATE XML", "Error creating XML");
        }
    }

    public void saveInstanceState(Bundle bundle, Context context) {
        bundle.putSerializable(context.getString(R.string.game_state), this);
    }
}
