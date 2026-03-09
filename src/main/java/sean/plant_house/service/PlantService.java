package sean.plant_house.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sean.plant_house.entity.Plant;
import sean.plant_house.repository.PlantRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlantService {

    private final PlantRepository plantRepository;

    public List<Plant> getAllPlants() {
        return plantRepository.findByDeletedFalse();
    }

    public Plant getPlantById(Long id) {
        return plantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Plant not found: " + id));
    }

    @Transactional
    public Plant savePlant(Plant plant) {
        // 생성자와 수정자는 0으로 설정
        if (plant.getId() == null) {
            plant.setCreatedBy("0");
        }
        plant.setUpdatedBy("0");
        return plantRepository.save(plant);
    }

    @Transactional
    public void deletePlant(Long id) {
        Plant plant = getPlantById(id);
        plant.setDeleted(true);
        plant.setUpdatedBy("0");
        plantRepository.save(plant);
    }
}