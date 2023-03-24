package com.mycompany.who.SuperVisor.Moudle;
import android.widget.*;
import android.content.*;
import android.util.*;
import com.mycompany.who.View.*;
import com.mycompany.who.SuperVisor.Config.*;
import android.view.*;
import com.mycompany.who.*;


/*
  标题栏，
  
  我想完成ReSpinner切换编辑器的梦
  
  我想用一些按扭来封装编辑器功能
  
*/
public class Title extends LinearLayout implements EditGroup.Init, Configer<Title>
{

	@Override
	public void init()
	{
		new TitleCreator(R.layout.Title).ConfigSelf(this);
		new Config_Level().ConfigSelf(this);
	}
	

	@Override
	public void loadSize(int width, int height, boolean is)
	{
		// TODO: Implement this method
	}

	@Override
	public void ConfigSelf(Title target)
	{
		// TODO: Implement this method
	}
	
	
	protected ReSpinner Spinner;
	protected LinearLayout ButtonBar;
	
	
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
	
	public static class Config_hesSize implements PageHandler.Config_Size<Title>
	{

		@Override
		public void set(int width, int height, boolean is, Title target)
		{
			// TODO: Implement this method
		}

		@Override
		public void change(Title target)
		{
			// TODO: Implement this method
		}

		@Override
		public void onChange(Title target)
		{
			// TODO: Implement this method
		}

		@Override
		public void ConfigSelf(Title target)
		{
			// TODO: Implement this method
		}
	}
	
	//一顿操作后，PageHandler所有成员都分配好了空间
	final static class TitleCreator extends EditGroup.Creator<Title>
	{

		public TitleCreator(int i){
			super(i);
		}

		@Override
		public void init(Title target, View root)
		{
			
		}

	}

	// 如何配置View层次结构
	final class Config_Level implements EditGroup.Level<Title>
	{

		@Override
		public void config(Title target)
		{
			// TODO: Implement this method
		}

		@Override
		public void ConfigSelf(Title target)
		{
			// TODO: Implement this method
		}

	}
	
}
