package com.ssafy.omg.domain.game.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LoanProduct implements Comparable<LoanProduct> {
    int interestRate;
    int loanPrincipal;
    int loanInterest;

    // loanInterest 기준 내림차순 정렬
    @Override
    public int compareTo(LoanProduct o) {
        return Integer.compare(o.getInterestRate(), this.getInterestRate());
    }
}
