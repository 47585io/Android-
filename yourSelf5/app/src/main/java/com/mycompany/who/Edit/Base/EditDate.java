package com.mycompany.who.Edit.Base;

import java.util.*;

public class EditDate
{
	private Stack<Token> UedoList;
	private Stack<Token> RedoList;
	
	public EditDate()
	{
		UedoList=new Stack<>();
		RedoList=new Stack<>();
	}

	public void put(int start, int end, CharSequence src)
	{
		UedoList.push(new Token(start, end, src));
	}
	public void Reput(int start, int end, CharSequence src)
	{
		RedoList.push(new Token(start, end, src));
	}
	public void put(Token token){
		UedoList.push(token);
	}
	public void Reput(Token token){
		RedoList.push(token);
	}

	public Token getLast()
	{	
	    if (UedoList.size() == 0)
			return null;
		return UedoList.pop();
	}
	public Token getNext()
	{
		if (RedoList.size() == 0)
			return null;
		return RedoList.pop();
	}
	
	public Token seeLast(){
		if (UedoList.size() == 0)
			return null;
		return UedoList.peek();
	}
	public Token seeNext(){
		if (RedoList.size() == 0)
			return null;
		return RedoList.peek();
	}

	public int Usize(){
		return UedoList.size();
	}
	public int Rsize(){
		return RedoList.size();
	}
	
	public static class Token
	{
		public Token(int start, int end, CharSequence src)
		{
			this.start=start;
			this.end=end;
			this.src=src;
		}
		public int start;
		public int end;
		public CharSequence src;
		//支持保存Span文本
	}
}


