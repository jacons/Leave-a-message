package project.leaveamessage.background;

import android.os.AsyncTask;
import android.util.Log;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import project.leaveamessage.interfaces.CallBackInterface;

import static project.leaveamessage.utils.Utils.ERRORTAG;

public class NearestMessageTask extends AsyncTask<Double,Void, JsonMessage> {


    private final WeakReference<CallBackInterface> callbackRef;
    private URL urlGetMessage;    //  Url per rest call
    private final Gson gson;      // avoid to allocate a lot of memory
    private final String userid;

    public NearestMessageTask(CallBackInterface c, Gson g, String id) {
        callbackRef = new WeakReference<>(c);
        gson = g;
        userid = id;
        try {
            urlGetMessage = new URL("xxx");
        } catch (MalformedURLException ex) { ex.printStackTrace(); }

    }
    @Override
    protected JsonMessage doInBackground(Double... params) {
        //retrieving the nearest message
        String result = GetNearestMessage(params[0],params[1]);
        JsonMessage nearestJsonMessage = null;

        if(result!=null) try {
            // reflection power!
            nearestJsonMessage = gson.fromJson(result, JsonMessage.class);
        } catch (Exception e) {
            Log.e(ERRORTAG,"Reflection error");
        }
        return nearestJsonMessage;
    }

    @Override
    protected void onPostExecute(JsonMessage jsonMessage) {
        super.onPostExecute(jsonMessage);
        CallBackInterface ref = callbackRef.get();
        if(ref!=null) {
            // jsonmessage may be null, for example if don't exist other messages to discover.
            if(jsonMessage!=null)ref.callBackResult(jsonMessage);
        } else { Log.e(ERRORTAG,"(NearestMessage-OnPostExecute)Errore nei puntatori"); }
    }
    public String GetNearestMessage(double latitude, double longitude) {

        StringBuilder result = new StringBuilder();
        HttpURLConnection http;

        try {
            http = (HttpURLConnection) urlGetMessage.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");
        } catch (IOException e) {
            Log.e(ERRORTAG,"Errore di openConnection()");
            return null;
        }

        try(OutputStream stream = http.getOutputStream()) {

            //data consist of android app's latitude/longitude
            String data = "{\"request\":1,\"latitude\": "+latitude+",\"longitude\":"+longitude+",\"id\":\""+userid+"\"}";
            Log.d("REST",data);
            stream.write(data.getBytes(StandardCharsets.UTF_8));
            stream.flush();
        } catch (IOException e) {
            Log.e(ERRORTAG,"Errore in write()");
            return null;
        }
        //BufferedReader for optimization
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
            String output;
            while ((output = reader.readLine()) != null)  result.append(output);
        } catch (IOException e) {
            Log.e(ERRORTAG,"Errore in readLine()");
            return null;
        }
        http.disconnect();
        return result.toString();
    }
}
