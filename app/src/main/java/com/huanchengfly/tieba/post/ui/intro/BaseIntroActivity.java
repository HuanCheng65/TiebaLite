package com.huanchengfly.tieba.post.ui.intro;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.button.MaterialButton;
import com.huanchengfly.tieba.post.R;
import com.huanchengfly.tieba.post.ui.intro.adapters.ViewPagerAdapter;
import com.huanchengfly.tieba.post.ui.intro.fragments.BaseIntroFragment;
import com.huanchengfly.tieba.post.ui.intro.widgets.MyViewPager;

public abstract class BaseIntroActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    public static final String TAG = BaseIntroActivity.class.getSimpleName();

    private MaterialButton nextButton;
    private MaterialButton prevButton;
    private ViewPagerAdapter adapter;
    private MyViewPager myViewPager;

    protected @ColorInt
    int getColor() {
        return getResources().getColor(R.color.colorAccent);
    }

    public void setNextButtonEnabled(boolean enabled) {
        if (enabled) {
            nextButton.setVisibility(View.VISIBLE);
        } else {
            nextButton.setVisibility(View.INVISIBLE);
        }
    }

    private void refreshButtonState(int position) {
        BaseIntroFragment introFragment = (BaseIntroFragment) adapter.getItem(position);
        setNextButtonEnabled(introFragment.getDefaultNextButtonEnabled());
        if (introFragment.getNextButton() != null) {
            nextButton.setText(introFragment.getNextButton());
        } else {
            if (position + 1 >= adapter.getCount()) {
                nextButton.setText(R.string.button_next_last);
            } else {
                nextButton.setText(R.string.button_next_default);
            }
        }
        if (adapter.getCount() > 0 && position > 0) {
            prevButton.setVisibility(View.VISIBLE);
        } else {
            prevButton.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
        nextButton = findViewById(R.id.button_next);
        prevButton = findViewById(R.id.button_prev);
        nextButton.setOnClickListener(this);
        nextButton.setBackgroundColor(getColor());
        prevButton.setOnClickListener(this);
        myViewPager = findViewById(R.id.view_pager);
        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        myViewPager.addOnPageChangeListener(this);
        myViewPager.setCanScroll(false);
        onCreateIntro();
        myViewPager.setAdapter(adapter);
    }

    public ViewPagerAdapter getAdapter() {
        return adapter;
    }

    protected abstract void onCreateIntro();

    @Override
    public void onBackPressed() {
        if (myViewPager.getCurrentItem() - 1 >= 0) {
            prev();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_next:
                if (adapter.getCurrentFragmentPosition() + 1 >= adapter.getCount()) {
                    onFinish();
                    finish();
                    break;
                }
                if (((BaseIntroFragment) adapter.getItem(adapter.getCurrentFragmentPosition())).onNext()) {
                    break;
                }
                next();
                break;
            case R.id.button_prev:
                prev();
                break;
        }
    }

    protected void onFinish() {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        refreshButtonState(position);
        ((BaseIntroFragment) adapter.getItem(position)).onVisible();
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void next() {
        if (myViewPager.getCurrentItem() + 1 < adapter.getCount()) {
            refreshButtonState(adapter.getCurrentFragmentPosition() + 1);
            myViewPager.setCurrentItem(myViewPager.getCurrentItem() + 1, true);
        }
    }

    public void prev() {
        if (myViewPager.getCurrentItem() - 1 >= 0) {
            refreshButtonState(adapter.getCurrentFragmentPosition() - 1);
            myViewPager.setCurrentItem(myViewPager.getCurrentItem() - 1, true);
        }
    }
}
