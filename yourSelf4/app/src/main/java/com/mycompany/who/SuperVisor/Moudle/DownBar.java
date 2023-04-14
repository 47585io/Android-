package com.mycompany.who.SuperVisor.Moudle;

import android.content.*;
import android.view.*;
import com.mycompany.who.Share.*;
import com.mycompany.who.SuperVisor.Moudle.Config.*;
import java.util.logging.*;
import android.util.*;
import com.mycompany.who.Edit.Share.Share1.*;
import android.content.res.*;
import com.mycompany.who.View.*;

public class DownBar extends HasAll
{
	private View hander;
	private View vector;

	public DownBar(Context cont){
		super(cont);
	}
	public DownBar(Context cont,AttributeSet attrs){
		super(cont,attrs);
	}
	@Override
	public void init()
	{
		Creator=new DownBarCreator(0);
		Creator.ConfigSelf(this);
		super.init();
	}


	public void setHander(View v){
		hander = v;
		v.setOnTouchListener(new HanderTouch());
		if(hander!=null)
		    removeView(hander);
		addView(v,0);
		//Config_hesSize config = (DownBar.Config_hesSize) getConfig();
		//config.trim(v,config.getHanderSize());
	}
	public void setVector(View v){
		vector = v;
		if(vector!=null)
		    removeView(vector);
		addView(v,1);
		//Config_hesSize config = (DownBar.Config_hesSize) getConfig();
		//config.trim(v,config.getVectorSize());
		
	}

	class HanderTouch extends OnTouchToMove
	{

		@Override
		public void onMoveToLeft(View p1, MotionEvent p2, float dx)
		{
			if(getOrientation()==HORIZONTAL){
				Config_Size2 config = (Interfaces.Init.Config_Size2) getConfig();
				if(getWidth()<config.width)
				    setLeft(getLeft()-(int)dx);
				else
					setLeft(getRight()-config.width);
			}
		}

		@Override
		public void onMoveToRight(View p1, MotionEvent p2, float dx)
		{
			if(getOrientation()==HORIZONTAL){
				if(getWidth()>hander.getWidth())
				    setLeft(getLeft()+(int)dx);
				else
					setLeft(getRight()-hander.getWidth());
			}
		}

		@Override
		public void onMoveToTop(View p1, MotionEvent p2, float dy)
		{
			if(getOrientation()==VERTICAL){
				Config_Size2 config = (Interfaces.Init.Config_Size2) getConfig();

				if(getHeight()<config.height)
				    setTop(getTop()-(int)dy);
				else
					setTop(getBottom()-config.height);
			}
		}

		@Override
		public void onMoveToDown(View p1, MotionEvent p2, float dy)
		{
			if(getOrientation()==VERTICAL){
				if(getHeight()>hander.getHeight())
				    setTop(getTop()+(int)dy);
				else
					setTop(getBottom()-hander.getHeight());
			}
		}

		@Override
		public boolean onMoveEnd(View p1, MotionEvent p2)
		{
			if(p2.getAction()==MotionEvent.ACTION_UP){
				Config_hesSize config= (DownBar.Config_hesSize) getConfig();
			    if(config.isOpen)
					close();
				else
				    open();
			}
			invalidate();
			return true;
		}

	}
	
	class VectorTouch extends OnTouchToMove
	{

		@Override
		public void onMoveToLeft(View p1, MotionEvent p2, float dx)
		{
			p1.scrollBy(-(int)dx,0);
		}

		@Override
		public void onMoveToRight(View p1, MotionEvent p2, float dx)
		{
			p1.scrollBy((int)dx,0);
		}

		@Override
		public void onMoveToTop(View p1, MotionEvent p2, float dy)
		{
			// TODO: Implement this method
		}

		@Override
		public void onMoveToDown(View p1, MotionEvent p2, float dy)
		{
			// TODO: Implement this method
		}

		@Override
		public boolean onMoveEnd(View p1, MotionEvent p2)
		{
			return true;
		}
	}

	public void open(){
		Config_hesSize config= (DownBar.Config_hesSize) getConfig();
		config.isOpen=true;
		if(getOrientation()==HORIZONTAL)
			AnimatorColletion.getOpenAnimator(getLeft(),getRight()- getConfig().getSize().start,this,AnimatorColletion.Left).start();
		else if(getOrientation()==VERTICAL)
			AnimatorColletion.getOpenAnimator(getTop(),getBottom()- getConfig().getSize().end,this,AnimatorColletion.Top).start();
	}
	public void close(){
		Config_hesSize config= (DownBar.Config_hesSize) getConfig();
		config.isOpen=false;
		if(getOrientation()==HORIZONTAL)
		    AnimatorColletion.getOpenAnimator(getLeft(),getRight()-hander.getWidth(),this,AnimatorColletion.Left).start();
		else if(getOrientation()==VERTICAL)
			AnimatorColletion.getOpenAnimator(getTop(),getBottom()-hander.getHeight(),this,AnimatorColletion.Top).start();
	}


	static class DownBarCreator extends Creator<DownBar>
	{

		public DownBarCreator(int id){
			super(id);
		}

		@Override
		public void init(DownBar target, View root)
		{
			target.config=new Config_hesSize();
			target.Configer=new Config_hesView();
			target.setHander(new View(target.getContext()));
			target. setVector(new PageList(target.getContext()));
		}
	}

	static class Config_hesView implements Level<DownBar>
	{

		@Override
		public void ConfigSelf(DownBar target)
		{
			target.hander.setBackgroundColor(0);
			target.setBackgroundColor(0);
			target.vector.setBackgroundColor(0xffffffff);
		}

		@Override
		public void config(DownBar target)
		{
			// TODO: Implement this method
		}

		@Override
		public void clearConfig(DownBar target)
		{
			// TODO: Implement this method
		}
	}

	static class Config_hesSize extends Config_Size2<DownBar>
	{

		public boolean isOpen;
		
		@Override
		public void onChange(DownBar target, int src)
		{
			// TODO: Implement this method
			super.onChange(target, src);
			trim( target.hander,getHanderSize());
			trim( target.vector,getVectorSize());
			trim( target,getHanderSize());
			target.setOrientation(CastFlag(flag));
		}

		@Override
		public void onPort(DownBar target, int src)
		{
			// TODO: Implement this method
			super.onPort(target, src);
			target.setY(-getHanderSize().end);
		}

		@Override
		public void onLand(DownBar target, int src)
		{
			// TODO: Implement this method
			super.onLand(target, src);
			target.setX(-getHanderSize().start);
		}
		
		
		public size getHanderSize(){
			if(flag==Configuration.ORIENTATION_PORTRAIT)
			    return new size(width,(int)(height*0.1));
			else if(flag==Configuration.ORIENTATION_LANDSCAPE)
				return new size((int)(width*0.1),height);
			return null;
		}
		public size getVectorSize(){
			if(flag==Configuration.ORIENTATION_PORTRAIT)
			    return new size(width,(int)(height*0.9));
			else if(flag==Configuration.ORIENTATION_LANDSCAPE)
				return new size((int)(width*0.9),height);
			return null;
		}

	}
	@Override
	protected void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		getConfig().change(this,newConfig.orientation);
	}
	

}
