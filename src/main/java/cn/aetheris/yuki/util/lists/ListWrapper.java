package cn.aetheris.yuki.util.lists;

import lombok.Getter;

import java.util.List;


@Getter
public abstract class ListWrapper<T> implements List<T> {
    protected final List<T> base;

    public ListWrapper(List<T> base) {
        this.base = base;
    }

}