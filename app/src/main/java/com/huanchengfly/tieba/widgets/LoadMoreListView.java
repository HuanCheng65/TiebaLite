package com.huanchengfly.tieba.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.widget.ContentLoadingProgressBar;

import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.interfaces.OnLoadMoreListener;

public class LoadMoreListView extends ListView implements AbsListView.OnScrollListener {
    private OnLoadMoreListener onLoadMoreListener;

    private View footerViewParent = null;
    private View headerViewParent = null;
    private LayoutInflater mInflater = null;

    private RelativeLayout headerView;
    private RelativeLayout footerView;
    private TextView nextTipTextView;
    private ContentLoadingProgressBar nextProgressBar;
    private TextView prevTipTextView;
    private ContentLoadingProgressBar prevProgressBar;

    private int mTotalItemCount = 0;
    private int mLastItemCount = 0;
    private int mFirstItemCount = 0;
    private boolean mIsLoadingNext = false;
    private boolean mIsLoadingPrev = false;
    private boolean showNextTipText = false;
    private boolean showPrevTipText = false;
    private boolean enableLoadNext = true;
    private boolean enableLoadPrev = true;

    public LoadMoreListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public LoadMoreListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    public void setEnableLoadNext(boolean enableLoadNext) {
        this.enableLoadNext = enableLoadNext;
        updateViewVisibility();
    }

    public void setEnableLoadPrev(boolean enableLoadPrev) {
        this.enableLoadPrev = enableLoadPrev;
        updateViewVisibility();
    }

    public boolean isLoadingNext() {
        return mIsLoadingNext;
    }

    public void setShowNextTipText(boolean showNextTipText) {
        this.showNextTipText = showNextTipText;
        updateViewVisibility();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        onLoadMoreListener = listener;
    }

    public void setNextTipText(String tipText) {
        nextTipTextView.setText(tipText);
        updateViewVisibility();
    }

    public void setPrevTipText(String tipText) {
        prevTipTextView.setText(tipText);
        updateViewVisibility();
    }

    public void setIsLoadingNext(boolean isLoading) {
        this.mIsLoadingNext = isLoading;
        updateViewVisibility();
    }

    private void updateViewVisibility() {
        if (enableLoadPrev && (mIsLoadingPrev | showPrevTipText))
            headerView.setVisibility(View.VISIBLE);
        else headerView.setVisibility(GONE);
        if (enableLoadNext && (mIsLoadingNext | showNextTipText))
            footerView.setVisibility(View.VISIBLE);
        else footerView.setVisibility(GONE);
        if (mIsLoadingPrev) prevProgressBar.setVisibility(View.VISIBLE);
        else prevProgressBar.setVisibility(GONE);
        if (mIsLoadingNext) nextProgressBar.setVisibility(View.VISIBLE);
        else nextProgressBar.setVisibility(GONE);
        if (showNextTipText) nextTipTextView.setVisibility(View.VISIBLE);
        else nextTipTextView.setVisibility(GONE);
        if (showPrevTipText) prevTipTextView.setVisibility(View.VISIBLE);
        else prevTipTextView.setVisibility(GONE);
    }

    private void initView(Context context) {
        mInflater = LayoutInflater.from(context);
        headerViewParent = mInflater.inflate(R.layout.layout_header, null, false);
        footerViewParent = mInflater.inflate(R.layout.layout_footer, null, false);

        headerView = headerViewParent.findViewById(R.id.header_view);
        footerView = footerViewParent.findViewById(R.id.footer_view);

        nextTipTextView = footerViewParent.findViewById(R.id.footer_tip);
        nextProgressBar = footerViewParent.findViewById(R.id.footer_progress);
        nextProgressBar.setVisibility(GONE);
        nextTipTextView.setVisibility(GONE);
        footerView.setVisibility(GONE);

        prevTipTextView = headerViewParent.findViewById(R.id.header_tip);
        prevProgressBar = headerViewParent.findViewById(R.id.header_progress);
        prevProgressBar.setVisibility(GONE);
        prevTipTextView.setVisibility(GONE);
        headerView.setVisibility(GONE);

        addHeaderView(headerViewParent);
        addFooterView(footerViewParent);
        setHeaderDividersEnabled(false);
        setFooterDividersEnabled(false);

        setOnScrollListener(this);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if ((mTotalItemCount - mLastItemCount) <= 1 && scrollState == SCROLL_STATE_IDLE) {
            if (!mIsLoadingNext && enableLoadNext) {
                mIsLoadingNext = true;
                updateViewVisibility();
                onLoadMoreListener.onLoadNext();
            }
        } else if (mFirstItemCount <= 1) {
            if (!mIsLoadingPrev && enableLoadPrev) {
                mIsLoadingPrev = true;
                updateViewVisibility();
                onLoadMoreListener.onLoadPrev();
            }
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        mTotalItemCount = totalItemCount;
        mLastItemCount = firstVisibleItem + visibleItemCount;
        mFirstItemCount = firstVisibleItem;
    }
}
