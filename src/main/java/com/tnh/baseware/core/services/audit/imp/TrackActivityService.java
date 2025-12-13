package com.tnh.baseware.core.services.audit.imp;

import com.tnh.baseware.core.dtos.audit.TrackActivityDTO;
import com.tnh.baseware.core.dtos.audit.TrackActivityDetailDTO;
import com.tnh.baseware.core.entities.audit.TrackActivity;
import com.tnh.baseware.core.exceptions.BWCNotFoundException;
import com.tnh.baseware.core.forms.audit.TrackActivityEditorForm;
import com.tnh.baseware.core.mappers.audit.ITrackActivityMapper;
import com.tnh.baseware.core.properties.SecurityProperties;
import com.tnh.baseware.core.repositories.audit.ITrackActivityRepository;
import com.tnh.baseware.core.services.GenericService;
import com.tnh.baseware.core.services.MessageService;
import com.tnh.baseware.core.services.audit.ITrackActivityService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class TrackActivityService extends
        GenericService<TrackActivity, TrackActivityEditorForm, TrackActivityDTO, ITrackActivityRepository, ITrackActivityMapper, UUID>
        implements
        ITrackActivityService {

    SecurityProperties securityProperties;

    public TrackActivityService(ITrackActivityRepository repository,
                                ITrackActivityMapper mapper,
                                MessageService messageService,
                                SecurityProperties securityProperties) {
        super(repository, mapper, messageService, TrackActivity.class);
        this.securityProperties = securityProperties;
    }

    @Override
    @Transactional(readOnly = true)
    public TrackActivityDetailDTO getTrackActivityDetail(UUID id) {
        return repository.getTrackActivityDetail(id)
                .orElseThrow(() ->
                        new BWCNotFoundException(messageService.getMessage("track.activity.not.found", id)));
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void trackLogin(String ip, String device, String username) {
        var trackActivity = TrackActivity.builder()
                .requestUrl(securityProperties.getLogin().getUrlLogin())
                .method("POST")
                .status(200)
                .ipAddress(ip)
                .deviceInfo(device)
                .username(username)
                .actionDate(Instant.now())
                .responsePayload(messageService.getMessage("track.activity.login"))
                .build();

        repository.save(trackActivity);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void trackLogout(String ip, String device, String username) {
        var trackActivity = TrackActivity.builder()
                .requestUrl(securityProperties.getLogin().getUrlLogout())
                .method("POST")
                .status(200)
                .ipAddress(ip)
                .deviceInfo(device)
                .username(username)
                .actionDate(Instant.now())
                .responsePayload(messageService.getMessage("track.activity.logout"))
                .build();

        repository.save(trackActivity);
    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void trackRefreshToken(String ip, String device, String username) {
        var trackActivity = TrackActivity.builder()
                .requestUrl(securityProperties.getLogin().getUrlRefreshToken())
                .method("POST")
                .status(200)
                .ipAddress(ip)
                .deviceInfo(device)
                .username(username)
                .actionDate(Instant.now())
                .responsePayload(messageService.getMessage("track.activity.refresh.token"))
                .build();

        repository.save(trackActivity);
    }
}
