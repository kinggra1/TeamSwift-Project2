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
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;

public class Game implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String UTF8 = "UTF-8";
    private static final String COMM_EXCEPTION = "An exception occurred while communicating with the server";
    private static final String PARSING_EXCEPTION = "An exception occurred while parsing the server's return";

    /**
     * Used to track what state the game is currently in
     */
    private enum GameState {
        nameEntry,
        birdSelection,
        birdPlacement,
        gameOver
    }

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
     * The first player in the game
     */
    private Player player1;

    /**
     * The second player in the game
     */
    private Player player2;

    /**
     * The player that won the game
     */
    private Player winner;

    /**
     * The player turn: the first player to go for 0, or the second player to go for 1
     */
    private int playerTurn = 0;

    /**
     * The current round number (0 based)
     */
    private int roundNum = 0;

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
     * The current stage of the game
     */
    private GameState state = GameState.birdSelection;

    /**
     * Amount of time for the cloud thread to sleep between checks
     */
    private long sleepTime = 1000;

    /**
     * Whether the cloud thread is runnable currently.
     * Set to false when we want to kill the thread.
     */
    private boolean pullThreadRunnable = true;

    /**
     * Unique cloud identifier number to test for updates
     */
    private int cloudID = 0;

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
     * Determines if the game is in the selection state
     * @return true if the game is in the selection state; false otherwise
     */
    public boolean inSelectionState() {
        return state.equals(GameState.birdSelection);
    }

    /**
     * Determines if the game is in the game over state
     * @return true if the game is over; false otherwise
     */
    public boolean inGameOverState() { return state.equals(GameState.gameOver); }

    /**
     * Get the current player who's turn it is
     * @return the player who's turn it is
     */
    private Player getCurrentPlayer() {
        if(playerTurn == 0) {
            if(roundNum % 2 == 0) return player1;
            else return player2;
        }
        else {
            if(roundNum % 2 == 1) return player1;
            else return player2;
        }
    }

    /**
     * Get the player who's turn is next
     * @return the player who's turn is next
     */
    private Player getNextPlayer() {
        if(getCurrentPlayer() == player1) return player2;
        else return player1;
    }

    /**
     * Advance the game by one turn
     */
    private void advanceTurn() {
        if(isSecondTurn()) {
            playerTurn = 0;

            if(state == GameState.birdSelection) {
                state = GameState.birdPlacement;
                dragging = getCurrentPlayer().getSelectedBird();
            }
            else {
                state = GameState.birdSelection;
                dragging = null;
                roundNum++;
            }
        }
        else {
            playerTurn = 1;
            dragging = getCurrentPlayer().getSelectedBird();
        }
    }

    /**
     * Get whether the second player in the current state has their turn now
     * @return true if the second player in the current state is playing; false otherwise
     */
    private boolean isSecondTurn() {
        return playerTurn == 1;
    }

    /**
     * Set the names of the players playing the game
     * @param name1 player 1's name
     * @param name2 player 2's name
     */
    public void setPlayerNames(String name1, String name2) {
        player1 = new Player(name1);
        player2 = new Player(name2);

        state = GameState.birdSelection;
    }

    /**
     * Set the current player's bird selection
     * @param selection the bird selected to place this round
     */
    public void setPlayerSelection(Bird selection) {
        Bird copyOfSelected = new Bird(selection);
        getCurrentPlayer().setSelectedBird(copyOfSelected);

        advanceTurn();
    }

    /**
     * Confirms the player has chosen where their bird goes
     */
    public synchronized void confirmBirdPlacement() {
        // Check to see if the player's bird collides with any other bird
        for(int itr = 0; itr < birds.size(); itr++) {
            if(getCurrentPlayer().getSelectedBird().collisionTest(birds.get(itr))) {
                declareWinner(getNextPlayer());
                return;
            }
        }

        birds.add(getCurrentPlayer().getSelectedBird());

        advanceTurn();
    }

    /**
     * Set the passed player as the winner, and move the game into the final state
     * @param winner the player who won
     */
    private void declareWinner(Player winner) {
        this.winner = winner;
        state = GameState.gameOver;
    }

    /**
     * Gets the current player's name
     * @return the player's name
     */
    public String getCurrentPlayerName() {
        return getCurrentPlayer().getName();
    }

    /**
     * Get the name of the player who won
     * @return the name of the player who won
     */
    public String getWinningPlayerName() { return winner.getName(); }

    /**
     * Get the current number of birds placed
     * @return the current number of birds placed
     */
    public int getNumBirdsPlaced() { return birds.size(); }

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


        synchronized (this) {
            for (Bird bird : birds) {
                bird.draw(canvas, marginX, marginY, gameSize);
            }
        }

        if(dragging != null) {
            dragging.draw(canvas, marginX, marginY, gameSize);
        }

        canvas.restore();
    }

    public void reloadBirds(Context context) {

        synchronized (this) {
            for (Bird bird : birds) {
                bird.reloadBitmap(context);
            }
        }

        player1.getSelectedBird().reloadBitmap(context);
        player2.getSelectedBird().reloadBitmap(context);

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
    public void saveInstanceState(Bundle bundle, Context context) {
        bundle.putSerializable(context.getString(R.string.game_state), this);
        // Kill the cloud thread
        pullThreadRunnable = false;
    }

    private String CreateXML() {

        // Serializer used to create XML, stringwriter used to capture xml output
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        // Create an XML packet
        try
        {
            xmlSerializer.setOutput(writer);

            xmlSerializer.startDocument(UTF8, true);

            synchronized (this) {
                for (Bird bird : birds) {
                    xmlSerializer.startTag(null, "bird");

                    xmlSerializer.attribute(null, "id", String.valueOf(bird.getId()));
                    xmlSerializer.attribute(null, "relX", String.valueOf(bird.getRelX()));
                    xmlSerializer.attribute(null, "relY", String.valueOf(bird.getRelY()));
                    xmlSerializer.attribute(null, "x", String.valueOf(bird.getX()));
                    xmlSerializer.attribute(null, "y", String.valueOf(bird.getY()));

                    xmlSerializer.endTag(null, "bird");
                }
            }

            xmlSerializer.endDocument();
        }
        catch (IOException e)
        {
            // This won't occur when writing to a string
            return null;
        }

        // Convert string writer to string
        return writer.toString();
    }

    private void LoadXML(String xmlStr, GameView view) {
        ArrayList<Bird> tempList = new ArrayList<>();

        float x;
        float y;
        float relX;
        float relY;
        int id;

        try {
            XmlPullParser xmlParser = Xml.newPullParser();
            xmlParser.setInput(new StringReader(xmlStr));

            // Advance to first tag
            xmlParser.nextTag();

            // Get the context of the view for the Bird constructor
            Context context = view.getContext();

            while (xmlParser.getEventType() != xmlParser.END_DOCUMENT) {

                if (xmlParser.getEventType() == XmlPullParser.END_TAG)
                    xmlParser.nextTag();

                xmlParser.require(XmlPullParser.START_TAG, null, "bird");

                x = Float.parseFloat(xmlParser.getAttributeValue(null, "x"));
                y = Float.parseFloat(xmlParser.getAttributeValue(null, "y"));
                relX = Float.parseFloat(xmlParser.getAttributeValue(null, "relX"));
                relY = Float.parseFloat(xmlParser.getAttributeValue(null, "relY"));
                id = Integer.parseInt(xmlParser.getAttributeValue(null, "id"));

                // Add to temp list
                tempList.add(new Bird(context, id, relX, relY, x, y));

                // Advance to next tag
                xmlParser.nextTag();
            }
        } catch (Exception ex) {
            Log.e("EXCEPTION", "Exception Game.LoadXML");
        }

        synchronized (this) {
            // Set bird list to temp list
            birds = tempList;
        }
    }

    public void startPushThread(final GameView view) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // Get a new cloud instance
                Cloud cloud = new Cloud();

                String xmlStr = CreateXML();

                InputStream stream = cloud.Push(xmlStr);

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

    public void startPullThread(final GameView view) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Get a new cloud instance
                Cloud cloud = new Cloud();

                InputStream stream;

                while (pullThreadRunnable) {
                    // Pull data from the cloud, get the input stream
                    stream = cloud.Pull(cloudID);

                    if (stream != null) {
                        try {
                            //Create an XML parser for the stream
                            XmlPullParser xmlParser = Xml.newPullParser();
                            xmlParser.setInput(stream, UTF8);

                            xmlParser.nextTag();      // Advance to first tag
                            xmlParser.require(XmlPullParser.START_TAG, null, "flock");

                            String xmlStatus = xmlParser.getAttributeValue(null, "status");
                            String xmlMsg = xmlParser.getAttributeValue(null, "msg");

                            // Check if this thread should be running
                            if (!pullThreadRunnable)
                                return;

                            if (xmlStatus.equals("yes") && xmlMsg != null) {
                                String newCloudID = xmlParser.getAttributeValue(null, "id");

                                // Update id
                                cloudID = Integer.parseInt(newCloudID);

                                LoadXML(xmlMsg, view);
                            }
                            else if (xmlStatus.equals("no") && xmlMsg != null) {
                                ToastMessage(xmlMsg);
                            }
                        }
                        catch (Exception ex) {
                            ToastMessage(PARSING_EXCEPTION);
                        }
                    }
                    else {
                        ToastMessage(COMM_EXCEPTION);
                    }

                    // Check if this thread should be running
                    if (!pullThreadRunnable)
                        return;

                    // Sleep each while loop
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
