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
	protected Creator Creator;
	protected Level Configer;
	
	public HasAll(Context cont){
		super(cont);
		init();
	}
	public HasAll(Context cont,AttributeSet attrs){
		super(cont,attrs);
		init();
	}
	
	/*调用初始化成员*/
	@Override
	public void init(){}

	/*调用配置成员*/
	@Override
	public void config(){
		if(Configer!=null)
			Configer.ConfigSelf(this);
	}

	/*更改配置*/
	public void ShiftConfig(Level Configer){
		if(this.Configer!=null)
		    this.Configer.clearConfig(this);
		this.Configer = Configer;
		config();
	}
	
	/*锁定大小*/
	@Override
	public void loadSize(int width, int height, int is)
	{
		if(config!=null)
			config.set(width,height,is,this);
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
			return Target. onKeyUp(keyCode,event);
		return is;
	}

	@Override
	public boolean BubbleMotionEvent(MotionEvent event)
	{
		boolean is = super.onTouchEvent(event);
		if(Target!=null)  
			return Target.onTouchEvent(event);
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
		
		public void clearConfig(T target)
		
	}
	
	
	/*
	 Config_hesSize 

	 锁定我的大小，使用设置的大小

	 自动根据横竖屏改变大小

	 */
	public static interface Config_Size<T> extends Configer<T>{

		public void set(int width,int height,int is,T target)

		public void change(T target,int is)

		public void onChange(T target,int src)
	}
	
	
	public static class Config_Size2<T> implements Config_Size<T>
	{

		public int width,height,flag;
		
		@Override
		public void ConfigSelf(T target)
		{
			// TODO: Implement this method
		}

		@Override
		public void set(int width, int height, int is, T target)
		{
			int tmp = flag;
			this.width = width;
			this.height = height;
			this.flag = is;
			onChange(target,tmp);
		}

		@Override
		public void change(T target,int is)
		{
			if(is == flag)
				return;
			//屏幕方向与原来相同，不用change
			
			int tmp = width;
			width = height;
			height = tmp;
			tmp = flag;
			if(flag==Configuration.ORIENTATION_PORTRAIT){
				flag=Configuration.ORIENTATION_LANDSCAPE;
			}
			else if(flag==Configuration.ORIENTATION_LANDSCAPE){
				flag=Configuration.ORIENTATION_PORTRAIT;
			}
			//将所有值取反
			onChange(target,tmp);
		}

		@Override
		public void onChange(T target,int src)
		{
			//port or land
			if(flag==Configuration.ORIENTATION_PORTRAIT){
				onPort(target,src);
			}
			else if(flag==Configuration.ORIENTATION_LANDSCAPE){
				onLand(target,src);
			}
		}
		
		public void onPort(T target,int src){}
		
		public void onLand(T target,int src){}
		
		public int CastFlag(int flag){
			//将屏幕方向转化为排列方向
			if(flag==Configuration.ORIENTATION_PORTRAIT){
				return LinearLayout.VERTICAL;
			}
			else if(flag==Configuration.ORIENTATION_LANDSCAPE){
				return LinearLayout.HORIZONTAL;
			}
			return -9999;
		}
		
	}
	
	
	public static interface Init extends Interfaces.Init{
		
		public void ShiftConfig(Level Configer)
		
	}
	
}
