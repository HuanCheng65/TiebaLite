package com.huanchengfly.tieba.post.activities;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.google.gson.JsonSyntaxException;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.adapters.HistoryListAdapter;
import com.huanchengfly.tieba.post.models.ThreadHistoryInfoBean;
import com.huanchengfly.tieba.post.models.database.History;
import com.huanchengfly.tieba.post.utils.DisplayUtil;
import com.huanchengfly.tieba.post.utils.HistoryHelper;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.GsonUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryActivity extends BaseActivity {
    private HistoryHelper helper;
    private NavigationHelper navigationHelper;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        navigationHelper = NavigationHelper.newInstance(this);
        mListView = (ListView) findViewById(R.id.history_list);
        mListView.setDivider(new ColorDrawable(ThemeUtils.getColorByAttr(this, R.attr.colorDivider)));
        mListView.setDividerHeight(DisplayUtil.dp2px(this, 1));
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            HistoryListAdapter adapter = (HistoryListAdapter) mListView.getAdapter();
            History dataBean = (History) adapter.getItem(position);
            switch (dataBean.getType()) {
                case HistoryHelper.TYPE_URL:
                    navigationHelper.navigationByData(NavigationHelper.ACTION_URL, dataBean.getData());
                    break;
                case HistoryHelper.TYPE_BA:
                    navigationHelper.navigationByData(NavigationHelper.ACTION_FORUM, dataBean.getData());
                    break;
                case HistoryHelper.TYPE_THREAD:
                    if (dataBean.getExtras() != null) {
                        try {
                            ThreadHistoryInfoBean historyInfoBean = GsonUtil.getGson().fromJson(dataBean.getExtras(), ThreadHistoryInfoBean.class);
                            if (historyInfoBean == null) {
                                break;
                            }
                            Map<String, String> map = new HashMap<>();
                            map.put("tid", dataBean.getData());
                            map.put("pid", historyInfoBean.getPid());
                            map.put("from", ThreadActivity.FROM_HISTORY);
                            map.put("seeLz", historyInfoBean.isSeeLz() ? "1" : "0");
                            navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
                            break;
                        } catch (JsonSyntaxException ignored) {
                        }
                    }
                    Map<String, String> map = new HashMap<>();
                    map.put("tid", dataBean.getData());
                    navigationHelper.navigationByData(NavigationHelper.ACTION_THREAD, map);
                    break;
            }
        });
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_history);
        }
        helper = new HistoryHelper(this);
        refreshData();
    }

    private void refreshData() {
        List<History> array = helper.getAll();
        mListView.setAdapter(new HistoryListAdapter(this, array));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_history_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menu_delete:
                helper.delete();
                Toast.makeText(this, R.string.toast_delete_success, Toast.LENGTH_SHORT).show();
                refreshData();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
