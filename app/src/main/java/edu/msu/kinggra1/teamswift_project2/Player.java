package edu.msu.kinggra1.teamswift_project2;

import android.util.Log;

import java.io.Serializable;

public class Player implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * Get the player's name
     * @return the player's name
     */
    public String getName() {
        return name;
    }

    /**
     * The player's name
     */
    private String name;

    /**
     * Get the selected bird
     * @return the selected bird
     */
    public Bird getSelectedBird() {
        return selectedBird;
    }

    /**
     * Set the selected bird
     * @param selectedBird the selected bird
     */
    public void setSelectedBird(Bird selectedBird) {
        Log.i("setSelectedBird()", "bird set!" + selectedBird);
        this.selectedBird = selectedBird;
    }

    /**
     * The selected bird
     */
    private Bird selectedBird;

    public Player(String name) {
        this.name = name;
    }
}
