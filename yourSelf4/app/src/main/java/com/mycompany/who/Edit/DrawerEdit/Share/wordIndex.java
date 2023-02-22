package com.mycompany.who.Edit.DrawerEdit.Share;

public class wordIndex{
	//单个词的范围和颜料标签
	//对于查找词和替换，它们是要替换单个词的范围，以及要替换字符串重复的次数
	public wordIndex(int start,int end,byte b){
		this.start=start;
		this.end=end;
		this.b=b;
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
	
	
	public static class wordIndexS extends wordIndex{
		//扩展了更强大的功能，例如现在你可以将<和>替换为&lt;和&gt;了
		//不过为了节约内存，不建议使用
		wordIndexS(int start,int end,byte b,String to){
			super(start,end,b);
			this.to=to;
		}
		public String to;
	}
}

