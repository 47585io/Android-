package com.mycompany.who.Edit.EditBuilder.ListenerVistor.EditListener.BaseEditListener;

import android.text.*;
import com.mycompany.who.Edit.Base.Share.Share1.*;
import com.mycompany.who.Edit.Base.Share.Share2.*;
import com.mycompany.who.Edit.EditBuilder.WordsVistor.*;
import java.util.*;

public abstract interface EditCompletorListener extends EditListener
{
	public abstract List<Icon> LetMeSearch(String text,int index,CharSequence wantBefore,CharSequence wantAfter,int before,int after,Words Wordlib)
	
	public abstract int LetMeInsertWord(Editable editor,int index,size range,CharSequence word)
}
