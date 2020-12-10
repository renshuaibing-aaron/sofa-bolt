package com.alipay.remoting;

/**
 * The common command code, especially for heart beat command.
 *
 * @author jiangping
 * @version $Id: CommonCommandCode.java, v 0.1 2015-9-21 PM5:05:59 tao Exp $
 */
public enum CommonCommandCode implements CommandCode {

    HEARTBEAT(CommandCode.HEARTBEAT_VALUE);

    private short value;

    private CommonCommandCode(short value) {
        this.value = value;
    }

    public static CommonCommandCode valueOf(short value) {
        switch (value) {
            case CommandCode.HEARTBEAT_VALUE:
                return HEARTBEAT;
        }
        throw new IllegalArgumentException("Unknown Rpc command code value ," + value);
    }

    @Override
    public short value() {
        return this.value;
    }

}