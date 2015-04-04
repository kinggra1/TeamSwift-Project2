package edu.msu.kinggra1.teamswift_project2;

import android.util.Pair;
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

    private static final String UTF8 = "UTF-8";
    private static final String NOT_ENTERED = "Username or password not entered";
    private static final String EXCEPTION = "An exception occurred while communicating with the server";

    public String Register(String username, String password, String confirmPassword)
    {
        // Trim leading white spaces on user and pass
        username = username.trim();
        password = password.trim();
        confirmPassword = confirmPassword.trim();

        // If there is no username or password length, then we can't log in
        if(username.length() == 0 || password.length() == 0 || confirmPassword.length() == 0)
        {
            return NOT_ENTERED;
        }

        ArrayList<Pair<String, String>> attrList = new ArrayList<>();
        attrList.add(new Pair<>("user", username));
        attrList.add(new Pair<>("pass", password));
        attrList.add(new Pair<>("repass", confirmPassword));
        attrList.add(new Pair<>("magic", MAGIC));

        String xmlReturn = SendAndReceiveXML("flock", attrList, REGISTER_URL);

        if (xmlReturn != null)
        {
            return xmlReturn;
        }

        return null;
    }

    public String LogIn(String username, String password)
    {
        return LogInOrOut(username, password, LOGIN_URL);
    }

    public String LogOut(String username, String password)
    {
        return LogInOrOut(username, password, LOGOUT_URL);
    }

    public String LogInOrOut(String username, String password, String url)
    {
        // Trim leading white spaces on user and pass
        username = username.trim();
        password = password.trim();

        // If there is no username or password length, then we can't log in
        if(username.length() == 0 || password.length() == 0)
        {
            return NOT_ENTERED;
        }

        ArrayList<Pair<String, String>> attrList = new ArrayList<>();
        attrList.add(new Pair<>("user", username));
        attrList.add(new Pair<>("pass", password));
        attrList.add(new Pair<>("magic", MAGIC));

        String xmlReturn = SendAndReceiveXML("flock", attrList, url);

        if (xmlReturn != null)
        {
            return xmlReturn;
        }

        return null;
    }

    public void PushBirdData(String username, String password, String xmlData)
    {

    }

    /**
     * Sends xml to the given url and returns the msg data if the operation failed
     * @param tag start and end tag of the xml
     * @param attrList list of attributes to add
     * @param urlStr URL to use to send the data
     * @return If successful, null. If failed, returns the msg data
     */
    public String SendAndReceiveXML(String tag, List<Pair<String, String>> attrList, String urlStr)
    {
        // Serializer used to create XML, stringwriter used to capture xml output
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        // Create an XML packet
        try
        {
            xmlSerializer.setOutput(writer);

            xmlSerializer.startDocument(UTF8, true);

            xmlSerializer.startTag(null, tag);

            for (Pair<String, String> pair : attrList)
            {
                xmlSerializer.attribute(null, pair.first, pair.second);
            }

            xmlSerializer.endTag(null, tag);

            xmlSerializer.endDocument();
        }
        catch (IOException e)
        {
            // This won't occur when writing to a string
            return EXCEPTION;
        }

        // Convert string writer to string
        final String xmlStr = writer.toString();

        //Convert the XML into HTTP POST data
        String postDataStr;

        try
        {
            postDataStr = "xml=" + URLEncoder.encode(xmlStr, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            return EXCEPTION;
        }

        //Send the data to the server
        byte[] postData = postDataStr.getBytes();

        InputStream inputStream = null;

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
                return EXCEPTION;
            }

            inputStream = conn.getInputStream();

            //Create an XML parser for the result
            try
            {
                XmlPullParser xmlParser = Xml.newPullParser();
                xmlParser.setInput(inputStream, UTF8);

                xmlParser.nextTag();      // Advance to first tag
                xmlParser.require(XmlPullParser.START_TAG, null, "flock");

                String xmlStatus = xmlParser.getAttributeValue(null, "status");

                if(xmlStatus.equals("no"))
                {
                    return xmlParser.getAttributeValue(null, "msg");
                }
                // We are done
            }
            catch(XmlPullParserException ex)
            {
                return EXCEPTION;
            }
            catch(IOException ex)
            {
                return EXCEPTION;
            }
        }
        catch (MalformedURLException e)
        {
            return EXCEPTION;
        }
        catch (IOException ex)
        {
            return EXCEPTION;
        }
        finally
        {
            if(inputStream != null)
            {
                try
                {
                    inputStream.close();
                }
                catch(IOException ex)
                {
                    // Fail silently
                }
            }
        }

        return null;
    }
}
