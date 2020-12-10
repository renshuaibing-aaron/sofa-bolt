package com.alipay.remoting.rpc;

import com.alipay.remoting.CommonCommandCode;
import com.alipay.remoting.ResponseStatus;

/**
 * Heartbeat ack.
 *心跳响应命令
 * @author jiangping
 * @version $Id: HeartbeatAckCommand.java, v 0.1 2015-9-29 AM11:46:11 tao Exp $
 */
public class HeartbeatAckCommand extends ResponseCommand {
    /**
     * For serialization
     */
    private static final long serialVersionUID = 2584912495844320855L;

    /**
     * Constructor.
     */
    public HeartbeatAckCommand() {
        super(CommonCommandCode.HEARTBEAT);
        this.setResponseStatus(ResponseStatus.SUCCESS);
    }
}
