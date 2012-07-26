package com.rodrigo.apkclean;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ResourceGroup extends  TreeMap<String, ArrayList<Resource>> {
	private static final long serialVersionUID = 1L;
	File apk;
	DeviceInfo device;
	
	public ResourceGroup(File apk, DeviceInfo device, String[] folders) {
		this.apk = apk;
		this.device = device;
		
		ZipFile zipFile = null;
		try {
            zipFile = new ZipFile(apk);
		} catch (IOException ex) {
            ex.printStackTrace();
            throw new InvalidParameterException("apk:"+apk);
        }   
            
		Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            
        while (zipEntries.hasMoreElements()) {
        	ZipEntry zip = zipEntries.nextElement();
        	File file = new File(zip.getName());
        	
        	if (file.getParentFile()==null)
        		continue;
        	
        	String f = file.getParentFile().getName();
        	
			boolean isvalid = false;
			
			for (String folder:folders){
				if (f.contains(folder)){
					isvalid = true;
					break;
				}
			}
			
			if (!isvalid)
				continue;
            
			//is a valid folder and file
			String key = genKeyName(file);
			
			if (this.containsKey(key)){
				this.get(key).add(new Resource(file, zip.getCompressedSize()));
			}else{
				ArrayList<Resource> array = new ArrayList<Resource>();
				array.add(new Resource(file, zip.getCompressedSize()));
				this.put(key, array);
			}
        }
        
		
		for (String key:this.keySet()){
			ArrayList<Resource> reslist = this.get(key);
			Collections.sort(reslist);//sort resource group
											
			boolean first = false;
			for(Resource res:reslist){
				if (device.CanUse(res) && !first){
					res.used = true;
					first = true;
				}else{
					res.used = false;
				}
			}
		}
		
	
	}
	
	private static String genKeyName(File path){
		
		String filename=path.getName();
		String folder=path.getParentFile().getName();
		
		String[] all_tags = folder.split("-(?!([A-Z][A-Z]|r[A-Z][A-Z]))");//Split all "-" unless "-rAA" or "-AA"

		String state_tags="|"+all_tags[0];
		
		for (String tag:all_tags){
			if (IsState(tag)>=0)
				state_tags+=("|"+tag);
		}
				
		return filename+state_tags;
	}
	
	private static int IsState(String tag){
		for (int i=0;i<F.States.length;i++){
			if(tag.matches(F.States[i]))
				return i;
		}
		
		return -1;
	}
	
	public long SumRemoveableRes(){
		long totalsize = 0;
		
		for (String key: this.keySet()){
			ArrayList<Resource> res_list = this.get(key);
			for (Resource res: res_list){
				if (!res.used){
					totalsize+=res.size;
				}
			}
			
		}
					
		return totalsize;
	}
	
}
