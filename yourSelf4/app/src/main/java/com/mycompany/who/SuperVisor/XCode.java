package com.mycompany.who.SuperVisor;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;

import com.mycompany.who.Edit.*;

import java.io.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.View.Backgroud;
import com.mycompany.who.Share.myRet;
import com.mycompany.who.R;
import com.mycompany.who.Share.Share;
import java.util.*;

import android.graphics.drawable.Drawable;
import java.util.jar.*;
import android.util.*;
import java.security.acl.*;
import com.mycompany.who.View.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;


public class XCode extends LinearLayout
{
	
	private PageList mEditGroupPages;
	
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
	}	
	public PageList getEditGroupPages(){
		return mEditGroupPages;
	}
	
	
	
}
