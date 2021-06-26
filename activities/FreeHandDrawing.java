package project.leaveamessage.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import project.leaveamessage.R;
import project.leaveamessage.customView.DrawView;

import static project.leaveamessage.utils.Utils.ERRORTAG;

/** This activity is used for creating a drawing with fingers */
public class FreeHandDrawing extends AppCompatActivity implements View.OnClickListener {

    // Custom view
    private DrawView drawView;
    private Long tmpname;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_free_hand_drawing);

        Intent intent = getIntent();
        tmpname = intent.getLongExtra("name_file",-1);
        if(tmpname==-1) finish();
        // the name of file, it's different foreach "launch", allow to create multiple istance

        drawView = new DrawView(this);
        drawView.setDrawingCacheEnabled(true);

        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        linearLayout.setPadding(10,10,10,10);
        linearLayout.addView(drawView);

        // Used for change paint color
        findViewById(R.id.set_red).setOnClickListener(this);
        findViewById(R.id.set_green).setOnClickListener(this);
        findViewById(R.id.set_blue).setOnClickListener(this);
        findViewById(R.id.set_clear).setOnClickListener(this);
        findViewById(R.id.set_done).setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.set_red)         { drawView.updatePaint(Color.rgb(255,0,0)); }
        else if(id == R.id.set_green)  { drawView.updatePaint(Color.rgb(0,255,0)); }
        else if(id == R.id.set_blue)   { drawView.updatePaint(Color.rgb(0,0,255)); }
        else if (id == R.id.set_clear) { drawView.clear(); }

        else if (id == R.id.set_done) {

            Bitmap bitmap = drawView.getDrawingCache();

            File attachmentFolder = new File(getCacheDir(), "attachment");
            if(!attachmentFolder.exists()) if(!attachmentFolder.mkdir()) returnError();

            File file = new File(getCacheDir()+"/attachment/draw_"+tmpname+".jpg");
            try(FileOutputStream ostream = new FileOutputStream(file))  {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 30, ostream);
                drawView.invalidate();
            } catch (IOException e) {
                Log.e(ERRORTAG,e.getMessage());
                returnError();
            }
            finally { drawView.setDrawingCacheEnabled(false); }

            setResult(85,new Intent());
            finish();
        }
    }
    public void returnError() {

        setResult(86,new Intent());
        finish();
    }
}