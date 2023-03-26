package com.mycompany.who.SuperVisor.Moudle.Config;
import android.widget.*;
import android.content.*;
import android.view.*;
import java.text.*;
import android.util.*;
import android.content.res.*;
import com.mycompany.who.SuperVisor.Moudle.Config.Interfaces.*;


/*
  才发现接口太多了，如果每个组件都必须实现一次，直接累死了
  
  所以这里将大部分组件的接口实现在这里，以后直接继承即可

*/
public class HasAll extends LinearLayout implements Configer<ViewGroup>,Interfaces.BubbleEvent,Interfaces.Init
{

	protected Interfaces.BubbleEvent Target;
	protected Config_Size config;
	
	public HasAll(Context cont){
		super(cont);
		init();
	}
	public HasAll(Context cont,AttributeSet attrs){
		super(cont,attrs);
		init();
	}
	
	@Override
	public void init()
	{
	}
	@Override
	public void loadSize(int width, int height, int is)
	{
		
	}
	public Config_Size getConfig(){
		return config;
	}

	final public static void trim(View Father, int width, int height)
	{
		//调整空间
		ViewGroup.LayoutParams p = Father.getLayoutParams();
		p.width = width;
		p.height = height;
		Father.setLayoutParams(p);
	}
	final public static void trimAdd(View Father, int addWidth, int addHeight)
	{
		ViewGroup.LayoutParams p = Father.getLayoutParams();
		p.width += addWidth;
		p.height += addHeight;
		Father.setLayoutParams(p);
	}
	final public static void trimXel(View Father, float WidthX, float HeightX)
	{
		ViewGroup.LayoutParams p = Father.getLayoutParams();
		p.width *= WidthX;
		p.height *= HeightX;
		Father.setLayoutParams(p);
	}

	//在事件发生时，向上冒泡
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		return BubbleKeyEvent(keyCode,event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{	
	   	return BubbleMotionEvent(event);
	}
	
	//向上冒泡，若没有目标则原地Return
	@Override
	public boolean BubbleKeyEvent(int keyCode, KeyEvent event)
	{
		boolean is = super.onKeyUp(keyCode,event);
		if(Target!=null)
			return Target. BubbleKeyEvent(keyCode,event);
		return is;
	}

	@Override
	public boolean BubbleMotionEvent(MotionEvent event)
	{
		boolean is = super.onTouchEvent(event);
		if(Target!=null)  
			return Target.BubbleMotionEvent(event);
		return is;
	}
	
	@Override
	public void setTarget(Interfaces.BubbleEvent target)
	{
		Target=target;
	}
	@Override
	public Interfaces.BubbleEvent getTarget()
	{
		return Target;
	}
	

	@Override
	public void ConfigSelf(ViewGroup target)
	{
		// TODO: Implement this method
	}
	
	
	
	/* 非常好用 */
	public static abstract class Creator<T extends ViewGroup> implements Configer<T>{

		public int id;

		public Creator(int resid){
			id=resid;
		}
		@Override
		public void ConfigSelf(T target)
		{
			View tmp =  LayoutInflater.from(target.getContext()).inflate(id,target);
			init(target,tmp);
		}

		abstract public void init(T target,View root)

	}
	public static interface Level<T> extends Configer<T>{
		
        public void config(T target)
		
	}
	
	
	/*
	 Config_hesSize 

	 锁定我的大小，使用设置的大小

	 自动根据横竖屏改变大小

	 */
	public static interface Config_Size<T> extends Configer<T>{

		public void set(int width,int height,int is,T target)

		public void change(T target)

		public void onChange(T target)
	}
	
	
	public static class Config_Size2<T> implements Config_Size<T>
	{

		int width,height,portOrLand;
		
		@Override
		public void ConfigSelf(T target)
		{
			// TODO: Implement this method
		}

		@Override
		public void set(int width, int height, int is, T target)
		{
			this.width = width;
			this.height = height;
			this.portOrLand = is;
			onChange(target);
		}

		@Override
		public void change(T target)
		{
			int tmp = width;
			width = height;
			height = tmp;
			if(portOrLand==LinearLayout.VERTICAL)
				portOrLand=LinearLayout.HORIZONTAL;
			else
				portOrLand=LinearLayout.VERTICAL;
			onChange(target);
		}

		@Override
		public void onChange(T target)
		{
			// TODO: Implement this method
		}
	}
}
