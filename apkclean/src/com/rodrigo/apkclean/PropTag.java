package com.rodrigo.apkclean;

import java.security.InvalidParameterException;


public class PropTag implements Comparable<PropTag>{
	private int index=-1;
	private String name=null;
	private int subindex=-1;
	
	public int getIndex(){
		return index;
	}
	
	public String getName(){
		return name;
	}
	
	public int getSubindex(){
		return subindex;
	}
	
	public static PropTag parsePropTag(String tag){
		int i = F.IsProperty(tag);
		
		if (i>=0){
			PropTag ret = new PropTag();
			ret.index = i;
			ret.name = tag;
			if (!F.Properties[i][1].equals("")){//has a subindex
				ret.subindex = Integer.parseInt(tag.replaceAll(F.Properties[i][1], ""));
			}
			return ret;
		}
		throw new InvalidParameterException(tag);
				
	}

	@Override
	public int compareTo(PropTag tag) {
		
		if (this.index > tag.index)
			return 1;
		
		if (this.index < tag.index)
			return -1;
		Integer s1 = this.subindex;
		Integer s2 = tag.subindex;
		return s2.compareTo(s1);//inverse compare v15 is first then v14
	}
}