package com.nowcoder.community.event;

import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static com.nowcoder.community.util.CommunityConstant.*;

/**
 * @author aiolia
 * @version 1.0
 * @create 2023/2/3
 */
@Slf4j
@Component
public class EventConsumer
{
    @Autowired
    private MessageService messageService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    @Value("${wk.image.command}")
    private String wkImageCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretKey;

    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;

    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;


    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentService(ConsumerRecord record)
    {
        if (record == null || record.value() == null)
        {
            log.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null)
        {
            log.error("消息格式错误！");
            return;
        }
        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        message.setStatus(0);

        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityType", event.getEntityType());
        content.put("entityId", event.getEntityId());

        if (!event.getData().isEmpty())
        {
            for (Map.Entry<String, Object> entry : event.getData().entrySet())
            {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record)
    {
        if (record == null || record.value() == null)
        {
            log.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null)
        {
            log.error("消息格式错误！");
            return;
        }

        DiscussPost post = discussPostService.getById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }

    // 消费删帖事件
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record)
    {
        if (record == null || record.value() == null)
        {
            log.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null)
        {
            log.error("消息格式错误!");
            return;
        }

        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }

    //消费分享事件
    @KafkaListener(topics = TOPIC_SHARE)
    public void handleShareMessage(ConsumerRecord record)
    {
        if (record == null || record.value() == null)
        {
            log.error("消息的内容为空!");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if (event == null)
        {
            log.error("消息格式错误!");
            return;
        }

        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");

        String cmd = wkImageCommand + " --quality 75 " + htmlUrl + " " + wkImageStorage + "/" + fileName + suffix;
        try
        {
            Runtime.getRuntime().exec(cmd);
            log.info("生成长图成功：" + cmd);
        } catch (IOException e)
        {
            log.error("生成长图失败：" + e.getMessage());
        }
        // 启用定时器,监视该图片,一旦生成了,则上传至七牛云.
        UploadTask task = new UploadTask(fileName, suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);
        //qi
    }

    class UploadTask implements Runnable
    {

        // 文件名称
        private String fileName;
        // 文件后缀
        private String suffix;
        // 启动任务的返回值
        private Future future;
        // 开始时间
        private long startTime;
        // 上传次数
        private int uploadTimes;

        public UploadTask(String fileName, String suffix)
        {
            this.fileName = fileName;
            this.suffix = suffix;
            this.startTime = System.currentTimeMillis();
        }

        public void setFuture(Future future)
        {
            this.future = future;
        }

        @Override
        public void run()
        {
            // 生成失败
            if (System.currentTimeMillis() - startTime > 30000)
            {
                log.error("执行时间过长,终止任务:" + fileName);
                future.cancel(true);
                return;
            }
            // 上传失败
            if (uploadTimes >= 3)
            {
                log.error("上传次数过多,终止任务:" + fileName);
                future.cancel(true);
                return;
            }

            String path = wkImageStorage + "/" + fileName + suffix;
            File file = new File(path);
            if (file.exists())
            {
                log.info(String.format("开始第%d次上传[%s].", ++uploadTimes, fileName));
                // 设置响应信息
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJsonString(0));
                // 生成上传凭证
                Auth auth = Auth.create(accessKey, secretKey);
                String uploadToken = auth.uploadToken(shareBucketName, fileName, 3600, policy);
                // 指定上传机房
                UploadManager manager = new UploadManager(new Configuration(Zone.zone1()));
                try
                {
                    // 开始上传图片
                    Response response = manager.put(
                            path, fileName, uploadToken, null, "image/" + suffix, false);
                    // 处理响应结果
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if (json == null || json.get("code") == null || !json.get("code").toString().equals("0"))
                    {
                        log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                    } else
                    {
                        log.info(String.format("第%d次上传成功[%s].", uploadTimes, fileName));
                        future.cancel(true);
                    }
                } catch (QiniuException e)
                {
                    log.info(String.format("第%d次上传失败[%s].", uploadTimes, fileName));
                }
            } else
            {
                log.info("等待图片生成[" + fileName + "].");
            }
        }
    }

    // 定时方法，每隔4分钟清理一次用户生成的长图（清理1分钟之前创建的图片）
    // 这是每台主机上都要执行的操作，因此就用Spring定时任务线程池
    @Scheduled(fixedDelay = 240000)
    public void clearShareImage()
    {
        log.info("[任务开始] 开始清理服务器上用户分享生成的长图...");
        File dir = new File(wkImageStorage);
        if (dir.listFiles() != null && dir.listFiles().length != 0)
        {
            for (File file : dir.listFiles())
            {
                try
                {
                    Path path = Paths.get(file.getAbsolutePath());
                    FileTime creationTime = Files.readAttributes(path, BasicFileAttributes.class).creationTime();
                    if (System.currentTimeMillis() - creationTime.toMillis() >= 60 * 1000)
                    {
                        file.delete();
                        log.info("[删除成功] 文件[{}]已删除！", file.getName());
                    }
                } catch (IOException e)
                {
                    log.error("[删除失败] 文件[{}]删除失败！", file.getName());
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        log.info("[任务完成] 用户长图清理结束！");
    }
}
