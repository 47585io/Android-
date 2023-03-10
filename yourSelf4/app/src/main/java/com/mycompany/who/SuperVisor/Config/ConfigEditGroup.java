package com.mycompany.who.SuperVisor.Config;
import com.mycompany.who.SuperVisor.*;
import com.mycompany.who.Edit.Share.*;
import android.view.*;
import android.widget.*;

public class ConfigEditGroup
{
	
	public static class Config_hesWindowSize implements Configer<EditGroup>
	{
		public boolean portOrLand;
		public int WindowHeight=600, WindowWidth=600;
		public int selfWidth,selfHeight;
		
		@Override
		public void ConfigSelf(EditGroup target)
		{
			int height=MeasureWindowHeight(target.mWindow);
			if(portOrLand==ConfigViewWith_PortAndLand.Port){
				WindowWidth=WindowHeight=(int)(selfWidth*0.9);
			}
			else{
				WindowWidth=(int)(selfWidth*0.7);
				WindowHeight= (int)(selfWidth*0.3);
			}
			EditGroup.trim(target,selfWidth,selfHeight);
			if (height < WindowHeight)
				EditGroup.trim(target.mWindow,WindowWidth,height);
			else
				EditGroup.trim(target.mWindow,WindowWidth,WindowHeight);
		}
		
		public void set(int width,int height,boolean is){
			selfWidth=width;
			selfHeight=width;
			portOrLand=is;
		}
		public void change(){
			int tmp = selfWidth;
			selfWidth=selfHeight;
			selfHeight=selfWidth;
			portOrLand=!portOrLand;
		}
		public static int MeasureWindowHeight(ListView mWindow)
		{
			int height=0;
			int i;
			WordAdpter adapter= (WordAdpter) mWindow.getAdapter();
			for (i = 0;i < adapter.getCount();i++)
			{
				View view = adapter.getView(i, null, mWindow);
				view.measure(0, 0);
				height += view.getMeasuredHeight();
				//若View没有明确设定width和height时，它的大小为0
				//可以measure方法测量它的大小，这样测量的大小会被保存，然后获取测量的高
				//注意，getWidth不等于getMeasuredHeight
			}

			return height;
		}
		
	}
	
	
	
	
	
	
}
