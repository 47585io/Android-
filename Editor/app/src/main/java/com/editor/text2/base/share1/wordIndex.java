package com.editor.text2.base.share1;

public class wordIndex extends range
{
	//单个词的范围和Span
	public Object span;
	public int flags;

	public wordIndex(){}
	public wordIndex(int start,int end,Object span,int flags){
		this.start=start;
		this.end=end;
		this.span=span;
		this.flags=flags;
	}
	public wordIndex(wordIndex o){
		start = o.start;
		end = o.end;
		span = o.span;
		flags = o.flags;
	}

	public void set(int s,int e,Object span,int flags){
		this.start=s;
		this.end=e;
		this.span=span;
		this.flags=flags;
	}
	public void set(wordIndex node){
		this.start=node.start;
		this.end=node.end;
		this.span=node.span;
		this.flags = node.flags;
	}

}
