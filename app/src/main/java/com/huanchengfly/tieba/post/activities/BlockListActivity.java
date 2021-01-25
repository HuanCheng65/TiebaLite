package com.huanchengfly.tieba.post.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.BlockListAdapter;
import com.huanchengfly.tieba.post.components.dialogs.EditTextDialog;
import com.huanchengfly.tieba.post.models.database.Block;
import com.huanchengfly.tieba.post.utils.GsonUtil;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.Util;

public class BlockListActivity extends BaseActivity {
    private Toolbar toolbar;
    private AppBarLayout toolbarContainer;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private BlockListAdapter blockListAdapter;
    private EditTextDialog editTextDialog;
    private int type;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_list);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarContainer = (AppBarLayout) findViewById(R.id.appbar);
        recyclerView = (RecyclerView) findViewById(R.id.block_list_recycler_view);
        Intent intent = getIntent();
        this.type = intent.getIntExtra("category", Block.CATEGORY_BLACK_LIST);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        if (this.type == Block.CATEGORY_BLACK_LIST) {
            if (actionBar != null) {
                actionBar.setTitle(R.string.title_black_list);
            }
        } else if (this.type == Block.CATEGORY_WHITE_LIST) {
            if (actionBar != null) {
                actionBar.setTitle(R.string.title_white_list);
            }
        } else {
            finish();
            return;
        }
        layoutManager = new LinearLayoutManager(this);
        ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                viewHolder.itemView.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    viewHolder.itemView.setBackgroundColor(Util.getColorByAttr(BlockListActivity.this, R.attr.colorControlHighlight, R.color.transparent));
                }
            }

            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                int dragFlags = 0, swiped = ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
                return makeMovementFlags(dragFlags, swiped);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Block block = blockListAdapter.get(position);
                blockListAdapter.remove(position);
                Util.createSnackbar(recyclerView, R.string.toast_deleted, Snackbar.LENGTH_LONG)
                        .addCallback(new Snackbar.Callback() {
                            @Override
                            public void onDismissed(Snackbar snackbar, int event) {
                                if (event != DISMISS_EVENT_ACTION) {
                                    block.delete();
                                    blockListAdapter.refresh();
                                }
                            }
                        }).setAction(R.string.button_undo, mView -> blockListAdapter.insert(block, position)).show();
            }
        });
        mItemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        blockListAdapter = new BlockListAdapter(this, this.type);
        recyclerView.setAdapter(blockListAdapter);
        blockListAdapter.refresh();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_block_list_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add:
                editTextDialog = new EditTextDialog(this)
                        .setTipText(R.string.tip_input)
                        .setHelperText(R.string.tip_multi_block);
                if (this.type == Block.CATEGORY_BLACK_LIST)
                    editTextDialog.setTitle(R.string.title_add_black);
                else if (this.type == Block.CATEGORY_WHITE_LIST)
                    editTextDialog.setTitle(R.string.title_add_white);
                editTextDialog.setOnSubmitListener((String content) -> {
                    String[] strings = new String[]{content};
                    if (content.contains(" ")) {
                        strings = content.split(" ");
                    }
                    new Block()
                            .setKeywords(GsonUtil.getGson().toJson(strings))
                            .setType(Block.TYPE_KEYWORD)
                            .setCategory(this.type)
                            .save();
                    blockListAdapter.refresh();
                }).show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
