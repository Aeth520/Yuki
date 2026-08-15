package cn.aetheris.yuki.math.stats;

/**
 * Chi-square (χ²) contingency table analysis, ported from Intave's ContingencyTable/Gamma.
 * Used to detect statistically implausible distributions (e.g. click patterns,
 * rotation deltas) by measuring deviation from expected frequencies.
 */
public final class ChiSquare {
    private final int rows;
    private final int columns;
    private final long[][] table;

    public ChiSquare(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.table = new long[rows][columns];
    }

    public void increment(int row, int column) {
        if (row < 0 || row >= rows || column < 0 || column >= columns) {
            throw new IndexOutOfBoundsException(row + "x" + column + " in " + rows + "x" + columns);
        }
        table[row][column]++;
    }

    public long get(int row, int column) {
        return table[row][column];
    }

    public long rowSum(int row) {
        long sum = 0;
        for (int i = 0; i < columns; i++) {
            sum += table[row][i];
        }
        return sum;
    }

    public long columnSum(int column) {
        long sum = 0;
        for (int i = 0; i < rows; i++) {
            sum += table[i][column];
        }
        return sum;
    }

    public long total() {
        long sum = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                sum += table[i][j];
            }
        }
        return sum;
    }

    /**
     * P(A) — marginal probability of a row event.
     */
    public double probabilityOf(int row) {
        return (double) rowSum(row) / total();
    }

    /**
     * P(A|B) — conditional probability of a row event given a column condition.
     */
    public double conditionalProbabilityOf(int row, int column) {
        long denom = columnSum(column);
        return denom == 0 ? 0 : (double) get(row, column) / denom;
    }

    /**
     * Pearson chi-square statistic: measures how far observed counts
     * deviate from the independence-expected counts.
     */
    public double chi2() {
        long total = total();
        if (total == 0) return 0;

        double chi2 = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                double expected = (double) rowSum(i) * columnSum(j) / total;
                if (expected > 0) {
                    chi2 += Math.pow(get(i, j) - expected, 2) / expected;
                }
            }
        }
        return chi2;
    }

    /**
     * p-value of the chi-square statistic via the regularized incomplete gamma
     * function. Low p-value means the observed distribution is very unlikely
     * under the independence hypothesis (suspicious).
     */
    public double pValue() {
        double degreesOfFreedom = Math.max(1, (rows - 1) * (columns - 1));
        double p = Gamma.regularizedUpperIncompleteGamma(degreesOfFreedom * 0.5, chi2() * 0.5);
        return Math.max(0, Math.min(1, p));
    }

    public void clear() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                table[i][j] = 0;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                builder.append(get(i, j)).append(' ');
            }
            builder.append('\n');
        }
        return builder.toString();
    }
}
