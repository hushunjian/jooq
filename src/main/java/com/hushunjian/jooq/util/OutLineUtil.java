package com.hushunjian.jooq.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class OutLineUtil {

    private static final List<Character> OPTIONS = Lists.newArrayList('0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z');

    private static final int OPTIONS_SIZE = OPTIONS.size();

    public static final int STEP = 4;

    public static final int DIGIT = 4;

    private static final String POINT = ".";

    private static final String REG_POINT = "\\.";

    /**
     * 下一个path
     *
     * @param path
     * @param step
     * @return
     */
    private static String next(String path, int step) {
        char[] chars = path.toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
            if (step > 0) {
                int index = OPTIONS.indexOf(chars[i]);
                int sum = index + step;
                int nextCharIndex = sum % OPTIONS_SIZE;
                chars[i] = OPTIONS.get(nextCharIndex);
                step = sum / OPTIONS_SIZE;
            } else {
                break;
            }
        }
        // 循环结束了还有进位,超出限制了
        if (step != 0) {
            throw new RuntimeException("path超过限制");
        }
        return String.valueOf(chars);
    }

    public static String next(String parentPath, String prePath) {
        return next(parentPath, prePath, STEP, DIGIT);
    }

    public static String next(String parentPath, String prePath, int digit) {
        return next(parentPath, prePath, STEP, digit);
    }

    public static String getFirst(String parentPath, int digit) {
        String result = StringUtils.leftPad("", digit, "0");
        if (StringUtils.isNotBlank(parentPath)) {
            result = parentPath + POINT + result;
        }
        return result;
    }

    public static String next(String parentPath, String prePath, int step, int digit) {
        if (step <= 0) {
            throw new RuntimeException("错误步进!");
        }
        if (digit <= 0) {
            throw new RuntimeException("错误位数!");
        }
        if (StringUtils.isBlank(parentPath)) {
            parentPath = "";
        } else {
            parentPath = parentPath + POINT;
        }
        if (StringUtils.isBlank(prePath)) {
            prePath = parentPath + StringUtils.leftPad("", digit, "0");
        }
        if (parentPath.length() >= prePath.length()) {
            // 父级长度大于前一个,有问题的
            throw new RuntimeException("参数错误");
        }
        String pre = prePath.substring(parentPath.length());
        if (pre.length() != digit) {
            // 前一个path截取到父级之后与传入的位数不一致,有问题
            throw new RuntimeException("位数不匹配");
        }
        return parentPath + next(pre, step);
    }

    public static String children(String path) {
        return path + "__";
    }

    public static String getLevelPath(String path, int level) {
        String[] split = path.split(REG_POINT);
        if (level < 1 || level > split.length) {
            throw new RuntimeException("错误层级");
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < level - 1; i++) {
            result.append(split[i]).append(POINT);
        }
        return result.append(split[level - 1]).toString();
    }

    public static int level(String path) {
        return StringUtils.countMatches(path, POINT) + 1;
    }

    public static Set<String> getAllParentPath(Collection<String> paths) {
        Set<String> result = Sets.newHashSet();
        paths.forEach(path -> result.addAll(getAllParentPath(path)));
        return result;
    }

    public static List<String> getAllParentPath(String path) {
        return getAllParentPath(path, false);
    }

    public static List<String> allPath(String path) {
        List<String> result = getAllParentPath(path);
        result.add(path);
        return result;
    }

    public static List<String> getAllParentPath(String path, boolean asc) {
        return getAllParentPath(path, false, asc);
    }

    public static List<String> getAllParentPath(String path, boolean self, boolean asc) {
        List<String> result = Lists.newArrayList();
        if (self) {
            result.add(path);
        }
        while (path.lastIndexOf(POINT) > 0) {
            path = path.substring(0, path.lastIndexOf(POINT));
            result.add(path);
        }
        if (asc) {
            Collections.sort(result);
        }
        return result;
    }

    public static List<String> allPath(String path, boolean asc) {
        List<String> result = getAllParentPath(path);
        result.add(path);
        Collections.sort(result);
        if (!asc) {
            Collections.reverse(result);
        }
        return result;
    }

    public static String getParentPath(String path) {
        int lastIndexOf = path.lastIndexOf(POINT);
        if (lastIndexOf < 0) {
            return "";
        }
        return path.substring(0, lastIndexOf);
    }

    public static long calInterval(String start, String end) {
        if (!StringUtils.equals(getParentPath(start), getParentPath(end))) {
            throw new RuntimeException("同层级才有计算间隔的必要!");
        }
        char[] startChars = start.substring(start.lastIndexOf(POINT) + 1).toCharArray();
        char[] endChars = end.substring(end.lastIndexOf(POINT) + 1).toCharArray();
        if (startChars.length != endChars.length) {
            throw new RuntimeException("位数不一致,无法处理!");
        }
        long result = 0L;
        int size = startChars.length - 1;
        for (int i = 0; i <= size; i++) {
            char startChar = startChars[i];
            char endChar = endChars[i];
            // 不一样的才进行判断
            if (startChar != endChar) {
                result += Math.abs(OPTIONS.indexOf(startChar) - OPTIONS.indexOf(endChar)) * Math.pow(OPTIONS_SIZE, size - i);
            }
        }
        return result;
    }

//    public static void main(String[] args) {
//        String path = "001.002.003.004";
//        System.out.println("getLevelPath ===> " + getLevelPath(path, 1));
//
//        String start = "001.002.003.00A";
//        String end = "001.002.003.00B";
//        System.out.println(calInterval(start, end));
//    }
}
