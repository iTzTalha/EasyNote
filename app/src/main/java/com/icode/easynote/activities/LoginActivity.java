package com.icode.easynote.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.icode.easynote.R;
import com.icode.easynote.database.NoteDatabase;

public class LoginActivity extends AppCompatActivity {

    ProgressBar progressBar;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 0;
    SharedPreferences sharedPreferences;

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);

        if (sharedPreferences.getBoolean("LOGGED_IN", false)) {
            toMainActivity();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        progressBar = findViewById(R.id.progress_circular);
        sharedPreferences = getSharedPreferences("PREF", MODE_PRIVATE);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        RelativeLayout g_login = findViewById(R.id.btn_loginGoogle);
        g_login.setOnClickListener(v -> signIn());

//        Button login = findViewById(R.id.btn_login);
//        login.setOnClickListener(v -> {
//            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(LoginActivity.this, R.style.BottomSheetDialogTheme);
//            View view = LayoutInflater.from(LoginActivity.this).inflate(R.layout.layout_bottom_sheet_login, (LinearLayout) findViewById(R.id.layout_bottom_sheet_login));
//            view.findViewById(R.id.btn_continue).setOnClickListener(vv -> {
//                EditText inputName = view.findViewById(R.id.input_name);
//                String name = inputName.getText().toString().trim();
//                if (name.isEmpty()) {
//                    inputName.setError("Name can't be empty!");
//                } else {
//                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("NAME", name);
//                    editor.putBoolean("LOGGED_IN", true);
//                    editor.apply();
//                    bottomSheetDialog.dismiss();
//                    startActivity(intent);
//                    finish();
//                }
//            });
//            view.findViewById(R.id.btn_cancel).setOnClickListener(vv -> bottomSheetDialog.dismiss());
//            bottomSheetDialog.setContentView(view);
//            bottomSheetDialog.show();
//        });
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account != null) {
            toMainActivity();
        }
    }

    void toMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void signIn() {
        progressBar.setVisibility(View.VISIBLE);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
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

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            progressBar.setVisibility(View.GONE);
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("LOGIN ERROR", "signInResult:failed code=" + e.getStatusCode());
            progressBar.setVisibility(View.GONE);
            updateUI(null);
        }
    }
}