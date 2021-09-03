/*
 * Copyright 2017 Prodaxis, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.appformer.maven.support;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

public class ParteorLibsScanner {

	private static ParteorLibsScanner _instance;
	private List<URL> _urls;

	public static ParteorLibsScanner getInstance() {
		if (null == _instance)
			_instance = new ParteorLibsScanner();
		return _instance;
	}

	public List<URL> getParteorJarsUrl(){
		if(null == _urls){
			_urls = new ArrayList<URL>();
			 try {
		        	ArrayList<File> parteorFiles = getAllParteorFiles();
		            if(null != parteorFiles){
		            	for (File file : parteorFiles) {
		            		_urls.add( file.toURI().toURL() );
		                }
		            }	
		        }catch (Exception e) {
		            e.printStackTrace();
		        }
		}
		return _urls;
	}
	
	public URL[] getParteorJarsUrlArray(){
		List<URL> urls = getParteorJarsUrl();
		URL[] myArray = new URL[urls.size()];
		return urls.toArray(myArray);
	}

	public static String getInstallLocation() {
		return System.getProperty("jboss.home.dir") + File.separator + "modules" + File.separator + "com"
				+ File.separator + "prodaxis" + File.separator + "bpm" + File.separator + "main";
	}

	public ArrayList<File> getAllParteorFiles() {
		String startingPoint = getInstallLocation();
		String findPattern = "*.jar";
		Path startingDir = Paths.get(startingPoint);
		Finder theFinder = new Finder(findPattern);
		try {
			Files.walkFileTree(startingDir, theFinder);
			return theFinder.myFileArray;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static class Finder extends SimpleFileVisitor<Path> {
		private final PathMatcher theMatcher;
		ArrayList<File> myFileArray = new ArrayList<File>();

		Finder(String pattern) {
			theMatcher = FileSystems.getDefault().getPathMatcher("glob:" + pattern);
		}

		void find(Path file) {
			Path name = file.getFileName();
			if (name != null && theMatcher.matches(name)) {
				myFileArray.add(file.toFile());
			}
		}

		File[] returnFileArray() {
			File[] x = new File[myFileArray.size()];
			return myFileArray.toArray(x);
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			find(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
			find(dir);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) {
			System.err.println(exc);
			return FileVisitResult.CONTINUE;
		}
	}
}
