package com.mycompany.who.Edit.Base.Share.Share2;

import android.view.*;
import android.widget.*;
import java.util.*;
import com.mycompany.who.R;
import com.mycompany.who.Edit.Base.Share.Share1.*;


/*
  WordAdpter，智障的Adapter
  
  * 支持任意的xml文件，只要它拥有TextView和ImageView并设置了id
  
  * 支持让Icon来控制如何加载TextView和ImageView
 
  * 支持区间列表，可以给每一个区间的元素加上一个id
  
*/
public class WordAdpter extends BaseAdapter
{

	private List<size> indexs;
    private List<Icon> mfile;
	private int rid;

	public WordAdpter(int id) 
	{
        mfile = new ArrayList<>();
		indexs = new ArrayList<>();
		rid=id;	
    }
	public WordAdpter(List<Icon> file,int id,int flag)
	{
		mfile = new ArrayList<>();
		indexs = new ArrayList<>();
		rid=id;	
		addAll(file,flag);
	}
	
	public static WordAdpter getDefultAdapter()
	{
		return new WordAdpter(R.layout.WordIcon);
	}
	
	public void addAll(Collection<Icon> file,int flag)
	{
		if(file!=null)
		{
			size index = new size(mfile.size(),flag);
			indexs.add(index);
		    mfile.addAll(file);
		}
	}

	public List<Icon> getList(){
		return mfile;
	}
	
    @Override
    public int getCount() 
	{
        return mfile == null ? 0 : mfile.size();
    }

    @Override
    public Icon getItem(int position)
	{
        return mfile.get(position);
    }

    @Override
    public long getItemId(int position) 
	{
        for(int i=indexs.size()-1;i>=0;--i)
		{
			size index = indexs.get(i);
			if(index.start<=position){
				return index.end;
			}
		}
		return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
	{
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ViewHolder viewHolder = null;
		
        if (convertView == null) {
			//如果要创建一个新的列表项，创建并配置tag，之后在返回后ListView将convertView添加到自己上
            convertView = layoutInflater.inflate(rid, null);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }else {
			//如果要刷新一个已有的列表项，用Adpter中postion的值更新设置的列表项tag，但其实tag为ViewHolder，其中记录了这个列表项的子元素
            viewHolder = (ViewHolder) convertView.getTag();
        }

		Icon icon = mfile.get(position);	
        icon.loadImage(viewHolder.tvIcon);
		icon.loadText(viewHolder.tvName);
        return convertView;
    }

	
    static class ViewHolder 
	{
        protected TextView tvName;
        protected ImageView tvIcon;

        ViewHolder(View rootView) {
            initView(rootView);
        }

        private void initView(View rootView) {
            tvName = (TextView) rootView.findViewById(R.id.Filename);
            tvIcon = (ImageView) rootView.findViewById(R.id.Fileicon);
        }
    }
	
}

