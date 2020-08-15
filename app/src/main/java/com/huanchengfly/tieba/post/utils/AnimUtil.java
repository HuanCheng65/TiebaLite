package com.huanchengfly.tieba.post.utils;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.huanchengfly.tieba.post.R;

import com.huanchengfly.tieba.post.ui.overscroll.IOverScrollDecor;
import com.huanchengfly.tieba.post.ui.overscroll.IOverScrollUpdateListener;
import com.huanchengfly.tieba.post.ui.overscroll.VerticalOverScrollBounceEffectDecorator;
import com.huanchengfly.tieba.post.ui.overscroll.adapters.RecyclerViewOverScrollDecorAdapter;

public class AnimUtil {
    public static IOverScrollDecor setUpOverScroll(RecyclerView recyclerView) {
        IOverScrollDecor decor = new VerticalOverScrollBounceEffectDecorator(new RecyclerViewOverScrollDecorAdapter(recyclerView),
                1.25f,
                1f,
                -1.5f // Default is -2
        );
        decor.setOverScrollUpdateListener(new IOverScrollUpdateListener() {
            @Override
            public void onOverScrollUpdate(IOverScrollDecor decor, int state, float offset) {
                if (offset > 0) {

                }
            }
        });
        return decor;
    }

    public static ViewPropertyAnimator alphaIn(View view) {
        return alphaIn(view, 200);
    }

    public static ViewPropertyAnimator alphaIn(View view, int duration) {
        view.setAlpha(0F);
        view.setVisibility(View.VISIBLE);
        return view.animate()
                .alpha(1F)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(null);
    }

    public static ViewPropertyAnimator alphaOut(View view) {
        return alphaOut(view, 200);
    }

    public static ViewPropertyAnimator alphaOut(View view, int duration) {
        view.setAlpha(1F);
        view.setVisibility(View.VISIBLE);
        return view.animate()
                .alpha(0F)
                .setDuration(duration)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(null);
    }

    public static void rotate(ImageView imageView, int fromDegrees, int toDegrees) {
        Animation rotateAnimation = new RotateAnimation(fromDegrees, toDegrees, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(150);
        rotateAnimation.setRepeatCount(0);
        rotateAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        imageView.startAnimation(rotateAnimation);
    }

    public ObjectAnimator nope(View view) {
        int delta = view.getResources().getDimensionPixelOffset(R.dimen.spacing_medium);

        PropertyValuesHolder pvhTranslateX = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X,
                Keyframe.ofFloat(0f, 0),
                Keyframe.ofFloat(.10f, -delta),
                Keyframe.ofFloat(.26f, delta),
                Keyframe.ofFloat(.42f, -delta),
                Keyframe.ofFloat(.58f, delta),
                Keyframe.ofFloat(.74f, -delta),
                Keyframe.ofFloat(.90f, delta),
                Keyframe.ofFloat(1f, 0f)
        );

        return ObjectAnimator.ofPropertyValuesHolder(view, pvhTranslateX).
                setDuration(500);
    }
}
