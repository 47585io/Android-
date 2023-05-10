package com.mycompany.who.Edit.Base.Share.Share1;

/*  无论如何都浪费4个字节  */
public class size
{
	public int start;
	public int end;
	
	public size(int start,int end){
		this.start=start;
		this.end=end;
	}
	public size(size o){
		start = o.start;
		end = o.end;
	}
	public size(){}
	
	@Override
	public boolean equals(Object other)
	{
		if(start==((wordIndex)other).start && end==((wordIndex)other).end )
			return true;
		return false;
	}
	
	public void set(int s,int e){
		start=s;
		end=e;
	}
	
}
