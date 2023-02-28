package com.mycompany.who.Activity;


import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.SuperVisor.*;
import java.util.*;
import java.util.concurrent.*;
import com.mycompany.who.R;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.DrawerEdit.DrawerBase.*;
import com.mycompany.who.Edit.DrawerEdit.*;
import android.text.*;
import android.graphics.*;
import java.security.cert.*;
import android.widget.*;

public class EditActivity extends BaseActivity3
{

	protected EditList files;
	protected XCode Code;

	@Override
	public void TouchCollector()
	{
		setOnTouchListenrS(Code);
		super.TouchCollector();
	}

	@Override
	protected void initActivity()
	{
		super.initActivity();
		Code = new XCode(this);
		files=Code.getEditList();
		EditFather.addView(Code,0);
	}

	@Override
	public void configActivity()
	{
		super.configActivity();
		Code.setPool(getPool());
		Code.configWallpaper(R.drawable.f3,40);
		//Code.addAExtension(new ForXML());
	}

	protected ThreadPoolExecutor getPool()
	{
		return null;
	}

	
	class ForXML extends XCode.Extension
	{

		public void oninit(EditText self){
			ArrayList<EditListener> FS= ((CodeEdit)self).getFormatorList();
			FS.clear();
			FS.add(new FormatorForXML());
		}
		
		@Override
		public EditListener getFinder()
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public EditListener getDrawer()
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public EditListener getFormator()
		{
			
			return null;
		}

		@Override
		public EditListener getInsertor()
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public EditListener getCompletor()
		{
			// TODO: Implement this method
			return null;
		}

		@Override
		public EditListener getCanvaser()
		{
			// TODO: Implement this method
			return null;
		}
		
		class FormatorForXML extends EditFormatorListener
		{

			@Override
			public int dothing_Run(Editable editor, int nowIndex)
			{
				String src= editor.toString();
				int nextIndex= src.indexOf('\n', nowIndex + 1);
				//从上次的\n接着往后找一个\n

				//如果到了另一个代码块，不直接缩进
				int start_bindow = src.indexOf("<", nowIndex + 1);
				int end_bindow=src.indexOf("/", nowIndex + 1);
				
				if (nowIndex == -1 || nextIndex == -1)
					return -1;

				int nowCount,nextCount;
				nowCount = String_Splitor. calaN(src, nowIndex + 1);
				nextCount = String_Splitor. calaN(src, nextIndex + 1);
				//统计\n之后的分隔符数量

				if (end_bindow < nextIndex && end_bindow != -1)
				{
					//如果当前的nextindex出了代码块，将}设为前面的代码块中与{相同位置
					int index= String_Splitor.getBeforeBindow(src, end_bindow , "<", "/");
					int linestart=DrawerBase. tryLine_Start(src, index);
					int noline= DrawerBase. tryAfterIndex(src, linestart);
					int bindowstart=DrawerBase. tryLine_Start(src, end_bindow);
					int nobindow=DrawerBase. tryAfterIndex(src, bindowstart);
					if (nobindow - bindowstart != noline - linestart)
					{		
						editor.replace(bindowstart, nobindow, String_Splitor.getNStr(" ", noline - linestart));
						return nextIndex + (noline - linestart) - (nobindow - bindowstart);
					}
					return nextIndex;
				}

				String is= src.substring(DrawerBase. tryLine_Start(src, nextIndex + 1), DrawerBase. tryLine_End(src, nextIndex + 1));
				//如果下个的分隔符数量小于当前的，缩进至与当前的相同的位置
				if (nowCount >= nextCount && is.indexOf('<') == -1)
				{
					if (start_bindow < nextIndex && start_bindow != -1)
					{
						//如果它是{之内的，并且缩进位置小于{，则将其缩进至{内
						editor.insert(nextIndex + 1, String_Splitor. getNStr(" ", nowCount - nextCount + 4));
						return nextIndex;
					}
					editor.insert(nextIndex + 1, String_Splitor. getNStr(" ", nowCount - nextCount));
					return nextIndex;
				}

				return nextIndex;
				//下次从这个\n开始
				
			}

			@Override
			public int dothing_Start(Editable editor, int nowIndex,int start,int end)
			{
				String src= editor.toString();
				nowIndex = src.lastIndexOf('\n', nowIndex - 1);
				if (nowIndex == -1)
					nowIndex = src.indexOf('\n');
				return nowIndex;
				//返回now之前的\n
			
			}

			@Override
			public int dothing_End(Editable editor, int beforeIndex,int start,int end)
			{
				// TODO: Implement this method
				return 0;
			}

			
		}
	}
	
	

}
