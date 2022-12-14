package sy.util;

/**
 * @author YanShi
 * @date 2022/12/14 20:01
 */
public class NormalizedLevenshtein {
    public final double normalizedLevenshteinDistance(String s1, String s2) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        } else if (s1.equals(s2)) {
            return 0.0D;
        } else {
            int m_len = Math.max(s1.length(), s2.length());
            return m_len == 0 ? 0.0D : this.distance(s1, s2) / (double)m_len;
        }
    }

    public final double normalizedLevenshteinSimilarity(String s1, String s2) {
        return 1.0D - this.normalizedLevenshteinDistance(s1, s2);
    }

    public final double distance(String s1, String s2) {
        return this.distance(s1, s2, 2147483647);
    }

    public final double distance(String s1, String s2, int limit) {
        if (s1 == null) {
            throw new NullPointerException("s1 must not be null");
        } else if (s2 == null) {
            throw new NullPointerException("s2 must not be null");
        } else if (s1.equals(s2)) {
            return 0.0D;
        } else if (s1.length() == 0) {
            return s2.length();
        } else if (s2.length() == 0) {
            return s1.length();
        } else {
            int[] v0 = new int[s2.length() + 1];
            int[] v1 = new int[s2.length() + 1];

            int i;
            for(i = 0; i < v0.length; v0[i] = i++) {
            }

            for(i = 0; i < s1.length(); ++i) {
                v1[0] = i + 1;
                int minv1 = v1[0];

                for(int j = 0; j < s2.length(); ++j) {
                    int cost = 1;
                    if (s1.charAt(i) == s2.charAt(j)) {
                        cost = 0;
                    }

                    v1[j + 1] = Math.min(v1[j] + 1, Math.min(v0[j + 1] + 1, v0[j] + cost));
                    minv1 = Math.min(minv1, v1[j + 1]);
                }

                if (minv1 >= limit) {
                    return limit;
                }

                int[] vtemp = v0;
                v0 = v1;
                v1 = vtemp;
            }

            return v0[s2.length()];
        }
    }

}
