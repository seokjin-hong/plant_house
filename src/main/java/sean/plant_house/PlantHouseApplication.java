package sean.plant_house;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class PlantHouseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlantHouseApplication.class, args);
    }

}
