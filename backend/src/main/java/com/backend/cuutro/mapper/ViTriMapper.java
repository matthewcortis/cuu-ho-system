package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.ViTriDto;
import com.backend.cuutro.entities.ViTriEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring")
public interface ViTriMapper {

    ViTriDto toDto(ViTriEntity entity);

    ViTriEntity toEntity(ViTriDto dto);

    List<ViTriDto> toDtoList(List<ViTriEntity> entityList);

    List<ViTriEntity> toEntityList(List<ViTriDto> dtoList);

    default Page<ViTriDto> toDtoPage(Page<ViTriEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
