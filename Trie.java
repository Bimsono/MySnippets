import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;


public class Trie {

	/**
	 * The root of the trie, the state before any characters are processed
	 */
	private Node root;

	/**
	 * The current state of this trie
	 */
	private Node state;

	/**
	 * Constructor, builds a trie using the given dictionary (using buildTrie and buildFail), then
	 * sets the current state of the trie to be the root
	 * 
	 * @param dictionary , a dictionary of words
	 */
	public Trie(Set<String> dictionary) {
		root = new Node();
		buildTrie(dictionary);
		buildFail();
		reset();
	}

	/**
	 * Resets the state of this trie to be the root
	 */
	public void reset() {
		state = root;
	}

	/**
	 * Advances the state along the path c [Character], moving to the failure node WHILE necessary, and returns
	 * the set of Strings that should be output (potentially an empty set)
	 * 
	 * @param c , the character to move along
	 * @return what should be output at the new state
	 */
	public Set<String> next(char c) {
		Node currentState = getState();
		Node newState;
		
		if(currentState.hasChild(c)){
			setState(currentState.getChild(c));
			newState = getState();
			return newState.getOut();
		}else{
			//while current state doesn't have child by c and we haven't failed to the root
			while(!currentState.hasChild(c) && !currentState.equals(getRoot())){
				setState(currentState.getFail());//keep failing
				currentState = getState();
			}
			//Now, we're either at root of a state w/ child by c
			
			if(!currentState.equals(getRoot())){//if current state isn't root then we have a child by c
				setState(currentState.getChild(c));
				newState = getState();
				return newState.getOut();
			}
			//we're at root. Check if it has a child by c. If so, go to it.
			else if(currentState.equals(getRoot()) && currentState.hasChild(c)){
				setState(currentState.getChild(c));
				newState = getState();
				return newState.getOut();
			}else{//If not, return the empty set
				return currentState.getOut();
			}
		}
	}

	/**
	 * Sets all the children of the trie based on the given dictionary
	 * 
	 * @param dictionary , a dictionary of words
	 */
	public final void buildTrie(Set<String> dictionary) {
		for(String str: dictionary){
			reset();//start at root
			
			//put all string's characters in the trie and add the String as the output to last character
			for(char c: str.toCharArray()){
				if(getState().hasChild(c)){
					setState(getState().getChild(c));//move to that character node
				}else{//character node not in trie so make it and move to it
					getState().addChild(c);
					setState(getState().getChild(c));
				}
			}
			getState().addOut(str);//adding the String output to the last char
		}
	}

	/**
	 * Sets all the failures in the trie, should assume that buildTrie has already been called
	 */
	public final void buildFail() {
		Node r = getRoot();
		Queue<Node> q = new LinkedList<Node>();
		r.setFail(r);
		
		for(Node child: r.getAllChildren()){
			child.setFail(r);
			q.add(child);
		}
		while(!q.isEmpty()){
			//remove node (N) from (Q)
			Node n = q.remove();
			
			Collection<Character> childrenCharsOfN = n.getAllKeys();
			Collection<Node> childrenNodesOfN = n.getAllChildren();
			
			//will give (ch) used to get from (N) to (C) 
			ArrayList<Character> characters = new ArrayList<Character>(childrenCharsOfN);
			for(Node childNode: childrenNodesOfN){
				int index = 0;
				char ch = characters.remove(index);
				q.add(childNode);
				childNode.setFail(n.getFail());
				
				while(!childNode.getFail().hasChild(ch) && !childNode.getFail().equals(getRoot())){
					childNode.setFail(childNode.getFail().getFail());//set fail(C)=fail(fail(C))
				}
				if(childNode.getFail().hasChild(ch)){
					childNode.setFail(childNode.getFail().getChild(ch));
				}
				for(String str: childNode.getFail().getOut()){
					childNode.addOut(str);
				}
				index++;
			}
		}
	}
	
	/**
	 * Simple getters and setters used for grading
	 */
	public Node getRoot() {
		return root;
	}
	
	/**
	 * Simple getters and setters used for grading
	 */
	public void setRoot(Node root) {
		this.root = root;
	}
	
	/**
	 * Simple getters and setters used for grading
	 */
	public Node getState() {
		return state;
	}
	
	/**
	 * Simple getters and setters used for grading
	 */
	public void setState(Node state) {
		this.state = state;
	}
}
