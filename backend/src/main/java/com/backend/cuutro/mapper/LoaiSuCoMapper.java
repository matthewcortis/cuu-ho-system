package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.LoaiSuCoDto;
import com.backend.cuutro.entities.LoaiSuCoEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LoaiSuCoMapper {

    LoaiSuCoDto toDto(LoaiSuCoEntity entity);

    LoaiSuCoEntity toEntity(LoaiSuCoDto dto);

    List<LoaiSuCoDto> toDtoList(List<LoaiSuCoEntity> entityList);

    List<LoaiSuCoEntity> toEntityList(List<LoaiSuCoDto> dtoList);

    default Page<LoaiSuCoDto> toDtoPage(Page<LoaiSuCoEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
