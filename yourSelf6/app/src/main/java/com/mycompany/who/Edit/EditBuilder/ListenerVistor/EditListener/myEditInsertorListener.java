package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener;

import android.text.*;
import java.util.*;
import android.util.*;
import android.widget.*;
import com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener.*;


/* 
  插入字符
  
  editor表示编辑器的文本容器，nowIndex表示起始位置，count表示用户本次插入的字符数
  
  值得注意，返回的index可以调整编辑器的光标位置
  
*/
public abstract class myEditInsertorListener extends myEditListener implements EditInsertorListener
{
	@Override
	public abstract int onInsert(Editable editor, int nowIndex, int count)
}
