package com.mycompany.who.SuperVisor.CodeMoudle.Base.View3;

import android.content.res.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import android.util.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.EditMoudle.*;


/*  CodeBlock给出构建一个复杂View的核心方法和核心类  */
public abstract interface CodeBlock
{

	public abstract void init()

	public abstract void config()

	public abstract void ShiftConfig(Level Configer)

	public abstract void loadSize(int width, int height ,int is)

	public abstract Config_Size getConfig()

	
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
			if(id!=0){
				try{
				    tmp =  LayoutInflater.from(target.getContext()).inflate(id,target);
				}catch(Exception e){
					Log.e("CodeBlock Inflater Error",e.toString());
				}
			}
			init(target,tmp);
		}

		abstract public void init(T target,View root)

	}
	
	public static abstract interface Level<T> extends Configer<T>{

		public abstract void config(T target)

		public abstract void clearConfig(T target)

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

		public int getWidth()
		
		public int getHeight()
		
		public int getFlag()
		
	}

	public static class Config_Size2<T> implements Config_Size<T>
	{

		public int width,height,flag;

		@Override
		public void ConfigSelf(T target){}

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
		public int getWidth(){
			return width;
		}

		@Override
		public int getHeight(){
			return height;
		}

		@Override
		public int getFlag(){
			return flag;
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

		public int CastFlag(int flag)
		{
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
			//如果大小相同，则不设置
			//就算设置了，也不立即改变大小，而是延迟到下次onLayout时生效，以节省时间
			ViewGroup.LayoutParams p = Father.getLayoutParams();
			if(p.width!=width||p.height!=height){
				p.width = width;
				p.height = height;
				//Father.setLayoutParams(p);
			}
		}
		
		final public static void trim(View Father, size s)
		{
			//调整空间
			ViewGroup.LayoutParams p = Father.getLayoutParams();
			if(p.width!=s.start||p.height!=s.end){
				p.width = s.start;
				p.height = s.end;
				//Father.setLayoutParams(p);
			}
		}
		
		final public static void trim(View v,int left,int top,int right,int bottom)
		{
			//layout只设置View的范围，但并不立即改变，这或许等待到下次改变，但也许被父View重置
			if(v.getLeft()!=left || v.getRight()!=right || v.getTop()!=top || v.getBottom()!=bottom){
				v.layout(left,top,right,bottom);
			}
		}
		
		final public static void trimAdd(View Father, int addWidth, int addHeight)
		{
			if(addWidth==0&&addHeight==0)
				return;
			ViewGroup.LayoutParams p = Father.getLayoutParams();
			p.width += addWidth;
			p.height += addHeight;
			//Father.setLayoutParams(p);
		}
		
		final public static void trimXel(View Father, float WidthX, float HeightX)
		{
			if(WidthX==1&&HeightX==1)
				return;
			ViewGroup.LayoutParams p = Father.getLayoutParams();
			p.width *= WidthX;
			p.height *= HeightX;
			//Father.setLayoutParams(p);
		}
		
		

	}

}
