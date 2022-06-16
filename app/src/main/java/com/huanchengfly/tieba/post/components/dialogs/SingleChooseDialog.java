package com.huanchengfly.tieba.post.components.dialogs;

import android.content.Context;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.SingleChooseAdapter;
import com.huanchengfly.tieba.post.interfaces.OnChooseListener;

import java.util.Arrays;
import java.util.List;

public class SingleChooseDialog extends AlertDialog {
    private final Context mContext;
    private ListView listView;
    private final List<String> strings;
    private OnChooseListener onChooseListener;
    private SingleChooseAdapter adapter;

    public SingleChooseDialog(Context context, String[] strings) {
        super(context);
        this.mContext = context;
        this.strings = Arrays.asList(strings);
        initView();
    }

    public SingleChooseDialog setOnChooseListener(OnChooseListener onChooseListener) {
        this.onChooseListener = onChooseListener;
        return this;
    }

    public SingleChooseDialog setChoosePosition(int choosePosition) {
        adapter.setChoosePosition(choosePosition);
        return this;
    }

    private void initView() {
        View contentView = View.inflate(mContext, R.layout.dialog_choose, null);
        listView = contentView.findViewById(R.id.dialog_choose_list_view);
        adapter = new SingleChooseAdapter(mContext, strings);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> adapter.setChoosePosition(position));
        setButton(BUTTON_POSITIVE, mContext.getString(R.string.button_sure_default), (dialog, which) -> {
            if (onChooseListener != null) {
                int position = adapter.getChoosePosition();
                if (position != -1) {
                    onChooseListener.onChoose(position, strings.get(position));
                }
            }
        });
        setButton(BUTTON_NEGATIVE, mContext.getString(R.string.button_cancel), (dialog, which) -> {
            cancel();
        });
        setView(contentView);
    }
}
