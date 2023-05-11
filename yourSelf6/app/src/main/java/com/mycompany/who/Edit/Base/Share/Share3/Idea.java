package com.mycompany.who.Edit.Base.Share.Share3;

abstract public interface Idea
{
	
	public abstract boolean can(CharSequence s,CharSequence want,int start);
	
	
	public static final Idea ino = new INo();
	
	public static final Idea iyes = new Iyes();
	
	
	public static class INo implements Idea{
		@Override
		public boolean can(CharSequence s,CharSequence want,int start)
		{
			if(s.toString().toLowerCase().indexOf(want.toString().toLowerCase(),start)==start){
				//字符串出现位置必须在start
				return true;
			}
			return false;
		}
	}
	public static class Iyes implements Idea{
		@Override
		public boolean can(CharSequence s,CharSequence want,int start)
		{
			if(s.toString().toLowerCase().indexOf(want.toString().toLowerCase(),start)!=-1){
				////字符串出现位置可以在start后
				return true;
			}
			return false;
		}
	}
	
}
	
