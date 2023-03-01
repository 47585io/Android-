package com.mycompany.who.Edit;
import android.content.*;
import android.text.*;
import android.widget.*;

import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import com.mycompany.who.Edit.Share.*;
import android.util.*;
import android.graphics.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.DrawerEdit.Base.*;

public class FormatEdit extends DrawerHTML
{
	public static boolean Enabled_Format=false;
	protected boolean isFormat=false;
	protected EditDate stack;
	
	private ArrayList<EditListener> mlistenerMS;
	private ArrayList<EditListener> mlistenerIS;

	public FormatEdit(Context cont)
	{
		super(cont);
		this.stack = new EditDate();
		mlistenerMS=new ArrayList<>();
		mlistenerIS=new ArrayList<>();
		mlistenerMS.add(new DefaultFormatorListener());
		mlistenerIS.add(new DefaultInsertorListener());
	}
	public FormatEdit(Context cont,FormatEdit Edit)
	{
		super(cont,Edit);
		this.stack = new EditDate();
		mlistenerMS=new ArrayList<>();
		mlistenerMS.addAll(Edit.mlistenerMS);
		mlistenerMS.remove(0);
		mlistenerMS.add(0,new DefaultFormatorListener());
		mlistenerIS=Edit.mlistenerIS;
	}
	public FormatEdit(Context cont,AttributeSet set){
		super(cont,set);
		this.stack = new EditDate();
		mlistenerMS=new ArrayList<>();
		mlistenerIS=new ArrayList<>();
		mlistenerMS.add(new DefaultFormatorListener());
		mlistenerIS.add(new DefaultInsertorListener());
		
	}
	@Override
	public void reSet()
	{
		super.reSet();
		mlistenerMS.add(new DefaultFormatorListener());
		mlistenerIS.add(new DefaultInsertorListener());
	}

	
	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		if (IsModify!=0||IsModify2)
			return;
		//如果正被修改，则不允许修改
		
		int cale=0;
		//format后增加的字数
		if (lengthAfter != 0)
		{      
		   	    IsModify2=true;
				if (Enabled_Format)
				{		
					//是否启用自动format
					Insert(start);
					if (text.toString().indexOf('\n', start) != -1){
						cale= Format(start,start+lengthAfter);	
					}
				}
				IsModify2=false;
		}
		
		//format后才染色
		super.onTextChanged(text, start, lengthBefore,lengthAfter+cale);
		
	}
	
	public ArrayList<EditListener> getFormatorList(){
		return mlistenerMS;
	}
	public ArrayList<EditListener> getInsertorList(){
		return mlistenerIS;
	}
	
	@Override
	public void clearListener()
	{
		getFormatorList().clear();
		getInsertorList().clear();
		super.clearListener();
	}
	

	public int Format(int start, int end)
	{
		IsModify++;
		isFormat = true;
		//为提升效率，将原文本和目标文本装入buffer
		//您可以直接通过测量buffer.getSrc()的下标来修改buffer内部
		String buffer = null;
		if(getPool()==null)
		    buffer= FormatOtherText(start,end,getText().toString());
		else
			buffer=FormatOtherTextPool(start,end,getText().toString(),getPool());
		getText().replace(start,end,buffer);
		//最后，当所有人完成本次对文本的修改后，一次性将修改后的文件替换至Edit
		isFormat = false;
		IsModify--;
		return buffer.length()-(end-start);
		//返回较原文本增加的字符数
	}
	public String FormatOtherText(int start,int end,String src)
	{
		EditFormatorListener.ModifyBuffer buffer=new EditFormatorListener.ModifyBuffer(start,src,src.substring(start,end));
		for (EditListener total:getFormatorList())
		{
			if(total==null)
				continue;
			int beforeIndex = 0;
			int nowIndex=start;
			try
			{
				nowIndex =((EditFormatorListener) total).dothing_Start(buffer, nowIndex,start,end);
	            for (;nowIndex < end && nowIndex != -1;)
				{
					beforeIndex = nowIndex;
			        nowIndex = ((EditFormatorListener) total).dothing_Run(buffer, nowIndex);
		        }
				nowIndex = ((EditFormatorListener) total).dothing_End(buffer, beforeIndex,start,end);			
			}
			catch (Exception e)
			{}
		}
		return buffer.toString();
	}
	public String FormatOtherTextPool(final int start,final int end,String src,ThreadPoolExecutor pool)
	{
		ArrayList<Future<Integer>> results = new ArrayList<>();
		final EditFormatorListener.ModifyBuffer buffer=new EditFormatorListener.ModifyBuffer(start,src,src.substring(start,end));

		for (final EditListener total:getFormatorList())
		{
			if(total==null)
				continue;
			try
			{
				Callable<Integer> ca = new Callable<Integer>(){

					@Override
					public Integer call() throws Exception
					{
						int beforeIndex = 0;
						int nowIndex=start;
						nowIndex =((EditFormatorListener) total).dothing_Start(buffer, nowIndex,start,end);
						for (;nowIndex < end && nowIndex != -1;)
						{
							beforeIndex = nowIndex;
							nowIndex = ((EditFormatorListener) total).dothing_Run(buffer, nowIndex);
						}
						nowIndex = ((EditFormatorListener) total).dothing_End(buffer, beforeIndex,start,end);			
						
						return 0;
					}
				};
				results.add( pool.submit(ca));
			}
			catch (Exception e)
			{}
		}
		FuturePool.FutureGet(results);
		return buffer.toString();
	}
	
	public void Insert(int index)
	{
		IsModify++;
		isFormat = true;
		for (EditListener total:getInsertorList())
		{
			if(total==null)
				continue;
			try
			{
			    int selection=((EditInsertorListener)  total).dothing_insert(getText(), index);
				setSelection(selection);
			}
			catch (Exception e)
			{}
		}
		isFormat = false;
		IsModify--;
	}	
	
	
	

	class DefaultFormatorListener extends EditFormatorListener
	{

		public String START="{";
		public String END="}";
		public String SPILT="\n";
		public String INSERT=" ";
		public int CaCa=4;
		
		public  int dothing_Run(ModifyBuffer editor,int nowIndex)
		{
			String src=editor.getSrc();
			int nextIndex= src.indexOf(SPILT, nowIndex + 1);
			//从上次的\n接着往后找一个\n

			//如果到了另一个代码块，不直接缩进
			int start_bindow = src.indexOf(START, nowIndex + 1);
			int end_bindow=src.indexOf(END, nowIndex + 1);
			
			if (nowIndex == -1 || nextIndex == -1)
				return -1;

			int nowCount,nextCount;
			nowCount = String_Splitor. calaN(src, nowIndex + 1);
			nextCount = String_Splitor. calaN(src, nextIndex + 1);
			//统计\n之后的分隔符数量
			
			String is= src.substring(tryLine_Start(src, nextIndex + 1), tryLine_End(src, nextIndex + 1));
			//如果下个的分隔符数量小于当前的，缩进至与当前的相同的位置
			if (nowCount >= nextCount && is.indexOf(START) == -1)
			{
				if (end_bindow < nextIndex && end_bindow != -1)
				{
					//如果当前的nextindex出了代码块，将}设为前面的代码块中与{相同位置
					int index= String_Splitor.getBeforeBindow(src, end_bindow , START, END);
					if(index==-1)
						return nextIndex;
					int linestart=tryLine_Start(src, index);
					int noline= tryAfterIndex(src, linestart);
					int bindowstart=tryLine_Start(src, end_bindow);
					int nobindow=tryAfterIndex(src, bindowstart);
					if (nobindow - bindowstart != noline - linestart)
					{		
						editor.replace(bindowstart, nobindow, String_Splitor.getNStr(INSERT, noline - linestart));
						return nextIndex + (noline - linestart) - (nobindow - bindowstart);
					}
					editor.insert(nextIndex + 1, String_Splitor. getNStr(INSERT, noline - linestart-CaCa));
					return nextIndex;
				}
				if (start_bindow < nextIndex && start_bindow != -1)
				{
					//如果它是{之内的，并且缩进位置小于{，则将其缩进至{内
					editor.insert(nextIndex + 1, String_Splitor. getNStr(INSERT, nowCount - nextCount + CaCa));
					return nextIndex;
				}
	
				editor.insert(nextIndex + 1, String_Splitor. getNStr(INSERT, nowCount - nextCount));
				return nextIndex;
			}

			return nextIndex;
			//下次从这个\n开始

		}

		@Override
		public int dothing_Start(ModifyBuffer editor, int nowIndex,int start,int end)
		{
			editor. reSAll("\t","    ");
			String src= editor.getSrc();
			nowIndex = src.lastIndexOf(SPILT, nowIndex - 1);
			if (nowIndex == -1)
				nowIndex = src.indexOf(SPILT);
			return nowIndex;
			//返回now之前的\n
		}

		@Override
		public int dothing_End(ModifyBuffer editor, int beforeIndex,int start,int end)
		{
			
			return -1;
		}

	}
	
	class DefaultInsertorListener extends EditInsertorListener
	{

		@Override
		public void putWords(HashMap<String, String> words)
		{
			// TODO: Implement this method
		}
		
		public char insertarr[];
		public DefaultInsertorListener()
		{
			insertarr = new char[]{'{','(','[','\'','"','/'};
			Arrays.sort(insertarr);
		}
		public int dothing_insert(Editable editor, int nowIndex)
				{
					String src=editor.toString();
					int charIndex=Array_Splitor.indexOf(src.charAt(nowIndex), insertarr);
					if (charIndex != -1)
					{
						switch (src.charAt(nowIndex))
						{
							case '{':
								editor.insert(nowIndex + 1, "}");
								break;
							case '(':
								editor.insert(nowIndex + 1, ")");
								break;
							case '[':
								editor.insert(nowIndex + 1, "]");
								break;
							case '\'':
								editor.insert(nowIndex + 1, "'");
								break;
							case '"':
								editor.insert(nowIndex + 1, "\"");
								break;
							case '/':
								if (src.charAt(nowIndex - 1) == '<')
								{
									int index= String_Splitor.getBeforeBindow(src, nowIndex-1, "<", "</");
									wordIndex j= tryWordAfter(src, index);
									editor.insert(nowIndex + 1, src.substring(j.start, j.end) + ">");
									return j.end + 1;
								}
						}
					}
					return nowIndex + 1;
				}
	}

	
	public void reSAll(int start, int end, String want, String to)
	{
		IsModify++;
		isFormat = true;
		Editable editor = getText();
		String src=getText().toString().substring(start, end);
		int nowIndex = src.lastIndexOf(want);
		while (nowIndex != -1)
		{
			//从起始位置开始，反向把字符串中的want替换为to
			editor.replace(nowIndex + start, nowIndex + start + want.length(), to);	
			nowIndex = src.lastIndexOf(want, nowIndex - 1);
		}
		isFormat = false;
		IsModify--;
	}
}
