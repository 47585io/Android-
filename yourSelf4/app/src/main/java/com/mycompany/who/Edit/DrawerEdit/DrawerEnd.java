package com.mycompany.who.Edit.DrawerEdit;
import android.content.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;

public class DrawerEnd extends DrawerBaseForLuagua
{

	private FinderFactory mfactory;
	
	@Override
	public void setLuagua(String Lua)
	{
		laugua=Lua;
		switch(Lua){
			case "text":
				setFinder(mfactory.getTextFinder());
				break;
			case "xml":
				setFinder(mfactory.getXMLFinder());
				break;
			case "java":
				setFinder(mfactory.getJavaFinder());
				break;
			case "css":
				setFinder(mfactory.getCSSFinder());
				break;
			case "html":
				setFinder(mfactory.getHTMLFinder());
				break;
			default:
			    setFinder(null);
		}
	}
	
	
	public DrawerEnd(Context cont){
		super(cont);
		mfactory =new FinderFactory();
	}
	public DrawerEnd(Context cont,DrawerEnd Edit){
		super(cont,Edit);
		mfactory =new FinderFactory();
	}
	
	
	public class FinderFactory{
		public EditListener getTextFinder(){
			return new FinderText();
		}
		public EditListener getXMLFinder(){
			return new FinderXML();
		}
		public EditListener getJavaFinder(){
			return new FinderJava();
		}
		public EditListener getCSSFinder(){
			return new FinderCSS();
		}
		public EditListener getHTMLFinder(){
			return new FinderHTML();
		}
		
	}
	
	public FinderFactory getFinerFactory(){
		return mfactory;
	}
	
}
