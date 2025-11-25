package JAVA.Multi_Threading.java;
import java.util.*;
import java.util.stream.Collectors;

public class TestingStreams {
    public static void main(String[] args) {
        System.out.println(groupAnagrams(new String[]{"act", "god", "cat", "dog", "tac"}));
    }

    public static List<List<String>> groupAnagrams(String[] str) {
        HashMap<String,List<String>> map = new HashMap<>();

        System.out.println(Arrays.stream(str).collect(Collectors.groupingBy(ele ->
                Arrays.stream(ele.split("")).sorted().collect(Collectors.joining()),Collectors.toList())).
                values());
        return null;
    }

}


