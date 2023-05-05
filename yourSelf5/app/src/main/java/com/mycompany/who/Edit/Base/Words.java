package com.mycompany.who.Edit.Base;

import java.util.*;
import com.mycompany.who.Edit.Base.Share.Share3.*;

public interface Words extends EditMoudle.Creat<Words>
{

	public void clear()

	public int size()

	public char[] getFuhao()

	public char[] getSpilt()

	public Map<CharSequence,CharSequence> get_zhu()

	public Collection<CharSequence> getKeyword()

	public Collection<CharSequence> getConstword()

	public Collection<CharSequence> getLastfunc()

	public Collection<CharSequence> getHistoryVillber()

	public Collection<CharSequence> getThoseObject()

	public Collection<CharSequence> getBeforetype()

	public Collection<CharSequence> getTag()

	public Collection<CharSequence> getAttribute()

	public void setFuhao(char[] fuhao)

	public void setSpilt(char[] spilt)

	public void set_zhu(Map<CharSequence,CharSequence> zhu)

	public void setKeyword(Collection<CharSequence> keyword)

	public void setConstword(Collection<CharSequence> constword)

	public void setLastfunc(Collection<CharSequence> func)

	public void setHistoryVillber(Collection<CharSequence> villber)

	public void setThoseObject(Collection<CharSequence> obj)

	public void setBeforetype(Collection<CharSequence> type)

	public void setTag(Collection<CharSequence> tag)

	public void setAttribute(Collection<CharSequence> attr)

}


