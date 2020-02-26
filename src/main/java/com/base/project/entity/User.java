package com.base.project.entity;

import com.baomidou.mybatisplus.annotations.TableId;
import com.baomidou.mybatisplus.annotations.TableName;
import com.baomidou.mybatisplus.enums.IdType;
import lombok.Data;

@Data
@TableName("T_userInfo")
public class User {
    private String userName;
    private String passWords;
    private String pickName;
    @TableId(type = IdType.AUTO)
    private String id;
}
