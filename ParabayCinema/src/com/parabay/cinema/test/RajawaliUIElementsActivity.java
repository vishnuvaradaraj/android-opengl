package com.parabay.cinema.test;

import com.parabay.cinema.R;
import com.parabay.cinema.ui.Base3DActivity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.Keyframe;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class RajawaliUIElementsActivity extends Base3DActivity {
	
	private RajawaliUIElementsRenderer mRenderer;
	Animator animation = null;
	private LayoutTransition mTransitioner;
	ViewGroup container = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mRenderer = new RajawaliUIElementsRenderer(this);
		mRenderer.setSurfaceView(mSurfaceView);
		super.setRenderer(mRenderer);
		
		LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        ll.setGravity(Gravity.BOTTOM);
        this.container = ll;
        
        SampleView sv = new SampleView(this);
        sv.setLayoutParams(new LayoutParams(700, 106));
        ll.addView(sv);
        
        AnimationUtils.loadAnimation(this,
                R.anim.pushleft);
        
        TextView label = new TextView(this);
        label.setText("Halo Dunia!");
        label.setTextSize(20);
        label.setGravity(Gravity.CENTER);
        label.setHeight(100);
        //ll.addView(label);
        
        ImageView image = new ImageView(this);
        image.setImageResource(R.drawable.rajawali_tex);
        //ll.addView(image);
        
        Button newButton = new Button(this);
        newButton.setText("Show/Hide");
        ll.addView(newButton);
        newButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                v.setVisibility( View.GONE );
            }
        });
        
        
        mLayout.addView(ll);
		
        resetTransition();
        
        mTransitioner.setStagger(LayoutTransition.CHANGE_APPEARING, 30);
        mTransitioner.setStagger(LayoutTransition.CHANGE_DISAPPEARING, 30);
        setupCustomAnimations();
        mTransitioner.setDuration(500);
        
		initLoader();
	}
	
    private void resetTransition() {
        mTransitioner = new LayoutTransition();
        container.setLayoutTransition(mTransitioner);
    }

    private void setupCustomAnimations() {
        // Changing while Adding
        PropertyValuesHolder pvhLeft =
                PropertyValuesHolder.ofInt("left", 0, 1);
        PropertyValuesHolder pvhTop =
                PropertyValuesHolder.ofInt("top", 0, 1);
        PropertyValuesHolder pvhRight =
                PropertyValuesHolder.ofInt("right", 0, 1);
        PropertyValuesHolder pvhBottom =
                PropertyValuesHolder.ofInt("bottom", 0, 1);
        PropertyValuesHolder pvhScaleX =
                PropertyValuesHolder.ofFloat("scaleX", 1f, 0f, 1f);
        PropertyValuesHolder pvhScaleY =
                PropertyValuesHolder.ofFloat("scaleY", 1f, 0f, 1f);
        final ObjectAnimator changeIn = ObjectAnimator.ofPropertyValuesHolder(
                        this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhScaleX, pvhScaleY).
                setDuration(mTransitioner.getDuration(LayoutTransition.CHANGE_APPEARING));
        mTransitioner.setAnimator(LayoutTransition.CHANGE_APPEARING, changeIn);
        changeIn.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setScaleX(1f);
                view.setScaleY(1f);
            }
        });

        // Changing while Removing
        Keyframe kf0 = Keyframe.ofFloat(0f, 0f);
        Keyframe kf1 = Keyframe.ofFloat(.9999f, 360f);
        Keyframe kf2 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder pvhRotation =
                PropertyValuesHolder.ofKeyframe("rotation", kf0, kf1, kf2);
        final ObjectAnimator changeOut = ObjectAnimator.ofPropertyValuesHolder(
                        this, pvhLeft, pvhTop, pvhRight, pvhBottom, pvhRotation).
                setDuration(mTransitioner.getDuration(LayoutTransition.CHANGE_DISAPPEARING));
        mTransitioner.setAnimator(LayoutTransition.CHANGE_DISAPPEARING, changeOut);
        changeOut.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotation(0f);
            }
        });

        // Adding
        ObjectAnimator animIn = ObjectAnimator.ofFloat(null, "rotationY", 90f, 0f).
                setDuration(mTransitioner.getDuration(LayoutTransition.APPEARING));
        mTransitioner.setAnimator(LayoutTransition.APPEARING, animIn);
        animIn.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationY(0f);
            }
        });

        // Removing
        ObjectAnimator animOut = ObjectAnimator.ofFloat(null, "rotationX", 0f, 90f).
                setDuration(mTransitioner.getDuration(LayoutTransition.DISAPPEARING));
        mTransitioner.setAnimator(LayoutTransition.DISAPPEARING, animOut);
        animOut.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator anim) {
                View view = (View) ((ObjectAnimator) anim).getTarget();
                view.setRotationX(0f);
            }
        });

    }

	   private static class SampleView extends View implements ValueAnimator.AnimatorUpdateListener {
	        private Paint   mPaint;
	        private float   mOriginX = 10;
	        private float   mOriginY = 10;

	        private Drawable dr;
	        
	        public SampleView(Context context) {
	            super(context);
	            setFocusable(true);

	            mPaint = new Paint();
	            mPaint.setAntiAlias(true);
	        	mPaint.setFilterBitmap(true);
	            mPaint.setStrokeWidth(5);
	            mPaint.setStrokeCap(Paint.Cap.ROUND);
	            mPaint.setTextSize(50);
	            mPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF,
	                                               Typeface.BOLD));
	        }
	        
	        private void showText(Canvas canvas, String text, Paint.Align align) {

	            canvas.drawColor(Color.TRANSPARENT);
	           
	            NinePatchDrawable npd = (NinePatchDrawable) getResources().getDrawable(R.drawable.lbarmini);
	            
		        Rect npdBounds = new Rect(0,0,this.getWidth()-20, 100);
		        npd.setBounds(npdBounds);	
		        npd.draw(canvas);
	            
	            Rect    bounds = new Rect();
	            mPaint.getTextBounds(text, 0, text.length(), bounds);
	            
	            mPaint.setColor(Color.WHITE);
	            canvas.drawText(text, (this.getWidth()-bounds.width())/2, (100-bounds.height())/2+100/3, mPaint);
	            
	        }

	        @Override protected void onDraw(Canvas canvas) {
	        	
	            canvas.translate(mOriginX, mOriginY);
	            showText(canvas, "Parabay TV", Paint.Align.LEFT);

	        }
	        
	        public void onAnimationUpdate(ValueAnimator animation) {

	            invalidate();
	        }
	    }	
}
