package com.editor.text.base;

public class pos
{
	public float x;
	public float y;

	public pos(float x,float y){
		this.x=x;
		this.y=y;
	}
	public pos(pos o){
		x = o.x;
		y = o.y;
	}
	public pos(){}

	@Override
	public boolean equals(Object other){
		if(x==((pos)other).x && y==((pos)other).y )
			return true;
		return false;
	}
	public boolean equals(float x,float y){
		if(this.x==x && this.y==y )
			return true;
		return false;
	}

	public void set(float x,float y){
		this. x=x;
		this.y = y;
	}
	public void set(pos s){
		x = s.x;
		y = s.y;
	}
}
