package com.base.project.entity;

import lombok.Data;

import java.util.ArrayList;

@Data
public class Ticket {
    String number;
    ArrayList<String> prices = new ArrayList<String>();
    String priceName;
}
