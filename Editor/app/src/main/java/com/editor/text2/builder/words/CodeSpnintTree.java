package com.editor.text2.builder.words;
import android.text.*;
import com.editor.text.*;

public class CodeSpnintTree
{
	
	CodeSpnint mTreeRoot;
	public static final char BindowStart = '{';
	public static final char BindowEnd = '}';
	
	
	public void build(CharSequence text, int start, int end)
	{
		
	}
	
	public void decode(CharSequence text, int start, int end)
	{
		
	}
	
	//在指定代码块中的指定位置插入代码和代码块
	public void insert(CodeSpnint spint, int index, CharSequence text, int start, int end)
	{
		
	}
	
	//删除指定代码块中的指定范围内的代码和代码块
	public void delete(CodeSpnint spint, int start, int end)
	{
		if(spint.begin.length()<start){
			//要删除的范围包含我的起始文本中
			spint.begin.delete(start, end<=spint.begin.length() ? end:spint.begin.length());
		}
		for(;spint.next!=null;spint=spint.next)
		{
			
		}
	}
	
	public void findSpint(CodeSpnint spint,final int index,final int start)
	{
		if(start+spint.begin.length()>=index){
			//它处于我的起始文本中
			return;
		}
		if(start+spint.length>=index){
			//它处于文本块之内，加上起始文本长度后走到第一个子元素
			findSpint(spint.child,index,start+spint.begin.length());
			return;
		}
		if(start+spint.divider.length()>=index){
			//它处于分界中
			return;
		}
		//它处于我之后的文本块
		findSpint(spint.next,index,start+spint.length);
	}
	
	public void foreach(CodeSpnint spnint)
	{
		//先遍历自己的内容，在遍历child中，会将自己代码块内的内容遍历完
		spnint.begin.length();
		if(spnint.child!=null){
			foreach(spnint.child);
		}	
		//遍历同层级下个代码块的内容
		for(;spnint.next!=null;spnint=spnint.next){	
			spnint.next.divider.length();
			foreach(spnint.next);
		}
		spnint.end.length();
	}
	
	public static class CodeSpnint
	{
		//代码块的总长度，包含其子代码块长度
		int length;
		//同一层级的下个代码块
		CodeSpnint next;
		//下一层级的第一个孩子代码块
		CodeSpnint child;
		//分界代码段，分隔在此代码块和下个代码块之间的文本
		Editable divider;
		//遗漏的开头的一段文本
		Editable begin;
		//遗漏的末尾的一段文本
		Editable end;
		//可以在代码块中存储额外的东西
		Object date;
	}
}
