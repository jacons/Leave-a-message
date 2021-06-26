package project.leaveamessage.background;

import android.os.AsyncTask;
import android.util.Log;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static project.leaveamessage.utils.Utils.ERRORTAG;

    public class NewUserTask extends AsyncTask<String,Void,Void> {

    private URL urlInsertUser;

    public NewUserTask(){
        try {
            urlInsertUser = new URL("xxx");
        } catch (MalformedURLException ex) { ex.printStackTrace(); }
    }
    @Override
    protected Void doInBackground(String... strings) {

        StringBuilder result = new StringBuilder();

        HttpURLConnection http;
        try {
            // Stabilisco una connessione
            http = (HttpURLConnection) urlInsertUser.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");
        } catch (IOException e) {
            Log.d(ERRORTAG,"Errore nella openConnection()");
            return null;
        }

        try(OutputStream stream = http.getOutputStream()) {

            String data = "{\"request\":2,\"nome\":\""+strings[0]+"\",\"cognome\":\""+strings[1]+"\",\"id\":\""+strings[2]+"\"}";
            stream.write(data.getBytes(StandardCharsets.UTF_8));
            stream.flush();
        } catch (IOException e) {
            Log.e(ERRORTAG,"Errore in write() "+e.getMessage());
            return null;
        }

        try(BufferedReader reader = new BufferedReader(new InputStreamReader(http.getInputStream()))) {
            String output;
            while ((output = reader.readLine()) != null)  result.append(output);
        } catch (IOException e) {
            Log.e(ERRORTAG,"Errore in readLine()");
            return null;
        }
        http.disconnect();
        Log.d("USER INSERT", result.toString());
        return null;
    }
    
}
