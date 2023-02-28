package com.mycompany.who.SuperVisor;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;

import com.mycompany.who.Edit.*;
import com.mycompany.who.Edit.DrawerEdit.Share.*;

import java.io.*;
import java.util.concurrent.*;
import com.mycompany.who.Edit.Share.*;
import com.mycompany.who.View.Backgroud;
import com.mycompany.who.View.Page;
import com.mycompany.who.Share.myRet;
import com.mycompany.who.R;
import com.mycompany.who.Share.Share;
import java.util.*;
import com.mycompany.who.Edit.DrawerEdit.EditListener.*;
import android.graphics.drawable.Drawable;


public class XCode extends RelativeLayout
{

	@Override
	protected void onConfigurationChanged(Configuration config)
	{
		if (config.orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			onPort();
		}
		else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			onLand();
		}
		super.onConfigurationChanged(config);
	}

	public void onPort(){
		EditFather.getBackground().draw(new Canvas());
	}
	public void onLand(){
		EditFather.getBackground().draw(new Canvas());
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		int AndKey= keyPool.putkey(keyCode);
		if(AndKey!=-1){
			Runnable run= keysRunnar.get(AndKey);
			run.run();
		}
		return super.onKeyUp(keyCode, event);
	}
	

	public int WindowHeight=300;

	protected RelativeLayout EditFather;
	protected ScrollView EditScro;
	protected HorizontalScrollView EdithScro;
	protected LinearLayout ForEdit;
	protected ListView mWindow;

	private EditList EditCollect;
	private ThreadPoolExecutor pool=null;
	private CodeEdit Historyid;
	protected ArrayList<Extension> Extensions;
	protected KeyPool keyPool;
	private HashMap<String,Runnable> keysRunnar;

	public XCode(Context cont)
	{
		super(cont);
		Extensions = new ArrayList<>();
		EditCollect = new EditList();
		keyPool = new KeyPool();
		keysRunnar = new HashMap<>();
		init(cont);
		CodeEdit.Enabled_Format = true;
		CodeEdit.Enabled_Drawer = true;
		CodeEdit.Enabled_Complete = true;
		CodeEdit.Enabled_MakeHTML = true;
		config();
	}
	protected void init(Context cont)
	{
		EditFather = (RelativeLayout) LayoutInflater.from(getContext()).inflate(R.layout.EditGroup, null);
	    EditScro = EditFather.findViewById(R.id.meditScrollView);
		EdithScro = EditFather.findViewById(R.id.meditHorizontalScrollView);
		ForEdit = EditFather.findViewById(R.id.meditLinearLayout);
	    mWindow = EditFather.findViewById(R.id.mWindow);
		addView(EditFather);
	}
	protected void config(){
		mWindow.setOnItemClickListener(new onMyWindowClick());
		mWindow.setOnItemLongClickListener(new onMyWindowLongClick());
	}
	
	public void addView(String name)
	{
		CodeEdit Edit = creatAEdit();
		Page page = new Page(getContext(), Edit, name);
		page.addView(Edit.lines);
		page.addView(Edit);
		EditCollect.addAEdit(page, ForEdit);
		configEdit(Edit, name);
	}
	public void addView(String name, View... others)
	{
		Page page = new Page(getContext(), null, name);
		for (View other:others)
		{
			page.addView(other);
		}
		EditCollect.addAEdit(page, ForEdit);
	}
	public void tabView(int index){
		EditCollect.tabAEdit(index, ForEdit);
	}
	public void removeView(int index){
		EditCollect.delAEdit(index, ForEdit);
	}
	public void delAll(){
		EditCollect.delAll(ForEdit);
	}
	public void refresh(Spinner EditNames){
		EditCollect.refresh(getContext(), EditNames);
		EditNames.setSelection(EditCollect.getNowIndex());
	}

	protected CodeEdit creatAEdit(){
		CodeEdit Edit=new RCodeEdit(getContext());
		return Edit;
	}
	protected void configEdit(CodeEdit Edit, String name)
	{
		Edit.setPool(pool);
		for(Extension e:Extensions){
			e.oninit(Edit);
		    Edit.getFinderList().add(e.getFinder());
			Edit.getDrawerList().add(e.getDrawer());
			Edit.getFormatorList().add(e.getFormator());
			Edit.getInsertorList().add(e.getInsertor());
			Edit.getCompletorList().add(e.getCompletor());
			Edit.getCanvaserList().add(e.getCanvaser());
		}
		Share.setEdit(Edit, name);
		if (new File(name).length() > 0)
		{
			myRet re=new myRet(name);
			Edit.IsModify++;
			Edit.append(re.r("UTF-8"));
			Edit.reSAll(0, Edit.getText().toString().length(), "\t", "    ");	

			Edit.reDraw(0, Edit.getText().toString().length());
			re.close();
			Edit.IsModify--;
		}	
	}

	public void configWallpaper(String picture, int alpha)
	{
		Backgroud d = new Backgroud(picture);
		d.setAlpha(alpha);
		EditFather. setBackgroundDrawable(d);
	}
	public void configWallpaper(int id, int alpha)
	{
		Drawable d= new Backgroud(getContext().getResources(),id);
		d.setAlpha(alpha);
		EditFather. setBackgroundDrawable(d);
	}
	public EditList getEditList(){
		return EditCollect;
	}
	public void setPool(ThreadPoolExecutor pool){
		this.pool = pool;
	}
	protected int MeasureWindowHeight()
	{
		int height=0;
		int i;
		WordAdpter adapter= (WordAdpter) mWindow.getAdapter();
		for (i = 0;i < adapter.getCount();i++)
		{
			View view = adapter.getView(i, null, mWindow);
			view.measure(0, 0);
			height += view.getMeasuredHeight();
			//若View没有明确设定width和height时，它的大小为0
			//可以measure方法测量它的大小，这样测量的大小会被保存，然后获取测量的高
			//注意，getWidth不等于getMeasuredHeight
		}
		
		return height;
	}
	
	class RCodeEdit extends CodeEdit{
		
		RCodeEdit(Context cont){
			super(cont);
		}
		
		@Override
		public ListView getWindow()
		{
			return mWindow;
		}

		@Override
	 	public wordIndex calc(CodeEdit Edit)
		{
			//请求测量
			Historyid = Edit;
			//本次窗口谁请求的，就把单词给谁

			int offset=Edit.getSelectionStart();
			wordIndex pos = Edit.getScrollCursorPos(offset, EdithScro.getScrollX() - Edit.lines.getWidth(), EditScro.getScrollY());
			//start真实位置还少一个lines
			//Window必须在re内
			if (pos.start + mWindow.getWidth() > EditFather.getWidth() || pos.start < 0)
				pos.start = EditFather. getWidth() - mWindow.getWidth();
			//如果x超出屏幕，总是设置在最右侧

			if (pos.end + mWindow.getHeight() +  Edit.getLineHeight() * 2 > EditFather.getHeight())
				pos.end = pos.end - mWindow.getHeight() - Edit.getLineHeight();
			//如果y超出屏幕，将其调整为光标之前，否则调整在光标后
			else
				pos.end = pos.end + Edit.getLineHeight();

			//测量并修改Window大小
			int height = MeasureWindowHeight();
			MarginLayoutParams prams = (ViewGroup.MarginLayoutParams) mWindow.getLayoutParams();
			if (height < WindowHeight)
				prams.height = height;
			else
				prams.height = WindowHeight;

			mWindow.setLayoutParams(prams);

			return pos;
		}
	}
	
	class onMyWindowClick implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			//如果点击了就插入单词并关闭窗口
			WordAdpter adapter = (WordAdpter) p1.getAdapter();
			Icon icon = (Icon) adapter.getItem(p3);
			CodeEdit Edit = Historyid;
			Edit.insertWord(icon.getName(), Edit.getSelectionStart(), icon.getflag());
			mWindow.setX(-9999);
			mWindow.setY(-9999);
		}
	}
	
	class onMyWindowLongClick implements OnItemLongClickListener{
		@Override
		public boolean onItemLongClick(AdapterView<?> p1, View p2, int p3, long p4)
		{
			//如果长按了就去到单词第一次出现的地方
			Icon icon = (Icon) p1.getAdapter().getItem(p3);
			CodeEdit Edit = Historyid;
			int index= Edit.getText().toString().indexOf(icon.getName());
			if (index != -1)
			{
				wordIndex pos = Edit.getCursorPos(index);
				EdithScro.setScrollX(pos.start);
				EditScro.setScrollY(pos.end);   	
				Edit.setSelection(index, index + icon.getName().length());
			}
			return true;
		}
	}
	
	
	public void addAExtension(Extension extension)
	{
		Extensions.add(extension);
	}
	public void delAExtension(int i)
	{
		Extensions.remove(i);
	}
	public void clearExtension(){
		Extensions.clear();
	}
	
	public static abstract class Extension
	{
		public String name;
		public String path;
		public int id;
		public abstract void oninit(EditText self)
		public abstract EditListener getFinder()
		public abstract EditListener getDrawer()
		public abstract EditListener getFormator()
		public abstract EditListener getInsertor()
	  	public abstract EditListener getCompletor()
		public abstract EditListener getCanvaser()
	}

}
