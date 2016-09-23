package com.ksy.recordlib.service.recoder;

/**
 * Created by 1 on 2016/2/17.
 */
public class RtmpObj<T>{
    private T o = null;

    public void setObject(T n) {
        this.o = n;
    }

    public T getObject() {
        return this.o;
    }

    public boolean isEmpty() {
        return o == null;
    }
}
