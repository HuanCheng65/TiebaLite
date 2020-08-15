package com.huanchengfly.tieba.post.api.caster;

public abstract class ICaster<A, B> {
    public abstract B cast(A a);
}