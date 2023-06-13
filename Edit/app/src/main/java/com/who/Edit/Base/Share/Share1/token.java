package com.who.Edit.Base.Share.Share1;

public class token extends range
{
	public CharSequence src;

	public token(){}
	public token(int start,int end,CharSequence src){
		this.start=start;
		this.end=end;
		this.src=src;
	}
	public token(token o){
		start = o.start;
		end = o.end;
		src = o.src;
	}

	public void set(int s,int e,CharSequence src){
		this.start=s;
		this.end=e;
		this.src = src;
	}
	public void set(token t){
		this.start=t.start;
		this.end=t.end;
		this.src = t.src;
	}

}
