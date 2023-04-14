package com.mycompany.who.Share;
import android.view.*;
import android.graphics.*;
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



	public static void transtionX(View v, float x, float y)
	{
		ObjectAnimator animator=ObjectAnimator.ofFloat(v, "transtionX", x, y);
		animator.start();
	}
	public static void transtionY(View v, float x, float y)
	{
		ObjectAnimator animator=ObjectAnimator.ofFloat(v, "transtionY", x, y);
		animator.start();
	}

}
