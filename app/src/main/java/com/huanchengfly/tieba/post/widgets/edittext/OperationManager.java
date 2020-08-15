package com.huanchengfly.tieba.post.widgets.edittext;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;

public class OperationManager implements TextWatcher {
    private static final String KEY_UNDO_OPTS = "KEY_UNDO_OPTS";
    private static final String KEY_REDO_OPTS = "KEY_REDO_OPTS";
    private final EditText editText;

    /*编辑记录*/
    private final LinkedList<EditOperation> undoOpts = new LinkedList<>();
    private final LinkedList<EditOperation> redoOpts = new LinkedList<>();
    private EditOperation opt;
    private boolean enable = true;

    public OperationManager(EditText editText) {
        this.editText = editText;
    }

    public static OperationManager setup(EditText editText) {
        OperationManager mgr = new OperationManager(editText);
        editText.addTextChangedListener(mgr);
        return mgr;
    }

    public OperationManager disable() {
        enable = false;
        return this;
    }

    public OperationManager enable() {
        enable = true;
        return this;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (count > 0) {
            int end = start + count;
            if (enable) {
                if (opt == null) {
                    opt = new EditOperation();
                }
                opt.setSrc(s.subSequence(start, end), start, end);
            }
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (count > 0) {
            int end = start + count;
            if (enable) {
                if (opt == null) {
                    opt = new EditOperation();
                }
                opt.setDst(s.subSequence(start, end), start, end);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (enable && opt != null) {
            if (!redoOpts.isEmpty()) {
                redoOpts.clear();
            }

            undoOpts.push(opt);
        }
        opt = null;
    }

    public boolean canUndo() {
        return !undoOpts.isEmpty();
    }

    public boolean canRedo() {
        return !redoOpts.isEmpty();
    }

    public boolean undo() {
        if (canUndo()) {
            EditOperation undoOpt = undoOpts.pop();

            //屏蔽撤销产生的事件
            disable();
            undoOpt.undo(editText);
            enable();

            //填入重做栈
            redoOpts.push(undoOpt);
            return true;
        }
        return false;
    }

    /*保存/回复*/

    public boolean redo() {
        if (canRedo()) {
            EditOperation redoOpt = redoOpts.pop();

            //屏蔽重做产生的事件
            disable();
            redoOpt.redo(editText);
            enable();

            //填入撤销
            undoOpts.push(redoOpt);
            return true;
        }
        return false;
    }

    public Bundle exportState() {
        Bundle state = new Bundle();

        state.putSerializable(KEY_UNDO_OPTS, undoOpts);
        state.putSerializable(KEY_REDO_OPTS, redoOpts);

        return state;
    }

    public void importState(Bundle state) {
        Collection<EditOperation> savedUndoOpts = (Collection<EditOperation>) state.getSerializable(KEY_UNDO_OPTS);
        undoOpts.clear();
        undoOpts.addAll(savedUndoOpts);

        Collection<EditOperation> savedRedoOpts = (Collection<EditOperation>) state.getSerializable(KEY_REDO_OPTS);
        redoOpts.clear();
        redoOpts.addAll(savedRedoOpts);
    }

    private static class EditOperation implements Parcelable, Serializable {
        public static final Creator<EditOperation> CREATOR = new Creator<EditOperation>() {
            @Override
            public EditOperation createFromParcel(Parcel source) {
                return new EditOperation(source);
            }

            @Override
            public EditOperation[] newArray(int size) {
                return new EditOperation[size];
            }
        };
        private String src;
        private int srcStart;
        private int srcEnd;
        private String dst;
        private int dstStart;
        private int dstEnd;

        EditOperation() {
        }

        EditOperation(Parcel in) {
            this.src = in.readString();
            this.srcStart = in.readInt();
            this.srcEnd = in.readInt();
            this.dst = in.readString();
            this.dstStart = in.readInt();
            this.dstEnd = in.readInt();
        }

        EditOperation setSrc(CharSequence src, int srcStart, int srcEnd) {
            this.src = src != null ? src.toString() : "";
            this.srcStart = srcStart;
            this.srcEnd = srcEnd;
            return this;
        }

        EditOperation setDst(CharSequence dst, int dstStart, int dstEnd) {
            this.dst = dst != null ? dst.toString() : "";
            this.dstStart = dstStart;
            this.dstEnd = dstEnd;
            return this;
        }

        void undo(EditText text) {
            Editable editable = text.getText();

            int idx = -1;
            if (dstEnd > 0) {
                editable.delete(dstStart, dstEnd);

                if (src == null) {
                    idx = dstStart;
                }
            }
            if (src != null) {
                editable.insert(srcStart, src);
                idx = srcStart + src.length();
            }
            if (idx >= 0) {
                text.setSelection(idx);
            }
        }

        void redo(EditText text) {
            Editable editable = text.getText();

            int idx = -1;
            if (srcEnd > 0) {
                editable.delete(srcStart, srcEnd);
                if (dst == null) {
                    idx = srcStart;
                }
            }
            if (dst != null) {
                editable.insert(dstStart, dst);
                idx = dstStart + dst.length();
            }
            if (idx >= 0) {
                text.setSelection(idx);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.src);
            dest.writeInt(this.srcStart);
            dest.writeInt(this.srcEnd);
            dest.writeString(this.dst);
            dest.writeInt(this.dstStart);
            dest.writeInt(this.dstEnd);
        }
    }
}
