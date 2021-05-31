package com.github.michelemilesi.university.music;

import java.io.*;

public class FindFile implements FilenameFilter {

	private String extension;

	public FindFile() {
		this(new String(""));
	}

	public FindFile(String extension) {
		this.extension = extension;
	}

	public boolean accept(File dir,String name) {

		if (name.endsWith(extension))
			return true;
		else 
			return false;
	}

	
}