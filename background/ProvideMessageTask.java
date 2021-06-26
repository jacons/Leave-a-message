package project.leaveamessage.background;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import project.leaveamessage.R;
import project.leaveamessage.interfaces.MessageDoneInterface;
import project.leaveamessage.roomdb.Message;
import project.leaveamessage.roomdb.MessagesDatabase;

import static project.leaveamessage.utils.Utils.ERRORTAG;

public class ProvideMessageTask extends AsyncTask<TextView,Void,Drawable> {

    private final WeakReference<Activity> activity;
    private final WeakReference<MessageDoneInterface> callbackRef;
    private final WeakReference<ImageView> pictureMain; // Picture that will be sent
    private final int idMessage; // id message for retriving information

    private String type,hash;

    public ProvideMessageTask(MessageDoneInterface intf, Activity activity, ImageView pictureMain, int idMessage) {
        this.pictureMain = new WeakReference<>(pictureMain);
        this.callbackRef = new WeakReference<>(intf);
        this.activity = new WeakReference<>(activity);

        this.idMessage = idMessage;
    }
    @Override
    protected Drawable doInBackground(TextView... textViews) {

        Activity activity1 = activity.get();
        if(activity1==null) {
            Log.e(ERRORTAG,"(ProvideTask-doInBackground)Errore nei puntatori");
            return null;
        }

        MessagesDatabase db = MessagesDatabase.getDbInstance(activity1);
        Message msg = db.messageDao().getMessageById(idMessage);

        activity1.runOnUiThread(()-> {
            textViews[0].setText(msg.name+" "+msg.surname);
            textViews[1].setText(msg.discovered);
            textViews[2].setText(msg.published);
            textViews[3].setText(msg.latitude.substring(0,10));
            textViews[4].setText(msg.longitude.substring(0,10));
            textViews[5].setText(msg.description);
        });

        type = msg.type;
        hash = msg.resource;

        Drawable result = null ;

        if(type.equals("1"))  result = getAudio();
        else if(type.equals("0"))  result = getPicture();

        return result;
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);

        MessageDoneInterface ref = callbackRef.get();
        Activity activity1 = activity.get();
        ImageView content1 = pictureMain.get();

        if(ref==null || activity1==null || content1==null) {
            Log.e(ERRORTAG,"(ProvideTask-OnPostExcecute)Errore nei puntatori");
            return;
        }
        ref.showResults();

        // may be null, because there isn't any picture
        if(drawable==null) return;

        if(type.equals("0")){
            content1.setImageDrawable(drawable);
        }
        else if(type.equals("1")) {
            content1.setImageDrawable(drawable);
            ref.setListenerForAudio(hash);
        }
    }

    public Drawable getPicture() {

        Activity activity1 = activity.get();
        if(activity1==null) {
            Log.e(ERRORTAG,"(ProvideTask-getPicture)Errore nei puntatori");
            return null;
        }
        File cache_img = new File(activity1.getCacheDir(), "cache_img");
        if(!cache_img.exists()) if(!cache_img.mkdir()) {
            activity1.runOnUiThread(()-> Toast.makeText(activity1, "Error in mkdir", Toast.LENGTH_LONG).show());
            return  null;
        }
        File imgFile = new File(activity1.getCacheDir()+"/cache_img/"+hash+".jpg");

        // means that I've downloaded alreay
        if(!imgFile.exists()) {
            HttpURLConnection connection;
            try {
                URL url = new URL("xxx/"+hash+".jpg");
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
            } catch (IOException e) {
                activity1.runOnUiThread(()-> Toast.makeText(activity1, "Error during download", Toast.LENGTH_LONG).show());
                return null;
            }
            byte[] data = new byte[2048];
            int count;
            try(BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());

                OutputStream output = new FileOutputStream(imgFile)) {
                while ((count = inputStream.read(data)) != -1)  output.write(data, 0, count);
                output.flush();
            } catch (IOException e) {
                activity1.runOnUiThread(()-> Toast.makeText(activity1, "Error during download", Toast.LENGTH_LONG).show());
                return  null;
            }
        }
        return  Drawable.createFromPath(imgFile.getPath());

    }
    public Drawable getAudio() {

        // in questo caso restituisco un mio drawable che rappresenta il pulsate per eseguire l'audio
        Activity activity1 = activity.get();
        ImageView imageView1 = pictureMain.get();
        if(activity1==null|| imageView1==null) return null;

        // come sopra
        File cache_audio = new File(activity1.getCacheDir(), "cache_audio");
        if(!cache_audio.exists()) if(!cache_audio.mkdir()) {
            activity1.runOnUiThread(()-> Toast.makeText(activity1, "Error in mkdir", Toast.LENGTH_LONG).show());
            return  null;
        }

        File audioFile = new File(activity1.getCacheDir()+"/cache_audio/"+hash+".3gp");
        if(!audioFile.exists()) {
            HttpURLConnection connection;
            try {
                URL url = new URL("xxx/"+hash+".3gp");
                Log.d("url",url.toString());
                connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
            } catch (IOException e) {
                activity1.runOnUiThread(()-> Toast.makeText(activity1, "Error during download", Toast.LENGTH_LONG).show());
                return null;
            }

            byte[] data = new byte[1024];
            int count;
            try(BufferedInputStream inputStream = new BufferedInputStream(connection.getInputStream());

                OutputStream output = new FileOutputStream(audioFile)) {

                while ((count = inputStream.read(data)) != -1)  output.write(data, 0, count);
                output.flush();
            } catch (IOException e) {
                activity1.runOnUiThread(()-> Toast.makeText(activity1, "Error during download", Toast.LENGTH_LONG).show());
            }
        }
        return activity1.getDrawable(R.drawable.sound_bars);
    }
}
