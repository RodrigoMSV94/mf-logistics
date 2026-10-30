package com.mflogistica.web.controller;

import com.mflogistica.application.dto.FiscalDataRequestDto;
import com.mflogistica.application.service.FiscalDataService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/dashboard/datos-fiscales")
public class FiscalDataController {

    private final FiscalDataService fiscalDataService;

    public FiscalDataController(FiscalDataService fiscalDataService) {
        this.fiscalDataService = fiscalDataService;
    }

    @GetMapping
    public String showForm(@RequestParam(defaultValue = "false") boolean guardado, Model model) {
        model.addAttribute("fiscalData", FiscalDataRequestDto.from(fiscalDataService.getCurrent()));
        model.addAttribute("saved", guardado);
        return "datos-fiscales";
    }

    @PostMapping
    public String save(@Valid @ModelAttribute("fiscalData") FiscalDataRequestDto fiscalData,
                       BindingResult bindingResult,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("saved", false);
            return "datos-fiscales";
        }

        fiscalDataService.save(fiscalData.toDomain());
        redirectAttributes.addAttribute("guardado", true);
        return "redirect:/dashboard/datos-fiscales";
    }
}
