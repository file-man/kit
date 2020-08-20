package com.kit.utils;

public class NumberUtils {

    public static void main(String[] args) {
        for (String item : NumberUtils.split(7, new Integer[] {1, 2, 3, 4, 5}, 2).split(",")) {
            System.out.println(item);
        }
    }

    public static final String split(int number, Integer[] candidates, int repetition) {
        return split(number, candidates, 0, repetition);
    }

    private static final String split(int number, Integer[] candidates, int index, int repetition) {
        if (index >= candidates.length) {
            return "";
        }

        final int times = number / candidates[index];

        if (times > 0) {
            String value = "";

            for (int i = 0; i <= Math.min(repetition, times); i++) {
                final int others = number - (candidates[index] * i);
                String head = "";
                for (int n = 0; n < i; n++) {
                    head = concat(head, "" + candidates[index], "-");
                }

                String tails = "";
                if (others > 0) {
                    tails = split(others, candidates, index + 1, repetition);
                    if ("".equals(tails)) {
                        continue;
                    }
                }

                String items = "";
                if (!"".equals(tails)) {
                    for (String tail : tails.split(",")) {
                        items = concat(items, concat(head, tail, "-"), ",");
                    }
                } else {
                    items = head;
                }

                if (!"".equals(items)) {
                    value = concat(value, items, ",");
                }
            }

            return value;
        } else {
            return split(number, candidates, index + 1, repetition);
        }
    }

    private static final String concat(String a, String b, String divider) {
        if ("".equals(a)) {
            return b;
        } else if ("".equals(b)) {
            return a;
        } else {
            return a + divider + b;
        }
    }
}
