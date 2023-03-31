package com.mycompany.who.SuperVisor.Moudle;
import android.widget.*;
import android.content.*;
import android.util.*;
import com.mycompany.who.SuperVisor.Moudle.Config.*;
import android.view.*;
import com.mycompany.who.R;

/*
  底部栏

  不出意外，很麻烦
*/
public class DownBar extends HasAll
{
	
	protected SlidingDrawer slid;
	protected LinearLayout vector;
	protected ImageView handle;
	
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
		Creator = new DownBarCreator(R.layout.DownBar);
		Configer = new Config_hesView();
	}
	
	
	static class Config_hesSize extends Config_Size2<DownBar>{
		
	}
	
	static class DownBarCreator extends Creator<DownBar>
	{
		DownBarCreator(int id){
			super(id);
		}

		@Override
		public void init(DownBar target, View root)
		{
			target.config=new Config_hesSize();
			target.slid = root.findViewById(R.id.Slide);
			target.vector = root.findViewById(R.id.content);
			target.handle = root.findViewById(R.id.handle);
		}
	}
	
	static class Config_hesView implements Level<DownBar>
	{

		@Override
		public void ConfigSelf(DownBar target)
		{
			// TODO: Implement this method
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
	
}
