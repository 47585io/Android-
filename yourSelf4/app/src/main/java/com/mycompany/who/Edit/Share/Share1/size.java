package com.mycompany.who.Edit.Share.Share1;

public class size
{
	public int start;
	public int end;
	
	public size(int start,int end){
		this.start=start;
		this.end=end;
	}
	public size(){
		this.start=0;
		this.end=0;
	}
	
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
