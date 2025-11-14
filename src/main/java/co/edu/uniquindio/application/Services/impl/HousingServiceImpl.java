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
import co.edu.uniquindio.application.Dtos.Housing.Responses.AvailabilityCalendarResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingMetricsResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.HousingResponse;
import co.edu.uniquindio.application.Dtos.Housing.Responses.SummaryHousingResponse;
import co.edu.uniquindio.application.Exception.HousingUndeletedException;
import co.edu.uniquindio.application.Models.Housing;
import co.edu.uniquindio.application.Models.User;
import co.edu.uniquindio.application.Models.Comment;
import co.edu.uniquindio.application.Repositories.BookingRepository;
import co.edu.uniquindio.application.Repositories.CommentRepository;
import co.edu.uniquindio.application.Repositories.HousingRepository;
import co.edu.uniquindio.application.Services.BookingService;
import co.edu.uniquindio.application.Services.HousingService;
import co.edu.uniquindio.application.Services.UserService;
import co.edu.uniquindio.application.mappers.HousingMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class HousingServiceImpl implements HousingService {

    private final HousingRepository housingRepository;
    private final HousingMapper housingMapper;
    private final BookingService bookingService;
    private final UserService userService;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    private static final LocalDate CHECK_IN_DEFAULT = LocalDate.now();
    private static final LocalDate CHECK_OUT_DEFAULT = LocalDate.now().plusDays(1);

    @Override
    public EntityCreatedResponse create(Long hostId, CreateOrEditHousingRequest request) {
        // Validar imágenes: mínimo 1, máximo 10
        validateImages(request.imagesUrls());
        
        Housing housing = housingMapper.toHousing(request);
        housing.setHostId(hostId);
        housingRepository.save(housing);
        return new EntityCreatedResponse("Housing created successfully", Instant.now());
    }
    
    /**
     * Valida que haya entre 1 y 10 imágenes, y que haya una imagen principal
     */
    private void validateImages(List<String> imagesUrls) {
        // Las imágenes son opcionales, solo validamos el máximo
        if (imagesUrls != null && imagesUrls.size() > 10) {
            throw new IllegalArgumentException("No se pueden subir más de 10 imágenes");
        }
    }

    @Override
    public EntityChangedResponse delete(Long housingId, Long hostId) {
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }

        if (bookingService.existsFutureBookingsForHousing(housingId)) {
            throw new HousingUndeletedException("Housing with id: " + housingId + "and hostId: " + hostId + " has pending bookings");
        }
        housingRepository.softDeleteByIdAndHostId(housingId, hostId);;
        return new EntityChangedResponse("Housing deleted successfully", Instant.now());
    }

    @Override
    public EntityChangedResponse edit(Long housingId, Long hostId, CreateOrEditHousingRequest request) {
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }
        
        // Validar imágenes: máximo 10
        validateImages(request.imagesUrls());
        
        // Obtener el housing existente para preservar campos que no deben cambiar
        Housing existingHousing = findById(housingId);
        
        // Preservar valores críticos antes de actualizar
        String preservedState = existingHousing.getState();
        if (preservedState == null || preservedState.trim().isEmpty()) {
            preservedState = Housing.STATE_ACTIVE;
        }
        Long preservedHostId = existingHousing.getHostId();
        if (preservedHostId == null) {
            preservedHostId = hostId; // Asegurar que hostId no sea null
        }
        Double preservedAverageRating = existingHousing.getAverageRating();
        
        // Actualizar solo los campos que vienen en el request
        existingHousing.setTitle(request.title());
        existingHousing.setDescription(request.description());
        existingHousing.setCity(request.city());
        existingHousing.setAddress(request.address());
        existingHousing.setLatitude(request.latitude());
        existingHousing.setLength(request.length());
        existingHousing.setNightPrice(request.pricePerNight());
        existingHousing.setMaxCapacity(request.maxCapacity());
        existingHousing.setServices(request.services());
        existingHousing.setImages(request.imagesUrls());
        
        // Actualizar principalImage si hay imágenes
        if (request.imagesUrls() != null && !request.imagesUrls().isEmpty()) {
            existingHousing.setPrincipalImage(request.imagesUrls().get(0));
        }
        
        // Restaurar campos críticos que NO deben cambiar (SIEMPRE)
        existingHousing.setState(preservedState);
        existingHousing.setHostId(preservedHostId);
        if (preservedAverageRating != null) {
            existingHousing.setAverageRating(preservedAverageRating);
        }
        
        // Asegurar que el ID no cambie
        existingHousing.setId(housingId);
        
        log.debug("Updating housing {} for host {} with state: {}", housingId, hostId, preservedState);
        
        housingRepository.save(existingHousing);
        return new EntityChangedResponse("Housing updated succesfully", Instant.now());
    }

     @Override
     @Transactional(readOnly = true)
     public Page<SummaryHousingResponse> getAllActiveHousings(Integer page, Integer size) {
         Integer pageNum = (page != null && page >= 0) ? page : 0;
         Integer pageSize = (size != null && size > 0) ? size : 20;
         Pageable pageable = PageRequest.of(pageNum, pageSize);
         
         log.info("Getting all active housings - page: {}, size: {}", pageNum, pageSize);
         
         // Primero verificar cuántas propiedades hay en total (sin filtro de estado)
         Page<Housing> allHousings = housingRepository.findAllWithoutStateFilter(pageable);
         log.info("Total housings in DB (no filter): {}", allHousings.getTotalElements());
         
         // Listar estados de todas las propiedades para debugging
         if (allHousings.getTotalElements() > 0) {
             log.info("Sample housing states:");
             allHousings.getContent().stream()
                 .limit(5)
                 .forEach(h -> log.info("  Housing ID {}: state='{}', title='{}'", 
                     h.getId(), h.getState(), h.getTitle()));
         }
         
         // Ahora buscar solo las activas
         Page<Housing> housings = housingRepository.findAllActive(pageable);
         log.info("Found {} active housings (total: {})", housings.getNumberOfElements(), housings.getTotalElements());
         
         // Si no hay activas pero sí hay propiedades, loggear un warning
         if (housings.getTotalElements() == 0 && allHousings.getTotalElements() > 0) {
             log.warn("⚠️ WARNING: There are {} housings in DB but 0 are active! Check their state values.", 
                 allHousings.getTotalElements());
         }
         
         return housings.map(housingMapper::toSummaryHousingResponse);
     }

     @Override
     @Transactional(readOnly = true)
     public Page<SummaryHousingResponse> getHousingsByFilters(String city, LocalDate checkIn, LocalDate checkOut,
                                                              Double minPrice, Double maxPrice, Integer indexPage) {

         LocalDate dateIn = (checkIn != null) ? checkIn : CHECK_IN_DEFAULT;
         LocalDate dateOut = (checkOut != null) ? checkOut : CHECK_OUT_DEFAULT;
         Double min = (minPrice != null && minPrice >= 0) ? minPrice : 0;
         Double max = (maxPrice != null && maxPrice >= 0 && maxPrice != minPrice) ? maxPrice : 50; 
         Integer index = (indexPage != null && indexPage >= 0) ? indexPage : 0;

         Pageable pageable = PageRequest.of(index, 20);

         Page<Housing> housings = housingRepository.findHousingsByFilters(city, dateIn, dateOut, min, max, pageable);

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
        
        try {
            // Intentar primero con query nativa para evitar problemas con relaciones lazy
            try {
                Page<Object[]> nativeResults = housingRepository.findByHostIdNative(hostId, pageable);
                
                if (nativeResults != null && !nativeResults.isEmpty()) {
                    return nativeResults.map(row -> {
                        Long id = ((Number) row[0]).longValue();
                        String title = (String) row[1];
                        String city = (String) row[2];
                        Double nightPrice = row[3] != null ? ((Number) row[3]).doubleValue() : 0.0;
                        String principalImage = (String) row[4];
                        Double averageRating = row[5] != null ? ((Number) row[5]).doubleValue() : null;
                        
                        return new SummaryHousingResponse(
                            id,
                            title != null ? title : "Untitled",
                            city != null ? city : "Unknown",
                            nightPrice,
                            principalImage,
                            averageRating
                        );
                    });
                }
            } catch (Exception nativeEx) {
                log.warn("Native query failed, falling back to JPQL: {}", nativeEx.getMessage());
            }
            
            // Fallback a query JPQL si la nativa falla
            Page<Housing> housings = housingRepository.findByHostId(hostId, pageable);
            
            if (housings == null || housings.isEmpty()) {
                return Page.empty(pageable);
            }
            
            // Mapear de forma segura, creando el DTO manualmente para evitar problemas con relaciones lazy
            return housings.map(housing -> {
                return new SummaryHousingResponse(
                    housing.getId() != null ? housing.getId() : 0L,
                    housing.getTitle() != null ? housing.getTitle() : "Untitled",
                    housing.getCity() != null ? housing.getCity() : "Unknown",
                    housing.getNightPrice() != null ? housing.getNightPrice() : 0.0,
                    housing.getPrincipalImage(),
                    housing.getAverageRating()
                );
            });
        } catch (Exception e) {
            log.error("Error fetching housings for host {}: {}", hostId, e.getMessage(), e);
            // Retornar página vacía en lugar de lanzar excepción
            return Page.empty(pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public HousingResponse getHousingDetail(Long housingId) {
        if (housingId == null || housingId <= 0) {
            throw new IllegalArgumentException("Housing ID must be positive");
        }

        log.info("Attempting to fetch housing detail for ID: {}", housingId);

        try {
            // Intentar primero con query nativa para evitar problemas con ElementCollection
            java.util.Optional<Object[]> nativeResult = null;
            try {
                nativeResult = housingRepository.findByIdNative(housingId);
                log.debug("Native query result for housing {}: {}", housingId, nativeResult.isPresent() ? "found" : "not found");
            } catch (Exception e) {
                log.warn("Native query failed for housing {}: {}", housingId, e.getMessage(), e);
            }
            
            if (nativeResult != null && nativeResult.isPresent()) {
                log.info("Using native query result for housing {}", housingId);
                Object[] row = nativeResult.get();
                
                // Verificar el estado de la propiedad (índice 10 en el array)
                String state = row[10] != null ? (String) row[10] : null;
                log.info("Housing {} state: '{}'", housingId, state);
                
                // Si la propiedad está eliminada, lanzar excepción
                if (state != null && state.equals("deleted")) {
                    log.warn("Housing {} is deleted (state='deleted'), cannot retrieve details", housingId);
                    throw new ObjectNotFoundException("Housing with id: " + housingId + " not found (may be deleted)", Housing.class);
                }
                
                // Construir el DTO desde los resultados nativos
                HousingResponse response = new HousingResponse();
                
                // Mapear campos desde el array de resultados
                // Orden: id, title, description, city, address, latitude, length, 
                //        night_price, max_capacity, principal_image, state, average_rating, host_id
                response.setTitle((String) row[1]);
                response.setDescription((String) row[2]);
                response.setCity((String) row[3]);
                response.setAddress((String) row[4]);
                response.setLatitude(row[5] != null ? ((Number) row[5]).doubleValue() : null);
                response.setLength(row[6] != null ? ((Number) row[6]).doubleValue() : null);
                response.setNightPrice(row[7] != null ? ((Number) row[7]).doubleValue() : null);
                response.setMaxCapacity(row[8] != null ? ((Number) row[8]).intValue() : null);
                response.setAverageRating(row[11] != null ? ((Number) row[11]).doubleValue() : null);
                
                // Obtener servicios e imágenes con queries separadas (pueden fallar si las tablas tienen nombres diferentes)
                // Intentar cargar servicios
                try {
                    List<String> serviceStrings = housingRepository.findServicesByHousingId(housingId);
                    if (serviceStrings != null && !serviceStrings.isEmpty()) {
                        List<co.edu.uniquindio.application.Models.enums.ServicesEnum> services = serviceStrings.stream()
                            .map(s -> {
                                try {
                                    return co.edu.uniquindio.application.Models.enums.ServicesEnum.valueOf(s);
                                } catch (IllegalArgumentException e) {
                                    log.warn("Invalid service enum value: {}", s);
                                    return null;
                                }
                            })
                            .filter(s -> s != null)
                            .collect(java.util.stream.Collectors.toList());
                        response.setServices(services);
                    } else {
                        response.setServices(new ArrayList<>());
                    }
                } catch (Exception e) {
                    // Si falla la query, usar lista vacía (las tablas pueden tener nombres diferentes)
                    log.debug("Could not load services via native query for housing {}: {}", housingId, e.getMessage());
                    response.setServices(new ArrayList<>());
                }
                
                // Intentar cargar imágenes
                try {
                    List<String> images = housingRepository.findImagesByHousingId(housingId);
                    if (images != null && !images.isEmpty()) {
                        response.setImages(new ArrayList<>(images));
                    } else {
                        // Si no hay imágenes en la tabla separada, usar principalImage
                        String principalImage = (String) row[9];
                        if (principalImage != null && !principalImage.trim().isEmpty()) {
                            response.setImages(new ArrayList<>(List.of(principalImage)));
                        } else {
                            response.setImages(new ArrayList<>());
                        }
                    }
                } catch (Exception e) {
                    // Si falla la query, usar principalImage como fallback
                    log.debug("Could not load images via native query for housing {}: {}", housingId, e.getMessage());
                    String principalImage = (String) row[9];
                    if (principalImage != null && !principalImage.trim().isEmpty()) {
                        response.setImages(new ArrayList<>(List.of(principalImage)));
                    } else {
                        response.setImages(new ArrayList<>());
                    }
                }
                
                // NO incluir bookingsList y commentsList
                response.setBookingsList(null);
                response.setCommentsList(null);
                
                // Obtener el nombre del host
                Long hostId = row[12] != null ? ((Number) row[12]).longValue() : null;
                try {
                    if (hostId != null) {
                        User user = userService.findById(hostId);
                        if (user != null && user.getName() != null) {
                            response.setHostName(user.getName());
                        } else {
                            response.setHostName("Host");
                        }
                    } else {
                        response.setHostName("Host");
                    }
                } catch (Exception e) {
                    log.warn("Error getting host name for housing {}: {}", housingId, e.getMessage());
                    response.setHostName("Host");
                }
                
                return response;
            }
            
            // Fallback: usar findById pero con manejo especial para evitar ElementCollection
            log.info("Native query did not return results, trying findById for housing {}", housingId);
            Housing housing = null;
            try {
                // Verificar primero si existe
                boolean exists = housingRepository.existsById(housingId);
                log.info("Housing {} exists in database: {}", housingId, exists);
                
                if (!exists) {
                    log.warn("Housing with id {} does not exist in database", housingId);
                    throw new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class);
                }
                
                // Intentar cargar sin inicializar relaciones usando la query específica
                housing = housingRepository.findByIdWithoutRelations(housingId).orElse(null);
                
                if (housing == null) {
                    // Si la query específica falla, intentar con findById normal
                    log.debug("findByIdWithoutRelations returned null, trying findById");
                    housing = housingRepository.findById(housingId).orElse(null);
                }
                
                if (housing == null) {
                    log.error("Both findByIdWithoutRelations and findById returned null for housing {}", housingId);
                    throw new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class);
                }
                
                log.info("Successfully loaded housing entity for id {}", housingId);
            } catch (ObjectNotFoundException e) {
                throw e;
            } catch (Exception e) {
                log.error("Error loading housing entity for id {}: {}", housingId, e.getMessage(), e);
                throw new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class);
            }

            // Construir el DTO manualmente, evitando acceder a ElementCollection directamente
            HousingResponse response = new HousingResponse();
            
            // Copiar campos básicos de forma segura
            try {
                response.setTitle(housing.getTitle());
                response.setDescription(housing.getDescription());
                response.setCity(housing.getCity());
                response.setAddress(housing.getAddress());
                response.setLatitude(housing.getLatitude());
                response.setLength(housing.getLength());
                response.setNightPrice(housing.getNightPrice());
                response.setMaxCapacity(housing.getMaxCapacity());
                response.setAverageRating(housing.getAverageRating());
            } catch (Exception e) {
                log.error("Error copying basic fields for housing {}: {}", housingId, e.getMessage());
                throw new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class);
            }
            
            // Para servicios e imágenes, usar queries separadas o valores por defecto
            // NO intentar acceder directamente a getServices() o getImages() ya que pueden causar problemas
            response.setServices(new ArrayList<>()); // Inicializar vacío, se puede poblar después si es necesario
            response.setImages(new ArrayList<>()); // Inicializar vacío
            
            // Intentar obtener servicios desde la tabla separada
            try {
                List<String> serviceStrings = housingRepository.findServicesByHousingId(housingId);
                if (serviceStrings != null && !serviceStrings.isEmpty()) {
                    List<co.edu.uniquindio.application.Models.enums.ServicesEnum> services = serviceStrings.stream()
                        .map(s -> {
                            try {
                                return co.edu.uniquindio.application.Models.enums.ServicesEnum.valueOf(s);
                            } catch (IllegalArgumentException e) {
                                return null;
                            }
                        })
                        .filter(s -> s != null)
                        .collect(java.util.stream.Collectors.toList());
                    response.setServices(services);
                }
            } catch (Exception e) {
                log.debug("Could not load services for housing {}: {}", housingId, e.getMessage());
            }
            
            // Intentar obtener imágenes desde la tabla separada
            try {
                List<String> images = housingRepository.findImagesByHousingId(housingId);
                if (images != null && !images.isEmpty()) {
                    response.setImages(new ArrayList<>(images));
                } else {
                    // Intentar obtener principalImage de forma segura usando reflexión o query
                    try {
                        // Usar query nativa para obtener solo principalImage sin cargar colecciones
                        String principalImage = housingRepository.findByIdNative(housingId)
                            .map(row -> (String) row[9]) // principal_image está en posición 9
                            .orElse(null);
                        if (principalImage != null && !principalImage.trim().isEmpty()) {
                            response.setImages(new ArrayList<>(List.of(principalImage)));
                        }
                    } catch (Exception ex) {
                        log.debug("Could not access principalImage: {}", ex.getMessage());
                    }
                }
            } catch (Exception e) {
                log.debug("Could not load images for housing {}: {}", housingId, e.getMessage());
                // Fallback: intentar obtener principalImage desde query nativa
                try {
                    String principalImage = housingRepository.findByIdNative(housingId)
                        .map(row -> (String) row[9])
                        .orElse(null);
                    if (principalImage != null && !principalImage.trim().isEmpty()) {
                        response.setImages(new ArrayList<>(List.of(principalImage)));
                    }
                } catch (Exception ex) {
                    log.debug("Could not access principalImage via native query: {}", ex.getMessage());
                }
            }
            
            response.setBookingsList(null);
            response.setCommentsList(null);

            // Obtener el nombre del host
            try {
                if (housing.getHostId() != null) {
                    User user = userService.findById(housing.getHostId());
                    if (user != null && user.getName() != null) {
                        response.setHostName(user.getName());
                    } else {
                        response.setHostName("Host");
                    }
                } else {
                    response.setHostName("Host");
                }
            } catch (Exception e) {
                log.warn("Error getting host name for housing {}: {}", housingId, e.getMessage());
                response.setHostName("Host");
            }

            return response;
        } catch (ObjectNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error fetching housing detail for id {}: {}", housingId, e.getMessage(), e);
            throw new ObjectNotFoundException("Housing with id: " + housingId + " not found", Housing.class);
        }
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

        return housingRepository.existsByIdAndHostId(housingId, hostId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public HousingMetricsResponse getHousingMetrics(Long housingId, Long hostId, LocalDate dateFrom, LocalDate dateTo) {
        // Verificar que el alojamiento existe y pertenece al host
        if (!existsHousing(housingId, hostId)) {
            throw new ObjectNotFoundException("Housing with id: " + housingId + " and hostId: " + hostId + " not found", Housing.class);
        }
        
        Housing housing = findById(housingId);
        
        // Contar reservas en el rango de fechas
        Long totalBookings = bookingRepository.countBookingsByHousingAndDateRange(housingId, dateFrom, dateTo);
        if (totalBookings == null) {
            totalBookings = 0L;
        }
        
        // Calcular promedio de calificaciones
        Double averageRating = null;
        
        // Si no hay filtros de fecha, usar el promedio del alojamiento directamente
        if (dateFrom == null && dateTo == null) {
            averageRating = housing.getAverageRating() != null ? housing.getAverageRating() : 0.0;
        } else {
            // Si hay filtros de fecha, calcular manualmente filtrando en memoria
            try {
                List<Comment> allComments = commentRepository.findAllByHousingId(housingId);
                if (!allComments.isEmpty()) {
                    java.time.LocalDate dateFromLocal = dateFrom;
                    java.time.LocalDate dateToLocal = dateTo;
                    
                    List<Comment> filteredComments = allComments.stream()
                        .filter(c -> {
                            if (dateFromLocal != null && c.getCreatedAt().toLocalDate().isBefore(dateFromLocal)) {
                                return false;
                            }
                            if (dateToLocal != null && c.getCreatedAt().toLocalDate().isAfter(dateToLocal)) {
                                return false;
                            }
                            return true;
                        })
                        .collect(java.util.stream.Collectors.toList());
                    
                    if (!filteredComments.isEmpty()) {
                        averageRating = filteredComments.stream()
                            .mapToInt(Comment::getRate)
                            .average()
                            .orElse(0.0);
                    }
                }
            } catch (Exception e) {
                // Si hay error, usar el promedio del alojamiento
                averageRating = null;
            }
        }
        
        // Si no hay calificaciones o hubo error, usar el promedio general del alojamiento
        if (averageRating == null || averageRating == 0.0) {
            averageRating = housing.getAverageRating() != null ? housing.getAverageRating() : 0.0;
        }
        
        return new HousingMetricsResponse(
            housingId,
            housing.getTitle(),
            totalBookings != null ? totalBookings : 0L,
            averageRating,
            dateFrom,
            dateTo
        );
    }
    
    @Override
    @Transactional(readOnly = true)
    public AvailabilityCalendarResponse getAvailabilityCalendar(Long housingId, LocalDate startDate, LocalDate endDate) {
        Housing housing = findById(housingId);
        
        // Si no se proporcionan fechas, usar el próximo año
        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (endDate == null) {
            endDate = startDate.plusYears(1);
        }
        
        // Obtener rangos de fechas ocupadas
        List<Object[]> bookedRanges = bookingRepository.findBookedDateRanges(housingId, startDate, endDate);
        
        // Generar lista de todas las fechas ocupadas
        Set<LocalDate> bookedDates = new HashSet<>();
        for (Object[] range : bookedRanges) {
            LocalDate checkIn = (LocalDate) range[0];
            LocalDate checkOut = (LocalDate) range[1];
            
            // Agregar todas las fechas entre checkIn (inclusive) y checkOut (exclusive)
            LocalDate current = checkIn;
            while (current.isBefore(checkOut) && !current.isAfter(endDate) && !current.isBefore(startDate)) {
                bookedDates.add(current);
                current = current.plusDays(1);
            }
        }
        
        // Generar lista de fechas disponibles
        List<LocalDate> availableDates = new ArrayList<>();
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            if (!bookedDates.contains(current)) {
                availableDates.add(current);
            }
            current = current.plusDays(1);
        }
        
        return new AvailabilityCalendarResponse(
            housingId,
            housing.getTitle(),
            new ArrayList<>(bookedDates),
            availableDates
        );
    }
}


