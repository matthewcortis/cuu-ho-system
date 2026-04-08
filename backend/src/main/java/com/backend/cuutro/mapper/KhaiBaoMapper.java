package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.KhaiBaoDto;
import com.backend.cuutro.entities.KhaiBaoEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring")
public interface KhaiBaoMapper {

    KhaiBaoDto toDto(KhaiBaoEntity entity);

    KhaiBaoEntity toEntity(KhaiBaoDto dto);

    List<KhaiBaoDto> toDtoList(List<KhaiBaoEntity> entityList);

    List<KhaiBaoEntity> toEntityList(List<KhaiBaoDto> dtoList);

    default Page<KhaiBaoDto> toDtoPage(Page<KhaiBaoEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
