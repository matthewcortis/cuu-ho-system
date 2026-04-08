package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.DonViDto;
import com.backend.cuutro.entities.DonViEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring")
public interface DonViMapper {

    DonViDto toDto(DonViEntity entity);

    DonViEntity toEntity(DonViDto dto);

    List<DonViDto> toDtoList(List<DonViEntity> entityList);

    List<DonViEntity> toEntityList(List<DonViDto> dtoList);

    default Page<DonViDto> toDtoPage(Page<DonViEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
