package com.github.stephanenicolas.mimic;

public class MimicException extends Exception {

    private static final long serialVersionUID = 3416316648540263321L;

    public MimicException() {
        super();
    }

    public MimicException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    public MimicException(String arg0) {
        super(arg0);
    }

    public MimicException(Throwable arg0) {
        super(arg0);
    }
}
