---
Data structures , Algorithms , Design patterns(HLD,LLD)
---
# Data structures
---
Strings, Arrays, Hashing/HashMap, Linked Nodes, Stack, Queue
---

# Strings Programming questions with similar patterns
---
# 🔹 Category 1: Character Count & Frequency

Concept: Use HashMap, array[26], or Collections.frequency.

# 🔸 Questions
Count the frequency of each character in a string.

Find the first non-repeating character.

Check if two strings are anagrams.

Group anagrams from a list of strings.

Find the character with the highest frequency.

# 🔸 Similar Logic Patterns
HashMap for frequency count

Sorting + comparison

ASCII-based frequency array (int[26])
---
# 🔹 Category 2: Reversal & Rotations
Concept: Reverse with two pointers, use StringBuilder, or loop.

# 🔸 Questions
Reverse a string.

Reverse each word in a sentence.

Rotate string left/right by k characters.

Check if one string is a rotation of another.

Reverse a string without affecting special characters.

🔸 Similar Logic Patterns
Two-pointer technique

Splitting by whitespace

Substring + concatenation tricks

---

# 🔹 Category 3: Palindromes
Concept: Two pointers, expanding around center, dynamic programming.

# 🔸 Questions
Check if a string is a palindrome.

Longest palindromic substring.

Count all palindromic substrings.

Minimum insertions to make a string palindrome.

Valid palindrome with at most one removal.

🔸 Similar Logic Patterns
Expand around center

Reverse + compare

DP for substring tracking
---

# 🔹 Category 4: Substrings & Subsequences
Concept: Sliding window, hashing, brute-force + optimization.

# 🔸 Questions
Longest substring without repeating characters.

All possible substrings of a string.

Count of distinct substrings.

Longest common subsequence.

Longest repeating subsequence.

🔸 Similar Logic Patterns
Sliding window

DP Table (2D)

Set to track uniqueness

---

# 🔹 Category 5: Pattern Matching
Concept: Brute force, KMP, Z-algorithm, regex (basic understanding).

# 🔸 Questions
Implement strStr() (i.e., index of substring).

Pattern match with wildcards *, ?.

Word search in 2D board.

Minimum window substring.

Find all anagram indices in a string.

🔸 Similar Logic Patterns
KMP prefix array

HashMap sliding window

Two pointers window control

---
# 🔹 Category 6: Encoding / Decoding / Compression
Concept: Run Length Encoding, ASCII, count-based encoding.

🔸 Questions
Run-length encoding of a string.

Decode a string like 3[a2[c]].

Count and say problem.

Encode and decode TinyURL.

Custom compression logic (Leetcode-style)

---
# 🔹 Category 7: Formatting & Manipulation
Concept: Word operations, whitespace handling, case change.

# 🔸 Questions
Capitalize first letter of each word.

Remove extra spaces.

Convert string to integer (atoi).

Replace all spaces with %20.

Title case conversion.

🔹 Category 8: Mathematical String Problems
Concept: Parse digits, handle sign, overflow edge cases.

🔸 Questions
Add two numeric strings.

Multiply two numeric strings.

Valid number (with decimal, sign, exponent).

Compare version numbers.

Convert Roman numerals to integer and vice versa.

🔹 Category 9: Miscellaneous / Trick-based
Concept: Bit manipulation, modular arithmetic, creative encoding.

🔸 Questions
Zigzag string conversion (Leetcode).

Remove duplicate letters for smallest lexicographic order.

Excel column title to number and vice versa.

Shortest string that contains all characters of another string.

Check if two strings are isomorphic.

✅ Tips for Practice
Area	Tip
Brute-force	Always write brute-force first, then optimize
Hashing	Think HashMap<Character, Integer> as default
StringBuilder	Prefer over string concatenation in loops
Two pointers	Common in reversal, palindrome, and window logic
Edge cases	Handle "", null, spaces, case sensitivity
