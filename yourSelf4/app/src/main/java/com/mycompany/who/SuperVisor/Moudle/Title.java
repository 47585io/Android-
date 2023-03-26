package com.mycompany.who.SuperVisor.Moudle;
import android.content.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.*;
import com.mycompany.who.SuperVisor.Moudle.Config.*;
import com.mycompany.who.View.*;
import android.content.res.*;


/*
  标题栏，
  
  我想完成ReSpinner切换编辑器的梦
  
  我想用一些按扭来封装编辑器功能
  
*/
public class Title extends HasAll 
{

	@Override
	public void init()
	{
		new TitleCreator(R.layout.Title).ConfigSelf(this);
		new Config_Level().ConfigSelf(this);
	}
	
	protected ReSpinner Spinner;
	protected LinearLayout ButtonBar;
	public Config_hesSize config;
	
	public Title(Context cont){
		super(cont);
		init();
	}
	public Title(Context cont,AttributeSet attrs){
		super(cont,attrs);
		init();
	}
	
	public ReSpinner getReSpinner(){
		return Spinner;
	}
	public LinearLayout getButtonBar(){
		return ButtonBar;
	}
	
	public static class Config_hesSize implements Config_Size<Title>
	{
		
		public int TitleWidth=1000,TitleHeight=200;
		public int portOrLand;

		@Override
		public void set(int width, int height, int is, Title target)
		{
			TitleWidth=width;
			TitleHeight=height;
			portOrLand=is;
			onChange(target);
		}

		@Override
		public void change(Title target)
		{
			int tmp = TitleWidth;
			TitleWidth = TitleHeight;
			TitleHeight = tmp;
			if(portOrLand==LinearLayout.VERTICAL)
				portOrLand=LinearLayout.HORIZONTAL;
			else
				portOrLand=LinearLayout.VERTICAL;
			
			onChange(target);
		}

		@Override
		public void onChange(Title target)
		{
			trim(target.Spinner,TitleWidth/2,TitleHeight);
			trim(target.ButtonBar,TitleWidth/2,TitleHeight);
			trim(target,TitleWidth,TitleHeight);
			target.ButtonBar.setOrientation(portOrLand);
			target.setOrientation(portOrLand);
		}

		@Override
		public void ConfigSelf(Title target)
		{
			
		}
	}

	@Override
	public void loadSize(int width, int height, int is)
	{
		config.set(width,height,is,this);
		super.loadSize(width, height, is);
	}

	@Override
	protected void onConfigurationChanged(Configuration newConfig)
	{
		config.change(this);
		super.onConfigurationChanged(newConfig);
	}
	
	
	
	//一顿操作后，Title所有成员都分配好了空间
	final static class TitleCreator extends Creator<Title>
	{

		public TitleCreator(int i){
			super(i);
		}

		@Override
		public void init(Title target, View root)
		{
			target.Spinner = root.findViewById(R.id.ReSpinner);
			target.ButtonBar = root.findViewById(R.id.ButtonBar);
		    target.config = new Config_hesSize();
		}

	}

	// 如何配置View层次结构
	final class Config_Level implements Level<Title>
	{

		@Override
		public void config(Title target)
		{
			
		}

		@Override
		public void ConfigSelf(Title target)
		{
			
		}

	}
	
}
