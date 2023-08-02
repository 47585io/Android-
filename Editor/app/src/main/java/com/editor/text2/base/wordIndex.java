package com.editor.text2.base;

public class wordIndex extends range
{
	//单个词的范围和Span
	public Object span;

	public wordIndex(){}
	public wordIndex(int start,int end,Object span){
		this.start=start;
		this.end=end;
		this.span=span;
	}
	public wordIndex(wordIndex o){
		start = o.start;
		end = o.end;
		span = o.span;
	}

	public void set(int s,int e,Object span){
		this.start=s;
		this.end=e;
		this.span=span;
	}
	public void set(wordIndex node){
		this.start=node.start;
		this.end=node.end;
		this.span=node.span;
	}

}
