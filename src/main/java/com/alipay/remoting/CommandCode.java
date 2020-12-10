package com.alipay.remoting;

/**
 * Remoting command code stands for a specific remoting command, and every kind of command has its own code.
 *
 * @author jiangping
 * @version $Id: CommandCode.java, v 0.1 2015-9-7 PM7:10:18 tao Exp $
 */
public interface CommandCode {
    // value 0 is occupied by heartbeat, don't use value 0 for other commands
    short HEARTBEAT_VALUE = 0;

    /**
     * @return the short value of the code
     */
    short value();

}
