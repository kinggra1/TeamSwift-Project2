package edu.msu.kinggra1.teamswift_project2;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
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
    private static final String UTF8 = "UTF-8";

    public boolean LogIn(String username, String password) {

        // Trim leading white spaces on user and pass
        username = username.trim();
        password = password.trim();

        // If there is no username or password length, then we can't log in
        if(username.length() == 0 || password.length() == 0) {
            return false;
        }

        // Serializer used to create XML, stringwriter used to capture xml output
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        // Create an XML packet
        try {
            xmlSerializer.setOutput(writer);

            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag(null, "flock");
            xmlSerializer.attribute(null, "user", username);
            xmlSerializer.attribute(null, "pass", password);
            xmlSerializer.attribute(null, "magic", MAGIC);

            xmlSerializer.endTag(null, "flock");

            xmlSerializer.endDocument();

        } catch (IOException e) {
            // This won't occur when writing to a string
            return false;
        }

        // Convert stringwriter to string
        final String xmlStr = writer.toString();

        //Convert the XML into HTTP POST data
        String postDataStr;

        try {
            postDataStr = "xml=" + URLEncoder.encode(xmlStr, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return false;
        }

        //Send the data to the server
        byte[] postData = postDataStr.getBytes();

        InputStream inputStream = null;

        try {
            URL url = new URL(LOGIN_URL);

            HttpURLConnection conn = (HttpURLConnection)url.openConnection();

            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Length", Integer.toString(postData.length));
            conn.setUseCaches(false);

            OutputStream outputStream = conn.getOutputStream();
            outputStream.write(postData);
            outputStream.close();

            int responseCode = conn.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return false;
            }

            inputStream = conn.getInputStream();

            //Create an XML parser for the result
            try {
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream, UTF8);

                xmlParser.nextTag();      // Advance to first tag
                xmlParser.require(XmlPullParser.START_TAG, null, "flock");

                String xmlStatus = xmlParser.getAttributeValue(null, "status");
                if(xmlStatus.equals("no")) {
                    return false;
                }

                // We are done
            } catch(XmlPullParserException ex) {
                return false;
            } catch(IOException ex) {
                return false;
            }

        } catch (MalformedURLException e) {
            return false;
        } catch (IOException ex) {
            return false;
        } finally {
            if(inputStream != null) {
                try {
                    inputStream.close();
                } catch(IOException ex) {
                    // Fail silently
                }
            }
        }

        return true;
    }

    public void LogOut() {

    }

    public void PushBirdData() {

    }
}
