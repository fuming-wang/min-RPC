package org.study.rpc.protocol.serialization;

/**
 * @description: 序列化异常
 */
public class SerializationException extends RuntimeException{

    private static final long serialVersionUID = -1;

    public SerializationException() {
        super();
    }

    public SerializationException(String msg) {
        super(msg);
    }

    public SerializationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }
}
