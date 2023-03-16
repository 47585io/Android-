package com.mycompany.who.Edit.ListenerVistor;


import java.util.*;
import com.mycompany.who.Edit.ListenerVistor.EditListener.*;

public class EditListenerInfo
{
	public EditListener mlistenerF;
	
	public EditListener mlistenerD;
	
	public EditListener mlistenerM;
	
	public EditListener mlistenerI;
	
	public List<EditListener> mlistenerCS;
	
	public List<EditListener> mlistenerVS;
	
	public void addAListener(EditListener li){
		
		if(li==null)
			return;
			
		if(li instanceof EditFinderListener){
			mlistenerF=li;
		}
		else if(li instanceof EditDrawerListener){
			mlistenerD=li;
		}
		else if(li instanceof EditFormatorListener){
			mlistenerM=li;
		}
		else if(li instanceof EditInsertorListener){
			mlistenerI=li;
		}
		else if(li instanceof EditCompletorListener){
			mlistenerCS.add(li);
		}
		else if(li instanceof EditCanvaserListener){
			mlistenerVS.add(li);
		}
	}
	
	public void delAListener(EditListener li){
		if(li==null)
			return;
		
		if(li.equals(mlistenerD)){
			mlistenerD=null;
		}
		else if(li.equals(mlistenerF)){
			mlistenerF=null;
		}
		else if(li.equals(mlistenerM)){
			mlistenerM=null;
		}
		else if(li.equals(mlistenerI)){
			mlistenerI=null;
		}
		mlistenerCS.remove(li);
		mlistenerVS.remove(li);
	}
	
	public EditListener findListener(){
		return null;
	}
	
}
