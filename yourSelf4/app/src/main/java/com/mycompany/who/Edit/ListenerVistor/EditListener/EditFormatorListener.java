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
    
	/*
	public static class ModifyBuffer{
		StringBuilder src;
		StringBuilder target;
		int start;
		public ModifyBuffer(int start,String s,String t){
			src=new StringBuilder(s);
			target=new StringBuilder(t);
			this.start=start;
		}
		synchronized public void insert(int off,String s){
			src.insert(off,s);
			target.insert(off-start,s);
		}
		synchronized public void delete(int s,int e){
			src.delete(s,e);
			target.delete(s-start,e-start);
		}
		synchronized public void replace(int s,int e,String c)
		{
			src.replace(s,e,c);
			target.replace(s-start,e-start,c);
		}

		@Override
		public String toString()
		{
			return target.toString();
		}

		public String getSrc(){
			return src.toString();
		}
		public int length(){
			return target.length();
		}

		synchronized public void reSAll(String want, String to){
			int nowIndex = target.lastIndexOf(want);
			while (nowIndex != -1)
			{
				//从起始位置开始，反向把字符串中的want替换为to
				src.replace(nowIndex + start, nowIndex + start + want.length(), to);	
				target.replace(nowIndex,nowIndex+ want.length(),to);
				nowIndex = target.lastIndexOf(want, nowIndex - 1);
			}
		}

	}
	*/
	
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

