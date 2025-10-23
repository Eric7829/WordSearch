import java.util.*;

/*
 * AhoCorasickAutomaton
 * Author:
 * Version information: 1.0
 * Date: October 23, 2025

 ========
 * Input: A list of words (List<String>) to be searched for in a target text or grid lines.
 *        Words are treated case-insensitively and both forward and reversed variants are
 *        inserted into the automaton to enable bidirectional detection in a single forward scan.
 * Output: Provides a list of Match objects for each text scan. Each Match contains a pattern ID
 *         and the end position index in the scanned 1D string. The caller maps pattern IDs back
 *         to word strings and translates 1D indices into 2D grid coordinates where required.
 * Process: Builds a Trie of all patterns, computes BFS failure links (explained in documentation),
 *         and performs linear-time scanning of text by walking transitions and following failure links
 *         when mismatches occur. Matches are collected from node output lists (including inherited outputs).
 ======
 */
public class AhoCorasickAutomaton {
    
    /**
     * TrieNode represents a single state in the Aho-Corasick automaton.
     * Each node maintains:
     * - Children transitions (26 lowercase letters)
     * - Failure link pointer for mismatch handling
     * - Output list of pattern IDs ending at this node
     */
    private static class TrieNode {
        TrieNode[] children;
        TrieNode failureLink;
        List<Integer> output;
        
        TrieNode() {
            children = new TrieNode[26]; // a-z
            output = new ArrayList<>(2); // Most nodes have 0-2 patterns
            failureLink = null;
        }
    }
    
    private final TrieNode root;
    private final List<String> patterns;
    private final Map<Integer, Boolean> isReversed;
    
    /**
     * Constructs an Aho-Corasick automaton for the given word list.
     * Automatically adds both forward and reversed versions of each word.
     * 
     * @param words List of words to search for (will be converted to uppercase)
     */
    public AhoCorasickAutomaton(List<String> words) {
        this.root = new TrieNode();
        this.patterns = new ArrayList<>(words.size() * 2); // Pre-allocate for forward + reversed
        this.isReversed = new HashMap<>(words.size() * 2); // Pre-allocate for forward + reversed
        
        // Add both forward and reversed patterns
        for (String word : words) {
            String upperWord = word.toUpperCase();
            addPattern(upperWord, false);
            addPattern(new StringBuilder(upperWord).reverse().toString(), true);
        }
        
        // Build failure links after all patterns are inserted
        buildFailureLinks();
    }
    
    /**
     * Inserts a pattern into the Trie structure.
     * 
     * @param pattern The pattern to insert (must be uppercase)
     * @param reversed Whether this pattern is a reversed version
     */
    private void addPattern(String pattern, boolean reversed) {
        int patternId = patterns.size();
        patterns.add(pattern);
        isReversed.put(patternId, reversed);
        
        TrieNode current = root;
        for (char ch : pattern.toCharArray()) {
            int index = ch - 'A';
            if (index < 0 || index >= 26) continue; // Skip non-alphabetic
            
            if (current.children[index] == null) {
                current.children[index] = new TrieNode();
            }
            current = current.children[index];
        }
        
        // Mark this node as an end of pattern
        current.output.add(patternId);
    }
    
    /**
     * Builds failure links using Breadth-First Search (BFS).
     * 
     * FAILURE LINK LOGIC EXPLANATION:
     * ================================
     * The failure link of a node points to the longest proper suffix of the string
     * represented by that node which is also a prefix of some pattern in the Trie.
     * 
     * Algorithm (BFS-based):
     * 1. Initialize: Root's failure link points to itself. All depth-1 nodes
     *    (direct children of root) have failure links pointing to root.
     * 
     * 2. For each node at depth > 1 (processed in BFS order):
     *    a) Let 'current' be the node we're processing
     *    b) Let 'parent' be current's parent and 'ch' be the edge label from parent to current
     *    c) Start at parent's failure link: state = parent.failureLink
     *    d) Walk down failure links until we find a state that has a transition on 'ch',
     *       or until we reach the root
     *    e) If we found a state with transition on 'ch': current.failureLink = state.children[ch]
     *       Otherwise: current.failureLink = root
     * 
     * 3. Output Inheritance: Each node inherits outputs from its failure link.
     *    This ensures we detect all overlapping pattern matches.
     * 
     * Time Complexity: O(n * alphabet_size) where n is total pattern length
     * Space Complexity: O(n) for the Trie structure
     */
    private void buildFailureLinks() {
        Queue<TrieNode> queue = new LinkedList<>();
        
        // Step 1: Initialize depth-1 nodes (root's children)
        root.failureLink = root;
        for (int i = 0; i < 26; i++) {
            if (root.children[i] != null) {
                root.children[i].failureLink = root;
                queue.offer(root.children[i]);
            }
        }
        
        // Step 2: BFS to build failure links for deeper nodes
        while (!queue.isEmpty()) {
            TrieNode current = queue.poll();
            
            for (int i = 0; i < 26; i++) {
                if (current.children[i] != null) {
                    TrieNode child = current.children[i];
                    queue.offer(child);
                    
                    // Find failure link by following parent's failure link
                    TrieNode failureState = current.failureLink;
                    
                    // Walk down failure links until we find a valid transition or reach root
                    while (failureState != root && failureState.children[i] == null) {
                        failureState = failureState.failureLink;
                    }
                    
                    // Set the failure link
                    if (failureState.children[i] != null && failureState.children[i] != child) {
                        child.failureLink = failureState.children[i];
                    } else {
                        child.failureLink = root;
                    }
                    
                    // Step 3: Inherit outputs from failure link (detect overlapping patterns)
                    child.output.addAll(child.failureLink.output);
                }
            }
        }
    }
    
    /**
     * Searches for all patterns in the given text using the automaton.
     * 
     * @param text The text to search through (will be converted to uppercase)
     * @return List of Match objects containing pattern ID and end position
     */
    public List<Match> search(String text) {
        List<Match> matches = new ArrayList<>(text.length() / 4); // Estimate: ~25% match rate
        String upperText = text.toUpperCase();
        TrieNode current = root;
        
        for (int i = 0; i < upperText.length(); i++) {
            char ch = upperText.charAt(i);
            int index = ch - 'A';
            
            // Skip non-alphabetic characters
            if (index < 0 || index >= 26) {
                current = root;
                continue;
            }
            
            // Follow failure links until we find a valid transition or reach root
            while (current != root && current.children[index] == null) {
                current = current.failureLink;
            }
            
            // Make the transition
            if (current.children[index] != null) {
                current = current.children[index];
            } else {
                current = root;
            }
            
            // Report all patterns ending at this position
            for (int patternId : current.output) {
                matches.add(new Match(patternId, i));
            }
        }
        
        return matches;
    }
    
    /**
     * Gets the pattern string by its ID.
     * 
     * @param patternId The pattern ID
     * @return The pattern string
     */
    public String getPattern(int patternId) {
        return patterns.get(patternId);
    }
    
    /**
     * Checks if a pattern ID represents a reversed word.
     * 
     * @param patternId The pattern ID
     * @return true if the pattern is reversed, false otherwise
     */
    public boolean isReversedPattern(int patternId) {
        return isReversed.get(patternId);
    }
    
    /**
     * Match represents a single pattern match in the text.
     * Contains the pattern ID and the ending position in the text.
     */
    public static class Match {
        public final int patternId;
        public final int endPos;
        
        public Match(int patternId, int endPos) {
            this.patternId = patternId;
            this.endPos = endPos;
        }
    }
}
