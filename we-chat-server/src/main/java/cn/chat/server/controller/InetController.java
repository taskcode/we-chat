package cn.chat.server.controller;

import cn.chat.server.common.EasyResult;
import cn.chat.server.service.InetService;
import cn.chat.server.pojo.model.inet.ChannelUserInfo;
import cn.chat.server.pojo.model.inet.ChannelUserReq;
import cn.chat.server.pojo.model.inet.InetServerInfo;
import com.alibaba.fastjson.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName：
 * @Description:
 * @Author：13738700108
 * @Data 2021/8/8 9:43
 * @Version: v1.0
 **/
@Controller
public class InetController {

    private Logger logger = LoggerFactory.getLogger(InetController.class);

    @Resource
    private InetService inetService;

    @RequestMapping("api/queryNettyServerInfo")
    @ResponseBody
    public EasyResult queryNettyServerInfo() {
        try {
            return EasyResult.buildEasyResultSuccess(new ArrayList<InetServerInfo>() {{
                add(inetService.queryNettyServerInfo());
            }});
        } catch (Exception e) {
            logger.info("查询NettyServer失败。", e);
            return EasyResult.buildEasyResultError(e);
        }
    }

    @RequestMapping("api/queryChannelUserList")
    @ResponseBody
    public EasyResult queryChannelUserList(String json, String page, String limit) {
        try {
            logger.info("查询通信管道用户信息列表开始。req：{}", json);
            ChannelUserReq req = JSON.parseObject(json, ChannelUserReq.class);
            if (null == req) {
                req = new ChannelUserReq();
            }
            req.setPage(page, limit);
            Long count = inetService.queryChannelUserCount(req);
            List<ChannelUserInfo> list = inetService.queryChannelUserList(req);
            logger.info("查询通信管道用户信息列表完成。list：{}", JSON.toJSONString(list));
            return EasyResult.buildEasyResultSuccess(count, list);
        } catch (Exception e) {
            logger.info("查询通信管道用户信息列表失败。req：{}", json, e);
            return EasyResult.buildEasyResultError(e);
        }
    }

}
