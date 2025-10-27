package com.rideshare.userservice.config;

import com.rideshare.userservice.dto.UserDto;
import com.rideshare.userservice.entity.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration.AccessLevel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration()
                .setFieldMatchingEnabled(true)
                .setFieldAccessLevel(AccessLevel.PRIVATE);

        // 👇 Custom mapping for Role.name → UserDto.role
        mapper.typeMap(User.class, UserDto.class)
                .addMappings(m -> m.map(src -> src.getRole().getName(), UserDto::setRole));
        return mapper;
    }
}
