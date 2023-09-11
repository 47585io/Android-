package com.editor.text2.builder.listenerInfo.listener;

import android.text.*;
import android.util.*;
import android.widget.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;


/*
  对齐文本
  
  editor表示编辑器的文本容器，start和end分别表示文本格式化的起始和末尾，nowIndex表示文本格式到了哪儿，beforeIndex表示最后一次之前的位置
  
  三个抽象方法顺次调用，dothing_Start和dothing_End只在起始和末尾调一次，而dothing_Run只要没到end就一直调
  
*/
public abstract class myEditFormatorListener extends myEditListener implements EditFormatorListener
{
	
}
