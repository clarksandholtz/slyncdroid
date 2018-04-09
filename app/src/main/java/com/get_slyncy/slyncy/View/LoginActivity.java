package com.get_slyncy.slyncy.View;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
import com.get_slyncy.slyncy.Model.Util.Data;
import com.get_slyncy.slyncy.Model.Util.DownloadImageTask;
import com.get_slyncy.slyncy.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import javax.annotation.Nonnull;

import apollographql.apollo.LoginMutation;


public class LoginActivity extends Activity implements DownloadImageTask.PostExecCallBack
{

    public static final String SERVER_URL = "http://192.168.254.171:4000/";
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseApp.initializeApp(getApplicationContext());

        // Check if permissions are needed
        if (PermissionActivity.needPermissionRequest(this))
        {
            startActivity(new Intent(this, PermissionActivity.class));
            finish();
            return;
        }

        // Init settings
        Data.getInstance().updateCellSettings(this);

        // Setup notification channel
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O)
        {
            NotificationChannel channel = new NotificationChannel(getPackageName() + "notif"
                    , "Basic Notifications", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null)
            {
                notificationManager.createNotificationChannel(channel);
            }
        }
        if (getSharedPreferences("authorization", MODE_PRIVATE).contains("token"))
        {
            //need to keep the email name and phone number around.
            SharedPreferences prefs = getSharedPreferences("authorization", MODE_PRIVATE);
            String email = prefs.getString("email", "");
            String name = prefs.getString("name", "");
            String phone = prefs.getString("phone", "");
            String picUrl = prefs.getString("pic", null);
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("email", email);
            intent.putExtra("name", name);
            intent.putExtra("phone", phone);
            intent.putExtra("pic", picUrl);
            intent.putExtra("sync", false);
            startActivity(intent);
            finish();
            return;
        }
        ImageView logo = findViewById(R.id.slyncy_logo);
        logo.setImageDrawable(getDrawable(R.drawable.ic_logo_white));
//        logo.getLayoutParams().width = 1000;



        Button button = findViewById(R.id.sign_in_button);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signIn();
            }
        });

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mAuth = FirebaseAuth.getInstance();
    }


    @Override
    public void onStart()
    {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
//        FirebaseUser currentUser = mAuth.getCurrentUser();
//        updateUI(currentUser);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try
            {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            }
            catch (ApiException e)
            {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    public void callBack()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
        if (user != null)
        {
            intent.putExtra("name", user.getDisplayName());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("phone", user.getPhoneNumber());
            intent.putExtra("pic", user.getPhotoUrl().toString());
            intent.putExtra("sync", true);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct)
    {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            tryLogin(user, acct);
                        }
                        else
                        {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.main_layout), "Authentication Failed.",
                                    Snackbar.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
    }


    private void tryLogin(final FirebaseUser user, final GoogleSignInAccount acct)
    {
        ApolloClient client = ApolloClient.builder().serverUrl(SERVER_URL).build();

        client.mutate(LoginMutation.builder().email(user.getEmail()).uid(user.getUid()).build())
                .enqueue(new ApolloCall.Callback<LoginMutation.Data>()
                {
                    @Override
                    public void onResponse(@Nonnull Response<LoginMutation.Data> response)
                    {
                        if (response.hasErrors())
                        {
                            Intent intent = new Intent(LoginActivity.this, ConfirmationActivity.class);
                            intent.putExtra("name", user.getDisplayName());
                            intent.putExtra("email", user.getEmail());
                            intent.putExtra("phone", user.getPhoneNumber());
                            intent.putExtra("acct", acct);
                            intent.putExtra("emailCred", user);
                            intent.putExtra("pic", user.getPhotoUrl().toString().replace("s96-c", "s960-c"));

                            Log.d("UID:", user.getUid());
                            mAuth.signOut();

                            startActivity(intent);
                        }
                        else
                        {

                            SharedPreferences sharedPreferences = getSharedPreferences("authorization", MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("email", user.getEmail());
                            editor.putString("phone", user.getPhoneNumber());
                            editor.putString("name", user.getDisplayName());
                            editor.putString("pic", user.getPhotoUrl() == null ? null : user.getPhotoUrl().toString().replace("s96-c", "s960-c"));
                            editor.commit();
                            if (response.data() != null)
                            {
                                ClientCommunicator.persistAuthToken(response.data().login().token(), getApplicationContext());
                            }
                            new DownloadImageTask(LoginActivity.this.getCacheDir().getPath(),
                                    LoginActivity.this).execute(user.getPhotoUrl().toString()
                                    .replace("s96-c", "s960-c"));
                        }
                    }

                    @Override
                    public void onFailure(@Nonnull ApolloException e)
                    {
                        Log.e("Apollo error:", e.getMessage());
                        Snackbar.make(findViewById(R.id.main_layout), "Unable to reach Slyncy Servers. Please try again later.",
                                Snackbar.LENGTH_SHORT).show();


                        Intent intent = new Intent(LoginActivity.this, ConfirmationActivity.class);
                        intent.putExtra("name", user.getDisplayName());
                        intent.putExtra("email", user.getEmail());
                        intent.putExtra("phone", user.getPhoneNumber());
                        intent.putExtra("acct", acct);
                        intent.putExtra("emailCred", user);
                        intent.putExtra("pic", user.getPhotoUrl().toString().replace("s96-c", "s960-c"));

                        Log.d("UID:", user.getUid());
                        mAuth.signOut();

                        startActivity(intent);
/**     code below will allow you into settings without connecting to slyncy's server */
                        /**
                        new DownloadImageTask(LoginActivity.this.getCacheDir().getPath(),
                                LoginActivity.this).execute(user.getPhotoUrl().toString()
                                .replace("s96-c", "s960-c"));
                                //*/
                    }
                });
    }

    public void signIn()
    {
        Log.d("TEST", "signIn: TEST");
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        signInIntent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void updateUI(FirebaseUser user)
    {
        if (user != null)
        {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photo = user.getPhotoUrl().toString().replace("s96-c", "s960-c");
            String phone = user.getPhoneNumber();
            Log.d("name", name);
            Log.d("email", email);
            Log.d("phone", phone == null || phone.equals("") ? " " : phone);
            Log.d("photo", photo);
            Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
            intent.putExtra("name", user.getDisplayName());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("phone", user.getPhoneNumber());
            intent.putExtra("pic", user.getPhotoUrl().toString().replace("s96-c", "s960-c"));
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK
                            | Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(intent);
        }
    }
}