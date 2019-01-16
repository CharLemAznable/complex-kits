package com.github.charlemaznable.lang.ex;

public class BlankStringException extends RuntimeException {

    private static final long serialVersionUID = -1676373107317262934L;

    public BlankStringException() {
        super();
    }

    public BlankStringException(String s) {
        super(s);
    }
}
