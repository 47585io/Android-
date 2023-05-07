package com.mycompany.who.Edit.Base.Share.Share1;

public class wordIndex extends size
{
	//单个词的范围和颜料标签
	//对于查找词和替换，它们是要替换单个词的范围，以及要替换字符串重复的次数
	
	public byte b;
	
	public wordIndex(){}
	public wordIndex(int start,int end,byte b){
		this.start=start;
		this.end=end;
		this.b=b;
	}
	public wordIndex(wordIndex o){
		start = o.start;
		end = o.end;
		b = o.b;
	}
	
	public boolean equals2(Object other){
		if(start>=((wordIndex)other).start && end<=((wordIndex)other).end )
			return true;
		return false;
	}

	public void set(int s,int e,byte b){
		this.start=s;
		this.end=e;
		this.b=b;
	}
	
	
}

