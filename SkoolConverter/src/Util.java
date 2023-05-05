public class Util {

    public static String intToStr(int i, boolean hex, boolean isWord) {
        if (hex) {
            return tiHexString(i, isWord);
        } else {
            return Integer.toString(i);
        }
    }

    public static String hexString(int i, int length) {
        StringBuilder hex = new StringBuilder(Integer.toHexString(i));
        while (hex.length() < length) {
            hex.insert(0, "0");
        }
        return hex.toString();
    }

    public static String tiHexString(int i, boolean word) {
        return ">" + hexString(i, word ? 4 : 2);
    }

    public static String space(int n) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            stringBuilder.append(" ");
        }
        return stringBuilder.toString();
    }

    public static String fit(String s, int n) {
        if (s.length() > n) {
            return s.substring(0, n);
        } else if (s.length() < n) {
            return s + space(n - s.length());
        } else {
            return s;
        }
    }

    public static int parseInt(String s) {
        if (s.contains("$")) {
            try {
                return Integer.parseInt(s.replace("$", ""), 16);
            } catch (Exception e) {
                System.out.println(e);
                throw new RuntimeException("Error parsing string " + e.toString());
            }
        } else {
            return Integer.parseInt(s);
        }
    }
}
