package cn.aetheris.yuki.math.stats;

/**
 * Gamma function and regularized incomplete gamma functions.
 * Ported from Intave (originally smile.math.special.Gamma, Apache 2.0).
 */
public final class Gamma {
    private static final double FPMIN = 1.0E-300;
    private static final double[] LANCZOS_COEFF = {
            1.000000000190015, 76.18009172947146, -86.50532032941678,
            24.01409824083091, -1.231739572450155, 0.001208650973866179, -5.395239384953E-6
    };
    private static final int MAX_ITERATIONS = 1000;
    private static final double EPSILON = 1.0E-8;

    private Gamma() {
    }

    public static double gamma(double x) {
        double xcopy = x;
        double first = x + 5.5;
        double second = LANCZOS_COEFF[0];
        double fg;
        if (x >= 0.0) {
            if (x >= 1.0 && x - (int) x == 0.0) {
                fg = factorial((int) x - 1);
            } else {
                first = Math.pow(first, x + 0.5) * Math.exp(-first);
                for (int i = 1; i <= 6; ++i) {
                    second += LANCZOS_COEFF[i] / ++xcopy;
                }
                fg = first * Math.sqrt(2 * Math.PI) * second / x;
            }
        } else {
            fg = -Math.PI / (x * gamma(-x) * Math.sin(Math.PI * x));
        }
        return fg;
    }

    public static double lgamma(double x) {
        double xcopy = x;
        double fg;
        double first = x + 5.5;
        double second = LANCZOS_COEFF[0];
        if (x >= 0.0) {
            if (x >= 1.0 && x - (int) x == 0.0) {
                fg = lfactorial((int) x - 1);
            } else {
                first -= (x + 0.5) * Math.log(first);
                for (int i = 1; i <= 6; ++i) {
                    second += LANCZOS_COEFF[i] / ++xcopy;
                }
                fg = Math.log(Math.sqrt(2 * Math.PI) * second / x) - first;
            }
        } else {
            fg = Math.PI / (gamma(1.0 - x) * Math.sin(Math.PI * x));
            if (Double.isFinite(fg)) {
                if (fg < 0.0) {
                    throw new IllegalArgumentException("The gamma function is negative: " + fg);
                }
                fg = Math.log(fg);
            }
        }
        return fg;
    }

    public static double factorial(int n) {
        double f = 1.0;
        for (int i = 2; i <= n; ++i) {
            f *= i;
        }
        return f;
    }

    public static double lfactorial(int n) {
        double f = 0.0;
        for (int i = 2; i <= n; ++i) {
            f += Math.log(i);
        }
        return f;
    }

    /**
     * Regularized lower incomplete gamma P(s, x).
     */
    public static double regularizedIncompleteGamma(double s, double x) {
        if (s < 0.0) throw new IllegalArgumentException("Invalid s: " + s);
        if (x < 0.0) throw new IllegalArgumentException("Invalid x: " + x);
        if (x < s + 1.0) {
            return series(s, x);
        }
        return fraction(s, x);
    }

    /**
     * Regularized upper incomplete gamma Q(s, x) = 1 - P(s, x).
     * This is the survival function used for chi-square p-values.
     */
    public static double regularizedUpperIncompleteGamma(double s, double x) {
        if (s < 0.0) throw new IllegalArgumentException("Invalid s: " + s);
        if (x < 0.0) throw new IllegalArgumentException("Invalid x: " + x);
        if (x == 0.0) return 1.0;
        if (Double.isNaN(x)) return 1.0;
        if (x < s + 1.0) {
            return 1.0 - series(s, x);
        }
        return 1.0 - fraction(s, x);
    }

    private static double series(double a, double x) {
        int i = 0;
        double igf;
        double acopy = a;
        double sum = 1.0 / a;
        double incr = sum;
        double loggamma = lgamma(a);

        do {
            ++i;
            ++a;
            incr *= x / a;
            sum += incr;
        } while (i < MAX_ITERATIONS && Math.abs(incr) >= Math.abs(sum) * EPSILON);

        igf = sum * Math.exp(-x + acopy * Math.log(x) - loggamma);
        return igf;
    }

    private static double fraction(double a, double x) {
        int i = 0;
        double igf;
        double loggamma = lgamma(a);
        double numer = 0.0;
        double incr = 0.0;
        double denom = x - a + 1.0;
        double first = 1.0 / denom;
        double term = 9.999999999999999E299;
        double prod = first;

        do {
            ++i;
            double ii = i;
            numer = -ii * (ii - a);
            denom += 2.0;
            first = numer * first + denom;
            if (Math.abs(first) < FPMIN) {
                first = FPMIN;
            }

            term = denom + numer / term;
            if (Math.abs(term) < FPMIN) {
                term = FPMIN;
            }

            first = 1.0 / first;
            incr = first * term;
            prod *= incr;
        } while (i < MAX_ITERATIONS && Math.abs(incr - 1.0) >= EPSILON);

        igf = 1.0 - Math.exp(-x + a * Math.log(x) - loggamma) * prod;
        return igf;
    }
}
