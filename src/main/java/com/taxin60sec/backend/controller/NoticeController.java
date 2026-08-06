package com.taxin60sec.backend.controller;

import com.taxin60sec.backend.common.ApiResponse;
import com.taxin60sec.backend.common.PageResponse;
import com.taxin60sec.backend.dto.domain.NoticeDto;
import com.taxin60sec.backend.security.UserPrincipal;
import com.taxin60sec.backend.service.impl.NoticeService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/notices")
@PreAuthorize("hasAnyRole('CLIENT','CA','ADMIN')")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @GetMapping
    public ApiResponse<PageResponse<NoticeDto>> list(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.success("Notices", noticeService.listForUser(principal.getId(), page, size), null);
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Long>> unreadCount(@AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Unread notice count", Map.of("unread", noticeService.unreadCount(principal.getId())), null);
    }

    @PostMapping("/{id}/read")
    public ApiResponse<NoticeDto> markRead(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ApiResponse.success("Notice marked read", noticeService.markRead(id, principal.getId()), null);
    }

    @PostMapping("/read-all")
    public ApiResponse<Void> markAllRead(@AuthenticationPrincipal UserPrincipal principal) {
        noticeService.markAllRead(principal.getId());
        return ApiResponse.success("All notices marked read", null, null);
    }
}