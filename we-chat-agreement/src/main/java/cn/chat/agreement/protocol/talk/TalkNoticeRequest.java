package cn.chat.agreement.protocol.talk;

import cn.chat.agreement.protocol.Command;
import cn.chat.agreement.protocol.Packet;

/**
 * @ClassName：TalkNoticeRequest
 * @Description: 对话通知请求
 * @Author：13738700108
 * @Data 2021/8/8 9:43
 * @Version: v1.0
 **/
public class TalkNoticeRequest extends Packet {

    private String userId;       // 用户ID
    private String friendUserId; // 好友ID
    private Integer talkType;    // 对话框类型[0好友、1群组]

    public TalkNoticeRequest(){}

    public TalkNoticeRequest(String userId, String friendUserId, Integer talkType) {
        this.userId = userId;
        this.friendUserId = friendUserId;
        this.talkType = talkType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getFriendUserId() {
        return friendUserId;
    }

    public void setFriendUserId(String friendUserId) {
        this.friendUserId = friendUserId;
    }

    public Integer getTalkType() {
        return talkType;
    }

    public void setTalkType(Integer talkType) {
        this.talkType = talkType;
    }

    @Override
    public Byte getCommand() {
        return Command.TalkNoticeRequest;
    }
}