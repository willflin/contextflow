package com.contextflow.learning;

import com.contextflow.common.api.ApiResponse;
import com.contextflow.common.api.PageResponse;
import com.contextflow.learning.dto.AdminDialogueTurnResponse;
import com.contextflow.learning.dto.AdminDialogueTurnUpdateRequest;
import com.contextflow.learning.service.AdminDialogueTurnService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dialogues")
public class AdminDialogueTurnController {

    private final AdminDialogueTurnService adminDialogueTurnService;

    public AdminDialogueTurnController(AdminDialogueTurnService adminDialogueTurnService) {
        this.adminDialogueTurnService = adminDialogueTurnService;
    }

    @GetMapping
    public ApiResponse<PageResponse<AdminDialogueTurnResponse>> list(
            @RequestParam(required = false) Long packageId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.ok(adminDialogueTurnService.list(packageId, page, size));
    }

    @PutMapping("/{id}")
    public ApiResponse<AdminDialogueTurnResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody AdminDialogueTurnUpdateRequest request
    ) {
        return ApiResponse.ok(adminDialogueTurnService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminDialogueTurnService.delete(id);
        return ApiResponse.ok(null);
    }
}
