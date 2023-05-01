package com.mycompany.who.Edit.Base;

import java.util.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;

public class EditMoudle
{

	public static interface Creat<T>
	{
		public void Creat()

		public T CreatOne()

		public void CopyFrom(T target)

		public void CopyTo(T target)
	}

	
	public static interface Sizer
	{
		public int maxWidth()
		
		public int maxHeight()
		
		public size WAndH()
	}

	
	public static interface Drawer
	{	
		public void reDraw(int start,int end)
	}


	public static interface Formator
	{
		public void Format(int start, int end)

		public int Insert(int index, int count)
	}


	public static interface Completor
	{
		public void openWindow()
		
		public void closeWindow()
	}


	public static interface Canvaser{}
	
	
	public static interface Runnar
	{
		public String MakeCommand(String state)
		
		public void RunCommand(String command)
	}
	
	
	public static interface UedoWithRedo
	{
		public void Uedo()

		public void Redo()
	}
	
}
