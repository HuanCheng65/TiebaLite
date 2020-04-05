package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.reflect.TypeToken;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.models.database.Block;
import com.huanchengfly.tieba.post.utils.BlockUtil;
import com.huanchengfly.utils.GsonUtil;

import org.litepal.LitePal;

import java.util.List;

public class BlockListAdapter extends RecyclerView.Adapter<MyViewHolder> {
    private static final String TAG = "BlockListAdapter";
    private Context mContext;
    private List<Block> dataList;
    private int type;

    public BlockListAdapter(Context context, int type) {
        super();
        this.mContext = context;
        this.dataList = BlockUtil.getBlackList();
        this.type = type;
    }

    public void refresh() {
        refresh(true);
    }

    public void refresh(boolean notify) {
        if (this.type == Block.CATEGORY_WHITE_LIST) {
            this.dataList = LitePal.where("category = ?", "11").find(Block.class);
        } else {
            this.dataList = LitePal.where("category = ?", "10").find(Block.class);
        }
        if (notify) {
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_block_list, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Block block = get(position);
        TextView textView = holder.getView(R.id.item_block_list_word);
        if (block.getType() == Block.TYPE_KEYWORD) {
            List<String> stringList = GsonUtil.getGson().fromJson(block.getKeywords(), new TypeToken<List<String>>() {
            }.getType());
            textView.setText(listToString(stringList, " "));
        } else if (block.getType() == Block.TYPE_USER) {
            textView.setText(block.getUsername());
        }
    }

    public Block get(int position) {
        return dataList.get(position);
    }

    public void remove(int position) {
        if (position < dataList.size() && position >= 0) {
            dataList.remove(position);
            notifyItemRemoved(position);
            if (position != dataList.size()) {
                this.notifyItemRangeChanged(position, dataList.size() - position);
            }
        }
    }

    public void insert(Block block, int position) {
        if (position <= dataList.size() && position >= 0) {
            dataList.add(position, block);
            notifyItemInserted(position);
            notifyItemRangeChanged(position, dataList.size() - position);
        }
    }

    @NonNull
    private String listToString(List list, String separator) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i)).append(separator);
        }
        return sb.toString().substring(0, sb.toString().length() - 1);
    }
}