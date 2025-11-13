package co.edu.uniquindio.application.Services.impl;

import org.hibernate.ObjectNotFoundException;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import co.edu.uniquindio.application.Dtos.Generic.EntityChangedResponse;
import co.edu.uniquindio.application.Dtos.Generic.EntityCreatedResponse;
import co.edu.uniquindio.application.Dtos.Housing.Requests.CreateOrEditHousingRequest;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingMetricsResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Exception.HousingUndeletedException;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Models.enums.ServicesEnum;
import co.edu.uniquindio.application.Repositories.FavoriteRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.BookingService;
import co.edu.uniquindio.application.Services.HousingService;
import co.edu.uniquindio.application.Services.UserService;
import co.edu.uniquindio.application.mappers.HousingMapper;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class HousingServiceImpl implements HousingService {

    private final HousingRepository housingRepository;
    private final HousingMapper housingMapper;
    private final BookingService bookingService;
    private final UserService userService;
    private final FavoriteRepository favoriteRepository;

    private static final int PAGE_SIZE = 10;

    @Override
    public EntityCreatedResponse create(Long hostId, CreateOrEditHousingRequest request) {
        Housing housing = housingMapper.toHousing(request);
        housing.setHostId(hostId);
        updatePrincipalImage(housing, request.imagesUrls());
        housingRepository.save(housing);
        return new EntityCreatedResponse("Housing created successfully", Instant.now());
    }

    @Override
    public EntityChangedResponse delete(Long housingId, Long hostId) {
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }

        if (bookingService.existsFutureBookingsForHousing(housingId)) {
            throw new HousingUndeletedException("Housing with id: " + housingId + "and hostId: " + hostId + " has pending bookings");
        }
        housingRepository.softDeleteByIdAndHostId(housingId, hostId);
        return new EntityChangedResponse("Housing deleted successfully", Instant.now());
    }

    @Override
    public EntityChangedResponse edit(Long housingId, Long hostId, CreateOrEditHousingRequest request) {
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }
        Housing housing = housingRepository.findById(housingId)
            .orElseThrow(() -> new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class));

        if (Objects.equals(housing.getState(), Housing.STATE_DELETED)) {
            throw new IllegalStateException("Cannot edit a deleted housing");
        }

        housingMapper.updateHousingFromRequest(request, housing);
        updatePrincipalImage(housing, request.imagesUrls());
        housingRepository.save(housing);
        return new EntityChangedResponse("Housing updated succesfully", Instant.now());
    }

     @Override
     @Transactional(readOnly = true)
     public Page<SummaryHousingResponse> getHousingsByFilters(String city, LocalDate checkIn, LocalDate checkOut,
                                                              Double minPrice, Double maxPrice, Integer totalGuests,
                                                              List<ServicesEnum> services, Integer indexPage) {

         LocalDate dateIn = checkIn;
         LocalDate dateOut = checkOut;
         Double min = (minPrice != null && minPrice >= 0) ? minPrice : null;
         Double max = (maxPrice != null && maxPrice >= 0) ? maxPrice : null;
         Integer index = (indexPage != null && indexPage >= 0) ? indexPage : 0;
         Integer guests = (totalGuests != null && totalGuests > 0) ? totalGuests : null;
        List<ServicesEnum> serviceFilters = services != null ? new ArrayList<>(services) : new ArrayList<>();

         Pageable pageable = PageRequest.of(index, PAGE_SIZE);

         Page<Housing> housings;
         if (serviceFilters.isEmpty()) {
             housings = housingRepository.findHousingsByFilters(
                     city,
                     dateIn,
                     dateOut,
                     min,
                     max,
                     guests,
                     pageable
             );
         } else {
             housings = housingRepository.findHousingsByFilters(
                     city,
                     dateIn,
                     dateOut,
                     min,
                     max,
                     guests,
                     serviceFilters,
                     serviceFilters.size(),
                     pageable
             );
         }

         return housings.map(housingMapper::toSummaryHousingResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SummaryHousingResponse> getHousingsByHost(Long hostId, Integer page, Integer size) {
        if (hostId == null || hostId <= 0) {
            throw new IllegalArgumentException("The hostId must be positive");
        }
        
        Integer pageIndex = (page != null && page >= 0) ? page : 0;
        Integer pageSize = (size != null && size > 0) ? size : 10;
        
        Pageable pageable = PageRequest.of(pageIndex, pageSize);
        Page<Housing> housings = housingRepository.findByHostId(hostId, pageable);
        
        return housings.map(housingMapper::toSummaryHousingResponse);
    }

    @Override
@Transactional(readOnly = true)
    public HousingResponse getHousingDetail(Long housingId) {

    Housing housing = housingRepository.findById(housingId)
            .orElseThrow(() -> new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class));

    if (Housing.STATE_DELETED.equals(housing.getState())) {
        throw new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class);
    }

    HousingResponse response = housingMapper.toHousingResponse(housing);

    User user = userService.findById(housing.getHostId());
    response.setHostName(user.getName());

    return response;
}

    @Override
     @Transactional(readOnly = true)
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

        return housingRepository.existsByIdAndHostIdAndStateNot(housingId, hostId, Housing.STATE_DELETED);
    }

    @Override
    public HousingMetricsResponse getHousingMetrics(Long hostId, Long housingId, LocalDate startDate, LocalDate endDate) {
        Housing housing = housingRepository.findById(housingId)
            .orElseThrow(() -> new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class));

        if (!Objects.equals(housing.getHostId(), hostId)) {
            throw new IllegalArgumentException("The housing does not belong to the authenticated host");
        }

        if (Housing.STATE_DELETED.equals(housing.getState())) {
            throw new IllegalStateException("Housing is deleted");
        }

        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final");
        }

        Long bookingsCount = housingRepository.countBookingsForHousing(housingId, startDate, endDate);
        Double ratingAverage = housingRepository.calculateAverageRating(housingId, startDate, endDate);
        Long favoritesCount = favoriteRepository.countByHousingId(housingId);

        return new HousingMetricsResponse(
            housingId,
            bookingsCount,
            ratingAverage != null ? ratingAverage : 0.0,
            favoritesCount
        );
    }

    private void updatePrincipalImage(Housing housing, List<String> imagesUrls) {
        if (imagesUrls == null || imagesUrls.isEmpty()) {
            housing.setPrincipalImage(null);
            housing.setImages(new ArrayList<>());
            return;
        }

        List<String> sanitized = imagesUrls.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(url -> !url.isEmpty())
            .collect(Collectors.toList());

        housing.setImages(new ArrayList<>(sanitized));
        housing.setPrincipalImage(sanitized.isEmpty() ? null : sanitized.get(0));
    }
}


