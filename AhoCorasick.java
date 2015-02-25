import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.swing.text.html.HTMLDocument.Iterator;

public class AhoCorasick {

	/**
	 * Gets the location of all appearances of each word in dictionary in the given text (locations
	 * should be 0 indexed an example: in the text "Apple Cider" the word "Apple" occurs at index 0)
	 * 
	 * @param text , the text to search
	 * @param dictionary , words to search for
	 * @return a map of words to a set of locations
	 */
	public static Map<String, Set<Integer>> matches(String text, Set<String> dictionary) {
		Trie trie = new Trie(dictionary);
		trie.reset();
		Map<String,Set<Integer>> map = new HashMap<String,Set<Integer>>();
		int index = 0;
		for(char c: text.toCharArray()){//for e/c letter in text
			Set<String> trieNext = trie.next(c);
			
			if( !trieNext.isEmpty()){
				for(String str: trieNext){
					if(!map.containsKey(str)){
						Set<Integer> set = new HashSet<Integer>();
						int location = index - str.length() + 1;
						set.add(location);
						map.put(str,set);
					}else{
						int location = index - str.length() + 1;
						map.get(str).add(location);
					}
				}
			}
			index++;
		}
		return map;
	}
	
	public static void main(String[] args) throws FileNotFoundException{
		Scanner scan = new Scanner(new File("raven.txt"));
		scan.nextLine();//takes out (125)
		
		String text = "";
		for(int i = 1; i <= 125; i++){//125 lines of text
			text += scan.nextLine() + "\n";
		}
		
		scan.nextLine();//takes out (35)
		Set<String> dictionary = new HashSet<String>();
		for(int i = 1; i <= 35; i++){
			dictionary.add(scan.nextLine());
		}
		
		Map<String, Set<Integer>> results = matches(text,dictionary);
		
		Set<String> words = results.keySet();
		for(String str: words){
			Set<Integer> locations = results.get(str);
			System.out.print(str + " " + locations.size() + " ");
			for(Integer location: locations){
				System.out.print(location + " ");
			}
			System.out.println();
		}
		
		
	}

}
