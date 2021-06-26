package project.leaveamessage.background;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static project.leaveamessage.utils.Utils.ERRORTAG;

public class NewMessageTask extends AsyncTask<Void,Void,String> {

    private final String idAccount;   // user id
    private final Double latitude;    // of message
    private final Double longitude;   // of message
    private final int type;           // type of message (only text,audio,image)
    private final String description; // message's description

    private final WeakReference<ProgressBar> progress;
    private WeakReference<ImageView> picture = null; // just in case
    private final WeakReference<Activity> activity;
    private URL urlGetMessage;

    public NewMessageTask(Activity activity, ProgressBar progress, int typeR, String descriptionR, String user, Double lat, Double lon) {
        this.activity = new WeakReference<>(activity);
        this.progress = new WeakReference<>(progress);
        type = typeR;
        description = descriptionR;
        latitude = lat;
        longitude = lon;
        idAccount = user;
        try {
            urlGetMessage = new URL("xxx");
        } catch (MalformedURLException ex) { ex.printStackTrace(); }
    }

    public void setPicture(ImageView picture) {
        this.picture = new WeakReference<>(picture);
    }

    @Override
    protected String doInBackground(Void... voids) {
        String result = null;

        if(type == -1) result = createOnlyText();
        else if(type == 0 && picture !=null) result = createTextAndPicture();
        else if(type == 1) result = createTextAndAudio();
        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Activity activity1 = activity.get();
        if(activity1==null || s==null) {

            if(activity1!=null)activity1.runOnUiThread(()-> Toast.makeText(activity1,"Errrrrrrrrrrrrrore",Toast.LENGTH_SHORT).show());
            Log.e(ERRORTAG,"(NewMessageTask-OnPostExecute)Errore nei puntatori");
        }
        else if(s.equals("1")) {
            activity1.runOnUiThread(()-> Toast.makeText(activity1,"Message left!",Toast.LENGTH_SHORT).show());
        }
        else if(s.equals("2")) {
            activity1.runOnUiThread(()-> Toast.makeText(activity1, "You mustn't leave a message at the same point", Toast.LENGTH_SHORT).show());
        }
        ProgressBar progressBar = progress.get();
        if(progressBar!=null) {
            progressBar.postDelayed(()-> progressBar.setVisibility(View.INVISIBLE),200);
        }
    }

    public String createOnlyText() {
        String data = "{\"request\": 3,\"id\": \""+idAccount+"\",\"description\" : \""+addSlashes(description)+"\",\"typeresource\" : "+type+",\"latitude\" : "+latitude+",\"longitude\":"+longitude+",\"resource\" :\"\"}";
        Log.d("REST",data);
        return restCall(data.getBytes(StandardCharsets.UTF_8));
    }
    public static String addSlashes(String s) {
        s = s.replaceAll("\\\\", "\\\\\\\\");
        s = s.replaceAll("\\n", "\\\\n");
        s = s.replaceAll("\\r", "\\\\r");
        s = s.replaceAll("\\00", "\\\\0");
        s = s.replaceAll("'", "\\\\'");
        return s;
    }
    private String createTextAndPicture() {

        Activity activity1 = activity.get();
        ImageView picture1 = picture.get();

        if(activity1==null || picture1==null) {
            Log.e(ERRORTAG,"(NewMessageTask-Text&Picture)Errore nei puntatori");
            return null;
        }

        BitmapDrawable drawable = (BitmapDrawable) picture1.getDrawable();
        Bitmap bitmap = drawable.getBitmap();

        try(ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Trasformo l'immagine in base64 per poterla mandare attraverso una chiamata rest
            // con input formattato in json
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100  , baos);
            byte[] byte_arr = baos.toByteArray();
            String base64 = Base64.encodeToString(byte_arr, 1);
            base64 = base64.replace("\n","");

            String data = "{\"request\": 3,\"id\": \""+idAccount+"\",\"description\" : \""+addSlashes(description)+"\",\"typeresource\" : "+type+",\"latitude\" : "+latitude+",\"longitude\":"+longitude+",\"resource\" :\""+base64+"\"}";
            Log.d("REST",data);
            return restCall(data.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e){
            activity1.runOnUiThread(()-> Toast.makeText(activity1,"Error during the execution",Toast.LENGTH_SHORT).show());
            Log.e(ERRORTAG,"Error during the execution2");
        }
        return null;
    }
    private String createTextAndAudio() {

        Activity activity1 = activity.get();
        if(activity1==null) return null;

        try {
            // Recupero il file audio come una sequenza di byte
            byte[] encoded = Base64.encode(Files.readAllBytes(Paths.get(activity1.getCacheDir()+"/attachment/Audio4Message.3gp")),1);
            // la trasformo in base64 sempre per mandare la richiesta sotto il formato json
            String base64 =new String(encoded, StandardCharsets.US_ASCII);
            base64  = base64.replace("\n","");

            String data = "{\"request\": 3,\"id\": \""+idAccount+"\",\"description\" : \""+addSlashes(description)+"\",\"typeresource\" : "+type+",\"latitude\" : "+latitude+",\"longitude\":"+longitude+",\"resource\" :\""+base64+"\"}";
            Log.d("REST",data);
            return restCall(data.getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            activity1.runOnUiThread(()-> Toast.makeText(activity1,"Error during the execution",Toast.LENGTH_SHORT).show());
            Log.e(ERRORTAG,"Error during the execution2");
        }
        return null;
    }

    private String restCall(byte[] out) {

        StringBuilder result = new StringBuilder();
        HttpURLConnection http;
        try {
            // Stabilisco una connessione
            http = (HttpURLConnection) urlGetMessage.openConnection();
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json");
        } catch (IOException e) {
            Log.e(ERRORTAG,"Errore nella openConnection()");
            return null;
        }

        try(OutputStream stream = http.getOutputStream()) {
            // scrivo la mia richiesta sullo stream
            stream.write(out);
            stream.flush();
        } catch (IOException e) {
            Log.e(ERRORTAG,"Errore in write()");
            return null;
        }

        // e mi leggo comodamente la risposta,siamo il buffered reader per efficienza
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
