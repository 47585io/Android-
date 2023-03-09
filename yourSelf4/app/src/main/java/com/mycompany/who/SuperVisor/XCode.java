package com.mycompany.who.SuperVisor;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;

import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;

import java.io.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.View.Backgroud;
import com.mycompany.who.Share.myRet;
import com.mycompany.who.R;
import com.mycompany.who.Share.Share;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import android.graphics.drawable.Drawable;
import java.util.jar.*;
import android.util.*;
import java.security.acl.*;
import com.mycompany.who.View.*;


public class XCode extends LinearLayout
{
	
	private PageList mEditGroupPages;
	private List<XCode.Extension> Extensions;
	
	private KeyPool keyPool;
	private HashMap<String,Runnable> keysRunnar;
	
	public XCode(Context cont){
		super(cont);
		init(cont);
	}	
	public XCode(Context cont,AttributeSet set){
		super(cont,set);
		init(cont);
	}
	private void init(Context cont){
		mEditGroupPages = new PageList(cont);
		Extensions = new ArrayList<>();
	}	
	public PageList getEditGroupPages(){
		return mEditGroupPages;
	}
	
	public void addAExtension(Extension extension)
	{
		Extensions.add(extension);
	}
	public void delAExtension(Extension e)
	{
		Extensions.remove(e);
	}
	
	public static abstract class Extension
	{
		public String name;
		public String path;
		public List<Integer> ids;
		
		public Extension(){
			ids=new ArrayList<>();
		}
		
		public abstract void oninit(EditText self)
		public abstract EditListener getFinder()
		public abstract EditListener getDrawer()
		public abstract EditListener getFormator()
		public abstract EditListener getInsertor()
	  	public abstract EditListener getCompletor()
		public abstract EditListener getCanvaser()
		
		public EditListener F(){
			EditListener F = getFinder();
			if(F!=null)
				ids.add(F.hashCode());
			return F;
		}
		public EditListener D(){
			EditListener D = getDrawer();
			if(D!=null)
				ids.add(D.hashCode());
			return D;
		}
		public EditListener M(){
			EditListener M = getFormator();
			if(M!=null)
				ids.add(M.hashCode());
			return M;
		}
		public EditListener I(){
			EditListener I = getInsertor();
			if(I!=null)
				ids.add(I.hashCode());
			return I;
		}
		public EditListener C(){
			EditListener C = getCompletor();
			if(C!=null)
				ids.add(C.hashCode());
			return C;
		}
		public EditListener V(){
			EditListener V = getCanvaser();
			if(V!=null)
				ids.add(V.hashCode());
			return V;
		}
	}
	
	
	
	interface EditItrator{
		public void Config(CodeEdit Edit);
	}
	private void forech(EditItrator tor){
		int i;
		for(i=0;i<mEditGroupPages.getChildCount();){
			View v= mEditGroupPages.getChildAt(i);
			if(v instanceof EditGroup){
				List<CodeEdit> EditList = ((EditGroup)v).getEditList();
				for(CodeEdit Edit:EditList)
				    tor.Config(Edit);
			}
		}
	}
	
	
}
