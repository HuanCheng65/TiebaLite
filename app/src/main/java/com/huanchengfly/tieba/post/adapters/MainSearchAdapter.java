package com.huanchengfly.tieba.post.adapters;

import android.content.Context;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.components.MyViewHolder;
import com.huanchengfly.tieba.post.models.database.SearchHistory;

import org.litepal.LitePal;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class MainSearchAdapter extends RecyclerView.Adapter<MyViewHolder> implements Filterable {
    public static final String TAG = MainSearchAdapter.class.getSimpleName();
    private static final int TYPE_HISTORY = 0;
    private static final int TYPE_ACTION_CLEAR_ALL = 1;
    private WeakReference<Context> mContextWeakReference;
    private List<SearchHistory> mHistoryList;
    private List<SearchHistory> mResult;
    private String mConstraint;
    private OnSearchItemClickListener mListener;

    public MainSearchAdapter(Context context) {
        mContextWeakReference = new WeakReference<>(context);
        mResult = new ArrayList<>();
        refreshData();
    }

    public OnSearchItemClickListener getOnSearchItemClickListener() {
        return mListener;
    }

    public void setOnSearchItemClickListener(OnSearchItemClickListener onSearchItemClickListener) {
        this.mListener = onSearchItemClickListener;
    }

    public Context getContext() {
        return mContextWeakReference.get();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_ACTION_CLEAR_ALL) {
            return new MyViewHolder(getContext(), R.layout.item_action);
        }
        return new MyViewHolder(getContext(), R.layout.item_search_history);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == mResult.size()) {
            return TYPE_ACTION_CLEAR_ALL;
        }
        return TYPE_HISTORY;
    }

    public void refreshData() {
        mHistoryList = LitePal.order("timestamp DESC").find(SearchHistory.class);
        if (!TextUtils.isEmpty(mConstraint)) {
            getFilter().filter(mConstraint);
        } else {
            notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (getItemViewType(position) == TYPE_ACTION_CLEAR_ALL) {
            holder.setItemOnClickListener(v -> LitePal.deleteAllAsync(SearchHistory.class).listen(rowsAffected -> {
                mHistoryList = new ArrayList<>();
                mResult = new ArrayList<>();
                notifyDataSetChanged();
            }));
        } else {
            SearchHistory item = mResult.get(position);
            holder.setText(R.id.history_item_title, item.getContent());
            holder.setItemOnClickListener(v -> {
                if (getOnSearchItemClickListener() != null) {
                    getOnSearchItemClickListener().onSearchItemClick(position, item.getContent());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        if (mResult.size() == 0) {
            return 0;
        }
        if (!TextUtils.isEmpty(mConstraint)) {
            return mResult.size();
        }
        return mResult.size() + 1;
    }

    public void setData(List<SearchHistory> list) {
        mResult = list;
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                mConstraint = constraint.toString().toLowerCase();

                if (!TextUtils.isEmpty(mConstraint)) {
                    List<SearchHistory> history = new ArrayList<>();
                    List<SearchHistory> results = new ArrayList<>();

                    if (!mHistoryList.isEmpty()) {
                        history.addAll(mHistoryList);
                    }

                    for (SearchHistory item : history) {
                        String string = item.getContent().toLowerCase();
                        if (string.contains(mConstraint)) {
                            results.add(item);
                        }
                    }
                    if (results.size() > 0) {
                        filterResults.values = results;
                        filterResults.count = results.size();
                    }
                } else {
                    if (!mHistoryList.isEmpty()) {
                        filterResults.values = mHistoryList;
                        filterResults.count = mHistoryList.size();
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count > 0) {
                    List<SearchHistory> dataSet = new ArrayList<>();
                    List<?> resultSet = (List<?>) results.values;
                    int size = results.count < 8 ? results.count : 8;

                    for (int i = 0; i < size; i++) {
                        if (resultSet.get(i) instanceof SearchHistory) {
                            dataSet.add((SearchHistory) resultSet.get(i));
                        }
                    }

                    setData(dataSet);
                }
            }
        };
    }

    public interface OnSearchItemClickListener {
        void onSearchItemClick(int position, CharSequence content);
    }
}
