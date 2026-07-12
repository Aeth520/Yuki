package cn.aetheris.yuki.check.impl.combat.analysis.analysisH;

import cn.aetheris.yuki.Yuki;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.logging.Logger;

/**
 * Loads {@link LogisticRegressionModel} weights from external JSON files
 * located in {@code plugins/Yuki/models/}. On first run the default
 * model is written to disk so server owners can inspect and iterate on
 * the weights without recompiling the plugin.
 */
public final class ModelLoader {

    private static final String MODELS_DIR = "models";
    private static final String ANALYSIS_H_FILE = "analysis-h.json";

    private static final Gson GSON = new Gson();

    private ModelLoader() {
    }

    public static LogisticRegressionModel loadAnalysisH() {
        return load(ANALYSIS_H_FILE,
                LogisticRegressionModel.getDefaultWeights(),
                LogisticRegressionModel.getDefaultBias());
    }

    /**
     * Load a model from the models directory. If the file does not exist,
     * the supplied default weights/bias are persisted to disk first so the
     * user can edit them, then returned.
     *
     * @param fileName       file name inside the models directory
     * @param defaultWeights fallback weights
     * @param defaultBias    fallback bias
     * @return a model instance, never {@code null}
     */
    public static LogisticRegressionModel load(String fileName,
                                               double[] defaultWeights,
                                               double defaultBias) {
        File dir = new File(Yuki.getInstance().getDataFolder(), MODELS_DIR);
        if (!dir.exists() && !dir.mkdirs()) {
            return new LogisticRegressionModel(defaultWeights, defaultBias);
        }

        File file = new File(dir, fileName);
        if (!file.exists()) {
            writeDefaultModel(file, defaultWeights, defaultBias);
            return new LogisticRegressionModel(defaultWeights, defaultBias);
        }

        try {
            String raw = new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder(raw.length());
            for (String line : raw.split("\n", -1)) {
                String trimmed = line.trim();
                if (trimmed.startsWith("#")) continue;
                sb.append(line).append('\n');
            }
            JsonObject root = new JsonParser().parse(sb.toString()).getAsJsonObject();

            double[] weights = GSON.fromJson(root.get("weights"), double[].class);
            double bias = root.get("bias").getAsDouble();

            if (weights == null || weights.length == 0) {
                warn("Model '" + fileName + "' has empty weights, using defaults");
                return new LogisticRegressionModel(defaultWeights, defaultBias);
            }
            return new LogisticRegressionModel(weights, bias);
        } catch (Exception e) {
            warn("Failed to load model '" + fileName + "': " + e.getMessage() + ", using defaults");
            return new LogisticRegressionModel(defaultWeights, defaultBias);
        }
    }

    private static void writeDefaultModel(File file, double[] weights, double bias) {
        try {
            JsonObject root = new JsonObject();
            root.add("weights", GSON.toJsonTree(weights));
            root.addProperty("bias", bias);

            String header = "# Yuki AnalysisH logistic regression model\n" +
                    "# Edit weights/bias and reload with /yuki reload\n" +
                    "# Do not change the number of weights (must match feature count)\n";

            Files.write(file.toPath(),
                    (header + GSON.toJson(root)).getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            warn("Failed to write default model '" + file.getName() + "': " + e.getMessage());
        }
    }

    private static void warn(String message) {
        Logger logger = Yuki.getInstance().getLogger();
        if (logger != null) {
            logger.warning(message);
        }
    }
}
