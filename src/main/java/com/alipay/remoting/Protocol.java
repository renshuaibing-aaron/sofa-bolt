package com.alipay.remoting;

/**
 * A protocol contains a group of commands.
 *
 * @author jiangping
 * @version $Id: Protocol.java, v 0.1 2015-12-11 PM5:02:48 tao Exp $
 */
public interface Protocol {
    /**
     * Get the newEncoder for the protocol.
     *
     * @return
     */
    CommandEncoder getEncoder();

    /**
     * Get the decoder for the protocol.
     *
     * @return
     */
    CommandDecoder getDecoder();

    /**
     * Get the heartbeat trigger for the protocol.
     *
     * @return
     */
    HeartbeatTrigger getHeartbeatTrigger();

    /**
     * Get the command handler for the protocol.
     *
     * @return
     */
    CommandHandler getCommandHandler();

    /**
     * Get the command factory for the protocol.
     *
     * @return
     */
    CommandFactory getCommandFactory();
}
