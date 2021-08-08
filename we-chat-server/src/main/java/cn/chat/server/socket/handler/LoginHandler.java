package cn.chat.server.socket.handler;

import cn.chat.agreement.protocol.login.LoginRequest;
import cn.chat.agreement.protocol.login.LoginResponse;
import cn.chat.agreement.protocol.login.dto.ChatRecordDto;
import cn.chat.agreement.protocol.login.dto.ChatTalkDto;
import cn.chat.agreement.protocol.login.dto.GroupsDto;
import cn.chat.agreement.protocol.login.dto.UserFriendDto;
import cn.chat.server.application.UserService;
import cn.chat.server.domain.user.model.*;
import cn.chat.server.infrastructure.common.Constants;
import cn.chat.server.infrastructure.common.SocketChannelUtil;
import cn.chat.server.socket.MyBizHandler;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.List;


/**
 * @ClassName： LoginHandler
 * @Description: 登陆请求处理
 * @Author：13738700108
 * @Data 2021/8/8 9:43
 * @Version: v1.0
 **/
public class LoginHandler extends MyBizHandler<LoginRequest> {

    public LoginHandler(UserService userService) {
        super(userService);
    }

    @Override
    public void channelRead(Channel channel, LoginRequest msg) {
        logger.info("登陆请求处理：{} ", JSON.toJSONString(msg));
        // 1. 登陆失败返回false
        boolean auth = userService.checkAuth(msg.getUserId(), msg.getUserPassword());
        if (!auth) {
            // 传输消息
            channel.writeAndFlush(new LoginResponse(false));
            return;
        }
        // 2. 登陆成功绑定Channel
        // 2.1 绑定用户ID
        SocketChannelUtil.addChannel(msg.getUserId(), channel);
        // 2.2 绑定群组
        List<String> groupsIdList = userService.queryUserGroupsIdList(msg.getUserId());
        for (String groupId : groupsIdList) {
            SocketChannelUtil.addChannelGroup(groupId, channel);
        }
        // 3. 反馈消息；用户信息、用户对话框列表、好友列表、群组列表
        // 组装消息包
        LoginResponse loginResponse = new LoginResponse();
        // 3.1 用户信息
        UserInfo userInfo = userService.queryUserInfo(msg.getUserId());
        // 3.2 对话框
        List<TalkBoxInfo> talkBoxInfoList = userService.queryTalkBoxInfoList(msg.getUserId());
        for (TalkBoxInfo talkBoxInfo : talkBoxInfoList) {
            ChatTalkDto chatTalk = new ChatTalkDto();
            chatTalk.setTalkId(talkBoxInfo.getTalkId());
            chatTalk.setTalkType(talkBoxInfo.getTalkType());
            chatTalk.setTalkName(talkBoxInfo.getTalkName());
            chatTalk.setTalkHead(talkBoxInfo.getTalkHead());
            chatTalk.setTalkSketch(talkBoxInfo.getTalkSketch());
            chatTalk.setTalkDate(talkBoxInfo.getTalkDate());
            loginResponse.getChatTalkList().add(chatTalk);

            // 好友；聊天消息
            if (Constants.TalkType.Friend.getCode().equals(talkBoxInfo.getTalkType())) {
                List<ChatRecordDto> chatRecordDtoList = new ArrayList<>();
                List<ChatRecordInfo> chatRecordInfoList = userService.queryChatRecordInfoList(talkBoxInfo.getTalkId(), msg.getUserId(), Constants.TalkType.Friend.getCode());
                for (ChatRecordInfo chatRecordInfo : chatRecordInfoList) {
                    ChatRecordDto chatRecordDto = new ChatRecordDto();
                    chatRecordDto.setTalkId(talkBoxInfo.getTalkId());
                    boolean msgType = msg.getUserId().equals(chatRecordInfo.getUserId());
                    // 自己发的消息
                    if (msgType) {
                        chatRecordDto.setUserId(chatRecordInfo.getUserId());
                        chatRecordDto.setMsgUserType(0); // 消息类型[0自己/1好友]
                    }
                    // 好友发的消息
                    else {
                        chatRecordDto.setUserId(chatRecordInfo.getFriendId());
                        chatRecordDto.setMsgUserType(1); // 消息类型[0自己/1好友]
                    }
                    chatRecordDto.setMsgContent(chatRecordInfo.getMsgContent());
                    chatRecordDto.setMsgType(chatRecordInfo.getMsgType());
                    chatRecordDto.setMsgDate(chatRecordInfo.getMsgDate());
                    chatRecordDtoList.add(chatRecordDto);
                }
                chatTalk.setChatRecordList(chatRecordDtoList);
            }
            // 群组；聊天消息
            else if (Constants.TalkType.Group.getCode().equals(talkBoxInfo.getTalkType())) {
                List<ChatRecordDto> chatRecordDtoList = new ArrayList<>();
                List<ChatRecordInfo> chatRecordInfoList = userService.queryChatRecordInfoList(talkBoxInfo.getTalkId(), msg.getUserId(), Constants.TalkType.Group.getCode());
                for (ChatRecordInfo chatRecordInfo : chatRecordInfoList) {
                    UserInfo memberInfo = userService.queryUserInfo(chatRecordInfo.getUserId());
                    ChatRecordDto chatRecordDto = new ChatRecordDto();
                    chatRecordDto.setTalkId(talkBoxInfo.getTalkId());
                    chatRecordDto.setUserId(memberInfo.getUserId());
                    chatRecordDto.setUserNickName(memberInfo.getUserNickName());
                    chatRecordDto.setUserHead(memberInfo.getUserHead());
                    chatRecordDto.setMsgContent(chatRecordInfo.getMsgContent());
                    chatRecordDto.setMsgDate(chatRecordInfo.getMsgDate());
                    boolean msgType = msg.getUserId().equals(chatRecordInfo.getUserId());
                    chatRecordDto.setMsgUserType(msgType ? 0 : 1); // 消息类型[0自己/1好友]
                    chatRecordDto.setMsgType(chatRecordInfo.getMsgType());
                    chatRecordDtoList.add(chatRecordDto);
                }
                chatTalk.setChatRecordList(chatRecordDtoList);
            }

        }
        // 3.3 群组
        List<GroupsInfo> groupsInfoList = userService.queryUserGroupInfoList(msg.getUserId());
        for (GroupsInfo groupsInfo : groupsInfoList) {
            GroupsDto groups = new GroupsDto();
            groups.setGroupId(groupsInfo.getGroupId());
            groups.setGroupName(groupsInfo.getGroupName());
            groups.setGroupHead(groupsInfo.getGroupHead());
            loginResponse.getGroupsList().add(groups);
        }
        // 3.4 好友
        List<UserFriendInfo> userFriendInfoList = userService.queryUserFriendInfoList(msg.getUserId());
        for (UserFriendInfo userFriendInfo : userFriendInfoList) {
            UserFriendDto userFriend = new UserFriendDto();
            userFriend.setFriendId(userFriendInfo.getFriendId());
            userFriend.setFriendName(userFriendInfo.getFriendName());
            userFriend.setFriendHead(userFriendInfo.getFriendHead());
            loginResponse.getUserFriendList().add(userFriend);
        }

        loginResponse.setSuccess(true);
        loginResponse.setUserId(userInfo.getUserId());
        loginResponse.setUserNickName(userInfo.getUserNickName());
        loginResponse.setUserHead(userInfo.getUserHead());
        // 传输消息
        channel.writeAndFlush(loginResponse);
    }

}
