package rs.dao;

import sqlg2.RowType;

@RowType
public final class EmpRow {

    private int empNo;
    private String empName;
    private String job;
    private Integer mgr;
    private java.sql.Timestamp hireDate;

    public EmpRow() {
    }

    public EmpRow(int empNo, String empName, String job, Integer mgr, java.sql.Timestamp hireDate) {
        this.empNo = empNo;
        this.empName = empName;
        this.job = job;
        this.mgr = mgr;
        this.hireDate = hireDate;
    }

    public int getEmpNo() {
        return empNo;
    }

    public String getEmpName() {
        return empName;
    }

    public String getJob() {
        return job;
    }

    public Integer getMgr() {
        return mgr;
    }

    public java.sql.Timestamp getHireDate() {
        return hireDate;
    }
}
