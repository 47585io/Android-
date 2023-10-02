package com.editor.view.Share;
import android.animation.*;
import android.view.*;

public class AnimationCollection
{
	public static Animator getTranstion(View v,float fromx,float fromy, float tox, float toy)
	{
		Animator anim1 = ObjectAnimator.ofFloat(v, "translationX", fromx,tox);
		Animator anim2 = ObjectAnimator.ofFloat(v, "translationY", fromy,toy);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(anim1,anim2);
		return set;
	}

	public static Animator getScroll(final View v,int fromx,int fromy,int tox,int toy)
	{
		ValueAnimator t = ValueAnimator.ofInt(fromx,tox);
		ValueAnimator t2 = ValueAnimator.ofInt(fromy,toy);
		AnimatorSet set = new AnimatorSet();

		t.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					int value = p1.getAnimatedValue();
					int y = v.getScrollY();
					v.scrollTo(value,y);
				}
			});
		t2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

				@Override
				public void onAnimationUpdate(ValueAnimator p1)
				{
					int value = p1.getAnimatedValue();
					int x = v.getScrollX();
					v.scrollTo(x,value);
				}
			});

	    set.playTogether(t,t2);
		return set;
	}
	
}
