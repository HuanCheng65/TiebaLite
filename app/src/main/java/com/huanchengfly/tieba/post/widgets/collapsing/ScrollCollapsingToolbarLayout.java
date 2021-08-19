package com.huanchengfly.tieba.post.widgets.collapsing;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.math.MathUtils;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.internal.CollapsingTextHelper;
import com.google.android.material.internal.DescendantOffsetUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.huanchengfly.tieba.post.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@SuppressLint("RestrictedApi")
public class ScrollCollapsingToolbarLayout extends FrameLayout {

    private static final int DEF_STYLE_RES = R.style.Widget_ScrollCollapsingToolbar;
    private static final int DEFAULT_SCRIM_ANIMATION_DURATION = 600;
    @NonNull
    final CollapsingTextHelper collapsingTextHelper;
    private final Rect tmpRect = new Rect();
    @Nullable
    Drawable statusBarScrim;
    int currentOffset;
    @Nullable
    WindowInsetsCompat lastInsets;
    private boolean refreshToolbar = true;
    private int toolbarId;
    @Nullable
    private Toolbar toolbar;
    @Nullable
    private View toolbarDirectChild;
    private View dummyView;
    private int expandedMarginStart;
    private int expandedMarginTop;
    private int expandedMarginEnd;
    private int expandedMarginBottom;
    private boolean collapsingTitleEnabled;
    private boolean drawCollapsingTitle;
    @Nullable
    private Drawable contentScrim;
    private int scrimAlpha;
    private boolean scrimsAreShown;
    private ValueAnimator scrimAnimator;
    private long scrimAnimationDuration;
    private int scrimVisibleHeightTrigger = -1;
    private AppBarLayout.OnOffsetChangedListener onOffsetChangedListener;

    public ScrollCollapsingToolbarLayout(@NonNull Context context) {
        this(context, null);
    }

    public ScrollCollapsingToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollCollapsingToolbarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
        // Ensure we are using the correctly themed context rather than the context that was passed in.
        context = getContext();

        collapsingTextHelper = new CollapsingTextHelper(this);
        collapsingTextHelper.setTextSizeInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);

        TypedArray a =
                ThemeEnforcement.obtainStyledAttributes(
                        context,
                        attrs,
                        R.styleable.ScrollCollapsingToolbarLayout,
                        defStyleAttr,
                        DEF_STYLE_RES);

        collapsingTextHelper.setExpandedTextGravity(
                a.getInt(
                        R.styleable.ScrollCollapsingToolbarLayout_expandedTitleGravity,
                        GravityCompat.START | Gravity.BOTTOM));
        collapsingTextHelper.setCollapsedTextGravity(
                a.getInt(
                        R.styleable.ScrollCollapsingToolbarLayout_collapsedTitleGravity,
                        GravityCompat.START | Gravity.CENTER_VERTICAL));

        expandedMarginStart =
                expandedMarginTop =
                        expandedMarginEnd =
                                expandedMarginBottom =
                                        a.getDimensionPixelSize(
                                                R.styleable.ScrollCollapsingToolbarLayout_expandedTitleMargin, 0);

        if (a.hasValue(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleMarginStart)) {
            expandedMarginStart =
                    a.getDimensionPixelSize(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleMarginStart, 0);
        }
        if (a.hasValue(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleMarginEnd)) {
            expandedMarginEnd =
                    a.getDimensionPixelSize(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleMarginEnd, 0);
        }
        if (a.hasValue(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleMarginTop)) {
            expandedMarginTop =
                    a.getDimensionPixelSize(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleMarginTop, 0);
        }
        if (a.hasValue(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleMarginBottom)) {
            expandedMarginBottom =
                    a.getDimensionPixelSize(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleMarginBottom, 0);
        }

        collapsingTitleEnabled = a.getBoolean(R.styleable.ScrollCollapsingToolbarLayout_titleEnabled, true);
        setTitle(a.getText(R.styleable.ScrollCollapsingToolbarLayout_title));

        // First load the default text appearances
        collapsingTextHelper.setExpandedTextAppearance(
                R.style.TextAppearance_Design_CollapsingToolbar_Expanded);
        collapsingTextHelper.setCollapsedTextAppearance(
                androidx.appcompat.R.style.TextAppearance_AppCompat_Widget_ActionBar_Title);

        // Now overlay any custom text appearances
        if (a.hasValue(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleTextAppearance)) {
            collapsingTextHelper.setExpandedTextAppearance(
                    a.getResourceId(R.styleable.ScrollCollapsingToolbarLayout_expandedTitleTextAppearance, 0));
        }
        if (a.hasValue(R.styleable.ScrollCollapsingToolbarLayout_collapsedTitleTextAppearance)) {
            collapsingTextHelper.setCollapsedTextAppearance(
                    a.getResourceId(R.styleable.ScrollCollapsingToolbarLayout_collapsedTitleTextAppearance, 0));
        }

        scrimVisibleHeightTrigger =
                a.getDimensionPixelSize(R.styleable.ScrollCollapsingToolbarLayout_scrimVisibleHeightTrigger, -1);

        if (a.hasValue(R.styleable.ScrollCollapsingToolbarLayout_maxLines)) {
            collapsingTextHelper.setMaxLines(a.getInt(R.styleable.ScrollCollapsingToolbarLayout_maxLines, 1));
        }

        scrimAnimationDuration =
                a.getInt(
                        R.styleable.ScrollCollapsingToolbarLayout_scrimAnimationDuration,
                        DEFAULT_SCRIM_ANIMATION_DURATION);

        setContentScrim(a.getDrawable(R.styleable.ScrollCollapsingToolbarLayout_contentScrim));
        setStatusBarScrim(a.getDrawable(R.styleable.ScrollCollapsingToolbarLayout_statusBarScrim));

        toolbarId = a.getResourceId(R.styleable.ScrollCollapsingToolbarLayout_toolbarId, -1);

        a.recycle();

        setWillNotDraw(false);

        ViewCompat.setOnApplyWindowInsetsListener(
                this,
                new androidx.core.view.OnApplyWindowInsetsListener() {
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(
                            View v, @NonNull WindowInsetsCompat insets) {
                        return onWindowInsetChanged(insets);
                    }
                });
    }

    private static int getHeightWithMargins(@NonNull final View view) {
        final ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp instanceof MarginLayoutParams) {
            final MarginLayoutParams mlp = (MarginLayoutParams) lp;
            return view.getHeight() + mlp.topMargin + mlp.bottomMargin;
        }
        return view.getHeight();
    }

    @NonNull
    static ViewOffsetHelper getViewOffsetHelper(@NonNull View view) {
        ViewOffsetHelper offsetHelper = (ViewOffsetHelper) view.getTag(R.id.view_offset_helper);
        if (offsetHelper == null) {
            offsetHelper = new ViewOffsetHelper(view);
            view.setTag(R.id.view_offset_helper, offsetHelper);
        }
        return offsetHelper;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        // Add an OnOffsetChangedListener if possible
        final ViewParent parent = getParent();
        if (parent instanceof AppBarLayout) {
            // Copy over from the ABL whether we should fit system windows
            ViewCompat.setFitsSystemWindows(this, ViewCompat.getFitsSystemWindows((View) parent));

            if (onOffsetChangedListener == null) {
                onOffsetChangedListener = new OffsetUpdateListener();
            }
            ((AppBarLayout) parent).addOnOffsetChangedListener(onOffsetChangedListener);

            // We're attached, so lets request an inset dispatch
            ViewCompat.requestApplyInsets(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // Remove our OnOffsetChangedListener if possible and it exists
        final ViewParent parent = getParent();
        if (onOffsetChangedListener != null && parent instanceof AppBarLayout) {
            ((AppBarLayout) parent).removeOnOffsetChangedListener(onOffsetChangedListener);
        }

        super.onDetachedFromWindow();
    }

    WindowInsetsCompat onWindowInsetChanged(@NonNull final WindowInsetsCompat insets) {
        WindowInsetsCompat newInsets = null;

        if (ViewCompat.getFitsSystemWindows(this)) {
            // If we're set to fit system windows, keep the insets
            newInsets = insets;
        }

        // If our insets have changed, keep them and invalidate the scroll ranges...
        if (!ObjectsCompat.equals(lastInsets, newInsets)) {
            lastInsets = newInsets;
            requestLayout();
        }

        // Consume the insets. This is done so that child views with fitSystemWindows=true do not
        // get the default padding functionality from View
        return insets.consumeSystemWindowInsets();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        // If we don't have a toolbar, the scrim will be not be drawn in drawChild() below.
        // Instead, we draw it here, before our collapsing text.
        ensureToolbar();
        if (toolbar == null && contentScrim != null && scrimAlpha > 0) {
            contentScrim.mutate().setAlpha(scrimAlpha);
            contentScrim.draw(canvas);
        }

        // Let the collapsing text helper draw its text
        if (collapsingTitleEnabled && drawCollapsingTitle) {
            collapsingTextHelper.draw(canvas);
        }

        // Now draw the status bar scrim
        if (statusBarScrim != null && scrimAlpha > 0) {
            final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
            if (topInset > 0) {
                statusBarScrim.setBounds(0, -currentOffset, getWidth(), topInset - currentOffset);
                statusBarScrim.mutate().setAlpha(scrimAlpha);
                statusBarScrim.draw(canvas);
            }
        }
    }

    @Override
    protected boolean drawChild(Canvas canvas, View child, long drawingTime) {
        // This is a little weird. Our scrim needs to be behind the Toolbar (if it is present),
        // but in front of any other children which are behind it. To do this we intercept the
        // drawChild() call, and draw our scrim just before the Toolbar is drawn
        boolean invalidated = false;
        if (contentScrim != null && scrimAlpha > 0 && isToolbarChild(child)) {
            contentScrim.mutate().setAlpha(scrimAlpha);
            contentScrim.draw(canvas);
            invalidated = true;
        }
        return super.drawChild(canvas, child, drawingTime) || invalidated;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (contentScrim != null) {
            contentScrim.setBounds(0, 0, w, h);
        }
    }

    private void ensureToolbar() {
        if (!refreshToolbar) {
            return;
        }

        // First clear out the current Toolbar
        this.toolbar = null;
        toolbarDirectChild = null;

        if (toolbarId != -1) {
            // If we have an ID set, try and find it and it's direct parent to us
            this.toolbar = findViewById(toolbarId);
            if (this.toolbar != null) {
                toolbarDirectChild = findDirectChild(this.toolbar);
            }
        }

        if (this.toolbar == null) {
            // If we don't have an ID, or couldn't find a Toolbar with the correct ID, try and find
            // one from our direct children
            Toolbar toolbar = null;
            for (int i = 0, count = getChildCount(); i < count; i++) {
                final View child = getChildAt(i);
                if (child instanceof Toolbar) {
                    toolbar = (Toolbar) child;
                    break;
                }
            }
            this.toolbar = toolbar;
        }

        updateDummyView();
        refreshToolbar = false;
    }

    private boolean isToolbarChild(View child) {
        return (toolbarDirectChild == null || toolbarDirectChild == this)
                ? child == toolbar
                : child == toolbarDirectChild;
    }

    /**
     * Returns the direct child of this layout, which itself is the ancestor of the given view.
     */
    @NonNull
    private View findDirectChild(@NonNull final View descendant) {
        View directChild = descendant;
        for (ViewParent p = descendant.getParent(); p != this && p != null; p = p.getParent()) {
            if (p instanceof View) {
                directChild = (View) p;
            }
        }
        return directChild;
    }

    private void updateDummyView() {
        if (!collapsingTitleEnabled && dummyView != null) {
            // If we have a dummy view and we have our title disabled, remove it from its parent
            final ViewParent parent = dummyView.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(dummyView);
            }
        }
        if (collapsingTitleEnabled && toolbar != null) {
            if (dummyView == null) {
                dummyView = new View(getContext());
            }
            if (dummyView.getParent() == null) {
                toolbar.addView(dummyView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ensureToolbar();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final int mode = MeasureSpec.getMode(heightMeasureSpec);
        final int topInset = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
        if (mode == MeasureSpec.UNSPECIFIED && topInset > 0) {
            // If we have a top inset and we're set to wrap_content height we need to make sure
            // we add the top inset to our height, therefore we re-measure
            heightMeasureSpec =
                    MeasureSpec.makeMeasureSpec(getMeasuredHeight() + topInset, MeasureSpec.EXACTLY);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (lastInsets != null) {
            // Shift down any views which are not set to fit system windows
            final int insetTop = lastInsets.getSystemWindowInsetTop();
            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                if (!ViewCompat.getFitsSystemWindows(child)) {
                    if (child.getTop() < insetTop) {
                        // If the child isn't set to fit system windows but is drawing within
                        // the inset offset it down
                        ViewCompat.offsetTopAndBottom(child, insetTop);
                    }
                }
            }
        }

        // Update our child view offset helpers so that they track the correct layout coordinates
        for (int i = 0, z = getChildCount(); i < z; i++) {
            getViewOffsetHelper(getChildAt(i)).onViewLayout();
        }

        // Update the collapsed bounds by getting its transformed bounds
        if (collapsingTitleEnabled && dummyView != null) {
            // We only draw the title if the dummy view is being displayed (Toolbar removes
            // views if there is no space)
            drawCollapsingTitle =
                    ViewCompat.isAttachedToWindow(dummyView) && dummyView.getVisibility() == VISIBLE;

            if (drawCollapsingTitle) {
                final boolean isRtl =
                        ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;

                // Update the collapsed bounds
                final int maxOffset =
                        getMaxOffsetForPinChild(toolbarDirectChild != null ? toolbarDirectChild : toolbar);
                DescendantOffsetUtils.getDescendantRect(this, dummyView, tmpRect);
                collapsingTextHelper.setCollapsedBounds(
                        tmpRect.left + (isRtl ? toolbar.getTitleMarginEnd() : toolbar.getTitleMarginStart()),
                        tmpRect.top + maxOffset + toolbar.getTitleMarginTop(),
                        tmpRect.right - (isRtl ? toolbar.getTitleMarginStart() : toolbar.getTitleMarginEnd()),
                        tmpRect.bottom + maxOffset - toolbar.getTitleMarginBottom());

                // Update the expanded bounds
                collapsingTextHelper.setExpandedBounds(
                        isRtl ? expandedMarginEnd : expandedMarginStart,
                        tmpRect.top + expandedMarginTop,
                        right - left - (isRtl ? expandedMarginStart : expandedMarginEnd),
                        bottom - top - expandedMarginBottom);
                // Now recalculate using the new bounds
                collapsingTextHelper.recalculate();
            }
        }

        // Set our minimum height to enable proper AppBarLayout collapsing
        if (toolbar != null) {
            if (collapsingTitleEnabled && TextUtils.isEmpty(collapsingTextHelper.getText())) {
                // If we do not currently have a title, try and grab it from the Toolbar
                setTitle(toolbar.getTitle());
            }
            if (toolbarDirectChild == null || toolbarDirectChild == this) {
                setMinimumHeight(getHeightWithMargins(toolbar));
            } else {
                setMinimumHeight(getHeightWithMargins(toolbarDirectChild));
            }
        }

        updateScrollChild(left, top, right, bottom);

        updateScrimVisibility();

        // Apply any view offsets, this should be done at the very end of layout
        for (int i = 0, z = getChildCount(); i < z; i++) {
            getViewOffsetHelper(getChildAt(i)).applyOffsets();
        }
    }

    private void updateScrollChild(int left, int top, int right, int bottom) {
        int collapsedLeft = left;
        int collapsedTop = top;
        int collapsedRight = right;
        int collapsedBottom = bottom;

        if (toolbar != null) {
            collapsedLeft = tmpRect.left;
            collapsedTop = tmpRect.top;
            collapsedRight = tmpRect.right;
            collapsedBottom = tmpRect.bottom;
        }

        for (int i = 0, z = getChildCount(); i < z; i++) {
            View v = getChildAt(i);
            LayoutParams lp = (LayoutParams) v.getLayoutParams();
            if (lp.collapseMode == LayoutParams.COLLAPSE_MODE_SCROLL) {
                lp.setCollapsedBounds(collapsedLeft, collapsedTop,
                        collapsedRight, collapsedBottom);
                lp.setExpandedBounds(v.getLeft(), v.getTop(),
                        v.getRight(), v.getBottom());
                lp.recalculate();
            }
        }
    }

    /**
     * Returns the title currently being displayed by this view. If the title is not enabled, then
     * this will return {@code null}.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_title
     */
    @Nullable
    public CharSequence getTitle() {
        return collapsingTitleEnabled ? collapsingTextHelper.getText() : null;
    }

    /**
     * Sets the title to be displayed by this view, if enabled.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_title
     * @see #setTitleEnabled(boolean)
     * @see #getTitle()
     */
    public void setTitle(@Nullable CharSequence title) {
        collapsingTextHelper.setText(title);
        updateContentDescriptionFromTitle();
    }

    /**
     * Returns whether this view is currently displaying its own title.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_titleEnabled
     * @see #setTitleEnabled(boolean)
     */
    public boolean isTitleEnabled() {
        return collapsingTitleEnabled;
    }

    /**
     * Sets whether this view should display its own title.
     *
     * <p>The title displayed by this view will shrink and grow based on the scroll offset.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_titleEnabled
     * @see #setTitle(CharSequence)
     * @see #isTitleEnabled()
     */
    public void setTitleEnabled(boolean enabled) {
        if (enabled != collapsingTitleEnabled) {
            collapsingTitleEnabled = enabled;
            updateContentDescriptionFromTitle();
            updateDummyView();
            requestLayout();
        }
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
     * vertical scroll may overwrite this value. Any visibility change will be animated if this view
     * has already been laid out.
     *
     * @param shown whether the scrims should be shown
     * @see #getStatusBarScrim()
     * @see #getContentScrim()
     */
    public void setScrimsShown(boolean shown) {
        setScrimsShown(shown, ViewCompat.isLaidOut(this) && !isInEditMode());
    }

    /**
     * Set whether the content scrim and/or status bar scrim should be shown or not. Any change in the
     * vertical scroll may overwrite this value.
     *
     * @param shown   whether the scrims should be shown
     * @param animate whether to animate the visibility change
     * @see #getStatusBarScrim()
     * @see #getContentScrim()
     */
    public void setScrimsShown(boolean shown, boolean animate) {
        if (scrimsAreShown != shown) {
            if (animate) {
                animateScrim(shown ? 0xFF : 0x0);
            } else {
                setScrimAlpha(shown ? 0xFF : 0x0);
            }
            scrimsAreShown = shown;
        }
    }

    private void animateScrim(int targetAlpha) {
        ensureToolbar();
        if (scrimAnimator == null) {
            scrimAnimator = new ValueAnimator();
            scrimAnimator.setDuration(scrimAnimationDuration);
            scrimAnimator.setInterpolator(
                    targetAlpha > scrimAlpha
                            ? AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR
                            : AnimationUtils.LINEAR_OUT_SLOW_IN_INTERPOLATOR);
            scrimAnimator.addUpdateListener(
                    new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(@NonNull ValueAnimator animator) {
                            setScrimAlpha((int) animator.getAnimatedValue());
                        }
                    });
        } else if (scrimAnimator.isRunning()) {
            scrimAnimator.cancel();
        }

        scrimAnimator.setIntValues(scrimAlpha, targetAlpha);
        scrimAnimator.start();
    }

    int getScrimAlpha() {
        return scrimAlpha;
    }

    void setScrimAlpha(int alpha) {
        if (alpha != scrimAlpha) {
            final Drawable contentScrim = this.contentScrim;
            if (contentScrim != null && toolbar != null) {
                ViewCompat.postInvalidateOnAnimation(toolbar);
            }
            scrimAlpha = alpha;
            ViewCompat.postInvalidateOnAnimation(ScrollCollapsingToolbarLayout.this);
        }
    }

    /**
     * Set the color to use for the content scrim.
     *
     * @param color the color to display
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see #getContentScrim()
     */
    public void setContentScrimColor(@ColorInt int color) {
        setContentScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the content scrim from resources.
     *
     * @param resId drawable resource id
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see #getContentScrim()
     */
    public void setContentScrimResource(@DrawableRes int resId) {
        setContentScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Returns the drawable which is used for the foreground scrim.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see #setContentScrim(Drawable)
     */
    @Nullable
    public Drawable getContentScrim() {
        return contentScrim;
    }

    /**
     * Set the drawable to use for the content scrim from resources. Providing null will disable the
     * scrim functionality.
     *
     * @param drawable the drawable to display
     * @attr ref R.styleable#CollapsingToolbarLayout_contentScrim
     * @see #getContentScrim()
     */
    public void setContentScrim(@Nullable Drawable drawable) {
        if (contentScrim != drawable) {
            if (contentScrim != null) {
                contentScrim.setCallback(null);
            }
            contentScrim = drawable != null ? drawable.mutate() : null;
            if (contentScrim != null) {
                contentScrim.setBounds(0, 0, getWidth(), getHeight());
                contentScrim.setCallback(this);
                contentScrim.setAlpha(scrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int[] state = getDrawableState();
        boolean changed = false;

        Drawable d = statusBarScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        d = contentScrim;
        if (d != null && d.isStateful()) {
            changed |= d.setState(state);
        }
        if (collapsingTextHelper != null) {
            changed |= collapsingTextHelper.setState(state);
        }

        if (changed) {
            invalidate();
        }
    }

    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return super.verifyDrawable(who) || who == contentScrim || who == statusBarScrim;
    }

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);

        final boolean visible = visibility == VISIBLE;
        if (statusBarScrim != null && statusBarScrim.isVisible() != visible) {
            statusBarScrim.setVisible(visible, false);
        }
        if (contentScrim != null && contentScrim.isVisible() != visible) {
            contentScrim.setVisible(visible, false);
        }
    }

    /**
     * Set the color to use for the status bar scrim.
     *
     * <p>This scrim is only shown when we have been given a top system inset.
     *
     * @param color the color to display
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimColor(@ColorInt int color) {
        setStatusBarScrim(new ColorDrawable(color));
    }

    /**
     * Set the drawable to use for the status bar scrim from resources.
     *
     * @param resId drawable resource id
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrimResource(@DrawableRes int resId) {
        setStatusBarScrim(ContextCompat.getDrawable(getContext(), resId));
    }

    /**
     * Returns the drawable which is used for the status bar scrim.
     *
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see #setStatusBarScrim(Drawable)
     */
    @Nullable
    public Drawable getStatusBarScrim() {
        return statusBarScrim;
    }

    /**
     * Set the drawable to use for the status bar scrim from resources. Providing null will disable
     * the scrim functionality.
     *
     * <p>This scrim is only shown when we have been given a top system inset.
     *
     * @param drawable the drawable to display
     * @attr ref R.styleable#CollapsingToolbarLayout_statusBarScrim
     * @see #getStatusBarScrim()
     */
    public void setStatusBarScrim(@Nullable Drawable drawable) {
        if (statusBarScrim != drawable) {
            if (statusBarScrim != null) {
                statusBarScrim.setCallback(null);
            }
            statusBarScrim = drawable != null ? drawable.mutate() : null;
            if (statusBarScrim != null) {
                if (statusBarScrim.isStateful()) {
                    statusBarScrim.setState(getDrawableState());
                }
                DrawableCompat.setLayoutDirection(statusBarScrim, ViewCompat.getLayoutDirection(this));
                statusBarScrim.setVisible(getVisibility() == VISIBLE, false);
                statusBarScrim.setCallback(this);
                statusBarScrim.setAlpha(scrimAlpha);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /**
     * Sets the text color and size for the collapsed title from the specified TextAppearance
     * resource.
     *
     * @attr ref
     * com.google.android.material.R.styleable#CollapsingToolbarLayout_collapsedTitleTextAppearance
     */
    public void setCollapsedTitleTextAppearance(@StyleRes int resId) {
        collapsingTextHelper.setCollapsedTextAppearance(resId);
    }

    /**
     * Sets the text color of the collapsed title.
     *
     * @param color The new text color in ARGB format
     */
    public void setCollapsedTitleTextColor(@ColorInt int color) {
        setCollapsedTitleTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Sets the text colors of the collapsed title.
     *
     * @param colors ColorStateList containing the new text colors
     */
    public void setCollapsedTitleTextColor(@NonNull ColorStateList colors) {
        collapsingTextHelper.setCollapsedTextColor(colors);
    }

    /**
     * Returns the horizontal and vertical alignment for title when collapsed.
     *
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_collapsedTitleGravity
     */
    public int getCollapsedTitleGravity() {
        return collapsingTextHelper.getCollapsedTextGravity();
    }

    /**
     * Sets the horizontal alignment of the collapsed title and the vertical gravity that will be used
     * when there is extra space in the collapsed bounds beyond what is required for the title itself.
     *
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_collapsedTitleGravity
     */
    public void setCollapsedTitleGravity(int gravity) {
        collapsingTextHelper.setCollapsedTextGravity(gravity);
    }

    /**
     * Sets the text color and size for the expanded title from the specified TextAppearance resource.
     *
     * @attr ref
     * com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleTextAppearance
     */
    public void setExpandedTitleTextAppearance(@StyleRes int resId) {
        collapsingTextHelper.setExpandedTextAppearance(resId);
    }

    /**
     * Sets the text color of the expanded title.
     *
     * @param color The new text color in ARGB format
     */
    public void setExpandedTitleColor(@ColorInt int color) {
        setExpandedTitleTextColor(ColorStateList.valueOf(color));
    }

    /**
     * Sets the text colors of the expanded title.
     *
     * @param colors ColorStateList containing the new text colors
     */
    public void setExpandedTitleTextColor(@NonNull ColorStateList colors) {
        collapsingTextHelper.setExpandedTextColor(colors);
    }

    /**
     * Returns the horizontal and vertical alignment for title when expanded.
     *
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleGravity
     */
    public int getExpandedTitleGravity() {
        return collapsingTextHelper.getExpandedTextGravity();
    }

    /**
     * Sets the horizontal alignment of the expanded title and the vertical gravity that will be used
     * when there is extra space in the expanded bounds beyond what is required for the title itself.
     *
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleGravity
     */
    public void setExpandedTitleGravity(int gravity) {
        collapsingTextHelper.setExpandedTextGravity(gravity);
    }

    /**
     * Returns the typeface used for the collapsed title.
     */
    @NonNull
    public Typeface getCollapsedTitleTypeface() {
        return collapsingTextHelper.getCollapsedTypeface();
    }

    /**
     * Set the typeface to use for the collapsed title.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setCollapsedTitleTypeface(@Nullable Typeface typeface) {
        collapsingTextHelper.setCollapsedTypeface(typeface);
    }

    /**
     * Returns the typeface used for the expanded title.
     */
    @NonNull
    public Typeface getExpandedTitleTypeface() {
        return collapsingTextHelper.getExpandedTypeface();
    }

    /**
     * Set the typeface to use for the expanded title.
     *
     * @param typeface typeface to use, or {@code null} to use the default.
     */
    public void setExpandedTitleTypeface(@Nullable Typeface typeface) {
        collapsingTextHelper.setExpandedTypeface(typeface);
    }

    /**
     * Sets the expanded title margins.
     *
     * @param start  the starting title margin in pixels
     * @param top    the top title margin in pixels
     * @param end    the ending title margin in pixels
     * @param bottom the bottom title margin in pixels
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMargin
     * @see #getExpandedTitleMarginStart()
     * @see #getExpandedTitleMarginTop()
     * @see #getExpandedTitleMarginEnd()
     * @see #getExpandedTitleMarginBottom()
     */
    public void setExpandedTitleMargin(int start, int top, int end, int bottom) {
        expandedMarginStart = start;
        expandedMarginTop = top;
        expandedMarginEnd = end;
        expandedMarginBottom = bottom;
        requestLayout();
    }

    /**
     * @return the starting expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginStart
     * @see #setExpandedTitleMarginStart(int)
     */
    public int getExpandedTitleMarginStart() {
        return expandedMarginStart;
    }

    /**
     * Sets the starting expanded title margin in pixels.
     *
     * @param margin the starting title margin in pixels
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginStart
     * @see #getExpandedTitleMarginStart()
     */
    public void setExpandedTitleMarginStart(int margin) {
        expandedMarginStart = margin;
        requestLayout();
    }

    /**
     * @return the top expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginTop
     * @see #setExpandedTitleMarginTop(int)
     */
    public int getExpandedTitleMarginTop() {
        return expandedMarginTop;
    }

    /**
     * Sets the top expanded title margin in pixels.
     *
     * @param margin the top title margin in pixels
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginTop
     * @see #getExpandedTitleMarginTop()
     */
    public void setExpandedTitleMarginTop(int margin) {
        expandedMarginTop = margin;
        requestLayout();
    }

    /**
     * @return the ending expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginEnd
     * @see #setExpandedTitleMarginEnd(int)
     */
    public int getExpandedTitleMarginEnd() {
        return expandedMarginEnd;
    }

    /**
     * Sets the ending expanded title margin in pixels.
     *
     * @param margin the ending title margin in pixels
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginEnd
     * @see #getExpandedTitleMarginEnd()
     */
    public void setExpandedTitleMarginEnd(int margin) {
        expandedMarginEnd = margin;
        requestLayout();
    }

    /**
     * @return the bottom expanded title margin in pixels
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginBottom
     * @see #setExpandedTitleMarginBottom(int)
     */
    public int getExpandedTitleMarginBottom() {
        return expandedMarginBottom;
    }

    /**
     * Sets the bottom expanded title margin in pixels.
     *
     * @param margin the bottom title margin in pixels
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_expandedTitleMarginBottom
     * @see #getExpandedTitleMarginBottom()
     */
    public void setExpandedTitleMarginBottom(int margin) {
        expandedMarginBottom = margin;
        requestLayout();
    }

    /**
     * Gets the maximum number of lines to display in the expanded state.
     * Experimental Feature.
     */
    public int getMaxLines() {
        return collapsingTextHelper.getMaxLines();
    }

    /**
     * Sets the maximum number of lines to display in the expanded state.
     * Experimental Feature.
     */
    public void setMaxLines(int maxLines) {
        collapsingTextHelper.setMaxLines(maxLines);
    }

    /**
     * Returns the amount of visible height in pixels used to define when to trigger a scrim
     * visibility change.
     *
     * @see #setScrimVisibleHeightTrigger(int)
     */
    public int getScrimVisibleHeightTrigger() {
        if (scrimVisibleHeightTrigger >= 0) {
            // If we have one explicitly set, return it
            return scrimVisibleHeightTrigger;
        }

        // Otherwise we'll use the default computed value
        final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

        final int minHeight = ViewCompat.getMinimumHeight(this);
        if (minHeight > 0) {
            // If we have a minHeight set, lets use 2 * minHeight (capped at our height)
            return Math.min((minHeight * 2) + insetTop, getHeight());
        }

        // If we reach here then we don't have a min height set. Instead we'll take a
        // guess at 1/3 of our height being visible
        return getHeight() / 3;
    }

    /**
     * Set the amount of visible height in pixels used to define when to trigger a scrim visibility
     * change.
     *
     * <p>If the visible height of this view is less than the given value, the scrims will be made
     * visible, otherwise they are hidden.
     *
     * @param height value in pixels used to define when to trigger a scrim visibility change
     * @attr ref
     * com.google.android.material.R.styleable#CollapsingToolbarLayout_scrimVisibleHeightTrigger
     */
    public void setScrimVisibleHeightTrigger(@IntRange(from = 0) final int height) {
        if (scrimVisibleHeightTrigger != height) {
            scrimVisibleHeightTrigger = height;
            // Update the scrim visibility
            updateScrimVisibility();
        }
    }

    /**
     * Returns the duration in milliseconds used for scrim visibility animations.
     */
    public long getScrimAnimationDuration() {
        return scrimAnimationDuration;
    }

    /**
     * Set the duration used for scrim visibility animations.
     *
     * @param duration the duration to use in milliseconds
     * @attr ref com.google.android.material.R.styleable#CollapsingToolbarLayout_scrimAnimationDuration
     */
    public void setScrimAnimationDuration(@IntRange(from = 0) final long duration) {
        scrimAnimationDuration = duration;
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    public FrameLayout.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected FrameLayout.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }

    /**
     * Show or hide the scrims if needed
     */
    final void updateScrimVisibility() {
        if (contentScrim != null || statusBarScrim != null) {
            setScrimsShown(getHeight() + currentOffset < getScrimVisibleHeightTrigger());
        }
    }

    final int getMaxOffsetForPinChild(@NonNull View child) {
        final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);
        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        return getHeight() - offsetHelper.getLayoutTop() - child.getHeight() - lp.bottomMargin;
    }

    private void updateContentDescriptionFromTitle() {
        // Set this layout's contentDescription to match the title if it's shown by CollapsingTextHelper
        setContentDescription(getTitle());
    }

    public static class LayoutParams extends FrameLayout.LayoutParams {

        /**
         * The view will act as normal with no collapsing behavior.
         */
        public static final int COLLAPSE_MODE_OFF = 0;
        /**
         * The view will pin in place until it reaches the bottom of the {@link
         * CollapsingToolbarLayout}.
         */
        public static final int COLLAPSE_MODE_PIN = 1;
        /**
         * The view will scroll in a parallax fashion. See {@link #setParallaxMultiplier(float)} to
         * change the multiplier used.
         */
        public static final int COLLAPSE_MODE_PARALLAX = 2;
        public static final int COLLAPSE_MODE_SCROLL = 3;
        private static final float DEFAULT_PARALLAX_MULTIPLIER = 0.5f;
        final Rect collapsedRect = new Rect();
        final Rect expandedRect = new Rect();
        int collapseMode = COLLAPSE_MODE_OFF;
        float parallaxMult = DEFAULT_PARALLAX_MULTIPLIER;
        private int mCollapsedGravity;
        private int mCollapsedMarginStart;
        private int mCollapsedMarginTop;
        private int mCollapsedMarginEnd;
        private int mCollapsedMarginBottom;
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);

            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.ScrollCollapsingToolbarLayout_Layout);
            collapseMode =
                    a.getInt(
                            R.styleable.ScrollCollapsingToolbarLayout_Layout_collapseMode, COLLAPSE_MODE_OFF);
            setParallaxMultiplier(
                    a.getFloat(
                            R.styleable.ScrollCollapsingToolbarLayout_Layout_collapseParallaxMultiplier,
                            DEFAULT_PARALLAX_MULTIPLIER));

            mCollapsedGravity = a.getInt(R.styleable.
                            ScrollCollapsingToolbarLayout_Layout_collapsedGravity,
                    Gravity.START | Gravity.CENTER_VERTICAL);

            int margin = a.getDimensionPixelSize(R.styleable.
                    ScrollCollapsingToolbarLayout_Layout_collapsedMargin, 0);
            mCollapsedMarginStart = a.getDimensionPixelSize(R.styleable.
                    ScrollCollapsingToolbarLayout_Layout_collapsedMarginStart, margin);
            mCollapsedMarginTop = a.getDimensionPixelSize(R.styleable.
                    ScrollCollapsingToolbarLayout_Layout_collapsedMarginTop, margin);
            mCollapsedMarginEnd = a.getDimensionPixelSize(R.styleable.
                    ScrollCollapsingToolbarLayout_Layout_collapsedMarginEnd, margin);
            mCollapsedMarginBottom = a.getDimensionPixelSize(R.styleable.
                    ScrollCollapsingToolbarLayout_Layout_collapsedMarginBottom, margin);

            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height, gravity);
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams p) {
            super(p);
        }

        public LayoutParams(@NonNull MarginLayoutParams source) {
            super(source);
        }

        @RequiresApi(19)
        public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
            // The copy constructor called here only exists on API 19+.
            super(source);
        }

        /**
         * Returns the requested collapse mode.
         *
         * @return the current mode. One of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN} or
         * {@link #COLLAPSE_MODE_PARALLAX}.
         */
        @CollapseMode
        public int getCollapseMode() {
            return collapseMode;
        }

        /**
         * Set the collapse mode.
         *
         * @param collapseMode one of {@link #COLLAPSE_MODE_OFF}, {@link #COLLAPSE_MODE_PIN} or {@link
         *                     #COLLAPSE_MODE_PARALLAX}.
         */
        public void setCollapseMode(@CollapseMode int collapseMode) {
            this.collapseMode = collapseMode;
        }

        /**
         * Returns the parallax scroll multiplier used in conjunction with {@link
         * #COLLAPSE_MODE_PARALLAX}.
         *
         * @see #setParallaxMultiplier(float)
         */
        public float getParallaxMultiplier() {
            return parallaxMult;
        }

        /**
         * Set the parallax scroll multiplier used in conjunction with {@link #COLLAPSE_MODE_PARALLAX}.
         * A value of {@code 0.0} indicates no movement at all, {@code 1.0f} indicates normal scroll
         * movement.
         *
         * @param multiplier the multiplier.
         * @see #getParallaxMultiplier()
         */
        public void setParallaxMultiplier(float multiplier) {
            parallaxMult = multiplier;
        }

        void setCollapsedBounds(int l, int t, int r, int b) {
            collapsedRect.left = l;
            collapsedRect.top = t;
            collapsedRect.right = r;
            collapsedRect.bottom = b;
        }

        void setExpandedBounds(int l, int t, int r, int b) {
            expandedRect.left = l;
            expandedRect.top = t;
            expandedRect.right = r;
            expandedRect.bottom = b;
        }

        @SuppressLint("RtlHardcoded")
        void recalculate() {
            boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;

            final int collapsedAbsGravity = Gravity.getAbsoluteGravity(
                    mCollapsedGravity, getLayoutDirection());

            switch (collapsedAbsGravity & Gravity.VERTICAL_GRAVITY_MASK) {
                case Gravity.BOTTOM:
                    collapsedRect.bottom -= mCollapsedMarginBottom;
                    collapsedRect.top = collapsedRect.bottom - height;
                    break;
                case Gravity.TOP:
                    collapsedRect.top += mCollapsedMarginTop;
                    collapsedRect.bottom = collapsedRect.top + height;
                    break;
                case Gravity.CENTER_VERTICAL:
                default:
                    collapsedRect.top = (collapsedRect.top + collapsedRect.bottom) / 2
                            - height / 2;
                    collapsedRect.bottom = collapsedRect.top + height;
                    break;
            }

            switch (collapsedAbsGravity & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
                case Gravity.RIGHT:
                    collapsedRect.right -= isRtl ? mCollapsedMarginStart : mCollapsedMarginEnd;
                    collapsedRect.left = collapsedRect.right - width;
                    break;
                case Gravity.CENTER_HORIZONTAL:
                    collapsedRect.left = (collapsedRect.left + collapsedRect.right) / 2
                            - width / 2;
                    collapsedRect.right = collapsedRect.left + width;
                    break;
                case Gravity.LEFT:
                default:
                    collapsedRect.left += isRtl ? mCollapsedMarginEnd : mCollapsedMarginStart;
                    collapsedRect.right = collapsedRect.left + width;
                    break;
            }
        }

        @IntDef({COLLAPSE_MODE_OFF, COLLAPSE_MODE_PIN, COLLAPSE_MODE_PARALLAX, COLLAPSE_MODE_SCROLL})
        @Retention(RetentionPolicy.SOURCE)
        @interface CollapseMode {
        }
    }

    private class OffsetUpdateListener implements AppBarLayout.OnOffsetChangedListener {
        OffsetUpdateListener() {
        }

        @Override
        public void onOffsetChanged(AppBarLayout layout, int verticalOffset) {
            currentOffset = verticalOffset;

            final int insetTop = lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;

            for (int i = 0, z = getChildCount(); i < z; i++) {
                final View child = getChildAt(i);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                final ViewOffsetHelper offsetHelper = getViewOffsetHelper(child);

                final int expandRange = getHeight() - getMinimumHeight() - insetTop;
                final float percent = -1.0f * verticalOffset / expandRange;

                switch (lp.collapseMode) {
                    case LayoutParams.COLLAPSE_MODE_PIN:
                        offsetHelper.setTopAndBottomOffset(
                                MathUtils.clamp(-verticalOffset, 0, getMaxOffsetForPinChild(child)));
                        break;
                    case LayoutParams.COLLAPSE_MODE_PARALLAX:
                        offsetHelper.setTopAndBottomOffset(Math.round(-verticalOffset * lp.parallaxMult));
                        break;
                    case LayoutParams.COLLAPSE_MODE_SCROLL:
                        offsetHelper.setLeftAndRightOffset((int)
                                (percent * (lp.collapsedRect.left - lp.expandedRect.left)));
                        // 这里要多加一个偏移量，把竖直方向纠正过来
                        offsetHelper.setTopAndBottomOffset(-verticalOffset + (int)
                                (percent * (lp.collapsedRect.top - lp.expandedRect.top)));
                        break;
                    default:
                        break;
                }
            }

            // Show or hide the scrims if needed
            updateScrimVisibility();

            if (statusBarScrim != null && insetTop > 0) {
                ViewCompat.postInvalidateOnAnimation(ScrollCollapsingToolbarLayout.this);
            }

            // Update the collapsing text's fraction
            final int expandRange =
                    getHeight() - ViewCompat.getMinimumHeight(ScrollCollapsingToolbarLayout.this) - insetTop;
            collapsingTextHelper.setExpansionFraction(Math.abs(verticalOffset) / (float) expandRange);
        }
    }
}
