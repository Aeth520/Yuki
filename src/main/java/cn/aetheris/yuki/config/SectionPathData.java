package cn.aetheris.yuki.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

final class SectionPathData {

    private Object data;
    private List<String> comments;
    private List<String> inlineComments;

    public SectionPathData(@Nullable Object data) {
        this.data = data;
        comments = Collections.emptyList();
        inlineComments = Collections.emptyList();
    }

    @Nullable
    public Object getData() {
        return data;
    }

    public void setData(@Nullable final Object data) {
        this.data = data;
    }

    
    @NotNull
    public List<String> getComments() {
        return comments;
    }

    
    public void setComments(@Nullable final List<String> comments) {
        this.comments = (comments == null) ? Collections.emptyList() : Collections.unmodifiableList(comments);
    }

    
    @NotNull
    public List<String> getInlineComments() {
        return inlineComments;
    }

    
    public void setInlineComments(@Nullable final List<String> inlineComments) {
        this.inlineComments = (inlineComments == null) ? Collections.emptyList() : Collections.unmodifiableList(inlineComments);
    }
}
