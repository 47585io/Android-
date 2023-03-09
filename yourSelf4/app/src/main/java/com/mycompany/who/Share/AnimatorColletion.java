package com.mycompany.who.Share;
import android.view.*;
import android.graphics.*;
import android.animation.*;

public class AnimatorColletion
{
	public static final byte UP =0;
	public static final byte Down =1;
	public static final byte Left =2;
	public static final byte Right =3;
	
	public static void open(View v,Rect rect,byte flag){
		ObjectAnimator oa = null;
		v.setX(rect.centerX());
		v.setY(rect.centerY());
		ViewGroup.MarginLayoutParams params= (ViewGroup.MarginLayoutParams) v.getLayoutParams();
		
		switch(flag){
			case UP:
				params.width=rect.width();
				oa = ObjectAnimator.ofFloat(v,"scaleY",0.5f,1f);
				break;
			case Down:
				params.width=rect.width();
				break;
			case Left:
				params.height=rect.height();
				break;
			case Right:
				params.height=rect.height();
				oa = ObjectAnimator.ofFloat(v,"scaleX",0.5f,1f);
				break;
		}
		v.setLayoutParams(params);
		oa.start();
		
	}
	
	public static void transtionX(View v,float x,float y){
		ObjectAnimator animator=ObjectAnimator.ofFloat(v,"transtionX",x,y);
		animator.start();
	}
	public static void transtionY(View v,float x,float y){
		ObjectAnimator animator=ObjectAnimator.ofFloat(v,"transtionY",x,y);
		animator.start();
	}
	
}
