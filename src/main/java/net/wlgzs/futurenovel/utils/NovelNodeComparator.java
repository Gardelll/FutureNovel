package net.wlgzs.futurenovel.utils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map;
import net.wlgzs.futurenovel.bean.NovelNode;

/**
 * 小说章节小节排序
 * @param <T>
 * @see NovelNodeComparator#compareByTitle(NovelNode, NovelNode)
 */
public class NovelNodeComparator<T extends NovelNode> implements Comparator<T> {

    private static final char[] numChars;

    private static final Map<Character, Character> translationMap = Map.ofEntries(
        Map.entry('〇', '零'),
        Map.entry('壹', '一'),
        Map.entry('贰', '二'),
        Map.entry('叁', '三'),
        Map.entry('肆', '四'),
        Map.entry('伍', '五'),
        Map.entry('陆', '六'),
        Map.entry('柒', '七'),
        Map.entry('捌', '八'),
        Map.entry('玖', '九'),
        Map.entry('拾', '十'),
        Map.entry('佰', '百'),
        Map.entry('仟', '千'),
        Map.entry('萬', '万')
    );

    static {
        numChars = "零一二三四五六七八九十百千万亿".toCharArray();
        Arrays.sort(numChars);
    }

    // TODO 添加手动排序方式 - 根据数据库中 chapters/sections 的 ID 顺序

    /**
     * 根据标题排序
     * <p>
     * 优先级：不含数字的 < 含阿拉伯数字的 < 含中文数字的
     * @param o1 第一个元素
     * @param o2 第二个元素
     * @param <T> 泛型参数
     * @return 若第一个要排在前面，返回小于 0 的数字
     */
    public static <T extends NovelNode> int compareByTitle(T o1, T o2) {
        String str1 = getFirstNumberStr(o1.getTitle());
        String str2 = getFirstNumberStr(o2.getTitle());
        if (str1.isBlank() && str2.isBlank()) return o1.getTitle().compareTo(o2.getTitle());
        if (str1.isBlank()) return -1;
        if (str2.isBlank()) return 1;
        int a = -1, b = -1;
        try { a = Integer.parseUnsignedInt(str1); } catch (NumberFormatException ignored) {}
        try { b = Integer.parseUnsignedInt(str2); } catch (NumberFormatException ignored) {}
        if (a != -1 && b != -1) return a - b;
        if (a != -1) return -1;
        if (b != -1) return 1;
        return (int) (chineseNumber2Int(str1) - chineseNumber2Int(str2));
    }

    @Override
    public int compare(T o1, T o2) {
        return compareByTitle(o1, o2);
    }

    /**
     * 从字符串中提取第一个数字字符串，可以是大写、阿拉伯数字
     * <p>
     * 例如 "你是贰佰伍" -> "二百五"; "第一章" -> "一"
     *
     * @param str 输入字符串
     * @return 输入字符串中的第一个数字子串
     */
    public static String getFirstNumberStr(String str) {
        for (Map.Entry<Character, Character> entry : translationMap.entrySet()) {
            str = str.replace(entry.getKey(), entry.getValue());
        }
        StringBuilder result = new StringBuilder();
        boolean isFirst = true;
        char[] inputChar = str.toCharArray();
        for (char c : inputChar) {
            if (Character.isDigit(c) || Arrays.binarySearch(numChars, 0, numChars.length, c) >= 0) {
                result.append(c);
                if (isFirst) isFirst = false;
            } else {
                if (!isFirst) break;
            }
        }
        return result.toString();
    }

    /**
     * 将中文数字转换为阿拉伯数字值
     * @param chineseNumber 要转换的字符串，必须是中文数字，单位必须正确，
     *                      例如 "三千六百五十” 正确，"二百五" 错误
     * @return 对应的数值
     */
    public static long chineseNumber2Int(String chineseNumber) {
        String numBase = "零一二三四五六七八九";
        String uniteBase = "十百千万亿";
        int[] unitInt = {10, 100, 1000, 10000, 100000000};
        long result = 0;
        char[] arr = chineseNumber.toCharArray();
        LinkedList<Integer> stack = new LinkedList<>();
        for (char s : arr) {
            //跳过零
            if (s == '零') continue;
            //用下标找到对应数字
            int unitPower = uniteBase.indexOf(s);
            //当前字符为数字，直接入栈
            if (unitPower == -1) {
                int num = numBase.indexOf(s);
                if (num == -1) {
                    throw new IllegalArgumentException("'" + chineseNumber + "' 参数非法");
                }
                stack.push(num);
            } else { //当前字符为单位。
                int tempsum = 0;
                int val = unitInt[unitPower];
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
