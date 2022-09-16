package com.minzheng.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * @author caiguoyu
 * @date 2022/9/16 
 */
/**
    * 评论信息
    */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "tb_comment_info")
public class CommentInfo {
    @TableId(value = "id", type = IdType.INPUT)
    private Integer id;

    /**
     * 评论id

     */
    @TableField(value = "comment_id")
    private Integer commentId;

    @TableField(value = "ip_address")
    private String ipAddress;

    @TableField(value = "ip_source")
    private String ipSource;

    @TableField(value = "browser_name")
    private String browserName;

    @TableField(value = "system_name")
    private String systemName;

    @TableField(value = "agent")
    private String agent;

    @TableField(value = "created_time")
    private Date createdTime;

    public static final String COL_ID = "id";

    public static final String COL_COMMENT_ID = "comment_id";

    public static final String COL_IP_ADDRESS = "ip_address";

    public static final String COL_IP_SOURCE = "ip_source";

    public static final String COL_BROWSER_NAME = "browser_name";

    public static final String COL_SYSTEM_NAME = "system_name";

    public static final String COL_AGENT = "agent";

    public static final String COL_CREATED_TIME = "created_time";
}