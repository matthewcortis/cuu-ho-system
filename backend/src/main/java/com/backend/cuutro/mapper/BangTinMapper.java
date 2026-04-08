package com.backend.cuutro.mapper;

import com.backend.cuutro.dto.response.entities.BangTinDto;
import com.backend.cuutro.entities.BangTinEntity;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;

import java.util.List;

@Mapper(componentModel = "spring", uses = {TepTinMapper.class, ViTriMapper.class, NguoiDungMapper.class})
public interface BangTinMapper {

    BangTinDto toDto(BangTinEntity entity);

    BangTinEntity toEntity(BangTinDto dto);

    List<BangTinDto> toDtoList(List<BangTinEntity> entityList);

    List<BangTinEntity> toEntityList(List<BangTinDto> dtoList);

    default Page<BangTinDto> toDtoPage(Page<BangTinEntity> entityPage){
        if(entityPage == null){
            return Page.empty();
        }
        return entityPage.map(this::toDto);
    }
}
