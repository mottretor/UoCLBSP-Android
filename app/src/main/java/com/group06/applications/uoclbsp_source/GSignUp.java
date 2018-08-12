package com.group06.applications.uoclbsp_source;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class GSignUp extends AppCompatActivity implements View.OnClickListener {
    GoogleSignInClient mGoogleSignInClient;
    final int RC_SIGN_IN = 100;
    String personName;
    String personPhoto;
    String email;
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gsign_up);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
// the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    private void updateUI(GoogleSignInAccount account) {

        if(account !=null){
            System.out.println("Done");
            personName = account.getDisplayName();
            if(account.getPhotoUrl() !=null){
                personPhoto = account.getPhotoUrl().toString();
            }

            email = account.getEmail();

//            setProfileInfo();
            System.out.println("email"+email+" name "+personName+" url "+personPhoto);
            Intent intent = new Intent(this,MapsActivity.class);
            intent.putExtra("personName", personName);
            intent.putExtra("personPhoto", personPhoto);
            intent.putExtra("email", email);
            startActivity(intent);
            finish();
        }



    }


    @Override
    public void onClick(View view) {
//        Intent intent = new Intent(this,MapsActivity.class );
//        startActivity(intent);
//        finish();

        switch (view.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            // ...
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);

        }
    }

    public void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.i("Tag", "display name: " + account.getDisplayName());

            // Signed in successfully, show authenticated UI.
            updateUI(account);
//            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(this);
//        if (acct != null) {
//            String personName = acct.getDisplayName();
//            String personGivenName = acct.getGivenName();
//            String personFamilyName = acct.getFamilyName();
//            String personEmail = acct.getEmail();
//            String personId = acct.getId();
//            Uri personPhoto = acct.getPhotoUrl();
//
//            System.out.println(personName);
//            System.out.println(personEmail);
//
//        }


        } catch (ApiException e) {
            e.printStackTrace();
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
//            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
            System.out.println("nooooo");
        }
    }

//    GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getActivity());
//    if (acct != null) {
//        String personName = acct.getDisplayName();
//        String personGivenName = acct.getGivenName();
//        String personFamilyName = acct.getFamilyName();
//        String personEmail = acct.getEmail();
//        String personId = acct.getId();
//        Uri personPhoto = acct.getPhotoUrl();
//    }

//    public void setProfileInfo(){
//        TextView textView= findViewById(R.id.profile_name);
//        textView.setText(personName);
//    }

}
