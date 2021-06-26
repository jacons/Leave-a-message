package project.leaveamessage.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import project.leaveamessage.R;
import project.leaveamessage.background.ProvideMessageTask;
import project.leaveamessage.interfaces.MessageDoneInterface;


public class ViewSingeMessage extends AppCompatActivity implements MessageDoneInterface{

    private ProgressBar bar;        // Infinity bar for loading
    private GridLayout gridLayout;
    private TextView textDescription,description;
    private ImageView picture;     /* L'imageView conterrà o l'immagine scaricata o, un tasto
                                      per la riproduzione audio. */

    private MediaPlayer mediaPlayer; // Eventualmente usato per la riproduzione audio.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_singe_message);

        int id = getIntent().getIntExtra("idMessage",-1);

        TextView user          = findViewById(R.id.userName_text);
        TextView scoperta      = findViewById(R.id.data_scoperta_text);
        TextView pubblicazione = findViewById(R.id.data_pubblicazione_text);
        TextView latitudine    = findViewById(R.id.latitudine_text);
        TextView longitudine   = findViewById(R.id.longitudine_text);
        picture                = findViewById(R.id.message_picture);
        description            = findViewById(R.id.descrizione_text);
        bar                    = findViewById(R.id.progressbar);
        gridLayout             = findViewById(R.id.gridLayout);
        textDescription        = findViewById(R.id.descrizione);

        //Rendo visibile solo la barra di caricamento,tutto il resto no fino alla fine
        // del caricamento.
        bar.setVisibility(View.VISIBLE);
        gridLayout.setVisibility(View.INVISIBLE);
        textDescription.setVisibility(View.INVISIBLE);
        description.setVisibility(View.INVISIBLE);

        // Faccio partire un task che mi recupera i dati dal database,e mi va
        // a scaricare l'immagine da internet
        ProvideMessageTask task = new ProvideMessageTask(this,this, picture,id);
        task.execute(user,scoperta,pubblicazione,latitudine,longitudine, description);
    }

    // Callback che viene chiamata dal task quando ha finito la sua esecuzione
    // consiste nel visualizzare tutte le view con le informazioni
    @Override
    public void showResults() {
        bar.setVisibility(View.INVISIBLE);
        gridLayout.setVisibility(View.VISIBLE);
        textDescription.setVisibility(View.VISIBLE);
        description.setVisibility(View.VISIBLE);
    }

    // Callback che viene chiamata dal task per settare l'evento di riproduzio audio.
    @Override
    public void setListenerForAudio(String hash) {
        picture.setOnClickListener(v -> {
            mediaPlayer = new MediaPlayer();
            try {
                String path = getCacheDir()+"/cache_audio/"+hash+".3gp";

                if(!new File(path).exists()) throw new IOException();
                mediaPlayer.setVolume(1,1);
                mediaPlayer.setDataSource(path);
                mediaPlayer.prepare();
                mediaPlayer.start();
                Toast.makeText(this, "Riproduzione in corso...", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(this,"Impossibile riprodurre l'audio",Toast.LENGTH_SHORT).show();
            }
            mediaPlayer=null; // errore?
        });
    }
}