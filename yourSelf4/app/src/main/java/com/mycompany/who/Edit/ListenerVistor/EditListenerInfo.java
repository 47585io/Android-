package com.mycompany.who.Edit.ListenerVistor;


import java.util.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;

public class EditListenerInfo
{
	public List<EditListener> mlistenerFS;
	
	public EditListener mlistenerD;
	
	public EditListener mlistenerM;
	
	public List<EditListener> mlistenerIS;
	
	public List<EditListener> mlistenerCS;
	
	public List<EditListener> mlistenerVS;
	
	
	public EditListenerInfo(){
		mlistenerFS = new ArrayList<>();
		mlistenerIS = new ArrayList<>();
		mlistenerVS = new ArrayList<>();
		mlistenerCS = new ArrayList<>();				
	}
	
	
	public boolean addAListener(EditListener li){
		
		if(li==null)
			return false;
			
		if(li instanceof EditFinderListener){
			mlistenerFS.add(li);
			return true;
		}
		else if(li instanceof EditDrawerListener){
			mlistenerD=li;
			return true;
		}
		else if(li instanceof EditFormatorListener){
			mlistenerM=li;
			return true;
		}
		else if(li instanceof EditInsertorListener){
			mlistenerIS.add(li);
			return true;
		}
		else if(li instanceof EditCompletorListener){
			mlistenerCS.add(li);
			return true;
		}
		else if(li instanceof EditCanvaserListener){
			mlistenerVS.add(li);
			return true;
		}
		return false;
	}
	
	public boolean delAListener(EditListener li){
		if(li==null)
			return false;
		
		if(li.equals(mlistenerD)){
			mlistenerD=null;
			return true;
		}
		else if(mlistenerFS.remove(li)){
			return true;
		}
		else if(li.equals(mlistenerM)){
			mlistenerM=null;
			return true;
		}
		else if(mlistenerIS.remove(li)){
			return true;
		}
		else if(mlistenerCS.remove(li))
			return true;
		else if(mlistenerVS.remove(li))
			return true;
		return false;
	}
	
	public EditListener findListener(String name){
		return null;
	}
	
}
