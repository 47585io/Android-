package com.mycompany.who.Edit.Share;

public class wordIndex{
	//单个词的范围和颜料标签
	//对于查找词和替换，它们是要替换单个词的范围，以及要替换字符串重复的次数
	public wordIndex(int start,int end,byte b){
		this.start=start;
		this.end=end;
		this.b=b;
	}
	public wordIndex(){
		this.start=0;
		this.end=0;
		this.b=0;
	}
	@Override
	public boolean equals(Object other)
	{
		if(start==((wordIndex)other).start && end==((wordIndex)other).end )
			return true;
		return false;
	}

	public boolean equals2(Object other){
		if(start>=((wordIndex)other).start && end<=((wordIndex)other).end )
			return true;
		return false;
	}
	
	public int start;
	public int end;
	public byte b;
	
	
	public void set(int s,int e,byte b){
		start=s;
		end=e;
		this.b=b;
	}
}

