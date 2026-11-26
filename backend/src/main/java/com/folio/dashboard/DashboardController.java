package com.folio.dashboard;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final DashboardService dashboard;

    public DashboardController(DashboardService dashboard) {
        this.dashboard = dashboard;
    }

    @GetMapping("/summary")
    public DashboardService.Summary summary() {
        return dashboard.summary();
    }

    @GetMapping("/loan-trend")
    public List<DashboardService.TrendPoint> loanTrend(@RequestParam(defaultValue = "30") @Min(7) @Max(365) int days) {
        return dashboard.loanTrend(days);
    }

    @GetMapping("/top-books")
    public List<DashboardService.TopBook> topBooks(@RequestParam(defaultValue = "5") @Min(1) @Max(20) int limit,
                                                   @RequestParam(defaultValue = "90") @Min(7) @Max(365) int days) {
        return dashboard.topBooks(limit, days);
    }

    @GetMapping("/genre-distribution")
    public List<DashboardService.GenreShare> genreDistribution() {
        return dashboard.genreDistribution();
    }
}
