package com.mycompany.who.Edit.Share;

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

	public void put(int start, int end, String src)
	{
		UedoList.push(new Token(start, end, src));
	}
	public void Reput(int start, int end, String src)
	{
		RedoList.push(new Token(start, end, src));
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

	public static class Token
	{
		Token(int start, int end, String src)
		{
			this.start=start;
			this.end=end;
			this.src=src;
		}
		public int start;
		public int end;
		public String src;
	}
}


