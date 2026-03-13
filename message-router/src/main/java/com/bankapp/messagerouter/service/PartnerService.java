package com.bankapp.messagerouter.service;

import com.bankapp.messagerouter.entity.Partner;
import com.bankapp.messagerouter.error.PartnerNotFoundException;
import com.bankapp.messagerouter.repository.PartnerRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public PartnerService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public List<Partner> getAllPartners() {
        return partnerRepository.findAll();
    }

    public Page<Partner> getPartnersPaginated(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return partnerRepository.findAll(pageable);
    }

    public Partner getPartnerById(Long id) {
        return partnerRepository.findById(id)
                .orElseThrow(() -> new PartnerNotFoundException("Partner with ID " + id + " not found"));
    }

    public Partner savePartner(Partner partner) {
        return partnerRepository.save(partner);
    }

    public void deletePartner(Long id) {
        if (partnerRepository.existsById(id)) {
            partnerRepository.deleteById(id);
        } else {
            throw new PartnerNotFoundException("Partner with ID " + id + " not found");
        }
    }
}