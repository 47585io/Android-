package com.who.Edit.Base.Share.Share1;

public class range
{
	public int start;
	public int end;

	public range(int start,int end){
		this.start=start;
		this.end=end;
	}
	public range(range o){
		start = o.start;
		end = o.end;
	}
	public range(){}

	@Override
	public boolean equals(Object other){
		if(start==((range)other).start && end==((range)other).end )
			return true;
		return false;
	}
	public boolean equals(int start,int end){
		if(this.start==start && this.end==end)
			return true;
		return false;
	}

	public void set(int s,int e){
		start=s;
		end=e;
	}
	public void set(range s){
		start = s.start;
		end = s.end;
	}

}
