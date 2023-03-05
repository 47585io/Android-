package com.mycompany.who.Activity;

import android.os.*;
import android.view.*;
import android.widget.*;
import com.mycompany.who.*;
import com.mycompany.who.View.*;
import android.widget.AdapterView.*;
import android.graphics.drawable.*;

public class BaseActivity3 extends BaseActivity2
{
	
	protected RelativeLayout Tilte;
	protected ReSpinner menu;
	protected ReSpinner EditNames;
	protected LinearLayout ButtonBar;
	protected RelativeLayout floatWindow;
	protected RelativeLayout EditFather;
	
	protected ImageButton slidingopen;
	protected SlidingDrawer port;
	protected SlidingDrawer land;
	
	protected TextView portText;
	protected TextView landText;

	protected LinearLayout portvector;
	protected LinearLayout landvector;
	
	protected Spinner portselect;
	protected Spinner landselect;

	protected RelativeLayout re;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit);	
		initActivity();
		configActivity();
	}
	
	protected void initActivity(){
		
		Tilte=findViewById(R.id.Tilte);
		menu=findViewById(R.id.menu);
		EditNames=findViewById(R.id.EditList);
		ButtonBar=findViewById(R.id.buttonBar);
		floatWindow=findViewById(R.id.floatWindow);
		
		port=findViewById(R.id.portSlide);
		land=findViewById(R.id.landSlide);
		slidingopen=findViewById(R.id.slidingopentor);
		portText=findViewById(R.id.text2);
		landText=findViewById(R.id.text);
		portvector=findViewById(R.id.portVector);
		landvector=findViewById(R.id.landVector);
		portselect=findViewById(R.id.portSpinner);
		landselect=findViewById(R.id.landSpinner);
		
		re=findViewById(R.id.editRelativeLayout);
		EditFather=findViewById(R.id.editFather);
	    
	}
	public void configActivity(){
		
	}
	protected void clearFloatWindow(){
		
	}
	public ListView setFloatWindow(int bgcolor,String... items){
		floatWindow.removeAllViews();
		floatWindow.setBackgroundColor(bgcolor);
		ListView list=new ListView(this);
		list.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,items));
		list.setDivider(null);
		floatWindow.addView(list);
		return list;
	}
	
	public Button creatButton(String text,int size,int color){
		Button but = new Button(this);
		but.setText(text);
		but.setBackgroundColor(color);
		but.setWidth(size);
		return but;
	}
	public TextView creatText(String text,int color){
		TextView but = new TextView(this);
		but.setText(text);
		but.setTextColor(color);
		return but;
	}
	
	
}
