package com.mycompany.who.Edit.ListenerVistor.EditListener;

import java.util.*;
import android.text.*;
import android.widget.*;
import com.mycompany.who.Edit.Share.*;

public abstract class EditDrawerListener extends EditListener
{
	abstract public void onDraw(int start, int end, List<wordIndex> nodes,SpannableStringBuilder builder,Editable editor);
}
	
