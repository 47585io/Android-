package com.who.Edit.Base.Share.Share2;

import java.util.*;
import android.view.*;
import android.widget.*;
import android.content.*;
import com.who.R;
import com.who.Edit.Base.Share.Share2.*;


/*
 WordAdapter，智障的Adapter

 * 支持任意的xml文件，但需要提供ViewHolderFactory来加载它们

 * 支持区间列表，可以给每一个区间的元素加上一个id

 */
public class WordAdapter<T> extends BaseAdapter
{

	private List<Integer> indexs;
	private List<Integer> flags;
    private List<T> mfile;
	private ViewHolderFactory<T> mfactory;

	public WordAdapter() 
	{
        init();
    }
	public WordAdapter(WordAdapter<T> adapter)
	{
		init();
		copy(adapter);
	}
	protected void init()
	{
		mfile = new ArrayList<>();
		indexs = new ArrayList<>();
		flags = new ArrayList<>();
	}

	public static WordAdapter<Icon> getDefultAdapter()
	{
		WordAdapter<Icon> adapter = new WordAdapter<>();
		adapter.setViewHolderFactory(new Factory());
		return adapter;
	}

	/* 用指定的flag作为一组files的提交信息 */
	public void addAll(Collection<T> files,int flag)
	{
		if(files!=null)
		{
			indexs.add(mfile.size());
			flags.add(flag);
		    mfile.addAll(files);
		}
	}

	/* 在指定位置添加一个file，内部会自行扩展flag以包含这个file */
	public void add(int index,T file)
	{
		mfile.add(index,file);
		offsetIndex(index,1);
	}
	/* 在指定位置删除一个file，内部会自行缩减以填补空缺 */
	public void remove(int index)
	{
		mfile.remove(index);
		offsetIndex(index,-1);
	}

	/* 清空所有 */
	public void clear()
	{
		mfile.clear();
		indexs.clear();
		flags.clear();
	}
	/* 复制另一个adapter的数据 */
	public void copy(WordAdapter<T> adapter)
	{
		mfactory = adapter.mfactory;
		clear();
		mfile.addAll(adapter.mfile);
		indexs.addAll(adapter.indexs);
		flags.addAll(adapter.flags);
	}

    @Override
    public int getCount() 
	{
        return mfile == null ? 0 : mfile.size();
    }
    @Override
    public T getItem(int position)
	{
        return mfile.get(position);
    }
	public void setItem(int position,T file)
	{
		mfile.set(position,file);
	}

	/* 返回设置的值 */
    @Override
    public long getItemId(int position) 
	{
		int i = getPos(position);
		return i==-1 ? i:flags.get(i);
    }

	/* 获取position对应的区间在indexs和flags中的下标 */
	protected int getPos(int position)
	{
		for(int i=indexs.size()-1;i>=0;--i)
		{
			int index = indexs.get(i);
			if(index<=position){
				return i;
			}
		}
		return -1;
	}
	/* 偏移从mfile中的index所处区间之后的indexs，偏移offset */
	protected void offsetIndex(int index,int offset)
	{
		int i = getPos(index);
		for(++i;i<indexs.size();++i){
			index = indexs.get(i);
			indexs.set(i,index+offset);
		}
	}

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolderFactory.ViewHolder<T> holder;
        if(convertView==null)
		{
			//如果要创建一个新的列表项，创建并配置tag，之后在返回后ListView将convertView添加到自己上
			holder = mfactory.newViewHodler();
			convertView = holder.creatView(parent,getItem(position),position);
			convertView.setTag(holder);
		}
	    else{
			//如果要刷新一个已有的列表项，用Adpter中postion的值更新convertView
			holder = (ViewHolderFactory.ViewHolder<T>) convertView.getTag();
		}
		holder.resetView(parent,convertView,getItem(position),position);
		return convertView;
    }

	public void setViewHolderFactory(ViewHolderFactory<T> f)
	{
		mfactory = f;
	}


	/*
	 默认的工厂

	 * 默认使用R.layout.WordIcon

	 * 可以传另一个布局文件id，但它必须有TextView和ImageView且设置了指定id

	 */
	public static class Factory implements ViewHolderFactory<Icon>
	{

		private int rid;

		public Factory(){
			//rid = R.layout.WordIcon;
		}
		public Factory(int rid){
			this.rid = rid;
		}

		@Override
		public ViewHolderFactory.ViewHolder<Icon> newViewHodler()
		{
			return new Holder();
		}

		/* 
		 默认的Holder

		 * 支持让Icon来控制如何加载TextView和ImageView  

		 */
		public class Holder implements ViewHolder<Icon>
		{

			private TextView tvName;
			private ImageView tvIcon;

			@Override
			public View creatView(ViewGroup parent, Icon date, int position)
			{
				LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
				View v = layoutInflater.inflate(rid,null);
				//tvName = v.findViewById(R.id.Filename);
				//tvIcon = v.findViewById(R.id.Fileicon);
				return v;
			}

			@Override
			public void resetView(ViewGroup parent, View root, Icon date, int position)
			{
				date.loadImage(tvIcon);
				date.loadText(tvName);
			}

		}

	}

}
