package br.com.minietl.web;

import br.com.minietl.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("dashboard", dashboardService.montar());
        model.addAttribute("ativo", "dashboard");
        return "dashboard";
    }
}
