package com.mycompany.who.Edit.Base.Share.Share4;
import java.util.*;

public class TwoStack<T>
{
	private Stack<T> UedoList;
	private Stack<T> RedoList;

	public TwoStack()
	{
		UedoList=new Stack<>();
		RedoList=new Stack<>();
	}

	public void put(T token){
		UedoList.push(token);
	}
	public void Reput(T token){
		RedoList.push(token);
	}

	public T getLast()
	{	
	    if (UedoList.size() == 0)
			return null;
		return UedoList.pop();
	}
	public T getNext()
	{
		if (RedoList.size() == 0)
			return null;
		return RedoList.pop();
	}

	public T seeLast(){
		if (UedoList.size() == 0)
			return null;
		return UedoList.peek();
	}
	public T seeNext(){
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

	public void clear(){
		UedoList.clear();
		RedoList.clear();
	}
}
