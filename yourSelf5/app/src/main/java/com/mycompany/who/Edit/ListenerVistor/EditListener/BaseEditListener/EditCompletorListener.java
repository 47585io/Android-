package com.mycompany.who.Edit.ListenerVistor.EditListener.BaseEditListener;
import android.text.*;
import com.mycompany.who.Edit.Base.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import java.util.*;

public interface EditCompletorListener extends EditListener
{
	public List<Icon> LetMeSearch(String text,int index,CharSequence wantBefore,CharSequence wantAfter,int before,int after,Words Wordlib)
	
	public int LetMeInsertWord(Editable editor,int index,size range,CharSequence word)
}