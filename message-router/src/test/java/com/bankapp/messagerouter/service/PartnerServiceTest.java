package com.bankapp.messagerouter.service;

import com.bankapp.messagerouter.entity.Partner;
import com.bankapp.messagerouter.repository.PartnerRepository;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class PartnerServiceTest {

    private final PartnerRepository partnerRepository = mock(PartnerRepository.class);
    private final PartnerService partnerService = new PartnerService(partnerRepository);

    @Test
    public void testGetAllPartners() {
        Partner partner = new Partner();
        partner.setAlias("Test Partner");

        when(partnerRepository.findAll()).thenReturn(Collections.singletonList(partner));

        List<Partner> partners = partnerService.getAllPartners();

        assertEquals(1, partners.size());
        assertEquals("Test Partner", partners.get(0).getAlias());
        verify(partnerRepository, times(1)).findAll();
    }

    @Test
    public void testSavePartner() {
        Partner partner = new Partner();
        partner.setAlias("New Partner");

        when(partnerRepository.save(any(Partner.class))).thenReturn(partner);

        Partner savedPartner = partnerService.savePartner(partner);

        assertEquals("New Partner", savedPartner.getAlias());
        verify(partnerRepository, times(1)).save(partner);
    }

    @Test
    void deletePartner_shouldCallDeleteMethod_whenPartnerExists() {
        Long partnerId = 1L;

        when(partnerRepository.existsById(partnerId)).thenReturn(true);

        partnerService.deletePartner(partnerId);

        verify(partnerRepository, times(1)).deleteById(partnerId);
    }

    @Test
    void deletePartner_shouldThrowException_whenPartnerDoesNotExist() {
        Long partnerId = 1L;

        when(partnerRepository.existsById(partnerId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> partnerService.deletePartner(partnerId));

        // Vérifie que le message contient l'ID et "not found"
        assertTrue(exception.getMessage().contains("Partner with ID " + partnerId + " not found"));

        verify(partnerRepository, never()).deleteById(partnerId);
    }
}