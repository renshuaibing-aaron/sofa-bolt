package com.alipay.remoting.rpc;

import com.alipay.remoting.ProtocolCode;
import com.alipay.remoting.codec.Codec;
import com.alipay.remoting.codec.ProtocolCodeBasedDecoder;
import com.alipay.remoting.codec.ProtocolCodeBasedEncoder;
import com.alipay.remoting.rpc.protocol.RpcProtocolDecoder;
import com.alipay.remoting.rpc.protocol.RpcProtocolManager;
import com.alipay.remoting.rpc.protocol.RpcProtocolV2;
import io.netty.channel.ChannelHandler;

/**
 *  Rpc 场景下的编解码器工厂类
 *    注意类里面的继承关系
 *  todo
 *    编码器
 *    ProtocolCodeBasedEncoder-->MessageToByteEncoder<Serializable>
 *   解码器
 *    RpcProtocolDecoder-->ProtocolCodeBasedDecoder-->AbstractBatchDecoder(重写了netty中的ByteToMessageDecoder 提供了批量功能)
 *
 *  其实这里编码解码器都是代理类 因为真正的编码解码器是在协议里面封装这呢 当走到这些个编码解码方法的时候
 *  会从协议容器里面取出协议类 然后利用协议类中实际编码解码器类进行编码解码
 *
 * @author muyun.cyt
 * @version 2018/6/26 下午3:51
 */
public class RpcCodec implements Codec {

    @Override
    public ChannelHandler newEncoder() {
        //这里可以根据不同的协议 可以选择不同的编码器
        return new ProtocolCodeBasedEncoder(ProtocolCode.fromBytes(RpcProtocolV2.PROTOCOL_CODE));
    }

    @Override
    public ChannelHandler newDecoder() {
        //解码器代理类 根据不同的协议选择不同的解码器

       //return new ProtocolCodeBasedDecoder(RpcProtocolManager.DEFAULT_PROTOCOL_CODE_LENGTH);
        return new RpcProtocolDecoder(RpcProtocolManager.DEFAULT_PROTOCOL_CODE_LENGTH);
    }
}


/*
*

*
*
*
*
*
*
*
*
*
*
* */

