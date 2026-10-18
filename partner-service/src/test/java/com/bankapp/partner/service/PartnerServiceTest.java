package com.bankapp.partner.service;

import com.bankapp.partner.converter.PartnerDtoConverter;
import com.bankapp.partner.converter.PartnerRequestConverter;
import com.bankapp.partner.dto.PartnerDirection;
import com.bankapp.partner.dto.PartnerDto;
import com.bankapp.partner.dto.PartnerRequest;
import com.bankapp.partner.dto.PartnerType;
import com.bankapp.partner.dto.ProcessedFlowType;
import com.bankapp.partner.entity.Partner;
import com.bankapp.partner.exception.PartnerNotFoundException;
import com.bankapp.partner.repository.PartnerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartnerServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @Mock
    private PartnerRequestConverter partnerRequestConverter;

    @Mock
    private PartnerDtoConverter partnerDtoConverter;

    @InjectMocks
    private PartnerService partnerService;

    private Partner createPartner(Long id) {
        return Partner.builder()
                .id(id)
                .alias("bank-adapter")
                .type(PartnerType.MESSAGE)
                .direction(PartnerDirection.OUTBOUND)
                .application("bankapp")
                .processedFlowType(ProcessedFlowType.MESSAGE)
                .description("Bank messaging partner")
                .build();
    }

    private PartnerDto createDto(Long id) {
        return PartnerDto.builder()
                .id(id)
                .alias("bank-adapter")
                .type(PartnerType.MESSAGE)
                .direction(PartnerDirection.OUTBOUND)
                .application("bankapp")
                .processedFlowType(ProcessedFlowType.MESSAGE)
                .description("Bank messaging partner")
                .build();
    }

    private PartnerRequest createRequest() {
        return PartnerRequest.builder()
                .alias("bank-adapter")
                .type(PartnerType.MESSAGE)
                .direction(PartnerDirection.OUTBOUND)
                .application("bankapp")
                .processedFlowType(ProcessedFlowType.MESSAGE)
                .description("Bank messaging partner")
                .build();
    }

    @Test
    void shouldReturnAllPartners() {
        Partner partner = createPartner(1L);
        PartnerDto dto = createDto(1L);

        when(partnerRepository.findAll()).thenReturn(List.of(partner));
        when(partnerDtoConverter.convert(partner)).thenReturn(dto);

        List<PartnerDto> result = partnerService.getAllPartners();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("bank-adapter", result.get(0).getAlias());
        verify(partnerRepository).findAll();
        verify(partnerDtoConverter).convert(partner);
    }

    @Test
    void shouldReturnEmptyListWhenNoPartnersExist() {
        when(partnerRepository.findAll()).thenReturn(List.of());

        List<PartnerDto> result = partnerService.getAllPartners();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verifyNoInteractions(partnerDtoConverter);
    }

    @Test
    void shouldReturnPaginatedPartnersSortedAscending() {
        Partner partner = createPartner(1L);
        PartnerDto dto = createDto(1L);

        when(partnerRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(partner)));
        when(partnerDtoConverter.convert(partner)).thenReturn(dto);

        Page<PartnerDto> result =
                partnerService.getPartnersPaginated(0, 10, "alias", "asc");

        assertEquals(1, result.getTotalElements());
        assertEquals("bank-adapter", result.getContent().get(0).getAlias());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(partnerRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.Direction.ASC, pageable.getSort().getOrderFor("alias").getDirection());
    }

    @Test
    void shouldSupportDescendingPagination() {
        when(partnerRepository.findAll(any(Pageable.class)))
                .thenReturn(Page.empty());

        Page<PartnerDto> result =
                partnerService.getPartnersPaginated(1, 5, "alias", "desc");

        assertTrue(result.isEmpty());

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(partnerRepository).findAll(captor.capture());

        Pageable pageable = captor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(Sort.Direction.DESC, pageable.getSort().getOrderFor("alias").getDirection());
    }

    @Test
    void shouldReturnPartnerById() {
        Partner partner = createPartner(1L);
        PartnerDto dto = createDto(1L);

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));
        when(partnerDtoConverter.convert(partner)).thenReturn(dto);

        PartnerDto result = partnerService.getPartnerById(1L);

        assertEquals(1L, result.getId());
        assertEquals("bank-adapter", result.getAlias());
        verify(partnerRepository).findById(1L);
    }

    @Test
    void shouldThrowWhenPartnerIdDoesNotExist() {
        when(partnerRepository.findById(99L)).thenReturn(Optional.empty());

        PartnerNotFoundException exception = assertThrows(
                PartnerNotFoundException.class,
                () -> partnerService.getPartnerById(99L)
        );

        assertEquals("Partner with id 99 not found", exception.getMessage());
        verifyNoInteractions(partnerDtoConverter);
    }

    @Test
    void shouldSavePartnerFromRequest() {
        PartnerRequest request = createRequest();
        Partner converted = createPartner(null);
        Partner saved = createPartner(1L);
        PartnerDto dto = createDto(1L);

        when(partnerRequestConverter.convert(request)).thenReturn(converted);
        when(partnerRepository.save(converted)).thenReturn(saved);
        when(partnerDtoConverter.convert(saved)).thenReturn(dto);

        PartnerDto result = partnerService.savePartner(request);

        assertEquals(1L, result.getId());
        assertEquals("bank-adapter", result.getAlias());
        verify(partnerRequestConverter).convert(request);
        verify(partnerRepository).save(converted);
        verify(partnerDtoConverter).convert(saved);
    }

    @Test
    void shouldDeleteExistingPartner() {
        Partner partner = createPartner(1L);
        when(partnerRepository.findById(1L)).thenReturn(Optional.of(partner));

        partnerService.deletePartner(1L);

        verify(partnerRepository).findById(1L);
        verify(partnerRepository).delete(partner);
    }

    @Test
    void shouldThrowWhenDeletingNonexistentPartner() {
        when(partnerRepository.findById(99L)).thenReturn(Optional.empty());

        PartnerNotFoundException exception = assertThrows(
                PartnerNotFoundException.class,
                () -> partnerService.deletePartner(99L)
        );

        assertEquals("Partner with id 99 not found", exception.getMessage());
        verify(partnerRepository, never()).delete(any(Partner.class));
    }
}
