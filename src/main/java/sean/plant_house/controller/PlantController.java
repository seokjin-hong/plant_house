package sean.plant_house.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sean.plant_house.entity.Plant;
import sean.plant_house.service.PlantService;

@Controller
@RequestMapping("/plants")
@RequiredArgsConstructor
public class PlantController {

    private final PlantService plantService;

    @GetMapping
    public String listPlants(Model model) {
        model.addAttribute("plants", plantService.getAllPlants());
        return "plants/list";
    }

    @GetMapping("/new")
    public String newPlantForm(Model model) {
        model.addAttribute("plant", new Plant());
        model.addAttribute("isEdit", false);
        return "plants/form";
    }

    @GetMapping("/edit/{id}")
    public String editPlantForm(@PathVariable Long id, Model model) {
        Plant plant = plantService.getPlantById(id);
        model.addAttribute("plant", plant);
        model.addAttribute("isEdit", true);
        return "plants/form";
    }

    @PostMapping("/save")
    public String savePlant(@ModelAttribute Plant plant, RedirectAttributes redirectAttributes) {
        plantService.savePlant(plant);
        redirectAttributes.addFlashAttribute("message", "식물이 성공적으로 저장되었습니다.");
        return "redirect:/plants";
    }

    @PostMapping("/delete/{id}")
    public String deletePlant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        plantService.deletePlant(id);
        redirectAttributes.addFlashAttribute("message", "식물이 성공적으로 삭제되었습니다.");
        return "redirect:/plants";
    }
}