package com.icode.easynote.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;

import com.icode.easynote.R;
import com.icode.easynote.database.NoteDatabase;
import com.icode.easynote.listeners.INoteFragmentListener;
import com.icode.easynote.models.Note;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class AddNoteFragment extends DialogFragment {

    ImageView back, done, imageNote;
    EditText title, subtitle, note;
    TextView date;

    INoteFragmentListener listener;

    //Color Picker
    String selectedNoteColor;
    View subtitleIndicator;
    ImageView imageColor1, imageColor2, imageColor3, imageColor4, imageColor5;

    //Add Image & Link
    ImageView imageAddImage, imageAddLink;
    //Remove Image & Link
    ImageView imageRemoveImage, imageRemoveLink;

    ImageView imageDelete;

    //Image
    private static final int REQUEST_CODE_EXTERNAL_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;
    private String selectedImagePath;

    //Link
    TextView textUrl;
    LinearLayout layoutUrl;
    AlertDialog alertDialogUrl;

    ////
    boolean isUpdate;
    boolean isAction;
    Note tempNote;

    public static AddNoteFragment Instance() {
        return new AddNoteFragment();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
            dialog.getWindow().setWindowAnimations(R.style.AppTheme_Slide);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme_FullScreenDialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.layout_bottom_sheet_add_note, container, false);
        back = view.findViewById(R.id.imageBack);
        done = view.findViewById(R.id.imageDone);
        imageNote = view.findViewById(R.id.imageNote);
        title = view.findViewById(R.id.inputNoteTitle);
        subtitle = view.findViewById(R.id.inputNoteSubtitle);
        note = view.findViewById(R.id.inputNote);
        date = view.findViewById(R.id.textDateTime);

        //Color Picker
        subtitleIndicator = view.findViewById(R.id.viewSubtitleIndicator);
        imageColor1 = view.findViewById(R.id.imageColor1);
        imageColor2 = view.findViewById(R.id.imageColor2);
        imageColor3 = view.findViewById(R.id.imageColor3);
        imageColor4 = view.findViewById(R.id.imageColor4);
        imageColor5 = view.findViewById(R.id.imageColor5);

        //Add Image & Link
        imageAddImage = view.findViewById(R.id.imageAddImage);
        imageAddLink = view.findViewById(R.id.imageAddLink);

        //Remove Image & Link
        imageRemoveImage = view.findViewById(R.id.imageCloseImage);
        imageRemoveLink = view.findViewById(R.id.imageCloseLink);

        imageDelete = view.findViewById(R.id.imageDelete);

        //Link
        textUrl = view.findViewById(R.id.textURL);
        layoutUrl = view.findViewById(R.id.layoutURL);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title.requestFocus();
        back.setOnClickListener(v -> dismiss());
        date.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy, HH:mm a", Locale.getDefault()).format(new Date()));
        done.setOnClickListener(v -> saveNote(getActivity()));

        //Color Picker
        selectedNoteColor = "#333333";
        setSubtitleIndicatorColor();
        imageColor1.setOnClickListener(v -> {
            selectedNoteColor = "#333333";
            imageColor1.setImageResource(R.drawable.ic_done_right);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        imageColor2.setOnClickListener(v -> {
            selectedNoteColor = "#FDBE38";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(R.drawable.ic_done_right);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        imageColor3.setOnClickListener(v -> {
            selectedNoteColor = "#FF4842";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(R.drawable.ic_done_right);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        imageColor4.setOnClickListener(v -> {
            selectedNoteColor = "#3A52FC";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(R.drawable.ic_done_right);
            imageColor5.setImageResource(0);
            setSubtitleIndicatorColor();
        });
        imageColor5.setOnClickListener(v -> {
            selectedNoteColor = "#000000";
            imageColor1.setImageResource(0);
            imageColor2.setImageResource(0);
            imageColor3.setImageResource(0);
            imageColor4.setImageResource(0);
            imageColor5.setImageResource(R.drawable.ic_done_right);
            setSubtitleIndicatorColor();
        });

        selectedImagePath = "";

        //Add Image & Link
        imageAddImage.setOnClickListener(v -> selectImage());
        imageAddLink.setOnClickListener(v -> showUrlDialog());

        //Remove Image & Link
        imageRemoveImage.setOnClickListener(v -> removeImage());
        imageRemoveLink.setOnClickListener(v -> removeLink());

        Bundle bundle = getArguments();
        if (bundle != null) {
            isUpdate = bundle.getBoolean("IS_UPDATE", false);
            isAction = bundle.getBoolean("IS_ACTION", false);
            if (isUpdate) {
                tempNote = (Note) bundle.getSerializable("NOTE");
                viewUpdatedNote();

                //If user wants to delete the update note
                imageDelete.setVisibility(View.VISIBLE);
                imageDelete.setOnClickListener(v -> showDeleteNoteDialog(getActivity(), tempNote));
            } else if (isAction) {
                tempNote = (Note) bundle.getSerializable("NOTE");
                viewUpdatedNote();
            }
        }
    }

    private void saveNote(Context context) {
        if (title.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Title can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        } else if (note.getText().toString().trim().isEmpty()) {
            Toast.makeText(context, "Note can't be empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        Note note = new Note(title.getText().toString().trim(), subtitle.getText().toString().trim(), date.getText().toString().trim(), this.note.getText().toString().trim(), selectedNoteColor, selectedImagePath, null);
        if (layoutUrl.getVisibility() == View.VISIBLE) {
            note.setLink(textUrl.getText().toString().trim());
        }

        if (tempNote != null) {
            note.setId(tempNote.getId());
        }

        NoteDatabase db = new NoteDatabase(context);
        long id;
        if (isUpdate) {
            id = db.updateNote(note);
        } else {
            id = db.insertNote(note);
        }
        if (id < 0) {
            Toast.makeText(context, "Error!", Toast.LENGTH_SHORT).show();
        } else {
            dismiss();
            listener.onNoteInserted();
            //Toast.makeText(context, "Note successfully added.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNote(Context context, Note note) {
        NoteDatabase db = new NoteDatabase(context);
        db.deleteNote(note.getId());
        dismiss();
        listener.onNoteDeleted();
    }

    private void showDeleteNoteDialog(Context context, Note note) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_dialog_delete, (LinearLayout) getActivity().findViewById(R.id.layout_dialog_delete_container));
        builder.setView(view);

        alertDialogUrl = builder.create();
        if (alertDialogUrl.getWindow() != null) {
            alertDialogUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        view.findViewById(R.id.textDELETE_NOTE).setOnClickListener(v -> {
            alertDialogUrl.dismiss();
            deleteNote(context, note);
        });
        view.findViewById(R.id.textCANCEL).setOnClickListener(v -> alertDialogUrl.dismiss());
        alertDialogUrl.show();
    }

    private void setSubtitleIndicatorColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) subtitleIndicator.getBackground();
        gradientDrawable.setColor(Color.parseColor(selectedNoteColor));
    }

    private boolean requestExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_EXTERNAL_PERMISSION);
            return false;
        }
        return true;
    }

    private void selectImage() {
        if (requestExternalStoragePermission()) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
            }
        }
    }

    private String getPathFromUri(Uri uri) {
        String filePath;
        Cursor cursor = getActivity().getContentResolver().query(uri, null, null, null, null);
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

    private void showUrlDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_dialog_add_url, (LinearLayout) getActivity().findViewById(R.id.layout_dialog_container));
        builder.setView(view);

        alertDialogUrl = builder.create();
        if (alertDialogUrl.getWindow() != null) {
            alertDialogUrl.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        EditText inputUrl = view.findViewById(R.id.inputURL);
        inputUrl.requestFocus();

        view.findViewById(R.id.textADD).setOnClickListener(v -> {
            if (inputUrl.getText().toString().trim().isEmpty()) {
                Toast.makeText(getActivity(), "Enter URL", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.WEB_URL.matcher(inputUrl.getText().toString().trim()).matches()) {
                Toast.makeText(getActivity(), "Enter valid URL", Toast.LENGTH_SHORT).show();
            } else {
                textUrl.setText(inputUrl.getText().toString().trim());
                layoutUrl.setVisibility(View.VISIBLE);
                alertDialogUrl.dismiss();
            }
        });
        view.findViewById(R.id.textCANCEL).setOnClickListener(v -> alertDialogUrl.dismiss());
        alertDialogUrl.show();
    }

    private void viewUpdatedNote() {
        title.setText(tempNote.getTitle());
        subtitle.setText(tempNote.getSubtitle());
        note.setText(tempNote.getNote());

        String imagePath = tempNote.getPhotoPath();
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            imageNote.setImageBitmap(BitmapFactory.decodeFile(imagePath));
            imageNote.setVisibility(View.VISIBLE);
            selectedImagePath = imagePath;
            imageRemoveImage.setVisibility(View.VISIBLE);
        }

        String link = tempNote.getLink();
        if (link != null && !link.trim().isEmpty()) {
            textUrl.setText(link);
            layoutUrl.setVisibility(View.VISIBLE);
        }

        String color = tempNote.getColor();
        if (color != null && !color.trim().isEmpty()) {
            switch (color) {
                case "#333333":
                    imageColor1.performClick();
                    break;
                case "#FDBE38":
                    imageColor2.performClick();
                    break;
                case "#FF4842":
                    imageColor3.performClick();
                    break;
                case "#3A52FC":
                    imageColor4.performClick();
                    break;
                case "#000000":
                    imageColor5.performClick();
                    break;
            }
            selectedNoteColor = color;
            setSubtitleIndicatorColor();
        }
    }

    private void removeLink() {
        textUrl.setText(null);
        layoutUrl.setVisibility(View.GONE);
    }

    private void removeImage() {
        imageNote.setImageBitmap(null);
        imageNote.setVisibility(View.GONE);
        imageRemoveImage.setVisibility(View.GONE);
        selectedImagePath = "";
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (INoteFragmentListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "Must implement INoteInsertedListener");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_EXTERNAL_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(getActivity(), "Permission Denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == getActivity().RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getActivity().getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        imageNote.setImageBitmap(bitmap);
                        imageNote.setVisibility(View.VISIBLE);
                        selectedImagePath = getPathFromUri(selectedImageUri);
                        imageRemoveImage.setVisibility(View.VISIBLE);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }
}
