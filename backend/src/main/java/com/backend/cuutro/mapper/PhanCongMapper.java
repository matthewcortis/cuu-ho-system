package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.PhanCongDto;
import com.backend.cuutro.entities.PhanCongEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {PhieuCuuTroMapper.class, DoiNhomMapper.class})
public interface PhanCongMapper {

    PhanCongDto toDto(PhanCongEntity entity);

    PhanCongEntity toEntity(PhanCongDto dto);

    List<PhanCongDto> toDtoList(List<PhanCongEntity> entityList);

    List<PhanCongEntity> toEntityList(List<PhanCongDto> dtoList);

    default Page<PhanCongDto> toDtoPage(Page<PhanCongEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
