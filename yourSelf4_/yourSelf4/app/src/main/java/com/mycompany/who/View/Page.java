package com.mycompany.who.View;

import android.content.*;
import android.widget.*;
import com.mycompany.who.Edit.*;
import android.view.*;
import android.os.*;

public class Page extends LinearLayout
{
	public CodeEdit Edit=null;
	public String EditName="";
	public String path="";
	public Page(Context cont, CodeEdit Edit, String path){

		super(cont);
		this.Edit=Edit;
		if (path != null)
		{
			this. path=path;
		    EditName=path.substring(path.lastIndexOf('/') + 1, path.length());
		}
	}

	public CodeEdit getEdit()
	{
		return Edit;
	}
	public String getName()
	{
		return EditName;
	}
	public String getPath()
	{
		return path;
	}

	public void zoomBy(float size){
		
	}

}
