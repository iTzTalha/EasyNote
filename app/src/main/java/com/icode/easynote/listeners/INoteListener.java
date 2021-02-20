package com.icode.easynote.listeners;

import com.icode.easynote.models.Note;

public interface INoteListener {
    void onNoteClick(Note note, int position);
}
