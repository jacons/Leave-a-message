package project.leaveamessage.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import project.leaveamessage.R;
import project.leaveamessage.adapter.TabsAdapter;
import project.leaveamessage.background.TrackerLocationService;

public class  MainActivity extends AppCompatActivity {


    private GoogleSignInClient googleClient;
    private GoogleSignInAccount myaccount; // Google account

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Again
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        googleClient = GoogleSignIn.getClient(this, gso);
        myaccount = GoogleSignIn.getLastSignedInAccount(this);


        TabsAdapter tabsAdapter = new TabsAdapter(this, getSupportFragmentManager(), myaccount.getId());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(tabsAdapter);

        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //runtime permission
        runtime_permissions();
    }

    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        myaccount = GoogleSignIn.getLastSignedInAccount(this);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.logout) {
            stopService(new Intent(getApplicationContext(),TrackerLocationService.class));
            signOut();
        }
        if(id == R.id.startService) {
            Intent intent = new Intent(getApplicationContext(), TrackerLocationService.class);
            intent.putExtra("userid",myaccount.getId());
            if(startForegroundService(intent)==null) {
                Toast.makeText(this,"Service don't working",Toast.LENGTH_SHORT   ).show();
            }
        }
        if(id == R.id.stopSerivce) {
            stopService(new Intent(getApplicationContext(),TrackerLocationService.class));
        }
        return super.onOptionsItemSelected(item);
    }


    // if user denies permissions then show dialog and finish()
    private void errorPermission() {
        new AlertDialog.Builder(this)
                .setTitle("Damn!").setMessage("I cannot use gps sensor!")
                .setPositiveButton(android.R.string.ok, null).show();
    }

    // runtime permission
    private boolean runtime_permissions() {
        if(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);
            return false;
        }
        return true;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] != PackageManager.PERMISSION_GRANTED || grantResults[1] != PackageManager.PERMISSION_GRANTED){
                if(runtime_permissions()) errorPermission();
            }
        }
    }
    private void signOut() {
        googleClient.signOut().addOnCompleteListener(this, task -> finish());
    }
}