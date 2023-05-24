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
  
  注意: DownBar为了保证在每次父元素onLayout时不会将自己缩回，设置了固定大小，需要父元素将自己摆放到合适位置
  
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
	public View getHander(){
		return hander;
	}
	public View getVector(){
		return vector;
	}

	public void open()
	{
		Config_hesSize config= (DownBar.Config_hesSize) getConfig();
		config.isOpen=true;
		if(getOrientation()==HORIZONTAL){
			AnimatorColletion.getTranstion(this,getTranslationX(),getTranslationY(),0,getTranslationY()).start();
		}
		else if(getOrientation()==VERTICAL){
			AnimatorColletion.getTranstion(this,getTranslationX(),getTranslationY(),getTranslationX(),0).start();
		}
	}
	public void close()
	{
		Config_hesSize config= (DownBar.Config_hesSize) getConfig();
		config.isOpen=false;
		if(getOrientation()==HORIZONTAL){
			AnimatorColletion.getTranstion(this,getTranslationX(),getTranslationY(),config.getVectorWidth(),getTranslationY()).start();
		}
		else if(getOrientation()==VERTICAL){
			AnimatorColletion.getTranstion(this,getTranslationX(),getTranslationY(),getTranslationX(),config.getVectorHeight()).start();
		}
	}

	
/*
-----------------------------------------------------------------------------------
 
  HanderTouch，Handler如何感知触摸事件？
  
  非常感谢，OnTouchToMove提供了大量的好用接口，并允许我们重写坐标计算方式
  
  以下HanderTouch提供横向和纵向两种方向的滑动和感知
  
  当横向时，只有onMoveToLeft与onMoveToRight生效。当纵向时，只有onMoveToTop与onMoveToDown生效
  
  Handler追踪手指滑动方向来判断DownBar向哪个方向慢慢打开，具体过程是:
	 
  Handler保留上次的坐标，与本次坐标相比计算出差距，然后将DownBar内部画布平移，演示滑动效果
  
  为了避免自己移动而导致坐标出错，Handler永远感知绝对坐标，因为它是相对于屏幕而非Handler本身
  
  在手指抬起来时，Handler会根据当前已打开的大小和isOpen状态进行直接完全打开或完全关闭
  
-----------------------------------------------------------------------------------
*/
	class HanderTouch extends OnTouchToMove
	{
		
		@Override
		public void onMoveToLeft(View p1, MotionEvent p2, float dx)
		{
			if(getOrientation()==HORIZONTAL)
			{
				float x = getTranslationX()-dx;
				x = x<0 ? 0:x;
				setTranslationX(x);
			}
		}

		@Override
		public void onMoveToRight(View p1, MotionEvent p2, float dx)
		{
			if(getOrientation()==HORIZONTAL)
			{
				float x = getTranslationX()+dx;
				int width = ((Config_hesSize)config).getVectorWidth();
				x = x>width ? width:x;
				setTranslationX(x);
			}
		}

		@Override
		public void onMoveToTop(View p1, MotionEvent p2, float dy)
		{
			if(getOrientation()==VERTICAL)
			{
				float y = getTranslationY()-dy;
				y = y<0 ? 0:y;
				setTranslationY(y);
			}
		}

		@Override
		public void onMoveToDown(View p1, MotionEvent p2, float dy)
		{
			if(getOrientation()==VERTICAL)
			{
				float y = getTranslationY()+dy;
				int height = ((Config_hesSize)config).getVectorHeight();
				y = y>height ? height :y;
				setTranslationY(y);
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
					float height = config.getHeight()-getTranslationY();
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
					float width = config.getWidth()-getTranslationX();
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
			//消耗事件以供滑动
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
		    target.setVector(new PageHandler(target.getContext()));
			//预设一个Hander和Vector
		}
	}

	/* 配置我的成员 */
	public static class Config_hesView implements Level<DownBar>
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
	public static class Config_hesSize extends Config_Size2<DownBar>
	{

		public boolean isOpen;
		public int handerWidth,handerHeight;
		public int vectorWidth,vectorHeight;
		
		@Override
		public void onChange(final DownBar target, int src)
		{
			super.onChange(target, src);
			trim(target.hander,handerWidth,handerHeight);
			trim(target.vector,vectorWidth,vectorHeight);
			trim(target,width,height);
			target.setOrientation(CastFlag(flag));
			//将自己和子元素旋转，并设置排列方向
			
			float x = target. getTranslationX();
			float y = target. getTranslationY();
			target.setTranslationX(y);
			target.setTranslationY(x);
			//交换设置的x轴与y轴的平移值
		}

		@Override
		public void onPort(DownBar target, int src)
		{
			//记录大小
			handerWidth = width;
			handerHeight = (int)(height*0.1);
			vectorWidth = width;
			vectorHeight = (int)(height*0.9);
			super.onPort(target, src);
		}

		@Override
		public void onLand(DownBar target, int src)
		{
			handerWidth = (int)(width*0.1);
			handerHeight = height;
			vectorWidth = (int)(width*0.9);
			vectorHeight = height;
			super.onLand(target, src);
		}
		
		public int getHanderWidth()
		{
			return handerWidth;
		}
		public int getHanderHeight()
		{
			return handerHeight;
		}
		public int getVectorWidth()
		{
			return vectorWidth;
		}
		public int getVectorHeight()
		{
			return vectorHeight;
		}

	}

}
