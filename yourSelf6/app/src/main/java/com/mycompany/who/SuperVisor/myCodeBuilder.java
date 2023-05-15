package com.mycompany.who.SuperVisor;
import android.view.*;
import android.widget.*;
import com.mycompany.who.*;
import com.mycompany.who.Edit.Base.EditMoudle.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.SuperVisor.CodeMoudle.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View2.Share.*;
import com.mycompany.who.SuperVisor.CodeMoudleBuilder.*;
import java.util.*;
import com.mycompany.who.SuperVisor.CodeMoudle.Base.View.*;
import android.view.View.*;


public class myCodeBuilder implements Configer<XCode>
{
	
	Title t;
	PageHandler p;
	DownBar d;

	TitleBuilder tb;
	PageHandlerBuilder pb;
	DownBarBuilder db;
	
	@Override
	public void ConfigSelf(XCode target)
	{
		t = target.getTitle();
	    p = target.getPages();
		d = target.getDownBar();
		
		tb = new myTitleBuilder();
		pb = new myPageHandlerBuilder();
		db= new myDownBarBuilder();
		
		tb.ConfigSelf(t);
		pb.ConfigSelf(p);
		db.ConfigSelf(d);
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
			adapter.addAll(new String[]{"染色","对齐","语言","设置"});
		}

		@Override
		public void onMenuItemSelected(AdapterView v, int pos)
		{
			switch(pos){
				case 0:
				case 1:
				case 2:
				case 3:
			}
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
					EditGroup Group = (EditGroup) p.getChildAt(p.getNowIndex());
					Group.getEditManipulator().lockThem(true);
					p1.setOnClickListener(Write());
				}
		    }

			public class Write implements OnClickListener
			{
				@Override
				public void onClick(View p1)
				{
					p1.setBackgroundResource(R.drawable.Write);
					EditGroup Group = (EditGroup) p.getChildAt(p.getNowIndex());
					Group.getEditManipulator().lockThem(false);
					p1.setOnClickListener(Read());
				}
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
			WordAdpter adapter = (WordAdpter) spinner.getAdapter();
			Icon icon = new Icon3(Share.getFileIcon(name),name);

			if(adapter!=null){
				adapter.getList().add(icon);
				adapter.notifyDataSetChanged();	
			}
			else{
				List<Icon> list = new ArrayList<>();
				list.add(icon);
				spinner.setAdapter(new WordAdpter(list,R.layout.FileIcon,0));
			}
		}

		@Override
		public void onDelPage(int index)
		{
			WordAdpter adapter = (WordAdpter) spinner.getAdapter();
			if(adapter!=null){
				adapter.getList().remove(index);
				adapter.notifyDataSetChanged();	
			}
		}
		
		class myPageFactory implements PageFactory{
			
			
		}
		
	}
	
	class myDownBarBuilder extends DownBarBuilder
	{

		@Override
		public void loadPages(PageList pages)
		{
		}
		
	}
	
}
