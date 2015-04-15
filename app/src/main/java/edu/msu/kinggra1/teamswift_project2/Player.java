package edu.msu.kinggra1.teamswift_project2;

import android.util.Log;

import java.io.Serializable;

public class Player implements Serializable {

    private static final long serialVersionUID = 2L;

    /**
     * The number (1 or 2) of this player
     */
    private int playerNumber = 0;

    /**
     * Whether this player is the winner (tested in final score activity)
     */
    private boolean winner = false;

    /**
     * The selected bird
     */
    private Bird selectedBird;

    /**
     * Setter for player number
     * @param playerNumber new player number
     */
    public void setPlayerNumber(int playerNumber) { this.playerNumber = playerNumber; }

    /**
     * Getter for player number
     * @return the current player number
     */
    public int getPlayerNumber() { return playerNumber; }

    /**
     * Get the selected bird
     * @return the selected bird
     */
    public Bird getSelectedBird() {
        return selectedBird;
    }

    /**
     * Getter for the winner variable
     * @return true if this player is the winner
     */
    public boolean isWinner() { return winner; }

    /**
     * Setter for winner variable
     * @param winner whether the player is the winner or not
     */
    public void setWinner(boolean winner) { this.winner = winner; }

    /**
     * Set the selected bird
     * @param selectedBird the selected bird
     */
    public void setSelectedBird(Bird selectedBird) {
        Log.i("setSelectedBird()", "bird set!" + selectedBird);
        this.selectedBird = selectedBird;
    }

    /**
     * Determines if the player's number is even
     * @return true if the number is even
     */
    public boolean isEvenPlayer() { return playerNumber % 2 == 0; }
}
