package edu.ucsd.neurores;

/**
 * Created by tbpetersen on 11/16/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeSet;
import java.util.Set;
import java.io.DataOutputStream;
import java.net.ProtocolException;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

public class LoginTask extends AsyncTask<String,Void, String>{
    String username, password;
    RequestWrapper.OnCompleteListener ocl;
    boolean loginSuccessful;

    LoginTask(String username, String password,RequestWrapper.OnCompleteListener ocl){
        this.username = username;
        this.password = password;
        this.ocl = ocl;
        loginSuccessful = false;
    }

    @Override
    protected String doInBackground(String... params) {
        return login(username, password);
    }

    @Override
    protected void onPostExecute(String result){
        if(loginSuccessful){
            ocl.onComplete(result);
        }else{
            ocl.onError(result);
        }
    }

    private String login(String username, String password){
        String shibURL = "https://neurores.ucsd.edu/token/tokenGenerator.php";
        Set<String> allCookies = new TreeSet<String>();
        HttpsURLConnection con = null;
        HttpURLConnection.setFollowRedirects(false);

        try {
            // First
            con = performGET(shibURL, allCookies, 302);
            con.disconnect();

            //Second
            String nextURL = con.getHeaderField("Location");
            con = performGET(nextURL, allCookies, 302);
            con.disconnect();

            //Third
            nextURL = con.getHeaderField("Location");
            con = performGET(nextURL, allCookies, 302);
            con.disconnect();

            //Fourth
            nextURL = con.getHeaderField("Location");
            con = performGET(nextURL, allCookies, 200);
            con.disconnect();

            // Submit username/password

            List<Pair> formDataPairs = new ArrayList<Pair>();
            formDataPairs.add(new Pair("initAuthMethod", "urn:mace:ucsd.edu:sso:ad"));
            formDataPairs.add(new Pair("submit", "submit"));
            formDataPairs.add(new Pair("urn:mace:ucsd.edu:sso:authmethod", "urn:mace:ucsd.edu:sso:ad"));
            formDataPairs.add(new Pair("urn:mace:ucsd.edu:sso:username", username));
            formDataPairs.add(new Pair("urn:mace:ucsd.edu:sso:password", password));

            con = performPOST(nextURL, allCookies, formDataPairs, 302);
            con.disconnect();

            // Follow Redirect
            nextURL = con.getHeaderField("Location");
            con = performGET(nextURL, allCookies, 200);
            String body = getBody(con);
            con.disconnect();

            allCookies.clear();

            //Return to original site
            List<Pair> formData = getFormDataFromSSOPage(body);
            nextURL = getActionFromFirstForm(body);
            con = performPOST(nextURL, allCookies, formData, 302);

            //Redirect to page and get token
            nextURL = con.getHeaderField("Location");
            con = performGET(nextURL, allCookies, 200);
            body = getBody(con);
            con.disconnect();

            loginSuccessful = true;
            return getToken(body);
        }catch(Exception e){
            return "Failed";
        }
    }

    private String getToken(String body) {
        int index = body.indexOf("</body>");
        return body.substring(0, index);
    }

    public String getBody(HttpsURLConnection con) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    public List<String> getCookies(HttpsURLConnection con) {
        Map<String, List<String>> map = con.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getKey() != null && entry.getKey().toLowerCase().equals("set-cookie")) {
                return entry.getValue();
            }
        }
        return new ArrayList<String>();
    }

    public void combineCookies(Collection<String> newCookies, Collection<String> oldCookies) {
        for (String s : newCookies) {
            oldCookies.add(s);
        }
    }

    public void printCookies(HttpsURLConnection con) {
        Map<String, List<String>> map = con.getHeaderFields();
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            //System.out.println("Key : " + entry.getKey() + " ,Value : " + entry.getValue());
        }
    }

    public void setCookies(HttpURLConnection con, Collection<String> cookies) {
        String value = "";
        for (String s : cookies) {
            value += s + ";";
        }
        con.setRequestProperty("Cookie", value);
    }


    public String extractSubstring(String baseString, String beginSequence, String endSequence) {
        int indexOfActionStart = baseString.indexOf(beginSequence) + beginSequence.length();

        if (indexOfActionStart == -1) {
            return null;
        }

        String substringOfBaseString = baseString.substring(indexOfActionStart);
        int indexOfActionEnd = substringOfBaseString.indexOf(endSequence);

        return substringOfBaseString.substring(0, indexOfActionEnd);
    }

    public String decodeHTMLEntities(String text) {
        text = text.replace("&#x3a;", ":");
        text = text.replace("&#x2f;", "/");

        return text;
    }

    public String getActionFromFirstForm(String html) {
        String beginSequence = "<form action=\"";
        String endSequence = "\" method";
        String action = extractSubstring(html, beginSequence, endSequence);
        action = decodeHTMLEntities(action);
        return action;
    }

    public List<String> getInputsFromHtml(String html) {
        List<String> inputs = new ArrayList<String>();
        String seq = "<input";
        String endSeq = "/>";

        while (html.contains(seq)) {
            inputs.add(extractSubstring(html, seq, endSeq));
            html = html.substring(html.indexOf(seq) + seq.length());
        }

        return inputs;
    }

    public String getInputAttr(String input, String attr) {
        return extractSubstring(input, attr + "=\"", "\"");
    }

    public List<Pair> getFormDataFromSSOPage(String html) throws UnsupportedEncodingException {
        String body = html;
        List<String> inputs = getInputsFromHtml(body);

        String relayStateName = getInputAttr(inputs.get(0), "name");
        String relayStateValue = decodeHTMLEntities(getInputAttr(inputs.get(0), "value"));
        String samlName = getInputAttr(inputs.get(1), "name");
        String samlValue = getInputAttr(inputs.get(1), "value");

        List<Pair> formDataPairs = new ArrayList<Pair>();
        formDataPairs.add(new Pair(relayStateName, relayStateValue));
        formDataPairs.add(new Pair(samlName, samlValue));

        return formDataPairs;
    }

    public String getEncodedFormDataString(List<Pair> list) throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (Pair pair : list) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append("&");
            }
            String firstEncoded = URLEncoder.encode(pair.getFirst(), "UTF-8");
            String secondEncoded = URLEncoder.encode(pair.getSecond(), "UTF-8");

            sb.append(firstEncoded + "=" + secondEncoded);
        }

        return sb.toString();
    }

    public HttpsURLConnection performGET(String urlString, Collection<String> cookies, int expResCode) throws IOException, Exception {
        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        con.setFollowRedirects(false);
        setCookies(con, cookies);

        if (con.getResponseCode() != expResCode) {
            throw new Exception("Expected " + expResCode + " from " + con.getURL().toString() + ". Received " + con.getResponseCode());
        }

        combineCookies(getCookies(con), cookies);

        return con;
    }

    public HttpsURLConnection performPOST(String urlString, Collection<String> cookies, List<Pair> postDataList, int expResCode) throws ProtocolException, IOException, Exception {
        URL url = new URL(urlString);
        HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
        byte[] postData = getEncodedFormDataString(postDataList).getBytes("UTF-8");
        int postDataLength = postData.length;
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("charset", "utf-8");
        con.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        con.setUseCaches(false);
        con.setDoOutput(true);
        con.setFollowRedirects(false);
        setCookies(con, cookies);

        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(postData);
        wr.flush();

        if (con.getResponseCode() != expResCode) {
            throw new Exception("Expected " + expResCode + " from " + con.getURL().toString() + ". Received " + con.getResponseCode());
        }

        combineCookies(getCookies(con), cookies);

        return con;
    }

    static class Pair {
        private String first, second;

        public Pair(String first, String second) {
            this.first = first;
            this.second = second;
        }

        public String getFirst() {
            return first;
        }

        public String getSecond() {
            return second;
        }

        public void setFirst(String newFirst) {
            first = newFirst;
        }

        public void setSecond(String newSecond) {
            second = newSecond;
        }

        public String toString() {
            return first + ": " + second;
        }

    }

}
