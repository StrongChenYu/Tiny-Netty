package com.chenyu.netty.channel;

public class DefaultChannelId implements ChannelId {

    private static final long serialVersionUID = 3884076183504074063L;
    private String longValue;


    public static DefaultChannelId newInstance() {
        return new DefaultChannelId();
    }

    private DefaultChannelId() {
        long currentTimeMillis = System.currentTimeMillis();
        this.longValue = String.valueOf(currentTimeMillis);
    }

    @Override
    public String asShortText() {
        return null;
    }

    @Override
    public String asLongText() {
        String longValue = this.longValue;
        if (longValue == null) {
            this.longValue = longValue = String.valueOf(System.currentTimeMillis());
        }
        return longValue;
    }

    @Override
    public String toString() {
        return asShortText();
    }

    @Override
    public int compareTo(ChannelId o) {
        return 0;
    }
}
