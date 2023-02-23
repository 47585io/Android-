package com.mycompany.who.Edit;

import android.content.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;

import java.util.*;
import java.util.concurrent.*;
import android.view.*;

public abstract class CodeEdit extends CoCoEdit
{
	
	public CodeEdit(Context cont){
		super(cont);
		addTextChangedListener(new DefaultText());
	}

	@Override
	protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter)
	{
		try{
		super.onTextChanged(text, start, lengthBefore, lengthAfter);
		}catch(Exception e){}
	}
	
	
	class DefaultText implements TextWatcher
	{
		@Override
		public void afterTextChanged(Editable p1)
		{
			// TODO: Implement this method
		}

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
			//也可以用findViewById(index)
			if (count != 0 && !isDraw)
			{
				//在删除\n前，删除行
				ArrayList<Integer> indexs=String_Splitor.indexsOf("\n", str.toString().substring(start, start + count));
				for (int strindex:indexs)
				{
					delALine();
				}
			}

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
				}
				if (after != 0)
				{
					//如果还插入了字符，本次即将从start开始插入after个字符，那么上次的字符串就是：
					//删除现在start～start+after之间的字符串
					stack.put(start, start + after, "");
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
		public void onTextChanged(CharSequence str, int start, int count, int after)
		{
			if (IsModify!=0||IsModify2)
				return;
			openWindow(getWindow(), getSelectionStart(),getPool());

			if (getWindow().getAdapter().getCount() > 0)
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

		/**
		 *  editable 输入结束呈现在输入框中的信息
		 */
	}

	abstract public ListView getWindow();
	abstract public wordIndex calc(CodeEdit Edit);
}
