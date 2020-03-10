package com.base.project.entity;

import com.baomidou.mybatisplus.annotations.TableName;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@TableName()
@Data
public class Ticket {
    String id;
    String number;
    String price;
    List<String> prices;
    String priceName;
}
