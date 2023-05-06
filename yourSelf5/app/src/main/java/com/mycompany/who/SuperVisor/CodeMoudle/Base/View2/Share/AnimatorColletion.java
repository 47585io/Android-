package com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share;
import android.view.*;
import android.animation.*;

public class AnimatorColletion
{
	public static final byte Top =0;
	public static final byte Down =1;
	public static final byte Left =2;
	public static final byte Right =3;


	public static Animator getOpenAnimator(int start,int end, final View v, byte flag)
	{
		ValueAnimator t = ValueAnimator.ofInt(start,end);

		switch (flag)
		{
			case Top:
				t.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

						@Override
						public void onAnimationUpdate(ValueAnimator p1)
						{
							int now = p1.getAnimatedValue();
							v.setTop(now);
						}
					});
				break;
			case Down:
				t.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

						@Override
						public void onAnimationUpdate(ValueAnimator p1)
						{
							int now = p1.getAnimatedValue();
							v.setBottom(now);
						}
					});
				break;
			case Left:
				t.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

						@Override
						public void onAnimationUpdate(ValueAnimator p1)
						{
							int now = p1.getAnimatedValue();
							v.setLeft(now);
						}
					});
				break;
			case Right:
				t.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

						@Override
						public void onAnimationUpdate(ValueAnimator p1)
						{
							int now = p1.getAnimatedValue();
							v.setRight(now);
						}
					});
				break;
		}
		
		return t;
	}



	public static Animator getTranstion(View v,float fromx,float fromy, float tox, float toy)
	{
		Animator anim1 = ObjectAnimator.ofFloat(v, "transtionX", fromx,tox);
		Animator anim2 = ObjectAnimator.ofFloat(v, "transtionY", fromy,toy);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(anim1,anim2);
		return set;
	}
	
	public static Animator getScroll(final View v,int fromx,int fromy,int tox,int toy){
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
