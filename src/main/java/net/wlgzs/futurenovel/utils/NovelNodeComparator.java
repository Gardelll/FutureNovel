package net.wlgzs.futurenovel.utils;

import java.util.Comparator;
import java.util.LinkedList;
import net.wlgzs.futurenovel.bean.NovelNode;

public class NovelNodeComparator<T extends NovelNode> implements Comparator<T> {

    public static <T extends NovelNode> int compareByTitle(T o1, T o2) {
        return new NovelNodeComparator<>().compare(o1, o2);
    }

    @Override
    public int compare(T o1, T o2) {
        String str1 = getNumberStr(o1.getTitle());
        String str2 = getNumberStr(o2.getTitle());
        if (str1.isBlank() || str2.isBlank()) return o1.getTitle().compareTo(o2.getTitle());
        return (int) (chineseNumber2Int(str1) - chineseNumber2Int(str2));
    }

    private String getNumberStr(String str) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean isFirst = true;
        String chineseNumStr = "零一二三四五六七八九十百千万亿";
        for (int i = 0; i < str.length(); i++) {
            String tempStr = str.substring(i, i + 1);
            if (chineseNumStr.contains(tempStr)) {
                stringBuilder.append(tempStr);
                if (isFirst) {
                    isFirst = false;
                }
            } else {
                if (!isFirst) {
                    break;
                }
            }
        }
        return stringBuilder.toString();
    }

    private long chineseNumber2Int(String chineseNumber) {
        String numBase = "零一二三四五六七八九";
        String uniteBase = "十百千万亿";
        int[] uniteInt = {10, 100, 1000, 10000, 100000000};
        long result = 0;
        char[] arr = chineseNumber.toCharArray();
        LinkedList<Integer> stack = new LinkedList<>();
        for (char s : arr) {
            //跳过零
            if (s == '零') continue;
            //用下标找到对应数字
            int index = uniteBase.indexOf(s);
            //当前字符为数字，直接入栈
            if (index == -1) {
                stack.push(numBase.indexOf(s));
            } else { //当前字符为单位。
                int tempsum = 0;
                int val = uniteInt[index];
                if (stack.isEmpty()) {
                    stack.push(val);
                    continue;
                }
                while (!stack.isEmpty() && stack.peek() < val) {
                    tempsum += stack.pop();
                }
                if (tempsum == 0) {
                    stack.push(val);
                } else {
                    stack.push(tempsum * val);
                }
            }
        }
        while (!stack.isEmpty()) {
            result += stack.pop();
        }
        return result;
    }

}
