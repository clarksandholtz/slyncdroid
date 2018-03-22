package com.get_slyncy.slyncy.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.apollographql.apollo.ApolloCall;
import com.apollographql.apollo.ApolloClient;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.get_slyncy.slyncy.Model.Util.ClientCommunicator;
import com.get_slyncy.slyncy.Model.Util.DownloadImageTask;
import com.get_slyncy.slyncy.R;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import apollographql.apollo.SignupMutation;

import static android.content.ContentValues.TAG;

/**
 * Created by undermark5 on 2/22/18.
 */

public class ConfirmationActivity extends Activity implements DownloadImageTask.PostExecCallBack
{
    private EditText nameField;
    private EditText phoneField;
    private EditText emailField;
    private EditText verificationField;
    private String name;
    private String phone;
    private String email;
    private GoogleSignInAccount acct;
    private Button confirmButton;
    private boolean mVerificationInProgress = false;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        Bundle bundle = getIntent().getExtras();

        Integer bool;

        if (bundle != null)
        {
            name = bundle.getString("name");
            phone = bundle.getString("phone");
            email = bundle.getString("email");
            acct = (GoogleSignInAccount) bundle.get("acct");
        }

        confirmButton = findViewById(R.id.continue_button);
        nameField = findViewById(R.id.name_field);
        phoneField = findViewById(R.id.phone_field);
        emailField = findViewById(R.id.email_field);
        verificationField = findViewById(R.id.verification_code);

        if (name != null && !name.isEmpty())
        {
            nameField.setText(name);
            nameField.setBackground(getDrawable(R.drawable.edit_text_text));
        }
        if (phone != null && !phone.isEmpty())
        {
            phoneField.setText(phone);
            phoneField.setBackground(getDrawable(R.drawable.edit_text_text));
        }
        else
        {
//            TelephonyManager tMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//            try
//            {
//                String num = tMgr.getLine1Number();
//                if (verifyPhone(num))
//                {
//                    phone = num;
//                    phoneField.setText(num);
//                    phoneField.setBackground(getDrawable(R.drawable.edit_text_text));
//                }
//            }
//            catch (SecurityException e)
//            {
//                Log.e("error", "onCreate: " + e.toString());
//            }
        }
        if (email != null && !email.isEmpty())
        {
            emailField.setText(email);
            emailField.setBackground(getDrawable(R.drawable.edit_text_text));
            if (verifyEmail(email))
                emailField.setEnabled(false);
        }

        if (name != null && !name.isEmpty() && email != null && !email.isEmpty() && phone != null && !phone.isEmpty())
        {
            if (verifyPhone(phone) && verifyEmail(email))
            {
                confirmButton.setEnabled(true);
            }
        }

        nameField.addTextChangedListener(new MyTextWatcher(nameField));
        emailField.addTextChangedListener(new MyTextWatcher(emailField, "email"));
        phoneField.addTextChangedListener(new MyTextWatcher(phoneField, "phone"));
    }

    private boolean verifyEmail(String email)
    {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean verifyPhone(String phone)
    {
        return Patterns.PHONE.matcher(phone).matches();
    }


    public void confirmInfo(View view)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        final FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithCredential(credential).addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                if (task.isSuccessful())
                {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signin", "signInWithCredential:success");
                    FirebaseUser user = auth.getCurrentUser();
                    mCallbacks = new MyPhoneVerificationCallBacks(user);
                    startPhoneNumberVerification(phoneField.getText().toString());
                }
                else
                {
                    // If sign in fails, display a message to the user.
                    Log.w("signin", "signInWithCredential:failure", task.getException());
                }
            }
        });
    }

    private void startPhoneNumberVerification(String phoneNumber)
    {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

        mVerificationInProgress = true;
    }

    public void callBack()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent = new Intent(this, SettingsActivity.class);
        if (user != null)
        {
            intent.putExtra("name", user.getDisplayName());
            intent.putExtra("email", user.getEmail());
            intent.putExtra("phone", user.getPhoneNumber());
            intent.putExtra("pic", user.getPhotoUrl().toString().replace("s96-c", "s960-c"));
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private class MyPhoneVerificationCallBacks extends PhoneAuthProvider.OnVerificationStateChangedCallbacks
    {

        FirebaseUser user;

        public MyPhoneVerificationCallBacks(FirebaseUser user)
        {
            this.user = user;
        }


        @Override
        public void onCodeAutoRetrievalTimeOut(String s)
        {
            super.onCodeAutoRetrievalTimeOut(s);
            emailField.setVisibility(View.GONE);
            findViewById(R.id.email_confirm_pad).setVisibility(View.GONE);
            findViewById(R.id.sms_confirm_pad).setVisibility(View.VISIBLE);
            verificationField.setVisibility(View.VISIBLE);
            verificationField.setError("Please enter your verification code");
            emailField.setEnabled(false);
            phoneField.setEnabled(false);
            nameField.setEnabled(false);
            verificationField.addTextChangedListener(new TextWatcher()
            {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after)
                {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count)
                {
                    confirmButton.setEnabled(verificationField.getText().toString().length() >= 6);
                    if (verificationField.getText().toString().length() >= 6)
                    {
                        verificationField.setError(null);
                    }
                    else
                    {
                        verificationField.setError("Please enter your verification code");
                    }
                }

                @Override
                public void afterTextChanged(Editable s)
                {
                }
            });

            confirmButton.setText("Verify");
            confirmButton.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Snackbar.make(v, "Manual verification still needs implementation", Snackbar.LENGTH_LONG);
                }
            });
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential)
        {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential);

            mVerificationInProgress = false;

            user.updateProfile(
                    new UserProfileChangeRequest.Builder().setDisplayName(nameField.getText().toString()).build());
            user.updatePhoneNumber(credential).addOnCompleteListener(new OnCompleteListener<Void>()
            {
                @Override
                public void onComplete(@NonNull Task<Void> task)
                {
                    if (task.isSuccessful())
                    {
                        Log.d(TAG, "onComplete: Phone number update successful");
                        ApolloClient client = ApolloClient.builder().serverUrl(LoginActivity.SERVER_URL).build();


                        client.mutate(SignupMutation.builder().email(user.getEmail()).name(user.getDisplayName())
                                .phone(user.getPhoneNumber()).uid(user.getUid()).build())
                                .enqueue(new ApolloCall.Callback<SignupMutation.Data>()
                                {
                                    @Override
                                    public void onResponse(@Nonnull Response<SignupMutation.Data> response)
                                    {
                                        if (!response.hasErrors())
                                        {
                                            if (response.data() != null)
                                            {
                                                ClientCommunicator.setAuthToken(response.data().signup().token());
                                            }
                                            if (!new File(getCacheDir() + "/profilePic.jpg").exists())
                                            {
                                                new DownloadImageTask(ConfirmationActivity.this.getCacheDir().getPath(),
                                                        ConfirmationActivity.this).execute(user.getPhotoUrl().toString()
                                                        .replace("s96-c", "s960-c"));
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFailure(@Nonnull ApolloException e)
                                    {

                                    }
                                });
                    }
                }
            });
        }

        @Override
        public void onVerificationFailed(FirebaseException e)
        {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.w(TAG, "onVerificationFailed", e);

            mVerificationInProgress = false;

            if (e instanceof FirebaseAuthInvalidCredentialsException)
            {
                // Invalid request
                verificationField.setError("Invalid verification code");
            }
            else if (e instanceof FirebaseTooManyRequestsException)
            {
                // The SMS quota for the project has been exceeded
                Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT).show();

            }
        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token)
        {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent:" + verificationId);

            // Save verification ID and resending token so we can use them later
            mVerificationId = verificationId;
            mResendToken = token;
        }
    }

    private class MyTextWatcher implements TextWatcher
    {
        private EditText field;
        private boolean hasNoTextBG = true;
        private String type = null;

        public MyTextWatcher(EditText field)
        {
            this.field = field;
        }

        public MyTextWatcher(EditText field, String type)
        {
            this.field = field;
            this.type = type;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after)
        {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count)
        {
            if (field.getText().toString().isEmpty())
            {
                field.setBackground(getDrawable(R.drawable.edit_text_no_text));
                hasNoTextBG = true;
            }
            else if (hasNoTextBG)
            {
                field.setBackground(getDrawable(R.drawable.edit_text_text));
                hasNoTextBG = false;
            }


        }

        @Override
        public void afterTextChanged(Editable s)
        {
            if (type != null)
            {
                switch (type)
                {
                    case "email":
                        if (!verifyEmail(field.getText().toString()))
                        {
                            field.setError("Invalid email address!");
                        }
                        else
                        {
                            field.setError(null);
                        }
                        break;
                    case "phone":
                        if (!verifyPhone(field.getText().toString()) && !field.getText().toString().isEmpty())
                        {
                            field.setError("Invalid phone number!");
                        }
                        else
                        {
                            field.setError(null);
                        }
                        break;
                }
            }
            if (!nameField.getText().toString().isEmpty() && !phoneField.getText().toString().isEmpty()
                && phoneField.getError() == null && !emailField.getText().toString().isEmpty()
                && emailField.getError() == null)
            {
                confirmButton.setEnabled(true);
            }
            else
            {
                confirmButton.setEnabled(false);
            }
        }
    }
}
