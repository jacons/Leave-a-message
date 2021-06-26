package project.leaveamessage.tabs;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.io.IOException;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static project.leaveamessage.utils.Utils.isConnected;

import project.leaveamessage.activities.FreeHandDrawing;
import project.leaveamessage.R;
import project.leaveamessage.background.NewMessageTask;
import project.leaveamessage.customView.PostItEditView;


public class LeaveMessage extends Fragment
        implements View.OnClickListener,MediaPlayer.OnPreparedListener,MediaPlayer.OnCompletionListener {

    public static final int PERMISSION_CODE = 1;

    private final String   myidAccount;   // (unique) id user
    private Activity       activity;      // current activity (may be null)
    private RelativeLayout postitcanvas;  // used for runtime-add view
    private RelativeLayout.LayoutParams pictureParam;
    private ProgressBar    progressBar;   // infinity progress during message loading
    private PostItEditView postit;        // for retrive description
    private boolean        islocated;     // true if we've gps coonates
    private boolean        isrecording ;  // true if we're recoring now
    private String         path;          // Audio path
    private TextView       cordinateText; // updated with every change
    private long           imgName = -1;  // timestamp for pic name file
    private MediaRecorder  mediaRecorder;
    private MediaPlayer    mediaPlayer;
    private int typeResource = -1;     // -1 solo testo, 0 Camera, 1 Audio
    private double latitude ,longitude;  // current latitude and longitude

    /*
     * They are pics overlapped with main view(post-it)
     * picture   -> pic loaded by user
     * recording -> we're recording now
     * recorered -> we've recordered (listener for play back)
     */
    private ImageView picture,recording,recorered;

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d("FRAGMENT","Coordinate ricevute");
            latitude = intent.getDoubleExtra("Latitude",-1);
            longitude = intent.getDoubleExtra("Longitude",-1);
            islocated = true;

            String text_lat = String.valueOf(latitude);
            if(text_lat.length()>10) text_lat = text_lat.substring(0,10);
            String text_lon = String.valueOf(longitude);
            if(text_lon.length()>10) text_lon = text_lon.substring(0,10);

            cordinateText.setText(getString(R.string.cordinate_ottenute)+text_lat +", "+text_lon);
        }
    };

    public LeaveMessage(String id)  {
        islocated = isrecording = false;
        path = null;
        myidAccount = id;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_leavemsg, container, false);

        activity= getActivity();
        if(activity==null) return null;

        File attachmentFolder = new File(activity.getCacheDir(), "attachment");
        if(!attachmentFolder.exists()) attachmentFolder.mkdir();

        pictureParam = new RelativeLayout.LayoutParams(250,250);
        pictureParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        pictureParam.addRule(RelativeLayout.ALIGN_LEFT);
        pictureParam.setMargins(80,0,0,150);

        postitcanvas = rootView.findViewById(R.id.postitcanvas);
        cordinateText =rootView.findViewById(R.id.display_cordinates);

        (rootView.findViewById(R.id.leavemessagebutton)).setOnClickListener(this);
        (rootView.findViewById(R.id.camera_content)).setOnClickListener(this);
        (rootView.findViewById(R.id.audio_conent)).setOnClickListener(this);
        (rootView.findViewById(R.id.freedraw_content)).setOnClickListener(this);

        progressBar =rootView.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.INVISIBLE);

        postit = rootView.findViewById(R.id.cartaQuadretti);
        return rootView;
    }
    @Override
    public void onResume() {
        super.onResume();
        requireActivity().registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(broadcastReceiver);
    }

    public void onClick(View view) {
        int id = view.getId();

        if(id == R.id.leavemessagebutton) {

            if(!islocated) {
                Toast.makeText(getContext(),"Ancora non ti ho localizzato!",Toast.LENGTH_SHORT).show();
                return;
            }
            if(isConnected(getContext()))  {
                // We've to internet connection
                Toast.makeText(getContext(),"Non riesco a collegarmi ad internet!",Toast.LENGTH_SHORT).show();
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            NewMessageTask messageTask = new NewMessageTask(getActivity(),progressBar,typeResource,postit.getText().toString(),myidAccount,latitude,longitude);
            if(typeResource==0) messageTask.setPicture(picture);
            messageTask.execute();

        }
        else if(id==R.id.camera_content && !isrecording)  {
           // I can't pick a photo during recording
            postitcanvas.removeView(recorered);
            resultActivity.launch(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI));
        }
        else if(id==R.id.audio_conent) { getRecording(); }
        else if(id==R.id.freedraw_content && !isrecording) getDraw();
        else if(id==recorered.getId()) {
            if(mediaPlayer==null) {
                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(path);
                    mediaPlayer.setVolume(1,1);
                    mediaPlayer.setOnPreparedListener(this);
                    mediaPlayer.setOnCompletionListener(this);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    Toast.makeText(activity,"Impossibile riprodurre l'audio",Toast.LENGTH_SHORT).show();
                    Log.e("ERROR", "prepare() failed");
                    mediaPlayer.release();
                    mediaPlayer = null;
                }

            } else mediaPlayer.prepareAsync();
            }
        }

    @Override
    public void onPrepared(MediaPlayer player) {
        mediaPlayer.start();
        Toast.makeText(getContext(), "Riproduzione in corso...", Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(getContext(), "Riproduzione terminata..", Toast.LENGTH_SHORT).show();
        mediaPlayer.stop();
    }

    public void getRecording() {
        // clean up
        postitcanvas.removeView(picture);

        if(!isrecording)  {
            // runtime permission
            if(CheckPermissions()) {

                // I define audio attachment
                typeResource = 1;
                postitcanvas.removeView(recorered);

                // Add "recording"
                recording = new ImageView(getContext());
                recording.setLayoutParams(pictureParam);
                recording.setImageResource(R.drawable.record_start);
                postitcanvas.addView(recording);

                isrecording = true;
                if(mediaRecorder==null) {
                    mediaRecorder = new MediaRecorder();
                    path = activity.getCacheDir()+"/attachment/audio_"+System.currentTimeMillis()+".3gp";
                }

                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mediaRecorder.setOutputFile(path);

                try {
                    mediaRecorder.prepare();
                    mediaRecorder.start();
                } catch (IOException e) {
                    Toast.makeText(getContext(), "Errore nella ripresa audio", Toast.LENGTH_LONG).show();
                    mediaRecorder.release();
                    mediaRecorder = null;
                }

                Toast.makeText(getContext(), "Parla pure!", Toast.LENGTH_LONG).show();

            } else  { RequestPermissions(); }
        } else {
            isrecording = false;
            // remove "Sto registrando"
            postitcanvas.removeView(recording);

            recorered= new ImageView(getContext());
            recorered.setLayoutParams(pictureParam);
            recorered.setImageResource(R.drawable.sound_bars);
            recorered.setOnClickListener(this);

            postitcanvas.addView(recorered);
            mediaRecorder.stop();
        }
    }

    public void getDraw(){
        if(imgName ==-1) imgName = System.currentTimeMillis();

        resultActivity.launch(new Intent(getContext(),FreeHandDrawing.class)
                                .putExtra("name_file", imgName));
    }

    public boolean CheckPermissions() {
        int result = activity.checkSelfPermission(WRITE_EXTERNAL_STORAGE);
        int result1 = activity.checkSelfPermission(RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    private void RequestPermissions() {
        Activity activity = getActivity();
        if(activity==null) return;
        activity.requestPermissions(new String[]{RECORD_AUDIO, WRITE_EXTERNAL_STORAGE}, PERMISSION_CODE);
    }

    final ActivityResultLauncher<Intent> resultActivity = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {

                if(result.getData()!=null) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        try {
                            Bitmap  bitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), result.getData().getData());
                            postitcanvas.removeView(picture);

                            picture = new ImageView(getContext());
                            picture.setLayoutParams(pictureParam);
                            picture.setImageBitmap(bitmap);
                            picture.setRotation(-7);
                            postitcanvas.addView(picture);

                            // I define pic attachment
                            typeResource = 0;
                        } catch (IOException e) {
                            Toast.makeText(activity,"Errore nel recupero dell'immagine",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(result.getResultCode() == 85) {

                        File drawing = new File(activity.getCacheDir()+"/attachment/draw_"+imgName+".jpg");

                        if(drawing.exists()){
                            postitcanvas.removeView(recorered);
                            postitcanvas.removeView(this.picture);

                            this.picture = new ImageView(getContext());
                            this.picture.setLayoutParams(pictureParam);

                            Bitmap bitmap = BitmapFactory.decodeFile(drawing.getAbsolutePath());
                            this.picture.setImageBitmap(bitmap);
                            this.picture.setRotation(-7);
                            postitcanvas.addView(this.picture);
                            typeResource = 0;
                        } else {
                            Toast.makeText(activity,"Errore nel recupero dell'immagine",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if(result.getResultCode() == 86) {
                        Toast.makeText(getContext(), "Errore nella creazione dell'immagine", Toast.LENGTH_LONG).show();
                    }
                }
            });
}