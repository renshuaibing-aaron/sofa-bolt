package com.alipay.remoting.aaron.fullduplex;

// 双工通信机制的设计


/*
SOFABolt 四种调用模式：oneway / sync / future / callback
        SOFABolt 三种调用链路：addr / url / connection，
        注意：在整个调用过程中，调用链路会发生如下转化：addr -> url -> connection

        说明：
        基于 Connection 的，值得注意的是，Connection 也是三种调用链路最底层的,最后调用的都是connection方法
*/
