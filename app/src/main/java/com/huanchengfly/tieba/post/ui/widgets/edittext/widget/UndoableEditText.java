package com.huanchengfly.tieba.post.ui.widgets.edittext.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.widget.TextViewCompat;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.widgets.edittext.OperationManager;

public class UndoableEditText extends AppCompatEditText {
    private static final String KEY_SUPER = "KEY_SUPER";
    private static final String KEY_OPT = OperationManager.class.getCanonicalName();
    private final OperationManager mgr = new OperationManager(this);

    public UndoableEditText(Context context) {
        super(context);
        init();
    }

    public UndoableEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UndoableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public OperationManager getMgr() {
        return mgr;
    }

    public boolean canUndo() {
        return mgr.canUndo();
    }

    public boolean canRedo() {
        return mgr.canRedo();
    }

    public boolean undo() {
        return mgr.undo();
    }

    public boolean redo() {
        return mgr.redo();
    }

    private void init() {
        TextViewCompat.setCustomSelectionActionModeCallback(this, new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater menuInflater = mode.getMenuInflater();
                menuInflater.inflate(R.menu.menu_undoable_edit_text, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
            }
        });
        addTextChangedListener(mgr);
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_SUPER, super.onSaveInstanceState());
        bundle.putBundle(KEY_OPT, mgr.exportState());
        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        Parcelable superState = bundle.getParcelable(KEY_SUPER);

        mgr.disable();
        super.onRestoreInstanceState(superState);
        mgr.enable();

        mgr.importState(bundle.getBundle(KEY_OPT));
    }
}