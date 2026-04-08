package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.TinNhanDto;
import com.backend.cuutro.entities.TinNhanEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PhieuCuuTroMapper.class, NguoiDungMapper.class})
public interface TinNhanMapper {

    TinNhanDto toDto(TinNhanEntity entity);

    TinNhanEntity toEntity(TinNhanDto dto);

    List<TinNhanDto> toDtoList(List<TinNhanEntity> entityList);

    List<TinNhanEntity> toEntityList(List<TinNhanDto> dtoList);

    default Page<TinNhanDto> toDtoPage(Page<TinNhanEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
