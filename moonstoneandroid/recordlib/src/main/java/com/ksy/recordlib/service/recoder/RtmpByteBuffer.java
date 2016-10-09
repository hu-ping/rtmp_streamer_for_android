package com.ksy.recordlib.service.recoder;

import java.nio.ByteBuffer;

/**
 * Created by 1 on 2016/5/5.
 */
public class RtmpByteBuffer {
    private ByteBuffer buffer = null;
    private int length = 0;

    public RtmpByteBuffer(int length) {
        if (length <= 0) {
            length = 1;
        }

        this.length = length;
        this.buffer = ByteBuffer.allocate(this.length);
    }

    public int length() {
        return buffer.position();
    }

    public void append(byte[] data, int size) {
        int position = buffer.position();
        int remainder = buffer.remaining();


        if (remainder >= size) {
            for(int i = 0; i < size; i++) {
                buffer.put(data[i]);
            }
        } else {
            //(旧容量*3)/2+1
            ByteBuffer temp = ByteBuffer.allocate(((position + size) * 3)/2 + 1);
            for(int i = 0; i < position; i++) {
                byte current = buffer.get(i);
                temp.put(current);
            }

            for(int i = 0; i < size; i++) {
                temp.put(data[i]);
            }

            length = ((position + size) * 3)/2 + 1;
            buffer = temp;
        }
    }

    public byte[] toBytes() {
        int position = buffer.position();
        if(position == 0) {
            return new byte[0];
        }

        byte[] temp = new byte[position];
        for(int i = 0; i < position; i++) {
            temp[i] = buffer.get(i);
        }

        return temp;
    }

    // trim the buffer, decrease the buffer space.
    public byte[] trimToSize() {
        int s = buffer.position();
        if (s == buffer.limit()) {
            return buffer.array();
        }
        if (s == 0) {
            return new byte[0];
        } else {
            byte[] newArray = new byte[s];
            System.arraycopy(buffer.array(), 0, newArray, 0, s);
            buffer = ByteBuffer.wrap(newArray);
            buffer.position(newArray.length);

            return buffer.array();
        }
    }

}
