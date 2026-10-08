package com.bankapp.partner.service;

import com.bankapp.partner.converter.PartnerDtoConverter;
import com.bankapp.partner.converter.PartnerRequestConverter;
import com.bankapp.partner.dto.PartnerDto;
import com.bankapp.partner.dto.PartnerRequest;
import com.bankapp.partner.entity.Partner;
import com.bankapp.partner.exception.PartnerNotFoundException;
import com.bankapp.partner.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private final PartnerRequestConverter partnerRequestConverter;
    private final PartnerDtoConverter partnerDtoConverter;

    public List<PartnerDto> getAllPartners() {
        return partnerRepository.findAll()
                .stream()
                .map(partnerDtoConverter::convert)
                .collect(Collectors.toList());
    }

    public Page<PartnerDto> getPartnersPaginated(
            int page,
            int size,
            String sortBy,
            String direction) {

        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        return partnerRepository.findAll(pageable)
                .map(partnerDtoConverter::convert);
    }

    public PartnerDto getPartnerById(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() ->
                        new PartnerNotFoundException("Partner with id " + id + " not found"));

        return partnerDtoConverter.convert(partner);
    }

    public PartnerDto savePartner(PartnerRequest request) {
        Partner partner = partnerRequestConverter.convert(request);
        Partner savedPartner = partnerRepository.save(partner);

        return partnerDtoConverter.convert(savedPartner);
    }

    public void deletePartner(Long id) {
        Partner partner = partnerRepository.findById(id)
                .orElseThrow(() ->
                        new PartnerNotFoundException("Partner with id " + id + " not found"));

        partnerRepository.delete(partner);
    }
}
