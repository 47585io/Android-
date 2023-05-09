package com.mycompany.who.Edit.Base;

import java.util.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;

public abstract interface EditMoudle
{

	public static abstract interface Creat<T>
	{
		public abstract void Creat()

		public abstract T CreatOne()

		public abstract void CopyFrom(T target)

		public abstract void CopyTo(T target)
	}

	
	public static abstract interface Sizer
	{
		public abstract int maxWidth()
		
		public abstract int maxHeight()
		
		public abstract size WAndH()
	}

	
	public static abstract interface Liner{

		public abstract int getLineCount()

		public abstract void onLineChange(int start,int before,int after)

	}
	
	
	public static abstract interface LineSpiltor extends Liner{

		public abstract void reLines(int line)

		public abstract void addLines(int count)

		public abstract void delLines(int count)

	}
	
	
	public static abstract interface SelectionSeer{}
	
	
	public static abstract interface Drawer
	{	
		public abstract void reDraw(int start,int end)
	}


	public static abstract interface Formator
	{
		public abstract int Format(int start, int end)

		public abstract int Insert(int index, int count)
	}


	public static abstract interface Completor
	{
		public abstract void openWindow()
		
		public abstract void closeWindow()
	}


	public static abstract interface Canvaser{}
	
	
	public static abstract interface Runnar
	{
		public abstract String MakeCommand(String state)
		
		public abstract int RunCommand(String command)
	}
	
	
	public static abstract interface UedoWithRedo
	{
		public abstract void Uedo()

		public abstract void Redo()
	}
	
}
