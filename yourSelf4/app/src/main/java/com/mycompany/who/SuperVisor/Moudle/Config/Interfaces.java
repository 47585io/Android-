package com.mycompany.who.SuperVisor.Moudle.Config;
import android.content.res.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Share.Share1.*;

public class Interfaces
{
	
	public static interface Init{

		public void loadSize(int width, int height ,int is)

		public void init()
		
		public void config()
		
		public void ShiftConfig(Level Configer)
		
		public Config_Size getConfig()
		
		/* 非常好用 */
		public static abstract class Creator<T extends ViewGroup> implements Configer<T>{

			public int id;

			public Creator(int resid){
				id=resid;
			}
			@Override
			public void ConfigSelf(T target)
			{
				View tmp = null;
				if(id!=0)
				    tmp =  LayoutInflater.from(target.getContext()).inflate(id,target);
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
			
			public size getSize()
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
			public size getSize()
			{
				return new size(width,height);
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
			
			final public static void trim(View Father, int width, int height)
			{
				//调整空间
				ViewGroup.LayoutParams p = Father.getLayoutParams();
				p.width = width;
				p.height = height;
				Father.setLayoutParams(p);
			}
			final public static void trim(View Father, size s)
			{
				//调整空间
				ViewGroup.LayoutParams p = Father.getLayoutParams();
				p.width = s.start;
				p.height = s.end;
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

		}

	}
	
	public static interface BubbleEvent{
		
		public boolean onKeyUp(int keyCode, KeyEvent event)
		
		public boolean onTouchEvent(MotionEvent event)
		
		public boolean BubbleKeyEvent(int keyCode,KeyEvent event)
		
		public boolean BubbleMotionEvent(MotionEvent event)
		
		public void setTarget(BubbleEvent target)
		
		public BubbleEvent getTarget()
		
	}
	
}
