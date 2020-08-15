package com.huanchengfly.tieba.post.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.huanchengfly.tieba.post.ui.about.AboutPage;
import com.huanchengfly.tieba.post.ui.theme.utils.ThemeUtils;
import com.huanchengfly.tieba.post.api.LiteApi;
import com.huanchengfly.tieba.post.api.interfaces.CommonAPICallback;
import com.huanchengfly.tieba.post.api.models.NewUpdateBean;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.utils.NavigationHelper;
import com.huanchengfly.tieba.post.utils.ThemeUtil;
import com.huanchengfly.tieba.post.utils.VersionUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AboutActivity extends BaseActivity implements View.OnClickListener {
    public static final int STATE_ERROR = 0;
    public static final int STATE_NO_UPDATE = 1;
    public static final int STATE_UPDATE = 2;
    private View updateTip;
    private TextView updateTipHeaderTv;
    private TextView updateTipTitleTv;
    private TextView updateTipContentTv;
    private Button dismissBtn;
    private Button downloadBtn;

    private int updateState;
    private NewUpdateBean.ResultBean resultBean;
    private NavigationHelper navigationHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_about;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ThemeUtil.setTranslucentThemeBackground(findViewById(R.id.background));
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        RelativeLayout mainView = (RelativeLayout) findViewById(R.id.main);
        View headerView = View.inflate(this, R.layout.header_about, null);
        updateTip = headerView.findViewById(R.id.header_update_tip_shadow);
        updateTipHeaderTv = headerView.findViewById(R.id.header_update_tip_header_title);
        updateTipTitleTv = headerView.findViewById(R.id.header_update_tip_title);
        updateTipContentTv = headerView.findViewById(R.id.header_update_tip_content);
        dismissBtn = headerView.findViewById(R.id.header_update_tip_button_dismiss);
        downloadBtn = headerView.findViewById(R.id.header_update_tip_button_download);
        navigationHelper = NavigationHelper.newInstance(this);
        downloadBtn.setOnClickListener(this);
        dismissBtn.setOnClickListener(this);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_about);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        int colorIcon = ThemeUtils.getColorByAttr(this, R.attr.colorAccent);
        new AboutPage(this)
                .setHeaderView(headerView)
                .addTitle("应用信息", colorIcon)
                .addItem(new AboutPage.Item("当前版本", VersionUtil.getVersionName(this), R.drawable.ic_round_info, colorIcon))
                .addItem(new AboutPage.Item("源代码").setIcon(R.drawable.ic_codepen, colorIcon).setOnClickListener(v -> navigationHelper.navigationByData(NavigationHelper.ACTION_URL, "https://github.com/HuanCheng65/TiebaLite")))
                .into(mainView);
        checkUpdate();
    }

    /*
    private void openJoinGroupDialog() {
        if (groupInfoList == null) {
            return;
        }
        List<String> names = new ArrayList<>();
        List<UpdateInfoBean.GroupInfo> groupInfos = new ArrayList<>();
        for (UpdateInfoBean.GroupInfo groupInfo : groupInfoList) {
            if (groupInfo.isEnabled()) {
                groupInfos.add(groupInfo);
                names.add(groupInfo.getType() + " " + groupInfo.getName());
            }
        }
        DialogUtil.build(AboutActivity.this)
                .setTitle(R.string.title_join_group)
                .setItems(names.toArray(new String[0]), (dialog, which) -> {
                    if (TextUtils.isEmpty(groupInfos.get(which).getQGroupKey()) || !joinQQGroup(groupInfos.get(which).getQGroupKey())) {
                        navigationHelper.navigationByData(NavigationHelper.ACTION_URL, groupInfos.get(which).getLink());
                    }
                })
                .show();
    }
    private void loadGroups() {
        UpdateInfoBean cache = CacheUtil.getCache(this, CACHE_ID_QQ_GROUP, UpdateInfoBean.class);
        if (cache != null) {
            groupInfoList = cache.getGroups();
            if (TextUtils.equals(EXTRA_ACTION_JOIN_GROUP, action)) {
                openJoinGroupDialog();
                actionExecuted = true;
            }
        }
        LiteApi.getInstance().updateInfo(new CommonAPICallback<UpdateInfoBean>() {
            @Override
            public void onSuccess(UpdateInfoBean data) {
                CacheUtil.putCache(AboutActivity.this, CACHE_ID_QQ_GROUP, data);
                groupInfoList = data.getGroups();
                if (!actionExecuted && TextUtils.equals(EXTRA_ACTION_JOIN_GROUP, action)) {
                    openJoinGroupDialog();
                }
                for (UpdateInfoBean.SupportmentBean supportmentBean : data.getSupportment()) {
                    AboutPage.Item item = new AboutPage.Item(supportmentBean.getTitle(), supportmentBean.getSubtitle());
                    if (supportmentBean.getIcon() != null) {
                        if (supportmentBean.getIcon().getType() == UpdateInfoBean.SupportmentBean.IconBean.TYPE_RESOURCE) {
                            item.setIcon(getResources().getIdentifier(supportmentBean.getIcon().getId(), "drawable", BaseApplication._getPackageName()));
                        } else if (supportmentBean.getIcon().getType() == UpdateInfoBean.SupportmentBean.IconBean.TYPE_IMAGE) {
                            item.setIcon(supportmentBean.getIcon().getUrl());
                        }
                    }
                    if (supportmentBean.getAction() != null) {
                        item.setOnClickListener(v -> {
                            FlurryAgent.logEvent("clicked" + supportmentBean.getId());
                            if (supportmentBean.getAction().getType() == UpdateInfoBean.SupportmentBean.ActionBean.TYPE_LINK) {
                                navigationHelper.navigationByData(NavigationHelper.ACTION_URL, supportmentBean.getAction().getUrl());
                            } else if (supportmentBean.getAction().getType() == UpdateInfoBean.SupportmentBean.ActionBean.TYPE_IMAGE) {
                                PhotoViewActivity.launch(AboutActivity.this, new PhotoViewBean(supportmentBean.getAction().getUrl(), true));
                            }
                        });
                    }
                    if (supportmentBean.getExpireTime() > System.currentTimeMillis()) {
                        mAboutPage.addItem(item);
                    }
                }
                mAboutPage.notifyDataSetChanged();
            }

            @Override
            public void onFailure(int code, String error) {
            }
        });
    }
    */

    private void checkUpdate() {
        LiteApi.getInstance().newCheckUpdate(new CommonAPICallback<NewUpdateBean>() {
            @Override
            public void onSuccess(NewUpdateBean data) {
                if (data.isHasUpdate()) {
                    resultBean = data.getResult();
                    updateState = STATE_UPDATE;
                } else {
                    resultBean = null;
                    updateState = STATE_NO_UPDATE;
                }
                refreshUpdateTip();
            }

            @Override
            public void onFailure(int code, String error) {
                resultBean = null;
                updateState = STATE_ERROR;
                refreshUpdateTip();
            }
        });
    }

    private void refreshUpdateTip() {
        switch (updateState) {
            case STATE_NO_UPDATE:
                downloadBtn.setText(R.string.button_check_update);
                updateTip.setVisibility(View.VISIBLE);
                dismissBtn.setVisibility(View.GONE);
                updateTipHeaderTv.setText(getString(R.string.update_tip_no_header));
                updateTipTitleTv.setText(getString(R.string.update_tip_no_title));
                updateTipContentTv.setText(getString(R.string.update_tip_no_content));
                break;
            case STATE_UPDATE:
                if (resultBean != null) {
                    downloadBtn.setText(R.string.button_go_to_download);
                    updateTip.setVisibility(View.VISIBLE);
                    boolean cancelable = resultBean.isCancelable();
                    updateTipHeaderTv.setText(getString(R.string.update_tip_header, resultBean.getVersionType() == 0 ? getString(R.string.tip_release_version) : getString(R.string.tip_version_beta)));
                    updateTipTitleTv.setText(getString(R.string.update_tip_title, resultBean.getVersionName(), String.valueOf(resultBean.getVersionCode())));
                    StringBuilder stringBuilder = new StringBuilder();
                    for (String content : resultBean.getUpdateContent()) {
                        stringBuilder.append(content);
                        stringBuilder.append("\n");
                    }
                    updateTipContentTv.setText(stringBuilder);
                    dismissBtn.setVisibility(cancelable ? View.VISIBLE : View.GONE);
                    break;
                }
            default:
                updateTip.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.header_update_tip_button_download:
                if (resultBean == null) {
                    if (updateState != STATE_NO_UPDATE) {
                        checkUpdate();
                    }
                    return;
                }
                VersionUtil.showDownloadDialog(this, resultBean);
                break;
            case R.id.header_update_tip_button_dismiss:
                resultBean = null;
                refreshUpdateTip();
                break;
        }
    }

    public boolean joinQQGroup(String key) {
        Intent intent = new Intent();
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}