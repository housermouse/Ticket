package com.base.project.entity;


import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Performance {
    String name;
    String performTime;
    String address;
    String ticketValue;
    String picPath;
    ArrayList<Ticket> tickets;

}
