package com.taxin60sec.backend.service.impl;

import com.taxin60sec.backend.common.ApiErrorCode;
import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.dto.domain.NoticeDto;
import com.taxin60sec.backend.entity.Case;
import com.taxin60sec.backend.entity.Notice;
import com.taxin60sec.backend.entity.User;
import com.taxin60sec.backend.entity.enums.NoticeSeverity;
import com.taxin60sec.backend.entity.enums.NoticeType;
import com.taxin60sec.backend.exception.ApiException;
import com.taxin60sec.backend.mapper.NoticeMapper;
import com.taxin60sec.backend.repository.NoticeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Central place for creating and reading in-app notices. Other services (compliance reminders,
 * document review, case assignment, payments) call create(...) so the "Notices" panel in the
 * dashboard has something to show; this service itself doesn't know about those triggers.
 */
@Service
@Transactional
public class NoticeService {

    private final NoticeRepository notices;
    private final NoticeMapper mapper;

    public NoticeService(NoticeRepository notices, NoticeMapper mapper) {
        this.notices = notices;
        this.mapper = mapper;
    }

    public Notice create(User user, NoticeType type, NoticeSeverity severity, String title, String message, Case relatedCase) {
        Notice n = new Notice();
        n.setUser(user);
        n.setType(type);
        n.setSeverity(severity);
        n.setTitle(title);
        n.setMessage(message);
        n.setRelatedCase(relatedCase);
        return notices.save(n);
    }

    public PageResponse<NoticeDto> listForUser(Long userId, int page, int size) {
        var result = notices.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(
                userId, PageRequest.of(Math.max(page, 0), size <= 0 ? 20 : size)
        );
        return new PageResponse<>(
                result.getContent().stream().map(mapper::toDto).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages()
        );
    }

    public long unreadCount(Long userId) {
        return notices.countByUserIdAndDeletedFalseAndReadFalse(userId);
    }

    public NoticeDto markRead(Long id, Long userId) {
        Notice n = notices.findById(id)
                .filter(x -> !x.isDeleted())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, "Notice not found"));
        if (!n.getUser().getId().equals(userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, ApiErrorCode.FORBIDDEN, "Notice does not belong to current user");
        }
        if (!n.isRead()) {
            n.setRead(true);
            n.setReadAt(Instant.now());
        }
        return mapper.toDto(n);
    }

    public void markAllRead(Long userId) {
        notices.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId, PageRequest.of(0, 500))
                .forEach(n -> {
                    if (!n.isRead()) {
                        n.setRead(true);
                        n.setReadAt(Instant.now());
                    }
                });
    }
}