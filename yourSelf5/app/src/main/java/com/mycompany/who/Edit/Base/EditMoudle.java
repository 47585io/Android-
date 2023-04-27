package com.mycompany.who.Edit.Base;

public class EditMoudle
{

	public static interface Creat<T>
	{
		public void Creat()

		public T CreatOne()

		public void CopyFrom(T target)

		public void CopyTo(T target)
	}


	public static interface Drawer
	{	
		public void reDraw(int start,int end)
	}


	public static interface Formator
	{
		public void Format(int start, int end)

		public int Insert(int index)
	}


	public static interface Completor
	{
		public void openWindow()
		
		public void closeWindow()
	}


	public static interface Canvaser{}


	public static interface UedoWithRedo
	{
		public void Uedo()

		public void Redo()
	}
}
