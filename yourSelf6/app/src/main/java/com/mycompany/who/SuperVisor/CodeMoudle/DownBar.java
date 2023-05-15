package com.mycompany.who.SuperVisor.CodeMoudle;

import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View3.*;


/* 
  底部栏 
  
  支持设置把手和内容
  
  支持拖动把手打开DownBar查看设置的内容
  
  支持设置排列方向，这会使把手位置也变化
  
  应尽量将其设置在父View边缘，以显示出抽屉效果
  
*/
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
		super.init();
		Creator=new DownBarCreator(0);
		Creator.ConfigSelf(this);
	}

	public void setHander(View v)
	{
		hander = v;
		v.setOnTouchListener(new HanderTouch());
		//把手必须感知触摸
		if(hander!=null){
			//已有的把手先移除
		    removeView(hander);
		}
		addView(v,0);	
		//把手必须位于最前面
	}
	public void setVector(View v)
	{
		vector = v;
		if(vector!=null){
			//已有的内容先移除
		    removeView(vector);
		}
		addView(v,1);
		Rect delegateArea = new Rect();
		//获取按钮在父视图中的位置（区域，相对于父视图坐标）
		delegateArea.top = 0;
		delegateArea.left = 0;
		delegateArea.right = hander.getRight();
		//扩大区域范围，这里向下扩展200像素
		delegateArea.bottom = hander.getBottom()+ 200;
		//新建委托
		TouchDelegate touchDelegate = new TouchDelegate(delegateArea, hander);
		vector.setTouchDelegate(touchDelegate);
		//在父View的onTouchEvent中，如果触摸在指定的区域，都先调用hander的onTouchEvent
		//在hander的onTouchEvent中，会先调用自己的onTouchListener的方法
	}

	public void open()
	{
		Config_hesSize config= (DownBar.Config_hesSize) getConfig();
		config.isOpen=true;
		if(getOrientation()==HORIZONTAL){
			AnimatorColletion.getOpenAnimator(getLeft(),getRight()- getConfig().getWidth(),this,AnimatorColletion.Left).start();
		}
		else if(getOrientation()==VERTICAL){
			AnimatorColletion.getOpenAnimator(getTop(),getBottom()- getConfig().getHeight(),this,AnimatorColletion.Top).start();
	    }
	}
	public void close()
	{
		Config_hesSize config= (DownBar.Config_hesSize) getConfig();
		config.isOpen=false;
		if(getOrientation()==HORIZONTAL){
		    AnimatorColletion.getOpenAnimator(getLeft(),getRight()-hander.getWidth(),this,AnimatorColletion.Left).start();
		}
		else if(getOrientation()==VERTICAL){
			AnimatorColletion.getOpenAnimator(getTop(),getBottom()-hander.getHeight(),this,AnimatorColletion.Top).start();
		}
	}

/*
-----------------------------------------------------------------------------------
 
  HanderTouch，Handler如何感知触摸事件？
  
  非常感谢，OnTouchToMove提供了大量的好用接口，并允许我们重写坐标计算方式
  
  以下HanderTouch提供横向和纵向两种方向的滑动和感知
  
  当横向时，只有onMoveToLeft与onMoveToRight生效。当纵向时，只有onMoveToTop与onMoveToDown生效
  
  Handler追踪手指滑动方向来判断DownBar向哪个方向慢慢打开，具体过程是:
	 
  Handler保留上次的坐标，与本次坐标相比计算出差距，然后将DownBar整体扩大或缩小，演示滑动效果
  
  为了避免自己移动而导致坐标出错，Handler永远感知绝对坐标，因为它是相对于屏幕而非Handler本身
  
  在手指抬起来时，Handler会根据当前已打开的大小和isOpen状态进行直接完全打开或完全关闭
  
-----------------------------------------------------------------------------------
*/
	class HanderTouch extends OnTouchToMove
	{
		
		@Override
		public void onMoveToLeft(View p1, MotionEvent p2, float dx)
		{
			if(getOrientation()==HORIZONTAL){
				Config_Size2 config = (CodeBlock.Config_Size2) getConfig();
				if(getWidth()<config.width){
					//向左放大
				    setLeft(getLeft()-(int)dx);
				}
				else{
					//Width加上滑动距离后已经大于最大大小了，只能设置为最大
					setLeft(getRight()-config.width);
				}
			}
		}

		@Override
		public void onMoveToRight(View p1, MotionEvent p2, float dx)
		{
			if(getOrientation()==HORIZONTAL)
			{
				if(getWidth()>hander.getWidth()){
					//向右缩小
				    setLeft(getLeft()+(int)dx);
				}
				else{
					//Width已经小于滑动距离了，只能再缩小为0
					setLeft(getRight()-hander.getWidth());
				}
			}
		}

		@Override
		public void onMoveToTop(View p1, MotionEvent p2, float dy)
		{
			if(getOrientation()==VERTICAL)
			{
				Config_Size2 config = (CodeBlock.Config_Size2) getConfig();
				if(getHeight()<config.height){
				    setTop(getTop()-(int)dy);
				}
				else{
					setTop(getBottom()-config.height);
				}
			}
		}

		@Override
		public void onMoveToDown(View p1, MotionEvent p2, float dy)
		{
			if(getOrientation()==VERTICAL)
			{
				if(getHeight()>hander.getHeight()){
				    setTop(getTop()+(int)dy);
				}
				else{
					setTop(getBottom()-hander.getHeight());
				}
			}
		}

		@Override
		public boolean onMoveEnd(View p1, MotionEvent p2)
		{
			if(p2.getAction()==MotionEvent.ACTION_UP)
			{
				Config_hesSize config= (DownBar.Config_hesSize) getConfig();
				if(getOrientation()==VERTICAL)
				{
					int height = getBottom()-getTop();
					if((!config.isOpen && height>config.height*0.2) || (height>config.height*0.8)){
						open();
					}
					else if((config.isOpen && height<config.height*0.8) || (height<config.height*0.2)){
						close();
					}
					//close状态下，只要height大于总高度的0.2，就可以抬起来了，反之收起
					//open状态下，只要height小于总高度的0.8，就可以收起来了，反之抬起
				}
				else if(getOrientation()==HORIZONTAL)
				{
					int width = getRight()-getLeft();
					if((!config.isOpen && width>config.width*0.2) || (width>config.width*0.8)){
						open();
					}
					else if((config.isOpen && width<config.width*0.8) || (width<config.width*0.2)){
						close();
					}
				}
			}
			invalidate();
			return true;
		}

		@Override
		public void calc(MotionEvent p2)
		{
			Log.i("DownBar","Touch");
			//存储绝对坐标
			if(p2.getActionMasked()==MotionEvent.ACTION_DOWN){
				lastX=p2.getRawX();
				lastY=p2.getRawY();
			}
			else if(p2.getHistorySize()>0){
				nowX=p2.getRawX();
				nowY=p2.getRawY();
			}
		}

	}

/*
----------------------------------------------------------------------------------------------------------------------------------------------------------------------

  DownBar之三大类
  
  DownBarCreator和Config_hesView很简单，这里不再赘述，
  
  Config_hesSize不仅锁定DownBar的大小，还旋转自己和子元素，并重新排列子元素
  
-----------------------------------------------------------------------------------
*/

    /* 一顿操作后，DownBar所有成员都分配好了空间 */
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
			target.setVector(new PageList(target.getContext()));
		}
	}

	/* 配置我的成员 */
	static class Config_hesView implements Level<DownBar>
	{

		@Override
		public void ConfigSelf(DownBar target)
		{
			target.hander.setBackgroundColor(0);
			target.setBackgroundColor(0);
			target.vector.setBackgroundColor(0xff333333);
		}

		@Override
		public void config(DownBar target){}

		@Override
		public void clearConfig(DownBar target){}
		
	}

	/* 管理我的大小和子元素 */
	static class Config_hesSize extends Config_Size2<DownBar>
	{

		public boolean isOpen;
		
		@Override
		public void onChange(DownBar target, int src)
		{
			super.onChange(target, src);
			trim( target.hander,getHanderSize());
			trim( target.vector,getVectorSize());
			trim( target,getHanderSize());
			target.setOrientation(CastFlag(flag));
			//将自己和子元素旋转，并设置排列方向
		}

		@Override
		public void onPort(DownBar target, int src)
		{
			super.onPort(target, src);
			target.setTranslationX(0);
			target.setTranslationY(-getHanderSize().end);
			//将把手从边缘挤上来，以感知事件
		}

		@Override
		public void onLand(DownBar target, int src)
		{
			super.onLand(target, src);
			target.setTranslationY(0);
			target.setTranslationX(-getHanderSize().start);
		}
			
		public size getHanderSize()
		{
			if(flag==Configuration.ORIENTATION_PORTRAIT){
			    return new size(width,(int)(height*0.1));
			}
			else if(flag==Configuration.ORIENTATION_LANDSCAPE){
				return new size((int)(width*0.1),height);
			}
			return null;
		}
		public size getVectorSize()
		{
			if(flag==Configuration.ORIENTATION_PORTRAIT){
			    return new size(width,(int)(height*0.9));
			}
			else if(flag==Configuration.ORIENTATION_LANDSCAPE){
				return new size((int)(width*0.9),height);
			}
			return null;
		}

	}

}
