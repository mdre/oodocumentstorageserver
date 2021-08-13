package com.awesomecontrols.oodocumentstorageserver.service;

import com.awesomecontrols.oodocumentstorageserver.exception.FileNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface IFileSytemStorage {
	
	void init();
	String saveFile(MultipartFile file);
	Resource loadFile(String fileName);
        String getFileKey(String fileName) throws FileNotFoundException ;
        String getFilenameFromKey(String key);

}
