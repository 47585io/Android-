package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.text.*;
import android.util.*;
import android.widget.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;


/*
  对齐文本
  
  editor表示编辑器的文本容器，start和end分别表示文本格式化的起始和末尾，nowIndex表示文本格式到了哪儿，beforeIndex表示最后一次之前的位置
  
  三个抽象方法顺次调用，dothing_Start和dothing_End只在起始和末尾调一次，而dothing_Run只要没到end就一直调
  
*/
public abstract class myEditFormatorListener extends myEditListener implements EditFormatorListener
{
	
	abstract protected int dothing_Run(Editable editor, int nowIndex);
	//开始做事
	
	abstract protected int dothing_Start(Editable editor, int nowIndex,int start,int end);
	//为了避免繁琐的判断，一开始就调用start方法，将事情初始化为你想要的样子
	
	abstract protected int dothing_End(Editable editor, int beforeIndex,int start,int end);
	//收尾工作
	
	
	@Override
	public void LetMeFormat(int start, int end, Editable editor)
	{
		int beforeIndex = start;
		int nowIndex=start;

		nowIndex = dothing_Start(editor, nowIndex, start, end);
		for (;nowIndex < end && nowIndex != -1;)
		{
			beforeIndex = nowIndex;
			nowIndex = dothing_Run(editor, nowIndex);
			//您可以在上次返回一个index，这个index决定下次传入的nowIndex
		}
		nowIndex =  dothing_End(editor, beforeIndex, start, end);		
	}
	
	
	/* 从起始位置开始，反向把字符串中的want替换为to */
	final public static void reSAll(int start, int end, String want, CharSequence to,Editable editor)
	{
		int len = want.length();
		String src=editor.toString().substring(start, end);
		int nowIndex = src.lastIndexOf(want);
		while (nowIndex != -1)
		{
			editor.replace(nowIndex + start, nowIndex + start + len, to);	
			nowIndex = src.lastIndexOf(want, nowIndex - 1);
		}
	}
	
}
