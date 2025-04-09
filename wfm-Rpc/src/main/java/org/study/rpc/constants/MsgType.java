package org.study.rpc.constants;

/**
 * @description: 消息类型常量
 */
public enum  MsgType {

    REQUEST,
    RESPONSE,
    HEARTBEAT;

    public static MsgType findByType(int type) {

        return MsgType.values()[type];
    }
}
