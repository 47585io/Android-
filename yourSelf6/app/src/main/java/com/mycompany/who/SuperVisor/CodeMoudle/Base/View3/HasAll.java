package com.mycompany.who.SuperVisor.CodeMoudle.Base.View3;
import android.widget.*;
import android.content.*;
import android.view.*;
import android.util.*;


/*
  才发现接口太多了，如果每个组件都必须实现一次，直接累死了
  
  所以这里将大部分组件的接口实现在这里，以后直接继承即可

*/
public class HasAll extends LinearLayout implements Configer<ViewGroup>,BubbleEvent,CodeBlock
{

	protected BubbleEvent Target;
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
	public void setTarget(BubbleEvent target)
	{
		//默认不支持冒泡，因为这会增加事件分发的复杂度
		//Target=target;
	}
	@Override
	public BubbleEvent getTarget()
	{
		return Target;
	}
	

	@Override
	public void ConfigSelf(ViewGroup target)
	{
		// TODO: Implement this method
	}
	
	
}
