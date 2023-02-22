package com.mycompany.who.Activity;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.mycompany.who.Activity.*;
import com.mycompany.who.Share.*;
import com.mycompany.who.View.*;

import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import com.mycompany.who.*;

public class TitleActivity extends EditActivity
{
	public static int TiHeight=125;
	
	@Override
	public void TouchCollector()
	{
		setOnTouchListenrS(Tilte,menu,EditNames,ButtonBar,floatWindow,getBarButton(0),getBarButton(1),getBarButton(2));
		super.TouchCollector();
	}

	@Override
	public void whenTouchView()
	{
		clearFloatWindow();
		super.whenTouchView();
	}
	
	
	
	public void setDis(boolean dis){
		Display display = getWindowManager().getDefaultDisplay();  
		Displaywidth=display.getWidth();
	    Displayheight=display.getHeight();

		if(dis){
			Tilte.setVisibility(View.VISIBLE);
		    TiHeight=125;
			EditFather.setY(TiHeight);
			//can=false;
		}
		else{
			Tilte.setVisibility(View.GONE);
			TiHeight=0;
			EditFather.setY(0);
			MainActivity.dismiss_DownBar(this);
			can=true;
	
		}
	}
    
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		if(TiHeight==0&&can&&keyCode==KeyEvent.KEYCODE_BACK){
			//全屏模式下，拦截第一次返回键作为退出全屏
			setDis(true);
			return true;
		}
		return super.onKeyUp(keyCode,event);
	}

	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		//BaseActivity.dismiss_Title_And_ActionBar(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        //取消标题
		super.onCreate(savedInstanceState);
	}

	@Override
	public void configActivity()
	{
		super.configActivity();
		configMenu();
		configEditList();
		configTi();
		setDis(true);
		TitleButtonBar_URW();
	}
	
	
	
	public void configMenu(){
		ArrayAdapter<String> adpter= new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
		onMyMenuCreat(adpter);
		menu.setAdapter(adpter);
		menu.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{
					onMyMenuSelected(p3);
				}
				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{
				}
			});	
		
	}
	// 菜单
	public void onMyMenuCreat(ArrayAdapter menu){}

	protected void onMyMenuSelected(int id){}
	
	
	
	public void configTi(){
		Tilte.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View p1)
				{
					clearFloatWindow();
				}
			});
		
	}
	
	
	//按扭栏
	public View getBarButton(int index){
		return ButtonBar.getChildAt(index);
	}
	public void addBarButton(View but,int index){
		ButtonBar.addView(but,index);
	}
	public void delBarButton(int index){
		if(index>ButtonBar.getChildCount()-2)
			return;
		ButtonBar.removeViewAt(index);
	}
	public void messBarButton(final int index,final String mess){
		getBarButton(index).setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1)
				{
					int x= getBarButtonX(index);
					setFloatWindow(0xff888888,mess);
					openFloatWindow(x,Tilte.getHeight()+50);
					new Handler().postDelayed(new Runnable(){

							@Override
							public void run()
							{
								clearFloatWindow();
							}
						}, 3000);
					return true;
				}
			});
	}
	public int getBarButtonX(int index){
		int len = 0;
		for(;index<ButtonBar.getChildCount();index++){
			len+= ButtonBar.getChildAt(index).getWidth();
		}
		return Tilte.getWidth()-len;
	}

	
	//编辑器列表
	public void configEditList(){
		  EditNames. setBackgroundResource(0);
	      EditNames.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{
					onEditItemSelected(p3);			
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{
				}
			});
		EditNames.setonSelectionListener(new ReSpinner.onSelectionListener(){

				@Override
				public void onRepeatSelected(int postion)
				{
					onEditItemRepeatSelected(postion);
				}
			});
			
	}
	
	protected void onEditItemSelected(int postion)
	{
		//当EditNames选项被选中，tab到指定的页
		Code.tabView(postion);
		if (Share.getFileIcon(files.getPageAt(postion).getPath()) == R.drawable.file_type_html)
		//如果是页面是HTML，可以增加一个Viewer，否则去掉
			TitleButtonBar_V(3);
		else if(Share.isImage(files.getPageAt(postion).getPath())){
			//如果是页面是图片，可以增加一个大，否则去掉
			TitleButtonBar_A(3);
		}
		else
			TitleButtonBar_N(3);
		
		clearFloatWindow();
		
	}
	
	protected void onEditItemRepeatSelected(final int postion)
	{
		//选项重复的选择，用户可以关闭编辑器
		floatWindow.removeAllViews();
		ListView list= setFloatWindow(0xff333333,"   关闭         ","   关闭所有");
		list.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					switch(p3){
						case 0:
							onBeforesaveFile(postion);
							files.save(postion);
							Code.removeView(postion);
							break;
						case 1:
							onBeforesaveAllFile();
							files.save();
							Code.delAll();
							break;
					}
					files.refresh(TitleActivity.this, EditNames);
					if (files.getNowIndex() != -1)
						EditNames.setSelection(files.getNowIndex());
					clearFloatWindow();
				}
			});
		openFloatWindow(0,TiHeight);
	}
	
	public void onBeforesaveFile(int position){}
	public void onBeforesaveAllFile(){}
	
	
	public void TitleButtonBar_V(int index){}
	
	public void TitleButtonBar_U(int index)
	{
		getBarButton(index).setVisibility(View.VISIBLE);
		getBarButton(index).setBackgroundResource(R.drawable.Uedo);
		//getBarButton(index).setBackgroundDrawable(new MenuDraw(0xff222222,0xffcccccc));
		getBarButton(index).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if (files.getEditAt(files.getNowIndex()) == null)
						return;
					files.getEditAt(files.getNowIndex()).Uedo();
				}
			});
		messBarButton(index, "撤销");
	}
	public void TitleButtonBar_R(int index)
	{
		getBarButton(index).setVisibility(View.VISIBLE);
		getBarButton(index).setBackgroundResource(R.drawable.Redo);
		getBarButton(index).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if (files.getEditAt(files.getNowIndex()) == null)
						return;
					files.getEditAt(files.getNowIndex()).Redo();
				}
			});
		messBarButton(index, "恢复");
	}
	public void TitleButtonBar_W(int index)
	{
		getBarButton(index).setVisibility(View.VISIBLE);

		if (files.getCanWrite())
		    getBarButton(index).setBackgroundResource(R.drawable.write);
		else
		    getBarButton(index).setBackgroundResource(R.drawable.read);

		getBarButton(index).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if (files.getCanWrite())
					{
						p1.setBackgroundResource(R.drawable.read);
						files.setCanWrite(false);
					}
					else
					{
						p1.setBackgroundResource(R.drawable.write);
						files.setCanWrite(true);		
					}
				}
			});
		messBarButton(index, "禁止输入");
	}
	
	public void TitleButtonBar_A(int index)
	{
		getBarButton(index).setVisibility(View.VISIBLE);
		getBarButton(index).setBackgroundResource(R.drawable.All);
		getBarButton(index).setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					setDis(false);
				}
			});
		messBarButton(index,"全屏");
	}


	public void TitleButtonBar_N(int index)
	{
		getBarButton(index).setVisibility(View.GONE);
	}

	public void TitleButtonBar_URW()
	{
		//默认的按扭栏
		TitleButtonBar_U(0);
		TitleButtonBar_R(1);
		TitleButtonBar_W(2);	
	}
	
	
}




	
	

