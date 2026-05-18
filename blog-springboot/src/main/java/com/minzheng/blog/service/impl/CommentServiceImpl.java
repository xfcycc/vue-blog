package com.minzheng.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.minzheng.blog.dao.*;
import com.minzheng.blog.dto.*;
import com.minzheng.blog.entity.Comment;
import com.minzheng.blog.entity.CommentInfo;
import com.minzheng.blog.entity.UserInfo;
import com.minzheng.blog.service.BlogInfoService;
import com.minzheng.blog.service.CommentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.minzheng.blog.service.RedisService;
import com.minzheng.blog.util.HTMLUtils;
import com.minzheng.blog.util.IpUtils;
import com.minzheng.blog.util.PageUtils;
import com.minzheng.blog.util.UserUtils;
import com.minzheng.blog.vo.*;
import eu.bitwalker.useragentutils.UserAgent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.minzheng.blog.constant.CommonConst.*;
import static com.minzheng.blog.constant.RedisPrefixConst.COMMENT_LIKE_COUNT;
import static com.minzheng.blog.enums.CommentTypeEnum.*;

/**
 * 评论服务
 *
 * @author yezhiqiu
 * @date 2021/07/31
 * @since 2020-05-18
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentDao, Comment> implements CommentService {
    @Resource
    private CommentDao commentDao;
    @Resource
    private ArticleDao articleDao;
    @Resource
    private TalkDao talkDao;
    @Resource
    private RedisService redisService;
    @Resource
    private UserInfoDao userInfoDao;
    @Resource
    private JavaMailSender javaMailSender;
    @Resource
    private BlogInfoService blogInfoService;
    @Resource
    private CommentInfoMapper commentInfoMapper;
    /**
     * 网站网址
     */
    @Value("${website.url}")
    private String websiteUrl;
    @Value("${spring.mail.username}")
    private String mailUsername;
    @Value("${spring.mail.nickname}")
    private String mailNickname;

    @Override
    public PageResult<CommentDTO> listComments(CommentVO commentVO) {
        // 查询评论量
        Integer commentCount = commentDao.selectCount(new LambdaQueryWrapper<Comment>()
                .eq(Objects.nonNull(commentVO.getTopicId()), Comment::getTopicId, commentVO.getTopicId())
                .eq(Comment::getType, commentVO.getType())
                .isNull(Comment::getParentId)
                .eq(Comment::getIsReview, TRUE));
        if (commentCount == 0) {
            return new PageResult<>();
        }
        // 分页查询评论数据
        List<CommentDTO> commentDTOList = commentDao.listComments(PageUtils.getLimitCurrent(), PageUtils.getSize(), commentVO);
        if (CollectionUtils.isEmpty(commentDTOList)) {
            return new PageResult<>();
        }
        // 查询redis的评论点赞数据
        Map<String, Object> likeCountMap = redisService.hGetAll(COMMENT_LIKE_COUNT);
        // 提取评论id集合
        List<Integer> commentIdList = commentDTOList.stream()
                .map(CommentDTO::getId)
                .collect(Collectors.toList());
        // 根据评论id集合查询回复数据
        List<ReplyDTO> replyDTOList = commentDao.listReplies(commentIdList);
        // 封装回复点赞量
        replyDTOList.forEach(item -> item.setLikeCount((Integer) likeCountMap.get(item.getId().toString())));
        // 根据评论id分组回复数据
        Map<Integer, List<ReplyDTO>> replyMap = replyDTOList.stream()
                .collect(Collectors.groupingBy(ReplyDTO::getParentId));
        // 根据评论id查询回复量
        Map<Integer, Integer> replyCountMap = commentDao.listReplyCountByCommentId(commentIdList)
                .stream().collect(Collectors.toMap(ReplyCountDTO::getCommentId, ReplyCountDTO::getReplyCount));
        // 封装评论数据
        commentDTOList.forEach(item -> {
            item.setLikeCount((Integer) likeCountMap.get(item.getId().toString()));
            item.setReplyDTOList(replyMap.get(item.getId()));
            item.setReplyCount(replyCountMap.get(item.getId()));
        });
        return new PageResult<>(commentDTOList, commentCount);
    }

    @Override
    public List<ReplyDTO> listRepliesByCommentId(Integer commentId) {
        // 转换页码查询评论下的回复
        List<ReplyDTO> replyDTOList = commentDao.listRepliesByCommentId(PageUtils.getLimitCurrent(), PageUtils.getSize(), commentId);
        // 查询redis的评论点赞数据
        Map<String, Object> likeCountMap = redisService.hGetAll(COMMENT_LIKE_COUNT);
        // 封装点赞数据
        replyDTOList.forEach(item -> item.setLikeCount((Integer) likeCountMap.get(item.getId().toString())));
        return replyDTOList;
    }

    @Override
    public void saveComment(CommentVO commentVO, HttpServletRequest request) {
        // 判断是否需要审核
        WebsiteConfigVO websiteConfig = blogInfoService.getWebsiteConfig();
        Integer isReview = websiteConfig.getIsCommentReview();
        // 过滤标签
        commentVO.setCommentContent(HTMLUtils.filter(commentVO.getCommentContent()));
        Integer userId = 0;
        String ipAddress = IpUtils.getIpAddress(request);
        String ipSource = IpUtils.getIpSource(ipAddress);
        UserAgent userAgent = IpUtils.getUserAgent(request);
        try {
            UserDetailDTO loginUser = UserUtils.getLoginUser();
            userId = loginUser.getUserInfoId();
        } catch (Exception e) {
            // 匿名用户评论
            VisitorDTO visitorInfo = UserUtils.getVisitorInfo();
            userId = visitorInfo.getUserId();
        }
        Comment comment = Comment.builder()
                .userId(userId)
                .replyUserId(commentVO.getReplyUserId())
                .topicId(commentVO.getTopicId())
                .commentContent(commentVO.getCommentContent())
                .parentId(commentVO.getParentId())
                .type(commentVO.getType())
                .isReview(isReview == TRUE ? FALSE : TRUE)
                .build();
        commentDao.insert(comment);
        CommentInfo commentInfo = CommentInfo.builder()
                .commentId(comment.getId())
                .ipAddress(ipAddress)
                .ipSource(ipSource)
                .browserName(userAgent.getBrowser().getName())
                .systemName(userAgent.getOperatingSystem().getName())
                .agent(userAgent.toString())
                .build();
        commentInfoMapper.insert(commentInfo);
        // 判断是否开启邮箱通知,通知用户
        if (userId != -1 && websiteConfig.getIsEmailNotice().equals(TRUE)) {
            CompletableFuture.runAsync(() -> notice(comment));
        }
    }

    @Override
    public void saveCommentLike(Integer commentId) {
        redisService.hIncr(COMMENT_LIKE_COUNT, commentId.toString(), 1L);
    }

    @Override
    public void updateCommentsReview(ReviewVO reviewVO) {
        // 修改评论审核状态
        List<Comment> commentList = reviewVO.getIdList().stream().map(item -> Comment.builder()
                        .id(item)
                        .isReview(reviewVO.getIsReview())
                        .build())
                .collect(Collectors.toList());
        this.updateBatchById(commentList);
    }

    @Override
    public PageResult<CommentBackDTO> listCommentBackDTO(ConditionVO condition) {
        // 统计后台评论量
        Integer count = commentDao.countCommentDTO(condition);
        if (count == 0) {
            return new PageResult<>();
        }
        // 查询后台评论集合
        List<CommentBackDTO> commentBackDTOList = commentDao.listCommentBackDTO(PageUtils.getLimitCurrent(), PageUtils.getSize(), condition);
        return new PageResult<>(commentBackDTOList, count);
    }

    /**
     * 通知评论用户
     *
     * @param comment 评论信息
     */
    public void notice(Comment comment) {
        // 查询回复用户邮箱号
        Integer userId = BLOGGER_ID;
        String id = Objects.nonNull(comment.getTopicId()) ? comment.getTopicId().toString() : "";
        if (Objects.nonNull(comment.getReplyUserId())) {
            userId = comment.getReplyUserId();
        } else {
            switch (Objects.requireNonNull(getCommentEnum(comment.getType()))) {
                case ARTICLE:
                    userId = articleDao.selectById(comment.getTopicId()).getUserId();
                    break;
                case TALK:
                    userId = talkDao.selectById(comment.getTopicId()).getUserId();
                    break;
                case LINK:
                    userId = BLOGGER_ID;
                    break;
                default:
                    break;
            }
        }
        // 发送消息
        EmailDTO emailDTO = new EmailDTO();
        if (comment.getIsReview().equals(TRUE)) {
            // 评论提醒
            String email = userInfoDao.selectById(userId).getEmail();
            emailDTO.setEmail(email);
            emailDTO.setSubject(getCommentNoticeSubject(comment.getType()));
            // 获取评论路径
            String url = websiteUrl + getCommentPath(comment.getType()) + id;
            emailDTO.setContent("您收到了一条新的" + getCommentNoticeName(comment.getType()) + "，请前往" + url + "\n页面查看");
            if (StringUtils.isNotBlank(emailDTO.getEmail())) {
                sendEmail(emailDTO);
            }
            String adminEmail = userInfoDao.selectById(BLOGGER_ID).getEmail();
            if (StringUtils.isNotBlank(adminEmail) && !adminEmail.equals(email)) {
                emailDTO.setEmail(adminEmail);
                sendEmail(emailDTO);
            }
        } else {
            // 管理员审核提醒
            String adminEmail = userInfoDao.selectById(BLOGGER_ID).getEmail();
            emailDTO.setEmail(adminEmail);
            emailDTO.setSubject("审核提醒");
            emailDTO.setContent("您收到了一条新的" + getCommentNoticeName(comment.getType()) + "，请前往后台管理页面审核");
            if (StringUtils.isNotBlank(emailDTO.getEmail())) {
                sendEmail(emailDTO);
            }
        }
    }

    private String getCommentNoticeSubject(Integer type) {
        switch (Objects.requireNonNull(getCommentEnum(type))) {
            case LINK:
                return "友链留言提醒";
            case TALK:
                return "说说评论提醒";
            case ARTICLE:
            default:
                return "评论提醒";
        }
    }

    private String getCommentNoticeName(Integer type) {
        switch (Objects.requireNonNull(getCommentEnum(type))) {
            case LINK:
                return "友链留言";
            case TALK:
                return "说说评论";
            case ARTICLE:
            default:
                return "评论";
        }
    }

    private void sendEmail(EmailDTO emailDTO) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailNickname + '<' + mailUsername + '>');
        message.setTo(emailDTO.getEmail());
        message.setSubject(emailDTO.getSubject());
        message.setText(emailDTO.getContent());
        javaMailSender.send(message);
    }

}
