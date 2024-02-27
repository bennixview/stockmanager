package com.bewi.stockmanager.model;

import java.util.Date;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class Employee {


    private Date startDate;
    private Integer id;
    private String position;
    private String name;
    private Double salary;
    private String office;
    private Integer extn;

}
