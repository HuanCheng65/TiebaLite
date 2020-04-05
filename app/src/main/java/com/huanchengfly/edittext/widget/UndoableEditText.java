package com.huanchengfly.edittext.widget;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import com.huanchengfly.edittext.OperationManager;

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