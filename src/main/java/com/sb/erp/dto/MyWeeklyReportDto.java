package com.sb.erp.dto;

import java.util.List;

import lombok.Data;

@Data
public class MyWeeklyReportDto {
    private Integer empId;
    private String empName;

    private int totalTask;
    private int doneTaskCount;
    private int notDoneTaskCount;
    private int completedThisWeek;
    private int delayTaskCount;
    private int progressRate;
    private double avgTaskDays;
    private int avgDelayDays;

    private List<String> delayedTaskNames;
}
