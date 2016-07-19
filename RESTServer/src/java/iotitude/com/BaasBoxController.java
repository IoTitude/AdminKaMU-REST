/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package iotitude.com;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author h3694
 */
public class BaasBoxController {

    public BaasBoxController() {
    }
    
    public String logIn() {
        try {
            
            // Data parameters for POST method
            String urlParameters = "username=admin&password=admin&appcode=1234567890";
            byte[] postData = urlParameters.getBytes();
            // URL to connect to
            String urlStr = "http://192.168.142.37:9000/login";
            // Creating HttpURLConnection
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            // Request method. SetDoOutput must be declared to be able to POST/PUT output (parameters)
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            // Writer for data parameters
            DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
            wr.write(postData);
            // Readers for reading result
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            
            // Convert stringBuilder to string and create a JSON object based on that string, then create another JSON object to get specific data
            String jsonText = stringBuilder.toString();
            JSONObject json = new JSONObject(jsonText);
            JSONObject data = json.getJSONObject("data");
            // Get BaasBox session id. Needed for other queries
            String session = data.getString("X-BB-SESSION");
            return session;
            
        } catch (IOException ex) {
            
            System.out.println(ex.getMessage());
            return null;
        }
    }
    
    public JSONObject getDocuments(String session, String collection) {
        try {
            // Get documents from specific collection
            String urlStr = "http://192.168.142.37:9000/document/" + collection;
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(true);
            // SetRequestProperty sets needed headers
            conn.setRequestProperty("X-BB-SESSION", session);
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            String jsonText = stringBuilder.toString();
            JSONObject json = new JSONObject(jsonText);
            // Return result JSON object. We parse needed data from this later
            return json;
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
    
    public void getAdminHashes(String session) {
        // Get documents from Admin collection
        JSONObject json = getDocuments(session, "Admin");
        JSONArray data = json.getJSONArray("data");
        List<String> hashes = new ArrayList<>();
        // Iterate through JSON array and add each hash to list. This list can be returned
        for (int i = 0; i < data.length(); i++) {
            JSONObject hashObj = data.getJSONObject(i);
            String hash = hashObj.getString("hash");
            hashes.add(hash);
        }
    }
    
    public JSONArray getDeviceInfo(String session) {
        // Get documents from Device collection
        JSONObject json = getDocuments(session, "Master");
        JSONArray data = json.getJSONArray("data");
        return data;
    }
    
    public JSONArray getProfileInfo(String session) {
        // Get documents from Profile collection
        JSONObject json = getDocuments(session, "Profile");
        JSONArray data = json.getJSONArray("data");
        return data;
    }
    
    public JSONArray getVersionInfo(String session) {
        // Get documents from Profile collection
        JSONObject json = getDocuments(session, "Version");
        JSONArray data = json.getJSONArray("data");
        return data;
    }
    
    public boolean updateDeviceHash(String session, String mac, String hash) {
        try {
            JSONArray deviceData = getDeviceInfo(session);
            JSONObject obj = new JSONObject();
            String id = "";
            // Iterate through JSON array. Compare each mac address for the one we are looking for. Here "perkele" will be changed to a parameter
            for (int i = 0; i < deviceData.length(); i++) {
                obj = deviceData.getJSONObject(i);
                if (obj.getString("mac").equals(mac)) {
                    
                    // Break the loop when the correct document is found and save the id from that document
                    id = obj.getString("id");
                    break;
                }
            }
            // New JSON object. This will be used to rewrite data on the document
            JSONObject newObj = new JSONObject();
            // Loop through the original object. Change stuff when key is hash
            for (String key : obj.keySet()) {
                newObj.put(key, obj.get(key));
                if (key.equals("hash")) {
                    newObj.put(key, hash);
                }
            }
            // Convert new JSON object to string and then to bytes
            String json = newObj.toString();
            byte[] postData = json.getBytes();
            //System.out.println(id);
            String urlStr = "http://192.168.142.37:9000/document/Master/" + id;
            HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
            conn.setRequestMethod("PUT");
            conn.setDoOutput(true);
            conn.setRequestProperty("X-BB-SESSION", session);
            conn.setRequestProperty("Content-type", "application/json");
            DataOutputStream wr = new DataOutputStream( conn.getOutputStream());
            wr.write(postData);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = reader.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            System.out.println(stringBuilder.toString());
            JSONObject result = new JSONObject(stringBuilder.toString());
            if (result.getString("result").contains("ok")) {
                return true;
            }
            else {
                return false;
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return false;
        }
    }
}
