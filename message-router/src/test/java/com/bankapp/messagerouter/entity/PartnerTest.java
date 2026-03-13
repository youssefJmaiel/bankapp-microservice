package com.bankapp.messagerouter.entity;


import org.junit.jupiter.api.Test;

import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PartnerTest {

    private final Validator validator;

    public PartnerTest() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        this.validator = factory.getValidator();
    }

    @Test
    public void testValidPartner() {
        Partner partner = new Partner();
        partner.setAlias("PartnerAlias");
        partner.setType(PartnerType.MESSAGE);
        partner.setDirection(PartnerDirection.INBOUND);
        partner.setProcessedFlowType(ProcessedFlowType.MESSAGE);
        partner.setDescription("This is a test partner");

        var violations = validator.validate(partner);
        assertTrue(violations.isEmpty(), "Partner should be valid");
    }


    @Test
    void testInvalidPartner() {
        Partner partner = new Partner();
        partner.setAlias(null); // Alias est obligatoire
        partner.setType(PartnerType.MESSAGE);
        partner.setDirection(PartnerDirection.INBOUND);
        partner.setProcessedFlowType(ProcessedFlowType.MESSAGE);
        partner.setDescription("Valid Description");

        assertFalse(partner.isValid(), "Partner should be invalid");
    }

}

