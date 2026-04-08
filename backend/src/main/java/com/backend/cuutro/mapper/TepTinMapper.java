package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.TepTinDto;
import com.backend.cuutro.entities.TepTinEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;


@Mapper(componentModel = "spring")
public interface TepTinMapper {

    TepTinDto toDto(TepTinEntity entity);

    TepTinEntity toEntity(TepTinDto dto);

    List<TepTinDto> toDtoList(List<TepTinEntity> entityList);

    List<TepTinEntity> toEntityList(List<TepTinDto> dtoList);

    default Page<TepTinDto> toDtoPage(Page<TepTinEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
