package com.khryzyz.spotlight;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;

import com.khryzyz.spotlight.prefs.PreferencesManager;
import com.khryzyz.spotlight.shape.Circle;
import com.khryzyz.spotlight.shape.NormalLineAnimDrawable;
import com.khryzyz.spotlight.target.AnimPoint;
import com.khryzyz.spotlight.target.Target;
import com.khryzyz.spotlight.target.ViewTarget;
import com.khryzyz.spotlight.utils.SpotlightListener;
import com.khryzyz.spotlight.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jitender on 10/06/16.
 */

public class SpotlightView extends FrameLayout {

    private static final String TAG = "SpotlightView";

    /**
     * OverLay color
     */
    private int maskColor = 0x70000000;

    /**
     * Intro Animation Duration
     */
    private long introAnimationDuration = 400;

    /**
     * Toggel between reveal and fadein animation
     */
    private boolean isRevealAnimationEnabled = true;

    /**
     * Final fadein text duration
     */
    private long fadingTextDuration = 400;

    /**
     * Start intro once view is ready to show
     */
    private boolean isReady;

    /**
     * Overlay circle above the view
     */
    private Circle circleShape;

    /**
     * Target View
     */
    private Target targetView;

    /**
     * Eraser to erase the circle area
     */
    private Paint eraser;

    /**
     * Delay the intro view
     */
    private Handler handler;
    private Bitmap bitmap;
    private Canvas canvas;

    /**
     * Padding for circle
     */
    private int padding = 20;

    /**
     * View Width
     */
    private int width;

    /**
     * View Height
     */
    private int height;

    /**
     * Dismiss layout on touch
     */
    private boolean dismissOnTouch;
    private boolean dismissOnBackPress;
    private boolean enableDismissAfterShown;

    /**
     * Button
     */
    private boolean showButton;
    private int buttonSize = 24;
    private int buttonSizeDimenUnit = -1;
    private int buttonColorText = Color.parseColor("#ffffff");
    private int buttonColorBackground = Color.parseColor("#000000");
    private CharSequence buttonText = "Got it!";

    private PreferencesManager preferencesManager;
    private String usageId;

    /**
     * Listener for spotLight when user clicks on the view
     */
    private SpotlightListener listener;

    /**
     * Perform click when user clicks on the targetView
     */
    private boolean isPerformClick;

    /**
     * Margin from left, right, top and bottom till the line will stop
     */
    private int gutter = Utils.dpToPx(36);

    //Spaces above and below the line
    private int spaceAboveLine = Utils.dpToPx(8);
    private int spaceBelowLine = Utils.dpToPx(12);

    private int extramargin = Utils.dpToPx(16);

    /**
     * Views Heading and sub-heading for spotlight
     */
    private TextView subHeadingTv, headingTv;

    private LinearLayout linearLayoutContent;

    private Button buttonGotIt;

    /**
     * Whether to show the arc at the end of the line that points to the target.
     */
    private boolean showTargetArc = true;

    /**
     * Extra padding around the arc
     */
    private int extraPaddingForArc = 24;

    /**
     * Defaults for heading TextView
     */
    private int headingTvSize = 24;
    private int headingTvSizeDimenUnit = -1;
    private int headingTvColor = Color.parseColor("#eb273f");
    private CharSequence headingTvText = "Hello";

    /**
     * Defaults for sub-heading TextView
     */
    private int subHeadingTvSize = 24;
    private int subHeadingTvSizeDimenUnit = -1;
    private int subHeadingTvColor = Color.parseColor("#ffffff");
    private CharSequence subHeadingTvText = "Hello";

    /**
     * Values for line animation
     */
    private long lineAnimationDuration = 300;
    private int lineStroke;
    private PathEffect lineEffect;
    private int lineAndArcColor = Color.parseColor("#eb273f");

    private Typeface mTypeface = null;

    private int softwareBtnHeight;

    private boolean dismissCalled = false;

    public SpotlightView(Context context) {
        super(context);
        init(context);
    }

    public SpotlightView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SpotlightView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    private void init(Context context) {
        setWillNotDraw(false);
        setVisibility(INVISIBLE);

        lineStroke = Utils.dpToPx(4);
        isReady = false;
        isRevealAnimationEnabled = true;
        dismissOnTouch = false;
        isPerformClick = false;
        showButton = false;
        enableDismissAfterShown = false;
        dismissOnBackPress = false;
        handler = new Handler();
        preferencesManager = new PreferencesManager(context);
        eraser = new Paint();
        eraser.setColor(0xFFFFFFFF);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setFlags(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        try {
            if (!isReady) return;

            if (bitmap == null || canvas == null) {
                if (bitmap != null) bitmap.recycle();

                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                this.canvas = new Canvas(bitmap);
            }

            this.canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
            this.canvas.drawColor(maskColor);

            circleShape.draw(this.canvas, eraser, padding);

            canvas.drawBitmap(bitmap, 0, 0, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float xT = event.getX();
        float yT = event.getY();

        int xV = circleShape.getPoint().x;
        int yV = circleShape.getPoint().y;

        int radius = circleShape.getRadius();

        double dx = Math.pow(xT - xV, 2);
        double dy = Math.pow(yT - yV, 2);

        boolean isTouchOnFocus = (dx + dy) <= Math.pow(radius, 2);

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if (isTouchOnFocus && isPerformClick) {
                    targetView.getView().setPressed(true);
                    targetView.getView().invalidate();
                }

                return true;
            case MotionEvent.ACTION_UP:
                if (isTouchOnFocus || dismissOnTouch)
                    dismiss();

                if (isTouchOnFocus && isPerformClick) {
                    targetView.getView().performClick();
                    targetView.getView().setPressed(true);
                    targetView.getView().invalidate();
                    targetView.getView().setPressed(false);
                    targetView.getView().invalidate();
                }

                return true;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }


    /**
     * Show the view based on the configuration
     * Reveal is available only for Lollipop and above in other only fadein will work
     * To support reveal in older versions use github.com/ozodrukh/CircularReveal
     *
     * @param activity
     */
    private void show(final Activity activity) {

        if (preferencesManager.isDisplayed(usageId))
            return;

        ((ViewGroup) activity.getWindow().getDecorView()).addView(this);

        setReady(true);

        handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

                                            if (isRevealAnimationEnabled)
                                                startRevealAnimation(activity);
                                            else {
                                                startFadinAnimation(activity);
                                            }
                                        } else {
                                            startFadinAnimation(activity);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                , 100);

    }

    /**
     * Dissmiss view with reverse animation
     */
    public void dismiss() {
        if (dismissCalled) {
            return;
        }
        dismissCalled = true;

        preferencesManager.setDisplayed(usageId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (isRevealAnimationEnabled)
                exitRevealAnimation();
            else
                startFadeout();
        } else {
            startFadeout();
        }

    }


    /**
     * Revel animation from target center to screen width and height
     *
     * @param activity
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startRevealAnimation(final Activity activity) {

        float finalRadius = (float) Math.hypot(getViewWidth(), getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(this, targetView.getPoint().x, targetView.getPoint().y, 0, finalRadius);
        anim.setInterpolator(AnimationUtils.loadInterpolator(activity,
                android.R.interpolator.fast_out_linear_in));
        anim.setDuration(introAnimationDuration);

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (showTargetArc) {
                    addArcAnimation(activity);
                } else {
                    addPathAnimation(activity);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        setVisibility(View.VISIBLE);
        if (dismissOnBackPress) {
            requestFocus();
        }
        anim.start();
    }

    /**
     * Reverse reveal animation
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void exitRevealAnimation() {
        float finalRadius = (float) Math.hypot(getViewWidth(), getHeight());
        Animator anim = ViewAnimationUtils.createCircularReveal(this, targetView.getPoint().x, targetView.getPoint().y, finalRadius, 0);
        anim.setInterpolator(AnimationUtils.loadInterpolator(getContext(),
                android.R.interpolator.accelerate_decelerate));
        anim.setDuration(introAnimationDuration);

        anim.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                setVisibility(GONE);
                removeSpotlightView();

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

        anim.start();
    }

    private void startFadinAnimation(final Activity activity) {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(introAnimationDuration);
        fadeIn.setFillAfter(true);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (showTargetArc) {
                    addArcAnimation(activity);
                } else {
                    addPathAnimation(activity);
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        setVisibility(VISIBLE);
        if (dismissOnBackPress) {
            requestFocus();
        }
        startAnimation(fadeIn);
    }

    private void startFadeout() {
        AlphaAnimation fadeIn = new AlphaAnimation(1.0f, 0.0f);
        fadeIn.setDuration(introAnimationDuration);
        fadeIn.setFillAfter(true);
        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                setVisibility(GONE);
                removeSpotlightView();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        startAnimation(fadeIn);

    }

    /**
     * Add arc above/below the circular target overlay.
     */
    private void addArcAnimation(final Activity activity) {
        AppCompatImageView mImageView = new AppCompatImageView(activity);
        mImageView.setImageResource(R.drawable.ic_spotlight_arc);
        LayoutParams params = new LayoutParams(2 * (circleShape.getRadius() + extraPaddingForArc),
                2 * (circleShape.getRadius() + extraPaddingForArc));


        if (targetView.getPoint().y > getHeight() / 2) {//bottom
            if (targetView.getPoint().x > getWidth() / 2) {//Right
                params.rightMargin = getWidth() - targetView.getPoint().x - circleShape.getRadius() - extraPaddingForArc;
                params.bottomMargin = getHeight() - targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc;
                params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            } else {
                params.leftMargin = targetView.getPoint().x - circleShape.getRadius() - extraPaddingForArc;
                params.bottomMargin = getHeight() - targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc;
                params.gravity = Gravity.LEFT | Gravity.BOTTOM;
            }
        } else {//up
            mImageView.setRotation(180); //Reverse the view
            if (targetView.getPoint().x > getWidth() / 2) {//Right
                params.rightMargin = getWidth() - targetView.getPoint().x - circleShape.getRadius() - extraPaddingForArc;
                params.bottomMargin = getHeight() - targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc;
                params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            } else {
                params.leftMargin = targetView.getPoint().x - circleShape.getRadius() - extraPaddingForArc;
                params.bottomMargin = getHeight() - targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc;
                params.gravity = Gravity.LEFT | Gravity.BOTTOM;
            }

        }
        mImageView.postInvalidate();
        mImageView.setLayoutParams(params);
        addView(mImageView);

        PorterDuffColorFilter porterDuffColorFilter = new PorterDuffColorFilter(lineAndArcColor,
                PorterDuff.Mode.SRC_ATOP);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AnimatedVectorDrawable avd = (AnimatedVectorDrawable)
                    ContextCompat.getDrawable(activity, R.drawable.avd_spotlight_arc);
            avd.setColorFilter(porterDuffColorFilter);
            mImageView.setImageDrawable(avd);
            avd.start();
        } else {
            AnimatedVectorDrawableCompat avdc =
                    AnimatedVectorDrawableCompat.create(activity, R.drawable.avd_spotlight_arc);
            avdc.setColorFilter(porterDuffColorFilter);
            mImageView.setImageDrawable(avdc);
            avdc.start();
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                addPathAnimation(activity);
            }
        }, 400);
    }

    private void addPathAnimation(Activity activity) {

        View mView = new View(activity);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.width = getViewWidth();
        params.height = getViewHeight();
//        params.width = getMeasuredWidth();
//        params.height = getMeasuredHeight();
        addView(mView, params);

        //Textviews
        headingTv = new TextView(activity);
        if (mTypeface != null)
            headingTv.setTypeface(mTypeface);

        if (headingTvSizeDimenUnit != -1)
            headingTv.setTextSize(headingTvSizeDimenUnit, headingTvSize);
        else
            headingTv.setTextSize(headingTvSize);

        headingTv.setVisibility(View.GONE);
        headingTv.setTextColor(headingTvColor);
        headingTv.setText(headingTvText);

        LinearLayout.LayoutParams subHeadingParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        subHeadingTv = new TextView(activity);
        if (mTypeface != null)
            subHeadingTv.setTypeface(mTypeface);

        if (subHeadingTvSizeDimenUnit != -1)
            subHeadingTv.setTextSize(subHeadingTvSizeDimenUnit, subHeadingTvSize);
        else
            subHeadingTv.setTextSize(subHeadingTvSize);

        subHeadingTv.setLayoutParams(subHeadingParams);
        subHeadingTv.setTextColor(subHeadingTvColor);
        subHeadingTv.setVisibility(View.GONE);
        subHeadingTv.setText(subHeadingTvText);

        linearLayoutContent = new LinearLayout(activity);
        linearLayoutContent.setOrientation(LinearLayout.VERTICAL);

        //Button
        buttonGotIt = new Button(activity);
        if (mTypeface != null)
            buttonGotIt.setTypeface(mTypeface);

        if (buttonSizeDimenUnit != -1)
            buttonGotIt.setTextSize(buttonSizeDimenUnit, buttonSize);
        else
            buttonGotIt.setTextSize(buttonSize);

        LinearLayout.LayoutParams buttonGotItParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        buttonGotItParams.topMargin = extramargin;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            // If we're running on Honeycomb or newer, then we can use the Theme's
            // selectableItemBackground to ensure that the View has a pressed state
            TypedValue outValue = new TypedValue();
            activity.getTheme().resolveAttribute(buttonColorText, outValue, true);
            buttonGotIt.setBackgroundResource(outValue.resourceId);
        }

        buttonGotIt.setLayoutParams(buttonGotItParams);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setColor(buttonColorBackground);
        shape.setCornerRadii(new float[]{10, 10, 10, 10, 10, 10, 10, 10});
        shape.setStroke(3, Color.BLACK);

        buttonGotIt.setBackground(shape);

        buttonGotIt.setTextColor(buttonColorText);
//        buttonGotIt.setBackgroundColor(buttonColorBackground);
        buttonGotIt.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        buttonGotIt.setVisibility(View.GONE);
        buttonGotIt.setText(buttonText);

        //Line animation
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setStyle(Paint.Style.STROKE);
        p.setStrokeJoin(Paint.Join.ROUND);
        p.setStrokeCap(Paint.Cap.ROUND);
        p.setStrokeWidth(lineStroke);
        p.setColor(lineAndArcColor);
        p.setPathEffect(lineEffect);

        NormalLineAnimDrawable animDrawable1 = new NormalLineAnimDrawable(p);
        if (lineAnimationDuration > 0)
            animDrawable1.setLineAnimDuration(lineAnimationDuration);
        if (Build.VERSION.SDK_INT < 16) {
            mView.setBackgroundDrawable(animDrawable1);
        } else {
            mView.setBackground(animDrawable1);
        }

        animDrawable1.setPoints(checkLinePoint());
        animDrawable1.playAnim();

        animDrawable1.setmListner(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                fadeIn.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        if (enableDismissAfterShown)
                            dismissOnTouch = true;
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                fadeIn.setDuration(fadingTextDuration);
                fadeIn.setFillAfter(true);
                headingTv.startAnimation(fadeIn);
                subHeadingTv.startAnimation(fadeIn);
                headingTv.setVisibility(View.VISIBLE);
                subHeadingTv.setVisibility(View.VISIBLE);
                if (showButton)
                    buttonGotIt.setVisibility(View.VISIBLE);

            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });

    }

    private void enableDismissOnBackPress() {
        setFocusableInTouchMode(true);
        setFocusable(true);
        requestFocus();
    }

    private List<AnimPoint> checkLinePoint() {

        //Screen Height
        int screenWidth = getWidth();
        int screenHeight = getHeight();

        List<AnimPoint> animPoints = new ArrayList<>();

        //For TextViews
        LayoutParams headingParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        //For LinearLayout
        LayoutParams linearLayoutContentParams = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);

        //check up or down
        if (targetView.getPoint().y > screenHeight / 2) {//Down TODO: add a logic for by 2
            if (targetView.getPoint().x > screenWidth / 2) {//Right
                Log.i(TAG, "checkLinePoint: Down Right");
                animPoints.add(new AnimPoint((targetView.getViewRight() - targetView.getViewWidth() / 2),
                        targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc, (targetView.getViewRight() - targetView.getViewWidth() / 2),
                        targetView.getViewTop() / 2
                ));
                animPoints.add(new AnimPoint((targetView.getViewRight() - targetView.getViewWidth() / 2),
                        targetView.getViewTop() / 2,
                        gutter,
                        targetView.getViewTop() / 2));
                //TextViews
                headingParams.leftMargin = gutter;
                headingParams.rightMargin = screenWidth - (targetView.getViewRight() - targetView.getViewWidth() / 2) + extramargin;
                headingParams.bottomMargin = screenHeight - targetView.getViewTop() / 2 + spaceAboveLine;
                headingParams.topMargin = extramargin;
                headingParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                headingTv.setGravity(Gravity.LEFT);

                linearLayoutContentParams.rightMargin = screenWidth - (targetView.getViewRight() - targetView.getViewWidth() / 2) + extramargin;
                linearLayoutContentParams.leftMargin = gutter;
                linearLayoutContentParams.bottomMargin = extramargin;
                linearLayoutContentParams.topMargin = targetView.getViewTop() / 2 + spaceBelowLine;
                linearLayoutContentParams.gravity = Gravity.LEFT;

            } else {//left
                Log.i(TAG, "checkLinePoint: Down Left");
                animPoints.add(new AnimPoint((targetView.getViewRight() - targetView.getViewWidth() / 2), targetView.getPoint().y - circleShape.getRadius() - extraPaddingForArc,
                        (targetView.getViewRight() - targetView.getViewWidth() / 2),
                        targetView.getViewTop() / 2
                ));

                animPoints.add(new AnimPoint((targetView.getViewRight() - targetView.getViewWidth() / 2),
                        targetView.getViewTop() / 2,
                        screenWidth - ((screenHeight > screenWidth) ? gutter : (gutter + softwareBtnHeight)),
                        targetView.getViewTop() / 2));

                //TextViews
                if (screenHeight > screenWidth)
                    headingParams.rightMargin = gutter;//portrait
                else
                    headingParams.rightMargin = gutter + softwareBtnHeight;//landscape
                headingParams.leftMargin = (targetView.getViewRight() - targetView.getViewWidth() / 2) + extramargin;
                headingParams.bottomMargin = screenHeight - targetView.getViewTop() / 2 + spaceAboveLine;
                headingParams.topMargin = extramargin;
                headingParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                headingTv.setGravity(Gravity.LEFT);

                if (screenHeight > screenWidth)
                    linearLayoutContentParams.rightMargin = gutter;//portrait
                else
                    linearLayoutContentParams.rightMargin = gutter + softwareBtnHeight;//landscape
                linearLayoutContentParams.leftMargin = (targetView.getViewRight() - targetView.getViewWidth() / 2) + extramargin;
                linearLayoutContentParams.topMargin = targetView.getViewTop() / 2 + spaceBelowLine;
                linearLayoutContentParams.bottomMargin = extramargin;
                linearLayoutContentParams.gravity = Gravity.RIGHT;

            }
        } else {//top
            if (targetView.getPoint().x > screenWidth / 2) {//Right
                Log.i(TAG, "checkLinePoint: Top Right");
                animPoints.add(new AnimPoint(targetView.getViewRight() - targetView.getViewWidth() / 2,
                        targetView.getPoint().y + circleShape.getRadius() + extraPaddingForArc,
                        targetView.getViewRight() - targetView.getViewWidth() / 2,
                        (screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()));

                animPoints.add(new AnimPoint(targetView.getViewRight() - targetView.getViewWidth() / 2,
                        (screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom(),
                        gutter,
                        (screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()));

//                //TextViews
                headingParams.leftMargin = gutter;
                headingParams.rightMargin = screenWidth - (targetView.getViewRight() - targetView.getViewWidth() / 2) + extramargin;
                headingParams.bottomMargin = screenHeight - ((screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()) + spaceAboveLine;
                headingParams.topMargin = extramargin;
                headingParams.gravity = Gravity.BOTTOM | Gravity.LEFT;
                headingTv.setGravity(Gravity.LEFT);

                linearLayoutContentParams.leftMargin = gutter;
                linearLayoutContentParams.rightMargin = screenWidth - targetView.getViewRight() + targetView.getViewWidth() / 2 + extramargin;
                linearLayoutContentParams.bottomMargin = extramargin;
                linearLayoutContentParams.topMargin = ((screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()) + spaceBelowLine;
                linearLayoutContentParams.gravity = Gravity.LEFT;

            } else {//left
                Log.i(TAG, "checkLinePoint: Top Left");
                animPoints.add(new AnimPoint(targetView.getViewRight() - targetView.getViewWidth() / 2,
                        targetView.getPoint().y + circleShape.getRadius() + extraPaddingForArc,
                        targetView.getViewRight() - targetView.getViewWidth() / 2,
                        (screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()));

                animPoints.add(new AnimPoint(targetView.getViewRight() - targetView.getViewWidth() / 2,
                        (screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom(),
                        screenWidth - ((screenHeight > screenWidth) ? gutter : (gutter + softwareBtnHeight)),
                        (screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()));

//                //TextViews
                if (screenHeight > screenWidth)
                    headingParams.rightMargin = gutter;//portrait
                else
                    headingParams.rightMargin = gutter + softwareBtnHeight;//landscape
                headingParams.leftMargin = targetView.getViewRight() - targetView.getViewWidth() / 2 + extramargin;
                headingParams.bottomMargin = screenHeight - ((screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()) + spaceAboveLine;
                headingParams.topMargin = extramargin;
                headingParams.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                headingTv.setGravity(Gravity.LEFT);

                if (screenHeight > screenWidth)
                    linearLayoutContentParams.rightMargin = gutter;//portrait
                else
                    linearLayoutContentParams.rightMargin = gutter + softwareBtnHeight;//landscape
                linearLayoutContentParams.leftMargin = targetView.getViewRight() - targetView.getViewWidth() / 2 + extramargin;
                linearLayoutContentParams.bottomMargin = extramargin;
                linearLayoutContentParams.topMargin = ((screenHeight - targetView.getViewBottom()) / 2 + targetView.getViewBottom()) + spaceBelowLine;
                linearLayoutContentParams.gravity = Gravity.RIGHT;

            }
        }

        addView(headingTv, headingParams);
        linearLayoutContent.addView(subHeadingTv);
        linearLayoutContent.addView(buttonGotIt);
        addView(linearLayoutContent, linearLayoutContentParams);

        return animPoints;
    }

    /**
     * Remove the spotlight view
     */
    public void removeSpotlightView() {

        if (listener != null)
            listener.onUserClicked(usageId);

        if (getParent() != null)
            ((ViewGroup) getParent()).removeView(this);
    }

    /**
     * Remove the spotlight view
     *
     * @param needOnUserClickedCallback true, if user wants a call back when this spotlight view is removed from parent.
     */
    public void removeSpotlightView(boolean needOnUserClickedCallback) {
        try {
            if (needOnUserClickedCallback && listener != null)
                listener.onUserClicked(usageId);

            if (getParent() != null)
                ((ViewGroup) getParent()).removeView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Setters
     */

    public void setListener(SpotlightListener listener) {
        this.listener = listener;
    }

    private void setMaskColor(int maskColor) {
        this.maskColor = maskColor;
    }

    public void setReady(boolean ready) {
        isReady = ready;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public void setDismissOnTouch(boolean dismissOnTouch) {
        this.dismissOnTouch = dismissOnTouch;
    }

    public void setDismissOnBackPress(boolean dismissOnBackPress) {
        this.dismissOnBackPress = dismissOnBackPress;
    }

    public void setShowButton(boolean showButton) {
        this.showButton = showButton;
    }

    public void setButtonSize(int buttonSize) {
        this.buttonSize = buttonSize;
    }

    public void setButtonGotItSize(int dimenUnit, int buttonGotItSize) {
        this.buttonSizeDimenUnit = dimenUnit;
        this.buttonSize = buttonGotItSize;
    }

    public void setButtonColorText(int buttonColorText) {
        this.buttonColorText = buttonColorText;
    }

    public void setButtonColorBackground(int buttonColorBackground) {
        this.buttonColorBackground = buttonColorBackground;
    }

    public void setButtonText(CharSequence buttonText) {
        this.buttonText = buttonText;
    }

    public boolean isEnableDismissAfterShown() {
        return enableDismissAfterShown;
    }

    public void setEnableDismissAfterShown(boolean enableDismissAfterShown) {
        this.enableDismissAfterShown = enableDismissAfterShown;
    }

    public void setPerformClick(boolean performClick) {
        isPerformClick = performClick;
    }

    public void setExtraPaddingForArc(int extraPaddingForArc) {
        this.extraPaddingForArc = extraPaddingForArc;
    }

    /**
     * Whether to show the arc under/above the circular target overlay.
     *
     * @param show Set to true to show the arc line, false otherwise.
     */
    public void setShowTargetArc(boolean show) {
        this.showTargetArc = show;
    }

    public void setIntroAnimationDuration(long introAnimationDuration) {
        this.introAnimationDuration = introAnimationDuration;
    }


    public void setRevealAnimationEnabled(boolean revealAnimationEnabled) {
        isRevealAnimationEnabled = revealAnimationEnabled;
    }

    public void setFadingTextDuration(long fadingTextDuration) {
        this.fadingTextDuration = fadingTextDuration;
    }

    public void setCircleShape(Circle circleShape) {
        this.circleShape = circleShape;
    }

    public void setTargetView(Target targetView) {
        this.targetView = targetView;
    }

    public void setUsageId(String usageId) {
        this.usageId = usageId;
    }

    public void setHeadingTvSize(int headingTvSize) {
        this.headingTvSize = headingTvSize;
    }

    public void setHeadingTvSize(int dimenUnit, int headingTvSize) {
        this.headingTvSizeDimenUnit = dimenUnit;
        this.headingTvSize = headingTvSize;
    }

    public void setHeadingTvColor(int headingTvColor) {
        this.headingTvColor = headingTvColor;
    }

    public void setHeadingTvText(CharSequence headingTvText) {
        this.headingTvText = headingTvText;
    }

    public void setSubHeadingTvSize(int subHeadingTvSize) {
        this.subHeadingTvSize = subHeadingTvSize;
    }

    public void setSubHeadingTvSize(int dimenUnit, int subHeadingTvSize) {
        this.subHeadingTvSizeDimenUnit = dimenUnit;
        this.subHeadingTvSize = subHeadingTvSize;
    }

    public void setSubHeadingTvColor(int subHeadingTvColor) {
        this.subHeadingTvColor = subHeadingTvColor;
    }

    public void setSubHeadingTvText(CharSequence subHeadingTvText) {
        this.subHeadingTvText = subHeadingTvText;
    }

    public void setLineAnimationDuration(long lineAnimationDuration) {
        this.lineAnimationDuration = lineAnimationDuration;
    }

    public void setLineAndArcColor(int lineAndArcColor) {
        this.lineAndArcColor = lineAndArcColor;
    }

    public void setLineStroke(int lineStroke) {
        this.lineStroke = lineStroke;
    }

    public void setLineEffect(PathEffect pathEffect) {
        this.lineEffect = pathEffect;
    }

    private void setSoftwareBtnHeight(int px) {
        this.softwareBtnHeight = px;
    }

    public void setTypeface(Typeface typeface) {
        this.mTypeface = typeface;
    }

    public void setConfiguration(SpotlightConfig configuration) {

        if (configuration != null) {
            this.maskColor = configuration.getMaskColor();
            this.introAnimationDuration = configuration.getIntroAnimationDuration();
            this.isRevealAnimationEnabled = configuration.isRevealAnimationEnabled();
            this.fadingTextDuration = configuration.getFadingTextDuration();
            this.padding = configuration.getPadding();
            this.dismissOnTouch = configuration.isDismissOnTouch();
            this.dismissOnBackPress = configuration.isDismissOnBackpress();
            this.isPerformClick = configuration.isPerformClick();
            this.headingTvSize = configuration.getHeadingTvSize();
            this.headingTvSizeDimenUnit = configuration.getHeadingTvSizeDimenUnit();
            this.headingTvColor = configuration.getHeadingTvColor();
            this.headingTvText = configuration.getHeadingTvText();
            this.subHeadingTvSize = configuration.getSubHeadingTvSize();
            this.subHeadingTvSizeDimenUnit = configuration.getSubHeadingTvSizeDimenUnit();
            this.subHeadingTvColor = configuration.getSubHeadingTvColor();
            this.subHeadingTvText = configuration.getSubHeadingTvText();
            this.lineAnimationDuration = configuration.getLineAnimationDuration();
            this.lineStroke = configuration.getLineStroke();
            this.lineAndArcColor = configuration.getLineAndArcColor();
        }
    }

    /**
     * Builder Class
     */
    public static class Builder {

        private SpotlightView spotlightView;

        private Activity activity;


        public Builder(Activity activity) {
            this.activity = activity;
            spotlightView = new SpotlightView(activity);
            spotlightView.setSoftwareBtnHeight(getSoftButtonsBarHeight(activity));
        }

        public Builder maskColor(int maskColor) {
            spotlightView.setMaskColor(maskColor);
            return this;
        }

        public Builder introAnimationDuration(long delayMillis) {
            spotlightView.setIntroAnimationDuration(delayMillis);
            return this;
        }

        public Builder enableRevealAnimation(boolean isFadeAnimationEnabled) {
            spotlightView.setRevealAnimationEnabled(isFadeAnimationEnabled);
            return this;
        }


        public Builder target(View view) {
            spotlightView.setTargetView(new ViewTarget(view));
            return this;
        }

        public Builder targetPadding(int padding) {
            spotlightView.setPadding(padding);
            return this;
        }

        public Builder dismissOnTouch(boolean dismissOnTouch) {
            spotlightView.setDismissOnTouch(dismissOnTouch);
            return this;
        }

        public Builder dismissOnBackPress(boolean dismissOnBackPress) {
            spotlightView.setDismissOnBackPress(dismissOnBackPress);
            return this;
        }

        public Builder usageId(String usageId) {
            spotlightView.setUsageId(usageId);
            return this;
        }

        public Builder showButton(boolean showButton) {
            spotlightView.setShowButton(showButton);
            return this;
        }

        public Builder buttonSize(int buttonGotItSize) {
            spotlightView.setButtonSize(buttonGotItSize);
            return this;
        }

        public Builder buttonSize(int dimenUnit, int buttonGotItSize) {
            spotlightView.setButtonGotItSize(dimenUnit, buttonGotItSize);
            return this;
        }

        public Builder buttonColorText(int color) {
            spotlightView.setButtonColorText(color);
            return this;
        }

        public Builder buttonColorBackground(int color) {
            spotlightView.setButtonColorBackground(color);
            return this;
        }

        public Builder buttonText(CharSequence buttonText) {
            spotlightView.setButtonText(buttonText);
            return this;
        }

        public Builder setTypeface(Typeface typeface) {
            spotlightView.setTypeface(typeface);
            return this;
        }

        public Builder setListener(SpotlightListener spotlightListener) {
            spotlightView.setListener(spotlightListener);
            return this;
        }

        public Builder performClick(boolean isPerformClick) {
            spotlightView.setPerformClick(isPerformClick);
            return this;
        }


        public Builder fadeinTextDuration(long fadinTextDuration) {
            spotlightView.setFadingTextDuration(fadinTextDuration);
            return this;
        }

        public Builder headingTvSize(int headingTvSize) {
            spotlightView.setHeadingTvSize(headingTvSize);
            return this;
        }

        public Builder headingTvSize(int dimenUnit, int headingTvSize) {
            spotlightView.setHeadingTvSize(dimenUnit, headingTvSize);
            return this;
        }

        public Builder headingTvColor(int color) {
            spotlightView.setHeadingTvColor(color);
            return this;
        }

        public Builder headingTvText(CharSequence text) {
            spotlightView.setHeadingTvText(text);
            return this;
        }

        public Builder subHeadingTvSize(int headingTvSize) {
            spotlightView.setSubHeadingTvSize(headingTvSize);
            return this;
        }

        public Builder subHeadingTvSize(int dimenUnit, int headingTvSize) {
            spotlightView.setSubHeadingTvSize(dimenUnit, headingTvSize);
            return this;
        }

        public Builder subHeadingTvColor(int color) {
            spotlightView.setSubHeadingTvColor(color);
            return this;
        }

        public Builder subHeadingTvText(CharSequence text) {
            spotlightView.setSubHeadingTvText(text);
            return this;
        }

        public Builder lineAndArcColor(int color) {
            spotlightView.setLineAndArcColor(color);
            return this;
        }

        public Builder lineAnimDuration(long duration) {
            spotlightView.setLineAnimationDuration(duration);
            return this;
        }

        public Builder showTargetArc(boolean show) {
            spotlightView.setShowTargetArc(show);
            return this;
        }

        public Builder enableDismissAfterShown(boolean enable) {
            if (enable) {
                spotlightView.setEnableDismissAfterShown(enable);
                spotlightView.setDismissOnTouch(false);
            }
            return this;
        }

        public Builder lineStroke(int stroke) {
            spotlightView.setLineStroke(Utils.dpToPx(stroke));
            return this;
        }

        public Builder lineEffect(@Nullable PathEffect pathEffect) {
            spotlightView.setLineEffect(pathEffect);
            return this;
        }

        public Builder setConfiguration(SpotlightConfig configuration) {
            spotlightView.setConfiguration(configuration);
            return this;
        }

        public SpotlightView build() {
            Circle circle = new Circle(
                    spotlightView.targetView,
                    spotlightView.padding);
            spotlightView.setCircleShape(circle);
            if (spotlightView.dismissOnBackPress) {
                spotlightView.enableDismissOnBackPress();
            }
            return spotlightView;
        }

        public SpotlightView show() {
            build().show(activity);
            return spotlightView;
        }

    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (dismissOnBackPress && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_UP) {
                dismiss();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    public void logger(String s) {
        Log.d("Spotlight", s);
    }

    private int getViewHeight() {
        if (getWidth() > getHeight()) {
            //Landscape
            return getHeight();
        } else {
            //Portrait
            return (getHeight() - softwareBtnHeight);
        }
    }

    private int getViewWidth() {
        if (getWidth() > getHeight()) {
            //Landscape
            return (getWidth() - softwareBtnHeight);
        } else {
            //Portrait
            return getWidth();
        }
    }

    private static int getSoftButtonsBarHeight(Activity activity) {
        try {
            // getRealMetrics is only available with API 17 and +
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                DisplayMetrics metrics = new DisplayMetrics();
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                if (metrics.heightPixels > metrics.widthPixels) {
                    //Portrait
                    int usableHeight = metrics.heightPixels;
                    activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                    int realHeight = metrics.heightPixels;
                    if (realHeight > usableHeight)
                        return realHeight - usableHeight;
                    else
                        return 0;
                } else {
                    //Landscape
                    int usableHeight = metrics.widthPixels;
                    activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
                    int realHeight = metrics.widthPixels;
                    if (realHeight > usableHeight)
                        return realHeight - usableHeight;
                    else
                        return 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * This will remove all usage ids from preferences.
     */
    public void resetAllUsageIds() {
        try {
            preferencesManager.resetAll();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This will remove given usage id from preferences.
     *
     * @param id Spotlight usage id to be removed
     */
    public void resetUsageId(String id) {
        try {
            preferencesManager.reset(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
