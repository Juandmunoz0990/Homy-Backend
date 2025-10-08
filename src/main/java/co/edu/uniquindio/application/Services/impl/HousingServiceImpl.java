package co.edu.uniquindio.application.Services.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import co.edu.uniquindio.application.Dtos.Generic.EntityChangedResponse;
import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.HousingService;
import co.edu.uniquindio.application.mappers.HousingMapper;

import java.time.Instant;
import java.time.LocalDate;


public class HousingServiceImpl implements HousingService {

    private final HousingRepository housingRepository;
    private final HousingMapper housingMapper;

    @Value("${spring.pageable.default-page-size}")
    private int PAGE_SIZE;

    @Value("${spring.pageable.index-default}")
    private int FIRST_PAGE;

    private static final LocalDate CHECK_IN_DEFAULT = LocalDate.now();
    private static final LocalDate CHECK_OUT_DEFAULT = LocalDate.now().plusDays(1);

    public HousingServiceImpl(HousingRepository housingRepository, HousingMapper housingMapper) {
        this.housingRepository = housingRepository;
        this.housingMapper = housingMapper;
    }

    @Override
    public EntityCreatedResponse create(Long hostId, CreateOrEditHousingRequest request) {
        Housing housing = housingMapper.toHousing(request);
        housing.setHostId(hostId);
        housingRepository.save(housing);
        return new EntityCreatedResponse("Housing created successfully", Instant.now());
    }

    @Override
    public EntityChangedResponse delete(Long housingId, Long hostId) {
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }
        housingRepository.deleteById(housingId);
        return new EntityChangedResponse("Housing deleted successfully", Instant.now());
    }

    @Override
    public EntityChangedResponse edit(Long housingId, Long hostId, CreateOrEditHousingRequest request) {
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }
        Housing housing = housingMapper.toHousing(request);
        housing.setId(housingId);
        housingRepository.save(housing);
        return new EntityChangedResponse("Housing updated succesfully", Instant.now());
    }

    @Override
    public Page<SummaryHousingResponse> getHousingsByFilters(String city, LocalDate checkIn, LocalDate checkOut,
                                                             Integer minPrice, Integer maxPrice, Integer indexPage) {

        LocalDate dateIn = (checkIn != null) ? checkIn : CHECK_IN_DEFAULT;
        LocalDate dateOut = (checkOut != null) ? checkOut : CHECK_OUT_DEFAULT;
        Integer min = (minPrice != null && minPrice >= 0) ? minPrice : 0;
        Integer max = (maxPrice != null && maxPrice >= 0 && maxPrice != minPrice) ? maxPrice : 50; 
        Integer index = (indexPage != null && indexPage >= 0) ? indexPage : FIRST_PAGE;

        Pageable pageable = PageRequest.of(index, PAGE_SIZE);

        Page<Housing> housings = housingRepository.findHousingsByFilters(city, dateIn, dateOut, min, max, pageable);

        return housings.map(housingMapper::toSummaryHousingResponse);
    }

    @Override
    public Housing findById(Long id) {
        return housingRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Housing with id: " + id + " not found", Housing.class));
    }

    @Override
    public Boolean existsHousing(Long housingId, Long hostId) {
        if (housingId == null || housingId <= 0) {
            throw new IllegalArgumentException("The housingId must be positive");
        }

        if (hostId == null || hostId <= 0) {
            throw new IllegalArgumentException("The hostId must be positive");
        }

        return housingRepository.existsByIdAndHostId(housingId, hostId);
    }
}


