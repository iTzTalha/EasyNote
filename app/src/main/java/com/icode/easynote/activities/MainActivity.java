package com.icode.easynote.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.icode.easynote.adapters.NoteAdapter;
import com.icode.easynote.database.NoteDatabase;
import com.icode.easynote.fragments.AddNoteFragment;
import com.icode.easynote.R;
import com.icode.easynote.listeners.INoteFragmentListener;
import com.icode.easynote.listeners.INoteListener;
import com.icode.easynote.models.Note;

import java.util.Collections;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity implements INoteFragmentListener, INoteListener {

    private static final int REQUEST_CODE_SELECT_IMAGE = 1;
    private static final int REQUEST_CODE_EXTERNAL_PERMISSION = 2;
    SharedPreferences sharedPreferences;

    TextView myName;
    CircleImageView myPhoto;
    FloatingActionButton addNote;
    LinearLayout layoutProfile;
    EditText inputSearch;

    RecyclerView recyclerView;
    List<Note> notes;
    NoteAdapter noteAdapter;

    //Quick Actions
    ImageView imageAddNote, imageAddImage, imageAddLink;
    private AlertDialog alertDialogUrl;
    private String url;

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        updateUI(account);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("PREF", MODE_PRIVATE);
        initViews();
        url = "";
        fillRecyclerView();
        addNote.setOnClickListener(v -> openNoteFragment(null, null));
        layoutProfile.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomSheetDialogTheme);
            View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.layout_bottom_sheet_profile, (LinearLayout) findViewById(R.id.layout_bottom_sheet_profile_container));
            view.findViewById(R.id.layoutLogout).setOnClickListener(vv -> {
                bottomSheetDialog.dismiss();
                if (sharedPreferences.getBoolean("LOGGED_IN", false)) {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("LOGGED_IN", false);
                    editor.apply();
                    toLoginActivity();
                } else {
                    signOut();
                }
            });
            view.findViewById(R.id.layoutCancel).setOnClickListener(vv -> bottomSheetDialog.dismiss());
            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.show();
        });

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                noteAdapter.cancelTimer();
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (notes.size() != 0) {
                    noteAdapter.searchNotes(s.toString());
                }
            }
        });

        //Quick Actions
        imageAddNote.setOnClickListener(v -> openNoteFragment(null, null));
        imageAddImage.setOnClickListener(v -> selectImage());
        imageAddLink.setOnClickListener(v -> showUrlDialog());
    }

    void initViews() {
        myName = findViewById(R.id.textMyName);
        myPhoto = findViewById(R.id.imagePhoto);
        addNote = findViewById(R.id.floatingAction_AddNote);
        layoutProfile = findViewById(R.id.layoutProfile);
        inputSearch = findViewById(R.id.inputSearch);
        recyclerView = findViewById(R.id.recyclerViewNotes);

        //Quick actions
        imageAddNote = findViewById(R.id.imageAddNote);
        imageAddImage = findViewById(R.id.imageAddImage);
        imageAddLink = findViewById(R.id.imageAddLink);
    }

    private void updateUI(GoogleSignInAccount account) {
        if (account == null && !sharedPreferences.getBoolean("LOGGED_IN", false)) {
            toLoginActivity();
            finish();
        }

        if (sharedPreferences.getBoolean("LOGGED_IN", false)) {
            myName.setText(sharedPreferences.getString("NAME", ""));
            return;
        }

        String personName = account.getDisplayName();
//        String personGivenName = account.getGivenName();
//        String personFamilyName = account.getFamilyName();
//        String personEmail = account.getEmail();
//        String personId = account.getId();
        Uri personPhoto = account.getPhotoUrl();

        myName.setText(personName);
        Glide.with(this).load(String.valueOf(personPhoto)).into(myPhoto);
    }

    private void toLoginActivity() {
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        finish();
    }

    private void signOut() {
        GoogleSignInClient mGoogleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                        toLoginActivity();
                        Snackbar.make(MainActivity.this, findViewById(android.R.id.content), "Signed out Successfully", Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    List<Note> getNotes() {
        NoteDatabase db = new NoteDatabase(MainActivity.this);
        notes = db.getNotes();
        Collections.reverse(notes);
        return notes;
    }

    public void fillRecyclerView() {
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        noteAdapter = new NoteAdapter(getNotes(), this);
        recyclerView.setAdapter(noteAdapter);
        noteAdapter.notifyDataSetChanged();
    }

    private void openNoteFragment(Note note, String action) {
        AddNoteFragment dialogFragment = AddNoteFragment.Instance();
        if (note != null && action.equalsIgnoreCase("update")) {
            Bundle args = new Bundle();
            args.putBoolean("IS_UPDATE", true);
            args.putSerializable("NOTE", note);
            dialogFragment.setArguments(args);
        } else if (note != null && action.equalsIgnoreCase("action")) {
            Bundle args = new Bundle();
            args.putBoolean("IS_ACTION", true);
            args.putSerializable("NOTE", note);
            dialogFragment.setArguments(args);
        }
        dialogFragment.show(getSupportFragmentManager(), "add_note_dialog");
    }

    private boolean requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_EXTERNAL_PERMISSION);
            return false;
        }
        return true;
    }

    private void selectImage() {
        if (requestExternalStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(this.getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        String filePath;
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            filePath = uri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }

        return filePath;
    }

    @Override
    public void onNoteInserted() {
        fillRecyclerView();
    }

    @Override
    public void onNoteDeleted() {
        fillRecyclerView();
    }

    private void showUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_add_url, (LinearLayout) findViewById(R.id.layout_dialog_container));
        builder.setView(view);

        alertDialogUrl = builder.create();
        if (alertDialogUrl.getWindow() != null) {
            alertDialogUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        EditText inputUrl = view.findViewById(R.id.inputURL);
        inputUrl.requestFocus();

        view.findViewById(R.id.textADD).setOnClickListener(v -> {
            if (inputUrl.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Enter URL", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString().trim()).matches()) {
                Toast.makeText(this, "Enter valid URL", Toast.LENGTH_SHORT).show();
            } else {
                url = inputUrl.getText().toString().trim();
                Note note = new Note(-1, "", "", "", "", "", "", url);
                openNoteFragment(note, "action");
                alertDialogUrl.dismiss();

            }
        });
        view.findViewById(R.id.textCANCEL).setOnClickListener(v -> alertDialogUrl.dismiss());
        alertDialogUrl.show();
    }

    @Override
    public void onNoteClick(Note note, int position) {
        openNoteFragment(note, "update");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    Note note = new Note(-1, "", "", "", "", "", getPathFromUri(selectedImageUri), "");
                    openNoteFragment(note, "action");
                }
            }
        }
    }
}