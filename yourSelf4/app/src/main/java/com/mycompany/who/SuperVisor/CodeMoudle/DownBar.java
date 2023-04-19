package com.mycompany.who.SuperVisor.CodeMoudle;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.Share.*;

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
		
	}
	public void setVector(View v){
		vector = v;
		if(vector!=null)
		    removeView(vector);
		addView(v,1);
		Rect delegateArea = new Rect();
		//获取按钮在父视图中的位置（区域，相对于父视图坐标）
		delegateArea.top = 0;
		delegateArea.left = 0;
		delegateArea.right = hander.getRight()+100;
		//扩大区域范围，这里向下扩展200像素
		delegateArea.bottom = hander.getBottom()+ 200;
		//新建委托
		TouchDelegate touchDelegate = new TouchDelegate(delegateArea, hander);
		vector.setTouchDelegate(touchDelegate);
		//在父View的onTouchEvent中，如果触摸在指定的区域，都先调用hander的onTouchEvent
		//在hander的onTouchEvent中，会先调用自己的onTouchListener的方法
		
	}

	class HanderTouch extends OnTouchToMove
	{

		@Override
		public void onMoveToLeft(View p1, MotionEvent p2, float dx)
		{
			if(getOrientation()==HORIZONTAL){
				Config_Size2 config = (CodeBlock.Config_Size2) getConfig();
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
				Config_Size2 config = (CodeBlock.Config_Size2) getConfig();

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
				if(getOrientation()==VERTICAL){
					//close状态下，只要height大于总高度的0.2，就可以抬起来了，反之收起
					//open状态下，只要height小于总高度的0.8，就可以收起来了，反之抬起
					int height = getBottom()-getTop();
					if((!config.isOpen && height>config.height*0.2)
						||(height>config.height*0.8))
						open();
					else if((config.isOpen && height<config.height*0.8)
						||(height<config.height*0.2))
						close();
				}
				else if(getOrientation()==HORIZONTAL){
					int width = getRight()-getLeft();
					if((!config.isOpen && width>config.width*0.2)
					   ||(width>config.width*0.8))
						open();
					else if((config.isOpen && width<config.width*0.8)
							||(width<config.width*0.2))
						close();
				}
			}
			invalidate();
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

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		super.onTouchEvent(event);
		return false;
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

}
