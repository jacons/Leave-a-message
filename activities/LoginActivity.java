package project.leaveamessage.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import project.leaveamessage.R;
import project.leaveamessage.background.NewUserTask;

/**
 * For login system I used this guide
 * https://developers.google.com/android/reference/com/google/android/gms/auth/api/signin/GoogleSignInClient
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int CODE_SIGNIN = 782;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onStart() {
        super.onStart();

        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        loginDone(account);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Set the dimensions of the sign-in button.
        SignInButton signInButton = findViewById(R.id.sign_in_button);

        signInButton.setSize(SignInButton.SIZE_WIDE);
        signInButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sign_in_button) { signIn(); }
    }
    private void signIn() {
        // Despite is deprecated
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), CODE_SIGNIN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == CODE_SIGNIN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            if(account!=null) {
                //Effettua una chiamata rest per andare a verificare se l'utente è nuovo
                //in tal caso lo aggiunge a gli utenti della piattaforma.
                new NewUserTask().execute(account.getGivenName(),account.getFamilyName(),account.getId());
            }
            loginDone(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(this,"Failed Authentication "+ e.getStatusCode(),Toast.LENGTH_SHORT).show();
            Log.w("PROVA AUTHENTICATOR", "signInResult:failed code=" + e.getStatusCode());
            loginDone(null);
        }
    }

    private void loginDone(GoogleSignInAccount account) {
        if(account!=null) {
            // If user is already authenticated then it launches the main activity
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}