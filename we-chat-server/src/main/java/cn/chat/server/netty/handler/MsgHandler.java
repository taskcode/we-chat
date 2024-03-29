package cn.chat.server.netty.handler;

import cn.chat.agreement.protocol.msg.MsgRequest;
import cn.chat.agreement.protocol.msg.MsgResponse;
import cn.chat.server.common.enums.TalkTypeEnum;
import cn.chat.server.service.UserService;
import cn.chat.server.pojo.info.ChatRecordInfo;
import cn.chat.server.common.SocketChannelUtil;
import cn.chat.server.netty.MyBizHandler;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;



/**

 * 消息信息处理
 */
public class MsgHandler extends MyBizHandler<MsgRequest> {

    public MsgHandler(UserService userService) {
        super(userService);
    }

    @Override
    public void channelRead(Channel channel, MsgRequest msg) {
        logger.info("消息信息处理：{}", JSON.toJSONString(msg));
        // 异步写库
        userService.asyncAppendChatRecord(new ChatRecordInfo(msg.getUserId(), msg.getFriendId(), msg.getMsgText(), msg.getMsgType(), msg.getMsgDate()));
        // 添加对话框[如果对方没有你的对话框则添加]
        userService.addTalkBoxInfo(msg.getFriendId(), msg.getUserId(), TalkTypeEnum.Friend.getCode());
        // 获取好友通信管道
        Channel friendChannel = SocketChannelUtil.getChannel(msg.getFriendId());
        if (null == friendChannel) {
            logger.info("用户id：{}未登录！", msg.getFriendId());
            return;
        }
        // 发送消息
        friendChannel.writeAndFlush(new MsgResponse(msg.getUserId(), msg.getMsgText(), msg.getMsgType(), msg.getMsgDate()));
    }

}
