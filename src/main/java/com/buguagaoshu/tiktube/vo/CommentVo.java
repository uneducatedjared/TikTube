package com.buguagaoshu.tiktube.vo;

import com.buguagaoshu.tiktube.valid.ListValue;
import jakarta.validation.constraints.Size;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * @author Pu Zhiwei {@literal puzhiweipuzhiwei@foxmail.com}
 * create          2022-08-25 15:59
 */
@Data
public class CommentVo {
    @NotNull(message = "目标帖子ID不能为空")
    private Long articleId;

    /**
     *
     */
    private Long userId;

    /**
     * 评论
     */
    @NotBlank(message = "评论正文不能为空！")
    @Size(max = 10001, message = "评论正文不能超过 10000 字")
    private String comment;

    /**
     * 父级评论
     */
    private Long parentCommentId;

    /**
     * 评论对象
     */
    private Long parentUserId;


    private Long commentId;

    /**
     * 【1 一级评论  2 二级评论】
     */
    @ListValue(value = {1, 2})
    private Integer type;
}
