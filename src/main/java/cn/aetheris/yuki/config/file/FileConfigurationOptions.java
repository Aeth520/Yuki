package cn.aetheris.yuki.config.file;

import cn.aetheris.yuki.config.MemoryConfiguration;
import cn.aetheris.yuki.config.MemoryConfigurationOptions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;


public class FileConfigurationOptions extends MemoryConfigurationOptions {
    
    private static final boolean PAPER_PARSE_COMMENTS_BY_DEFAULT = Boolean.parseBoolean(System.getProperty("Paper.parseYamlCommentsByDefault", "true"));
    private List<String> header = Collections.emptyList();
    private List<String> footer = Collections.emptyList();
    private boolean parseComments = PAPER_PARSE_COMMENTS_BY_DEFAULT;
    

    protected FileConfigurationOptions(@NotNull MemoryConfiguration configuration) {
        super(configuration);
    }

    @Override
    public @NotNull MemoryConfiguration configuration() {
        return super.configuration();
    }

    @NotNull
    @Override
    public FileConfigurationOptions copyDefaults(boolean value) {
        super.copyDefaults(value);
        return this;
    }

    @NotNull
    @Override
    public FileConfigurationOptions pathSeparator(char value) {
        super.pathSeparator(value);
        return this;
    }

    
    @NotNull
    public List<String> getHeader() {
        return header;
    }


    @NotNull
    public FileConfigurationOptions setHeader(@Nullable List<String> value) {
        this.header = (value == null) ? Collections.emptyList() : Collections.unmodifiableList(value);
        return this;
    }

    
    @NotNull
    public List<String> getFooter() {
        return footer;
    }

    
    @NotNull
    public FileConfigurationOptions setFooter(@Nullable List<String> value) {
        this.footer = (value == null) ? Collections.emptyList() : Collections.unmodifiableList(value);
        return this;
    }

    
    public boolean parseComments() {
        return parseComments;
    }

    
    @NotNull
    public MemoryConfigurationOptions parseComments(boolean value) {
        parseComments = value;
        return this;
    }
}
