package com.editor.text2.builder.listenerInfo.listener;

import android.text.*;
import android.util.*;
import java.util.*;
import com.editor.text2.builder.listenerInfo.listener.baselistener.*;


/*
  找单词，转化为Icon，插入单词
  
  Wordlib表示编辑器内部的单词库，在Finder查找nodes时自动存储一些了，您可以返回其中的一个库，也可以返回外部的任意的单词库
  
  在之后，我们用SearchOnce找到当前光标位置对应的单词，然后以words发送给您，您只要把对应的Icon添加到adapter中，可以用addSomeWord
  
  最后，我们会将单词放到ListView的adapter中，并为这组单词添加一个id(这个id就是当前listener的hashCode)，然后展示ListView
  
  当用户点击一个列表项，会寻找这个单词所在范围，拿到这组单词的id，遍历所有listener，找到指定hashCode的listener，并回调它的LetMeInsertWord方法
  
*/
public abstract class myEditCompletorListener extends myEditListener implements EditCompletorListener
{
	
}
