package com.mycompany.who.Edit.ListenerVistor.EditListener;

import android.text.*;
import android.util.*;
import android.widget.*;

public abstract class EditFormatorListener extends EditListener
{
	public abstract int dothing_Run(EditText self, int nowIndex);
	//开始做事
	public abstract int dothing_Start(EditText self, int nowIndex,int start,int end);
	//为了避免繁琐的判断，一开始就调用start方法，将事情初始化为你想要的样子
	public abstract int dothing_End(EditText self, int beforeIndex,int start,int end);
	//收尾工作
	
	final public void LetMeFormat(int start, int end, EditText self)
	{
		if (!Enabled())
			return ;

		try
		{
			Format(start,end,self);
		}
		catch (IndexOutOfBoundsException e)
		{
			Log.e("Formating Error", toString()+" "+e.toString());
			//格式化的过程中出现了问题，返回原字符串
		}
	}
	
	protected void Format(int start, int end, EditText self){
		
		int beforeIndex = 0;
		int nowIndex=start;
		
		nowIndex = dothing_Start(self, nowIndex, start, end);
		
		for (;nowIndex < end && nowIndex != -1;)
		{
			beforeIndex = nowIndex;
			nowIndex = dothing_Run(self, nowIndex);
		}
		nowIndex =  dothing_End(self, beforeIndex, start, end);		
		
	}
}

