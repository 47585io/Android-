package com.mycompany.who.Edit;

import android.content.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;

import java.util.*;
import java.util.concurrent.*;
import android.view.*;
import android.text.style.*;
import android.util.*;
import com.mycompany.who.Edit.Share.*;

public abstract class CodeEdit extends CoCoEdit
{
	protected EditDate stack;

	public CodeEdit(Context cont)
	{
		super(cont);
		this.stack = new EditDate();
		addTextChangedListener(new DefaultText());
	}
	public CodeEdit(Context cont, CodeEdit Edit)
	{
		super(cont, Edit);
		this.stack = new EditDate();
		addTextChangedListener(new DefaultText());
	}

	@Override
	protected void onTextChanged(final CharSequence text, final int start, final int lengthBefore, final int lengthAfter)
	{
		Runnable run = new Runnable(){

			@Override
			public void run()
			{
				try
				{
					CodeEdit.super.onTextChanged(text, start, lengthBefore, lengthAfter);
				}
				catch (Exception e)
				{}
			}
				
		};
		post(run);
		
	}

	final public int Uedo_(EditDate.Token token)
	{
		IsModify++;
		isUR = true;
		int endSelection=0;
		if (token != null)
		{
			//范围限制
			if (token.start < 0)
				token.start = 0;
			if (token.end > getText().length())
				token.end = getText().length();
			ongetUR(token);

			if (token.src == "")
			{
				stack.Reput(token.start, token.start, getText().subSequence(token.start, token.end).toString());
				//如果Uedo会将范围内字符串删除，则我要将其保存，待之后插入
				getText().delete(token.start, token.end);	
				endSelection = token.start;
			}
			else if (token.start == token.end)
			{
				//如果Uedo会将在那里插入一个字符串，则我要将其下标保存，待之后删除
				stack.Reput(token.start, token.start + token.src.length(), "");
				getText().insert(token.start, token.src);
				endSelection = token.start + token.src.length();
			}
			else
			{
				stack.Reput(token.start, token.start + token.src.length(), getText().subSequence(token.start, token.end).toString());
				//另外的，则是反向替换某个字符串
			    getText().replace(token.start, token.end, token.src);
				endSelection = token.start + token.src.length();
			}
		}
		isUR = false;
		IsModify--;
		return endSelection;
	}

	final public int Redo_(EditDate.Token token)
	{
		IsModify++;
		isUR = true;
		int endSelection=0;
		if (token != null)
		{
			if (token.start < 0)
				token.start = 0;
			if (token.end > getText().length())
				token.end = getText().length();
			ongetUR(token);

			if (token.src == "")
			{
				stack.put(token.start, token.start , getText().subSequence(token.start, token.end).toString());
				//如果Redo会将范围内字符串删除，则我要将其保存，待之后插入
				getText().delete(token.start, token.end);
				endSelection = token.start;
			}
			else if (token.start == token.end)
			{
				//如果Redo会将在那里插入一个字符串，则我要将其下标保存，待之后删除
				stack.put(token.start, token.start + token.src.length(), "");
				getText().insert(token.start, token.src);
				endSelection = token.start + token.src.length();
			}
			else
			{
				stack.put(token.start, token.start + token.src.length(), getText().subSequence(token.start, token.end).toString());
				//另外的，则是反向替换某个字符串
			    getText().replace(token.start, token.end, token.src);
				endSelection = token.start + token.src.length();
		    }
		}
		isUR = false;
		IsModify--;
		return endSelection;
	}

	final public void Uedo()
	{
		//批量Uedo
		if (stack == null)
			return;

		EditDate.Token token;	
		int endSelection;
		try
		{
			while (true)
			{
				token = stack.getLast();
				endSelection = Uedo_(token);
				setSelection(endSelection);
				//设置光标位置
				EditDate.Token token2=stack.seeLast();
				if (token2 == null)
					return;
				else if (token2.start == token.end)	
					continue;
				//如果token位置紧挨着，持续Uedo	
				else
					break;
			}
		}
		catch (Exception e)
		{}
	}
	final public void Redo()
	{
		//批量Redo
		if (stack == null)
			return;

		EditDate.Token token;
		int endSelection;
		try
		{
			while (true)
			{
				token = stack.getNext();
				endSelection = Redo_(token);
				setSelection(endSelection);
				EditDate.Token token2=stack.seeNext();
				if (token2 == null)
					return;
				else if (token2.start == token.end)	
					continue;
				else
					break;
			}
		}
		catch (Exception e)
		{}
	}

	protected void ongetUR(EditDate.Token token)
	{
	}
	protected void onPutUR(EditDate.Token token)
	{
	}

	public class DefaultText implements TextWatcher
	{

		/**
		 * 输入框改变前的内容
		 *  charSequence 输入前字符串
		 *  start 起始光标，在最前面的位置
		 *  count 删除字符串的数量（这里的count是用str.length()计算的，因为删除一个emoji表情，count打印结果是 2）
		 *  after 输入框中改变后的字符串与起始位置的偏移量（也就是输入字符串的length）
		 */
		/*
		 输入4个字符，删除一个字符，它们的值变化：
		 0, 0, 1  从0开始插入1个字符
		 1, 0, 1  从1开始插入1个字符
		 2, 0, 1  从2开始插入1个字符
		 3, 0, 1  从3开始插入1个字符
		 3, 1, 0  从4开始删除1个字符，达到3 
		 */

		/*
		 这里需要注意的是，任何replace,insert,append,delete函数中都会调ontextChange
		 另外的，replace并不分两次调用ontextChange，而是直接把删除count与插入after一并传过来，所以都得判断
		 因此，start光标总是以在最前面的位置为准
		 */
		@Override
		public void beforeTextChanged(CharSequence str, int start, int count, int after)
		{
			/*
			 if(count!=0 && !isDraw) 
			 { 
			 //在删除\n前，删除行 
			 List<Integer>indexs=String_Splitor.indexsOf('\n', str.toString().substring(start,start + count)); 
			 delLines( (indexs.size())); 
			 }
			 */

			if (isDraw || isUR)
			{
				return;
				//如果它是由于Uedo本身或无需处理的（例如染色）造成的修改，则不能装入
				//另一个情况是，Uedo需要保存格式化时，额外插入的文本
			}

			try
			{
				if (count != 0)
				{
					//如果删除了字符，本次删除了count个字符后达到start，那么上次的字符串就是：
					//从现在start开始，插入start～start+count之间的字符串
					stack.put(start, start, str.toString().substring(start , start + count));
					onPutUR(stack.seeLast());
				}
				if (after != 0)
				{
					//如果还插入了字符，本次即将从start开始插入after个字符，那么上次的字符串就是：
					//删除现在start～start+after之间的字符串
					stack.put(start, start + after, "");
					onPutUR(stack.seeLast());
				}					

			}
			catch (Exception e)
			{}
		}

		/**
		 * 输入框改变后的内容
		 *  charSequence 字符串信息
		 *  start 起始光标
		 *  before 输入框中改变前的字符串与起始位置的偏移量（也就是删除字符串的length）
		 *  count 输入字符串的数量（输入一个emoji表情，count打印结果是2）
		 */
		@Override
		public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
		{
			/*
			 if (!isDraw&&lengthAfter != 0){
			 List<Integer> indexs = null;
			 indexs = String_Splitor.indexsOf('\n', text.toString().substring(start, start + lengthAfter));	
			 addLines(indexs.size());
			 //增加行
			 }*/
		}

		/**
		 *  editable 输入结束呈现在输入框中的信息
		 */
		@Override
		public void afterTextChanged(Editable p1)
		{
			if (IsModify != 0 || IsModify2)
				return;
			openWindow(getWindow(), getSelectionStart(), getPool());
		}

	}

	@Override
	protected void callOnopenWindow(ListView Window)
	{
		if (getWindow().getAdapter()!=null&&getWindow().getAdapter().getCount()>0)
		{
			wordIndex pos = calc(CodeEdit.this);
			getWindow().setX(pos.start);
			getWindow().setY(pos.end);
		}
		else
		{
			//如果删除字符后没有了单词，则移走
			getWindow().setX(-9999);
			getWindow().setY(-9999);
		}
	}
	
	
	
	public TextWatcher getDefultTextListener()
	{
		return new DefaultText();
	}

	abstract public ListView getWindow();
	abstract public wordIndex calc(CodeEdit Edit);
}
