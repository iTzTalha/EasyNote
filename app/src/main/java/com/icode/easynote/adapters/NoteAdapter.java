package com.icode.easynote.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.icode.easynote.R;
import com.icode.easynote.listeners.INoteListener;
import com.icode.easynote.models.Note;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> {

    List<Note> notes;
    INoteListener noteListener;

    Timer timer;
    List<Note> newNotes;

    public NoteAdapter(List<Note> notes, INoteListener noteListener) {
        this.notes = notes;
        this.noteListener = noteListener;
        this.newNotes = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.setNote(note);
        holder.layout.setOnClickListener(v -> noteListener.onNoteClick(note, position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView title, subtitle, date, note;
        LinearLayout layout;
        RoundedImageView photo;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.textTitle);
            subtitle = itemView.findViewById(R.id.textSubtitle);
            date = itemView.findViewById(R.id.textdate);
            note = itemView.findViewById(R.id.textNote);
            layout = itemView.findViewById(R.id.layout_note);
            photo = itemView.findViewById(R.id.imageNote);
        }

        void setNote(Note note) {
            title.setText(note.getTitle());
            if (!note.getSubtitle().trim().isEmpty()) {
                subtitle.setText(note.getSubtitle());
                subtitle.setVisibility(View.VISIBLE);
            } else {
                subtitle.setVisibility(View.GONE);
            }
            date.setText(note.getDate());
            this.note.setText(note.getNote());
            GradientDrawable gradientDrawable = (GradientDrawable) layout.getBackground();
            if (note.getColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (note.getPhotoPath() != null) {
                photo.setImageBitmap(BitmapFactory.decodeFile(note.getPhotoPath()));
                photo.setVisibility(View.VISIBLE);
            } else {
                photo.setVisibility(View.GONE);
            }
        }
    }

    public void searchNotes(String keyword) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (keyword.trim().isEmpty()) {
                    notes = newNotes;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : newNotes) {
                        if (note.getTitle().toLowerCase().contains(keyword) || note.getSubtitle().toLowerCase().contains(keyword) || note.getNote().toLowerCase().contains(keyword)) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post(() -> notifyDataSetChanged());
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
