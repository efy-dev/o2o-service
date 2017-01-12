package com.efeiyi.ec.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

/**
 * Created by Administrator on 2015/5/28.
 */
@Entity
@Table(name = "organization_consumer")
public class Consumer extends BigUser {
    private BigDecimal deposit;
    private Integer score;
    private BigDecimal balance;
    //private List<ProjectContent> projectContents;

    public Consumer() {
        setAccountExpired(false);
        setAccountLocked(false);
        setCredentialsExpired(false);
        setEnabled(true);
        setStatus("1");
    }

    @Column(name = "deposit")
    public BigDecimal getDeposit() {
        return deposit;
    }

    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit;
    }

    @Column(name = "score")
    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }


    @Column(name = "balance")
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    /* @ManyToMany(fetch = FetchType.LAZY,mappedBy = "consumer")
     public List<ProjectContent> getProjectContents() {
         return projectContents;
     }

     public void setProjectContents(List<ProjectContent> projectContents) {
         this.projectContents = projectContents;
     }*/
    @Override
    public String toString() {
        return "Consumer{id = " + id + "}";
    }
}
