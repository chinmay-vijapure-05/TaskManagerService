package com.example.TaskManagementService.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(
        name = "PagedResponse",
        description = "Generic paginated response wrapper"
)
public class PagedResponse<T> {

    @ArraySchema(
            schema = @Schema(description = "List of items for the current page")
    )
    private List<T> content;

    @Schema(description = "Current page number (0-based index)", example = "0")
    private int pageNumber;

    @Schema(description = "Number of elements per page", example = "10")
    private int pageSize;

    @Schema(description = "Total number of elements across all pages", example = "57")
    private long totalElements;

    @Schema(description = "Total number of available pages", example = "6")
    private int totalPages;

    @Schema(description = "Indicates if this is the last page", example = "false")
    private boolean last;

    @Schema(description = "Indicates if this is the first page", example = "true")
    private boolean first;

    public PagedResponse(List<T> content, org.springframework.data.domain.Page<?> page) {
        this.content = content;
        this.pageNumber = page.getNumber();
        this.pageSize = page.getSize();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.last = page.isLast();
        this.first = page.isFirst();
    }
}
