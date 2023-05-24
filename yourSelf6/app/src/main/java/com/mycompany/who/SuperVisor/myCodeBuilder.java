package com.mycompany.who.SuperVisor;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.Base.EditMoudle.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.SuperVisor.CodeMoudle.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import com.mycompany.who.SuperVisor.CodeMoudleBuilder.*;
import com.mycompany.who.SuperVisor.CodeMoudleBuilder.CodeMoudleBuilderInterface.*;
import com.mycompany.who.SuperVisor.CodeShare.*;
import java.io.*;
import java.util.*;

import android.view.View.OnClickListener;
import com.mycompany.who.Edit.*;


public class myCodeBuilder implements Configer<XCode>
{
	
	Title t;
	PageHandler p;
	DownBar d;

	TitleBuilder tb;
	PageHandlerBuilder pb;
	DownBarBuilder db;
	
	Handler handler;
	
	@Override
	public void ConfigSelf(XCode target)
	{
		handler = new CodeHandler();
		
		t = target.getTitle();
	    p = target.getPages();
		d = target.getDownBar();
		
		tb = new myTitleBuilder();
		pb = new myPageHandlerBuilder();
		db= new myDownBarBuilder();
		
		tb.setHandler(handler);
		pb.setHandler(handler);
		db.setHandler(handler);
		
		tb.ConfigSelf(t);
		pb.ConfigSelf(p);
		db.ConfigSelf(d);	
	}
	
	public static void foreachPage(PageList p,APage p2)
	{
		for(int i = p.getChildCount()-1;i>=0;--i){
			p2.page(p.getChildAt(i));
		}
	}
	
	public static interface APage
	{
		public void page(View page)
	}
	
	
	class CodeHandler extends Handler{
		
		public CodeHandler(){
			super();
		}

		@Override
		public void handleMessage(Message msg)
		{
			switch(msg.what){
				
			}
			super.handleMessage(msg);
		}
		
		//dispatch get handle obtain post remove send
		
	}
	
	class myTitleBuilder extends TitleBuilder
	{

		@Override
		public void ConfigSelf(Title target)
		{
			super.ConfigSelf(target);
		}
		
		@Override
		public void loadButton(LinearLayout ButtonBar)
		{
			myButtonClickFactory ButtonBarListener = new myButtonClickFactory();
			ButtonBar.getChildAt(0).setOnClickListener(ButtonBarListener.Uedo());
			ButtonBar.getChildAt(1).setOnClickListener(ButtonBarListener.Redo());
			ButtonBar.getChildAt(2).setOnClickListener(ButtonBarListener.Read());
		}
		
		@Override
		public void onMenuCreat(AdapterView v)
		{
			ArrayAdapter adapter = new ArrayAdapter(v.getContext(),android.R.layout.simple_list_item_1);
			v.setAdapter(adapter);
			adapter.addAll(new String[]{"代码染色","对齐文本","选择语言","保存","设置"});
		}

		@Override
		public void onMenuItemSelected(AdapterView v, int pos)
		{
			Message msg = Message.obtain(getHandler(),pos,v);
			
		}

		@Override
		public void onRepeatSelected(int postion)
		{
			// TODO: Implement this method
		}

		@Override
		public void onItemSelected(AdapterView<?> p1, View p2, int p3, long p4)
		{
			p.tabView(p3);
		}

		@Override
		public void onNothingSelected(AdapterView<?> p1)
		{
			// TODO: Implement this method
		}
		
		class myButtonClickFactory implements ButtonClickFactory
		{
			
			private int mSaveFlags;
			
			public OnClickListener Uedo(){
				return new Uedo();
			}
			public OnClickListener Redo(){
				return new Redo();
			}
			public OnClickListener Read(){
				return new Read();
			}
			public OnClickListener Write(){
				return new Write();
			}

			public class Uedo implements OnClickListener
			{
				@Override
				public void onClick(View p1)
				{
					EditGroup Group = (EditGroup) p.getChildAt(p.getNowIndex());
					Group.getEditManipulator().Uedo();
				}
			}

			public class Redo implements OnClickListener
			{
				@Override
				public void onClick(View p1)
				{
					EditGroup Group = (EditGroup) p.getChildAt(p.getNowIndex());
					Group.getEditManipulator().Redo();
				}
			}

			public class Read implements OnClickListener
			{
				@Override
				public void onClick(View p1)
				{
					p1.setBackgroundResource(R.drawable.Read);
					p1.setOnClickListener(Write());
					mSaveFlags = CodeEdit.mPublicFlags;
					CodeEdit.mPublicFlags = CodeEdit.CanvasMask;
					lock(true);
				}
		    }

			public class Write implements OnClickListener
			{
				@Override
				public void onClick(View p1)
				{
					p1.setBackgroundResource(R.drawable.Write);
				    p1.setOnClickListener(Read());
					CodeEdit.mPublicFlags = mSaveFlags;
					lock(false);
				}
			}
			
			public void lock(final boolean is)
			{
				APage page = new APage(){

					@Override
					public void page(View page)
					{
						if(page instanceof EditGroup){
							EditGroup.EditManipulator man = ((EditGroup)page).getEditManipulator();
							man.lockThem(is);
						}
					}
				};
				foreachPage(p,page);
			}
			
		}
		
	}
	
	class myPageHandlerBuilder extends PageHandlerBuilder
	{
		
		ReSpinner spinner;

		@Override
		public void ConfigSelf(PageHandler target)
		{
			super.ConfigSelf(target);
			spinner = t.getReSpinner();
		}
		
		@Override
		public void onTabPage(int index)
		{
			spinner. setSelection(index);
		}

		@Override
		public void onAddPage(View v, String name)
		{
			name = name.substring(name.lastIndexOf('/')+1,name.length());
			WordAdapter<Icon> adapter = (WordAdapter<Icon>) spinner.getAdapter();
			Icon icon = new Icon3(Share.getFileIcon(name),name);

			if(adapter!=null){
				adapter.add(adapter.getCount(),icon);
				adapter.notifyDataSetChanged();	
			}
			else{
				adapter = WordAdapter.getDefultAdapter();
				adapter.add(adapter.getCount(),icon);
				spinner.setAdapter(adapter);
			}
		}

		@Override
		public void onDelPage(int index)
		{
			WordAdapter adapter = (WordAdapter) spinner.getAdapter();
			if(adapter!=null){
				adapter.remove(index);
				adapter.notifyDataSetChanged();	
			}
		}
		
		class StateBuilder implements PageHandler.ViewBuilder
		{

			boolean lockState;
			
			@Override
			public void eatView(View v, String name, PageHandler self)
			{
				// TODO: Implement this method
			}

			@Override
			public boolean onPageTouch(View p1, MotionEvent p2, PageHandler self)
			{
				// TODO: Implement this method
				return false;
			}

			@Override
			public boolean onPageKey(int keyCode, KeyEvent p2, PageHandler self)
			{
				// TODO: Implement this method
				return false;
			}
		}
		
	}
	
	class myDownBarBuilder extends DownBarBuilder 
	{
	
		ListView fileList;
		ListView searchResult;
		ListView consoleLog;
		myPageFactory mfactory;

		@Override
		public void loadPages(PageList pages)
		{
			mfactory = new myPageFactory();
			fileList = mfactory.getFileList(pages.getContext());
			searchResult = mfactory.getSearchResult(pages.getContext());
			consoleLog = mfactory.getConsoleLog(pages.getContext());
			pages.addView(fileList);
			pages.addView(searchResult);
			pages.addView(consoleLog);
		}
		
		public class myPageFactory implements PageHandlerBuilderInterface.PageFactory
		{
			
			public ListView getFileList(Context cont)
			{
				return new FileListView(cont);
			}
			
			public ListView getSearchResult(Context cont)
			{
				return new SearchResult(cont);
			}
			
			public ListView getConsoleLog(Context cont)
			{
				return new ConsoleLog(cont);
			}
			
			/*
			  在ListView中，拦截点击事件，每次自己被点击，就寻找指定坐标的子View，并记录它，它的position和id
			  
			  如果长按了，则一定也是对记录的Item的长按
			*/
			public abstract class autoChangeList extends ListView implements OnItemClickListener,OnItemLongClickListener
			{
				
				public autoChangeList(Context cont){
					super(cont);
				}

				@Override
				public boolean performItemClick(View view, int position, long id)
				{
					onItemClick(this,view,position,id);
					return super.performItemClick(view, position, id);
				}
				
				@Override
				public boolean performLongClick()
				{
					onItemLongClick(this,getSelectedView(),getSelectedItemPosition(),getSelectedItemId());
					return super.performLongClick();
				}
				
			}
			
			public class FileListView extends autoChangeList implements FileList.FileChangeLisrener
			{

				FileList files = new FileList();
				
				public FileListView(Context cont)
				{
					super(cont);
					setDivider(null);
					files.setFileChangeListener(this);
					files.refreshDate();
				}
				
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					File f = files.getFile(p3-1);
					if(f!=null && f.isFile())
					{
						int id = Share.getFileIcon(f);
						switch(id)
						{
							case R.drawable.file_type_unknown:
								break;
							case R.drawable.file_type_pic:
								p.addView(new ImageView(getContext()),f.getPath());
							default:
							    p.addEdit(f.getPath());
						}
					}
				}

				@Override
				public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					return true;
				}
				
				@Override
				public void Refresh(List<File> files)
				{
					WordAdapter adapter = new WordAdapter();
					adapter.setViewHolderFactory(new WordAdapter.Factory(R.layout.FileIcon));
					List<Icon> list = toList(files);
					list.add(0,new Icon3(R.drawable.folder_open,"..."));
					adapter.addAll(list,0);
					setAdapter(adapter);
				}

				@Override
				public void addAFile(List<File> files, int index)
				{
					WordAdapter adapter = (WordAdapter) getAdapter();
					File f = files.get(index);
					Icon icon = new Icon3(Share.getFileIcon(f),f.getName());
					adapter.add(index,icon);
					adapter.notifyDataSetChanged();
				}

				@Override
				public void delAFile(List<File> files, int index)
				{
					WordAdapter adapter = (WordAdapter) getAdapter();
					adapter.remove(index);
					adapter.notifyDataSetChanged();
				}

				@Override
				public void Rename(List<File> files, int index)
				{
					WordAdapter adapter = (WordAdapter) getAdapter();
					File f = files.get(index);
					Icon3 icon = (Icon3) adapter.getItem(index);
					icon.setName(f.getName());
					adapter.notifyDataSetChanged();
				}
				
				public List<Icon> toList(List<File> files)
				{
					List<Icon> list = new ArrayList<>();
					for(File f:files)
					{
						String name = f.getName();
						Icon icon = new Icon3(Share.getFileIcon(f),name);
						list.add(icon);
					}
					return list;
				}
				
			}
			
			/*
			  每一行都是TextView
			*/
			public class SearchResult extends autoChangeList
			{

				public SearchResult(Context cont){
					super(cont);
					WordAdapter adapter = new WordAdapter();
					adapter.setViewHolderFactory(new WordAdapter.Factory(R.layout.FileIcon));
					Icon icon = new Icon3(R.drawable.All,"");
					adapter.add(0,icon);
					setAdapter(adapter);
				}
				
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					// TODO: Implement this method
				}

				@Override
				public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					// TODO: Implement this method
					return false;
				}
			
			}
			
			/*
			  每一行都是EditText，此行输入后将上行lock
			*/
			public class ConsoleLog extends autoChangeList
			{

				public ConsoleLog(Context cont){
					super(cont);
				}
				
				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					// TODO: Implement this method
				}

				@Override
				public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
				{
					// TODO: Implement this method
					return false;
				}
			}
			
		}
		
	}
	
}
