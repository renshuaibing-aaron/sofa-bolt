如上图所示，整个 RemotingCommand 命令协议包括以下几部分：


RemotingCommand 命令结构的设计：定义 RemotingCommand 基本结构
CommandFactory 命令工厂的设计：创建 RemotingCommand 实例
CommandHandler 命令处理器调用者的设计： RemotingCommand 处理器调用者
ProcessorManager 命令处理器容器的设计：RemotingCommand 处理器的容器
RemotingProcessor 命令处理器的设计：真正的 RemotingCommand 处理器
UserProcessor 用户处理器的设计：用户自定义处理器

https://github.com/sofastack/sofa-bolt#14-%E5%9F%BA%E7%A1%80%E9%80%9A%E4%BF%A1%E6%A8%A1%E5%9E%8B



请求链 RpcHandler -> RpcCommandHandler -> RpcRequestProcessor -> UserProcessor
响应链 RpcHandler -> RpcCommandHandler -> RpcResponseProcessor
心跳链 RpcHandler -> RpcCommandHandler -> RpcHeartBeatProcessor



为什么需要序列化协议，在编解码过程中，需要对java对象进行序列化

当发起请求时，例如 invokeSync() 时，RpcRemoting 会先对请求数据进行序列化，之后编码发送
当收到请求时，对请求消息进行解码，然后 RpcRequestProcessor 会对解码后的请求数据进行精细的反序列化；
处理请求完成之后，RpcRequestProcessor 会对响应消息进行序列化，之后编码发送
收到响应消息后，对响应消息进行解码，然后会在 RpcInvokeCallbackListener 或者 RpcResponseResolver 中对解码后的响应消息进行反序列化


疑问：

RemotingCommand 作为命令模式的 Command 角色
CommandHandler 作为命令模式的 Client 角色
ProcessorManager 作为命令模式的 Invoker 角色
RemotingProcessor 作为命令模式的 Receiver 角色


CommandHandler 从 ProcessorManager 中取出相应的处理器 RemotingProcessor 实例，对 RemotingCommand 进行处理。