package com.mycompany.who.Activity;
import android.content.res.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.mycompany.who.Share.*;
import com.mycompany.who.SuperVisor.*;
import com.mycompany.who.View.*;
import java.io.*;

import android.view.View.OnClickListener;

public class DownBarActivity extends TitleActivity
{

	public int now=0;
	public FileList filechooser;
	public EditList slidHandler;
	public ListView FileList;
	public ListView SearchResult;
	public ListView logCat;
	
	@Override
	public void TouchCollector()
	{
		// TODO: Implement this method
		super.TouchCollector();
		try{
		setOnTouchListenrS(slidingopen,port,land,FileList,portText,landText);
		}catch(Exception e){}
	}
	
	public void onConfigurationChanged(Configuration config)
	{
		Display display = getWindowManager().getDefaultDisplay();  
		Displaywidth=display.getWidth();
	    Displayheight=display.getHeight();
		
		//棋竖屏切换时，切换底部栏
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			port.setVisibility(View.VISIBLE);
			land.close();
			//必须关闭，否则bug
			land.setVisibility(View.GONE);
			landvector.removeAllViews();
			//清空子元素
			now=0;
			//如果切换至竖屏，则关闭land，并且现在是port
		}

		else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			port.close();
			port.setVisibility(View.GONE);
			land.setVisibility(View.VISIBLE);
			portvector.removeAllViews();
			now=1;
			//否则关闭port，且现在为land
		}
		tabApage(slidHandler.getNowIndex());
		//把子元素换到另一个中
		
		super.onConfigurationChanged(config);

	}
	
	
	@Override
	protected void initActivity()
	{
		super.initActivity();
		filechooser=new FileList();
		slidHandler=new EditList();
		FileList=new ListView(this);
		SearchResult=new ListView(this);
		logCat=new ListView(this);
	}
	

	@Override
	public void configActivity()
	{
		super.configActivity();
		if(Displaywidth>Displayheight){
			port.setVisibility(View.GONE);
			now=1;
			//横屏，则屏蔽port，并且现在是land
		}
		else{
			land.setVisibility(View.GONE);
			now=0;
			//否则屏蔽land，并且现在是port
		}
		MarginLayoutParams lay= (ViewGroup.MarginLayoutParams) port.getLayoutParams();
		lay.height=Displayheight/10*4;
		port.setLayoutParams(lay);
		lay = (ViewGroup.MarginLayoutParams) land.getLayoutParams();
		lay.width=Displayheight/10*4;
		land.setLayoutParams(lay);
		
		filechooser.refresh(this, FileList);
		configFileList(FileList);
		configSlidingopen();
		configSelctor(portselect);
		configSelctor(landselect);
		initPage();
	}
	
	public void initPage(){
		addApage("文件列表",FileList);
		addApage("搜索结果",SearchResult);
		addApage("输出控制台",logCat);
	}
	
	
	public TextView getnowText(){
		if (now == 0)
			return portText;
		else if (now == 1)
			return landText;
		return null;
	}
	public SlidingDrawer getnowSlid(){
		if (now == 0)
			return port;
		else if (now == 1)
			return land;
		return null;
	}
	public ViewGroup getnowVector(){
		if (now == 0)
			return portvector;
		else if (now == 1)
			return landvector;
		return null;
	}
	
	public void configSelctor(Spinner sel){
		sel.setOnItemSelectedListener(new OnItemSelectedListener(){

				@Override
				public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
				{
					//其实子元素已经父元素无关了，不管怎么样，选项被选中，都将把指定page添加到当前的vector
					tabApage(p3);
				}

				@Override
				public void onNothingSelected(AdapterView<?> p1)
				{
					// TODO: Implement this method
				}
			});
	}
	
	public void addApage(String name,View...S){
		Page page = new Page(this,null,name);
		getnowText().setText(name);
		for(View s:S){
			page.addView(s);
		}
		slidHandler.addAEdit(page,getnowVector());
	    slidHandler.refreshText(this,portselect);
		slidHandler.refreshText(this,landselect);
		//两个slid共享选项
		
	}
	public void tabApage(int index){
		getnowText().setText(slidHandler.getPageAt(index).getName());
		slidHandler.tabAEdit(index,getnowVector());
		//一是page，二是改当前文本
	}
	
	public void configSlidingopen(){
		slidingopen.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1)
				{
					if(getnowSlid().isOpened())
				        getnowSlid().animateClose();
				    else
						getnowSlid().animateOpen();
				}
			});
	}
	
	public void configFileList(ListView list)
	{
		list.setDivider(null);
		list.setPadding(40,0,0,0);
		list.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					File f = null;	
					f=filechooser.getFile(p3);
					
					if (!f.isDirectory())
						onFileChooser(f);
					else{
						filechooser.refreshDate();
						filechooser.refresh(DownBarActivity.this, FileList);
						getnowText().setText(f.getPath());
					}
				}
			});
		list.setOnItemLongClickListener(new OnItemLongClickListener(){

				@Override
				public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					floatWindow.setBackgroundColor(0xff333333);
					if(p3==0)
						ToCreat();
					else
						ToDel(p3);
					
					return true;
					//长按后不再触发其它事件
				}
			});

		list.setOnTouchListener(new onmyTouchListener(){

				@Override
				public boolean ontouch(View p1, MotionEvent p2)
				{
					clearFloatWindow();
					return false;
				}
			});
	}

	
	public void ToCreat(){
		ListView list= setFloatWindow(0xff333333,new String[]{"创建一个新的文件","创建一个新的文件夹"});
		list.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, final int p3, long p4)
				{
					input.setlistener(new WhenInputEnter(){
							@Override
							public void whenenter(String text)
							{
								switch(p3){
									case 0:
										filechooser.addAfile(text);
										break;
									case 1:
										filechooser.addAFolder(text);
										break;
								}
								filechooser.refresh(DownBarActivity.this,FileList);
								clearFloatWindow();
							}
						});
					input.show();
				}
			});
		openFloatWindow(MouseRx,MouseRy);
	}
	public void ToDel(final int index){
		
		ListView list= setFloatWindow(0xff333333,new String[]{"重命名","删除文件"});
		list.setOnItemClickListener(new OnItemClickListener(){
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					switch(p3){
						case 0:
							input.show();
							input.setlistener(new WhenInputEnter(){
								@Override
								public void whenenter(String text)
								{
								    filechooser.Rename(index,text);
									filechooser.refresh(DownBarActivity.this,FileList);
								}
							});
							break;
						case 1:
							filechooser.delAfile(index);
							filechooser.refresh(DownBarActivity.this,FileList);
							break;
					}
					clearFloatWindow();
				}
			});
		openFloatWindow(MouseRx,MouseRy-120);
		getnowText().setText("已选择："+filechooser.getFile(index).getPath());
	}
	
	
	protected void onFileChooser(File f)
	{
		String path=f.getPath();
		int index=0;
		index=files.contrans(path);
		if (index != -1)
		{
			EditNames.setSelection(index);
			Code.tabView(index);
		}//文件存在就不用加载任何东西

		else if (Share.unknowFile(path))
			return;
		else if(Share.isGif(path)){
			WebViewer web = new WebViewer(this);
			Share.setToWeb(web,path);
			Code.addView(path, web);
		}
		else if (Share.isImage(path))
		{	
			ImageView i=new ImageView(this);
			Share.setImage(i,path);
		    Code.addView(path, i);
		}
		else
		{
			Code.addView(path);
		}
		Code.refresh(EditNames);
	}

	@Override
	public void onBeforesaveFile(int position)
	{  
	    if(files.getPageAt(position).getEdit()==null)
	       return;
		String path= files.getPageAt(position).getPath();
		if(!new File(path).exists())
			return;
		myRet ret=new myRet(path);
		String src=ret.r("UTF-8");
	    myLog dst = new myLog( files.getPageAt(position).getPath()+".bak");

		dst.e(src,true);
		dst.close();
		super.onBeforesaveFile(position);
		
	}

	@Override
	public void onBeforesaveAllFile()
	{
		for(int i=0;i<=files.getNowIndex();i++){
			onBeforesaveFile(i);
		}
		super.onBeforesaveAllFile();
	}
	
	
}









