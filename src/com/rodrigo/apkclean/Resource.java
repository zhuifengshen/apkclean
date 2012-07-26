package com.rodrigo.apkclean;

import java.io.File;
import java.util.ArrayList;


public class Resource  implements Comparable<Resource>{
	ArrayList<PropTag> prop_tags;
	File file;
	boolean used = true;
	long size=0;
	
	public Resource(File file, long size) {
		this.file = file;
		this.size = size;
		String folder=file.getParentFile().getName();
		
		String[] all_tags = folder.split("-(?!r[A-Z][A-Z])");//Split all "-" unless "-rAA"

		prop_tags = new ArrayList<PropTag>();
		
		for (String tag:all_tags){
			if (F.IsProperty(tag)>=0)
				prop_tags.add(PropTag.parsePropTag(tag));
		}

	}
	
	@Override
	public int compareTo(Resource res) {
		int max = this.prop_tags.size() <= res.prop_tags.size() ? this.prop_tags.size(): res.prop_tags.size();
		
		for (int i=0; i<max; i++){
			int compare = this.prop_tags.get(i).compareTo(res.prop_tags.get(i));
			if (compare!=0)
				return compare;
		}
		
		if (this.prop_tags.size() > res.prop_tags.size())
			return -1;
		if (this.prop_tags.size() < res.prop_tags.size())
			return 1;
		
		return 0;
	}
}
