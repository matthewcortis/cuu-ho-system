package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.DoiNhomDto;
import com.backend.cuutro.entities.DoiNhomEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ViTriMapper.class})
public interface DoiNhomMapper {

    DoiNhomDto toDto(DoiNhomEntity entity);

    DoiNhomEntity toEntity(DoiNhomDto dto);

    List<DoiNhomDto> toDtoList(List<DoiNhomEntity> entityList);

    List<DoiNhomEntity> toEntityList(List<DoiNhomDto> dtoList);

    default Page<DoiNhomDto> toDtoPage(Page<DoiNhomEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
