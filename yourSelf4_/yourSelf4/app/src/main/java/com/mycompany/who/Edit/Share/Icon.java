package com.mycompany.who.Edit.Share;

public  class Icon{
	private int resid;
	private String name;
	private byte flag=-128;
	public Icon(){

	}
	public Icon(int resid,String name){
		this.resid=resid;
		this.name=name;
	}
	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return resid;
    }

    public void setIcon(int resid) {
        this.resid = resid;
    }
	
	public int getflag() {
        return flag;
    }

    public void setflag(int flag) {
        this.flag= (byte) flag;
    }


}

