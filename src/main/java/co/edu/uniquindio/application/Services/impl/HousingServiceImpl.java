package co.edu.uniquindio.application.Services.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.HousingService;
import co.edu.uniquindio.application.mappers.HousingMapper;

import java.time.Instant;
import java.time.LocalDate;

@Service
public class HousingServiceImpl implements HousingService {

    private final HousingRepository repo;
    private final HousingMapper mapper;

    @Value("${spring.pageable.default-page-size}")
    private int PAGE_SIZE;

    @Value("${spring.pageable.index-default}")
    private int FIRST_PAGE;

    private static final LocalDate CHECK_IN_DEFAULT = LocalDate.now();

    private static final LocalDate CHECK_OUT_DEFAULT = LocalDate.now().plusDays(1);


    public HousingServiceImpl(HousingRepository repo, HousingMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Override
    public EntityCreatedResponse create(CreateHousingRequest request) {
        Housing housing = mapper.toHousing(request);
        repo.save(housing);
        return new EntityCreatedResponse("Housing created succesfully", Instant.now());
    }

    @Override
    public Page<SummaryHousingResponse> searchHousingsByFilters(String city, LocalDate checkIn, LocalDate checkOut,
            Integer totalGuests, Integer indexPage) {

       return null;
    }

    @Override
    public Housing findById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'findById'");
    }

    @Override
    public void deleteById(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteById'");
    }

}