package edu.msu.kinggra1.teamswift_project2;

import android.util.Pair;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Communications class to the server
 */
public class Cloud {

    private static final String MAGIC = "s5VKYyBMzQrT6Ktx";
    private static final String REGISTER_URL = "http://webdev.cse.msu.edu/~kinggra1/cse476/Project2/register.php";
    private static final String LOGIN_URL = "http://webdev.cse.msu.edu/~kinggra1/cse476/Project2/login.php";
    private static final String LOGOUT_URL = "http://webdev.cse.msu.edu/~kinggra1/cse476/Project2/logout.php";
    private static final String PUSH_URL = "http://webdev.cse.msu.edu/~kinggra1/cse476/Project2/push.php";
    private static final String PULL_URL = "http://webdev.cse.msu.edu/~kinggra1/cse476/Project2/pull.php";
    private static final String WAIT_URL = "http://webdev.cse.msu.edu/~kinggra1/cse476/Project2/findgame.php";
    private static final String LOGOUTALL_URL = "http://webdev.cse.msu.edu/~kinggra1/cse476/Project2/logoutall.php";

    private static final String UTF8 = "UTF-8";

    /**
     * Register a new user on the server
     * @param username User
     * @param password Password
     * @param confirmPassword Password check
     * @return The server's response
     */
    public InputStream Register(String username, String password, String confirmPassword)
    {
        // Trim leading white spaces on user and pass
        username = username.trim();
        password = password.trim();
        confirmPassword = confirmPassword.trim();

        // If there is no username or password length, then we can't log in
        if(username.length() == 0 || password.length() == 0 || confirmPassword.length() == 0)
        {
            return null;
        }

        ArrayList<Pair<String, String>> attrList = new ArrayList<>();
        attrList.add(new Pair<>("user", username));
        attrList.add(new Pair<>("pass", password));
        attrList.add(new Pair<>("repass", confirmPassword));
        attrList.add(new Pair<>("magic", MAGIC));

        String xmlStr = CreateDefaultXML(attrList);

        return SendXML(xmlStr, REGISTER_URL);
    }

    /**
     * Log in to the server
     * @param username User
     * @param password Password
     * @return The server's response
     */
    public InputStream LogIn(String username, String password)
    {
        return LogInOrOut(username, password, LOGIN_URL);
    }

    /**
     * Log out of the server
     * @param username User
     * @param password Password
     * @return The server's response
     */
    public InputStream LogOut(String username, String password)
    {
        return LogInOrOut(username, password, LOGOUT_URL);
    }

    /**
     * Log in or out of the server
     * @param username User
     * @param password Password
     * @param url LOGIN or LOGOUT URL
     * @return The server's response
     */
    public InputStream LogInOrOut(String username, String password, String url)
    {
        // Trim leading white spaces on user and pass
        username = username.trim();
        password = password.trim();

        // If there is no username or password length, then we can't log in
        if(username.length() == 0 || password.length() == 0)
        {
            return null;
        }

        ArrayList<Pair<String, String>> attrList = new ArrayList<>();
        attrList.add(new Pair<>("user", username));
        attrList.add(new Pair<>("pass", password));
        attrList.add(new Pair<>("magic", MAGIC));

        String xmlStr = CreateDefaultXML(attrList);

        return SendXML(xmlStr, url);
    }

    /**
     * Pull the latest Game version from the server
     * @param id The current id this instance of the game is using
     * @return The server's response
     */
    public InputStream Pull(int id)
    {
        ArrayList<Pair<String, String>> attrList = new ArrayList<>();
        attrList.add(new Pair<>("magic", MAGIC));
        attrList.add(new Pair<>("id", String.valueOf(id)));

        String xmlStr = CreateDefaultXML(attrList);

        return SendXML(xmlStr, PULL_URL);
    }

    /**
     * Push the game XML to the server
     * @return The server's response
     */
    public InputStream Push(Game game)
    {
        ArrayList<Pair<String, String>> attrList = new ArrayList<>();
        attrList.add(new Pair<>("magic", MAGIC));

        String xmlStr = CreatePushXML(game);

        return SendXML(xmlStr, PUSH_URL);
    }

    /**
     * Wait for the server to find a game
     * @return The server's response
     */
    public InputStream Wait() {

        ArrayList<Pair<String, String>> attrList = new ArrayList<>();
        attrList.add(new Pair<>("magic", MAGIC));

        String xmlStr = CreateDefaultXML(attrList);

        return SendXML(xmlStr, WAIT_URL);
    }

    /**
     * Used for debugging purposes only - Log out all users on the server
     * @return The server's response
     */
    public InputStream LogOutAll() {
        ArrayList<Pair<String, String>> attrList = new ArrayList<>();
        attrList.add(new Pair<>("magic", MAGIC));

        String xmlStr = CreateDefaultXML(attrList);

        return SendXML(xmlStr, LOGOUTALL_URL);
    }

    /**
     * Sends xml to the given url and returns the msg data if the operation failed
     * @param xmlStr String of XML to send
     * @param urlStr URL to use to send the data
     * @return If successful, null. If failed, returns the msg data
     */
    public InputStream SendXML(String xmlStr, String urlStr)
    {
        //Convert the XML into HTTP POST data
        String postDataStr;

        try
        {
            postDataStr = "xml=" + URLEncoder.encode(xmlStr, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return null;
        }

        //Send the data to the server
        byte[] postData = postDataStr.getBytes();

        InputStream inputStream;

        try
        {
            URL url = new URL(urlStr);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(postData);
            outputStream.close();

            int responseCode = conn.getResponseCode();

            if (responseCode != HttpURLConnection.HTTP_OK)
            {
                return null;
            }

            inputStream = conn.getInputStream();
        }
        catch (MalformedURLException e)
        {
            return null;
        }
        catch (IOException ex)
        {
            return null;
        }

        return inputStream;
    }

    public String CreateDefaultXML(List<Pair<String, String>> attrList) {
        // Serializer used to create XML, stringwriter used to capture xml output
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        // Create an XML packet
        try
        {
            xmlSerializer.setOutput(writer);

            xmlSerializer.startDocument(UTF8, true);

            xmlSerializer.startTag(null, "flock");

            for (Pair<String, String> pair : attrList)
            {
                xmlSerializer.attribute(null, pair.first, pair.second);
            }

            xmlSerializer.endTag(null, "flock");

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

    public String CreatePushXML(Game game) {
        // Serializer used to create XML, stringwriter used to capture xml output
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        // Create an XML packet
        try
        {
            xmlSerializer.setOutput(writer);

            xmlSerializer.startDocument(UTF8, true);

            xmlSerializer.startTag(null, "flock");

            xmlSerializer.attribute(null, "id", String.valueOf(game.getCloudID()));

            xmlSerializer.attribute(null, "magic", MAGIC);

            game.CreateXML(xmlSerializer);

            xmlSerializer.endTag(null, "flock");

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
}
