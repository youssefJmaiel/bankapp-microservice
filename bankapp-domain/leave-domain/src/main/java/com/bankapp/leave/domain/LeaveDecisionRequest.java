package com.bankapp.leave.domain;

import javax.validation.constraints.NotNull;

public class LeaveDecisionRequest {

    public enum Decision {
        APPROVED,
        REJECTED
    }

    @NotNull(message = "La décision est obligatoire")
    private Decision decision;

    public LeaveDecisionRequest() {
    }

    public Decision getDecision() {
        return decision;
    }

    public void setDecision(Decision decision) {
        this.decision = decision;
    }
}
