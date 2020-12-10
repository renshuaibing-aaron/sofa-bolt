package com.alipay.remoting.rpc;

import com.alipay.remoting.CommonCommandCode;
import com.alipay.remoting.util.IDGenerator;

/**
 * Heart beat.
 *心跳请求命令
 * @author jiangping
 * @version $Id: HeartbeatCommand.java, v 0.1 2015-9-10 AM9:46:36 tao Exp $
 */
public class HeartbeatCommand extends RequestCommand {

    /**
     * For serialization
     */
    private static final long serialVersionUID = 4949981019109517725L;

    /**
     * Construction.
     */
    public HeartbeatCommand() {
        super(CommonCommandCode.HEARTBEAT);
        this.setId(IDGenerator.nextId());
    }

}
